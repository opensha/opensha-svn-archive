package scratch.UCERF3.erf.ETAS;

import java.io.FileWriter;
import java.util.ArrayList;

import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;

/**
 * This uses a faster, more approximate distance calculation 
 * (see getDistance(double relLat, double relLon, double relDep) here)
 * @author field
 *
 */
public class ETAS_LocationWeightCalculatorHypDepDep {
	
	final static boolean D = false;
	
	int numLatLon, numDepth;
	double maxLatLonDeg, maxDepthKm, latLonDiscrDeg, depthDiscr, midLat, maxDistKm;
	
	double distDecay, minDist;
	
	double cosMidLat;
	
	int iHypoDep, iTestHypo;
	
	double[][][] pointWt;
	
	double histLogMin=-2.0;	// log10 distance
	double histLogMax = 4.0;	// log10 distance
	int histNum = 31;
	
	LocationList[][][] subLocsArray;
	IntegerPDF_FunctionSampler[][][] subLocSamplerArray;

	int[] numSubDistances = {100,20,10,5,2,2};
	
	EvenlyDiscretizedFunc targetLogDistDecay;
	EvenlyDiscretizedFunc logDistWeightHist;
	
	/**
	 * 
	 * @param maxDistKm - the maximum distance for sampling in km
	 * @param maxDepthKm - the max seismogenic thickness
	 * @param latLonDiscrDeg - the lat and lon discretization in degrees (0.2 is recommented)
	 * @param depthDiscr - the depth discretization in km (2.0 is recommended)
	 * @param midLat - the mid latitude used to compute bin widths (since widths decrease with latitude)
	 * @param distDecay - the ETAS distance decay parameter
	 * @param minDist - the ETAS min distance
	 * @param iHypoDep - index of parent depth (which has a range of numDepth+1, since parent depth is half way between points here
	 */
	public ETAS_LocationWeightCalculatorHypDepDep(double maxDistKm, double maxDepthKm, double latLonDiscrDeg, double depthDiscr, 
			double midLat, double distDecay, double minDist, int iHypoDep) {
		
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
						
		// the number of points in each direction
		numLatLon = (int)Math.round(maxLatLonDeg/latLonDiscrDeg);
		numDepth = (int)Math.round(maxDepthKm/depthDiscr);
	
		this.iHypoDep = iHypoDep;
		this.iTestHypo = Math.min(iHypoDep, numDepth-iHypoDep); // test for knowing when relDepth applies to two layers
		if(D) System.out.println("iHypoDep="+iHypoDep);

		if (D) System.out.println("aveLatLonDiscrKm="+aveLatLonDiscrKm+
				"\nmaxLatLonDeg="+maxLatLonDeg+
				"\ncosMidLat="+cosMidLat+
				"\nnumLatLon="+numLatLon+
				"\nnumDepth="+numDepth);
		
		// the following is info for the close points that are subdivided
		int maxNumPtsWithSubLocs = numSubDistances.length;
		subLocsArray = new LocationList[maxNumPtsWithSubLocs][maxNumPtsWithSubLocs][maxNumPtsWithSubLocs];
		subLocSamplerArray = new IntegerPDF_FunctionSampler[maxNumPtsWithSubLocs][maxNumPtsWithSubLocs][maxNumPtsWithSubLocs];

		pointWt = new double[numLatLon][numLatLon][numDepth];
		
		// make distances weight histogram for the various log distances
		logDistWeightHist = new EvenlyDiscretizedFunc(histLogMin,histLogMax,histNum);
		logDistWeightHist.setTolerance(logDistWeightHist.getDelta());
		double[] distances=null;
		if(D) System.out.println("\niHypoDep="+iHypoDep);
		for(int iLat=0;iLat<numLatLon; iLat++) {
			for(int iLon=0;iLon<numLatLon; iLon++) {
				// sum those at same depth and below; all if at surface (iHypoDepth=0); non if at bottom (iHypoDepth=numDepth)
				// THE FOLLOWING TWO FOR LOOPS COULD BE COMBINED INTO ONE (DOUBLING THE WEIGHT WHERE THERE IS OVERLAP)
				// THIS WOULD ALSO NEED TO BE DONE FOR THE DUPLICATE FOR LOOPS BELOW
				for(int iDep=0; iDep<numDepth-iHypoDep;iDep++) {
					if(D) {
						if(iLat==0 && iLon==0) System.out.print(iDep+", ");
					}
					// find the largest index) proxy for farthest distance
					int maxIndex = Math.max(iDep, Math.max(iLat, iLon));
					if(maxIndex<numSubDistances.length) {
						distances = getSubDistances(iLat, iLon, iDep, numSubDistances[maxIndex]);
						for(int i=0;i<distances.length;i++) {
							double dist = distances[i];
							double wt = ETAS_Utils.getDistDecayValue(dist, minDist, -distDecay)/distances.length;
							double logDist = Math.log10(dist);
							if(logDist<logDistWeightHist.getX(0))	// in case it's below the first bin
								logDistWeightHist.add(0, wt);
							else if (dist<maxDistKm)
								logDistWeightHist.add(logDist,wt);
						}
					}
					else {
						double dist = getDistance(iLat, iLon, iDep);
						if(dist<maxDistKm) {
							double wt = ETAS_Utils.getDistDecayValue(dist, minDist, -distDecay);;
							logDistWeightHist.add(Math.log10(dist),wt);							
						}
					}

				}
				// sum those above; none if iHypoDepth=0; all those above if at bottom (iHypoDepth=numDepth)
				if(D){
					if(iLat==0 && iLon==0) System.out.print("\n");
				}

				for(int iDep=0; iDep<iHypoDep;iDep++) {
					if(D) {
						if(iLat==0 && iLon==0) System.out.print(iDep+", ");
					}

					// find the largest index) proxy for farthest distance
					int maxIndex = Math.max(iDep, Math.max(iLat, iLon));
					if(maxIndex<numSubDistances.length) {
						distances = getSubDistances(iLat, iLon, iDep, numSubDistances[maxIndex]);
						for(int i=0;i<distances.length;i++) {
							double dist = distances[i];
							double wt = ETAS_Utils.getDistDecayValue(dist, minDist, -distDecay)/distances.length;
							double logDist = Math.log10(dist);
							if(logDist<logDistWeightHist.getX(0))	// in case it's below the first bin
								logDistWeightHist.add(0, wt);
							else if (dist<maxDistKm)
								logDistWeightHist.add(logDist,wt);
						}
					}
					else {
						double dist = getDistance(iLat, iLon, iDep);
						if(dist<maxDistKm) {
							double wt = ETAS_Utils.getDistDecayValue(dist, minDist, -distDecay);;
							logDistWeightHist.add(Math.log10(dist),wt);							
						}
					}
				}
			}
		}
		if(D) System.out.print("\n\n");


		// plot to check for any zero bins
		if (D) {
			GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(logDistWeightHist, "test hist"); 
		}

		// make target distances decay histogram (this is what we will match_
		targetLogDistDecay = ETAS_Utils.getTargetDistDecayFunc(histLogMin, histLogMax, histNum, distDecay, minDist);
		
		// normalize
		double tot = targetLogDistDecay.calcSumOfY_Vals();
		if (D) System.out.println("logWtHistogram.calcSumOfY_Vals()="+tot);
//		logTargetDecay.scale(1.0/tot);
//		System.out.println("logWtHistogram.calcSumOfY_Vals()="+logTargetDecay.calcSumOfY_Vals());
		
		// now fill in weights for each point
		for(int iLat=0;iLat<numLatLon; iLat++) {
			for(int iLon=0;iLon<numLatLon; iLon++) {
				for(int iDep=0; iDep<numDepth-iHypoDep;iDep++) {
//if(iLat==0 && iLon==0) System.out.print(iDep+", ");

					// find the largest index) proxy for farthest distance
					int maxIndex = Math.max(iDep, Math.max(iLat, iLon));
					if(maxIndex<numSubDistances.length) {
						distances = getSubDistances(iLat, iLon, iDep, numSubDistances[maxIndex]);
						for(int i=0;i<distances.length;i++) {
							double dist = distances[i];
							double wt = ETAS_Utils.getDistDecayValue(dist, minDist, -distDecay)/distances.length;
							double logDist = Math.log10(dist);
							if(logDist<logDistWeightHist.getX(0))	// in case it's below the first bin
								pointWt[iLat][iLon][iDep] += wt*targetLogDistDecay.getY(0)/logDistWeightHist.getY(0);
							else if (dist<maxDistKm)
								pointWt[iLat][iLon][iDep] += wt*targetLogDistDecay.getY(logDist)/logDistWeightHist.getY(logDist);
						}
					}
					else {
						double dist = getDistance(iLat, iLon, iDep);
						if(dist<maxDistKm) {
							double wt = ETAS_Utils.getDistDecayValue(dist, minDist, -distDecay);
							double logDist = Math.log10(dist);
							pointWt[iLat][iLon][iDep] += wt*targetLogDistDecay.getY(logDist)/logDistWeightHist.getY(logDist);							
						}
					}
				}
//if(iLat==0 && iLon==0) System.out.print("\n");
				for(int iDep=0; iDep<iHypoDep;iDep++) {
//if(iLat==0 && iLon==0) System.out.print(iDep+", ");

					// find the largest index) proxy for farthest distance
					int maxIndex = Math.max(iDep, Math.max(iLat, iLon));
					if(maxIndex<numSubDistances.length) {
						distances = getSubDistances(iLat, iLon, iDep, numSubDistances[maxIndex]);
						for(int i=0;i<distances.length;i++) {
							double dist = distances[i];
							double wt = ETAS_Utils.getDistDecayValue(dist, minDist, -distDecay)/distances.length;
							double logDist = Math.log10(dist);
							if(logDist<logDistWeightHist.getX(0))	// in case it's below the first bin
								pointWt[iLat][iLon][iDep] += wt*targetLogDistDecay.getY(0)/logDistWeightHist.getY(0);
							else if (dist<maxDistKm)
								pointWt[iLat][iLon][iDep] += wt*targetLogDistDecay.getY(logDist)/logDistWeightHist.getY(logDist);
						}
					}
					else {
						double dist = getDistance(iLat, iLon, iDep);
						if(dist<maxDistKm) {
							double wt = ETAS_Utils.getDistDecayValue(dist, minDist, -distDecay);
							double logDist = Math.log10(dist);
							pointWt[iLat][iLon][iDep] += wt*targetLogDistDecay.getY(logDist)/logDistWeightHist.getY(logDist);							
						}
					}
				}
			}
		}
//System.out.print("\n\n");


		// test total weight
		double totWtTest=0;
		for(int iDep=0;iDep<numDepth; iDep++) {
			for(int iLat=0;iLat<numLatLon; iLat++) {
				for(int iLon=0;iLon<numLatLon; iLon++) {
					totWtTest += pointWt[iLat][iLon][iDep];
				}
			}
		}
		if (D) System.out.println("totWtTest = "+ totWtTest);
		
		// APPLY THE ABOVE NORMALIZATION?
		
	}
	
	
	/**
	 * This returns a random location (containing delta lat, lon, and depth) for the 
	 * given location and based on the distance decay.  This first chooses among the
	 * sub-locations at the given point, and then adds some additional randomness to
	 * the sublocation.
	 * @param relLat
	 * @param relLon
	 * @param relDep
	 * @return
	 */
	public Location getRandomDeltaLoc(double relLat, double relLon, double relDep) {
		int iLat = getLatIndex(relLat);
		int iLon = getLonIndex(relLon);
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
							double wt = ETAS_Utils.getDistDecayValue(dist,minDist, -distDecay);
							double normWt;
							if(logDist<logDistWeightHist.getX(0))
								normWt = targetLogDistDecay.getY(0)/logDistWeightHist.getY(0);
							else
								normWt = targetLogDistDecay.getY(logDist)/logDistWeightHist.getY(logDist);
							newSampler.add(index, wt*normWt);		// add the sampler
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
			loc = new Location(0, 0, 0);	// no delta
		}
		// Add an additional random element
		return new Location(loc.getLatitude()+deltaSubLatLon*(Math.random()-0.5)*0.999,
				loc.getLongitude()+deltaSubLatLon*(Math.random()-0.5)*0.999,
				loc.getDepth()+deltaDepth*(Math.random()-0.5)*0.999);
//		return loc;
		
	}
	
	/**
	 * Get the distance (km) to the given point
	 * @param iLat
	 * @param iLon
	 * @param iDep
	 * @return
	 */
	private double getDistance(int iLat, int iLon, int iDep) {
		return getDistance(getLat(iLat), getLon(iLon), getDepth(iDep));
	}
	
	/**
	 * Get the distance (km) to the given location (approx distance calculation is applied)
	 * @param relLat
	 * @param relLon
	 * @param relDep
	 * @return
	 */
	private double getDistance(double relLat, double relLon, double relDep) {
		double latDistKm = relLat*111;
		double lonDistKm = relLon*111*cosMidLat;
		return Math.sqrt(latDistKm*latDistKm+lonDistKm*lonDistKm+relDep*relDep);
	}

	
	/**
	 * This give the probability of an event at the given point, and for the 
	 * given main shock hypocenter depth
	 * @param relLat
	 * @param relLon
	 * @param relDep
	 * @return
	 */
	public double getProbAtPoint(double relLat, double relLon, double relDep) {
		// are there two layers for this relative depth?
		int relDepIndex = getDepthIndex(relDep);
		int relLatIndex = getLatIndex(relLat);
		int relLonIndex = getLonIndex(relLon);
		if(relLatIndex>= numLatLon || relLonIndex>=numLatLon) {
			return 0.0;
		}
		if(relDepIndex<iTestHypo)
			return pointWt[relLatIndex][relLonIndex][relDepIndex]/(2*4.0); // factor of four below is to account for the other 3 quadrants
		else
			return pointWt[relLatIndex][relLonIndex][relDepIndex]/4.0;
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
					double wt = getProbAtPoint(getLat(iLat), getLon(iLon), getDepth(iDep));
//					sampler.set(index,pointWt[iLat][iLon][iDep]);
					sampler.set(index,wt);
					iLatArray[index]=iLat;
					iLonArray[index]=iLon;
					iDepArray[index]=iDep;
					index +=1;
				}
			}
		}
		
		// create histogram
		EvenlyDiscretizedFunc testLogHistogram = new EvenlyDiscretizedFunc(histLogMin,histLogMax,histNum);
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
		
		DefaultXY_DataSet epicenterLocs = new DefaultXY_DataSet();

		for(int i=0;i<numSamples;i++) {
			int sampIndex = sampler.getRandomInt();
			double relLat = getLat(iLatArray[sampIndex]);
			double relLon = getLon(iLonArray[sampIndex]);
			double relDep = getDepth(iDepArray[sampIndex]);
			Location deltaLoc=getRandomDeltaLoc(relLat, relLon, relDep);
			double dist = getDistance(relLat+deltaLoc.getLatitude(), relLon+deltaLoc.getLongitude(), relDep+deltaLoc.getDepth());
			epicenterLocs.set(relLat+deltaLoc.getLatitude(), relLon+deltaLoc.getLongitude());
			if(dist<this.maxDistKm) {
				testHistogram.add(dist, 1.0/numSamples);
				double logDist = Math.log10(dist);
				if(logDist<testLogHistogram.getX(0))
					testLogHistogram.add(0, 1.0/numSamples);
				else if (logDist<histLogMax)
					testLogHistogram.add(logDist,1.0/numSamples);
			}
		}
		
		ArrayList funcs1 = new ArrayList();
		funcs1.add(testLogHistogram);
		funcs1.add(targetLogDistDecay);

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
		
		GraphiWindowAPI_Impl graph3 = new GraphiWindowAPI_Impl(epicenterLocs, "epicenterLocs"); 
		
// TES OUT FILE
		try{
			FileWriter fw1 = new FileWriter("test456.txt");
			fw1.write("iLat\tiLon\tiDep\trelLat\trelLon\trelDep\twt\n");
			for(int i=0; i<sampler.getNum(); i++) {
				int iLat = iLatArray[i];
				int iLon = iLonArray[i];
				int iDep = iDepArray[i];
				double relLat = this.getLat(iLat);
				double relLon = this.getLon(iLon);
				double relDep = this.getDepth(iDep);
				if(relLat<0.25 && relLon<0.25)
					fw1.write(iLat+"\t"+iLon+"\t"+iDep+"\t"+(float)relLat+"\t"+(float)relLon+"\t"+(float)relDep+"\t"+(float)sampler.getY(i)+"\n");
			}
			fw1.close();
		}catch(Exception e) {
			e.printStackTrace();
		}


	}
	
	
	
	private double[] getSubDistances(int iLat, int iLon, int iDep, int numDiscr) {
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
		double midLat=37.25;
		double distDecay=2;
		double minDist=0.3;
		
		ArrayList<ETAS_LocationWeightCalculatorHypDepDep>  calcList = new ArrayList<ETAS_LocationWeightCalculatorHypDepDep>();
		for(int iParDep=0;iParDep<13;iParDep ++) {
//		for(int iParDep=0;iParDep<1;iParDep ++) {
			ETAS_LocationWeightCalculatorHypDepDep calc = new ETAS_LocationWeightCalculatorHypDepDep(maxDistKm, maxDepthKm, 
					latLonDiscrDeg, depthDiscr, midLat, distDecay, minDist, iParDep);
			calc.testRandomSamples(100000);
			calcList.add(calc);
		}
	}

}