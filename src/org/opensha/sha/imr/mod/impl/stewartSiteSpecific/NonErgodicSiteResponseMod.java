package org.opensha.sha.imr.mod.impl.stewartSiteSpecific;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opensha.commons.data.Site;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.constraint.impl.StringConstraint;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.ButtonParameter;
import org.opensha.commons.param.impl.DoubleParameter;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.commons.util.ClassUtils;
import org.opensha.commons.util.Interpolate;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.attenRelImpl.ngaw2.BSSA_2014;
import org.opensha.sha.imr.mod.AbstractAttenRelMod;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.StdDevTypeParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.imr.param.SiteParams.Vs30_TypeParam;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

public class NonErgodicSiteResponseMod extends AbstractAttenRelMod implements ParameterChangeListener {
	
	private static final boolean D = true;
	private static final boolean DD = D && false;
	
	public static final String NAME = "Non Ergodic Site Response 2016 Mod";
	public static final String SHORT_NAME = "NonErgodic2016";
	
	public enum Params {
		F1("f1"),
		F2("f2"),
		F3("f3"),
		PHI_lnY("ϕ lnY"),
		PHI_S2S("ϕ S2S"),
		F("F"),
		Ymax("Ymax");
		
		private String name;
		
		private Params(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private PeriodDependentParamSet<Params> periodParams;
	private double curPeriod;
	private double[] curParamValues;
	private PeriodDependentParamSetParam<Params> periodParamsParam;
	
	private static final String REF_IMT_DEFAULT = PGA_Param.NAME;
	private StringParameter refIMTParam;
	
	private DoubleParameter tSiteParam;
	
//	private ButtonParameter plotInterpParam;
	
	private ParameterList paramList;
	
	private Parameter<Double> imt;
	
	private Site curSite;
	
	private ParamInterpolator<Params> interp;
	
	private ParameterList referenceSiteParams;
	
	public NonErgodicSiteResponseMod() {
		try {
			periodParams = PeriodDependentParamSet.loadCSV(Params.values(), this.getClass().getResourceAsStream("params.csv"));
			if (DD) System.out.println("Loaded default params:\n"+periodParams);
		} catch (IOException e) {
			System.err.println("Error loading default params:");
			e.printStackTrace();
			periodParams = new PeriodDependentParamSet<NonErgodicSiteResponseMod.Params>(Params.values());
		}
		periodParamsParam = new PeriodDependentParamSetParam<Params>("Period Dependent Params", periodParams);
		periodParamsParam.addParameterChangeListener(this);
		
		paramList = new ParameterList();
		paramList.addParameter(periodParamsParam);
		
		tSiteParam = new DoubleParameter("Tsite", Double.NaN);
		tSiteParam.addParameterChangeListener(this);
		paramList.addParameter(tSiteParam);
		
//		plotInterpParam = new ButtonParameter("Interpolation", "Plot Interpolation");
//		plotInterpParam.addParameterChangeListener(this);
//		paramList.addParameter(plotInterpParam);
		
		refIMTParam = new StringParameter("Reference IMT", Lists.newArrayList(REF_IMT_DEFAULT));
		refIMTParam.setValue(REF_IMT_DEFAULT);
		paramList.addParameter(refIMTParam);
		
		setReferenceSiteParams(getDefaultReferenceSiteParams());
	}
	
	static ParameterList getDefaultReferenceSiteParams() {
		ParameterList params = new ParameterList();
		
		Vs30_Param vs30 = new Vs30_Param(760);
		vs30.setValueAsDefault();
		params.addParameter(vs30);
		
		Vs30_TypeParam vs30Type = new Vs30_TypeParam();
		vs30Type.setValue(Vs30_TypeParam.VS30_TYPE_INFERRED);
		params.addParameter(vs30Type);
		
		DepthTo2pt5kmPerSecParam z25 = new DepthTo2pt5kmPerSecParam(null, true);
		z25.setValueAsDefault();
		params.addParameter(z25);
		
		DepthTo1pt0kmPerSecParam z10 = new DepthTo1pt0kmPerSecParam(null, true);
		z10.setValueAsDefault();
		params.addParameter(z10);
		
		return params;
	}
	
	public void setReferenceSiteParams(ParameterList referenceSiteParams) {
		this.referenceSiteParams = referenceSiteParams;
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
	public void setIMRParams(ScalarIMR imr) {
		// surround each with a try catch as certain IMRs may not have the given site parameter
		try {
			Parameter<Double> param = imr.getParameter(Vs30_Param.NAME);
			param.setValue(referenceSiteParams.getParameter(Double.class, Vs30_Param.NAME).getValue());
		} catch (ParameterException e) {
			// do nothing - IMR does not have this parameter
			if (D) System.out.println("IMR doesn't support Vs30");
		}
		try {
			Parameter<String> param = imr.getParameter(Vs30_TypeParam.NAME);
			param.setValue(referenceSiteParams.getParameter(String.class, Vs30_TypeParam.NAME).getValue());
		} catch (ParameterException e) {
			// do nothing - IMR does not have this parameter
			if (D) System.out.println("IMR doesn't support Vs30 Type");
		}
		try {
			Parameter<Double> param = imr.getParameter(DepthTo2pt5kmPerSecParam.NAME);
			param.setValue(referenceSiteParams.getParameter(Double.class, DepthTo2pt5kmPerSecParam.NAME).getValue()); // disable Z2500
		} catch (ParameterException e) {
			// do nothing - IMR does not have this parameter
			if (D) System.out.println("IMR doesn't support Z2.5");
		}
		try {
			Parameter<Double> param = imr.getParameter(DepthTo1pt0kmPerSecParam.NAME);
			param.setValue(referenceSiteParams.getParameter(Double.class, DepthTo1pt0kmPerSecParam.NAME).getValue());
		} catch (ParameterException e) {
			// do nothing - IMR does not have this parameter
			if (D) System.out.println("IMR doesn't support Z1.0");
		}
		if (DD) System.out.println("Set site params to default");
		
		Preconditions.checkState(imr.isIntensityMeasureSupported(REF_IMT_DEFAULT));
		// the following can be used to enable other reference IMRs. for now just force PGA
		// (will have to update interpolation for F3 if enabled and add specifics for other GMPEs)
//		StringConstraint imtConstr = (StringConstraint) refIMTParam.getConstraint();
//		ArrayList<String> allowedIMTs = Lists.newArrayList();
//		for (Parameter<?> param : imr.getSupportedIntensityMeasures())
//			allowedIMTs.add(param.getName());
//		Preconditions.checkState(!allowedIMTs.isEmpty(), "IMR doesn't support any IMTs???");
//		imtConstr.setStrings(allowedIMTs);
//		if (imtConstr.isAllowed(REF_IMT_DEFAULT)) {
//			if (D && !refIMTParam.getValue().equals(REF_IMT_DEFAULT))
//				System.out.println("Resetting reference IMT to default, "+REF_IMT_DEFAULT);
//			refIMTParam.setValue(REF_IMT_DEFAULT);
//		} else {
//			String val = allowedIMTs.get(0);
//			if (D) System.out.println("WARNING: Reference IMR doesn't support default ref IMT, "
//					+REF_IMT_DEFAULT+". Fell back to "+val);
//			refIMTParam.setValue(val);
//		}
//		refIMTParam.getEditor().refreshParamEditor();
	}

	@Override
	public void setIMT_IMT(ScalarIMR imr, Parameter<Double> imt) {
		this.imt = imt;
		if (imt.getName().equals(SA_Param.NAME)) {
			curPeriod = SA_Param.getPeriodInSA_Param(imt);
		} else {
			Preconditions.checkState(imt.getName().equals(PGA_Param.NAME), "Only SA and PGA supported");
			curPeriod = 0;
		}
		
		curParamValues = null;
		super.setIMT_IMT(imr, imt);
	}
	
	private synchronized double[] getCurParams(ScalarIMR imr) {
		if (curParamValues == null) {
			if (imr.getName().startsWith(BSSA_2014.NAME)) {
				if (interp == null || !(interp instanceof BSSA_ParamInterpolator))
					interp = new BSSA_ParamInterpolator();
			} else {
				interp = null;
			}
			if (interp == null || curPeriod <= 0)
				curParamValues = periodParams.getInterpolated(periodParams.getParams(), curPeriod);
			else
				curParamValues = interp.getInterpolated(periodParams, curPeriod, tSiteParam.getValue(), curSite);
		}
		return curParamValues;
	}
	
	void printCurParams() {
		// only used for testing
		if (curParamValues == null)
			return;
		StringBuilder sb = new StringBuilder("Current Params:\n");
		sb.append("\tPeriod\t").append(PeriodDependentParamSet.j.join(periodParams.getParams())).append("\n");
		sb.append("\t").append(curPeriod).append("\t").append(
				PeriodDependentParamSet.j.join(Doubles.asList(curParamValues))).append("\n");
		System.out.println(sb.toString());
	}
	
	public void setSiteAmpParams(double period, Map<Params, Double> values) {
		for (Params param : values.keySet())
			periodParams.set(period, param, values.get(param));
		curParamValues = null;
	}
	
	public void setTsite(double tSite) {
		tSiteParam.setValue(tSite);
	}

	@Override
	public void setIMRSiteParams(ScalarIMR imr, Site site) {
		// just update the site location, don't call setSite as it will override our site parameters
		imr.setSiteLocation(site.getLocation());
		// store site with params for empirical model
		this.curSite = site;
		// params when interpolated can depend on Vs30, clear
		curParamValues = null;
	}
	
	public void setReferenceIMT(String refIMT) {
		Preconditions.checkArgument(refIMTParam.isAllowed(refIMT), "Value is not allowed: %s", refIMT);
		refIMTParam.setValue(refIMT);
	}

	@Override
	public double getModMean(ScalarIMR imr) {
		String origIMT = imt.getName();
		Preconditions.checkState(imr.getIntensityMeasure().getName().equals(origIMT));
		double u_lnX = imr.getMean();
		if (DD) {
			String imt = imr.getIntensityMeasure().getName();
			if (imt.equals(SA_Param.NAME))
				imt += " "+(float)+SA_Param.getPeriodInSA_Param(imr.getIntensityMeasure())+"s";
			if (DD) System.out.println("Orig IMT: "+imt);
		}
		if (DD) System.out.println("Orig Mean, "+origIMT+", u_X="+Math.exp(u_lnX));
		
		// now set to to ref IMT
		String refIMT = refIMTParam.getValue();
		if (DD) System.out.println("Setting to reference IMT: "+refIMT);
		imr.setIntensityMeasure(refIMT);
		Preconditions.checkState(imr.getIntensityMeasure().getName().equals(refIMT));
		double x_ref_ln = imr.getMean();
		double x_ref = Math.exp(x_ref_ln); // ref IMR, must be linear
		if (DD) System.out.println("Ref IMT, "+refIMT+", x_ref="+x_ref);
		// set back to orig IMT
		imr.setIntensityMeasure(imt);
		Preconditions.checkState(imr.getIntensityMeasure().getName().equals(origIMT));
		
		double[] params = getCurParams(imr);
		double f1 = params[periodParams.getParamIndex(Params.F1)];
		double f2 = params[periodParams.getParamIndex(Params.F2)];
		double f3 = params[periodParams.getParamIndex(Params.F3)];
		
		if (DD) System.out.println("Calculating mean with f1="+f1+", f2="+f2+", f3="+f3);
		
		double ln_y = f1 + f2*Math.log((x_ref + f3)/f3);
		Preconditions.checkState(Doubles.isFinite(ln_y));
		if (DD) System.out.println("y="+Math.exp(ln_y));
		double yMax = Math.log(params[periodParams.getParamIndex(Params.Ymax)]);
		if (Doubles.isFinite(yMax)) {
			ln_y = Math.min(ln_y, yMax);
			if (DD) System.out.println("new y (after yMax="+yMax+"): "+Math.exp(ln_y));
		}
		
		Preconditions.checkState(Doubles.isFinite(ln_y));
		
		return u_lnX + ln_y;
	}
	
	@Override
	public double getModStdDev(ScalarIMR imr) {
		// get values for the IMT of interest
		StringParameter imrTypeParam = (StringParameter) imr.getParameter(StdDevTypeParam.NAME);
		String origIMRType = imrTypeParam.getValue();
		imrTypeParam.setValue(StdDevTypeParam.STD_DEV_TYPE_INTER);
		double origTau = imr.getStdDev();
		if (DD) System.out.println("Orig inter event, tau="+origTau);
		imrTypeParam.setValue(StdDevTypeParam.STD_DEV_TYPE_INTRA);
		double origPhi = imr.getStdDev();
		if (DD) System.out.println("Orig intra event, phi="+origPhi);
		imrTypeParam.setValue(origIMRType);
		
		double[] params = getCurParams(imr);
		double f2 = params[periodParams.getParamIndex(Params.F2)];
		double f3 = params[periodParams.getParamIndex(Params.F3)];
		double F = params[periodParams.getParamIndex(Params.F)];
		double phiS2S = params[periodParams.getParamIndex(Params.PHI_S2S)];
		double phiLnY = params[periodParams.getParamIndex(Params.PHI_lnY)];
		
		if (DD) System.out.println("Calculating std dev with f2="+f2+", f3="+f3+", F="+F+", phiS2S="+phiS2S+", phiLnY="+phiLnY);
		
		// now set to ref IMT
		String refIMT = refIMTParam.getValue();
		if (DD) System.out.println("Setting to reference IMT: "+refIMT);
		imr.setIntensityMeasure(refIMT);
		double x_ref_ln = imr.getMean();
		double x_ref = Math.exp(x_ref_ln); // ref IMR, must be linear
		if (DD) System.out.println("x_ref="+x_ref);
		// set back to orig IMT
		imr.setIntensityMeasure(imt);
		
		double term1 = Math.pow((f2*x_ref)/(x_ref+f3) + 1, 2);
		
		double phi_lnZ = Math.sqrt(term1 * (origPhi*origPhi - F*phiS2S*phiS2S) + phiLnY*phiLnY);
		
		if (DD) System.out.println("phi_lnZ="+phi_lnZ);
		
		double modStdDev = Math.sqrt(phi_lnZ*phi_lnZ + origTau*origTau);
		
		if (DD) System.out.println("modStdDev="+modStdDev);
		
		return modStdDev;
	}

	@Override
	public ParameterList getModParams() {
		return paramList;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (event.getSource() == periodParamsParam || event.getSource() == tSiteParam) {
			if (D) System.out.println("Period params change, clearing");
			curParamValues = null;
		}
//		} else if (event.getSource() == plotInterpParam) {
//			// TODO
////			getCurParams(imr)
//		}
	}
	
	public static void main(String[] args) {
		new NonErgodicSiteResponseMod();
	}

}
