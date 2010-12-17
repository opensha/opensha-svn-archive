package scratch.ned.ETAS_Tests;

import java.util.ArrayList;

import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;

public class ETAS_Simulator {
	
	ArrayList<EqksInGeoBlock> blockList;
	GriddedRegion griddedRegion;
	EqkRupForecast erf;
	
	
	public ETAS_Simulator(EqkRupForecast erf,GriddedRegion griddedRegion, boolean adaptiveBlocks) {
		this.erf = erf;
		this.griddedRegion = griddedRegion;

		makeAllEqksInGeoBlocks();
		
		//  Point source rupture:
/*		ProbEqkRupture rup = new ProbEqkRupture();
		rup.setMag(5);
		rup.setPointSurface(new Location(34,-118,8));
//		rup.setPointSurface(new Location(34.0125,	-118.0125,	6.0));
*/	
		// 68	S. San Andreas;CH+CC+BB+NM+SM+NSB+SSB+BG+CO	 #rups=8
		ProbEqkRupture rup = erf.getSource(68).getRupture(0);

		ETAS_PrimaryEventSampler sampler = new ETAS_PrimaryEventSampler(rup,blockList, erf, 1.4,2.0, adaptiveBlocks);
		sampler.testRandomDistanceDecay();
		sampler.writeRelBlockProbToFile();
	}

	
	public void makeAllEqksInGeoBlocks() {

		blockList = new ArrayList<EqksInGeoBlock>();
		for(Location loc: griddedRegion) {
			EqksInGeoBlock block = new EqksInGeoBlock(loc,griddedRegion.getSpacing(),0,16);
			blockList.add(block);
		}
		System.out.println("Number of Blocks: "+blockList.size()+" should be("+griddedRegion.getNodeCount()+")");


		double startTime=System.currentTimeMillis();
		System.out.println("Starting to make blocks");

		double forecastDuration = erf.getTimeSpan().getDuration();
		double rateUnAssigned = 0;
		int numSrc = erf.getNumSources();
		for(int s=0;s<numSrc;s++) {
			ProbEqkSource src = erf.getSource(s);
			//			System.out.println(s+"\t"+src.getName()+"\t"+numSrc);
			int numRups = src.getNumRuptures();
			for(int r=0; r<numRups;r++) {
				ProbEqkRupture rup = src.getRupture(r);
				ArbDiscrEmpiricalDistFunc numInEachNode = new ArbDiscrEmpiricalDistFunc(); // node on x-axis and num on y-axis
				EvenlyGriddedSurfaceAPI surface = rup.getRuptureSurface();
				double rate = rup.getMeanAnnualRate(forecastDuration);
				int numUnAssigned=0;
				for(Location loc: surface) {
					int nodeIndex = griddedRegion.indexForLocation(loc);
					if(nodeIndex != -1)
						numInEachNode.set((double)nodeIndex,1.0);
					else
						numUnAssigned +=1;
				}
				int numNodes = numInEachNode.getNum();
				if(numNodes>0) {
					for(int i=0;i<numNodes;i++) {
						int nodeIndex = (int)Math.round(numInEachNode.getX(i));
						double fracInside = numInEachNode.getY(i)/surface.size();
						double nodeRate = rate*fracInside;	// fraction of rate in node
						blockList.get(nodeIndex).processRate(nodeRate, fracInside, s, r, rup.getMag());
					}
				}
				float fracUnassigned = (float)numUnAssigned/(float)surface.size();
				if(numUnAssigned>0) System.out.println(fracUnassigned+" of rup "+r+" were unassigned for source "+s+" ("+erf.getSource(s).getName()+")");
				rateUnAssigned += rate*fracUnassigned;
			}
		}

		System.out.println("rateUnAssigned = "+rateUnAssigned);

		double runtime = (System.currentTimeMillis()-startTime)/1000;
		System.out.println("Making blocks took "+runtime+" seconds");

		System.out.println("TESTING RESULT");
		double testRate1=0;
		for(EqksInGeoBlock block: blockList) {
			testRate1+=block.getTotalRateInside();
		}
		testRate1+=rateUnAssigned;
		double testRate2=0;
		for(int s=0;s<numSrc;s++) {
			ProbEqkSource src = erf.getSource(s);
			int numRups = src.getNumRuptures();
			for(int r=0; r<numRups;r++) {
				testRate2 += src.getRupture(r).getMeanAnnualRate(forecastDuration);
			}
		}
		System.out.println("\tRate1="+testRate1);
		System.out.println("\tRate2="+testRate2);

/*
		// Make SubBlocks
		startTime=System.currentTimeMillis();
		System.out.println("Starting to make subblocks");
		ArrayList<EqksInGeoBlock> subBlockList = new ArrayList<EqksInGeoBlock>();
		int counter =0, counterThresh=100, counterIncr=100;
		for(EqksInGeoBlock block:blockList) {
			subBlockList.addAll(block.getSubBlocks(2, 2,erf));
			counter+=1;
			if(counter==counterThresh) {
				System.out.println("\t"+counter+"\tof\t"+blockList.size());
				counterThresh+=counterIncr;
			}
		}
		runtime = (System.currentTimeMillis()-startTime)/1000;
		System.out.println("Making sub-blocks took "+runtime+" seconds");
		*/
		
//		blockList.get(0).testRandomSampler();
		
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		CaliforniaRegions.RELM_GRIDDED griddedRegion = new CaliforniaRegions.RELM_GRIDDED();
		
		// Create UCERF2 instance
		System.out.println("Starting ERF instantiation");
		long startTime=System.currentTimeMillis();
		int duration = 1;
		MeanUCERF2 meanUCERF2 = new MeanUCERF2();
		meanUCERF2.setParameter(UCERF2.RUP_OFFSET_PARAM_NAME, new Double(10.0));
		meanUCERF2.getParameter(UCERF2.PROB_MODEL_PARAM_NAME).setValue(UCERF2.PROB_MODEL_POISSON);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
		meanUCERF2.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
		meanUCERF2.getTimeSpan().setDuration(duration);
		meanUCERF2.updateForecast();
		double runtime = (System.currentTimeMillis()-startTime)/1000;
		System.out.println("ERF instantiation took "+runtime+" seconds");
		
		// print out first 200 source names
		//for(int s=0;s<200;s++) System.out.println(s+"\t"+meanUCERF2.getSource(s).getName()+"\t #rups="+meanUCERF2.getSource(s).getNumRuptures());

		startTime=System.currentTimeMillis();
		ETAS_Simulator tests = new ETAS_Simulator(meanUCERF2,griddedRegion, true);
		runtime = (int)(System.currentTimeMillis()-startTime)/1000;
		System.out.println("Tests Run took "+runtime+" seconds");
	}

}
