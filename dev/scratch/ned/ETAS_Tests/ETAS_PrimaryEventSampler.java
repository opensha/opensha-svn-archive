package scratch.ned.ETAS_Tests;

import java.io.FileWriter;
import java.util.ArrayList;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceRupParameter;

public class ETAS_PrimaryEventSampler {
	
	ArrayList<EqksInGeoBlock> revisedBlockList, origBlockList;
	ProbEqkRupture parentRup;
	EqkRupForecast erf;
	double[] relBlockProb;
	
	public ETAS_PrimaryEventSampler(ProbEqkRupture parentRup,ArrayList<EqksInGeoBlock> blockList, EqkRupForecast erf, double distDecay, double minDist) {
		
		this.parentRup=parentRup;
		origBlockList = blockList;
		this.erf = erf;
		
		// no resampling yet
		revisedBlockList=origBlockList;
		
		int numBlocks = revisedBlockList.size();
		
		// compute distances
		EvenlyDiscretizedFunc distHist = new EvenlyDiscretizedFunc(2.5, 400, 5.0);
		distHist.setTolerance(5.0);
		EvenlyGriddedSurfaceAPI rupSurf = parentRup.getRuptureSurface();
		double[] blockDistances = new double[numBlocks];
		System.out.println("computing block distances from rup");
		for(int i=0; i<numBlocks;i++) {
			// THE FOLLOWING PARAMETER ASSUMES THE LOCATION IS AT ZERO DEPTH (FOR EFFICIENCY)?
			blockDistances[i] = DistanceRupParameter.getDistance(revisedBlockList.get(i).getBlockCenterLoc(), rupSurf);
			distHist.add(blockDistances[i], 1.0);
		}
//		System.out.println(distHist);
		
		
		// compute relative probability of each block
		System.out.println("computing relative block probabilities");
		relBlockProb = new double[numBlocks];
		double total=0;
		for(int i=0; i<numBlocks;i++) {
//			relBlockProb[i] = revisedBlockList.get(i).getTotalRateInside()*Math.pow(blockDistances[i]+minDist, -distDecay)/distHist.getClosestY(blockDistances[i]);
			relBlockProb[i] = Math.pow(blockDistances[i]+minDist, -distDecay)/distHist.getClosestY(blockDistances[i]);
			total += relBlockProb[i];
		}
		for(int i=0; i<numBlocks;i++) relBlockProb[i] /= total;	// normalize so total is 1.0	
		System.out.println("Done computing relative block probabilities");

	}
	
	
	
	public void writeRelBlockProbToFile() {
		System.out.println("starting writeRelBlockProbToFile()");
		try{
			FileWriter fw1 = new FileWriter("/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_Tests/relBlockProbs.txt");
			String outputString1 = new String();
			outputString1+= "lat\tlon\trelProb\n";
			for(int i=0;i<revisedBlockList.size();i++) {
				Location loc = revisedBlockList.get(i).getBlockCenterLoc();
				outputString1 += (float)loc.getLatitude()+"\t"+(float)loc.getLongitude()+"\t"+(float)relBlockProb[i]+"\n";
			}
			fw1.write(outputString1);
			fw1.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done with writeRelBlockProbToFile()");

	}
	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
