package scratchJavaDevelopers.martinez;

import java.util.ArrayList;
import java.util.ListIterator;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;

import Jama.Matrix;

import scratchJavaDevelopers.martinez.VulnerabilityModels.VulnerabilityModel;

public class LossCurveCalculator {

	public LossCurveCalculator() {
		
	}
	
	public ArbitrarilyDiscretizedFunc getLossCurve(ArbitrarilyDiscretizedFunc hazFunc, VulnerabilityModel curVulnModel) {
		ArbitrarilyDiscretizedFunc lossCurve = new ArbitrarilyDiscretizedFunc();
		// Get the damage factors (these will be x values later)...
		double[] dfs = curVulnModel.getDEMDFVals();
		
		// Get the probabilities of exceedance (hazard curve)...
		ListIterator iter = hazFunc.getYValuesIterator();
		ArrayList<Double> pelist = new ArrayList<Double>();
		while(iter.hasNext())
			pelist.add((Double) iter.next());
		
		pelist = diffIt(pelist);
		
		Matrix PEMatrix = new Matrix(pelist.size(), 1);
		for(int i = 0; i < pelist.size(); ++i)
			PEMatrix.set(i, 0, pelist.get(i));
		
		// Get the DEM
		Matrix DEMMatrix = new Matrix(curVulnModel.getDEMMatrix());
		
		Matrix result = DEMMatrix.times(PEMatrix);
		
		for(int i = 0; i < dfs.length; ++i)
			lossCurve.set(dfs[i], result.get(i, 0));
		
		return lossCurve;
	}
	
	public static ArrayList<Double> diffIt(ArrayList<Double> vals) {
		ArrayList<Double> rtn = new ArrayList<Double>();
		int i = 1;
		for(i=1; i < vals.size(); ++i)
			rtn.add( Math.abs( vals.get(i) - vals.get(i-1) ) );
		rtn.add(vals.get(--i));
		return rtn;
	}

}
