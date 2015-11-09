package scratch.kevin.cybershake.ucerf3.safWallToWallTests;

import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.ERF2DB;
import org.opensha.sha.earthquake.param.IncludeBackgroundOption;
import org.opensha.sha.earthquake.param.IncludeBackgroundParam;
import org.opensha.sha.earthquake.param.ProbabilityModelOptions;
import org.opensha.sha.earthquake.param.ProbabilityModelParam;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.erf.FaultSystemSolutionERF;

public class FSS_ERF2DB extends ERF2DB {
	
	public FSS_ERF2DB(FaultSystemSolution sol, DBAccess db) {
		super(db);
		this.eqkRupForecast = getUCERF3_ERF(sol);
	}
	
	public static FaultSystemSolutionERF getUCERF3_ERF(FaultSystemSolution sol) {
		FaultSystemSolutionERF erf = new FaultSystemSolutionERF(sol);
		erf.setParameter(ProbabilityModelParam.NAME, ProbabilityModelOptions.POISSON);
		erf.setParameter(IncludeBackgroundParam.NAME, IncludeBackgroundOption.EXCLUDE);
		erf.getTimeSpan().setDuration(1d);
		
		erf.updateForecast();
		
		return erf;
	}

}
