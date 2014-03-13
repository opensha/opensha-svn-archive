package scratch.peter.nga.single;

import static java.lang.Double.NaN;
import static java.lang.Math.sin;
import static org.opensha.commons.geo.GeoTools.TO_RAD;
import static scratch.peter.nga.FaultStyle.NORMAL;
import static scratch.peter.nga.FaultStyle.REVERSE;
import static scratch.peter.nga.FaultStyle.STRIKE_SLIP;
import static scratch.peter.nga.FaultStyle.UNKNOWN;

import java.util.List;
import java.util.Map;

import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.commons.exceptions.IMRException;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.geo.Location;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.constraint.impl.DoubleDiscreteConstraint;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;
import org.opensha.nshmp2.erf.source.PointSource13b.PointSurface13b;
import org.opensha.nshmp2.util.Period;
import org.opensha.nshmp2.util.Utils;
import org.opensha.sha.earthquake.rupForecastImpl.PointEqkSource;
import org.opensha.sha.faultSurface.RuptureSurface;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.attenRelImpl.BA_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CY_2008_AttenRel;
import org.opensha.sha.imr.param.IntensityMeasureParams.DampingParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.PeriodParam;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;

import scratch.peter.newcalc.ScalarGroundMotion;
import scratch.peter.nga.ASK_2013_Transitional;
import scratch.peter.nga.BSSA_2013_Transitional;
import scratch.peter.nga.CB_2013_Transitional;
import scratch.peter.nga.CY_2013_Transitional;
import scratch.peter.nga.FaultStyle;
import scratch.peter.nga.GK_2013_Transitional;
import scratch.peter.nga.IMT;
import scratch.peter.nga.Idriss_2013_Transitional;
import scratch.peter.nga.NGAW2_GMM;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

//@formatter:off
/**
 * This is an implementation of the combined attenuation relationships used 
 * in California and the Western US for the 2008 National Seismic Hazard Mapping
 * Program (NSHMP). The three next generation attenuation relationships (NGAs)
 * used are:
 * <ul>
 * <li>{@link BA_2008_AttenRel Boore &amp; Atkinson (2008)}</li>
 * <li>{@link CB_2008_AttenRel Cambell &amp; Bozorgnia (2008)}</li>
 * <li>{@link CY_2008_AttenRel Chiou &amp; Youngs (2008)}</li>
 * </ul>
 * Each attenuation relationship gets 1/3 weight.
 * 
 * <p>As with other NSHMP attenutation relationships, this may only be used via
 * {@code setSite()} and {@code setEqkRupture()} as the calculations are
 * {@code PropagationEffect} dependent.</p>
 * 
 * <p><b>Additional Epistemic Uncertainty</b></p>
 * <p>Additional epistemic uncertainty is considered for each NGA according to
 * the following distance and magnitude matrix:
 * <pre>
 *             M<6      6%le;M<7      7&le;M
 *          =============================
 *   D<10     0.375  |  0.230  |  0.400v
 * 10&le;D<30    0.210  |  0.225  |  0.360
 *   30&le;D     0.245  |  0.230  |  0.310
 *          =============================
 * </pre>
 * For an earthquake rupture at a given distance and magnitude, the
 * corresponding uncertainty is applied to a particular NGA with the following
 * weights:
 * <pre>
 *     hazard curve           weight
 * ======================================
 *      mean + unc            0.185
 *      mean                  0.630
 *      mean - unc            0.185
 * ======================================
 * </pre>
 * 
 * @author Peter Powers
 * @version $Id:$
 */
//@formatter:on
public class NSHMP13_WUS_GK extends AttenuationRelationship implements
		ParameterChangeListener {

	public final static String NAME = "NSHMP 2013 Western US Combined";
	public final static String SHORT_NAME = "NSHMP13_WUS";
//	private static final long serialVersionUID = 1L;

	// this is the minimum range of vs30 spanned by BA, CB, & CY (the NGA's)
//	private final static double VS30_WARN_MIN = 80;
//	private final static double VS30_WARN_MAX = 1300;

	// imr weight maps
	Map<NGAW2_GMM, Double> gmpeMap;
	
	// custom params
	public static final String IMR_UNCERT_PARAM_NAME = "IMR uncertainty";
	private boolean epi = false;
//	private static final String HW_EFFECT_PARAM_NAME = "Hanging Wall Effect Approx.";
//	private boolean hwEffectApprox = true;
	
	
	/**
	 * @param epi flag for additional epistemic distnace and magnitude dependent
	 * epistemic uncertainty on ground motions.
	 */
	public NSHMP13_WUS_GK() {
		initGMPEmap();
		
		BooleanParameter imrUncertParam = new BooleanParameter(
			IMR_UNCERT_PARAM_NAME, epi);
		imrUncertParam.addParameterChangeListener(this);
		otherParams.addParameter(imrUncertParam);
		
		// these methods are called for each attenRel upon construction; we
		// do some local cloning so that a minimal set of params may be
		// exposed and modified in gui's and/or to ensure some parameters
		// adhere to NSHMP values
		initSupportedIntensityMeasureParams();
//		initEqkRuptureParams();
//		initPropagationEffectParams();
//		initSiteParams();
//		initOtherParams();
//		initParameterEventListeners();
//		
//		setParamDefaults();
	}
		
	void initGMPEmap() {
		gmpeMap = Maps.newHashMap();
		gmpeMap.put(new GK_2013_Transitional(), 1.0);
	}
	
	@Override
	public void setParamDefaults() {
		throw new UnsupportedOperationException();
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
	protected void initSupportedIntensityMeasureParams() {
		
		List<Double> perVals = Lists.newArrayList();
		for (Period p : Period.getWUS()) {
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

		//  Create PGA Parameter (pgaParam):
		pgaParam = new PGA_Param();
		pgaParam.setNonEditable();

		// Put parameters in the supportedIMParams list:
		supportedIMParams.clear();
		supportedIMParams.addParameter(saParam);
		supportedIMParams.addParameter(pgaParam);
	}

	@Override
	protected void initSiteParams() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void initEqkRuptureParams() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void initPropagationEffectParams() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	protected void setPropagationEffectParams() {
		throw new UnsupportedOperationException();
	}
//		if (site != null && eqkRupture != null) {
//			distanceJBParam.setValue(eqkRupture, site);
//		}
//	}
	
	private IMT imt = null;
	
	private double Mw = NaN;
	private double rJB = NaN;
	private double rRup = NaN;
	private double rX = NaN;
	private double dip = NaN;
	private double width = NaN;
	private double zTop = NaN;
	private double zHyp = NaN;
	private double vs30 = NaN;
	private boolean vsInf = true;
	private double z2p5 = NaN;
	private double z1p0 = NaN;
	private FaultStyle style = UNKNOWN;

	private void updateArgs() {
		
//		for (Parameter<?> p : site) {
//			System.out.println(p.getValue());
//		}

		// site args
		vs30 = (Double) site.getParameter(Vs30_Param.NAME).getValue();
		z2p5 = Double.NaN; //(Double) site.getParameter(DepthTo2pt5kmPerSecParam.NAME).getValue();
		z1p0 = Double.NaN; //(Double) site.getParameter(DepthTo1pt0kmPerSecParam.NAME).getValue();
		
		// eq args
		Mw = eqkRupture.getMag();
		RuptureSurface surf = eqkRupture.getRuptureSurface();
		Location loc = site.getLocation();
		rJB = surf.getDistanceJB(loc);
		rRup = surf.getDistanceRup(loc);
		rX = surf.getDistanceX(loc);
		dip = surf.getAveDip();
		width = surf.getAveWidth();
		zTop = surf.getAveRupTopDepth();
		zHyp = zTop + sin(dip * TO_RAD) * width / 2.0;
		
		style = getFaultStyleForRake(eqkRupture.getAveRake());
	
	}
	
	private void initGMPEs() {
		for (NGAW2_GMM gmpe : gmpeMap.keySet()) {

			gmpe.set_IMT(imt);
			
			gmpe.set_Mw(Mw);
			gmpe.set_rJB(rJB);
			gmpe.set_rRup(rRup);
			gmpe.set_rX(rX);
			gmpe.set_dip(dip);
			gmpe.set_width(width);
			gmpe.set_zTop(zTop);
			gmpe.set_zHyp(zHyp);
			gmpe.set_vs30(vs30);
			gmpe.set_vsInf(vsInf);
			gmpe.set_z2p5(z2p5);
			gmpe.set_z1p0(z1p0);
			gmpe.set_fault(style);
		}
	}
	
	
	/*
	 * Returns the NSHMP interpretation of fault type based on rake; divisions
	 * on 45 deg. diagonals. Doesn't necessarily agree with cutoffs defined by
	 * NGAW2 developers. TODO recheck developer cutoffs
	 */
	private FaultStyle getFaultStyleForRake(double rake) {
		return (rake >= 45 && rake <= 135) ? REVERSE :
			   (rake >= -135 && rake <= -45) ? NORMAL : STRIKE_SLIP;
	}
	
	@Override
	public double getMean() {
		// tmp KLUDGY for Nico
		double mean = 0;
		updateArgs();
		initGMPEs();
		for (NGAW2_GMM gmpe : gmpeMap.keySet()) {
			mean += gmpeMap.get(gmpe) * gmpe.calc().mean();
		}
		return mean;
//		throw new UnsupportedOperationException();
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
	
	boolean display = false;
	
	@Override
	public DiscretizedFunc getExceedProbabilities(DiscretizedFunc imls)
			throws ParameterException {
		
		updateArgs();
		initGMPEs();
		
//		if (display && rJB < 60.0) {
//			System.out.println(imt + "," +Mw + "," +rJB + "," +rRup + "," +rX + "," +dip + "," +width + "," +zTop + "," +zHyp + "," +vs30 + "," +vsInf + "," +z2p5 + "," +z1p0 + "," +style);
//			System.out.println(eqkRupture.getRuptureSurface());
//			System.out.println(eqkRupture.getRuptureSurface().getClass());
//		}
		
		// set mean, sigma, and weight arrays
		int curveCount = gmpeMap.size();
		if (epi) curveCount *= EPI_CT;
		double[] means = new double[curveCount];
		double[] sigmas = new double[curveCount];
		double[] weights = new double[curveCount];
		
		int idx = 0;
		for (NGAW2_GMM gmpe : gmpeMap.keySet()) {
			ScalarGroundMotion sgm = gmpe.calc();
			double m = sgm.mean();
			double s = sgm.stdDev();
//			System.out.println("mean " + m + "  sigma " + s);
			double w = gmpeMap.get(gmpe);
			
//			if (display && rJB < 60.0) {
//				System.out.println(gmpe.getClass() + " " + m +  " " + s);
//			}
			
			if (epi) {
				double epiVal = getUncertainty(Mw, rJB);
				for (int i=0; i<EPI_CT; i++) {
					means[idx] = m + epiVal * EPI_SIGN[i];
					weights[idx] = w * EPI_WT[i];
					sigmas[idx] = s;
					idx++;
				}
			} else {
				means[idx] = m;
				sigmas[idx] = s;
				weights[idx] = w;
				idx++;
			}
		}
//		if (rJB < 60.0) {
//			display = false;
//		}
		
		
		// get and sum curves
		Utils.zeroFunc(imls);
		DiscretizedFunc f = imls.deepClone();
		for (int i=0; i<means.length; i++) {
//			if (Double.isNaN(means[i]) || Double.isNaN(sigmas[i])) {
//				System.out.println(eqkRupture.getRuptureSurface().getClass());
//				System.out.println(eqkRupture.getRuptureSurface());
//				PointSurface13b surf = (PointSurface13b) eqkRupture.getRuptureSurface();
//				System.out.println(surf.getLocation());
//				List<Double> attr = Lists.newArrayList(
//					eqkRupture.getMag(),
//					eqkRupture.getAveRake(),
//					surf.getAveDip(),
//					surf.getDepth(),
//					surf.getDistanceJB(site.getLocation()),
//					surf.getDistanceRup(site.getLocation()),
//					surf.getDistanceX(site.getLocation()));
//					
//				
//				System.out.println(Joiner.on(" ").join(attr) + " " + surf.isOnFootwall());
//
//			}
			
			f = Utils.getExceedProbabilities(f, means[i], sigmas[i], false, 0.0);
			f.scale(weights[i]);
			Utils.addFunc(imls, f);
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIntensityMeasureLevel(Double iml) throws ParameterException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIntensityMeasureLevel(Object iml) throws ParameterException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setIntensityMeasure(String intensityMeasureName)
			throws ParameterException {
		super.setIntensityMeasure(intensityMeasureName);
		
		if (intensityMeasureName.equals(PGA_Param.NAME)) {
			imt = IMT.PGA;
		}
	}

	@Override
	public void parameterChange(ParameterChangeEvent e) {

		// handle SA period change; this is picked up independently by atten
		// rels at calculation time so changes here need to be transmitted to
		// children
		if (e.getParameterName().equals(PeriodParam.NAME)) {
			double period = (Double) getParameter(PeriodParam.NAME).getValue();
			imt = (period == 0.0) ? IMT.PGA : IMT.getSA((Double) getParameter(
				PeriodParam.NAME).getValue());
		}
		
		// handle locals
		if (e.getParameterName().equals(IMR_UNCERT_PARAM_NAME)) {
			epi = (Boolean) e.getParameter().getValue();
		}
	}

	private static final int EPI_CT = 3;
	private static final double[] EPI_SIGN = {-1.0, 0.0, 1,0};
	private static final double[] EPI_WT = {0.185, 0.630, 0.185};
	private static final double[][] EPI_VAL = {
		{0.375, 0.250, 0.400},
		{0.220, 0.230, 0.360},
		{0.220, 0.230, 0.330}};

	/*
	 * Returns the epistemic uncertainty for the supplied magnitude (M) and
	 * distance (D) that
	 */
	private static double getUncertainty(double M, double D) {
		int mi = (M<6) ? 0 : (M<7) ? 1 : 2;
		int di = (D<10) ? 0 : (D<30) ? 1 : 2;
		return EPI_VAL[di][mi];
	}
	
	
	
}
