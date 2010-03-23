package org.opensha.sha.earthquake.rupForecastImpl.GEM1.calc.gemOutput;


import java.util.ArrayList;
import java.util.HashMap;

import org.opensha.commons.data.Site;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMSourceData;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.calc.gemLogicTree.GemLogicTree;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.commons.UnoptimizedDeepCopy;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.util.TectonicRegionType;

public class GEMHazardCurveRepositoryList {

	private String modelName;
	private ArrayList<GEMHazardCurveRepository> hcRepList;
	private ArrayList<String> endBranchLabels;
	
	/**
	 * Constructor
	 */
	public GEMHazardCurveRepositoryList(){
		this.modelName = "";
		this.endBranchLabels = new ArrayList<String>();
		this.hcRepList = new ArrayList<GEMHazardCurveRepository>();
	}
	
	public void setModelName(String str){
		this.modelName = str;
	}
	
	public String getModelName(){
		return this.modelName;
	}
	
	/**
	 * Add a GEMHazardCurveRepository to the GEMHazardCurveRepositoryList
	 * @param hcRep
	 */
	public void add(GEMHazardCurveRepository hcRep, String lab){
		this.hcRepList.add(hcRep);
		this.endBranchLabels.add(lab);
	}
	
	/**
	 * This method computes the mean hazard curve on each node of the grid given the 
	 * GemLogicTreeInputToERF and the GemLogicTreeGMPE
	 * 
	 * @return				meanhc - Mean hazard curve on each node of the grid
	 */
	public GEMHazardCurveRepository getMeanHazardCurve(GemLogicTree<ArrayList<GEMSourceData>> ilTree, GemLogicTree<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> gmpeLT){
		
		// define GEMHazardCurveRepository for storing the mean hazard map
		// the initialization is done considering the first HazardCurveRepository in the HazardCurveRepositoryList
        UnoptimizedDeepCopy udp = new UnoptimizedDeepCopy();
		GEMHazardCurveRepository meanhc = new GEMHazardCurveRepository((ArrayList<Site>) udp.copy(hcRepList.get(0).getGridNode()),
				(ArrayList<Double>) udp.copy(hcRepList.get(0).getGmLevels()), (ArrayList<Double[]>) udp.copy(hcRepList.get(0).getProbExList()), hcRepList.get(0).getUnitsMeas());
		
		// initialize ProbExList to 0 for all nodes
		// loop over grid nodes
		for(int i=0;i<meanhc.getGridNode().size();i++){
			// loop over probability values
			for(int j=0;j<meanhc.getProbExceedanceList(i).length;j++){
				meanhc.getProbExceedanceList(i)[j] = 0.0;
			}
		}
		
		// loop over end-branches
		for (int i=0; i<hcRepList.size(); i++){
	
			// take the i-th end-branch
			GEMHazardCurveRepository hcTmp = hcRepList.get(i);
			// get the i-th end branch label
			String lab = endBranchLabels.get(i);
			
			// separate the end branch label in the part which belongs
			// to the GemLogicTreeInputToERF and that belonging to
			// GemLogicTreeGMPE
			String[] strarr = lab.split("_");
			
			// GemLogicTreeInputToERF end branch label
			String erfLab = strarr[0];
			for(int ii=1;ii<ilTree.getBranchingLevelsList().size();ii++) erfLab = erfLab+"_"+strarr[ii];
			
			// GemLogicTreeGMPE end branch label
			String gmpeLab = strarr[ilTree.getBranchingLevelsList().size()];
			for(int ii=ilTree.getBranchingLevelsList().size()+1;ii<strarr.length;ii++) gmpeLab = gmpeLab+"_"+strarr[ii];

			// Find the weight 
            // given by the product of the total weight of the InputToERF logic tree end branch
			// and the total weight of the GMPE logic tree end branch
			double wei = ilTree.getTotWeight(erfLab)*gmpeLT.getTotWeight(gmpeLab);
			
			    
			// loop over nodes
			for (int j=0; j < hcTmp.getNodesNumber(); j++){
				
				// loop over prob values
				for (int k=0; k<hcTmp.getProbExceedanceList(j).length; k++){
					meanhc.getProbExList().get(j)[k] = meanhc.getProbExceedanceList(j)[k]+hcTmp.getProbExceedanceList(j)[k]*wei;
				}
		    }
			
		}
		return meanhc;
	}
	
	/**
	 * 
	 * @param 				probEx - Probability of exceedance
	 * @param 				inputToERFLT - 
	 * @return 				meanHM
	 */
	public ArrayList<Double> getMeanHazardMap(double probEx, GemLogicTree<ArrayList<GEMSourceData>> inputToERFLT, GemLogicTree<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> gmpeLT){
		GEMHazardCurveRepository meanHC = getMeanHazardCurve(inputToERFLT,gmpeLT);
		ArrayList<Double> meanHM = meanHC.getHazardMap(probEx);
		return meanHM;
	}
	
	public ArrayList<Double> getMeanGroundMotionMap(double probEx, GemLogicTree<ArrayList<GEMSourceData>> ilTree, GemLogicTree<HashMap<TectonicRegionType, ScalarIntensityMeasureRelationshipAPI>> gmpeLT){
		
		// instantiate mean hazard map
		ArrayList<Double> meanHM =hcRepList.get(0).getHazardMap(probEx);
		// initialize to zero
		for(int i=0;i<meanHM.size();i++) meanHM.set(i, 0.0);
		
		// loop over end-branches
		for (int i=0; i<hcRepList.size(); i++){
			
			// get the current hazard map
			ArrayList<Double> HM = hcRepList.get(i).getHazardMap(probEx);
			
			// get the i-th end branch label
			String lab = endBranchLabels.get(i);
			
			// separate the end branch label in the part which belongs
			// to the GemLogicTreeInputToERF and that belonging to
			// GemLogicTreeGMPE
			String[] strarr = lab.split("_");
			// GemLogicTreeInputToERF end branch label
			String erfLab = strarr[0];
			for(int ii=1;ii<ilTree.getBranchingLevelsList().size();ii++) erfLab = erfLab+"_"+strarr[ii];
			// GemLogicTreeGMPE end branch label
			String gmpeLab = strarr[ilTree.getBranchingLevelsList().size()];
			for(int ii=ilTree.getBranchingLevelsList().size()+1;ii<strarr.length;ii++) gmpeLab = gmpeLab+"_"+strarr[ii];
			
			// Find the weight 
            // given by the product of the total weight of the InputToERF logic tree end branch
			// and the total weight of the GMPE logic tree end branch
			double wei = ilTree.getTotWeight(erfLab)*gmpeLT.getTotWeight(gmpeLab);
			
			// loop over grid nodes
			for(int ii=0;ii<meanHM.size();ii++){
				double val = meanHM.get(ii)+HM.get(ii)*wei;
				meanHM.set(ii, val);
			}
			
		}
		
		return meanHM;
	}
	
	/**
	 * 
	 * @return
	 */
	public ArrayList<GEMHazardCurveRepository> getHcRepList() {
		return hcRepList;
	}
	public void setHcRepList(ArrayList<GEMHazardCurveRepository> hcRepList) {
		this.hcRepList = hcRepList;
	}
	public ArrayList<String> getEndBranchLabels() {
		return endBranchLabels;
	}
	public void setEndBranchLabels(ArrayList<String> endBranchLabels) {
		this.endBranchLabels = endBranchLabels;
	}
	
}
