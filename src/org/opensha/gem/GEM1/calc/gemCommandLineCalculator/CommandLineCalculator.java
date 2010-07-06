package org.opensha.gem.GEM1.calc.gemCommandLineCalculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.opensha.gem.GEM1.calc.gemLogicTree.GemLogicTreeBranch;
import org.opensha.gem.GEM1.calc.gemLogicTree.GemLogicTreeBranchingLevel;
import org.opensha.sha.util.TectonicRegionType;

public class CommandLineCalculator {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SecurityException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
		
//		/Users/damianomonelli/Documents/Workspace/OpenSHA/src/org/opensha/gem/GEM1/data/command_line_input_files/CalculatorConfig.inp
		
		// calculator configuration file
		String calculatorConfigFile = null;
		
		// set up reader from standard input
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(inp);

        // ask for calculator configuration file
	    System.out.println("Enter calculator configuration file (press Enter to continue): ");
	    // read file name
	    calculatorConfigFile = br.readLine();
	    
	    // read configuration file
	    CalculatorConfigData calcConfig = new CalculatorConfigData(calculatorConfigFile);
	    
	    // read ERF logic tree file
	    ErfLogicTreeData erfLogicTree = new ErfLogicTreeData(calcConfig.getErfLogicTreeFile());
	    
	    // print to standard output the erf logic tree structure
	    // just to be sure that input file is read correctly
	    erfLogicTree.getErfLogicTree().printGemLogicTreeStructure();
	    
	    // read GMPE logic tree file and set gmpe logic tree
	    GmpeLogicTreeData gmpeLogicTree = new GmpeLogicTreeData(calcConfig.getGmpeLogicTreeFile(),calcConfig.getComponent(),calcConfig.getIntensityMeasureType(),
	    		                               calcConfig.getPeriod(), calcConfig.getDamping(), calcConfig.getTruncationType(), calcConfig.getTruncationLevel(),
	    		                               calcConfig.getStandardDeviationType(), calcConfig.getVs30Reference());
	    
    	// get logic tree for each tectonic type and print the structure to standard output
	    // again to check that the input file is read correctly
	    Iterator<TectonicRegionType> tecRegTypeIter =  gmpeLogicTree.getGmpeLogicTreeHashMap().keySet().iterator();
	    while(tecRegTypeIter.hasNext()){
	    	TectonicRegionType trt = tecRegTypeIter.next();
	    	System.out.println("Gmpe Logic Tree for "+trt);
	    	gmpeLogicTree.getGmpeLogicTreeHashMap().get(trt).printGemLogicTreeStructure();
	    }
	    
	    
	    

	}

}
