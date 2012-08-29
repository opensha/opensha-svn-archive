package scratch.UCERF3.utils;

import java.awt.Color;
import java.util.ArrayList;

import org.opensha.commons.data.function.AbstractDiscretizedFunc;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.inversion.UCERF2_ComparisonSolutionFetcher;

/**
 * This class essentially represents a magnitude frequency distribution with 
 * non-evenly discretized mags.  This is to ensure that there are no bins with zero
 * grand-inversion ruptures at lower magnitudes (due to our fault discretization into 
 * lengths that are half the down-dip widths).  For reasons described below, only the  
 * first two bins have widths greater than 0.1, and subsequent bins are given a width of 0.1
 * (or just less so that the maximum magnitude is always bin centered).
 * 
 * For the distances between the first three discrete mags, we assume the first mag has
 * an area=2A, where A = subsection area, the second mag has area=3A, and the third mag 
 * has area=4A.  We also assume M is proportional to log(A), which gives a spacing of 0.18
 * between the first and second mag, and a distance of 0.12 between the second and third
 * mag.  Because the third spacing would be 0.1, all spacings after the first two are given 
 * 0.1 (or less, so that magMag is exactly the final mag).
 * 
 * Magnitudes that are exactly on a bin boundary are assigned to the next highest bin 
 * (as with our discretized funtions).
 * 
 * The given minMag is adjusted slightly (up to 0.05) so the lower edge of the first bin
 * (minMag-0.09) is a perfect integer when multiplied by 10 (ensuring a perfect match with 
 * the upper bin-edge for sub-seismogenic ruptures).  This adjustment also influences the higher
 * magnitudes, although the shift becomes less at higher bins and is non-existent at the very
 * last bin (unless there are only two or three bins).  This means...
 * 
 * After the above shift, all mags are bin centered except for the second one (and the third
 * one if the maxMag falls in there).
 * 
 * The bottom line is that final bin rates or moment rates may be biased by up to 20% depending
 * on where the exact rupture mags fall.
 * 
 * Notes:
 * 
 * targetMFD is scaled by 1/delta to make it comparable to what's returned by getMFD() (where the
 * latter is also scaled by 1/delta to make the two comparable in a plot). targetCumMFD is computed
 * before the scaling of targetMFD.
 * 
 * @author field
 *
 */
public class SectionMFD_constraint {
	
	final static boolean D = false; // for debugging
	
	ArrayList<Double> mags = new ArrayList<Double>();		// the bin-center mags (although 2nd one is not perfectly centered)
	double[] rates;											// the rate of events in each bin
	ArrayList<Double> magEdges = new ArrayList<Double>();	// the edges of the bins (this has one more element than mags)
	
	double maxMag, minMag, origMinMag;
	double upperDelta=Double.NaN;
	
	SummedMagFreqDist targetMFD;
	EvenlyDiscretizedFunc targetCumMFD;
	
	// the following assumes first mag has area=2A, where A= subsection area, 
	// second mag has area=3A, and third mag has area=4A, and the spacing also
	// assumes M is proportional to log(A)
	public final static double DIST_BET_FIRST_AND_SECOND_BIN = 0.18;
	public final static double DIST_BET_SECOND_AND_THIRD_BIN = 0.12;
	public final static double MAX_UPPER_DELTA = 0.1;

	
	
//	This is not needed (or not useful without an external way of setting rates):
	
//	/**
//	 * This constructor just creates the bins (sets no rates)
//	 * @param minMag
//	 * @param maxMag
//	 */
//	public SectionMFD_constraint(double minMag, double maxMag) {
//		this.origMinMag=minMag;
//		this.maxMag=maxMag;
//		makeMagBinArrays();
//		if(D)  testMagBinArrays();
//	}
	
	
	/**
	 * This makes the MFD nucleation constraint for the specified subSectIndex from the given
	 * fault system solution (typeically the UCERF2 mapped fault system solution).  The min 
	 * and max magnitudes are determined from the list given by 
	 * fltSysSol.getRupturesForSection(subSectIndex), and then nucleation rates are computed 
	 * for each of these ruptures and the values are added to the rate in each bin here.  No rate
	 * corrections are applied if the mag of a given rupture differs from that at bin center 
	 * (need to think about this more carefully before doing any such thing).
	 * @param fltSysSol
	 * @param subSectIndex
	 */
	public SectionMFD_constraint(FaultSystemSolution fltSysSol, int subSectIndex) {
		origMinMag = 100;
		maxMag = 0;
		ArrayList<Double> rupMags = new ArrayList<Double>();
		ArrayList<Double> rupNuclRates = new ArrayList<Double>();
		
		for (int r : fltSysSol.getRupturesForSection(subSectIndex)) {
			double rate = fltSysSol.getRateForRup(r);
			if(rate>0) {
				rupNuclRates.add(rate*fltSysSol.getAreaForSection(subSectIndex)/fltSysSol.getAreaForRup(r));
				double mag = fltSysSol.getMagForRup(r);
//				System.out.println(rate+"\t"+mag);
				rupMags.add(mag);
				if(origMinMag>mag) 
					origMinMag = mag;
				else if(maxMag<mag) 
					maxMag = mag;
			}
		}
		
		// now make mag bins
		makeMagBinArrays();
		if(D)  testMagBinArrays();
		
		if(D) System.out.println("origMinMag="+origMinMag+"\tmaxMag="+maxMag+"\trupMags.size()="+rupMags.size()+"\trupNuclRates.size()="+rupNuclRates.size());
		
		// now fill in the rates
		rates = new double[mags.size()];
		for(int r=0;r<rupNuclRates.size();r++) {
			double mag = rupMags.get(r);
//			System.out.println(mag+"\t"+rupNuclRates.get(r));
			for(int i=0;i<mags.size();i++) {	// loop over mag bins
				if(isMagInBin(mag, i))	// check if it's in this bin
					rates[i] += rupNuclRates.get(r);
			}
		}
		
		// check to make sure each bin has a non-zero rate
		for(int i=0;i<rates.length;i++) {	// loop over mag bins
			if(rates[i] <=0 )
				throw new RuntimeException("Non-zero rate at bin # "+i+";\tmag="+mags.get(i));
		}
		
		// now make target MFDs
		double targetDelta = 0.01;
		double magRange = magEdges.get(magEdges.size()-1)-magEdges.get(0);	// includes bin widths
		int numPts = (int)Math.round(magRange/targetDelta);
		double delta = magRange/numPts;
		double distMinMag = magEdges.get(0)+delta/2;
		double distMaxMag = magEdges.get(magEdges.size()-1)-delta/2;
		targetMFD = new SummedMagFreqDist(distMinMag, numPts, delta);
		targetMFD.setName("Target MFD");
		targetMFD.setInfo("(mags and rates from fault system solution)");
		for(int r=0;r<rupNuclRates.size();r++) {
			targetMFD.addResampledMagRate(rupMags.get(r), rupNuclRates.get(r), true);
		}

		// get cumulative dist
		targetCumMFD = targetMFD.getCumRateDistWithOffset();
		targetCumMFD.setName("Cumulative Target MFD");
		targetCumMFD.setInfo("(mags and cumulative rates from fault system solution)");


		// scale incremental target to be a density distribution (so it can be compared with the other)
		targetMFD.scale(1.0/targetMFD.getDelta());

	}

	

	/**
	 * This constructor sets the rates for a GR plus characteristic MFD (the latter being
	 * a spike at the maximum magnitude: mags.get(mags.size()-1). For a UCERF2 type-B-fault-
	 * like constraint, set fractGR=0.333.
	 * 
	 * Note that unlike UCERF2, b=1 (since aftershocks are included), there is no b=0 option 
	 * included (since the grand inversion takes care of this), and no aleatory variability
	 * is applied to the characteristic part (because this should be added later as in UCERF2).
	 * 
	 * @param minMag - the smallest fault-system magnitude that can nucleate on the section 
	 * @param maxMag - the largest fault-system magnitude that can nucleate on the section
	 * @param totMoRate
	 * @param fractGR - fraction moment rate put into GR (vs Char) MFD
	 */
	public SectionMFD_constraint(double minMag, double maxMag, double totMoRate, double fractGR) {
		this.origMinMag=minMag;
		this.maxMag=maxMag;
		makeMagBinArrays();
		if(D)  testMagBinArrays();

		double targetDelta = 0.01;
		double magRange = magEdges.get(magEdges.size()-1)-magEdges.get(0);	// includes bin widths
		int numPts = (int)Math.round(magRange/targetDelta);
		double delta = magRange/numPts;
		double distMinMag = magEdges.get(0)+delta/2;
		double distMaxMag = magEdges.get(magEdges.size()-1)-delta/2;
		rates = new double[mags.size()];


		// now make total target MFD
		targetMFD = new SummedMagFreqDist(distMinMag, numPts, delta);
		targetMFD.setName("Target MFD");
		targetMFD.setInfo(" ");
		
		double totGRdistRate=0;

		if(fractGR>0) {

			double bValue = 1;
			double grMoment = totMoRate*fractGR;
			if(D)
				System.out.println("grMinMag="+(float)distMinMag+"\tgrMaxMag="+(float)distMaxMag+"\tnumPts="+numPts+"\tdelta="+(float)delta+"\tgrMoment="+(float)grMoment+"\tbValue="+bValue);
			GutenbergRichterMagFreqDist grDist = new GutenbergRichterMagFreqDist(distMinMag, numPts, delta, grMoment, bValue);
			testFractDifference(grDist.getMagUpper(), distMaxMag, "test of grMaxMag");
			//		if(D) System.out.println(grDist);

			EvenlyDiscretizedFunc cumDist = grDist.getCumRateDistWithOffset();
			//		if(D) System.out.println(cumDist);

			for(int i=0; i<mags.size(); i++) {
				double rate1 = cumDist.getInterpolatedY_inLogYDomain(magEdges.get(i));
				double rate2;
				if(i==mags.size()-1)	// it's the last point
					rate2 =0;
				else
					rate2 = cumDist.getInterpolatedY_inLogYDomain(magEdges.get(i+1));
				rates[i] = rate1-rate2;
			}

			targetMFD.addIncrementalMagFreqDist(grDist);

			totGRdistRate = grDist.getTotalIncrRate();
			if(D) {
				double totRate=0;
				for(double rate:rates) totRate += rate;
				testFractDifference(totRate, grDist.getTotalIncrRate(), "testing GR rates");
				System.out.println("totRate="+totRate+"\tgrDist.getTotalIncrRate()="+totGRdistRate);
			}

		}

		if(fractGR <1) {

			// now add the Characteristic rates
			double charMag = mags.get(mags.size()-1);
			double charMoRate = totMoRate*(1-fractGR);
			double charRateAtMaxMag = charMoRate/MagUtils.magToMoment(charMag);
			if(D) System.out.println("charMag="+charMag+"\tcharMoRate="+(float)charMoRate+"\tcharRateAtMaxMag="+(float)charRateAtMaxMag);
			rates[rates.length-1] += charRateAtMaxMag;

			targetMFD.addResampledMagRate(charMag, charRateAtMaxMag, true);

			if(D) {
				double totRate=0;
				for(double rate:rates) totRate += rate;
				double testTotalRate=totGRdistRate+charRateAtMaxMag;
				testFractDifference(totRate, testTotalRate, "testing total rates");
				System.out.println("totRate="+(float)totRate+"\ttestTotalRate="+(float)testTotalRate);
			}

		}

		// get cumulative dist
		targetCumMFD = targetMFD.getCumRateDistWithOffset();
		targetCumMFD.setName("Cumulative Target MFD");

		// scale incremental target to be a density distribution (so it can be compared with the other)
		targetMFD.scale(1.0/targetMFD.getDelta());

		//		// this test is only valid if there is only one bin
		//		double singleMagRate = grMoment/MagUtils.magToMoment(origMinMag);
		//		if(D) System.out.println("singleMagRate="+singleMagRate+"\tfractDiff="+(singleMagRate-totRate)/totRate);

		//		EvenlyDiscretizedFunc moDist = grDist.getMomentRateDist();
		//		double sumMo=0;
		//		for(int i=0;i<moDist.getNum();i++) {
		//			sumMo += moDist.getY(i);
		//			System.out.println((float)moDist.getX(i)+"\t"+(float)sumMo);
		//		}

	}

	
	/**
	 * This tests various attributes of the bins (throwing exceptions for failures)
	 */
	private void testMagBinArrays() {

		System.out.println("minMag="+minMag+"\tmaxMag="+maxMag);

		System.out.println("numMags="+mags.size()+"\tnumMagEdges="+magEdges.size()+"\n");

		// print out data
		System.out.println("edge\tmag\tdMag\tdBin\tdiffBetMagAndBinCenter");
		for(int i=0; i<magEdges.size()-1;i++) {
			if(i==0)
				System.out.println((float)(double)magEdges.get(i));
			else
				System.out.println((float)(double)magEdges.get(i)+"\t\t"+(float)(mags.get(i)-mags.get(i-1)));
			double magDiff = mags.get(i) - (magEdges.get(i+1)+magEdges.get(i))/2;
			System.out.println("\t"+(float)(double)mags.get(i)+"\t\t"+(float)(magEdges.get(i+1)-magEdges.get(i))+
					"\t"+(float)magDiff);		
		}
		System.out.println((float)(double)magEdges.get(magEdges.size()-1));


		// test spacing between mags
		if(mags.size()>1)
			testFractDifference(mags.get(1)-mags.get(0), DIST_BET_FIRST_AND_SECOND_BIN, "test dist between mag indices: 0 & 1");
		if(mags.size()>2)
			testFractDifference(mags.get(2)-mags.get(1), DIST_BET_SECOND_AND_THIRD_BIN, "test dist between mag indices: 1 & 2");
		for(int i=3;i<mags.size(); i++)
			testFractDifference(mags.get(i)-mags.get(i-1), upperDelta, "test dist between mag indices: "+i+" & "+(i+1));

		// test the first edge of first bin
		double firstEdge=magEdges.get(0);
		double roundTest = ((double)Math.round(firstEdge*10.0))/10.0;
		testFractDifference(roundTest,firstEdge, "test of rounding of the first bin edge");  // this makes sure it's rounded properly
		// range of values already tested in getLowerEdgeOfFirstBin(double minMag)

		// the rest of the bin edges should all be half way between mags (except for last)
		for(int i=0;i<mags.size()-1; i++)
			testFractDifference((mags.get(i)+mags.get(i+1))/2, magEdges.get(i+1), "test mag bin edge for index: "+
					(i+1)+"\t"+mags.get(i)+"\t"+mags.get(i+1));
		// test the last edge
		if(mags.size()>2)	// last bin edge defined by upperDelta/2
			testFractDifference(mags.get(mags.size()-1)+upperDelta/2, magEdges.get(magEdges.size()-1), "test mag bin edge for index: "+(magEdges.size()-1));	
		else if(mags.size()==2)	// last bin edge defined by DIST_BET_SECOND_AND_THIRD_BIN/2
			testFractDifference(mags.get(mags.size()-1)+DIST_BET_SECOND_AND_THIRD_BIN/2, magEdges.get(magEdges.size()-1), "test mag bin edge for index: "+(magEdges.size()-1));	
	}
	
	
	/**
	 * This constructs the mags and binEdges
	 */
	private void makeMagBinArrays() {
		if(maxMag<origMinMag)
			throw new RuntimeException("minMag must be less than maxMag)");
		
		// set first bin value and the two edges
		int currentBin=0;
		
		// set first edge of first bin and then adjust origMinMag to minMag
		double firstEdge = getLowerEdgeOfFirstBin(origMinMag);
		minMag = firstEdge+DIST_BET_FIRST_AND_SECOND_BIN/2;
		// seems to need rounding to look nice
		minMag = (double)(Math.round(minMag*100.0))/100.0;
		
		if(D) System.out.println("origMinMag="+(float)origMinMag+"\t"+"minMag="+(float)minMag+"\tdiff="+(float)(minMag-origMinMag));
				
		mags.add(minMag);	// first bin value
		magEdges.add(firstEdge);
		magEdges.add(minMag+DIST_BET_FIRST_AND_SECOND_BIN/2);
		
		// proceed if maxMag is not in this first bin
		if(!isMagInBin(maxMag, currentBin)) {
			currentBin = 1;
			mags.add(mags.get(0)+DIST_BET_FIRST_AND_SECOND_BIN);
			magEdges.add(mags.get(1)+DIST_BET_SECOND_AND_THIRD_BIN/2);
			if(!isMagInBin(maxMag, currentBin)) {
				currentBin = 2;
				mags.add(mags.get(1)+DIST_BET_SECOND_AND_THIRD_BIN);
				
				double nextEdgeTest = mags.get(2)+MAX_UPPER_DELTA/2;
				if(maxMag<nextEdgeTest) {
					magEdges.add(nextEdgeTest);	// done here at 3 mag bins
					// verify
					if(!isMagInBin(maxMag, currentBin))
						throw new RuntimeException("Problem");
				}
				else {		// delta from here is somewhere between 0.05 (if maxMag just greater than nextEdge) and 0.1
					double magRange = maxMag-mags.get(2);
					upperDelta = magRange/Math.ceil(magRange/MAX_UPPER_DELTA);
				
					if(D) System.out.println("delta="+upperDelta);

					magEdges.add(mags.get(2)+upperDelta/2);
				
					// now do the rest
					while(!isMagInBin(maxMag, currentBin)) {
						currentBin += 1;
						mags.add(mags.get(currentBin-1)+upperDelta);
						magEdges.add(mags.get(currentBin)+upperDelta/2);
					}
				}
			}
		}
	}
	
	/**
	 * This tells whether the given mag is in the ith bin;
	 * true if:  magEdges.get(ithBin)<=mag<magEdges.get(ithBin+1)
	 * @param mag
	 * @param ithBin
	 * @return
	 */
	private boolean isMagInBin(double mag, int ithBin) {
		if(mag<magEdges.get(ithBin+1) && magEdges.get(ithBin)<=mag)
			return true;
		else
			return false;
	}
	
	
	/**
	 * This throws a runtime exception if the absolute value of the fractional difference
	 * greater that 1e-6
	 * @param val1
	 * @param val2
	 */
	private void testFractDifference(double val1, double val2, String infoString) {
		double diff = Math.abs((val1-val2)/val1);
		if(diff>1e-6)
			throw new RuntimeException("Problem with "+infoString+"\tval1="+val1+"\tval2="+val2+"\tfrDiff="+diff);
		
	}
	
	/**
	 * This finds the edge of the first bin and also rounds it to be exactly an 
	 * integer when multiplied by 10 (so it has the same edge as the background 
	 * seismsicity)
	 * @param orgMinMag
	 * @return
	 */
	private static double getLowerEdgeOfFirstBin(double origMinMag) {
		double edgeMag = origMinMag-DIST_BET_FIRST_AND_SECOND_BIN/2; 
		return (double)Math.floor(edgeMag*10.0)/10.0;
	}
	
	
	/**
	 * This returns the target MFD, and note that this has been scaled by
	 * 1/delta to make it comparable to what's returned by getMFD().
	 * @return
	 */
	public IncrementalMagFreqDist getTargetMFD() {
		return targetMFD;
	}
	
	
	/**
	 * This returns the target cumulative MFD
	 * @return
	 */
	public EvenlyDiscretizedFunc getTargetCumMFD() {
		return targetCumMFD;
	}
	
	
	/**
	 * This returns the cumulative MFD (the rate of events above
	 * the lower edge of each bin, where the latter are the x-axis
	 * values here).
	 */
	public ArbitrarilyDiscretizedFunc getCumMFD() {
		double[] cumRates = new double[rates.length];
		cumRates[cumRates.length-1] = rates[rates.length-1];	// setting the last one
		for(int i=rates.length-2; i>=0; i--)
			cumRates[i] = rates[i] + cumRates[i+1];
		
		ArbitrarilyDiscretizedFunc cumMFD = new ArbitrarilyDiscretizedFunc("Cumulative MFD Constraint (red circles)");
		for(int i=0; i<rates.length;i++)
			cumMFD.set(magEdges.get(i), cumRates[i]);
		
		return cumMFD;
	}
	
	
	/**
	 * This returns a representation of the MFD that plots well
	 * (by splitting internal bin edges to make each bin and actual
	 * boxcar rather than straight lines between bin centers).  This
	 * also scales the rates by 1/getBinWidth(i) to make it comparable
	 * to what's returned by getTargetMFD().
	 */
	public ArbitrarilyDiscretizedFunc getMFD() {
		
		// scale rates to make it a density distribution
		double[] scaledRates = new double[rates.length];
		for(int i=0; i<rates.length;i++) scaledRates[i] = rates[i]/getBinWidth(i);
		
		ArbitrarilyDiscretizedFunc mfd = new ArbitrarilyDiscretizedFunc("MFD Constraint");
		mfd.setInfo("(origMinMag="+origMinMag+"\tminMag="+minMag+"\tmaxMag="+maxMag+")");
		mfd.set(magEdges.get(0), scaledRates[0]);
		for(int i=0; i<rates.length;i++) {
			double mag = magEdges.get(i+1);
			if(i == rates.length-1) {	// if it's the last point
				mfd.set(mag, scaledRates[i]);
			}
			else {
				mfd.set(mag-0.001, scaledRates[i]);
				mfd.set(mag+0.001, scaledRates[i+1]);
			}
		}
		
		return mfd;
	}
	
	
	
	private double getBinWidth(int ithBin) {
		return magEdges.get(ithBin+1)-magEdges.get(ithBin);
	}

	
	public void plotMFDs() {
		ArrayList<AbstractDiscretizedFunc> funcs = new ArrayList<AbstractDiscretizedFunc>();
		funcs.add(getCumMFD());
		funcs.add(getMFD());
		funcs.add(getTargetCumMFD());
		funcs.add(getTargetMFD());
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2, PlotSymbol.CIRCLE,4, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2, Color.RED));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(PlotLineType.SOLID, 2, Color.BLUE));
		
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Section Constraint MFDs", plotChars); 
		graph.setX_AxisLabel("Mangitude");
		graph.setY_AxisLabel("Rate (per year)");
		graph.setYLog(true);

	}

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		SimpleFaultSystemSolution testFltSysSol = UCERF2_ComparisonSolutionFetcher.getUCERF2Solution(FaultModels.FM2_1);
		
		// list the parent names
//		ArrayList<String> parSectNames = new ArrayList<String>();
//		for(int s=0; s<testFltSysSol.getNumSections();s++) {
//			String name = testFltSysSol.getFaultSectionData(s).getParentSectionName();
//			if(!parSectNames.contains(name))
//				parSectNames.add(name);
//		}
//		for(String name:parSectNames) {
//			System.out.println(name);
//		}
		
		// list the subsection for the target parent
//		String targetParName = 	"San Andreas (Mojave S)";
//		for(int s=0; s<testFltSysSol.getNumSections();s++) {
//			if(testFltSysSol.getFaultSectionData(s).getParentSectionName().equals(targetParName))
//				System.out.println(s+"\t"+testFltSysSol.getFaultSectionData(s).getSectionId()+
//						"\t"+testFltSysSol.getFaultSectionData(s).getSectionName());
//		}
		
		int sectIndex = 1159; // "San Andreas (Mojave S), Subsection 0"
		SectionMFD_constraint test = new SectionMFD_constraint(testFltSysSol,1159);
		test.plotMFDs();
		
		
		
//		SectionMFD_constraint test = new SectionMFD_constraint(6.14, 7, 1e18, 1);
//		test.plotMFDs();
		
		
//		double minX=6.05;
//		double delta=0.1;
//		double[] testVals = {6,6.1,6.2,6.3,6.4,6.5,6.6,6.7,6.8,6.9,7};
//		for(double x:testVals) {
//			int index = (int)Math.round((x-minX)/delta);
//			double binCenter = minX+delta*index;
//			System.out.println(x+"\tindex = "+index+"\tbinCenter="+binCenter);
//		}
		
//		HistogramFunction hist = new HistogramFunction(6.05, 10, 0.1);
//		System.out.println(hist.getXIndex(6));
//		System.out.println(hist.getXIndex(6.1));
//		System.out.println(hist.getXIndex(6.2));
//		System.out.println(hist.getXIndex(6.3));
//		System.out.println(hist.getXIndex(6.4));
//		System.out.println(hist.getXIndex(6.5));
//		System.out.println(hist.getXIndex(6.6));
//		System.out.println(hist.getXIndex(6.7));
//		System.out.println(hist.getXIndex(6.8));
//		System.out.println(hist.getXIndex(6.9));
//		System.out.println(hist.getXIndex(7));
//
//		
//		EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(6.05, 10, 0.1);
//		System.out.println(func);
//		System.out.println(func.getClosestXIndex(6));
//		System.out.println(func.getClosestXIndex(6.1));
//		System.out.println(func.getClosestXIndex(6.2));
//		System.out.println(func.getClosestXIndex(6.3));
//		System.out.println(func.getClosestXIndex(6.4));
//		System.out.println(func.getClosestXIndex(6.5));
//		System.out.println(func.getClosestXIndex(6.6));
//		System.out.println(func.getClosestXIndex(6.7));
//		System.out.println(func.getClosestXIndex(6.8));
//		System.out.println(func.getClosestXIndex(6.9));
//		System.out.println(func.getClosestXIndex(7));
		
		

//		double[] testMags = {6.38, 6.33, 6.3, 6.35, 6.350000001, 6.3499999};
//		for(double testMag:testMags) {
//			double edgeMag = getLowerEdgeOfFirstBin(testMag);
//			System.out.println(testMag+"\t"+edgeMag+"\t"+(float)(testMag-edgeMag)+"\t");
//		}
	}

}
