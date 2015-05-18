package org.opensha.sha.cybershake.calc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFunc;
import org.opensha.sha.cybershake.bombay.BombayBeachHazardCurveCalc;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.CybershakeRun;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.ERF2DB;
import org.opensha.sha.cybershake.db.ERF2DBAPI;
import org.opensha.sha.cybershake.db.PeakAmplitudesFromDB;
import org.opensha.sha.cybershake.db.PeakAmplitudesFromDBAPI;
import org.opensha.sha.cybershake.db.Runs2DB;
import org.opensha.sha.cybershake.db.SiteInfo2DB;
import org.opensha.sha.cybershake.db.SiteInfo2DBAPI;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

public class HazardCurveComputation {


	private static final double CUT_OFF_DISTANCE = 200;
	private PeakAmplitudesFromDBAPI peakAmplitudes;
	private ERF2DBAPI erfDB;
	private SiteInfo2DBAPI siteDB;
	private Runs2DB runs2db;
	public static final double CONVERSION_TO_G = 980;
	
	private RuptureProbabilityModifier rupProbMod = null;
	private RuptureVariationProbabilityModifier rupVarProbMod = null;

	//	private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>();

	public HazardCurveComputation(DBAccess db){
		peakAmplitudes = new PeakAmplitudesFromDB(db);
		erfDB = new ERF2DB(db);
		siteDB = new SiteInfo2DB(db);
		runs2db = new Runs2DB(db);
	}
	
	public void setPeakAmpsAccessor(PeakAmplitudesFromDBAPI peakAmplitudes) {
		this.peakAmplitudes = peakAmplitudes;
	}

	public void setRupProbModifier(RuptureProbabilityModifier rupProbMod) {
		this.rupProbMod = rupProbMod;
	}
	
	public void setRupVarProbModifier(RuptureVariationProbabilityModifier rupVarProbMod) {
		this.rupVarProbMod = rupVarProbMod;
	}

	/**
	 * 
	 * @return the List of supported Peak amplitudes
	 */
	public ArrayList<CybershakeIM> getSupportedSA_PeriodStrings(){

		return peakAmplitudes.getSupportedIMs();
	}

	/**
	 * 
	 * @return the List of supported Peak amplitudes for a given site, ERF ID, SGT Var ID, and Rup Var ID
	 */
	public ArrayList<CybershakeIM> getSupportedSA_PeriodStrings(int runID){

		return peakAmplitudes.getSupportedIMs(runID);
	}

	/**
	 * Computes the Hazard Curve at the given site 
	 * @param imlVals
	 * @param site
	 * @param erfName
	 * @param srcId
	 * @param rupId
	 * @param imType
	 */
	public DiscretizedFunc computeDeterministicCurve(ArrayList<Double> imlVals, String site,int erfId, int sgtVariation, int rvid,
			int velModelID, int srcId,int rupId,CybershakeIM imType){
		CybershakeRun run = getRun(site, erfId, sgtVariation, rvid, velModelID);
		if (run == null)
			return null;
		else
			return computeDeterministicCurve(imlVals, run, srcId, rupId, imType);
	}

	private CybershakeRun getRun(String site, int erfID, int sgtVarID, int rupVarID, int velModelID) {
		int siteID = siteDB.getSiteId(site);
		ArrayList<CybershakeRun> runIDs = runs2db.getRuns(siteID, erfID, sgtVarID, rupVarID, velModelID, null, null, null, null);
		if (runIDs == null || runIDs.size() < 0)
			return null;
		return runIDs.get(0);
	}

	/**
	 * Computes the Hazard Curve at the given runID 
	 * @param imlVals
	 * @param runID
	 * @param srcId
	 * @param rupId
	 * @param imType
	 */
	public DiscretizedFunc computeDeterministicCurve(ArrayList<Double> imlVals, int runID,
			int srcId,int rupId,CybershakeIM imType){
		CybershakeRun run = runs2db.getRun(runID);
		if (run == null)
			return null;
		else
			return computeDeterministicCurve(imlVals, run, srcId, rupId, imType);
	}


	/**
	 * Computes the Hazard Curve at the given run 
	 * @param imlVals
	 * @param run
	 * @param srcId
	 * @param rupId
	 * @param imType
	 */
	public DiscretizedFunc computeDeterministicCurve(ArrayList<Double> xVals, CybershakeRun run,
			int srcId,int rupId,CybershakeIM imType){

		DiscretizedFunc hazardFunc = new ArbitrarilyDiscretizedFunc();
		int numIMLs  = xVals.size();
		for(int i=0; i<numIMLs; ++i) hazardFunc.set((xVals.get(i)).doubleValue(), 1.0);

		int runID = run.getRunID();

//		double qkProb = erfDB.getRuptureProb(run.getERFID(), srcId, rupId);
		double qkProb = 1.0;
		List<Double> imVals;
		try {
			imVals = peakAmplitudes.getIM_Values(runID, srcId, rupId, imType);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		if (rupProbMod != null)
			qkProb = rupProbMod.getModifiedProb(srcId, rupId, qkProb);
		handleRupture(xVals, imVals, hazardFunc, qkProb, srcId, rupId, rupVarProbMod, run, imType);

		for(int j=0; j<numIMLs; ++j) 
			hazardFunc.set(hazardFunc.getX(j),(1-hazardFunc.getY(j)));

		return hazardFunc;
	}

	/**
	 * Computes the Hazard Curve at the given site 
	 * @param imlVals
	 * @param site
	 * @param erfName
	 * @param imType
	 */
	public DiscretizedFunc computeHazardCurve(ArrayList<Double> imlVals, String site,String erfName,int sgtVariation, int rvid, int velModelID, CybershakeIM imType){
		int erfId = erfDB.getInserted_ERF_ID(erfName);
		System.out.println("for erfname: " + erfName + " found ERFID: " + erfId + "\n");
		return computeHazardCurve(imlVals, site, erfId, sgtVariation, rvid, velModelID, imType);
	}

	/**
	 * Computes the Hazard Curve at the given site 
	 * @param imlVals
	 * @param site
	 * @param erfName
	 * @param imType
	 */
	public DiscretizedFunc computeHazardCurve(ArrayList<Double> imlVals, String site,int erfId,int sgtVariation, int rvid, int velModelID, CybershakeIM imType){
		CybershakeRun run = getRun(site, erfId, sgtVariation, rvid, velModelID);
		if (run == null)
			return null;
		else
			return computeHazardCurve(imlVals, run, imType);
	}

	/**
	 * Computes the Hazard Curve at the given site 
	 * @param imlVals
	 * @param site
	 * @param erfName
	 * @param imType
	 */
	public DiscretizedFunc computeHazardCurve(ArrayList<Double> imlVals, int runID, CybershakeIM imType){
		CybershakeRun run = runs2db.getRun(runID);
		if (run == null)
			return null;
		else
			return computeHazardCurve(imlVals, run, imType);
	}

	/**
	 * Computes the Hazard Curve at the given site 
	 * @param imlVals
	 * @param site
	 * @param erfName
	 * @param imType
	 */
	public DiscretizedFunc computeHazardCurve(List<Double> xVals, CybershakeRun run,
			CybershakeIM imType) {
		List<Integer> srcIdList = siteDB.getSrcIdsForSite(run.getSiteID(), run.getERFID());
		return computeHazardCurve(xVals, run, imType, srcIdList);
	}
	

	public DiscretizedFunc computeHazardCurve(List<Double> xVals, CybershakeRun run,
			CybershakeIM imType, List<Integer> srcIdList) {
		DiscretizedFunc hazardFunc = new ArbitrarilyDiscretizedFunc();
		int siteID = run.getSiteID();
		int erfID = run.getERFID();
		int runID = run.getRunID();
		int numIMLs  = xVals.size();
		for(int i=0; i<numIMLs; ++i) hazardFunc.set((xVals.get(i)).doubleValue(), 1.0);

		int numSrcs = srcIdList.size();
		for(int srcIndex =0;srcIndex<numSrcs;++srcIndex){
			//			updateProgress(srcIndex, numSrcs);
//			System.out.println("Source " + srcIndex + " of " + numSrcs + ".");
			int srcId = srcIdList.get(srcIndex);
			List<Integer> rupIdList = siteDB.getRupIdsForSite(siteID, erfID, srcId);
			int numRupSize = rupIdList.size();
			for(int rupIndex = 0;rupIndex<numRupSize;++rupIndex){
				int rupId = rupIdList.get(rupIndex);
				double qkProb = erfDB.getRuptureProb(erfID, srcId, rupId);
				if (rupProbMod != null)
					qkProb = rupProbMod.getModifiedProb(srcId, rupId, qkProb);
				if (qkProb == 0) {
					// if the probability is zero and we're not modifying anything then we can skip this rupture
					if (rupVarProbMod == null)
						continue;
					else {
						List<Double> modProbs = rupVarProbMod.getVariationProbs(srcId, rupId, qkProb, run, imType);
						if (modProbs == null || modProbs.isEmpty())
							continue;
					}
				}
				List<Double> imVals;
				try {
					imVals = peakAmplitudes.getIM_Values(runID, srcId, rupId, imType);
				} catch (SQLException e) {
					throw new RuntimeException("SQL Exception calculating curve for runID="+runID
							+", src="+srcId+", rup="+rupId+", imType="+imType.getID(), e);
				}
				handleRupture(xVals, imVals, hazardFunc, qkProb, srcId, rupId, rupVarProbMod, run, imType);
			}
		}

		for(int j=0; j<numIMLs; ++j) 
			hazardFunc.set(hazardFunc.getX(j),(1-hazardFunc.getY(j)));

		return hazardFunc;
	}
	
	public static void handleRupture(List<Double> xVals, List<Double> imVals,
			DiscretizedFunc hazardFunc, double qkProb, int sourceID, int rupID,
			RuptureVariationProbabilityModifier rupProbVarMod, CybershakeRun run, CybershakeIM im) {
		if (rupProbVarMod == null) {
			// we don't have a rupture variation probability modifier
			handleRupture(xVals, imVals, hazardFunc, qkProb);
			return;
		}
		List<Double> modProbs = rupProbVarMod.getVariationProbs(sourceID, rupID, qkProb, run, im);
		if (modProbs == null) {
			// we have a rup var prob mod, but it doesn't apply to this rupture
			handleRupture(xVals, imVals, hazardFunc, qkProb);
			return;
		}
		
		// old way which splits up into multiple cum dist funcs but can be inaccurate at low probabilities
//		for (Double prob : modProbs.keySet()) {
//			List<Integer> rvs = modProbs.get(prob);
//			Preconditions.checkState(!rvs.isEmpty());
//			Preconditions.checkState(Doubles.isFinite(prob) && prob >= 0);
//			if (prob == 0)
//				continue;
//			ArrayList<Double> modVals = Lists.newArrayList();
//			for (int rvID : modProbs.get(prob))
//				modVals.add(imVals.get(rvID));
//			handleRupture(xVals, modVals, hazardFunc, prob);
//		}
		
		Preconditions.checkState(modProbs.size() == imVals.size(), "%s != %s", modProbs.size(), imVals.size());
		
		// new way which weights the empirical exceedance distribution with the modified probabilities
		double modQkProb = 0d;
		ArbDiscrEmpiricalDistFunc function = new ArbDiscrEmpiricalDistFunc();
		for (int i=0; i<modProbs.size(); i++) {
			double prob = modProbs.get(i);
			double imVal = imVals.get(i);
			Preconditions.checkState(Doubles.isFinite(prob) && prob >= 0);
			if (prob == 0)
				continue;
			modQkProb += prob;
			function.set(imVal/CONVERSION_TO_G,prob);
		}
		setIMLProbs(xVals,hazardFunc, function.getNormalizedCumDist(), modQkProb);
	}
	
	public static void handleRupture(List<Double> xVals, List<Double> imVals,
			DiscretizedFunc hazardFunc, double qkProb) {
		ArbDiscrEmpiricalDistFunc function = new ArbDiscrEmpiricalDistFunc();
		for (double val : imVals) {
			function.set(val/CONVERSION_TO_G,1);
		}
		setIMLProbs(xVals,hazardFunc, function.getNormalizedCumDist(), qkProb);
	}
	
	public static DiscretizedFunc setIMLProbs( List<Double> imlVals,DiscretizedFunc hazFunc,
			ArbitrarilyDiscretizedFunc normalizedFunc, double rupProb) {
		// find prob. for each iml value
		int numIMLs  = imlVals.size();
		for(int i=0; i<numIMLs; ++i) {
			double iml = imlVals.get(i);
			double prob=0;
			if(iml < normalizedFunc.getMinX()) prob = 0;
			else if(iml > normalizedFunc.getMaxX()) prob = 1;
			else prob = normalizedFunc.getInterpolatedY(iml);
//			else prob = normalizedFunc.getInterpolatedY_inLogYDomain(iml);
//			else prob = normalizedFunc.getInterpolatedY_inLogXLogYDomain(iml);
			double y = hazFunc.getY(i);
			double newY = y*Math.pow(1-rupProb,1-prob);
//			System.out.println("IML: " + iml + " rupProb: " + rupProb + " POE: " + prob + " oldY: " + y + " newY: " + y);
			hazFunc.set(i, newY);
		}
		return hazFunc;
	}

	public PeakAmplitudesFromDBAPI getPeakAmpsAccessor() {
		return peakAmplitudes;
	}

	//    public void addProgressListener(ProgressListener listener) {
	//    	progressListeners.add(listener);
	//    }
	//	
	//    public void removeProgressListener(ProgressListener listener) {
	//    	progressListeners.remove(listener);
	//    }
	//    
	//    private void updateProgress(int current, int total) {
	//    	for (ProgressListener listener : progressListeners) {
	//    		listener.setProgress(current, total);
	//    	}
	//    }
}