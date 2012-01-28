package scratch.ned.ETAS_ERF.sandbox;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import scratch.ned.ETAS_ERF.EqksInGeoBlock;
import scratch.ned.ETAS_Tests.ETAS_Utils;
import scratch.ned.ETAS_Tests.IntegerPDF_FunctionSampler;

public class ETAS_LocationWeightCalculatorAlt {
	
	final static boolean D = true;
	
	int numLatLon, numDepth;
	double maxLatLonDeg, maxDepthKm, latLonDiscrDeg, depthDiscr, midLat, maxDistKm;
	
	double distDecay, minDist;
	
	double cosMidLat;
	
	double[] totWtAtDepth;	
	double[][][] pointWt;
	
	LocationList[][][] subLocsArray;
	IntegerPDF_FunctionSampler[][][] subLocSamplerArray;
//	int maxNumPtsWithSubLocs = 6;
//	int[] numSubLocDivisions = {50,40,30};
//	int[] numSubLocDivisions = {100,50,20,10,4,2};
	
//	int[] numSubDistances = {500,100,50,10,5,2};
	int[] numSubDistances = {300,200,100,50,25,10};
	
	EvenlyDiscretizedFunc logTargetDecay;
	EvenlyDiscretizedFunc logDistWeightHist;
	
	
	public ETAS_LocationWeightCalculatorAlt(double maxDistKm, double maxDepthKm, double latLonDiscrDeg, double depthDiscr, 
			double midLat, double distDecay, double minDist) {
		
		cosMidLat = Math.cos(midLat*Math.PI/180);
		double aveLatLonDiscrKm = (latLonDiscrDeg+cosMidLat*latLonDiscrDeg)*111/2.0;
		this.maxDistKm = maxDistKm;
		this.maxLatLonDeg = maxDistKm/(111*cosMidLat);	// degrees
		
		this.maxDepthKm = maxDepthKm;
		this.latLonDiscrDeg = latLonDiscrDeg;
		this.depthDiscr = depthDiscr;
		this.midLat = midLat;
		this.distDecay=distDecay;
		this.minDist=minDist;
				
		numLatLon = (int)Math.round(maxLatLonDeg/latLonDiscrDeg);
		numDepth = (int)Math.round(maxDepthKm/depthDiscr);
	
		System.out.println("aveLatLonDiscrKm="+aveLatLonDiscrKm+
				"\nmaxLatLonDeg="+maxLatLonDeg+
				"\ncosMidLat="+cosMidLat+
				"\nnumLatLon="+numLatLon+
				"\nnumDepth="+numDepth);
		
		int maxNumPtsWithSubLocs = numSubDistances.length;
		subLocsArray = new LocationList[maxNumPtsWithSubLocs][maxNumPtsWithSubLocs][maxNumPtsWithSubLocs];
		subLocSamplerArray = new IntegerPDF_FunctionSampler[maxNumPtsWithSubLocs][maxNumPtsWithSubLocs][maxNumPtsWithSubLocs];

		pointWt = new double[numLatLon][numLatLon][numDepth];
		
//getSubDistances(0, 1, 2, 4);
//System.exit(0);

		// make distances histogram
		logDistWeightHist = new EvenlyDiscretizedFunc(-2.0,4.0,61);
		logDistWeightHist.setTolerance(logDistWeightHist.getDelta());
		double[] distances=null;
		for(int iDep=0;iDep<numDepth; iDep++) {
			System.out.println("Working on depth "+iDep);
			for(int iLat=0;iLat<numLatLon; iLat++) {
				for(int iLon=0;iLon<numLatLon; iLon++) {
					// find the largest index) proxy for farthest distance
					int maxIndex = Math.max(iDep, Math.max(iLat, iLon));
					if(maxIndex<numSubDistances.length) {
						distances = getSubDistances(iLat, iLon, iDep, numSubDistances[maxIndex]);
						for(int i=0;i<distances.length;i++) {
							double wt = Math.pow(distances[i]+minDist, -distDecay);
							double logDist = Math.log10(distances[i]);
							if(logDist<logDistWeightHist.getX(0))	// in case it's below the first bin
								logDistWeightHist.add(0, wt);
							else if (logDist<maxDistKm)
								logDistWeightHist.add(logDist,wt);
//							if(maxIndex<7)
//							System.out.println(maxIndex+"\t"+distances.length);
						}
					}
					else {
						double dist = getDistance(iLat, iLon, iDep);
						double wt = Math.pow(dist+minDist, -distDecay);
						logDistWeightHist.add(Math.log10(dist),wt);
					}
				}
			}
		}
		
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(logDistWeightHist, "test hist"); 

		// make distances histogram
		logTargetDecay = new EvenlyDiscretizedFunc(-2.0,4.0, 61);
		logTargetDecay.setTolerance(logDistWeightHist.getDelta());

//		EvenlyDiscretizedFunc logWtHistogram2 = new EvenlyDiscretizedFunc(-2.0 , 4.0, 61);
		double logBinHalfWidth = logTargetDecay.getDelta()/2;
		double upperBinEdge = Math.pow(10,logTargetDecay.getX(0)+logBinHalfWidth);
		double lowerBinEdge;
		double binWt = ETAS_Utils.getDecayFractionInsideDistance(distDecay, minDist, upperBinEdge);	// everything within the upper edge of first bin
		logTargetDecay.set(0,binWt);
//		logWtHistogram2.set(0,Math.pow(Math.pow(10,logWtHistogram2.getX(0))+minDist, -distDecay));
		for(int i=1;i<logTargetDecay.getNum();i++) {
			double logLowerEdge = logTargetDecay.getX(i)-logBinHalfWidth;
			lowerBinEdge = Math.pow(10,logLowerEdge);
			double logUpperEdge = logTargetDecay.getX(i)+logBinHalfWidth;
			upperBinEdge = Math.pow(10,logUpperEdge);
			double wtLower = ETAS_Utils.getDecayFractionInsideDistance(distDecay, minDist, lowerBinEdge);
			double wtUpper = ETAS_Utils.getDecayFractionInsideDistance(distDecay, minDist, upperBinEdge);
			binWt = wtUpper-wtLower;
			
//			System.out.println((float)logLowerEdge+"\t"+(float)logUpperEdge+"\t"+lowerBinEdge+"\t"+upperBinEdge+"\t"+wtLower+"\t"+wtUpper);

			logTargetDecay.set(i,binWt);
//			logWtHistogram2.set(i,Math.pow(Math.pow(10,logWtHistogram2.getX(i))+minDist, -distDecay)*(upperBinEdge-lowerBinEdge));

		}
		
		// normalize
		double tot = logTargetDecay.calcSumOfY_Vals();
		System.out.println("logWtHistogram.calcSumOfY_Vals()="+tot);
		logTargetDecay.scale(1.0/tot);
		System.out.println("logWtHistogram.calcSumOfY_Vals()="+logTargetDecay.calcSumOfY_Vals());
		
//		GraphiWindowAPI_Impl graph2 = new GraphiWindowAPI_Impl(logWtHistogram, "logWtHistogram"); 
//		GraphiWindowAPI_Impl graph3 = new GraphiWindowAPI_Impl(logWtHistogram2, "logWtHistogram2"); 

		// now fill in weights for each point
		for(int iDep=0;iDep<numDepth; iDep++) {
			System.out.println("Working on depth "+iDep);
			for(int iLat=0;iLat<numLatLon; iLat++) {
				for(int iLon=0;iLon<numLatLon; iLon++) {
					// find the largest index) proxy for farthest distance
					int maxIndex = Math.max(iDep, Math.max(iLat, iLon));
					if(maxIndex<numSubDistances.length) {
						distances = getSubDistances(iLat, iLon, iDep, numSubDistances[maxIndex]);
						for(int i=0;i<distances.length;i++) {
							double wt = Math.pow(distances[i]+minDist, -distDecay);
							double logDist = Math.log10(distances[i]);
							if(logDist<logDistWeightHist.getX(0))	// in case it's below the first bin
								pointWt[iLat][iLon][iDep] += wt*logTargetDecay.getY(0)/logDistWeightHist.getY(0);
							else if (logDist<maxDistKm)
								pointWt[iLat][iLon][iDep] += wt*logTargetDecay.getY(logDist)/logDistWeightHist.getY(logDist);
						}
					}
					else {
						double dist = getDistance(iLat, iLon, iDep);
						double wt = Math.pow(dist+minDist, -distDecay);
						double logDist = Math.log10(dist);
						pointWt[iLat][iLon][iDep] += wt*logTargetDecay.getY(logDist)/logDistWeightHist.getY(logDist);
					}
				}
			}
		}
		
		// test total weight
		double totWtTest=0;
		for(int iDep=0;iDep<numDepth; iDep++) {
			for(int iLat=0;iLat<numLatLon; iLat++) {
				for(int iLon=0;iLon<numLatLon; iLon++) {
					totWtTest += pointWt[iLat][iLon][iDep];
				}
			}
		}
		System.out.println("totWtTest = "+ totWtTest);
		
		
		totWtAtDepth = new double[numDepth];
		double testTot=0;
		for(int iDep=0;iDep<numDepth; iDep++) {
			double wtAtDep=0;
			for(int iLat=0;iLat<numLatLon; iLat++) {
				for(int iLon=0;iLon<numLatLon; iLon++) {
					wtAtDep += pointWt[iLat][iLon][iDep];
				}
			}
			totWtAtDepth[iDep]=wtAtDep;
//			System.out.println("totWtAtDepth\t"+iDep+"\t"+wtAtDep);
			testTot += wtAtDep;
		}

		
		
	}
	
	
	/**
	 * This returns a location containing delta lat, lon, and depth based on distance decay
	 * @param relLat
	 * @param relLon
	 * @param relDep
	 * @return
	 */
	public Location getRandomDeltaLoc(double relLat, double relLon, double relDep) {
		int iLat = getLatIndex(relLat);
		int iLon = getLatIndex(relLon);
		int iDep = getDepthIndex(relDep);
		Location loc;	// the location before some added randomness
		double deltaSubLatLon;
		double deltaDepth;
		
		int maxIndex = Math.max(iDep, Math.max(iLat, iLon));
		if(maxIndex<numSubDistances.length) {
			int numSubLoc = numSubDistances[maxIndex];
			deltaSubLatLon = latLonDiscrDeg/numSubLoc;
			deltaDepth = depthDiscr/numSubLoc;
	

			if(subLocsArray[iLat][iLon][iDep] == null) {
				double midLat = getLat(iLat);
				double midLon = getLon(iLon);
				double midDepth = getDepth(iDep);
				
//if(iLat==0 && iLon==1 && iDep==2) {
//					System.out.println("midLat="+midLat+"\tmidLon="+midLon+"\tmidDepth="+midDepth);
//					System.out.println("relLat\trelLon\trelDepth\tdist");
//				}
				LocationList locList = new LocationList();
				IntegerPDF_FunctionSampler newSampler = new IntegerPDF_FunctionSampler(numSubLoc*numSubLoc*numSubLoc);
				int index = 0;
				for(int iSubLat = 0; iSubLat < numSubLoc; iSubLat++) {
					double lat = (midLat-latLonDiscrDeg/2) + iSubLat*deltaSubLatLon + deltaSubLatLon/2;
					for(int iSubLon = 0; iSubLon < numSubLoc; iSubLon++) {
						double lon = (midLon-latLonDiscrDeg/2) + iSubLon*deltaSubLatLon + deltaSubLatLon/2;
						for(int iSubDep = 0; iSubDep < numSubLoc; iSubDep++) {
							double dep = (midDepth-depthDiscr/2) + iSubDep*deltaDepth + deltaDepth/2;
							locList.add(new Location(lat-midLat,lon-midLon,dep-midDepth));	// add the deltaLoc to list
							double dist = getDistance(lat, lon, dep);
							double logDist = Math.log10(dist);
							double normWt;
							if(logDist<logDistWeightHist.getX(0))
								normWt = logDistWeightHist.getY(0);
							else
								normWt = logDistWeightHist.getY(logDist);
							newSampler.add(index, Math.pow(dist+minDist, -distDecay)/normWt);		// add the sampler
//if(iLat==0 && iLon==1 && iDep==2) {
//								System.out.println((float)lat+"\t"+(float)lon+"\t"+(float)dep+"\t"+(float)dist);
//							}
							index ++;
						}
					}
				}
				subLocsArray[iLat][iLon][iDep] = locList;
				subLocSamplerArray[iLat][iLon][iDep] = newSampler;
			}
			
			int randLocIndex = subLocSamplerArray[iLat][iLon][iDep].getRandomInt();
			loc = subLocsArray[iLat][iLon][iDep].get(randLocIndex);			
		}
		else {
			deltaSubLatLon = latLonDiscrDeg;
			deltaDepth = depthDiscr;
			loc = new Location(getLat(0), getLon(0), getDepth(0));	// not delta
		}
		// ADD A RANDOM ELEMENT
//		return new Location(loc.getLatitude()+deltaSubLatLon*(Math.random()-0.5)*0.999,
//				loc.getLongitude()+deltaSubLatLon*(Math.random()-0.5)*0.999,
//				loc.getDepth()+deltaDepth*(Math.random()-0.5)*0.999);
		return loc;
		
	}
	
	private double getDistance(int iLat, int iLon, int iDep) {
		return getDistance(getLat(iLat), getLon(iLon), getDepth(iDep));
	}
	
	private double getDistance(double relLat, double relLon, double relDep) {
		double latDistKm = relLat*111;
		double lonDistKm = relLon*111*cosMidLat;
		return Math.sqrt(latDistKm*latDistKm+lonDistKm*lonDistKm+relDep*relDep);
	}

	
	
	public double getProbAtPoint(double relLat, double relLon, double relDep, double hypoDep) {
		int iLat = getLatIndex(relLat);
		int iLon = getLatIndex(relLon);
		int iDep = getDepthIndex(relDep);
		int iHypoDep = getDepthIndex(hypoDep);
		
		// solve for the total weight for the associated layers
		double normWt=0;
		// sum those at same depth and below	// if at surface (iHypoDepth=0), should include all; if at bottom (iHypoDepth=numDepth-1), should include just 0th
		for(int d=0; d<numDepth-iHypoDep;d++)
			normWt += totWtAtDepth[d];
		// sum those above; none if iHypoDepth=0; those above if at bottom (iHypoDepth=numDepth-1)
		if(iHypoDep > 0)
			for(int d=1; d<=iHypoDep;d++)
				normWt += totWtAtDepth[d];
		
		if(iLat >= numLatLon || iLon >= numLatLon || iDep >= numDepth) {
//			System.out.println("relLat="+relLat+"\tiLat="+iLat);
//			System.out.println("relLon="+relLon+"\tiLon="+iLon);
//			System.out.println("relDep="+relDep+"\tiDep="+iDep);
			return 0;
		}

		// factor of four below is to account for the other 3 quadrants
		return pointWt[iLat][iLon][iDep]/(normWt*4);
	}
	
	private double getLat(int iLat) {
		return iLat*latLonDiscrDeg+latLonDiscrDeg/2.0;
	}
	
	private int getLatIndex(double  relLat) {
		return (int) Math.round((relLat-latLonDiscrDeg/2.0)/latLonDiscrDeg);
	}

	
	private double getLon(int iLon) {
		return iLon*latLonDiscrDeg+latLonDiscrDeg/2.0;
	}
	
	private int getLonIndex(double  relLon) {
		return (int) Math.round((relLon-latLonDiscrDeg/2.0)/latLonDiscrDeg);
	}

	private double getDepth(int iDep) {
		return iDep*depthDiscr+depthDiscr/2.0;
	}
	
	private int getDepthIndex(double relDepth) {
		return (int)Math.round((relDepth-depthDiscr/2.0)/depthDiscr);
	}



	public void testRandomSamples(int numSamples) {
		
		//test
//		getRandomDeltaLoc(this.getLat(0), this.getLon(1), this.getDepth(2));
//		System.exit(0);
		
		
		IntegerPDF_FunctionSampler sampler;
		int totNumPts = numLatLon*numLatLon*numDepth;
		sampler = new IntegerPDF_FunctionSampler(totNumPts);
		int[] iLatArray = new int[totNumPts];
		int[] iLonArray = new int[totNumPts];
		int[] iDepArray = new int[totNumPts];
		int index=0;
		for(int iDep=0;iDep<numDepth; iDep++) {
			for(int iLat=0;iLat<numLatLon; iLat++) {
				for(int iLon=0;iLon<numLatLon; iLon++) {
					sampler.set(index,pointWt[iLat][iLon][iDep]);
					iLatArray[index]=iLat;
					iLonArray[index]=iLon;
					iDepArray[index]=iDep;
					index +=1;
				}
			}
		}
		
		// create histogram
		EvenlyDiscretizedFunc testLogHistogram = new EvenlyDiscretizedFunc(-2.0 , 4.0, 61);
		testLogHistogram.setTolerance(testLogHistogram.getDelta());
		
		EvenlyDiscretizedFunc testHistogram = new EvenlyDiscretizedFunc(0.5 , 1009.5, 1010);
		testHistogram.setTolerance(testHistogram.getDelta());
		
		// make target histogram
		EvenlyDiscretizedFunc targetHist = new EvenlyDiscretizedFunc(0.5 , 999.5, 1000);
		double halfDelta=targetHist.getDelta()/2;
		for(int i=0;i<targetHist.getNum();i++) {
			double upper = ETAS_Utils.getDecayFractionInsideDistance(distDecay, minDist, targetHist.getX(i)+halfDelta);
			double lower = ETAS_Utils.getDecayFractionInsideDistance(distDecay, minDist, targetHist.getX(i)-halfDelta);
			targetHist.set(i,upper-lower);
		}


//		double deltaDistForHist = depthDiscr/10;
//		double min = deltaDistForHist/2;
//		int num = (int) Math.round((maxDistKm+10)/deltaDistForHist);	// plus 10 to test for zeros at end
//		double max = (num)*deltaDistForHist-deltaDistForHist/2;
//		EvenlyDiscretizedFunc distHistogram = new EvenlyDiscretizedFunc(min , max, num);
//		distHistogram.setTolerance(deltaDistForHist);
		
		for(int i=0;i<numSamples;i++) {
			int sampIndex = sampler.getRandomInt();
			double relLat = getLat(iLatArray[sampIndex]);
			double relLon = getLon(iLonArray[sampIndex]);
			double relDep = getDepth(iDepArray[sampIndex]);
			Location deltaLoc=getRandomDeltaLoc(relLat, relLon, relDep);
			double dist = getDistance(relLat+deltaLoc.getLatitude(), relLon+deltaLoc.getLongitude(), relDep+deltaLoc.getDepth());
			if(dist<this.maxDistKm) {
				testHistogram.add(dist, 1.0/numSamples);
				double logDist = Math.log10(dist);
				if(logDist<testLogHistogram.getX(0))
					testLogHistogram.add(0, 1.0/numSamples);
				else if (logDist<3.0)
					testLogHistogram.add(logDist,1.0/numSamples);
			}
		}
		
		ArrayList funcs1 = new ArrayList();
		funcs1.add(testLogHistogram);
		funcs1.add(logTargetDecay);

		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs1, "testLogHistogram"); 
		graph.setAxisRange(-2, 3, 1e-6, 1);
		graph.setYLog(true);

		
		ArrayList funcs2 = new ArrayList();
		funcs2.add(testHistogram);
		funcs2.add(targetHist);
		GraphiWindowAPI_Impl graph2 = new GraphiWindowAPI_Impl(funcs2, "testHistogram"); 
		graph2.setAxisRange(0.1, 1000, 1e-6, 1);
		graph2.setYLog(true);
		graph2.setXLog(true);
//		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
//		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
//		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 2f, Color.BLUE));
//		graph.setPlottingFeatures(plotChars);
//		graph.setX_AxisLabel("Distance (km)");
//		graph.setY_AxisLabel("Probability");




	}
	
	private double getPointWeightFast(int iLat, int iLon, int iDep, int numDiscr) {
		
		double midLat = getLat(iLat);
		double midLon = getLon(iLon);
		double midDepth = getDepth(iDep);
		double totWt = 0;
		double deltaSubLatLon = latLonDiscrDeg/numDiscr;
		double deltaDepth = depthDiscr/numDiscr;
		for(int latIndex = 0; latIndex < numDiscr; latIndex++) {
			double relLat = (midLat-latLonDiscrDeg/2) + latIndex*deltaSubLatLon + deltaSubLatLon/2;
			for(int lonIndex = 0; lonIndex < numDiscr; lonIndex++) {
				double relLon = (midLon-latLonDiscrDeg/2) + lonIndex*deltaSubLatLon + deltaSubLatLon/2;
				for(int depIndex = 0; depIndex < numDiscr; depIndex++) {
					double relDep = (midDepth-depthDiscr/2) + depIndex*deltaDepth + deltaDepth/2;
					double dist = this.getDistance(relLat, relLon, relDep);
					totWt += Math.pow(dist+minDist, -distDecay);
				}
			}
		}
		return totWt/(double)(numDiscr*numDiscr*numDiscr);
	}
	
	
	
	
	
	public double[] getSubDistances(int iLat, int iLon, int iDep, int numDiscr) {
		double[] distances = new double[numDiscr*numDiscr*numDiscr];
		double midLat = getLat(iLat);
		double midLon = getLon(iLon);
		double midDepth = getDepth(iDep);
		double deltaSubLatLon = latLonDiscrDeg/numDiscr;
		double deltaDepth = depthDiscr/numDiscr;
		int index=0;
//System.out.println("midLat="+midLat+"\tmidLon="+midLon+"\tmidDepth="+midDepth);
//System.out.println("relLat\trelLon\trelDepth\tdist");

		for(int latIndex = 0; latIndex < numDiscr; latIndex++) {
			double relLat = (midLat-latLonDiscrDeg/2) + latIndex*deltaSubLatLon + deltaSubLatLon/2;
			for(int lonIndex = 0; lonIndex < numDiscr; lonIndex++) {
				double relLon = (midLon-latLonDiscrDeg/2) + lonIndex*deltaSubLatLon + deltaSubLatLon/2;
				for(int depIndex = 0; depIndex < numDiscr; depIndex++) {
					double relDep = (midDepth-depthDiscr/2) + depIndex*deltaDepth + deltaDepth/2;
					distances[index] = getDistance(relLat, relLon, relDep);
//System.out.println((float)relLat+"\t"+(float)relLon+"\t"+(float)relDep+"\t"+(float)distances[index]);

					index+=1;
				}
			}
		}
		return distances;
	}




	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		ETAS_LocationWeightCalculator calc = new ETAS_LocationWeightCalculator(1000.0, 24.0, 0.01, 1.0, 38.0, 2, 0.3);
		
		double maxDistKm=1000.0;
		double maxDepthKm=24;
//		double latLonDiscrDeg=0.005;
//		double depthDiscr=0.5;
		double latLonDiscrDeg=0.02;
		double depthDiscr=2.0;
		double midLat=38;
		double distDecay=2;
		double minDist=0.3;
		ETAS_LocationWeightCalculatorAlt calc = new ETAS_LocationWeightCalculatorAlt(maxDistKm, maxDepthKm, latLonDiscrDeg, 
				depthDiscr, midLat, distDecay, minDist);
		
		
//		for(int i=0;i<100;i++) {
//			Location loc = calc.getRandomDeltaLoc(.01, .01, 0.1);
//			System.out.println(loc.getLatitude()+"\t"+loc.getLongitude()+"\t"+loc.getDepth());
//		}
		
		System.out.println("Testing random samples...");
//		calc.getEquivDistanceFast(0, 0, 0, 100);
		calc.testRandomSamples(1000000);

	}

}
