package org.opensha.sha.calc;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import org.opensha.data.Location;
import org.opensha.data.Site;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.data.function.DiscretizedFuncAPI;
import org.opensha.exceptions.ParameterException;
import org.opensha.param.DoubleParameter;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.sha.earthquake.EqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.MeanUCERF2.MeanUCERF2;
import org.opensha.nshmp.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.IMT_Info;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;

public class GridHardcodedHazardMapCalculator implements ParameterChangeWarningListener {

	boolean xLogFlag = true;
	private DecimalFormat decimalFormat=new DecimalFormat("0.00##");
	boolean timer = true;
	boolean showResult = true;

	public GridHardcodedHazardMapCalculator(boolean debug) {
		timer = debug;
		showResult = debug;
		try {
			long start = 0;
			if (timer) {
				start = System.currentTimeMillis();
			}
			double maxDistance =  200.0;
			
			AttenuationRelationshipAPI imr = new CB_2008_AttenRel(this);
			imr.setIntensityMeasure(CB_2008_AttenRel.PGA_NAME);
			imr.setParamDefaults();

			//FileUtils.saveObjectInFile("/home/kevin/OpenSHA/condor/java_test_2/imr.obj", imr);

			//get the selected EqkRupForecast
			System.out.println("Creating Forecast");
			//EqkRupForecastAPI erf = new Frankel96_AdjustableEqkRupForecast();
			//EqkRupForecastAPI erf = new UCERF2();
			EqkRupForecastAPI erf = new MeanUCERF2();
			System.out.println("Updating Forecast");
			long start_erf = 0;
			if (timer) {
				start_erf = System.currentTimeMillis();
			}
			erf.updateForecast();
			if (timer) {
				System.out.println("Took " + getTime(start_erf) + " seconds to update forecast.");
			}


			HazardCurveCalculator calc =new HazardCurveCalculator();
			calc.setMaxSourceDistance(maxDistance);

			System.out.println("Setting up Hazard Function");
			IMT_Info imtInfo = new IMT_Info();
			ArbitrarilyDiscretizedFunc hazFunction = imtInfo.getDefaultHazardCurve(CB_2008_AttenRel.PGA_NAME);
			
			double[] xValues = new double[hazFunction.getNum()];
		    for(int i = 0; i<hazFunction.getNum(); ++i)
		    	xValues[i] = hazFunction.getX(i);
			// get the MAX_SOURCE distance
			
			int numPoints = xValues.length;
			
			ArbitrarilyDiscretizedFunc logHazFunction = initX_Values(hazFunction);
			
			System.out.println("Setting Up Site");
			Site site = new Site(new Location(34.053, -118.243));
			DoubleParameter vs30 = new DoubleParameter(CB_2008_AttenRel.VS30_NAME, CB_2008_AttenRel.VS30_UNITS);
			vs30.setValue(CB_2008_AttenRel.VS30_DEFAULT);
			
			DoubleParameter depth = new DoubleParameter(CB_2008_AttenRel.DEPTH_2pt5_NAME, CB_2008_AttenRel.DEPTH_2pt5_UNITS);
			depth.setValue(CB_2008_AttenRel.DEPTH_2pt5_DEFAULT);
			
			site.addParameter(vs30);
			site.addParameter(depth);
			
			long start_curve = 0;
			long start_post = 0;
			if (timer) {
				System.out.println(getTime(start) + " seconds total overhead before calculation");
				start_curve = System.currentTimeMillis();
			}
			
			System.out.println("Calculating Hazard Curve");
			calc.getHazardCurve(logHazFunction,site,imr,erf);
			System.out.println("Calculated a curve!");
			if (timer) {
				System.out.println("Took " + getTime(start_curve) + " seconds to calculate curve.");
				start_post = System.currentTimeMillis();
			}
			String lat = decimalFormat.format(site.getLocation().getLatitude());
			String lon = decimalFormat.format(site.getLocation().getLongitude());
			hazFunction = toggleHazFuncLogValues(hazFunction, logHazFunction);

			// write the result to the file
			System.out.println("Writing Results to File");
			FileWriter fr = new FileWriter(lat + "_" + lon + ".txt");
			for (int i = 0; i < numPoints; ++i)
				fr.write(hazFunction.getX(i) + " " + hazFunction.getY(i) + "\n");
			fr.close();
			if (timer) {
				System.out.println(getTime(start_post) + " seconds total overhead after calculation");
				System.out.println("Took " + getTime(start) + " seconds TOTAL.");
			}
			System.out.println("***DONE***");
			
			if (showResult) {
				ArrayList data = new ArrayList<ArbitrarilyDiscretizedFunc>();
				data.add(hazFunction);
				GraphWindow graph = new GraphWindow(data);
				graph.setVisible(true);
			}

		} catch (ParameterException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public String getTime(long before) {
		double time = ((double)System.currentTimeMillis() - (double)before)/1000d; 
		return new DecimalFormat(	"###.##").format(time);
	}

	private ArbitrarilyDiscretizedFunc initX_Values(DiscretizedFuncAPI arb) {
		ArbitrarilyDiscretizedFunc new_func = new ArbitrarilyDiscretizedFunc();
		// take log only if it is PGA, PGV or SA
		if (this.xLogFlag) {
			for (int i = 0; i < arb.getNum(); ++i)
				new_func.set(Math.log(arb.getX(i)), 1);
			return new_func;
		}
		else
			throw new RuntimeException("Unsupported IMT");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length > 1)
			new GridHardcodedHazardMapCalculator(false);
		else new GridHardcodedHazardMapCalculator(true);
		//System.exit(0);
	}

	public void parameterChangeWarning(ParameterChangeWarningEvent event) {

	}

	private ArbitrarilyDiscretizedFunc toggleHazFuncLogValues(
			ArbitrarilyDiscretizedFunc oldHazFunc, ArbitrarilyDiscretizedFunc logHazFunction) {
		int numPoints = oldHazFunc.getNum();
		ArbitrarilyDiscretizedFunc hazFunc = new ArbitrarilyDiscretizedFunc();
		// take log only if it is PGA, PGV or SA
		if (this.xLogFlag) {
			for (int i = 0; i < numPoints; ++i) {
				hazFunc.set(oldHazFunc.getX(i), logHazFunction.getY(i));
			}
			return hazFunc;
		}
		else
			throw new RuntimeException("Unsupported IMT");
	}

}
