package scratch.ned.ETAS_ERF;

import java.util.ArrayList;

import org.opensha.commons.data.function.ArbDiscrEmpiricalDistFunc;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationList;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;

import scratch.UCERF3.erf.FaultSystemSolutionPoissonERF;

public class EqksInGeoBlockUtils {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * This creates an EqksInGeoBlock for the given ERF at each point in the GriddedRegion region
	 */
	public static ArrayList<EqksInGeoBlock> makeAllEqksInGeoBlocks(FaultSystemSolutionPoissonERF erf, GriddedRegion griddedRegion, double maxDepth) {

		double calcStartTime=System.currentTimeMillis();
		System.out.println("Starting to make blocks");

		ArrayList<EqksInGeoBlock> blockList = new ArrayList<EqksInGeoBlock>();
		for(Location loc: griddedRegion) {
			EqksInGeoBlock block = new EqksInGeoBlock(loc,griddedRegion.getSpacing(),0,maxDepth);
			blockList.add(block);
		}
		System.out.println("Number of Blocks: "+blockList.size()+" should be("+griddedRegion.getNodeCount()+")");

		double forecastDuration = erf.getTimeSpan().getDuration();
		double rateUnAssigned = 0;
		int numSrc = erf.getNumSources();
		double maxRupDepth = 0;
		for(int s=0;s<numSrc;s++) {
			ProbEqkSource src = erf.getSource(s);
			int numRups = src.getNumRuptures();
			for(int r=0; r<numRups;r++) {
				ProbEqkRupture rup = src.getRupture(r);
				ArbDiscrEmpiricalDistFunc numInEachNode = new ArbDiscrEmpiricalDistFunc(); // node on x-axis and num on y-axis
				LocationList locsOnRupSurf = rup.getRuptureSurface().getEvenlyDiscritizedListOfLocsOnSurface();
				double rate = rup.getMeanAnnualRate(forecastDuration);
				int numUnAssigned=0;
				for(Location loc: locsOnRupSurf) {
					if(maxRupDepth<loc.getDepth()) maxRupDepth = loc.getDepth();
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
						double fracInside = numInEachNode.getY(i)/(double)locsOnRupSurf.size();
						double nodeRate = rate*fracInside;	// fraction of rate in node
						int nthRup = erf.getIndexN_ForSrcAndRupIndices(s, r);
						blockList.get(nodeIndex).processRate(nodeRate, fracInside, nthRup, rup.getMag());
					}
				}
				float fracUnassigned = (float)numUnAssigned/(float)locsOnRupSurf.size();
				if(numUnAssigned>0) 
					System.out.println(fracUnassigned+" (fraction) of rup "+r+" were unassigned for source "+s+" ("+erf.getSource(s).getName()+")");
				rateUnAssigned += rate*fracUnassigned;
			}
		}
		
		if(maxRupDepth > maxDepth) {
			throw new RuntimeException("ruptures go deepter the the given maxDepth:\tmaxRupDepth="+maxRupDepth);
		}

		System.out.println("rateUnAssigned = "+rateUnAssigned);

		double runtime = (System.currentTimeMillis()-calcStartTime)/1000;
		System.out.println("Making blocks took "+runtime+" seconds");

		// This checks to make sure total rate in all blocks (plus rate unassigned) is equal the the total ERF rate
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
		System.out.println("\tRate1="+(float)testRate1+" should equal Rate2="+(float)testRate2+";\tratio="+(float)(testRate1/testRate2));
		
//		System.out.println("LONGER TEST OF BLOCKS");
//		
//		System.out.println("Testing block 630");
//		blockList.get(630).testThisBlock(erf);
//		
//		
//		int index=0;
//		for(EqksInGeoBlock block: blockList) {
//			System.out.println(index);
//			block.testThisBlock(erf);
//			index +=1;
//		}

		
		return blockList;
		
	}
	
	/**
	 * This checks whether the total rate in sub-block lists is equal to the rate in the main block
	 * @param erf
	 * @param blockList
	 * @param subBlockList1
	 * @param subBlockList2
	 */
	public static void testSubBlockListRates(AbstractERF erf, ArrayList<EqksInGeoBlock> blockList, 
			ArrayList<ArrayList<EqksInGeoBlock>> subBlockList1, ArrayList<ArrayList<EqksInGeoBlock>> subBlockList2) {
		
//		System.out.println("Testing sub-block rates:\t");
		
		boolean blockList1_Tested = false;
		boolean blockList2_Tested = false;
		double totalRateOverAllBlocks =0;
		
		// test blockList1
		for(int b=0; b<blockList.size(); b++) {
			double blockRate=blockList.get(b).getTotalRateInside();
			totalRateOverAllBlocks += blockRate;
			ArrayList<EqksInGeoBlock> blocks1 = subBlockList1.get(b);
			if(blocks1 != null) {
				blockList1_Tested = true;
				double totRate=0;
				for(EqksInGeoBlock blk:blocks1) {
					totRate += blk.getTotalRateInside();
				}
				double ratio = totRate/blockRate;
				if(Math.abs(totRate) < 1e-15 && Math.abs(blockRate) < 1e-15 )
					ratio = 1;
				if(ratio <0.999 || ratio > 1.001) {
					throw new RuntimeException("Descrepancy for block "+b+" of blockList1;\tratio="+ratio+
							"\ttotRate="+totRate+"\tblockRate="+blockRate);
				}

				
			}
			
			ArrayList<EqksInGeoBlock> blocks2 = subBlockList2.get(b);
			if(blocks2 != null) {
				blockList2_Tested = true;
				double totRate=0;
				for(EqksInGeoBlock blk:blocks2) {
					totRate += blk.getTotalRateInside();
				}
				double ratio = totRate/blockRate;
				if(Math.abs(totRate) < 1e-15 && Math.abs(blockRate) < 1e-15 )
					ratio = 1;
				if(ratio <0.999 || ratio > 1.001) {
					throw new RuntimeException("Descrepancy for block "+b+" of blockList2;\tratio="+ratio+
							"\ttotRate="+totRate+"\tblockRate="+blockRate);
				}
			}
		}
//		System.out.println("blockList1 was testable = "+blockList1_Tested+
//				";\tblockList2 was testable = "+blockList2_Tested+
//				"\ttotalRateOverAllBlocks="+totalRateOverAllBlocks);
	}
}
