package org.opensha.gem.GEM1.calc.gemCommandLineCalculator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opensha.commons.geo.Location;
import org.opensha.gem.GEM1.util.ResultTypeParams;

/**
 * This object reads configuration file for the command line calculator
 * @author damianomonelli
 *
 */
public class CalculatorConfigData {
	
	// ERF logic tree file
	private static String erfLogicTreeFile = null;
	// GMPEs logic tree file
	private static String gmpeLogicTreeFile = null;
	// output directory path
	private static String outputDir = null;
	// minimum (moment) magnitude
	private static Double minMag = null;
	// investigation time (years)
	private static Double investigationTime = null;
	// ground motion parameter
	private static String groundMotionParam = null;
	// period
	private static Double period = null;
	// number of sigmas for GMPEs truncation
	private static Integer numSigmaTrunc = null;
	// reference Vs30 value (m/s)
	private static Double vs30Reference = null;
	// magnitude frequency distribution bin width
	private static Double mfdBinWidth = null;
	// number of treads for calculation
	private static Integer numThreads = null;
	// results type
	private static String resultType = null;
	
	// probability of exceedance
	private static Double probExc = null;
	// lower left corner of the region where to compute hazard map
	private static Location leftLowerCorner = null;
	// upper right corner of the region where to compute hazard map
	private static Location upperRightCorner = null;
	// grid spacing
	private static Double gridSpacing = null;
	
	// comment line identifier
	private static String comment = "//";
	
	/**
	 * 
	 * @param configFile: configuration file name
	 * @throws IOException 
	 */
	public CalculatorConfigData(String configFile) throws IOException{
		
        String sRecord = null;
        StringTokenizer st = null;
		
		// open file
		File file = new File(configFile);
        FileInputStream oFIS = new FileInputStream(file.getPath());
        BufferedInputStream oBIS = new BufferedInputStream(oFIS);
        BufferedReader oReader = new BufferedReader(new InputStreamReader(oBIS));
        
        // skip comment lines
        while((sRecord=oReader.readLine()).contains(comment.subSequence(0, comment.length())))
        	continue;
        this.erfLogicTreeFile = sRecord;
        
        // read GMPEs logic tree file
        // skip comment lines
        while((sRecord=oReader.readLine()).contains(comment.subSequence(0, comment.length())))
        	continue;
        this.gmpeLogicTreeFile = sRecord;
        
        // read output directory path
        // skip comment lines
        while((sRecord=oReader.readLine()).contains(comment.subSequence(0, comment.length())))
        	continue;
        this.outputDir = sRecord;
        
        // read minimum magnitude
        // skip comment lines
        while((sRecord=oReader.readLine()).contains(comment.subSequence(0, comment.length())))
        	continue;
        this.minMag = Double.parseDouble(sRecord);
        
        // read investigation time
        // skip comment lines
        while((sRecord=oReader.readLine()).contains(comment.subSequence(0, comment.length())))
        	continue;
        this.investigationTime = Double.parseDouble(sRecord);
        
        // read ground motion parameter
        while((sRecord=oReader.readLine()).contains(comment.subSequence(0, comment.length())))
        	continue;
        this.groundMotionParam = sRecord;
        
        // read period
        while((sRecord=oReader.readLine()).contains(comment.subSequence(0, comment.length())))
        	continue;
        this.period = Double.parseDouble(sRecord);
        
        // read number of sigmas for GMPE truncation
        while((sRecord=oReader.readLine()).contains(comment.subSequence(0, comment.length())))
        	continue;
        this.numSigmaTrunc = Integer.parseInt(sRecord);
        
        // read reference vs30
        // skip comment lines
        while((sRecord=oReader.readLine()).contains(comment.subSequence(0, comment.length())))
        	continue;
        this.vs30Reference = Double.parseDouble(sRecord);
        
        // read MFD bin width
        while((sRecord=oReader.readLine()).contains(comment.subSequence(0, comment.length())))
        	continue;
        this.mfdBinWidth = Double.parseDouble(sRecord);
        
        // read number of threads
        while((sRecord=oReader.readLine()).contains(comment.subSequence(0, comment.length())))
        	continue;
        this.numThreads = Integer.parseInt(sRecord);
        
        // read result type
        while((sRecord=oReader.readLine()).contains(comment.subSequence(0, comment.length())))
        	continue;
        this.resultType = sRecord;
        
        // if hazard map
        if(this.resultType.equalsIgnoreCase(ResultTypeParams.HAZARD_MAP.toString())){
        	
        	// read probability of exceedance
            while((sRecord=oReader.readLine()).contains(comment.subSequence(0, comment.length())))
            	continue;
            this.probExc = Double.parseDouble(sRecord);
        	
            // read rectangle coordinates
            while((sRecord=oReader.readLine()).contains(comment.subSequence(0, comment.length())))
            	continue;
            
            st = new StringTokenizer(sRecord);
            
            double lon = Double.parseDouble(st.nextToken());
            double lat = Double.parseDouble(st.nextToken());
            this.leftLowerCorner = new Location(lat,lon);
            
            lon = Double.parseDouble(st.nextToken());
            lat = Double.parseDouble(st.nextToken());
            this.upperRightCorner = new Location(lat,lon);
            
            // read grid spacing
            this.gridSpacing = Double.parseDouble(st.nextToken());
        	
        }
        
        // close file
		oFIS.close();
		oBIS.close();
		oReader.close();
		
	}

	public static String getErfLogicTreeFile() {
		return erfLogicTreeFile;
	}

	public static String getGmpeLogicTreeFile() {
		return gmpeLogicTreeFile;
	}

	public static String getOutputDir() {
		return outputDir;
	}

	public static Double getMinMag() {
		return minMag;
	}

	public static Double getInvestigationTime() {
		return investigationTime;
	}

	public static String getGroundMotionParam() {
		return groundMotionParam;
	}

	public static Double getPeriod() {
		return period;
	}

	public static Integer getNumSigmaTrunc() {
		return numSigmaTrunc;
	}

	public static Double getVs30Reference() {
		return vs30Reference;
	}

	public static Double getMfdBinWidth() {
		return mfdBinWidth;
	}

	public static Integer getNumThreads() {
		return numThreads;
	}

	public static String getResultType() {
		return resultType;
	}

	public static Double getProbExc() {
		return probExc;
	}

	public static Location getLeftLowerCorner() {
		return leftLowerCorner;
	}

	public static Location getUpperRightCorner() {
		return upperRightCorner;
	}

	public static Double getGridSpacing() {
		return gridSpacing;
	}

}
