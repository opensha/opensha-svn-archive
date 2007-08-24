package org.opensha.cybershake.db;

import java.util.ArrayList;

import org.opensha.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;

public class HazardCurveComputation {

	
	private static final double CUT_OFF_DISTANCE = 200;
	private PeakAmplitudesFromDBAPI peakAmplitudes;
	private ERF2DBAPI erfDB;
	private SiteInfo2DBAPI siteDB;
	private double CONVERSION_TO_G = 980;
	
	public HazardCurveComputation(DBAccess db){
		peakAmplitudes = new PeakAmplitudesFromDB(db);
		erfDB = new ERF2DB(db);
		siteDB = new SiteInfo2DB(db);
	}
	
	
	
	/**
	 * 
	 * @returns the List of supported Peak amplitudes
	 */
	public ArrayList<String> getSupportedSA_PeriodStrings(){
		
		return peakAmplitudes.getSupportedSA_PeriodList();
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
	public DiscretizedFuncAPI computeDeterministicCurve(ArrayList imlVals, String site,String erfName,
			                                     int srcId,int rupId,String imType){
		
		DiscretizedFuncAPI hazardFunc = new ArbitrarilyDiscretizedFunc();
		int erfId = erfDB.getInserted_ERF_ID(erfName);
		int siteId = siteDB.getSiteId(site);
		int numIMLs  = imlVals.size();
		for(int i=0; i<numIMLs; ++i) hazardFunc.set(((Double)imlVals.get(i)).doubleValue(), 1.0);
		
		
		double qkProb = erfDB.getRuptureProb(erfId, srcId, rupId);
		ArbDiscrEmpiricalDistFunc function = new ArbDiscrEmpiricalDistFunc();
		ArrayList<Integer> rupVariations = peakAmplitudes.getRupVarationsForRupture(erfId, srcId, rupId);
		int size = rupVariations.size();
		for(int i=0;i<size;++i){
			int rupVarId =  rupVariations.get(i);
			double imVal = peakAmplitudes.getIM_Value(siteId, erfId, srcId, rupId, rupVarId, imType);
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
	public DiscretizedFuncAPI computeHazardCurve(ArrayList imlVals, String site,String erfName,String imType){
		System.out.println("imType = "+imType);
		DiscretizedFuncAPI hazardFunc = new ArbitrarilyDiscretizedFunc();
		int erfId = erfDB.getInserted_ERF_ID(erfName);
		int siteId = siteDB.getSiteId(site);
		int numIMLs  = imlVals.size();
		for(int i=0; i<numIMLs; ++i) hazardFunc.set(((Double)imlVals.get(i)).doubleValue(), 1.0);
		
		ArrayList<Integer> srcIdList = siteDB.getSrcIdsForSite(site);
		int numSrcs = srcIdList.size();
		for(int srcIndex =0;srcIndex<numSrcs;++srcIndex){
			int srcId = srcIdList.get(srcIndex);
			ArrayList<Integer> rupIdList = siteDB.getRupIdsForSite(site, srcId);
			int numRupSize = rupIdList.size();
			for(int rupIndex = 0;rupIndex<numRupSize;++rupIndex){
				int rupId = rupIdList.get(rupIndex);
				double qkProb = erfDB.getRuptureProb(erfId, srcId, rupId);
				ArbDiscrEmpiricalDistFunc function = new ArbDiscrEmpiricalDistFunc();
				ArrayList<Integer> rupVariations = peakAmplitudes.getRupVarationsForRupture(erfId, srcId, rupId);
				int size = rupVariations.size();
				for(int i=0;i<size;++i){
					int rupVarId =  rupVariations.get(i);
					double imVal = peakAmplitudes.getIM_Value(siteId, erfId, srcId, rupId, rupVarId, imType);
					function.set(imVal/CONVERSION_TO_G,1);
				}
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

	
}
