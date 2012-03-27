package scratch.UCERF3.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import org.apache.commons.math.stat.StatUtils;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.xyz.EvenlyDiscrXYZ_DataSet;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.commons.util.FileUtils;

import com.google.common.base.Preconditions;

/**
 * This loads in Glenn's paleoseismic trench probabilities.
 * 
 * @author Kevin
 *
 */
public class PaleoProbabilityModel {
	
	private EvenlyDiscrXYZ_DataSet xyz;
	private ArbitrarilyDiscretizedFunc dispMagFunc;
	
	private PaleoProbabilityModel(EvenlyDiscrXYZ_DataSet xyz, ArbitrarilyDiscretizedFunc dispMagFunc) {
		this.xyz = xyz;
		this.dispMagFunc = dispMagFunc;
	}
	
	public double getForSlip(double slip, double distAlongRup) {
		Preconditions.checkArgument(!Double.isNaN(slip), "slip cannot be NaN!");
		if (slip < dispMagFunc.getMinX())
			return 0;
		if (slip > dispMagFunc.getMaxX())
			return 1;
		return getForMag(dispMagFunc.getInterpolatedY(slip), distAlongRup);
	}
	
	public double getForMag(double mag, double distAlongRup) {
		Preconditions.checkArgument(distAlongRup >= xyz.getMinX() && distAlongRup <= xyz.getMaxX(),
				"distance along rup must be between "+xyz.getMinX()+" and "+xyz.getMaxX());
		Preconditions.checkArgument(!Double.isNaN(mag), "magnitude cannot be NaN!");
		if (mag < xyz.getMinY())
			return 0;
		if (mag > xyz.getMaxY())
			return 1;
		return xyz.bilinearInterpolation(distAlongRup, mag);
	}
	
	public static PaleoProbabilityModel loadUCERF3PaleoProbabilityModel() throws IOException {
		return fromURL(UCERF3_DataUtils.locateResource("paleoRateData", "pdetection2.txt"));
	}
	
	public static PaleoProbabilityModel fromFile(File file) throws IOException {
		try {
			return fromURL(file.toURI().toURL());
		} catch (MalformedURLException e) {
			throw ExceptionUtils.asRuntimeException(e);
		}
	}
	
	public static PaleoProbabilityModel fromURL(URL url) throws IOException {
		double[] xVals = null;
		ArrayList<Double> yVals = new ArrayList<Double>();
		ArrayList<double[]> vals = new ArrayList<double[]>();
		ArbitrarilyDiscretizedFunc dispMagFunc = new ArbitrarilyDiscretizedFunc();
		int numVals = -1;
		for (String line : FileUtils.loadFile(url)) {
			StringTokenizer tok = new StringTokenizer(line.trim());
			if (numVals < 0)
				// minus 2 because we don't want the first two columns here
				numVals = tok.countTokens()-2;
			
			double[] lineVals = new double[numVals];
			double mag = Double.parseDouble(tok.nextToken());
			double disp = Double.parseDouble(tok.nextToken());
			for (int i=0; i<numVals; i++)
				lineVals[i] = Double.parseDouble(tok.nextToken());
			
			if (xVals == null) {
				// first line
				xVals = lineVals;
			} else {
				// regular line
				yVals.add(mag);
				vals.add(lineVals);
				dispMagFunc.set(disp, mag);
			}
		}
		double minX = StatUtils.min(xVals);
		EvenlyDiscrXYZ_DataSet xyz = new EvenlyDiscrXYZ_DataSet(
				xVals.length, yVals.size(), minX, Collections.min(yVals), Math.abs(xVals[1]-xVals[0]));
		
		for (int yInd=0; yInd<yVals.size(); yInd++)
			for (int xInd=0; xInd<xVals.length; xInd++)
				xyz.set(xVals[xInd], yVals.get(yInd), vals.get(yInd)[xInd]);
		
		for (int i=0; i<xyz.size(); i++)
			Preconditions.checkState(xyz.get(i) >= 0, "something didn't get set right!");
		Preconditions.checkState((float)xyz.getMaxX() == (float)StatUtils.max(xVals),
				"maxX is incorrect! "+(float)xyz.getMaxX()+" != "+(float)StatUtils.max(xVals));
		Preconditions.checkState((float)xyz.getMaxY() == Collections.max(yVals).floatValue(),
				"maxY is incorrect! "+(float)xyz.getMaxY()+" != "+Collections.max(yVals).floatValue());
		
		return new PaleoProbabilityModel(xyz, dispMagFunc);
	}
	
	public void writeTableData() {
		for(double mag=5d; mag <=8.05; mag+=0.5) {
			double aveSlip = dispMagFunc.getFirstInterpolatedX(mag);
			double p05=getForMag(mag,0.05);
			double p25=getForMag(mag,0.25);
			double p50=getForMag(mag,0.4999);
			System.out.println((float)aveSlip+"\t"+mag+"\t"+(float)p05+"\t"+(float)p25+"\t"+(float)p50);
		}
	}
	
	public static void main(String[] args) throws IOException {
		PaleoProbabilityModel model = loadUCERF3PaleoProbabilityModel();
		model.writeTableData();
//		for (int yInd=0; yInd<model.xyz.getNumY(); yInd++) {
//			String line = null;
//			for (int xInd=0; xInd<model.xyz.getNumX(); xInd++) {
//				if (line == null)
//					line = "";
//				else
//					line += "\t";
//				line += model.xyz.get(xInd, yInd);
//			}
//			System.out.println(line);
//		}
	}

}