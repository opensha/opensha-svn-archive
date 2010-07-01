package org.opensha.gem.GEM1.calc.gemCommandLineCalculator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.opensha.commons.param.event.ParameterChangeWarningEvent;
import org.opensha.commons.param.event.ParameterChangeWarningListener;
import org.opensha.gem.GEM1.calc.gemLogicTree.GemLogicTree;
import org.opensha.gem.GEM1.calc.gemLogicTree.GemLogicTreeBranch;
import org.opensha.gem.GEM1.calc.gemLogicTree.GemLogicTreeBranchingLevel;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.util.TectonicRegionType;

public class GmpeLogicTreeData {
	
	// gmpe logic tree
	private HashMap<TectonicRegionType,GemLogicTree<ScalarIntensityMeasureRelationshipAPI>> gmpeLogicTreeHashMap;
	
	// comment line identifier
	private static String comment = "//";
	
	// for debugging
	private static Boolean D = false;
	
	public GmpeLogicTreeData(String gmpeInputFile) throws IOException{
		
		// instatiate gmpe logic tree
		gmpeLogicTreeHashMap = new HashMap<TectonicRegionType,GemLogicTree<ScalarIntensityMeasureRelationshipAPI>>();
		
	    ParameterChangeWarningEvent event = null;
        
        String sRecord = null;
        
        String activeShallowGmpeNames = null;
        String activeShallowGmpeWeights = null;
        
        String stableShallowGmpeNames = null;
        String stableShallowGmpeWeights = null;
        
        String subductionInterfaceGmpeNames = null;
        String subductionInterfaceGmpeWeights = null;
		
        String subductionIntraSlabGmpeNames = null;
        String subductionIntraSlabGmpeWeights = null;
        
		// open file
		File file = new File(gmpeInputFile);
        FileInputStream oFIS = new FileInputStream(file.getPath());
        BufferedInputStream oBIS = new BufferedInputStream(oFIS);
        BufferedReader oReader = new BufferedReader(new InputStreamReader(oBIS));
        
        if(D) System.out.println("\n\n");
        if(D) System.out.println("GMPE Logic Tree structure");
        
        sRecord = oReader.readLine();
        // start reading the file
        while(sRecord!=null){
        	
        	// skip comments or empty lines
            while(sRecord.contains(comment.subSequence(0, comment.length())) || sRecord.isEmpty()){
            	sRecord = oReader.readLine();
            	continue;
            }
            
            // if gmpes for Active shallow crust are defined
            if(sRecord.equalsIgnoreCase(TectonicRegionType.ACTIVE_SHALLOW.toString())){
            	
            	// read names
            	sRecord = oReader.readLine();
                while(sRecord.contains(comment.subSequence(0, comment.length())) || sRecord.isEmpty()){
                	sRecord = oReader.readLine();
                	continue;
                }
                activeShallowGmpeNames = sRecord;
                
                if(D) System.out.println("Gmpes for "+TectonicRegionType.ACTIVE_SHALLOW+": "+activeShallowGmpeNames);
                
                // read weights
            	sRecord = oReader.readLine();
                while(sRecord.contains(comment.subSequence(0, comment.length())) || sRecord.isEmpty()){
                	sRecord = oReader.readLine();
                	continue;
                }
                activeShallowGmpeWeights = sRecord;
                
                if(D) System.out.println("Gmpes weights: "+activeShallowGmpeWeights);
                
                sRecord = oReader.readLine();
                
            }
            
            // if gmpes for stable continental crust are defined
            else if(sRecord.equalsIgnoreCase(TectonicRegionType.STABLE_SHALLOW.toString())){
            	
            	// read names
            	sRecord = oReader.readLine();
                while(sRecord.contains(comment.subSequence(0, comment.length())) || sRecord.isEmpty()){
                	sRecord = oReader.readLine();
                	continue;
                }
                stableShallowGmpeNames = sRecord;
                
                if(D) System.out.println("Gmpes for "+TectonicRegionType.STABLE_SHALLOW+": "+stableShallowGmpeNames);
                
                // read weights
            	sRecord = oReader.readLine();
                while(sRecord.contains(comment.subSequence(0, comment.length())) || sRecord.isEmpty()){
                	sRecord = oReader.readLine();
                	continue;
                }
                stableShallowGmpeWeights = sRecord;
                
                if(D) System.out.println("Gmpes weights: "+stableShallowGmpeWeights);
                
                sRecord = oReader.readLine();
                
            }
            
            // if gmpes for subduction interface are defined
            else if(sRecord.equalsIgnoreCase(TectonicRegionType.SUBDUCTION_INTERFACE.toString())){
            	
            	// read names
            	sRecord = oReader.readLine();
                while(sRecord.contains(comment.subSequence(0, comment.length())) || sRecord.isEmpty()){
                	sRecord = oReader.readLine();
                	continue;
                }
                subductionInterfaceGmpeNames = sRecord;
                
                if(D) System.out.println("Gmpes for "+TectonicRegionType.SUBDUCTION_INTERFACE+": "+subductionInterfaceGmpeNames);
                
                // read weights
            	sRecord = oReader.readLine();
                while(sRecord.contains(comment.subSequence(0, comment.length())) || sRecord.isEmpty()){
                	sRecord = oReader.readLine();
                	continue;
                }
                subductionInterfaceGmpeWeights = sRecord;
                
                if(D) System.out.println("Gmpes weights: "+subductionInterfaceGmpeWeights);
                
                sRecord = oReader.readLine();
                
            }
            
            // if gmpes for subduction intraslab are defined
            else if(sRecord.equalsIgnoreCase(TectonicRegionType.SUBDUCTION_SLAB.toString())){
            	
            	// read names
            	sRecord = oReader.readLine();
                while(sRecord.contains(comment.subSequence(0, comment.length())) || sRecord.isEmpty()){
                	sRecord = oReader.readLine();
                	continue;
                }
                subductionIntraSlabGmpeNames = sRecord;
                
                if(D) System.out.println("Gmpes for "+TectonicRegionType.SUBDUCTION_SLAB+": "+subductionIntraSlabGmpeNames);
                
                // read weights
            	sRecord = oReader.readLine();
                while(sRecord.contains(comment.subSequence(0, comment.length())) || sRecord.isEmpty()){
                	sRecord = oReader.readLine();
                	continue;
                }
                subductionIntraSlabGmpeWeights = sRecord;
                
                if(D) System.out.println("Gmpes weights: "+subductionIntraSlabGmpeWeights);
                
                sRecord = oReader.readLine();
            }
            
        	
        }// end if sRecord!=null
        
        // create logic tree structure for gmpe in active shallow region
        if(activeShallowGmpeNames!=null){
        	// add logic tree to logic tree list
        	gmpeLogicTreeHashMap.put(TectonicRegionType.ACTIVE_SHALLOW,createGmpeLogicTree(activeShallowGmpeNames, activeShallowGmpeWeights));
        	
        } // end active shallow
        
        // create logic tree structure for gmpe in stable shallow region
        if(stableShallowGmpeNames!=null){
        	// add logic tree to logic tree list
        	gmpeLogicTreeHashMap.put(TectonicRegionType.STABLE_SHALLOW, createGmpeLogicTree(stableShallowGmpeNames, stableShallowGmpeWeights));
        } // end stable shallow
        
        // create logic tree structure for gmpe in subduction interface
        if(subductionInterfaceGmpeNames!=null){
        	// add logic tree to logic tree list
        	gmpeLogicTreeHashMap.put(TectonicRegionType.SUBDUCTION_INTERFACE, createGmpeLogicTree(subductionInterfaceGmpeNames, subductionInterfaceGmpeWeights));
        }
        
        // create logic tree structure for gmpe in subduction intraslab
        if(subductionIntraSlabGmpeNames!=null){
        	// add logic tree to logic tree list
        	gmpeLogicTreeHashMap.put(TectonicRegionType.SUBDUCTION_SLAB, createGmpeLogicTree(subductionIntraSlabGmpeNames, subductionIntraSlabGmpeWeights));
        }
        
		
	}
	
	/**
	 * create logic tree from string of names and string of weights
	 */
	
	private GemLogicTree<ScalarIntensityMeasureRelationshipAPI> createGmpeLogicTree(String gmpeNames, String gmpeWeights){
		
		StringTokenizer name = new StringTokenizer(gmpeNames);
    	
		StringTokenizer weight = new StringTokenizer(gmpeWeights);
    	
    	if(name.countTokens()!=weight.countTokens()){
    		System.out.println("Number of gmpes do not corresponds to number of weights!");
    		System.out.println("Check your input!");
    		System.out.println("Execution stopped!");
    		System.exit(0);
    	}
    	
    	// create logic tree
    	GemLogicTree<ScalarIntensityMeasureRelationshipAPI> gmpeLogicTree
    	 = new GemLogicTree<ScalarIntensityMeasureRelationshipAPI>();
    	
    	// create branching level
        GemLogicTreeBranchingLevel branchingLevel = new GemLogicTreeBranchingLevel(1,"Gmpe Uncertainties",-1);
        
        // define branch
        GemLogicTreeBranch branch = null;
        
    	// number of branches
    	int numBranch = name.countTokens();
    	
    	// loop over branches
    	for(int i=0;i<numBranch;i++){
    		
    		// gmpe name
    		String gmpeName = name.nextToken();
    		
    		// gmpe weight
    		double gmpeWeight = Double.parseDouble(weight.nextToken());
    		
    		branch = new GemLogicTreeBranch((i+1), gmpeName, gmpeWeight);
    		
    		branchingLevel.addTreeBranch(branch);
    		
    	}
    	
    	// add branching level to logic tree
    	gmpeLogicTree.addBranchingLevel(branchingLevel);
    	
    	return gmpeLogicTree;
	}
	
	/**
	 * 
	 * @param event
	 * @return
	 */
	private static ParameterChangeWarningListener ParameterChangeWarningListener(
			ParameterChangeWarningEvent event) {
		return null;
	}
	
	public HashMap<TectonicRegionType,GemLogicTree<ScalarIntensityMeasureRelationshipAPI>> getGmpeLogicTreeHashMap(){
		return this.gmpeLogicTreeHashMap;
	}
	
}
