package scratchJavaDevelopers.martinez;

import java.util.ArrayList;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;

import scratchJavaDevelopers.martinez.VulnerabilityModels.VulnerabilityModel;

public class LossCurveCalculator {

	public LossCurveCalculator() {
		
	}
	
	public ArbitrarilyDiscretizedFunc getLossCurve(ArbitrarilyDiscretizedFunc hazFunc, VulnerabilityModel curVulnModel) {
		ArbitrarilyDiscretizedFunc lossCurve = new ArbitrarilyDiscretizedFunc();
		ArrayList<Double> dfs = curVulnModel.getDFVals();
		int numVals = hazFunc.getNum();
		for(int i = 0; i < numVals; ++i) {
			double xval = hazFunc.getX(i);
			double yval = hazFunc.getY(i) * dfs.get(i);
			lossCurve.set(xval, yval);
		}
		
		return lossCurve;
	}

}
