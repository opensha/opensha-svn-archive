package org.opensha.sha.nshmp.imr;

import static com.google.common.base.Preconditions.*;
//import static org.opensha.sha.imr.PropagationEffect.*;
import static org.opensha.sha.imr.AttenRelRef.*;
import static org.opensha.sha.nshmp.Period.*;
import static org.opensha.sha.nshmp.SiteType.FIRM_ROCK;
import static org.opensha.sha.nshmp.SiteType.HARD_ROCK;
import static org.opensha.sha.earthquake.rupForecastImpl.nshmp.CEUS_ERF.SourceCEUS.*;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.opensha.commons.data.Site;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.exceptions.IMRException;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.constraint.impl.DoubleDiscreteConstraint;
import org.opensha.commons.param.constraint.impl.StringConstraint;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.event.ParameterChangeWarningListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.rupForecastImpl.PointEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.CEUS_ERF.SourceCEUS;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.util.FaultCode;
import org.opensha.sha.imr.AttenRelRef;
import org.opensha.sha.imr.AttenuationRelationship;
//import org.opensha.sha.imr.PropagationEffect;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.param.IntensityMeasureParams.DampingParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.ComponentParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncLevelParam;
import org.opensha.sha.imr.param.OtherParams.SigmaTruncTypeParam;
import org.opensha.sha.imr.param.OtherParams.StdDevTypeParam;
import org.opensha.sha.imr.param.OtherParams.TectonicRegionTypeParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.nshmp.Period;
import org.opensha.sha.nshmp.RateTable;
import org.opensha.sha.nshmp.SiteType;
import org.opensha.sha.nshmp.Utils;
import org.opensha.sha.util.TectonicRegionType;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This is an implementation of the combined attenuation relationships used in
 * the CEUS for the 2008 National Seismic Hazard Mapping Program (NSHMP). The
 * eight possible attenuation relationships used are:
 * <ul>
 * <li>{@link BA_2008_AttenRel Boore &amp; Atkinson (2008)}</li>
 * <li>{@link CB_2008_AttenRel Cambell &amp; Bozorgnia (2008)}</li>
 * <li>{@link CY_2008_AttenRel Chiou &amp; Youngs (2008)}</li>
 * </ul>
 * Please take note of the following implementation details:
 * <ul>
 * <li></li>
 * <li></li>
 * <li></li>
 * </ul>
 * 
 * this has a few customizations tailored to the ERF's that will be used
 * 
 * Mag conversions:
 * ALL CEUS IMRs were developed for Mw, however, gridded sources are based on
 * mblg. As such, M conversions are applied according to which of two gridded
 * sources are passed in (AB or J) when building curve tables. The exception is
 * Toro which uses it's own coefficients to handle mblg. See RateTable for
 * switches.
 * 
 * If a source is not gridded, no such M conversion options are available or 
 * necessary.
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class NSHMP08_CEUS extends AttenuationRelationship implements
		ParameterChangeListener {

	public final static String NAME = "NSHMP 2008 CEUS Combined";
	public final static String SHORT_NAME = "NSHMP_2008_CEUS";
	private static final long serialVersionUID = 1L;

	private static Set<Period> supportedPeriods = EnumSet.of(GM0P00, GM0P20,
		GM0P50, GM1P00, GM2P00);

	// possibly temp; child imrs should eb ignoring
	private final static double VS30_WARN_MIN = 80;
	private final static double VS30_WARN_MAX = 1300;

	// imr weight maps; n.b. Charleston fixed-strike uses fault weights
	private Map<ScalarIMR, Double> imrFltMap;
	private Map<ScalarIMR, Double> imrGrdMap;
	private Map<ScalarIMR, Double> imrMap;

	private RateTable gridTable;

	// custom params
	private BooleanParameter ptSrcCorrParam;
	private BooleanParameter nshmpPtSrcCorrParam;
	private EnumParameter<SourceCEUS> sourceParam;
	private EnumParameter<SiteType> siteTypeParam;

	public NSHMP08_CEUS(ParameterChangeWarningListener listener) {
		initIMRs();

//		propEffect = new PropagationEffect();
//		propEffect.fixDistanceJB(true);

		// these methods are called for each attenRel upon construction; we
		// do some local cloning so that a minimal set of params may be
		// exposed and modified in gui's and/or to ensure some parameters
		// adhere to NSHMP values
		initSupportedIntensityMeasureParams();
		initSiteParams();
		initOtherParams();
		initParameterEventListeners();
	}

	private void initIMRs() {
		
		imrFltMap = Maps.newHashMap();
		imrFltMap.put(TORO_1997.instance(null), 0.2);
		imrFltMap.put(SOMERVILLE_2001.instance(null), 0.2);
		imrFltMap.put(FEA_1996.instance(null), 0.1);
		imrFltMap.put(AB_2006_140.instance(null), 0.1);
		imrFltMap.put(AB_2006_200.instance(null), 0.1);
		imrFltMap.put(CAMPBELL_2003.instance(null), 0.1);
		imrFltMap.put(TP_2005.instance(null), 0.1);
		imrFltMap.put(SILVA_2002.instance(null), 0.1);
		
		imrGrdMap = Maps.newHashMap();
		imrGrdMap.put(TORO_1997.instance(null), 0.25);
		imrGrdMap.put(FEA_1996.instance(null), 0.125);
		imrGrdMap.put(AB_2006_140.instance(null), 0.125);
		imrGrdMap.put(AB_2006_200.instance(null), 0.125);
		imrGrdMap.put(CAMPBELL_2003.instance(null), 0.125);
		imrGrdMap.put(TP_2005.instance(null), 0.125);
		imrGrdMap.put(SILVA_2002.instance(null), 0.125);
		imrMap = imrFltMap;
	}

	private void updateWtList() {
		imrMap = (sourceParam.getValue() == GRID) ? imrGrdMap : imrFltMap;
	}

	private void initGridTable() {
		System.out.println("grid start");
		Stopwatch sw = new Stopwatch();
		sw.start();
		gridTable = RateTable.create(1000, 5, 24, 5.05, 0.1, supportedPeriods,
			FaultCode.M_CONV_AB, imrGrdMap);
		
		System.out.println(gridTable.get(10, 6.45, GM0P00));
		
		System.out.println("grid end");
		sw.stop();
		System.out.println(sw.elapsedTime(TimeUnit.SECONDS));
//		System.out.println(gridTable.size());
//		System.out.println(sw.elapsedTime(TimeUnit.SECONDS));
//		System.out.println(gridTable.get(11, 7.35, Period.GM0P00));
	}

	@Override
	public void setParamDefaults() {

		pgaParam.setValueAsDefault();
		saParam.setValueAsDefault();
		saPeriodParam.setValueAsDefault();
		saDampingParam.setValueAsDefault();

		siteTypeParam.setValueAsDefault(); // shouldn't be necessary

		sourceParam.setValueAsDefault();
		componentParam.setValueAsDefault();
		tectonicRegionTypeParam.setValueAsDefault();
//		ptSrcCorrParam.setValueAsDefault();
//		nshmpPtSrcCorrParam.setValueAsDefault();

		for (ScalarIMR imr : imrMap.keySet()) {
			imr.setParamDefaults();
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

		List<Double> perVals = Lists.newArrayList();
		for (Period p : supportedPeriods) {
			perVals.add(p.getValue());
		}
		DoubleDiscreteConstraint periodConstraint = new DoubleDiscreteConstraint(
			perVals);
		periodConstraint.setNonEditable();
		saPeriodParam = new PeriodParam(periodConstraint, 1.0, false);
		saPeriodParam.addParameterChangeListener(this);
		saDampingParam = new DampingParam();
		saParam = new SA_Param(saPeriodParam, saDampingParam);
		saParam.setNonEditable();

		// Create PGA Parameter (pgaParam):
		pgaParam = new PGA_Param();
		pgaParam.setNonEditable();

		// Create PGV Parameter (pgvParam):
		// pgvParam = new PGV_Param();
		// pgvParam.setNonEditable();

		// Add the warning listeners: TODO clean?
		// saParam.addParameterChangeWarningListener(listener);
		// pgaParam.addParameterChangeWarningListener(listener);
		// pgvParam.addParameterChangeWarningListener(listener);

		// Put parameters in the supportedIMParams list:
		supportedIMParams.clear();
		supportedIMParams.addParameter(saParam);
		supportedIMParams.addParameter(pgaParam);
		// supportedIMParams.addParameter(pgvParam);
	}

	@Override
	protected void initSiteParams() {
		siteParams.clear();
		vs30Param = new Vs30_Param(VS30_WARN_MIN, VS30_WARN_MAX);
		siteParams.addParameter(vs30Param);
		vs30Param.setValueAsDefault();
		
		siteTypeParam = new EnumParameter<SiteType>("Site Type", EnumSet.of(
			FIRM_ROCK, HARD_ROCK), FIRM_ROCK, null);
		siteParams.clear();
		siteParams.addParameter(siteTypeParam);
	}

	@Override
	protected void initOtherParams() {
		// this imr ignores the params set in super()
		// super.initOtherParams();

		// Source type
		sourceParam = new EnumParameter<SourceCEUS>("Source", EnumSet.of(FAULT,
			GRID, GRID_FIXED), FAULT, null);
		sourceParam.addParameterChangeListener(this);
		otherParams.addParameter(sourceParam);

		// Component Parameter - uneditable
		StringConstraint compConst = new StringConstraint();
		compConst.addString(ComponentParam.COMPONENT_AVE_HORZ);
		compConst.setNonEditable();
		componentParam = new ComponentParam(compConst,
			ComponentParam.COMPONENT_AVE_HORZ);
		otherParams.addParameter(componentParam);

		// Tect Reg Type - uneditable
		StringConstraint trtConst = new StringConstraint();
		String trtDefault = TectonicRegionType.STABLE_SHALLOW.toString();
		trtConst.addString(trtDefault);
		tectonicRegionTypeParam = new TectonicRegionTypeParam(trtConst,
			trtDefault);
		otherParams.addParameter(tectonicRegionTypeParam);

		// Prop effect pt src correction - hidden
//		ptSrcCorrParam = (BooleanParameter) propEffect
//			.getAdjustableParameterList().getParameter(
//				POINT_SRC_CORR_PARAM_NAME);
//		ptSrcCorrParam.setDefaultValue(true);

		// NSHMP pt src correction - hidden
//		nshmpPtSrcCorrParam = (BooleanParameter) propEffect
//			.getAdjustableParameterList().getParameter(
//				NSHMP_PT_SRC_CORR_PARAM_NAME);
//		nshmpPtSrcCorrParam.setDefaultValue(true);
		// otherParams.addParameter(nshmpPtSrcCorrParam);

		// enforce default values used by NSHMP
		for (ScalarIMR imr : imrMap.keySet()) {
			ParameterList list = imr.getOtherParams();

			// ComponentParam cp = (ComponentParam) list.getParameter(
			// ComponentParam.NAME);
			// cp.setValue(ComponentParam.COMPONENT_GMRotI50);

			// StdDevTypeParam stp = (StdDevTypeParam) list.getParameter(
			// StdDevTypeParam.NAME);
			// stp.setValue(StdDevTypeParam.STD_DEV_TYPE_TOTAL);

			// TODO HOW TO REINTEGRATE AND INCLUDE CLAMPING
			// SigmaTruncTypeParam sttp = (SigmaTruncTypeParam)
			// list.getParameter(
			// SigmaTruncTypeParam.NAME);
			// sttp.setValue(SigmaTruncTypeParam.SIGMA_TRUNC_TYPE_1SIDED);
			//
			// SigmaTruncLevelParam stlp = (SigmaTruncLevelParam)
			// list.getParameter(
			// SigmaTruncLevelParam.NAME);
			// stlp.setValue(3.0);
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
//		propEffect.setSite(site);

		// being done to satisfy unit tests
		vs30Param.setValueIgnoreWarning((Double) site.getParameter(
			Vs30_Param.NAME).getValue());

//		if (propEffect.getEqkRupture() != null) {
//			setPropagationEffect(propEffect);
//		}
	}

	@Override
	public void setEqkRupture(EqkRupture eqkRupture) {
		this.eqkRupture = eqkRupture;
//		propEffect.setEqkRupture(eqkRupture);
//		if (propEffect.getSite() != null) {
//			setPropagationEffect(propEffect);
//		}
	}

//	@Override
//	public void setPropagationEffect(PropagationEffect propEffect) {
//		this.propEffect = propEffect;
//		if (sourceParam.getValue() == GRID) return;
//		for (ScalarIMR imr : imrMap.keySet()) {
//			imr.setPropagationEffect(propEffect);
//		}
//	}

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
	public DiscretizedFunc getExceedProbabilities(DiscretizedFunc imls)
			throws ParameterException {

		if (sourceParam.getValue() == GRID) {
			double M = eqkRupture.getMag();
			double D = eqkRupture.getRuptureSurface().getDistanceJB(site.getLocation());
			Period P = Period.valueForPeriod((Double) getParameter(
				PeriodParam.NAME).getValue());
			return gridTable.get(D, M, P);
		}

		Utils.zero(imls);
		DiscretizedFunc f = (DiscretizedFunc) imls.deepClone();
		for (ScalarIMR imr : imrMap.keySet()) {
			f = imr.getExceedProbabilities(f);
			f.scale(imrMap.get(imr));
			Utils.addFunc(imls, f);
		}
//		System.out.println(imls);
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
	public DiscretizedFunc getSA_ExceedProbSpectrum(double iml)
			throws ParameterException, IMRException {
		throw new UnsupportedOperationException();
	}

	@Override
	public DiscretizedFunc getSA_IML_AtExceedProbSpectrum(double exceedProb)
			throws ParameterException, IMRException {
		throw new UnsupportedOperationException();
	}

	@Override
	public double getTotExceedProbability(PointEqkSource ptSrc, double iml) {
		throw new UnsupportedOperationException(
			"getTotExceedProbability is unsupported for " + C);
	}

	@Override
	public void setIntensityMeasureLevel(Double iml) throws ParameterException {
		for (ScalarIMR imr : imrMap.keySet()) {
			imr.setIntensityMeasureLevel(iml);
		}
	}

	@Override
	public void setIntensityMeasureLevel(Object iml) throws ParameterException {
		for (ScalarIMR imr : imrMap.keySet()) {
			imr.setIntensityMeasureLevel(iml);
		}
	}

	@Override
	public void setIntensityMeasure(String intensityMeasureName)
			throws ParameterException {
		super.setIntensityMeasure(intensityMeasureName);
		for (ScalarIMR imr : imrMap.keySet()) {
			imr.setIntensityMeasure(intensityMeasureName);
		}
	}

	@Override
	public void parameterChange(ParameterChangeEvent e) {
//		System.out.println(e);
		String pName = e.getParameterName();

		if (pName.equals(sourceParam.getName())) {
			if (sourceParam.getValue() == GRID) {
				updateWtList();
				initGridTable();
			}
		}

		// pass through changes
		for (ScalarIMR imr : imrMap.keySet()) {
			ParameterChangeListener pcl = (ParameterChangeListener) imr;
			pcl.parameterChange(e);
		}

		// pass through those changes that are picked up at calculation time
		if (otherParams.containsParameter(e.getParameter())) {
			for (ScalarIMR imr : imrMap.keySet()) {
				ParameterList pList = imr.getOtherParams();
				// String pName = e.getParameterName();
				if (!pList.containsParameter(pName)) continue;
				// TODO above shouldn't be necessary; pLists should not throw
				// exceptions fo missing parameters
				Parameter<?> param = imr.getOtherParams().getParameter(
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
			for (ScalarIMR imr : imrMap.keySet()) {
				ParameterList pList = imr.getSupportedIntensityMeasures();
				SA_Param sap = (SA_Param) pList.getParameter(SA_Param.NAME);
				sap.getPeriodParam().setValue(saPeriodParam.getValue());
			}
		}

		// toggling nshmp pt src is picked up directly by PropEffect,
		// need to turn off basic point source correction too; TODO this
		// could be implemented better e.g. as a choice: none | field | nshmp
//		if (e.getParameterName().equals(NSHMP_PT_SRC_CORR_PARAM_NAME)) {
//			ptSrcCorrParam.setValue((Boolean) e.getParameter().getValue());
//		}

	}

}
