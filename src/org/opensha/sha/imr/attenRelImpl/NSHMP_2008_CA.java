package org.opensha.sha.imr.attenRelImpl;

import static com.google.common.base.Preconditions.*;
import static org.opensha.sha.imr.PropagationEffect.*;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.lang.StringUtils;
import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.exceptions.ConstraintException;
import org.opensha.commons.exceptions.IMRException;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.geo.RegionUtils;
import org.opensha.commons.param.BooleanParameter;
import org.opensha.commons.param.DependentParameterAPI;
import org.opensha.commons.param.DoubleDiscreteConstraint;
import org.opensha.commons.param.DoubleParameter;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.StringConstraint;
import org.opensha.commons.param.StringParameter;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.event.ParameterChangeWarningListener;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.rupForecastImpl.PointEqkSource;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.PropagationEffect;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.param.IntensityMeasureParams.DampingParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGV_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.ComponentParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncLevelParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncTypeParam;
import org.opensha.sha.imr.param.OtherParams.StdDevTypeParam;
import org.opensha.sha.imr.param.OtherParams.TectonicRegionTypeParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.imr.param.SiteParams.Vs30_TypeParam;

/**
 * This is an implementation of the combined attenuation relationships used
 * in California for the 2008 National Seismic Hazard Mapping Program (NSHMP).
 * Please take note of the following implementation details:
 * <ul>
 * <li></li>
 * </ul>
 * 
 * <pre>
 *             M<6      6≤M<7      7≤M
 *          =============================
 *   D<10     0.375  |  0.210  |  0.245
 * 10≤D<30    0.230  |  0.225  |  0.230
 *   30≤D     0.400  |  0.360  |  0.310
 *          =============================
 * </pre>
 * 
 * weights 0.185 0.630 0.185
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class NSHMP_2008_CA extends AttenuationRelationship implements
ParameterChangeListener {

	public final static String NAME = "NSHMP 2008 California Combined";
	public final static String SHORT_NAME = "NSHMP_2008_CA";
	private static final long serialVersionUID = 1L;

	// Wrapped Attenuation Relationship instances
	private NSHMP_BA_2008 ba08;
	private NSHMP_CB_2008 cb08;
	private NSHMP_CY_2008 cy08;
	private List<AttenuationRelationship> arList;

	// this is the minimum range of vs30 spanned by BA, CB, & CY
	private final static double VS30_WARN_MIN = 80;
	private final static double VS30_WARN_MAX = 1300;
	
	// custom params
	private static final String IMR_UNCERT_PARAM_NAME = "IMR uncertainty";
	private boolean includeImrUncert = true;
	private BooleanParameter ptSrcCorrParam;

	// flags and tuning values
	private double weight = 1.0;
	private double imrUncert = 0.0;
	
	public NSHMP_2008_CA(ParameterChangeWarningListener listener) {

		ba08 = new NSHMP_BA_2008(listener);
		cb08 = new NSHMP_CB_2008(listener);
		cy08 = new NSHMP_CY_2008(listener);

		arList = new ArrayList<AttenuationRelationship>();
		arList.add(ba08);
		arList.add(cb08);
		arList.add(cy08);

		propEffect = new PropagationEffect();
		
		// these methods are called for each attenRel upon construction; we
		// do some local cloning so that a minimal set of params may be
		// exposed and modified in gui's and/or to ensure some parameters
		// adhere to NSHMP values
		initSupportedIntensityMeasureParams();
		initSiteParams();
		initOtherParams();
		initParameterEventListeners();
	}

	@Override
	public void setParamDefaults() {
		vs30Param.setValueAsDefault();
		saParam.setValueAsDefault();
		saPeriodParam.setValueAsDefault();
		saDampingParam.setValueAsDefault();
		pgaParam.setValueAsDefault();
		pgvParam.setValueAsDefault();
		
		for (ScalarIntensityMeasureRelationshipAPI ar : arList) {
			ar.setParamDefaults();
		}
	}

	@Override
	public String getShortName() {
		return SHORT_NAME;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected void setPropagationEffectParams() {}

	@Override
	protected void initSupportedIntensityMeasureParams() {
		// clone periods from ba08
		DoubleDiscreteConstraint periodConstraint = new DoubleDiscreteConstraint(
			((SA_Param) ba08.getSupportedIntensityMeasuresList().getParameter(
				SA_Param.NAME)).getPeriodParam().getAllowedDoubles());
		periodConstraint.setNonEditable();
		saPeriodParam = new PeriodParam(periodConstraint, 1.0, false);
		saPeriodParam.addParameterChangeListener(this);
		saDampingParam = new DampingParam();
		saParam = new SA_Param(saPeriodParam, saDampingParam);
		saParam.setNonEditable();

		//  Create PGA Parameter (pgaParam):
		pgaParam = new PGA_Param();
		pgaParam.setNonEditable();

		//  Create PGV Parameter (pgvParam):
		pgvParam = new PGV_Param();
		pgvParam.setNonEditable();

		// Add the warning listeners: TODO clean?
//		saParam.addParameterChangeWarningListener(listener);
//		pgaParam.addParameterChangeWarningListener(listener);
//		pgvParam.addParameterChangeWarningListener(listener);

		// Put parameters in the supportedIMParams list:
		supportedIMParams.clear();
		supportedIMParams.addParameter(saParam);
		supportedIMParams.addParameter(pgaParam);
		supportedIMParams.addParameter(pgvParam);
		
		
	}

	@Override
	protected void initSiteParams() {
		siteParams.clear();
		vs30Param = new Vs30_Param(VS30_WARN_MIN, VS30_WARN_MAX);
		siteParams.addParameter(vs30Param);
		
		// Campbell & bozorgnia hidden
		depthTo2pt5kmPerSecParam = new DepthTo2pt5kmPerSecParam(0.0, 10.0, true);
		depthTo2pt5kmPerSecParam.setValue(null);
		depthTo2pt5kmPerSecParam.getEditor().setVisible(false);
		siteParams.addParameter(depthTo2pt5kmPerSecParam);

		// Chiou & Youngs hidden
		depthTo1pt0kmPerSecParam = new DepthTo1pt0kmPerSecParam(100, 1, 10000, true);
		depthTo1pt0kmPerSecParam.setValue(null);
		depthTo1pt0kmPerSecParam.getEditor().setVisible(false);
		siteParams.addParameter(depthTo1pt0kmPerSecParam);
		vs30_TypeParam = new Vs30_TypeParam();
		vs30_TypeParam.getEditor().setVisible(false);
		siteParams.addParameter(vs30_TypeParam);

	}

	@Override
	protected void initOtherParams() {
		super.initOtherParams();
		
		// the Component Parameter - common to NGA's but uneditable
		StringConstraint constraint = new StringConstraint();
		constraint.addString(ComponentParam.COMPONENT_GMRotI50);
		constraint.setNonEditable();
		componentParam = new ComponentParam(constraint,ComponentParam.COMPONENT_GMRotI50);
		componentParam.setValueAsDefault();
		
		// the stdDevType Parameter - common to NGA's
		StringConstraint stdDevTypeConstraint = new StringConstraint();
		stdDevTypeConstraint.addString(StdDevTypeParam.STD_DEV_TYPE_TOTAL);
		stdDevTypeConstraint.addString(StdDevTypeParam.STD_DEV_TYPE_NONE);
		stdDevTypeConstraint.addString(StdDevTypeParam.STD_DEV_TYPE_INTER);
		stdDevTypeConstraint.addString(StdDevTypeParam.STD_DEV_TYPE_INTRA);
		stdDevTypeConstraint.setNonEditable();
		stdDevTypeParam = new StdDevTypeParam(stdDevTypeConstraint);
		stdDevTypeParam.setValueAsDefault();
		
		// alllow toggling of AttenRel epistemic uncertainty
		BooleanParameter imrUncertParam = new BooleanParameter(
			"IMR uncertainty", includeImrUncert);
		
		// display prop effect pt src correction; set generic pt src to true
		ptSrcCorrParam = (BooleanParameter) propEffect
			.getAdjustableParameterList().getParameter(POINT_SRC_CORR_PARAM_NAME);
		ptSrcCorrParam.setValue(true);
		// as well as nshmp pt src correction
		BooleanParameter nshmpPtSrcCorrParam = (BooleanParameter) propEffect
			.getAdjustableParameterList().getParameter(NSHMP_PT_SRC_CORR_PARAM_NAME);
		nshmpPtSrcCorrParam.setValue(true);		
		
		// add these to the list
		otherParams.addParameter(componentParam);
		otherParams.addParameter(stdDevTypeParam);
		otherParams.addParameter(imrUncertParam);
		otherParams.addParameter(nshmpPtSrcCorrParam);
		
		sigmaTruncTypeParam.setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
		sigmaTruncLevelParam.setValue(3.0);
		
		sigmaTruncTypeParam.addParameterChangeListener(this);
		sigmaTruncLevelParam.addParameterChangeListener(this);
		tectonicRegionTypeParam.addParameterChangeListener(this);
		componentParam.addParameterChangeListener(this);
		stdDevTypeParam.addParameterChangeListener(this);
		imrUncertParam.addParameterChangeListener(this);
		
		// enforce default values used by NSHMP
		for (AttenuationRelationship ar : arList) {
			ParameterList list = ar.getOtherParamsList();

			ComponentParam cp = (ComponentParam) list.getParameter(
				ComponentParam.NAME);
			cp.setValue(ComponentParam.COMPONENT_GMRotI50);

			StdDevTypeParam stp = (StdDevTypeParam) list.getParameter(
				StdDevTypeParam.NAME);
			stp.setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);

			SigmaTruncTypeParam sttp = (SigmaTruncTypeParam) list.getParameter(
				SigmaTruncTypeParam.NAME);
			sttp.setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);

			SigmaTruncLevelParam stlp = (SigmaTruncLevelParam) list.getParameter(
				SigmaTruncLevelParam.NAME);
			stlp.setValue(3.0);
		}
	}

	@Override
	protected void initEqkRuptureParams() {}

	@Override
	protected void initPropagationEffectParams() {}

	@Override
	protected void initParameterEventListeners() {
		vs30Param.addParameterChangeListener(this);
		saPeriodParam.addParameterChangeListener(this);
	}

	@Override
	public void setSite(Site site) {
		this.site = site;
		
		// KLUDGY adding addt'l params that are required but that we want
		// hidden and set to null so that the atten rel calcs a default value
		//site.addParameter(param)
		
		propEffect.setSite(site);
		if (propEffect.getEqkRupture() != null) {
			setPropagationEffect(propEffect);
		}
	}
	
	@Override
	public void setEqkRupture(EqkRupture eqkRupture) {
		this.eqkRupture = eqkRupture;
		propEffect.setEqkRupture(eqkRupture);
		if (propEffect.getSite() != null) {
			setPropagationEffect(propEffect);
		}
	}
	
	@Override
	public void setPropagationEffect(PropagationEffect propEffect) {
		this.propEffect = propEffect;
		for (AttenuationRelationship ar : arList) {
			ar.setPropagationEffect(propEffect);
		}
	}

	@Override
	public double getMean() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getStdDev() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getEpsilon() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getEpsilon(double iml) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DiscretizedFuncAPI getExceedProbabilities(DiscretizedFuncAPI imls)
			throws ParameterException {
		
		//System.out.println(depthTo2pt5kmPerSecParam.getValue());
		
		// function collator with associated weights
		Map<DiscretizedFuncAPI, Double> funcs = new HashMap<DiscretizedFuncAPI, Double>();
		
		double imrWeight = 1 / (double) arList.size();
		
		// flag for epistemic uncertainty
		if (includeImrUncert) {
			// lookup M and dist
			double mag = propEffect.getEqkRupture().getMag();
			double dist = propEffect.getDistanceJB();
			System.out.println("M: " + mag + " D: " + dist);
			double uncert = getUncertainty(mag, dist);
			for (AttenuationRelationship ar : arList) {
				for (int i=0; i<3; i++) {
					imrUncert = imrUncertSign[i] * uncert;
					DiscretizedFuncAPI func = (DiscretizedFuncAPI) imls.deepClone();
					funcs.put(ar.getExceedProbabilities(func), imrWeight * imrUncertWeights[i]);
				}
			}
		} else {
			imrUncert = 0;
			for (AttenuationRelationship ar : arList) {
				DiscretizedFuncAPI func = (DiscretizedFuncAPI) imls.deepClone();
				funcs.put(ar.getExceedProbabilities(func), imrWeight);
			}
		}
		
		// populate original
		for (int i=0; i<imls.getNum(); i++) {
			double val = 0.0;
			for (DiscretizedFuncAPI f : funcs.keySet()) {
				val += f.getY(i) * funcs.get(f);
			}
			imls.set(i, val);
		}
		return imls;
	}
	
	@Override
	public double getExceedProbability() throws ParameterException,
			IMRException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected double getExceedProbability(double mean, double stdDev, double iml)
			throws ParameterException, IMRException {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getExceedProbability(double iml) throws ParameterException,
			IMRException {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getIML_AtExceedProb() throws ParameterException {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getIML_AtExceedProb(double exceedProb)
			throws ParameterException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DiscretizedFuncAPI getSA_ExceedProbSpectrum(double iml)
			throws ParameterException, IMRException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DiscretizedFuncAPI getSA_IML_AtExceedProbSpectrum(double exceedProb)
			throws ParameterException, IMRException {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getTotExceedProbability(PointEqkSource ptSrc, double iml) {
		throw new UnsupportedOperationException("getTotExceedProbability is unsupported for "+C);
	}

	@Override
	public void setIntensityMeasureLevel(Double iml) throws ParameterException {
		for (AttenuationRelationship ar : arList) {
			ar.setIntensityMeasureLevel(iml);
		}
	}

	@Override
	public void setIntensityMeasureLevel(Object iml) throws ParameterException {
		for (AttenuationRelationship ar : arList) {
			ar.setIntensityMeasureLevel(iml);
		}
	}

//	@Override
//	public void setIntensityMeasure(ParameterAPI intensityMeasure)
//			throws ParameterException, ConstraintException {
//		super.setIntensityMeasure(intensityMeasure);
//		System.out.println("IMparam: " + intensityMeasure);
//		for (ScalarIntensityMeasureRelationshipAPI ar : arList) {
//			ar.setIntensityMeasure(intensityMeasure);
//		}
//	}

	@Override
	public void setIntensityMeasure(String intensityMeasureName)
			throws ParameterException {
		super.setIntensityMeasure(intensityMeasureName);
		for (ScalarIntensityMeasureRelationshipAPI ar : arList) {
			ar.setIntensityMeasure(intensityMeasureName);
		}
	}

	@Override
	public void parameterChange(ParameterChangeEvent e) {
		// pass through changes to params we know nga's are listeneing to
		for (AttenuationRelationship ar : arList) {
			ParameterChangeListener pcl = (ParameterChangeListener) ar;
			pcl.parameterChange(e);
		}
		// pass through those changes that are picked up at calculation time
		if (otherParams.containsParameter(e.getParameter())) {
			for (AttenuationRelationship ar : arList) {
				ParameterList pList = ar.getOtherParamsList();
				String pName = e.getParameterName();
				if (!pList.containsParameter(pName)) continue;
				// TODO above shouldn't be necessary; pLists should not throw
				// exceptions fo missing parameters
				ParameterAPI<?> param = ar.getOtherParamsList().getParameter(
					e.getParameterName());
				if (param instanceof StringParameter) {
					((StringParameter) param).setValue((String) e
						.getParameter().getValue());
				} else {
					((DoubleParameter) param).setValue((Double) e
						.getParameter().getValue());
				}
			}
		}
		// handle SA period change; this is picked up independently by atten
		// rels at calculation time so changes here need to be transmitted to
		// children
		if (e.getParameterName().equals(PeriodParam.NAME)) {
			for (AttenuationRelationship ar : arList) {
				ParameterList pList = ar.getSupportedIntensityMeasuresList();
				SA_Param sap = (SA_Param) pList.getParameter(SA_Param.NAME);
				sap.getPeriodParam().setValue(saPeriodParam.getValue());
			}
		}
		// handle locals
		if (e.getParameterName().equals(IMR_UNCERT_PARAM_NAME)) {
			includeImrUncert = (Boolean) e.getParameter().getValue();
		}
		
		// toggling nshmp pt src is picked up directly by PropEffect,
		// need to turn off basic point source correction too; TODO this
		// could be implemented better e.g. as a choice: none | field | nshmp
		if (e.getParameterName().equals(NSHMP_PT_SRC_CORR_PARAM_NAME)) {
			ptSrcCorrParam.setValue((Boolean) e.getParameter().getValue());
		}
		
	}

	private static double[] imrUncertSign = {-1.0, 0.0, 1,0};
	private static double[] imrUncertWeights = {0.185, 0.630, 0.185};
	private static double[][] imrUncertVals = {
		{0.375, 0.210, 0.245},
		{0.230, 0.225, 0.230},
		{0.400, 0.360, 0.310}};

	/*
	 * Returns the epistemic uncertainty for the supplied magnitude (M) and
	 * distance (D) that
	 */
	private static double getUncertainty(double M, double D) {
		int mi = (M<6) ? 0 : (M<7) ? 1 : 2;
		int di = (D<10) ? 0 : (D<30) ? 1 : 2;
		return imrUncertVals[di][mi];
	}

//	public static void main(String[] args) {
//		System.out.println(getUncertainty(5,5));
//		System.out.println(getUncertainty(6,5));
//		System.out.println(getUncertainty(7,5));
//
//		System.out.println(getUncertainty(5,20));
//		System.out.println(getUncertainty(6,20));
//		System.out.println(getUncertainty(7,20));
//
//		System.out.println(getUncertainty(5,35));
//		System.out.println(getUncertainty(6,35));
//		System.out.println(getUncertainty(7,35));
//
//		System.out.println(getUncertainty(5,5));
//		System.out.println(getUncertainty(5,10));
//		System.out.println(getUncertainty(5,30));
//
//		System.out.println(getUncertainty(6,5));
//		System.out.println(getUncertainty(6,10));
//		System.out.println(getUncertainty(6,30));
//
//		System.out.println(getUncertainty(7,5));
//		System.out.println(getUncertainty(7,10));
//		System.out.println(getUncertainty(7,30));
//
//	}
	//public static void main(String[] args) {
		//RegionUtils.regionToKML(new CaliforniaRegions.RELM_NOCAL(), "relm_nocal2", Color.RED);
	//}
		
	private class NSHMP_BA_2008 extends BA_2008_AttenRel {
		public NSHMP_BA_2008(ParameterChangeWarningListener listener) {
			super(listener);
		}
		@Override
		public double getExceedProbability(double mean, double stdDev, double iml) {
			return super.getExceedProbability(
				mean + imrUncert, stdDev, iml);
		}
	}

	private class NSHMP_CB_2008 extends CB_2008_AttenRel {
		public NSHMP_CB_2008(ParameterChangeWarningListener listener) {
			super(listener);
		}
		@Override
		public double getExceedProbability(double mean, double stdDev, double iml) {
			return super.getExceedProbability(
				mean + imrUncert, stdDev, iml);
		}
	}

	private class NSHMP_CY_2008 extends CY_2008_AttenRel {
		public NSHMP_CY_2008(ParameterChangeWarningListener listener) {
			super(listener);
		}
		@Override
		public double getExceedProbability(double mean, double stdDev, double iml) {
			return super.getExceedProbability(
				mean + imrUncert, stdDev, iml);
		}
	}

}
