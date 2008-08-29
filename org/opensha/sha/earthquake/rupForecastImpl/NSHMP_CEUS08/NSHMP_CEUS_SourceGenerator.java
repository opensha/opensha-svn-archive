/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.NSHMP_CEUS08;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.data.Location;
import org.opensha.data.region.EvenlyGriddedRectangularGeographicRegion;
import org.opensha.exceptions.RegionConstraintException;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UnsegmentedSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.Point2Vert_FaultPoisSource;
import org.opensha.sha.magdist.ArbIncrementalMagFreqDist;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

/**
 * TO DO
 * 
 * 1) make sure rup depths are handles correctly (presently 5km for M<6.5, and 1 km above); I sent an email to Harmsen
 * 2) Need to figure out what to do if max-mag gets converted to below 5.0 (getMaxMagAtLoc() method)
 * 4) is default b-value correct?
 * 5) Charleston sources should have fixed strike and fault type?
 */


/**
 * Read NSHMP backgroud seismicity files for CEUS.  
 * 
 * @author Ned Field
 *
 */
public class NSHMP_CEUS_SourceGenerator extends EvenlyGriddedRectangularGeographicRegion {

	private final static WC1994_MagLengthRelationship magLenRel = new WC1994_MagLengthRelationship();

	private final static String PATH = "org"+File.separator+"opensha"+File.separator+"sha"+File.separator+"earthquake"+File.separator+"rupForecastImpl"+File.separator+"NSHMP_CEUS08"+File.separator+"inputFiles"+File.separator;
	
	private double MIN_MAG = 5.0, DELTA_MAG = 0.1, MAX_MAG_DEFAULT = 7, DEFAULT_B_VALUE=0.9;
	// broad CEUS a-value files
	double[] adapt_cn_vals, adapt_cy_vals;
	
	// CEUS b-values
	double[] gb_vals;
	
	//CEUS Mmax files
	double[] 	gm_ab_6p6_7p1_vals,gm_ab_6p8_7p3_vals,gm_ab_7p0_7p5_vals, gm_ab_7p2_7p7_vals, 
				gm_j_6p6_7p1_vals, gm_j_6p8_7p3_vals, gm_j_7p0_7p5_vals, gm_j_7p2_7p7_vals;

		// Charleston a-value files
	double[] 	agrd_chrls3_6p8_vals, agrd_chrls3_7p1_vals, agrd_chrls3_7p3_vals, agrd_chrls3_7p5_vals,
	 			charlnA_vals, charlnB_vals, charlnarrow_vals, charnCagrid1008_vals;
	
	int locIndexForSource[], locIndexForCharlSources[];
	
	double lastCharlDuration = -10;
	int lastCharlType = -1;
	
	double ptSrcMagCutOff = 6.0;
	double fracStrikeSlip=1,fracNormal=0,fracReverse=0;
	
	ArrayList<ProbEqkSource> CharlSources;


	public NSHMP_CEUS_SourceGenerator() throws RegionConstraintException {
			super(24.6, 50, -115, -65, 0.1);


		// lat range: 24.6 to 50.0
		// lon range: -115.0 to -65.0
		// grid spacing: 0.1
		// num points: 127755

		readAllGridFiles();
		
		mkLocIndexForSource();
		
		mkLocIndexForCharlSources();
		
//		System.out.println("num Charl sources = "+this.locIndexForCharlSources.length);
	}

	
	/**
	 * This returns the total number of sources (different from the number
	 * of locs because some locs have zero a-values).  This does not include
	 * the Charleston sources
	 * @return
	 */
	public int getNumSources() {
		return locIndexForSource.length;
	}
	
	
	
	/**
	 * This reads all grid files into arrays
	 *
	 */
	private void readAllGridFiles() {


		adapt_cn_vals = readGridFile(PATH+"adapt_cn_vals.txt");
		adapt_cy_vals  = readGridFile(PATH+"adapt_cy_vals.txt");
		
		gb_vals = readGridFile(PATH+"gb_vals.txt");
		
		gm_ab_6p6_7p1_vals =   readGridFile(PATH+"gm_ab_6p6_7p1_vals.txt");
		gm_ab_6p8_7p3_vals  = readGridFile(PATH+"gm_ab_6p8_7p3_vals.txt");
		gm_ab_7p0_7p5_vals  = readGridFile(PATH+"gm_ab_7p0_7p5_vals.txt");
		gm_ab_7p2_7p7_vals  =  readGridFile(PATH+"gm_ab_7p2_7p7_vals.txt");
		gm_j_6p6_7p1_vals  =  readGridFile(PATH+"gm_j_6p6_7p1_vals.txt");
		gm_j_6p8_7p3_vals  =  readGridFile(PATH+"gm_j_6p8_7p3_vals.txt");
		gm_j_7p0_7p5_vals  =  readGridFile(PATH+"gm_j_7p0_7p5_vals.txt");
		gm_j_7p2_7p7_vals =   readGridFile(PATH+"gm_j_7p2_7p7_vals.txt");

		/* comment out until needed */
		agrd_chrls3_6p8_vals  = readGridFile(PATH+"agrd_chrls3_6p8_vals.txt");
		agrd_chrls3_7p1_vals  = readGridFile(PATH+"agrd_chrls3_7p1_vals.txt");
		agrd_chrls3_7p3_vals  = readGridFile(PATH+"agrd_chrls3_7p3_vals.txt");
		agrd_chrls3_7p5_vals  = readGridFile(PATH+"agrd_chrls3_7p5_vals.txt");

		charlnA_vals  = readGridFile(PATH+"charlnA_vals.txt");
		charlnB_vals  = readGridFile(PATH+"charlnB_vals.txt");
		charlnarrow_vals  = readGridFile(PATH+"charlnarrow_vals.txt");
		charnCagrid1008_vals  = readGridFile(PATH+"charnCagrid1008_vals.txt");
		 
	}
	

	/**
	 * this reads an NSHMP grid file.  The boolean specifies whether to add this to a running 
	 * total (sumOfAllAvals[i]).
	 * This could be modified to read binary files
	 * @param fileName
	 * @return
	 */
	public double[] readGridFile(String fileName) {
		double[] allGridVals = new double[this.getNumGridLocs()];
//		System.out.println("    Working on "+fileName);
		try { 
			InputStreamReader ratesFileReader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName));
			BufferedReader ratesFileBufferedReader = new BufferedReader(ratesFileReader);

			for(int line=0;line<getNumGridLocs();line++) {
				String lineString = ratesFileBufferedReader.readLine();
				StringTokenizer st = new StringTokenizer(lineString);
				double lon = new Double(st.nextToken());
				double lat = new Double(st.nextToken());
				double val = new Double(st.nextToken());
				

				//find index of this location
				int index = getNearestLocationIndex(new Location(lat,lon));
				
				if(index == -1)
					throw new RuntimeException("Error in getting index for "+fileName);

				allGridVals[index] = val;
			}
			ratesFileBufferedReader.close();
			ratesFileReader.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return allGridVals;
	}
	
	public double getMaxMagAtLoc(int locIndex) {
		
		double maxMagAtLoc = convertMbToMw(gm_ab_6p6_7p1_vals[locIndex], 3); // type 3 is always greater
		
		double mag = convertMbToMw(gm_ab_6p8_7p3_vals[locIndex], 3); // type 3 is always greater
		if(mag>maxMagAtLoc) maxMagAtLoc = mag;
		
		mag = convertMbToMw(gm_ab_7p0_7p5_vals[locIndex], 3); // type 3 is always greater
		if(mag>maxMagAtLoc) maxMagAtLoc = mag;
		
		mag = convertMbToMw(gm_ab_7p2_7p7_vals[locIndex], 3); // type 3 is always greater
		if(mag>maxMagAtLoc) maxMagAtLoc = mag;
		
		mag = convertMbToMw(gm_j_6p6_7p1_vals[locIndex], 3); // type 3 is always greater
		if(mag>maxMagAtLoc) maxMagAtLoc = mag;
		
		mag = convertMbToMw(gm_j_6p8_7p3_vals[locIndex], 3); // type 3 is always greater
		if(mag>maxMagAtLoc) maxMagAtLoc = mag;
		
		mag = convertMbToMw(gm_j_7p0_7p5_vals[locIndex], 3); // type 3 is always greater
		if(mag>maxMagAtLoc) maxMagAtLoc = mag;
		
		mag = convertMbToMw(gm_j_7p2_7p7_vals[locIndex], 3); // type 3 is always greater
		if(mag>maxMagAtLoc) maxMagAtLoc = mag;
		

		/* OLD way before mb --> mw conversion
		// find max mag among all contributions
		double maxMagAtLoc = gm_ab_6p6_7p1_vals[locIndex];
		if(gm_ab_6p8_7p3_vals[locIndex]>maxMagAtLoc) maxMagAtLoc = gm_ab_6p8_7p3_vals[locIndex];
		if(gm_ab_7p0_7p5_vals[locIndex]>maxMagAtLoc) maxMagAtLoc = gm_ab_7p0_7p5_vals[locIndex];
		if(gm_ab_7p2_7p7_vals[locIndex]>maxMagAtLoc) maxMagAtLoc = gm_j_6p6_7p1_vals[locIndex];
		if(gm_j_6p6_7p1_vals[locIndex]>maxMagAtLoc) maxMagAtLoc = gm_j_6p6_7p1_vals[locIndex];
		if(gm_j_6p8_7p3_vals[locIndex]>maxMagAtLoc) maxMagAtLoc = gm_j_6p8_7p3_vals[locIndex];
		if(gm_j_7p0_7p5_vals[locIndex]>maxMagAtLoc) maxMagAtLoc = gm_j_7p0_7p5_vals[locIndex];
		if(gm_j_7p2_7p7_vals[locIndex]>maxMagAtLoc) maxMagAtLoc = gm_j_7p2_7p7_vals[locIndex];
		*/
		
//		System.out.println(maxMagAtLoc);
		
//		if(maxMagAtLoc>0 && maxMagAtLoc<5)
//			System.out.println(locIndex+"\t"+maxMagAtLoc);
		
		if (maxMagAtLoc>=5)
			return maxMagAtLoc;
		else
			return MAX_MAG_DEFAULT;
		
	}
	
	/**
	 * This converts Mb to Mw, using either the Johnson method (typeConversion=3)
	 * or the Boore Atkinson method (typeConversion=4).
	 * Johnson mags are always greater than Boore Atkinson mags
	 */
	public double convertMbToMw(double mb, int typeConversion) {
		// Johnston:
		if(typeConversion == 3)
			return 1.14 + 0.24*mb+0.0933*mb*mb;
		// Boore Atkinson:
		else if (typeConversion == 4)
			return 2.715 - 0.277*mb+0.127*mb*mb;
		else
			throw new RuntimeException("that conversion type is not supported");
	}

	public SummedMagFreqDist getTotMFD_atLoc(int locIndex) {

		double maxMagAtLoc = getMaxMagAtLoc(locIndex);

		// create summed MFD
		int numMags = (int) Math.round((maxMagAtLoc-MIN_MAG)/DELTA_MAG) + 1;
		SummedMagFreqDist mfdAtLoc = new SummedMagFreqDist(MIN_MAG, maxMagAtLoc, numMags);
		
		boolean allValuesZero = true;
		
		if(adapt_cn_vals[locIndex] > 0) {
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(convertMbToMw(gm_ab_6p6_7p1_vals[locIndex], 4), 4), adapt_cn_vals[locIndex], gb_vals[locIndex], 0.01667), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_ab_6p8_7p3_vals[locIndex], 4), adapt_cn_vals[locIndex], gb_vals[locIndex], 0.0333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_ab_7p0_7p5_vals[locIndex], 4), adapt_cn_vals[locIndex], gb_vals[locIndex], 0.08333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_ab_7p2_7p7_vals[locIndex], 4), adapt_cn_vals[locIndex], gb_vals[locIndex], 0.0333), true);

			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_j_6p6_7p1_vals[locIndex], 3), adapt_cn_vals[locIndex], gb_vals[locIndex],  0.01667), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_j_6p8_7p3_vals[locIndex], 3), adapt_cn_vals[locIndex], gb_vals[locIndex],  0.0333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_j_7p0_7p5_vals[locIndex], 3), adapt_cn_vals[locIndex], gb_vals[locIndex],  0.08333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_j_7p2_7p7_vals[locIndex], 3), adapt_cn_vals[locIndex], gb_vals[locIndex],  0.0333), true);		
			
			allValuesZero = false;
		}

		if(adapt_cy_vals[locIndex] > 0) {
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_ab_6p6_7p1_vals[locIndex], 4), adapt_cy_vals[locIndex], gb_vals[locIndex], 0.0333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_ab_6p8_7p3_vals[locIndex], 4), adapt_cy_vals[locIndex], gb_vals[locIndex], 0.0667), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_ab_7p0_7p5_vals[locIndex], 4), adapt_cy_vals[locIndex], gb_vals[locIndex], 0.16667), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_ab_7p2_7p7_vals[locIndex], 4), adapt_cy_vals[locIndex], gb_vals[locIndex], 0.0667), true);

			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_j_6p6_7p1_vals[locIndex], 3), adapt_cy_vals[locIndex], gb_vals[locIndex],  0.0333), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_j_6p8_7p3_vals[locIndex], 3), adapt_cy_vals[locIndex], gb_vals[locIndex],  0.0667), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_j_7p0_7p5_vals[locIndex], 3), adapt_cy_vals[locIndex], gb_vals[locIndex],  0.16667), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(MIN_MAG, convertMbToMw(gm_j_7p2_7p7_vals[locIndex], 3), adapt_cy_vals[locIndex], gb_vals[locIndex],  0.0667), true);			

			allValuesZero = false;
		}

		// for each branch separately: 
		/*
		GutenbergRichterMagFreqDist mfd01 = getMFD(MIN_MAG, gm_ab_6p6_7p1_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);
		GutenbergRichterMagFreqDist mfd02 = getMFD(MIN_MAG, gm_ab_6p8_7p3_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);
		GutenbergRichterMagFreqDist mfd03 = getMFD(MIN_MAG, gm_ab_7p0_7p5_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);
		GutenbergRichterMagFreqDist mfd04 = getMFD(MIN_MAG, gm_ab_7p2_7p7_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);

		GutenbergRichterMagFreqDist mfd05 = getMFD(MIN_MAG, gm_j_6p6_7p1_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);
		GutenbergRichterMagFreqDist mfd06 = getMFD(MIN_MAG, gm_j_6p8_7p3_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);
		GutenbergRichterMagFreqDist mfd07 = getMFD(MIN_MAG, gm_j_7p0_7p5_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);
		GutenbergRichterMagFreqDist mfd08 = getMFD(MIN_MAG, gm_j_7p2_7p7_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);

		GutenbergRichterMagFreqDist mfd09 = getMFD(MIN_MAG, gm_ab_6p6_7p1_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);
		GutenbergRichterMagFreqDist mfd10 = getMFD(MIN_MAG, gm_ab_6p8_7p3_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);
		GutenbergRichterMagFreqDist mfd11 = getMFD(MIN_MAG, gm_ab_7p0_7p5_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);
		GutenbergRichterMagFreqDist mfd12 = getMFD(MIN_MAG, gm_ab_7p2_7p7_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex], 1);

		GutenbergRichterMagFreqDist mfd13 = getMFD(MIN_MAG, gm_j_6p6_7p1_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);
		GutenbergRichterMagFreqDist mfd14 = getMFD(MIN_MAG, gm_j_6p8_7p3_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);
		GutenbergRichterMagFreqDist mfd15 = getMFD(MIN_MAG, gm_j_7p0_7p5_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);
		GutenbergRichterMagFreqDist mfd16 = getMFD(MIN_MAG, gm_j_7p2_7p7_vals[locIndex], adapt_cn_vals[locIndex], gb_vals[locIndex],  1);
		*/
		
		if(allValuesZero)
			return null;
		else
			return mfdAtLoc;

	}
	
	public ArbIncrementalMagFreqDist getTotCharl_MFD_atLoc(int locIndex) {
		
		// create summed MFD
		double mMin = 6.8;
		double mMax = 7.5;
		double rate;
		int numMags = (int) Math.round((mMax-mMin)/DELTA_MAG) + 1;
		ArbIncrementalMagFreqDist mfdAtLoc = new ArbIncrementalMagFreqDist(mMin, mMax, numMags);
		
		// 	agrd_chrls3_6p8_vals, agrd_chrls3_7p1_vals, agrd_chrls3_7p3_vals, agrd_chrls3_7p5_vals;
		// 	charlnA_vals, charlnB_vals, charlnarrow_vals, charnCagrid1008_vals;
		
		// M 6.8
		rate = 0.1*(agrd_chrls3_6p8_vals[locIndex]+charnCagrid1008_vals[locIndex])*Math.pow(10,-1.0*6.8);
		mfdAtLoc.set(6.8, rate);

		// M 7.1
		rate = 0.1*(agrd_chrls3_7p1_vals[locIndex]+charlnA_vals[locIndex])*Math.pow(10,-1.0*7.1);
		mfdAtLoc.set(7.1, rate);

		// M 7.3
		rate = 0.225*(agrd_chrls3_7p3_vals[locIndex]+charlnarrow_vals[locIndex])*Math.pow(10,-1.0*7.3);
		mfdAtLoc.set(7.3, rate);

		// M 7.5
		rate = 0.075*(agrd_chrls3_7p5_vals[locIndex]+charlnB_vals[locIndex])*Math.pow(10,-1.0*7.5);
		mfdAtLoc.set(7.5, rate);

		return mfdAtLoc;
	}

	
	/**
	 * This creates an NSHMP mag-freq distribution from their a-value etc, 
	 * @param minMag
	 * @param maxMag
	 * @param aValue
	 * @param bValue
	 * @param weight - rates get multiplied by this number
	 * @return
	 */		
	public GutenbergRichterMagFreqDist getMFD(double minMag, double maxMag, double aValue, double bValue, double weight) {
		
//		System.out.println(minMag+"\t"+maxMag+"\t"+aValue+"\t"+bValue+"\t"+weight);

		// check for a low maxMag
		if (maxMag<5) maxMag = MAX_MAG_DEFAULT;

		minMag += DELTA_MAG/2;
		maxMag -= DELTA_MAG/2;
		
		// check for zero b-balue
		double b_val;
		if(bValue ==0) b_val = this.DEFAULT_B_VALUE;
		else b_val = bValue;
		
		int numMag = Math.round((float)((maxMag-minMag)/DELTA_MAG+1));
		GutenbergRichterMagFreqDist mfd = new GutenbergRichterMagFreqDist(minMag, numMag, DELTA_MAG, 1.0, b_val);
		mfd.scaleToIncrRate(minMag, weight*aValue*Math.pow(10,-bValue*minMag));

		return mfd;
	}

	/**
	 * Because we don't have sources where the a-values at a grid point are zero, 
	 * this makes locIndexForSource[] which provides the loc index for each source, 
	 * as well as the total number of sources.
	 */
	private void mkLocIndexForSource() {
		ArrayList<Integer>  tempArrayList = new ArrayList<Integer>();
		for(int i=0; i<this.getNumGridLocs(); i++) {
			if((adapt_cn_vals[i]+adapt_cy_vals[i]) > 0)
				tempArrayList.add(new Integer(i));
		}
		
//		System.out.println("num locs:"+this.getNumGridLocs()+"; num sources:"+tempArrayList.size());
		
		locIndexForSource = new int[tempArrayList.size()];
		for(int i=0; i<tempArrayList.size();i++)
			locIndexForSource[i] = tempArrayList.get(i).intValue();
		
	}
	
	
	
	public ArrayList<ProbEqkSource> getCharlestonSourceList(double duration,int type) {
		
		// don't duplicate if nothing has changed
		if(lastCharlDuration == duration && lastCharlType == type) {
			return CharlSources;
		}
		else {
			CharlSources = new ArrayList<ProbEqkSource>();
			int locIndex;
			for(int s=0;s<locIndexForCharlSources.length;s++) {
				locIndex = locIndexForCharlSources[s];
				ArbIncrementalMagFreqDist mfdAtLoc = getTotCharl_MFD_atLoc(locIndex);
				if(type == 0) // point gridded source
					CharlSources.add(new Point2Vert_FaultPoisSource(this.getGridLocation(locIndex), mfdAtLoc, magLenRel, duration, 10,
							fracStrikeSlip,fracNormal,fracReverse, false));
				else if (type == 1) // cross hair
					CharlSources.add(new Point2Vert_FaultPoisSource(this.getGridLocation(locIndex), mfdAtLoc, magLenRel, duration, ptSrcMagCutOff,
							fracStrikeSlip,fracNormal,fracReverse, true));
				else // random strike
					CharlSources.add(new Point2Vert_FaultPoisSource(this.getGridLocation(locIndex), mfdAtLoc, magLenRel, duration, ptSrcMagCutOff,
							fracStrikeSlip,fracNormal,fracReverse, false));
			}
			return CharlSources;			
		}
	}
	
	/**
	 * This stores the location indices and total number of Charleston sources
	 */
	private void mkLocIndexForCharlSources() {
		ArrayList<Integer>  tempArrayList = new ArrayList<Integer>();
		double rate;
		for(int i=0; i<this.getNumGridLocs(); i++) {
			rate = 	agrd_chrls3_6p8_vals[i] + agrd_chrls3_7p1_vals[i] + agrd_chrls3_7p3_vals[i] + agrd_chrls3_7p5_vals[i] +
					charlnA_vals[i] + charlnB_vals[i] + charlnarrow_vals[i] + charnCagrid1008_vals[i];
			if(rate > 0)
				tempArrayList.add(new Integer(i));
		}
		
//		System.out.println("num locs:"+this.getNumGridLocs()+"; num sources:"+tempArrayList.size());
		
		locIndexForCharlSources = new int[tempArrayList.size()];
		for(int i=0; i<tempArrayList.size();i++)
			locIndexForSource[i] = tempArrayList.get(i).intValue();
		
	}
	
	
	
		public void test() {
/*			
		gm_ab_6p6_7p1_vals,gm_ab_6p8_7p3_vals,gm_ab_7p0_7p5_vals, gm_ab_7p2_7p7_vals, 
		gm_j_6p6_7p1_vals, gm_j_6p8_7p3_vals, gm_j_7p0_7p5_vals, gm_j_7p2_7p7_vals;
		
		agrd_chrls3_6p8_vals, agrd_chrls3_7p1_vals, agrd_chrls3_7p3_vals, agrd_chrls3_7p5_vals;
		charlnA_vals, charlnB_vals, charlnarrow_vals, charnCagrid1008_vals


*/
			int num;
			System.out.println("num Locs: "+getNumGridLocs());
			num=0; for(int i=0; i<this.getNumGridLocs(); i++) if(agrd_chrls3_6p8_vals[i] > 0) num +=1;  System.out.println("char num: "+num);
			num=0; for(int i=0; i<this.getNumGridLocs(); i++) if(agrd_chrls3_7p1_vals[i] > 0) num +=1;  System.out.println("char num: "+num);
			num=0; for(int i=0; i<this.getNumGridLocs(); i++) if(agrd_chrls3_7p3_vals[i] > 0) num +=1;  System.out.println("char num: "+num);
			num=0; for(int i=0; i<this.getNumGridLocs(); i++) if(agrd_chrls3_7p5_vals[i] > 0) num +=1;  System.out.println("char num: "+num);
			num=0; for(int i=0; i<this.getNumGridLocs(); i++) if(charlnA_vals[i] > 0) num +=1;  System.out.println("char num: "+num);
			num=0; for(int i=0; i<this.getNumGridLocs(); i++) if(charlnB_vals[i] > 0) num +=1;  System.out.println("char num: "+num);
			num=0; for(int i=0; i<this.getNumGridLocs(); i++) if(charlnarrow_vals[i] > 0) num +=1;  System.out.println("char num: "+num);
			num=0; for(int i=0; i<this.getNumGridLocs(); i++) if(charnCagrid1008_vals[i] > 0) num +=1;  System.out.println("char num: "+num);
			
			/*
			double m_min, m_max;
			int numDiffs=0;
			for(int i=0; i<this.getNumGridLocs(); i++) {
				m_min = 10;
				m_max = -1;
				if(gm_ab_6p6_7p1_vals[i]<m_min) m_min = gm_ab_6p6_7p1_vals[i];
				if(gm_ab_6p6_7p1_vals[i]>m_max) m_max = gm_ab_6p6_7p1_vals[i];
				
				if(gm_ab_6p8_7p3_vals[i]<m_min) m_min = gm_ab_6p8_7p3_vals[i];
				if(gm_ab_6p8_7p3_vals[i]>m_max) m_max = gm_ab_6p8_7p3_vals[i];
				
				if(gm_ab_7p0_7p5_vals[i]<m_min) m_min = gm_ab_7p0_7p5_vals[i];
				if(gm_ab_7p0_7p5_vals[i]>m_max) m_max = gm_ab_7p0_7p5_vals[i];
				
				if(gm_ab_7p2_7p7_vals[i]<m_min) m_min = gm_ab_7p2_7p7_vals[i];
				if(gm_ab_7p2_7p7_vals[i]>m_max) m_max = gm_ab_7p2_7p7_vals[i];
				
				if(gm_j_6p6_7p1_vals[i]<m_min) m_min = gm_j_6p6_7p1_vals[i];
				if(gm_j_6p6_7p1_vals[i]>m_max) m_max = gm_j_6p6_7p1_vals[i];
				
				if(gm_j_6p8_7p3_vals[i]<m_min) m_min = gm_j_6p8_7p3_vals[i];
				if(gm_j_6p8_7p3_vals[i]>m_max) m_max = gm_j_6p8_7p3_vals[i];
				
				if(gm_j_7p0_7p5_vals[i]<m_min) m_min = gm_j_7p0_7p5_vals[i];
				if(gm_j_7p0_7p5_vals[i]>m_max) m_max = gm_j_7p0_7p5_vals[i];
				
				if(gm_j_7p2_7p7_vals[i]<m_min) m_min = gm_j_7p2_7p7_vals[i];
				if(gm_j_7p2_7p7_vals[i]>m_max) m_max = gm_j_7p2_7p7_vals[i];
				
				if((m_max-m_min) > 0.01) numDiffs +=1;
				
				
			}
			System.out.println("num diffs = "+numDiffs);
			
			*/
		}

		
		/**
		 * Get the random strike gridded source at a specified index
		 * 
		 * @param srcIndex
		 * @return
		 */
		public ProbEqkSource getRandomStrikeGriddedSource(int srcIndex, double duration) {
			int locIndex = locIndexForSource[srcIndex];
			SummedMagFreqDist mfdAtLoc = getTotMFD_atLoc(locIndex);
			return new Point2Vert_FaultPoisSource(this.getGridLocation(locIndex), mfdAtLoc, magLenRel, duration, ptSrcMagCutOff,
					fracStrikeSlip,fracNormal,fracReverse, false);
		}

		
		/**
		 * Get the the point source at a specified index
		 * 
		 * @param srcIndex
		 * @return
		 */
		public ProbEqkSource getPointGriddedSource(int srcIndex, double duration) {
			int locIndex = locIndexForSource[srcIndex];
			SummedMagFreqDist mfdAtLoc = getTotMFD_atLoc(locIndex);
			double magCutoff = 10;
			return new Point2Vert_FaultPoisSource(this.getGridLocation(locIndex), mfdAtLoc, magLenRel, duration, magCutoff,
					fracStrikeSlip,fracNormal,fracReverse, false);
		}

		/**
		 * Get Crosshair gridded source at a specified index
		 * 
		 * @param srcIndex
		 * @return
		 */
		public ProbEqkSource getCrosshairGriddedSource(int srcIndex, double duration) {
			int locIndex = locIndexForSource[srcIndex];
			SummedMagFreqDist mfdAtLoc = getTotMFD_atLoc(locIndex);
			return new Point2Vert_FaultPoisSource(this.getGridLocation(locIndex), mfdAtLoc, magLenRel, duration, ptSrcMagCutOff,
					fracStrikeSlip,fracNormal,fracReverse, true);
		}


	public static void main(String args[]) {
		try {
			NSHMP_CEUS_SourceGenerator srcGen = new NSHMP_CEUS_SourceGenerator();
			srcGen.getCharlestonSourceList(30, 0);
			//srcGen.test();
			
//			srcGen.test();
			
//			for(int i=0; i< srcGen.getNumGridLocs(); i++)
//				srcGen.getMaxMagAtLoc(i);
			
//			System.out.print(srcGen.getMaxMagAtLoc(i)+", ");
	
			/* 
			System.out.println("0\t"+srcGen.getMaxMagAtLoc(0));

			Location loc = new Location(50.0,-113.7);
			int index = srcGen.getNearestLocationIndex(loc);
			System.out.println(index+"\t"+srcGen.getMaxMagAtLoc(index));
			
			SummedMagFreqDist mfd = srcGen.getTotMFD_atLoc(index);
			System.out.println(mfd);
		*/	
//			System.out.println(srcGen.getNumGridLats());
//			System.out.println(srcGen.getNumGridLons());
//			System.out.println(srcGen.getNumGridLocs());
//			System.out.println(srcGen.getGridLocation(0));
//			System.out.println(srcGen.getGridLocation(srcGen.getNumGridLocs()-1));
		}catch(Exception e) {
			e.printStackTrace();
		}

	}
}
