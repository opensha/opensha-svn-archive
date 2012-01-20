package scratch.ned.ETAS_ERF.sandbox;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

import scratch.ned.ETAS_ERF.EqksInGeoBlock;
import scratch.ned.ETAS_ERF.IntegerPDF_FunctionSampler;

public class ETAS_LocationWeightCalculator {
	
	int numLatLon, numDepth;
	double maxLatLonDeg, maxDepthKm, latLonDiscrDeg, depthDiscr, midLat, maxDistKm;
	
	double distDecay, minDist;
	
	double cosMidLat;
	
	double[] totWtAtDepth;	
	double[][][] nominalWt;
	
	public ETAS_LocationWeightCalculator(double maxDistKm, double maxDepthKm, double latLonDiscrDeg, double depthDiscr, 
			double midLat, double distDecay, double minDist) {
		
		cosMidLat = Math.cos(midLat*Math.PI/180);
		double aveLatLonDiscrKm = (latLonDiscrDeg+cosMidLat*latLonDiscrDeg)*111/2.0;
		long startTime = System.currentTimeMillis();
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

//		double deltaDistForHist = Math.round(aveLatLonDiscrKm);
		double deltaDistForHist = depthDiscr;
		double min = deltaDistForHist/2;
		int num = (int) Math.round(maxDistKm/deltaDistForHist);
		double max = num*deltaDistForHist-deltaDistForHist/2;
		EvenlyDiscretizedFunc distHistogram = new EvenlyDiscretizedFunc(min , max, num);
		distHistogram.setTolerance(deltaDistForHist);

		
		double[][][] distances = new double[numLatLon][numLatLon][numDepth];
		nominalWt = new double[numLatLon][numLatLon][numDepth];
		for(int iLat=0;iLat<numLatLon; iLat++) {
			for(int iLon=0;iLon<numLatLon; iLon++) {
				for(int iDep=0;iDep<numDepth; iDep++) {
					double dist = getDistance(iLat, iLon, iDep);
//					if(dist<depthDiscr)	// fix close/biased distances
						dist = getEquivDistFast(iLat, iLon, iDep, 10);
					distances[iLat][iLon][iDep] = dist;
					if(dist<=maxDistKm) {
						distHistogram.add(dist, 1.0);
						nominalWt[iLat][iLon][iDep] = Math.pow(dist+minDist, -distDecay);
					}
					else {
						nominalWt[iLat][iLon][iDep] = 0;
					}
				}
			}
		}
		
		ArrayList funcs = new ArrayList();
		funcs.add(distHistogram);
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "test"); 

		double totWt=0;
		for(int iLat=0;iLat<numLatLon; iLat++) {
			for(int iLon=0;iLon<numLatLon; iLon++) {
				for(int iDep=0;iDep<numDepth; iDep++) {
					double dist = distances[iLat][iLon][iDep];
					if(dist<maxDistKm) {
						nominalWt[iLat][iLon][iDep] /= distHistogram.getY(dist);
						totWt += nominalWt[iLat][iLon][iDep];
					}
				}
			}
		}
//		System.out.println("totWt="+totWt);
		
		double finalTotWt=0;
		for(int iDep=0;iDep<numDepth; iDep++) {
			for(int iLat=0;iLat<numLatLon; iLat++) {
				for(int iLon=0;iLon<numLatLon; iLon++) {
					double dist = distances[iLat][iLon][iDep];
					if(dist<maxDistKm) {
						nominalWt[iLat][iLon][iDep] /= totWt;
						finalTotWt += nominalWt[iLat][iLon][iDep];
					}
				}
			}
		}
		System.out.println("finalTotWt="+(float)finalTotWt);
		
		totWtAtDepth = new double[numDepth];
		double testTot=0;
		for(int iDep=0;iDep<numDepth; iDep++) {
			double wtAtDep=0;
			for(int iLat=0;iLat<numLatLon; iLat++) {
				for(int iLon=0;iLon<numLatLon; iLon++) {
					wtAtDep += nominalWt[iLat][iLon][iDep];
				}
			}
			totWtAtDepth[iDep]=wtAtDep;
//			System.out.println("totWtAtDepth\t"+iDep+"\t"+wtAtDep);
			testTot += wtAtDep;
		}
		System.out.println("TotWt over all depths="+(float)testTot);
		
		System.out.println("Constructor runtime = "+ (System.currentTimeMillis()-startTime)/1000 +" sec");

	}
	
	private double getDistance(int iLat, int iLon, int iDep) {
		double dep = getDepth(iDep);
		double latDistKm = getLat(iLat)*111;
		double lonDistKm = getLon(iLon)*111*cosMidLat;
		return Math.sqrt(latDistKm*latDistKm+lonDistKm*lonDistKm+dep*dep);
	}
	
	
	public double getProbAtPoint(double relLat, double relLon, double relDep, double hypoDep) {
		int iLat = getLatIndex(relLat);
		int iLon = getLatIndex(relLon);
		int iDep = getLatIndex(relDep);
		int iHypoDep = getLatIndex(hypoDep);
		
		// solve for the total weight for the associated layers
		double normWt=0;
		// at same depth and below	// if at surface (iHypoDepth=0), should include all; if at bottom (iHypoDepth=numDepth-1), should include just 0th
		for(int d=0; d<numDepth-iHypoDep;d++)
			normWt += totWtAtDepth[d];
		// those above; none if iHypoDepth=0; those above if at bottom (iHypoDepth=numDepth-1)
		if(iHypoDep > 0)
			for(int d=1; d<=iHypoDep;d++)
				normWt += totWtAtDepth[d];
		
		// factor of four below is to account for the other 3 quadrants
		return nominalWt[iLat][iLon][iDep]/(normWt*4);
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
		IntegerPDF_FunctionSampler sampler;
		sampler = new IntegerPDF_FunctionSampler(numLatLon*numLatLon*numDepth);
		double[] distanceArray = new double[numLatLon*numLatLon*numDepth];
		int index=0;
		for(int iDep=0;iDep<numDepth; iDep++) {
			for(int iLat=0;iLat<numLatLon; iLat++) {
				for(int iLon=0;iLon<numLatLon; iLon++) {
					sampler.set(index,nominalWt[iLat][iLon][iDep]);
					distanceArray[index] = getDistance(iLat, iLon, iDep);
					index +=1;
				}
			}
		}
		
		// create histogram
		double deltaDistForHist = depthDiscr;
		double min = deltaDistForHist/2;
		int num = (int) Math.round((maxDistKm+10)/deltaDistForHist);	// plus 10 to test for zeros at end
		double max = (num)*deltaDistForHist-deltaDistForHist/2;
		EvenlyDiscretizedFunc distHistogram = new EvenlyDiscretizedFunc(min , max, num);
		distHistogram.setTolerance(deltaDistForHist);
		
		for(int i=0;i<numSamples;i++) {
			distHistogram.add(distanceArray[sampler.getRandomInt()], 1.0/numSamples);
		}
		
		EvenlyDiscretizedFunc targetHist = new EvenlyDiscretizedFunc(min , max, num);
		targetHist.setTolerance(deltaDistForHist);

		deltaDistForHist = depthDiscr/10;
		min = deltaDistForHist/2;
		num = (int) Math.round((maxDistKm+10)/deltaDistForHist);	// plus 10 to test for zeros at end
		max = (num)*deltaDistForHist-deltaDistForHist/2;
		EvenlyDiscretizedFunc target = new EvenlyDiscretizedFunc(min, max, num);
		
		
		for(int i=0; i<target.getNum();i++) target.set(i,Math.pow(target.getX(i)+minDist, -distDecay));
		double sum2 = target.calcSumOfY_Vals();
		for(int i=0; i<target.getNum();i++) target.set(i,target.getY(i)/sum2);
		for(int i=0; i<target.getNum();i++) targetHist.add(target.getX(i), target.getY(i));
		targetHist.setName("Target Distance Decay for Primary Aftershocks");

		
		// plot the results
		ArrayList funcs = new ArrayList();
		funcs.add(targetHist);
		funcs.add(distHistogram);
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "test"); 
//		graph.setAxisRange(1, 1200, 1e-6, 1);
//		graph.setYLog(true);
//		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
//		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2f, Color.RED));
//		plotChars.add(new PlotCurveCharacterstics(PlotSymbol.CROSS, 2f, Color.BLUE));
//		graph.setPlottingFeatures(plotChars);
//		graph.setX_AxisLabel("Distance (km)");
//		graph.setY_AxisLabel("Probability");




	}
	
	public double getEquivDistFast(int iLat, int iLon, int iDep, int numDiscr) {
		
		double midLat = getLat(iLat);
		double midLon = getLon(iLon);
		double midDepth = getDepth(iDep);
		double totSum = 0;
		double deltaLat = latLonDiscrDeg/numDiscr;
		double deltaLon = latLonDiscrDeg/numDiscr;
		double deltaDepth = depthDiscr/numDiscr;
		for(int latIndex = 0; latIndex < numDiscr; latIndex++) {
			double distLat = (midLat-latLonDiscrDeg + latIndex*deltaLat + deltaLat/2)*111.0;
			for(int lonIndex = 0; lonIndex < numDiscr; lonIndex++) {
				double distLon = (midLon-latLonDiscrDeg + lonIndex*deltaLon + deltaLon/2) * 111.0 * cosMidLat;
				for(int depIndex = 0; depIndex < numDiscr; depIndex++) {
					double distDepth = (midDepth-depthDiscr + depIndex*deltaDepth + deltaDepth/2);
					double dist = Math.sqrt(distLat*distLat+distLon*distLon+distDepth*distDepth);
					totSum += Math.pow(dist+minDist, -distDecay);
				}
			}
		}

		totSum /= (double)(numDiscr*numDiscr*numDiscr);
		
		double equivDist = Math.pow(totSum,-1.0/distDecay)-minDist;
		
//		double origDist = getDistance(iLat, iLon, iDep);
//		System.out.println("equivDist="+ equivDist+"\torigDist="+origDist);
//		
//		System.out.println("revisedWt="+ totSum+"\torigWt="+Math.pow(origDist+minDist, -distDecay));


		return equivDist;

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
		double latLonDiscrDeg=0.01;
		double depthDiscr=1.0;
		double midLat=38;
		double distDecay=2;
		double minDist=0.3;
		ETAS_LocationWeightCalculator calc = new ETAS_LocationWeightCalculator(maxDistKm, maxDepthKm, latLonDiscrDeg, 
				depthDiscr, midLat, distDecay, minDist);
		
		System.out.println("Testing randome samples...");
		calc.getEquivDistFast(0, 0, 0, 100);
		calc.testRandomSamples(1000000);

	}

}
