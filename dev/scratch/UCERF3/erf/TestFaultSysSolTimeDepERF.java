package scratch.UCERF3.erf;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.Region;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.gui.infoTools.CalcProgressBar;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2;

public class TestFaultSysSolTimeDepERF {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		
//		FaultSystemSolutionTimeDepERF invERF = new FaultSystemSolutionTimeDepERF("/Users/field/ALLCAL_UCERF2.zip");
		
//		File fssFile = new File("dev/scratch/UCERF3/data/scratch/InversionSolutions/2012_10_14-fm3-logic-tree-sample-x5_MEAN_BRANCH_AVG_SOL.zip");
		String fileName="dev/scratch/UCERF3/data/scratch/InversionSolutions/2012_10_14-fm3-logic-tree-sample-x5_MEAN_BRANCH_AVG_SOL.zip";
		FaultSystemSolutionTimeDepERF invERF = new FaultSystemSolutionTimeDepERF(fileName);

		
		invERF.aleatoryMagAreaStdDevParam.setValue(0.0);
		invERF.bpt_AperiodicityParam.setValue(0.2);
		invERF.getTimeSpan().setStartTimeInMillis(0);
		invERF.getTimeSpan().setDuration(10000);	// yrs
		
		long runtime = System.currentTimeMillis();
		invERF.testER_SimulationFast();
		runtime -= System.currentTimeMillis();
		System.out.println("simulation took "+runtime/(1000*60)+" minutes");
//		System.exit(0);
//
//		invERF.getTimeSpan().setStartTime(1970);
//		invERF.getTimeSpan().setDuration(2299);
//		invERF.updateForecast();
////		System.out.println("done with invERF");
////		System.out.println("getNumSources() = "+invERF.getNumSources());
////		System.out.println("getTotNumRups() = "+invERF.getTotNumRups());
//		
//		System.out.println("CHANGING TIMESPAN TEST");
////		invERF.getTimeSpan().setStartTime(1971);
//		invERF.getTimeSpan().setDuration(1970);
//		invERF.updateForecast();
//		
//
//		TimeSpan timeSpan = invERF.getTimeSpan();
//		
//		System.out.println("timeSpan start year:  "+timeSpan.getStartTimeYear());
//		System.out.println("timeSpan start millis:  "+timeSpan.getStartTimeCalendar().getTimeInMillis());
//		System.out.println("timeSpan dur:  "+timeSpan.getDuration()+" ("+timeSpan.getDurationUnits()+")");
//		
//		System.out.println("s=4753 name: "+invERF.getSource(4753).getName()+"; nth rups:\n");
//		int[] rupIndices = invERF.get_nthRupIndicesForSource(4753);
//		for(int i=0; i<rupIndices.length;i++)
//			System.out.println("\t"+rupIndices[i]);
//		System.out.println("\n");
//		
//		long eventTime = -(long)(1.0*FaultSystemSolutionTimeDepERF.MILLISEC_PER_YEAR);
//		invERF.setRuptureOccurrence(27596, eventTime);
//		
//
//		
//		ModMeanUCERF2 modMeanUCERF2 = FindEquivUCERF2_FM2pt1_Ruptures.getMeanUCERF2_Instance();
//		modMeanUCERF2.updateForecast();
//		System.out.println("done with modMeanUCERF2");
//		
//		Region testRegion = new CaliforniaRegions.RELM_SOCAL();
//		
//		long startTime = System.currentTimeMillis();
//		SummedMagFreqDist invMFD = ERF_Calculator.getMagFreqDistInRegion(invERF, testRegion, 5.05, 40, 0.1, true);
//		runtime = System.currentTimeMillis()-startTime;
//		System.out.println("done with invMFD; took following seconds"+runtime/1000);
//		SummedMagFreqDist origMFD = ERF_Calculator.getMagFreqDistInRegion(modMeanUCERF2, testRegion, 5.05, 40, 0.1, true);
//		System.out.println("done with origMFD");
//
//		
//		ArrayList funcs = new ArrayList();
//		funcs.add(invMFD);
//		funcs.add(origMFD);
//		funcs.add(invMFD.getCumRateDistWithOffset());
//		funcs.add(origMFD.getCumRateDistWithOffset());
//		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Incremental Mag-Freq Dists"); 
//		graph.setX_AxisLabel("Mag");
//		graph.setY_AxisLabel("Rate");
//		graph.setYLog(true);
//		graph.setY_AxisRange(1e-6, 1.0);
//
//		
//		System.out.println("done");

		
		
		
		
//		CalcProgressBar progressBar = new CalcProgressBar("label1", "label2");
		//
//				progressBar.displayProgressBar();
////				progressBar.showProgress(true);
//				
//				try {
//					Thread.sleep(2000L);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
		//
		//
//				
//				for(int i=0;i<100;i++) {
//					try {
//						Thread.sleep(30L);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
		//
//					progressBar.updateProgress(i, 100);
//					progressBar.setProgressMessage("message");
//				}
//				
		//
//				
////				progressBar.setProgressMessage("here it is...");
		//
//				
//				
//				System.exit(0);

	}

}
