package scratch.ned.ETAS_Tests.MeanUCERF2;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.TreeMap;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.faultSurface.EvenlyGriddedSurface;
import org.opensha.sha.faultSurface.FaultTrace;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.utils.ModUCERF2.MeanUCERF2;

/**
 * This simply overrides nshmp_gridSrcGen of parent with one that goes down to M 2.5 for background seismicity
 * and changes b-values to 1.0 and ups the a-values to include aftershocks (in NSHMP_GridSourceGeneratorMod2)
 * 
 * @author field
 *
 */
public class MeanUCERF2_ETAS extends org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2 {
	
	public MeanUCERF2_ETAS() {
		nshmp_gridSrcGen = new NSHMP_GridSourceGeneratorMod2();
	}
	
	/**
	 * This plots various nucleation MDFs in a box around the Landers source
	 * 
	 * @param distance - distance from the landers fault trace to include
	 */
	public void plotMFD_InRegionNearLanders(double distance) {

//		Region region = new Region(new Location(33.85,-117.85),new Location(35.35,-115.85));
		
		FaultTrace landersTrace = getSource(195).getRupture(41).getRuptureSurface().getRowAsTrace(0);
		Region region = new Region(landersTrace, distance);
//		System.out.println("Landers Trace\n");
//		for(Location loc: landersTrace)
//			System.out.println(loc.getLongitude()+"\t"+loc.getLatitude());

		ArrayList<String> srcNamesList = new ArrayList<String>();
		HashMap<String,SummedMagFreqDist> srcMFDs = new HashMap<String,SummedMagFreqDist>();

		SummedMagFreqDist magFreqDist = new SummedMagFreqDist(5.05, 36, 0.1);
		double duration = getTimeSpan().getDuration();
		for (int s = 0; s < getNumSources(); ++s) {
			ProbEqkSource source = getSource(s);
			for (int r = 0; r < source.getNumRuptures(); ++r) {
				ProbEqkRupture rupture = source.getRupture(r);
				double mag = rupture.getMag();
				double equivRate = rupture.getMeanAnnualRate(duration);
				EvenlyGriddedSurface rupSurface = rupture.getRuptureSurface();
				double ptRate = equivRate/rupSurface.size();
				ListIterator<Location> it = rupSurface.getAllByRowsIterator();
				while (it.hasNext()) {
					//discard the pt if outside the region 
					if (!region.contains(it.next()))
						continue;
					//************** following if is temporary
//					if(source.getName().equals("Point2Vert_SS_FaultPoisSource")){
//						System.out.println("HERE:\t"+s+"\t"+(float)mag+"\t"+(float)equivRate);
//					}
					magFreqDist.addResampledMagRate(mag, ptRate, true);
					if(!srcMFDs.containsKey(source.getName())) {
						SummedMagFreqDist srcMFD = new SummedMagFreqDist(5.05, 36, 0.1);
						srcMFD.setName(source.getName());
						srcMFD.setInfo(" ");
						srcMFDs.put(source.getName(), srcMFD);
						srcNamesList.add(source.getName());
					}
					srcMFDs.get(source.getName()).addResampledMagRate(mag, ptRate, true);
				}
			}
		}
		
		TreeMap<Double,IncrementalMagFreqDist> treemap = new TreeMap<Double,IncrementalMagFreqDist>();
		double totRate = magFreqDist.getCumRate(6.05);
		for(String keyName: srcMFDs.keySet()) {
			SummedMagFreqDist mfd = srcMFDs.get(keyName);
			double rate = mfd.getCumRate(6.05);
			treemap.put(rate, mfd);
			double percent = Math.round(100.0*rate/totRate);
			mfd.setInfo("rate>=M6.0 = "+(float)rate+" ("+percent+" % of total)");
		}
		
		
		String info  = "Sources in box:\n\n";
		for(String name:srcNamesList)
			info += "\t"+name+"\n";

//		magFreqDist.setInfo(info);
		magFreqDist.setInfo(" ");
		magFreqDist.setName("Total Incremental MFD for nucleations within "+distance+" km");
		ArrayList funcs = new ArrayList();
		funcs.add(magFreqDist);
		EvenlyDiscretizedFunc cumMFD = magFreqDist.getCumRateDistWithOffset();
		cumMFD.setName("Total Cumulative MFD for nucleations within "+distance+" km");
		cumMFD.setInfo(" ");
		funcs.add(cumMFD);	
		for(Double key:treemap.keySet())
			funcs.add(treemap.get(key));
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Mag-Freq Dists for nucleations within "+distance+" km"); 
		graph.setX_AxisLabel("Mag");
		graph.setY_AxisLabel("Rate");
		graph.setY_AxisRange(1e-6, 0.1);
		graph.setX_AxisRange(5, 8);
		/**/
		ArrayList<PlotCurveCharacterstics> curveCharacteristics = new ArrayList<PlotCurveCharacterstics>();
		curveCharacteristics.add(new PlotCurveCharacterstics(PlotLineType.HISTOGRAM, 1f, null, 4f, Color.BLUE, 1));
		curveCharacteristics.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 4f, null, 4f, Color.BLACK, 1));
		graph.setPlottingFeatures(curveCharacteristics);
		
		graph.setYLog(true);
		try {
			graph.saveAsPDF("MFD_NearNorthridgeUCERF2.pdf");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

}
