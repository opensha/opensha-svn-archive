package scratch.ned.ETAS_ERF.sandbox;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

public class ETAS_BlockWeightCalculator {
	
	int numLatLon, numDepth;
	double maxLatLonDeg, maxDepthKm, latLonDiscrDeg, depthDiscr, midLat, maxDistKm;
	
	
	public ETAS_BlockWeightCalculator(double maxDistKm, double maxDepthKm, double latLonDiscrDeg, double depthDiscr, 
			double midLat, double distDecay, double minDist) {
		
		double cosMidLat = Math.cos(midLat*Math.PI/180);
		double aveLatLonDiscrKm = (latLonDiscrDeg+cosMidLat*latLonDiscrDeg)*111/2.0;
		long startTime = System.currentTimeMillis();
		this.maxDistKm = maxDistKm;
		this.maxLatLonDeg = maxDistKm/(111*cosMidLat);	// degrees
		
		this.maxDepthKm = maxDepthKm;
		this.latLonDiscrDeg = latLonDiscrDeg;
		this.depthDiscr = depthDiscr;
		this.midLat = midLat;
		
		numLatLon = (int)Math.round(maxLatLonDeg/latLonDiscrDeg) + 1;
		numDepth = (int)Math.round(maxDepthKm/depthDiscr) + 1;
	
		System.out.println("aveLatLonDiscrKm="+aveLatLonDiscrKm+
				"\nmaxLatLonDeg="+maxLatLonDeg+
				"\ncosMidLat="+cosMidLat+
				"\nnumLatLon="+numLatLon+
				"\nnumDepth="+numDepth);

		double deltaDistForHist = Math.round(2*aveLatLonDiscrKm);
		double min = deltaDistForHist/2;
		int num = (int) Math.round(maxDistKm/deltaDistForHist);
		double max = num*deltaDistForHist-deltaDistForHist/2;
		EvenlyDiscretizedFunc distHistogram = new EvenlyDiscretizedFunc(min , max, num);
		distHistogram.setTolerance(deltaDistForHist);

		
		double[][][] distances = new double[numLatLon][numLatLon][numDepth];
		double[][][] nominalWt = new double[numLatLon][numLatLon][numDepth];
		for(int iLat=0;iLat<numLatLon; iLat++) {
			double lat = getLat(iLat);
			for(int iLon=0;iLon<numLatLon; iLon++) {
				double lon = getLon(iLon);
				for(int iDep=0;iDep<numDepth; iDep++) {
					double dep = getDepth(iDep);
					
					double latDistKm = lat*111;
					double lonDistKm = lon*111*cosMidLat;
					double dist = Math.sqrt(latDistKm*latDistKm+lonDistKm*lonDistKm+dep*dep);
					if(dist<maxDistKm)
						distHistogram.add(dist, 1.0);
					distances[iLat][iLon][iDep] = dist;
					nominalWt[iLat][iLon][iDep] = Math.pow(dist+minDist, -distDecay);
				}
			}
		}
		
		ArrayList funcs = new ArrayList();
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
		

		
		System.out.println("Constructor runtime = "+ (System.currentTimeMillis()-startTime)/1000 +" sec");

	}
	
	private double getLat(int iLat) {
		return iLat*latLonDiscrDeg+latLonDiscrDeg/2.0;
	}
	
	private double getLon(int iLon) {
		return iLon*latLonDiscrDeg+latLonDiscrDeg/2.0;
	}
	
	private double getDepth(int iDep) {
		return iDep*depthDiscr+depthDiscr/2.0;
	}




	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// double maxDistKm, double maxDepthKm, double latLonDiscrDeg, double depthDiscr, double midLat, double distDecay, double minDist
		ETAS_BlockWeightCalculator calc = new ETAS_BlockWeightCalculator(1000.0, 24.0, 0.01, 1.0, 38.0, 2, 0.3);

	}

}
