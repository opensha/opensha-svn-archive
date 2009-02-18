package org.opensha.cybershake.db;

import java.sql.SQLException;
import java.util.ArrayList;

import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
//import org.opensha.util.ProgressListener;

public class HazardCurveComputation {

	
	private static final double CUT_OFF_DISTANCE = 200;
	private PeakAmplitudesFromDBAPI peakAmplitudes;
	private ERF2DBAPI erfDB;
	private SiteInfo2DBAPI siteDB;
	public static final double CONVERSION_TO_G = 980;
	
//	private ArrayList<ProgressListener> progressListeners = new ArrayList<ProgressListener>();
	
	public HazardCurveComputation(DBAccess db){
		peakAmplitudes = new PeakAmplitudesFromDB(db);
		erfDB = new ERF2DB(db);
		siteDB = new SiteInfo2DB(db);
	}
	
	
	
	/**
	 * 
	 * @returns the List of supported Peak amplitudes
	 */
	public ArrayList<CybershakeIM> getSupportedSA_PeriodStrings(){
		
		return peakAmplitudes.getSupportedIMs();
	}
	
	/**
	 * 
	 * @returns the List of supported Peak amplitudes for a given site, ERF ID, SGT Var ID, and Rup Var ID
	 */
	public ArrayList<CybershakeIM> getSupportedSA_PeriodStrings(int siteID, int erfID, int sgtVariation, int rupVarID){
		
		return peakAmplitudes.getSupportedIMs(siteID, erfID, sgtVariation, rupVarID);
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
	public DiscretizedFuncAPI computeDeterministicCurve(ArrayList imlVals, String site,int erfId, int sgtVariation, int rvid,
			                                     int srcId,int rupId,CybershakeIM imType){
		
		DiscretizedFuncAPI hazardFunc = new ArbitrarilyDiscretizedFunc();
		int siteId = siteDB.getSiteId(site);
		int numIMLs  = imlVals.size();
		for(int i=0; i<numIMLs; ++i) hazardFunc.set(((Double)imlVals.get(i)).doubleValue(), 1.0);
		
		
		double qkProb = erfDB.getRuptureProb(erfId, srcId, rupId);
		ArbDiscrEmpiricalDistFunc function = new ArbDiscrEmpiricalDistFunc();
		ArrayList<Integer> rupVariations = peakAmplitudes.getRupVarationsForRupture(erfId, srcId, rupId);
		int size = rupVariations.size();
		for(int i=0;i<size;++i){
			int rupVarId =  rupVariations.get(i);
			double imVal = peakAmplitudes.getIM_Value(siteId, erfId, sgtVariation, rvid, srcId, rupId, rupVarId, imType);
			function.set(imVal/CONVERSION_TO_G,1);
		}
		setIMLProbs(imlVals,hazardFunc, function.getNormalizedCumDist(), qkProb);
		
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
	public DiscretizedFuncAPI computeHazardCurve(ArrayList imlVals, String site,String erfName,int sgtVariation, int rvid, CybershakeIM imType){
		int erfId = erfDB.getInserted_ERF_ID(erfName);
		System.out.println("for erfname: " + erfName + " found ERFID: " + erfId + "\n");
		return computeHazardCurve(imlVals, site, erfId, sgtVariation, rvid, imType);
	}
	
	
	/**
	 * Computes the Hazard Curve at the given site 
	 * @param imlVals
	 * @param site
	 * @param erfName
	 * @param imType
	 */
	public DiscretizedFuncAPI computeHazardCurve(ArrayList imlVals, String site,int erfId,int sgtVariation, int rvid, CybershakeIM imType){
		DiscretizedFuncAPI hazardFunc = new ArbitrarilyDiscretizedFunc();
		int siteId = siteDB.getSiteId(site);
		int numIMLs  = imlVals.size();
		for(int i=0; i<numIMLs; ++i) hazardFunc.set(((Double)imlVals.get(i)).doubleValue(), 1.0);
		
		ArrayList<Integer> srcIdList = siteDB.getSrcIdsForSite(site, erfId);
		int numSrcs = srcIdList.size();
		for(int srcIndex =0;srcIndex<numSrcs;++srcIndex){
//			updateProgress(srcIndex, numSrcs);
			System.out.println("Source " + srcIndex + " of " + numSrcs + ".");
			int srcId = srcIdList.get(srcIndex);
			ArrayList<Integer> rupIdList = siteDB.getRupIdsForSite(site, erfId, srcId);
			int numRupSize = rupIdList.size();
			for(int rupIndex = 0;rupIndex<numRupSize;++rupIndex){
				int rupId = rupIdList.get(rupIndex);
				double qkProb = erfDB.getRuptureProb(erfId, srcId, rupId);
				ArbDiscrEmpiricalDistFunc function = new ArbDiscrEmpiricalDistFunc();
				ArrayList<Double> imVals;
				try {
					imVals = peakAmplitudes.getIM_Values(siteId, erfId, sgtVariation, rvid, srcId, rupId, imType);
				} catch (SQLException e) {
					return null;
				}
				for (double val : imVals) {
					function.set(val/CONVERSION_TO_G,1);
				}
//				ArrayList<Integer> rupVariations = peakAmplitudes.getRupVarationsForRupture(erfId, srcId, rupId);
//				int size = rupVariations.size();
//				for(int i=0;i<size;++i){
//					int rupVarId =  rupVariations.get(i);
//					double imVal = peakAmplitudes.getIM_Value(siteId, erfId, sgtVariation, rvid, srcId, rupId, rupVarId, imType);
//					function.set(imVal/CONVERSION_TO_G,1);
//				}
				setIMLProbs(imlVals,hazardFunc, function.getNormalizedCumDist(), qkProb);
			}
		}
	     
	    for(int j=0; j<numIMLs; ++j) 
	    	hazardFunc.set(hazardFunc.getX(j),(1-hazardFunc.getY(j)));

		return hazardFunc;
	}
	
    protected DiscretizedFuncAPI setIMLProbs( ArrayList imlVals,DiscretizedFuncAPI hazFunc,
      		ArbitrarilyDiscretizedFunc normalizedFunc, double rupProb) {
        // find prob. for each iml value
    	int numIMLs  = imlVals.size();
        for(int i=0; i<numIMLs; ++i) {
          double iml = ((Double)imlVals.get(i)).doubleValue();
          double prob=0;
          if(iml < normalizedFunc.getMinX()) prob = 0;
          else if(iml > normalizedFunc.getMaxX()) prob = 1;
          else prob = normalizedFunc.getInterpolatedY(iml);
          //System.out.println(iml + "\t" + prob);
          hazFunc.set(i, hazFunc.getY(i)*Math.pow(1-rupProb,1-prob));
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
