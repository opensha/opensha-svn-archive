package scratch.ned.ETAS_Tests;

import java.awt.Color;
import java.io.FileWriter;
import java.util.ArrayList;

import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.LocationUtils;
import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.imr.param.PropagationEffectParams.DistanceRupParameter;

/**
 * This class allows one to randomly sample which block a primary aftershock nucleates in 
 * based on the original block rates and distance decay.
 * @author field
 *
 */
public class ETAS_PrimaryEventSampler {
	
	ArrayList<EqksInGeoBlock> origBlockList;	// the original list of blocks given
	ArrayList<EqksInGeoBlock> revisedBlockList;	// a revised list (in case some close to main shock need to be more densely sampled
	ArrayList<Double> revisedBlockDistances;	// distances to parentRup for the revised blocks
	ProbEqkRupture parentRup;					// the main shock
	EqkRupForecast erf;							// the ERF from which primary aftershocks will be sampled
	double[] relBlockProb;						// the relative probability that each block will nucleate an event (REDUNDANT WITH randomBlockSampler BELOW)
	int numBlocks;
	IntegerPDF_FunctionSampler randomBlockSampler;	// used to randomly sample blocks
	final static double ADAPT_BLOCK_DIST1 = 8;		// first distance threshold for sampling blocks more densely
	final static double ADAPT_BLOCK_DIST2 = 16;		// second distance threshold for sampling blocks more densely
	double distDecay, minDist;
	
	
	/**
	 * 
	 * @param parentRup - the main shock
	 * @param blockList - the list of blocks to sample from
	 * @param erf - the ERF from which primary aftershocks are sampled from
	 * @param distDecay - distance decay parameter
	 * @param minDist - value that prevents singularity at zero distance
	 * @param useAdaptiveBlocks - indicate whether blocks close to parentRup should be sub-sampled
	 * @param includeBlockRates - whether or not to use rates inside of blocks to modify spatial probabilities
	 */
	public ETAS_PrimaryEventSampler(ProbEqkRupture parentRup,ArrayList<EqksInGeoBlock> blockList, 
			EqkRupForecast erf, double distDecay, double minDist, boolean useAdaptiveBlocks, boolean includeBlockRates) {
		
		this.parentRup=parentRup;
		origBlockList = blockList;
		this.erf = erf;
		this.distDecay = distDecay;
		this.minDist = minDist;
		
		EvenlyGriddedSurfaceAPI rupSurf = parentRup.getRuptureSurface();
		
		revisedBlockList = new ArrayList<EqksInGeoBlock>();  // revised is for replacing blocks with sub-blocks close in
		ArrayList<EqksInGeoBlock> subBlocks = new ArrayList<EqksInGeoBlock>();
		revisedBlockDistances = new ArrayList<Double>();

		System.out.println("computing block distances");
		int counter=0, counterThresh = origBlockList.size()/20, counterIncr=counterThresh;
		for(EqksInGeoBlock origBlock : origBlockList) {
			counter++;
			if(counter>counterThresh) {
				System.out.println("\t"+(float)counter/(float)origBlockList.size());
				counterThresh += counterIncr;
			}
			double dist = LocationUtils.distanceToSurfFast(origBlock.getBlockCenterLoc(), rupSurf);
			if(dist>ADAPT_BLOCK_DIST2  || !useAdaptiveBlocks) {
				revisedBlockDistances.add(dist);
				revisedBlockList.add(origBlock);
			}
			else {  // apply adaptive block sizes
				if (dist > ADAPT_BLOCK_DIST1) 
					subBlocks = origBlock.getSubBlocks(2, 2, erf);
				else 
					subBlocks = origBlock.getSubBlocks(4, 4, erf);
				for(EqksInGeoBlock subBlock:subBlocks) {
					double dist2 = LocationUtils.distanceToSurfFast(subBlock.getBlockCenterLoc(), rupSurf);
					revisedBlockDistances.add(dist2);
					revisedBlockList.add(subBlock);
				}
			}
		}
		numBlocks = revisedBlockList.size();
		
		System.out.println("num revised blocks="+numBlocks);
		
		// find closest block
		double minBlockDist=Double.MAX_VALUE;
		int minDistIndex=-1;
		for(int i=0;i<revisedBlockDistances.size() ;i++) {
			double dist = revisedBlockDistances.get(i);
			if(dist<minBlockDist) {
				minBlockDist=dist;
				minDistIndex = i;
			}
		}
		System.out.println("minBlockDist ="+minBlockDist+" for block index "+minDistIndex);

		// compute relative probability of each block
		System.out.println("computing relative block probabilities");
		relBlockProb = new double[numBlocks];
		
		double rupLength = parentRup.getRuptureSurface().getSurfaceLength();
		System.out.println("rupLength="+rupLength);
		
		if(rupLength>5.0) {	// a non point source rupture
			double total=0;
			for(int i=0; i<numBlocks;i++) {
				double vol = revisedBlockList.get(i).getBlockVolume();
				double dist = revisedBlockDistances.get(i);
				double blockWt = vol/(2*Math.PI*(dist+minDist)+2*rupLength); // fraction of the annulus
				relBlockProb[i] = Math.pow(revisedBlockDistances.get(i)+minDist, -distDecay)*blockWt;
				if(includeBlockRates) relBlockProb[i] *= revisedBlockList.get(i).getTotalRateInside()/vol;
				total += relBlockProb[i];
			}
			// create the random block sampler
			randomBlockSampler = new IntegerPDF_FunctionSampler(numBlocks);
			for(int i=0; i<numBlocks;i++) {
				relBlockProb[i] /= total;	// normalize so total is 1.0	
				randomBlockSampler.set(i,relBlockProb[i]);
			}
		}
		else {	// Point-source case
			double total=0;
			double closestBlockVal=0;
			if(minBlockDist>1) // check whether nucleation point is closer to the center or edge of closest block
				minDistIndex=-1;  // don't do special case below
			for(int i=0; i<numBlocks;i++) {
				double vol = revisedBlockList.get(i).getBlockVolume();
				double dist = revisedBlockDistances.get(i);
				double blockWt;
				if(i == minDistIndex) {  // this will calculate the fraction in here directly; value of "1" depends on smallest block size!
					double radius = Math.pow(vol/(4*Math.PI),0.33333);	// 4*PI*r^3;
					closestBlockVal=getDecayFractionInsideSphericalVolume(distDecay, minDist, radius);
					relBlockProb[i] = closestBlockVal;
					System.out.println("Calculated wt of closest block directly");
				}
				else {
					blockWt = vol/(2*Math.PI*(dist)+2*rupLength);
					relBlockProb[i] = Math.pow(revisedBlockDistances.get(i)+0.1, -distDecay)*blockWt; // min-dist of 0.1 was found by trial and error
					total += relBlockProb[i];
				}
			}
			// normalize
			for(int i=0; i<numBlocks;i++)
				if(i != minDistIndex)
					relBlockProb[i] *= (1-closestBlockVal)/total;	// normalize so total is 1.0
			// add block rates
			double totalHere = 0;
			if(includeBlockRates) {
				for(int i=0; i<numBlocks;i++) {
					relBlockProb[i] *= revisedBlockList.get(i).getTotalRateInside();
					totalHere += relBlockProb[i];
				}
			}
			else
				totalHere = 1;

			// final normalization and to create the random block sampler
			randomBlockSampler = new IntegerPDF_FunctionSampler(numBlocks);
			for(int i=0; i<numBlocks;i++) {
				relBlockProb[i] /= totalHere;	
				randomBlockSampler.set(i,relBlockProb[i]);
			}

		}
		
		System.out.println("should equal 1.0:\t"+randomBlockSampler.getSumOfY_Vals());
		
		System.out.println("Done computing relative block probabilities");

	}
	
	/**
	 * This returns a randomly sampled block for a primary aftershock
	 * @return
	 */
	public int sampleRandomBlockIndex() {
		return randomBlockSampler.getRandomInt();
	}
	
	/**
	 * This returns the revised block list
	 * @return
	 */
	public ArrayList<EqksInGeoBlock> getRevisedBlockList () { return revisedBlockList; }
	
	
	/**
	 * This returns the random block sampler (where the relative rate of each block is on the y-axis, 
	 * and the block index is both the function index and x-axis value)
	 * @return
	 */
	public IntegerPDF_FunctionSampler getRandomBlockSampler() {return randomBlockSampler;}
	
	
	/**
	 * Check and write results
	 */
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
		
		System.out.println("starting writeRelBlockProbToFile()");
		try{
			FileWriter fw1 = new FileWriter("/Users/field/workspace/OpenSHA/dev/scratch/ned/ETAS_Tests/relBlockProbs.txt");
			fw1.write("index\tlat\tlon\tdepth\trelProb\trandProbs\tvol\tblockDist\n");
			for(int i=0;i<numBlocks;i++) {
				Location loc = revisedBlockList.get(i).getBlockCenterLoc();
				double vol = revisedBlockList.get(i).getBlockVolume(); // normalize rates by vol for adaptive block spacing
				fw1.write(i+"\t"+(float)loc.getLatitude()+"\t"+(float)loc.getLongitude()+"\t"+(float)loc.getDepth()+"\t"+(float)relBlockProb[i]+"\t"+
						testFunc.getY(i)+"\t"+vol+"\t"+revisedBlockDistances.get(i)+"\n");
			}
			fw1.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Done with writeRelBlockProbToFile()");

	}
	
	public void testRandomDistanceDecay() {
		double deltaDist = 5;
		int num = (int)(2000.0/deltaDist)+1;

		EvenlyDiscretizedFunc distHist = new EvenlyDiscretizedFunc(deltaDist/2, num, deltaDist);
		distHist.setTolerance(5);
		EvenlyDiscretizedFunc target = new EvenlyDiscretizedFunc(0.5, 2000, 1.0);
		EvenlyDiscretizedFunc targetHist = new EvenlyDiscretizedFunc(deltaDist/2, num, deltaDist);
		targetHist.setTolerance(5);
		
		double sum=0;
		for(int i=0;i<10000000;i++) {
			double dist = revisedBlockDistances.get(sampleRandomBlockIndex());
			distHist.add(dist,1.0);
			sum+=1;
		}
		for(int i=0;i<distHist.getNum();i++) distHist.set(i,distHist.getY(i)/sum);
		
		for(int i=0; i<target.getNum();i++) target.set(i,Math.pow(target.getX(i)+minDist, -distDecay));
		double sum2 = target.getSumOfY_Vals();
		for(int i=0; i<target.getNum();i++) target.set(i,target.getY(i)/sum2);
		
		for(int i=0; i<target.getNum();i++) targetHist.add(target.getX(i), target.getY(i));
		
		ArrayList funcs = new ArrayList();
		funcs.add(distHist);
		funcs.add(targetHist);
		GraphiWindowAPI_Impl sr_graph = new GraphiWindowAPI_Impl(funcs, ""); 
		sr_graph.setAxisRange(1, 1200, 1e-6, 1);
		sr_graph.setYLog(true);
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,Color.RED, 2));
		plotChars.add(new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS,Color.BLUE, 2));
		sr_graph.setPlottingFeatures(plotChars);
		
	}
	
	private static double getDecayFractionInsideSphericalVolume(double distDecay, double minDist, double radius) {
		double oneMinus = 1-distDecay;
		return -(Math.pow(radius+minDist,oneMinus) - Math.pow(minDist,oneMinus))/Math.pow(minDist,oneMinus);
	}

	
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		System.out.println(ETAS_PrimaryEventSampler.getDecayFractionInsideSphericalVolume(1.4,2.0, 10));
		double r = Math.pow(25.6*0.75/Math.PI, 0.33333);
		System.out.println(r);
	}

}
