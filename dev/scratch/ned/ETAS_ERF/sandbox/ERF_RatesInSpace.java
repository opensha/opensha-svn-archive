package scratch.ned.ETAS_ERF.sandbox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.gui.infoTools.CalcProgressBar;

import scratch.UCERF3.erf.FaultSystemSolutionPoissonERF;
import scratch.UCERF3.erf.UCERF2_FaultSysSol_ERF;

public class ERF_RatesInSpace {
	
	int numDepths;
	double maxDepth, depthDiscr;
	GriddedRegion region;
	
	EqksAtPoint[][] eqksAtPointArray;
	EqksAtPointTest[][] eqksAtPointArrayTest;

	/**
	 * TO DO
	 * 
	 * 1) write out unassigned locations
	 * 2) option to read and write from file
	 * 
	 * 
	 * @param griddedRegion
	 * @param erf
	 * @param maxDepth
	 * @param depthDiscr
	 * @param pointSrcDiscr
	 */
	public ERF_RatesInSpace(GriddedRegion griddedRegion, FaultSystemSolutionPoissonERF erf, double maxDepth, double depthDiscr,
			double pointSrcDiscr) {
		
		this.maxDepth=maxDepth;
		this.depthDiscr=depthDiscr;
		numDepths = (int)Math.round(maxDepth/depthDiscr);
		
		this.region = griddedRegion;
		int numRegLocs = griddedRegion.getNumLocations();
		double regSpacing = griddedRegion.getLatSpacing();
		if(griddedRegion.getLonSpacing() != regSpacing)
			throw new RuntimeException("griddedRegion.getLonSpacing() must equal griddedRegion.getLatSpacing()");
		int numPtSrcSubPts = (int)Math.round(pointSrcDiscr/regSpacing);
		double extra;
		if (numPtSrcSubPts % 2 == 0) {	// if even
			extra=0;
		}
		else							// if odd
			extra = regSpacing/2.0;
	
		System.out.println("Making eqksAtPointArray");

		eqksAtPointArray = new EqksAtPoint[numRegLocs][numDepths];
		for(int j=0;j<numDepths;j++) {
			System.out.println("working on depth "+j);
			for(int i=0;i<numRegLocs;i++) {
				eqksAtPointArray[i][j] = new EqksAtPoint();
			}
		}
		
		System.out.println("Done making eqksAtPointArray");

		double duration = erf.getTimeSpan().getDuration();
		
		double rateUnassigned=0;

		CalcProgressBar progressBar = new CalcProgressBar("Events to process", "junk");
		progressBar.displayProgressBar();
		progressBar.showProgress(true);
		
		int totNumSrc = erf.getNumSources();
		
		int numFltSystRups = erf.getNumFaultSystemSources();

		System.out.println("Starting big loop");
		int n=-1;
		for(int s=0;s<totNumSrc;s++) {
			ProbEqkSource src = erf.getSource(s);
			progressBar.updateProgress(s, totNumSrc);

			// If it's not a point sources:
			if(s<numFltSystRups) {
				for(int r=0;r<src.getNumRuptures();r++) {
					n +=1;
					ProbEqkRupture rup = src.getRupture(r);
					LocationList locsOnRupSurf = rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
					double ptRate = rup.getMeanAnnualRate(duration)/locsOnRupSurf.size();
					for(Location loc: locsOnRupSurf) {
						int regIndex = griddedRegion.indexForLocation(loc);
						int depIndex = getDepthIndex(loc.getDepth());
						if(regIndex != -1) {
							eqksAtPointArray[regIndex][depIndex].addRupRate(ptRate, n);
						}
						else
							rateUnassigned += ptRate;
					}				

				}
			}
			else {	// It's a point source
				for(ProbEqkRupture rup: src)
					if(!rup.getRuptureSurface().isPointSurface())	// make sure they're all point surfaces
						throw new RuntimeException("All ruptures for source must have point surfaces here");

				Location centerLoc = src.getRupture(0).getRuptureSurface().getFirstLocOnUpperEdge();
				//				Location regLoc = griddedRegion.getLocation(griddedRegion.indexForLocation(centerLoc));
				//				Location regLoc2 = griddedRegion.getLocation(griddedRegion.indexForLocation(centerLoc)+1);
				//				if(regLoc != null) {
				//					System.out.println(centerLoc+"\n"+regLoc+"\n"+regLoc2);
				//					System.exit(0);
				//				}
				double ptRate = src.computerTotalEquivMeanAnnualRate(duration)/(numPtSrcSubPts*numPtSrcSubPts*numDepths);
				for(int iLat=0; iLat<numPtSrcSubPts;iLat++) {
					double lat = centerLoc.getLatitude()-pointSrcDiscr/2 + iLat*regSpacing+extra;
					for(int iLon=0; iLon<numPtSrcSubPts;iLon++) {
						double lon = centerLoc.getLongitude()-pointSrcDiscr/2 + iLon*regSpacing+extra;
						int regIndex = griddedRegion.indexForLocation(new Location(lat,lon));
						if(regIndex != -1){
							for(int iDep =0; iDep<numDepths; iDep++) {
								eqksAtPointArray[regIndex][iDep].addSrcRate(ptRate, s);
								//								if(regIndex == 500 && iDep ==2)
								//									System.out.println(n+"\t"+erf.getSrcIndexForNthRup(n)+"\t"+
								//											erf.getRupIndexInSourceForNthRup(n)+"\t"+rup.getMag()+
								//											"\t"+rup.getAveRake());
							}
						}
						else {
							rateUnassigned += ptRate*numDepths;
						}
					}
				}


			}
		}
		progressBar.showProgress(false);
		System.out.println("rateUnassigned="+rateUnassigned);
		
		
//		double aveNum =0;
//		for(int j=0;j<numDepths;j++) {
//			for(int i=0;i<numRegLocs;i++) {
//				aveNum += rupIndexAtElement[i][j].size();
//			}
//		}
//		aveNum /= (numDepths*numRegLocs);
//		System.out.println("aveNum="+aveNum);
//		
//		HashSet test = rupIndexAtElement[500][2];
//		System.out.println(test);

		
		System.out.println("Now shrinking sizes");
		double totRateTest=0;
		for(int j=0;j<numDepths;j++) {
			System.out.println("working on depth "+j);
			for(int i=0;i<numRegLocs;i++) {
				eqksAtPointArray[i][j].finishAndShrinkSize(erf);
				totRateTest += eqksAtPointArray[i][j].getTotalRateInside();
			}
		}
		
		totRateTest+=rateUnassigned;
		double testRate2=0;
		for(int s=0;s<erf.getNumSources();s++) {
			ProbEqkSource src = erf.getSource(s);
			int numRups = src.getNumRuptures();
			for(int r=0; r<numRups;r++) {
				testRate2 += src.getRupture(r).getMeanAnnualRate(duration);
			}
		}
		System.out.println("\ttotRateTest="+(float)totRateTest+" should equal Rate2="+(float)testRate2+";\tratio="+(float)(totRateTest/testRate2));
	}
	
	private int getDepthIndex(double depth) {
		return (int)Math.round((depth-depthDiscr/2.0)/depthDiscr);
	}
	
	private double getDepth(int depthIndex) {
		return (double)depthIndex*depthDiscr + depthDiscr/2;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("Instantiating ERF");
		UCERF2_FaultSysSol_ERF erf = new UCERF2_FaultSysSol_ERF();
		erf.updateForecast();

//		CaliforniaRegions.RELM_GRIDDED griddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		
		System.out.println("Instantiating Region");
		GriddedRegion gridedRegion = new GriddedRegion(new CaliforniaRegions.RELM_TESTING(), 0.02, GriddedRegion.ANCHOR_0_0);
		
		long startTime = System.currentTimeMillis();
		System.out.println("Instantiating ERF_RatesInSpace");
		ERF_RatesInSpace erf_RatesInSpace = new ERF_RatesInSpace(gridedRegion,erf,24d,2d,0.1);
		System.out.println("... that took "+(System.currentTimeMillis()-startTime)/1000+" sec");


	}
	
	private void writeEqksAtPointArrayToFile(String fullpathname) {
		  try {

//"/Users/field/workspace/OpenSHA/dev/scratch/ned/rupsInFaultSystem/PreComputedSubSectionDistances/"+name;
			  File file = new File (fullpathname);

			  // Create an output stream to the file.
			  FileOutputStream file_output = new FileOutputStream (file);
			  // Wrap the FileOutputStream with a DataOutputStream
			  DataOutputStream data_out = new DataOutputStream (file_output);
			  data_out.writeInt(1);
			  data_out.writeDouble(1);
			  file_output.close ();
		  }
		  catch (IOException e) {
			  System.out.println ("IO exception = " + e );
		  }
	}
	
	private void readEqksAtPointArrayFromFile(String fullpathname) {
		File file = new File (fullpathname);

		// Read data if already computed and saved
		if(file.exists()) {
			System.out.println("Reading existing file: "+ fullpathname);
			try {
				// Wrap the FileInputStream with a DataInputStream
				FileInputStream file_input = new FileInputStream (file);
				DataInputStream data_in    = new DataInputStream (file_input );
				data_in.readDouble();
				data_in.close ();
			} catch  (IOException e) {
				System.out.println ( "IO Exception =: " + e );
			}

		}
	}


}
