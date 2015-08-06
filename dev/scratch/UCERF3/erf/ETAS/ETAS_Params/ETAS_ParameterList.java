package scratch.UCERF3.erf.ETAS.ETAS_Params;

import org.opensha.commons.param.ParameterList;

/**
 * This holds a complete list of ETAS parameters
 * @author field
 *
 */
public class ETAS_ParameterList extends ParameterList {
	
	private static final long serialVersionUID = 1L;

	ETAS_ProductivityParam_k kParam = new ETAS_ProductivityParam_k();
	ETAS_FractionSpontaneousParam fractSpontParam = new ETAS_FractionSpontaneousParam();
	ETAS_TemporalDecayParam_p pParam = new ETAS_TemporalDecayParam_p();
	ETAS_MinTimeParam_c cParam = new ETAS_MinTimeParam_c();
	ETAS_DistanceDecayParam_q qParam = new ETAS_DistanceDecayParam_q();
	ETAS_MinDistanceParam_d dParam = new ETAS_MinDistanceParam_d();
	ETAS_ImposeGR_SamplingParam imposeGR = new ETAS_ImposeGR_SamplingParam();
	ETAS_ApplyLongTermRatesInSamplingParam applyLongTermRatesParam = new ETAS_ApplyLongTermRatesInSamplingParam();
	U3ETAS_ProbabilityModelParam probModelParam = new U3ETAS_ProbabilityModelParam();
	
	public ETAS_ParameterList() {
		this.addParameter(probModelParam);
		this.addParameter(kParam);
		this.addParameter(fractSpontParam);
		this.addParameter(pParam);
		this.addParameter(cParam);
		this.addParameter(qParam);
		this.addParameter(dParam);
		this.addParameter(imposeGR);
		this.addParameter(applyLongTermRatesParam);
		
	}
	
	public double get_k() {return kParam.getValue();}
	public double getFractSpont() {return fractSpontParam.getValue();}
	public double get_p() {return pParam.getValue();}
	public double get_c() {return cParam.getValue();}
	public double get_q() {return qParam.getValue();}
	public double get_d() {return dParam.getValue();}
	public boolean getImposeGR() {return imposeGR.getValue();}
	public boolean getApplyLongTermRates() {return applyLongTermRatesParam.getValue();}
	public U3ETAS_ProbabilityModelOptions getU3ETAS_ProbModel() {return probModelParam.getValue();}
	
	
	public void set_d_MinDist(double minDist) { dParam.setValue(minDist);}
	public void setImposeGR(boolean value) { imposeGR.setValue(value);}
	public void setApplyLongTermRates(boolean value) { applyLongTermRatesParam.setValue(value);}
	public void setU3ETAS_ProbModel(U3ETAS_ProbabilityModelOptions value) { probModelParam.setValue(value); }

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
