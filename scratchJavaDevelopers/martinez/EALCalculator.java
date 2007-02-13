package scratchJavaDevelopers.martinez;

import java.util.ArrayList;

/**
 * This class computes the expected annualized damage factor for a given building using a hazard curve.
 * 
 * @author <a href="mailto:emartinez@usgs.gov">Eric Martinez</a>
 * @author Keith Porter
 *
 */
public class EALCalculator {
	private ArrayList<Double> IML = null;
	private ArrayList<Double> DF = null;
	private ArrayList<Double> PE = null;
	
	////////////////////////////////////////////////////////////////////////////////
	//                             Public Constructors                            //
	////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Default constructor.  IML, DF, and PE are set to null and must be manually
	 * set using the setVAR functions before you can compute any EAL.
	 */
	public EALCalculator() {}
	/**
	 * A more useful constructor.  IML, DF, and PE are set as one might assume, and
	 * the calculator is ready to compute EAL.
	 * 
	 * @param IML An <code>ArrayList</code> of doubles representing the Intensity Measure Values.
	 * @param DF An <code>ArrayList</code> of doubles representing the Damage Factor Values.
	 * @param PE An <code>ArrayList</code> of doubles representing the Probability of Exceedance Values.
	 */
	public EALCalculator(ArrayList<Double> IML, ArrayList<Double> DF, ArrayList<Double> PE) {
		this.IML = IML;
		this.DF = DF;
		this.PE = PE;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	//                               Public Functions                             //
	////////////////////////////////////////////////////////////////////////////////
	/**
	 * Same as <code>computeEAL()</code> except as specified below.  But requires arguments
	 * for IML, DF, and PE since it is accessed statically.
	 * 
	 * @param IML An <code>ArrayList</code> of doubles representing the Intensity Measure Values.
	 * @param DF An <code>ArrayList</code> of doubles representing the Damage Factor Values.
	 * @param PE An <code>ArrayList</code> of doubles representing the Probability of Exceedance Values.
	 * @return The Expected Annualized Loss for the given parameters.
	 */
	public static double computeEAL(ArrayList<Double> IML, ArrayList<Double> DF, ArrayList<Double> PE) {
		if(IML == null || DF == null || PE == null)
			throw new IllegalArgumentException("Null Values are not allowed!");
		EALCalculator calc = new EALCalculator(IML, DF, PE);
		return calc.computeEAL();
	}

	/**
	 * Computes the Expected Annualized Loss for the current values of IML, DF, and PE.
	 * @return The Expected Annualized Loss for the current parameters.
	 */
	public double computeEAL() {
		if(IML == null || DF == null || PE == null)
			throw new IllegalStateException("IML, DF, and PE must all be set before computing!");
		if(IML.size() != DF.size() || IML.size() != PE.size())
			throw new IllegalStateException("IML, DF, and PE must all be the same size for computing!");
		
		double answer = 0.0;
		double iml_cur, df_cur, pe_cur;
		double iml_pre, df_pre, pe_pre;
		double iml_delta, df_delta;
		double g, holder;
		
		double R = 0.0;
		double V = 1.0;
		
		for(int i = 1; i < IML.size(); ++i) {
			iml_cur = IML.get(i);
			iml_pre = IML.get(i-1);
			iml_delta = iml_cur - iml_pre;
			
			df_cur = DF.get(i);
			df_pre = DF.get(i-1);
			df_delta = df_cur - df_pre;
			
			pe_cur = PE.get(i);
			pe_pre = PE.get(i-1);
			
			// Get the log-linear slope of the curve
			g = (Math.log((pe_cur/pe_pre)) / iml_delta);
			
			holder = (df_pre*pe_pre)*(1 - Math.exp( (g*iml_cur) ) );
			holder -= ( ( (df_delta/iml_delta)*(pe_pre) )*( Math.exp( (g*iml_delta) ) * ( iml_delta - (1/g) ) + (1/g) ) );
			answer += holder;
		} // END: for(int i < IML.size())
		
		// Adjust the answer
		answer *= V;
		answer += R;
		
		return answer;
	}


	////////////////////////////////////////////////////////////////////////////////
	//                            Simple Getters and Setters                      //
	////////////////////////////////////////////////////////////////////////////////
	public ArrayList<Double> getDF() {
		return DF;
	}
	public void setDF(ArrayList<Double> df) {
		DF = df;
	}
	public ArrayList<Double> getIML() {
		return IML;
	}
	public void setIML(ArrayList<Double> iml) {
		IML = iml;
	}
	public ArrayList<Double> getPE() {
		return PE;
	}
	public void setPE(ArrayList<Double> pe) {
		PE = pe;
	}
	
	/**
	 * A simple function to test if the EALCalculator is working on the current
	 * machine/in the current application.  The function has predefined values
	 * for IML, DF, and PE, and when working properly, should return the value:
	 * 0.29209.
	 * 
	 * @return The test EAL value. (0.29209)
	 */
	public static double testCalc() {
		ArrayList<Double> testIML = new ArrayList<Double>(19);
		ArrayList<Double> testDF = new ArrayList<Double>(19);
		ArrayList<Double> testPE = new ArrayList<Double>(19);
		double [] IMLvals = {
				0.005, 0.007, 0.010, 0.014, 0.019, 0.027, 0.038, 0.053, 0.074,
				0.103, 0.145, 0.203, 0.284, 0.397, 0.556, 0.778, 1.090, 1.520, 2.130
		};
		double [] DFvals = {
				0.00, 0.05, 0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50,
				0.55, 0.60, 0.65, 0.75, 0.80, 0.85, 0.90, 0.95
		};
		double [] PEvals = {
				8.030E-01, 7.068E-01, 5.880E-01, 4.550E-01, 3.218E-01,
				2.068E-01, 1.216E-01, 6.583E-02, 3.380E-02, 1.681E-02,
				7.637E-03, 3.092E-03, 1.093E-03, 3.557E-04, 1.121E-04,
				3.314E-05, 7.738E-06, 1.207E-06, 6.705E-08
		};
		
		for(int i = 0; i < 19; ++i) {
			testIML.add(IMLvals[i]);
			testDF.add(DFvals[i]);
			testPE.add(PEvals[i]);
		}
		return EALCalculator.computeEAL(testIML, testDF, testPE);
	}
	////////////////////////////////////////////////////////////////////////////////
	//                             Private Functions                              //
	////////////////////////////////////////////////////////////////////////////////
}