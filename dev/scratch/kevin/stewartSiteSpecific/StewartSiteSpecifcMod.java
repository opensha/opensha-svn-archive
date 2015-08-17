package scratch.kevin.stewartSiteSpecific;

import org.opensha.commons.data.Site;
import org.opensha.commons.exceptions.ParameterException;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.StringParameter;
import org.opensha.sha.imr.ScalarIMR;
import org.opensha.sha.imr.mod.AbstractAttenRelMod;
import org.opensha.sha.imr.param.IntensityMeasureParams.PGA_Param;
import org.opensha.sha.imr.param.IntensityMeasureParams.SA_Param;
import org.opensha.sha.imr.param.OtherParams.StdDevTypeParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo1pt0kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.DepthTo2pt5kmPerSecParam;
import org.opensha.sha.imr.param.SiteParams.Vs30_Param;
import org.opensha.sha.imr.param.SiteParams.Vs30_TypeParam;

import com.google.common.base.Preconditions;
import com.google.common.primitives.Doubles;

public class StewartSiteSpecifcMod extends AbstractAttenRelMod implements ParameterChangeListener {
	
	private static final boolean D = true;
	
	public static final String NAME = "Stewart 2014 Site Specific";
	public static final String SHORT_NAME = "Stewart2014";
	
	private enum Params {
		F1("f1"),
		F2("f2"),
		F3("f3"),
		PHI_lnY("ϕ lnY"),
		PHI_S2S("ϕ S2S"),
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
	
	private ParameterList paramList;
	
	private Parameter<Double> imt;
	
	public StewartSiteSpecifcMod() {
		periodParams = new PeriodDependentParamSet<StewartSiteSpecifcMod.Params>(Params.values());
		periodParamsParam = new PeriodDependentParamSetParam<Params>("Period Dependent Params", periodParams);
		periodParamsParam.addParameterChangeListener(this);
		
		paramList = new ParameterList();
		paramList.addParameter(periodParamsParam);
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
		// TODO Auto-generated method stub

		// surround each with a try catch as certain IMRs may not have the given site parameter
		try {
			Parameter<Double> param = imr.getParameter(Vs30_Param.NAME);
			param.setValue(760d);
		} catch (ParameterException e) {
			// do nothing - IMR does not have this parameter
			if (D) System.out.println("IMR doesn't support Vs30");
		}
		try {
			Parameter<String> param = imr.getParameter(Vs30_TypeParam.NAME);
			param.setValue(Vs30_TypeParam.VS30_TYPE_INFERRED);
		} catch (ParameterException e) {
			// do nothing - IMR does not have this parameter
			if (D) System.out.println("IMR doesn't support Vs30 Type");
		}
		try {
			Parameter<Double> param = imr.getParameter(DepthTo2pt5kmPerSecParam.NAME);
			param.setValue(null); // disable Z2500
		} catch (ParameterException e) {
			// do nothing - IMR does not have this parameter
			if (D) System.out.println("IMR doesn't support Z2.5");
		}
		try {
			Parameter<Double> param = imr.getParameter(DepthTo1pt0kmPerSecParam.NAME);
			param.setValue(null);
		} catch (ParameterException e) {
			// do nothing - IMR does not have this parameter
			if (D) System.out.println("IMR doesn't support Z1.0");
		}
		if (D) System.out.println("Set site params to default");
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
	
	private synchronized double[] getCurParams() {
		if (curParamValues == null)
			curParamValues = periodParams.getInterpolated(periodParams.getParams(), curPeriod);
		return curParamValues;
	}

	@Override
	public void setIMRSiteParams(ScalarIMR imr, Site site) {
		// just update the site location, don't call setSite as it will override our site parameters
		imr.setSiteLocation(site.getLocation());
	}

	@Override
	public double getModMean(ScalarIMR imr) {
		double u_lnX = imr.getMean();
		
		// now set to to PGA
		imr.setIntensityMeasure(PGA_Param.NAME);
		double x_ref_ln = imr.getMean();
		double x_ref = Math.exp(x_ref_ln); // ref IMR, must be linear
		// set back to orig IMT
		imr.setIntensityMeasure(imt);
		
		double[] params = getCurParams();
		double f1 = params[periodParams.getParamIndex(Params.F1)];
		double f2 = params[periodParams.getParamIndex(Params.F2)];
		double f3 = params[periodParams.getParamIndex(Params.F3)];
		
		double ln_y = f1 + f2*Math.log((x_ref + f3)/f3);
		
		Preconditions.checkState(Doubles.isFinite(ln_y));
		
		return u_lnX + ln_y;
	}

	@Override
	public double getModStdDev(ScalarIMR imr) {
		// TODO
		StringParameter imrTypeParam = (StringParameter) imr.getParameter(StdDevTypeParam.NAME);
		String origIMRType = imrTypeParam.getValue();
		imrTypeParam.setValue(StdDevTypeParam.STD_DEV_TYPE_INTER);
		double interStdDev = imr.getStdDev();
		imrTypeParam.setValue(StdDevTypeParam.STD_DEV_TYPE_INTRA);
		double intraStdDev = imr.getStdDev();
		imrTypeParam.setValue(origIMRType);
		
//		double phi_lnZ = Math.sqrt(a)
		
//		imr.set
//		
//		double u_lnX = imr.getMean();
//		
//		// now set to to PGA
//		imr.setIntensityMeasure(PGA_Param.NAME);
//		double x_ref_ln = imr.getMean();
//		double x_ref = Math.exp(x_ref_ln); // ref IMR, must be linear
//		// set back to orig IMT
//		imr.setIntensityMeasure(imt);
//		
//		double[] params = getCurParams();
//		double f1 = params[periodParams.getParamIndex(Params.F1)];
//		double f2 = params[periodParams.getParamIndex(Params.F2)];
//		double f3 = params[periodParams.getParamIndex(Params.F3)];
//		
//		double ln_y = f1 + f2*Math.log((x_ref + f3)/f3);
//		
//		Preconditions.checkState(Doubles.isFinite(ln_y));
//		
//		return u_lnX + ln_y;
		return imr.getStdDev();
	}

	@Override
	public ParameterList getModParams() {
		// TODO Auto-generated method stub
		return paramList;
	}

	@Override
	public void parameterChange(ParameterChangeEvent event) {
		if (D) System.out.println("Period params change, clearing");
		curParamValues = null;
	}

}
