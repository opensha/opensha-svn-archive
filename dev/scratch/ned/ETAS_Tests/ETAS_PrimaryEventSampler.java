package scratch.ned.ETAS_Tests;

import java.io.FileWriter;
import java.util.ArrayList;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceRupParameter;

/**
 * 
 * @author field
 *
 */
public class ETAS_PrimaryEventSampler {
	
	ArrayList<EqksInGeoBlock> revisedBlockList, origBlockList;
	ProbEqkRupture parentRup;
	EqkRupForecast erf;
	double[] relBlockProb;
	int numBlocks;
	IntegerPDF_FunctionSampler randomBlockSampler;
	final static double ADAPT_BLOCK_DIST1 = 8;
	final static double ADAPT_BLOCK_DIST2 = 16;
	
	
	public ETAS_PrimaryEventSampler(ProbEqkRupture parentRup,ArrayList<EqksInGeoBlock> blockList, 
			EqkRupForecast erf, double distDecay, double minDist, boolean useAdaptiveBlocks) {
		
		this.parentRup=parentRup;
		origBlockList = blockList;
		this.erf = erf;
		
		// Histogram of distances - SHOULD BE LOG SPACED ON X AXIS?
		EvenlyDiscretizedFunc distHist = new EvenlyDiscretizedFunc(2.5, 400, 5.0);
		distHist.setTolerance(5.0);
		
		EvenlyGriddedSurfaceAPI rupSurf = parentRup.getRuptureSurface();
		
		ArrayList<Double> blockDistances = new ArrayList<Double>();

		revisedBlockList = new ArrayList<EqksInGeoBlock>();
		ArrayList<EqksInGeoBlock> subBlocks = new ArrayList<EqksInGeoBlock>();

		System.out.println("computing block distances");
		int counter=0, thresh = origBlockList.size()/20, threshIncr=thresh;
		for(EqksInGeoBlock origBlock : origBlockList) {
			counter++;
			if(counter>thresh) {
				System.out.println("\t"+(float)counter/(float)origBlockList.size());
				thresh += threshIncr;
			}
			double dist = LocationUtils.distanceToSurfFast(origBlock.getBlockCenterLoc(), rupSurf);
			if(dist>ADAPT_BLOCK_DIST2  || !useAdaptiveBlocks) {
				blockDistances.add(dist);
				revisedBlockList.add(origBlock);
				distHist.add(dist, 1.0);
			}
			else {  // apply adaptive block sizes
				if (dist > ADAPT_BLOCK_DIST1) 
					subBlocks = origBlock.getSubBlocks(2, 2, erf);
				else 
					subBlocks = origBlock.getSubBlocks(4, 4, erf);
				for(EqksInGeoBlock subBlock:subBlocks) {
					double dist2 = LocationUtils.distanceToSurfFast(subBlock.getBlockCenterLoc(), rupSurf);
					blockDistances.add(dist2);
					revisedBlockList.add(subBlock);
					distHist.add(dist2, 1.0);

				}
			}
		}
		numBlocks = revisedBlockList.size();

		//		System.out.println(distHist);

		// compute relative probability of each block
		System.out.println("computing relative block probabilities");
		relBlockProb = new double[numBlocks];
		double total=0;
		for(int i=0; i<numBlocks;i++) {
			relBlockProb[i] = revisedBlockList.get(i).getTotalRateInside()*Math.pow(blockDistances.get(i)+minDist, -distDecay)/distHist.getClosestY(blockDistances.get(i));
//			relBlockProb[i] = Math.pow(blockDistances[i]+minDist, -distDecay)/distHist.getClosestY(blockDistances[i]);
			total += relBlockProb[i];
		}
		// create the random block sampler
		randomBlockSampler = new IntegerPDF_FunctionSampler(numBlocks);
		for(int i=0; i<numBlocks;i++) {
			relBlockProb[i] /= total;	// normalize so total is 1.0	
			randomBlockSampler.set(i,relBlockProb[i]);
		}
		System.out.println("Done computing relative block probabilities");

	}
	
	
	public int getRandomBlockIndex() {
		return randomBlockSampler.getRandomInt();
	}
	
	
	// Check random relBlockProb reproduction
	
	
	
	public void writeRelBlockProbToFile() {
		
		System.out.println("starting random block sampling test");
		/**/
		
		/**/
		EvenlyDiscretizedFunc testFunc = new EvenlyDiscretizedFunc(0.0, numBlocks, 1.0);
		int numSamples=100000000;
		int thresh = 10000000, threshIncr=10000000;
		for(int i=0;i<numSamples;i++) {
			testFunc.add(randomBlockSampler.getRandomInt(),1.0);
			if(i==thresh){
				System.out.println("\t"+((float)thresh/(float)numSamples));
				thresh+=threshIncr;
			}
		}
		for(int i=0;i<testFunc.getNum();i++) testFunc.set(i,testFunc.getY(i)/numSamples);
		
		// make orig dist for plotting comparison
		EvenlyDiscretizedFunc origFunc = new EvenlyDiscretizedFunc(0.0, numBlocks, 1.0);
		for(int i=0;i<numBlocks;i++)  origFunc.set(i,relBlockProb[i]);
		
		
		// plot functions
		ArrayList funcs = new ArrayList();
		funcs.add(origFunc);
		funcs.add(testFunc);
		GraphiWindowAPI_Impl sr_graph = new GraphiWindowAPI_Impl(funcs, "");  

		
		System.out.println("starting writeRelBlockProbToFile()");
		try{
			FileWriter fw1 = new FileWriter("/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_Tests/relBlockProbs.txt");
			fw1.write("lat\tlon\trelProb\trandProbs\n");
			thresh = numBlocks/10;
			threshIncr=thresh;
			for(int i=0;i<numBlocks;i++) {
				Location loc = revisedBlockList.get(i).getBlockCenterLoc();
				fw1.write((float)loc.getLatitude()+"\t"+(float)loc.getLongitude()+"\t"+(float)relBlockProb[i]+"\t"+testFunc.getY(i)+"\n");
				if(i==thresh){
					System.out.println("\t"+((float)thresh/(float)numBlocks));
					thresh+=threshIncr;
				}
			}
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
