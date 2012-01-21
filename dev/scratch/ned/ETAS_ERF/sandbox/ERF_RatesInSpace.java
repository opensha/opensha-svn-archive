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
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.exceptions.GMT_MapException;
import org.opensha.commons.geo.BorderType;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.geo.Region;
import org.opensha.commons.mapping.gmt.GMT_MapGenerator;
import org.opensha.commons.mapping.gmt.gui.GMT_MapGuiBean;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.gui.infoTools.ImageViewerWindow;

import scratch.UCERF3.erf.FaultSystemSolutionPoissonERF;
import scratch.UCERF3.erf.UCERF2_FaultSysSol_ERF;
import scratch.ned.ETAS_ERF.EqksInGeoBlock;
import scratch.ned.ETAS_ERF.IntegerPDF_FunctionSampler;

public class ERF_RatesInSpace {
	
	int numDepths, numRegLocs;
	double maxDepth, depthDiscr;
	GriddedRegion region;
	
	// this stores the rates of erf ruptures that go unassigned (outside the region here)
	double rateUnassigned;
	double totRate;
	
	EqksAtPoint[][] eqksAtPointArray;
	
	IntegerPDF_FunctionSampler pointSampler;
	
	
	
	
	/**
	 * TO DO
	 * 
	 * 1) option to read and write from file
	 * 
	 * 
	 * @param griddedRegion
	 * @param erf
	 * @param maxDepth
	 * @param depthDiscr
	 * @param pointSrcDiscr - the grid spacing of off-fault/background events
	 */
	public ERF_RatesInSpace(GriddedRegion griddedRegion, FaultSystemSolutionPoissonERF erf, double maxDepth, double depthDiscr,
			String oututFileNameWithPath) {
		
		this.maxDepth=maxDepth;
		this.depthDiscr=depthDiscr;
		numDepths = (int)Math.round(maxDepth/depthDiscr);
		
		this.region = griddedRegion;
		numRegLocs = griddedRegion.getNumLocations();
		
		readEqksAtPointArrayFromFile(oututFileNameWithPath);
		
		testRates(erf);
	}
	
	
	

	/**
	 * TO DO
	 * 
	 * @param griddedRegion
	 * @param erf
	 * @param maxDepth
	 * @param depthDiscr
	 * @param pointSrcDiscr - the grid spacing of off-fault/background events
	 */
	public ERF_RatesInSpace(GriddedRegion griddedRegion, FaultSystemSolutionPoissonERF erf, double maxDepth, double depthDiscr,
			double pointSrcDiscr, String oututFileNameWithPath) {
		
		this.maxDepth=maxDepth;
		this.depthDiscr=depthDiscr;
		numDepths = (int)Math.round(maxDepth/depthDiscr);
		
		this.region = griddedRegion;
		numRegLocs = griddedRegion.getNumLocations();
		double regSpacing = griddedRegion.getLatSpacing();
		if(griddedRegion.getLonSpacing() != regSpacing)
			throw new RuntimeException("griddedRegion.getLonSpacing() must equal griddedRegion.getLatSpacing()");
		
		
		int numPtSrcSubPts = (int)Math.round(pointSrcDiscr/regSpacing);
		double extra;	// this is needed to get the right intervals withing each point source
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
		
		rateUnassigned=0;

		CalcProgressBar progressBar = new CalcProgressBar("Events to process", "junk");
		progressBar.displayProgressBar();
		progressBar.showProgress(true);
		
		int totNumSrc = erf.getNumSources();
		
		int numFltSystRups = erf.getNumFaultSystemSources();

		System.out.println("Starting big loop");
		for(int s=0;s<totNumSrc;s++) {
			ProbEqkSource src = erf.getSource(s);
			progressBar.updateProgress(s, totNumSrc);

			// If it's not a point sources:
			if(s<numFltSystRups) {
				for(int r=0;r<src.getNumRuptures();r++) {
					ProbEqkRupture rup = src.getRupture(r);
					LocationList locsOnRupSurf = rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
					double ptRate = rup.getMeanAnnualRate(duration)/locsOnRupSurf.size();
					for(Location loc: locsOnRupSurf) {
						int regIndex = griddedRegion.indexForLocation(loc);
						int depIndex = getDepthIndex(loc.getDepth());
						if(regIndex != -1) {
							eqksAtPointArray[regIndex][depIndex].addRupRate(ptRate, erf.getIndexN_ForSrcAndRupIndices(s, r));
						}
						else {
							rateUnassigned += ptRate;
//							System.out.println("0\t"+loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth());
						}
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
				double ptRate = src.computeTotalEquivMeanAnnualRate(duration)/(numPtSrcSubPts*numPtSrcSubPts*numDepths);
				// distribution this among the locations within the space represented by the point source
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
//							System.out.println("1\t"+centerLoc.getLatitude()+"\t"+centerLoc.getLongitude()+"\t"+centerLoc.getDepth());
						}
					}
				}
			}
		}
		progressBar.showProgress(false);
		System.out.println("rateUnassigned="+rateUnassigned);
		
		System.out.println("Now shrinking sizes");
		for(int j=0;j<numDepths;j++) {
			System.out.println("working on depth "+j);
			for(int i=0;i<numRegLocs;i++) {
				eqksAtPointArray[i][j].finishAndShrinkSize(erf);
			}
		}
		
		testRates(erf);
		
		// write results to file
		if(oututFileNameWithPath != null)
			writeEqksAtPointArrayToFile(oututFileNameWithPath);
		
	}
	
	/**
	 * This method
	 */
	public IntegerPDF_FunctionSampler getPointSampler() {
		if(pointSampler == null) {
			pointSampler = new IntegerPDF_FunctionSampler(numDepths*numRegLocs);
			int index=0;
			for(int j=0;j<numDepths;j++) {
				for(int i=0;i<numRegLocs;i++) {
					pointSampler.set(index,eqksAtPointArray[i][j].getTotalRateInside());
					
					// check
					int[] indices = getRegAndDepIndicesForSamplerIndex(index);
					if(indices[1] != j || indices[0] != i)
						throw new RuntimeException("Error - iDepths "+indices[1]+" vs "+j+"\tiReg: "+indices[0]+" vs "+i);
					// check 2
					if(index !=  getSamplerIndexForRegAndDepIndices(indices[0], indices[1]))
						throw new RuntimeException("Error");

					index += 1;
				}
			}			
		}
		return pointSampler;
	}
	
	/**
	 * Region index is first element, and depth index is second
	 * @param index
	 * @return
	 */
	private int[] getRegAndDepIndicesForSamplerIndex(int index) {
		
		int[] indices = new int[2];
		indices[1] = (int)Math.floor((double)index/(double)numRegLocs);	// depth index
		indices[0] = index - indices[1]*numRegLocs;						// region index
		return indices;
	}
	
	private EqksAtPoint getEqksAtPointForSamplerIndex(int index) {
		int[] indices = getRegAndDepIndicesForSamplerIndex(index);
		return eqksAtPointArray [indices[0]][indices[1]];		// region is first index
	}

	
	private int getSamplerIndexForRegAndDepIndices(int iReg,int iDep) {
		return iDep*numRegLocs+iReg;
	}


	public void testRates(FaultSystemSolutionPoissonERF erf) {
		
		System.out.println("Testing total rate");
		
		double duration = erf.getTimeSpan().getDuration();

		totRate=0;
		for(int j=0;j<numDepths;j++) {
			for(int i=0;i<numRegLocs;i++) {
				totRate += eqksAtPointArray[i][j].getTotalRateInside();
			}
		}
		totRate+=rateUnassigned;
		
		double testRate2=0;
		for(int s=0;s<erf.getNumSources();s++) {
			ProbEqkSource src = erf.getSource(s);
			int numRups = src.getNumRuptures();
			for(int r=0; r<numRups;r++) {
				testRate2 += src.getRupture(r).getMeanAnnualRate(duration);
			}
		}
		System.out.println("\ttotRateTest="+(float)totRate+" should equal Rate2="+(float)testRate2+";\tratio="+(float)(totRate/testRate2));
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
		
		String testFileName = "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/testBinaryFile";

		ERF_RatesInSpace erf_RatesInSpace = new ERF_RatesInSpace(gridedRegion,erf,24d,2d,testFileName);
		erf_RatesInSpace.plotRatesMap("test", true, "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/mapTest");
		erf_RatesInSpace.plotOrigERF_RatesMap("orig test", true, "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/mapOrigTest", erf);
		erf_RatesInSpace.plotRandomSampleRatesMap("random test", true, "/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_ERF/mapRandomTest", erf,10000);
//		ERF_RatesInSpace erf_RatesInSpace = new ERF_RatesInSpace(gridedRegion,erf,24d,2d,0.1,testFileName);

		System.out.println("... that took "+(System.currentTimeMillis()-startTime)/1000+" sec");
		
		System.out.println("Making point sampler");
		startTime = System.currentTimeMillis();
		IntegerPDF_FunctionSampler pointSampler = erf_RatesInSpace.getPointSampler();
		System.out.println("Making point sampler took "+(System.currentTimeMillis()-startTime)/1000+" sec");
		startTime = System.currentTimeMillis();
		System.out.println("Sampling 2000 points");
		for(int i=0;i<2000;i++)
			pointSampler.getRandomInt();
		System.out.println("Sampling that took "+(System.currentTimeMillis()-startTime)/1000+" sec");


	}
	
	private void writeEqksAtPointArrayToFile(String fullpathname) {
		try {
			
			System.out.println("Writing results to file: "+fullpathname);

			File file = new File (fullpathname);

			// Create an output stream to the file.
			FileOutputStream file_output = new FileOutputStream (file);
			// Wrap the FileOutputStream with a DataOutputStream
			DataOutputStream data_out = new DataOutputStream (file_output);
			
			// first write the rate of unassigned events
			data_out.writeDouble(rateUnassigned);
			
			for(int j=0;j<numDepths;j++) {
				System.out.println("writing depth "+j);
				for(int i=0;i<numRegLocs;i++) {
					EqksAtPoint eqksAtPt = eqksAtPointArray[i][j];

					int[] rupIndexN_Array = eqksAtPt.getRupIndexN_Array();
					double[] rupRateInsideArray = eqksAtPt.getRupRateInsideArray();
					double[] rupFractInsideArray = eqksAtPt.getRupFractInsideArray();
					int[] srcIndexN_Array = eqksAtPt.getSrcIndexN_Array();
					double[] srcRateInsideArray = eqksAtPt.getSrcRateInsideArray();
					double[] srcFractInsideArray = eqksAtPt.getSrcFractInsideArray();

					data_out.writeInt(rupIndexN_Array.length);
					for(int n=0;n<rupIndexN_Array.length;n++) {
						data_out.writeInt(rupIndexN_Array[n]);
						data_out.writeDouble(rupRateInsideArray[n]);
						data_out.writeDouble(rupFractInsideArray[n]);
					}

					data_out.writeInt(srcIndexN_Array.length);
					for(int n=0;n<srcIndexN_Array.length;n++) {
						data_out.writeInt(srcIndexN_Array[n]);
						data_out.writeDouble(srcRateInsideArray[n]);
						data_out.writeDouble(srcFractInsideArray[n]);
					}
				}
			}
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
				
				eqksAtPointArray = new EqksAtPoint[numRegLocs][numDepths];

				// first read the rate of unassigned events
				rateUnassigned = data_in.readDouble();
				
				for(int j=0;j<numDepths;j++) {
					System.out.println("reading depth "+j);
					for(int i=0;i<numRegLocs;i++) {
						
						int numRup = data_in.readInt();
						int[] rupIndexN_Array = new int[numRup];
						double[] rupRateInsideArray = new double[numRup];
						double[] rupFractInsideArray = new double[numRup];
						for(int n=0;n<numRup;n++) {
							rupIndexN_Array[n] = data_in.readInt();
							rupRateInsideArray[n] = data_in.readDouble();
							rupFractInsideArray[n] = data_in.readDouble();
						}

						int numSrc = data_in.readInt();
						int[] srcIndexN_Array = new int[numSrc];
						double[] srcRateInsideArray = new double[numSrc];
						double[] srcFractInsideArray = new double[numSrc];
						for(int n=0;n<numSrc;n++) {
							srcIndexN_Array[n] = data_in.readInt();
							srcRateInsideArray[n] = data_in.readDouble();
							srcFractInsideArray[n] = data_in.readDouble();
						}
						
						eqksAtPointArray[i][j] = new EqksAtPoint(rupIndexN_Array, rupRateInsideArray, rupFractInsideArray,
								srcIndexN_Array, srcRateInsideArray, srcFractInsideArray);
					}
				}
				data_in.close ();
				
			} catch  (IOException e) {
				System.out.println ( "IO Exception =: " + e );
			}

		}
	}
	
	
	/**
	 * This plots the rates in space for the RELM region (rates are summed inside each spatial bin).
	 * 
	 * Compare this to what produced by the plotOrigERF_RatesMap(*) method (should be same)
	 * 
	 * @param label - plot label
	 * @param local - whether GMT map is made locally or on server
	 * @param dirName
	 * @return
	 */
	public String plotRatesMap(String label, boolean local, String dirName) {
		
		GMT_MapGenerator mapGen = new GMT_MapGenerator();
		mapGen.setParameter(GMT_MapGenerator.GMT_SMOOTHING_PARAM_NAME, false);
		mapGen.setParameter(GMT_MapGenerator.TOPO_RESOLUTION_PARAM_NAME, GMT_MapGenerator.TOPO_RESOLUTION_NONE);
		mapGen.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME,31.5);		// -R-125.4/-113.0/31.5/43.0
		mapGen.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME,43.0);
		mapGen.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME,-125.4);
		mapGen.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME,-113.0);
		mapGen.setParameter(GMT_MapGenerator.LOG_PLOT_NAME,true);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,-3.5);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,1.5);


		CaliforniaRegions.RELM_GRIDDED mapGriddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		GriddedGeoDataSet xyzDataSet = new GriddedGeoDataSet(mapGriddedRegion, true);
		
		// initialize values to zero
		for(int i=0; i<xyzDataSet.size();i++) xyzDataSet.set(i, 0);
		
		for(int j=0;j<numDepths;j++) {
			for(int i=0;i<numRegLocs;i++) {
				double ptRate = eqksAtPointArray[i][j].getTotalRateInside();
				Location loc = region.getLocation(i);
				int locIndex = mapGriddedRegion.indexForLocation(loc);
				if(locIndex>=0) {
					double oldRate = xyzDataSet.get(locIndex);
					xyzDataSet.set(locIndex, ptRate+oldRate);					
				}
			}
		}
//		System.out.println("Min & Max Z: "+xyzDataSet.getMinZ()+"\t"+xyzDataSet.getMaxZ());
		String metadata = "no metadata";
		
		try {
			String name;
			if(local)
				name = mapGen.makeMapLocally(xyzDataSet, "Prob from "+label, metadata, dirName);
			else {
				name = mapGen.makeMapUsingServlet(xyzDataSet, "Prob from "+label, metadata, dirName);
				metadata += GMT_MapGuiBean.getClickHereHTML(mapGen.getGMTFilesWebAddress());
				ImageViewerWindow imgView = new ImageViewerWindow(name,metadata, true);				
			}

//			System.out.println("GMT Plot Filename: "+name);
		} catch (GMT_MapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "For Block Prob Map: "+mapGen.getGMTFilesWebAddress()+" (deleted at midnight)";
	}
	
	
	
	/**
	 * 
	 * @param label - plot label
	 * @param local - whether GMT map is made locally or on server
	 * @param dirName
	 * @return
	 */
	public String plotOrigERF_RatesMap(String label, boolean local, String dirName, FaultSystemSolutionPoissonERF erf) {
		
		GMT_MapGenerator mapGen = new GMT_MapGenerator();
		mapGen.setParameter(GMT_MapGenerator.GMT_SMOOTHING_PARAM_NAME, false);
		mapGen.setParameter(GMT_MapGenerator.TOPO_RESOLUTION_PARAM_NAME, GMT_MapGenerator.TOPO_RESOLUTION_NONE);
		mapGen.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME,31.5);		// -R-125.4/-113.0/31.5/43.0
		mapGen.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME,43.0);
		mapGen.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME,-125.4);
		mapGen.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME,-113.0);
		mapGen.setParameter(GMT_MapGenerator.LOG_PLOT_NAME,true);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,-3.5);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,1.5);


		CaliforniaRegions.RELM_GRIDDED mapGriddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		GriddedGeoDataSet xyzDataSet = new GriddedGeoDataSet(mapGriddedRegion, true);
		
		// initialize values to zero
		for(int i=0; i<xyzDataSet.size();i++) xyzDataSet.set(i, 0);
		
		double duration = erf.getTimeSpan().getDuration();
		for(ProbEqkSource src : erf) {
			for(ProbEqkRupture rup : src) {
				LocationList locList = rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
				double ptRate = rup.getMeanAnnualRate(duration)/locList.size();
				for(Location loc:locList) {
					int locIndex = mapGriddedRegion.indexForLocation(loc);
					if(locIndex>=0) {
						double oldRate = xyzDataSet.get(locIndex);
						xyzDataSet.set(locIndex, ptRate+oldRate);					
					}
				}
			}
		}
		
//		System.out.println("Min & Max Z: "+xyzDataSet.getMinZ()+"\t"+xyzDataSet.getMaxZ());
		String metadata = "no metadata";
		
		try {
			String name;
			if(local)
				name = mapGen.makeMapLocally(xyzDataSet, "Prob from "+label, metadata, dirName);
			else {
				name = mapGen.makeMapUsingServlet(xyzDataSet, "Prob from "+label, metadata, dirName);
				metadata += GMT_MapGuiBean.getClickHereHTML(mapGen.getGMTFilesWebAddress());
				ImageViewerWindow imgView = new ImageViewerWindow(name,metadata, true);				
			}

//			System.out.println("GMT Plot Filename: "+name);
		} catch (GMT_MapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "For Block Prob Map: "+mapGen.getGMTFilesWebAddress()+" (deleted at midnight)";
	}

	/**
	 * 
	 * @param label - plot label
	 * @param local - whether GMT map is made locally or on server
	 * @param dirName
	 * @return
	 */
	public String plotRandomSampleRatesMap(String label, boolean local, String dirName, FaultSystemSolutionPoissonERF erf, int numYrs) {
		
		GMT_MapGenerator mapGen = new GMT_MapGenerator();
		mapGen.setParameter(GMT_MapGenerator.GMT_SMOOTHING_PARAM_NAME, false);
		mapGen.setParameter(GMT_MapGenerator.TOPO_RESOLUTION_PARAM_NAME, GMT_MapGenerator.TOPO_RESOLUTION_NONE);
		mapGen.setParameter(GMT_MapGenerator.MIN_LAT_PARAM_NAME,31.5);		// -R-125.4/-113.0/31.5/43.0
		mapGen.setParameter(GMT_MapGenerator.MAX_LAT_PARAM_NAME,43.0);
		mapGen.setParameter(GMT_MapGenerator.MIN_LON_PARAM_NAME,-125.4);
		mapGen.setParameter(GMT_MapGenerator.MAX_LON_PARAM_NAME,-113.0);
		mapGen.setParameter(GMT_MapGenerator.LOG_PLOT_NAME,true);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MODE_NAME,GMT_MapGenerator.COLOR_SCALE_MODE_MANUALLY);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MIN_PARAM_NAME,-3.5);
		mapGen.setParameter(GMT_MapGenerator.COLOR_SCALE_MAX_PARAM_NAME,1.5);


		CaliforniaRegions.RELM_GRIDDED mapGriddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		GriddedGeoDataSet xyzDataSet = new GriddedGeoDataSet(mapGriddedRegion, true);
		
		// initialize values to zero
		for(int i=0; i<xyzDataSet.size();i++) xyzDataSet.set(i, 0);
		
		// get 1000 yrs worth of samples
		int numSamples = numYrs*(int)totRate;
		// do this to make sure it exists
		getPointSampler();
		
		for(int i=0;i<numSamples;i++) {
			int indexFromSampler = pointSampler.getRandomInt();
			int[] regAndDepIndex = this.getRegAndDepIndicesForSamplerIndex(indexFromSampler);
			int indexForMap = mapGriddedRegion.indexForLocation(region.locationForIndex(regAndDepIndex[0]));	// ignoring depth
			xyzDataSet.set(indexForMap, 1.0/(double)numYrs);
			
//			EqksAtPoint eqksAtPt = getEqksAtPointForSamplerIndex(indexFromSampler);
//			int[] rupOrSrc = eqksAtPt.getRandomRupOrSrc();
//			if(rupOrSrc[0] == 0) { // it's a rup index
//				int rthRup = rupOrSrc[1];
//				// sample a hypocenter (sample one of the points on rup surface that's at this point)
//			}
//			else {	// it's a source index
//				int iSrc = rupOrSrc[1];
//				// sample a hypocenter (the location of the point source)
//			}
		}
		
		
//		System.out.println("Min & Max Z: "+xyzDataSet.getMinZ()+"\t"+xyzDataSet.getMaxZ());
		String metadata = "no metadata";
		
		try {
			String name;
			if(local)
				name = mapGen.makeMapLocally(xyzDataSet, "Prob from "+label, metadata, dirName);
			else {
				name = mapGen.makeMapUsingServlet(xyzDataSet, "Prob from "+label, metadata, dirName);
				metadata += GMT_MapGuiBean.getClickHereHTML(mapGen.getGMTFilesWebAddress());
				ImageViewerWindow imgView = new ImageViewerWindow(name,metadata, true);				
			}

//			System.out.println("GMT Plot Filename: "+name);
		} catch (GMT_MapException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "For Block Prob Map: "+mapGen.getGMTFilesWebAddress()+" (deleted at midnight)";
	}


}
