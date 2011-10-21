package scratch.UCERF3.inversion;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.EvenlyDiscretizedFunc;
import org.opensha.commons.geo.Region;
import org.opensha.commons.geo.RegionUtils;
import org.opensha.commons.gui.plot.PlotLineType;
import org.opensha.commons.gui.plot.PlotSymbol;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.SegRateConstraint;
import org.opensha.sha.faultSurface.StirlingGriddedSurface;
import org.opensha.sha.gui.infoTools.GraphiWindowAPI_Impl;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.simulatedAnnealing.SerialSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.SimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.ThreadedSimulatedAnnealing;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.MatrixIO;
import cern.colt.list.tdouble.DoubleArrayList;
import cern.colt.list.tint.IntArrayList;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;

/**
 * This class represents the UCERF3 "Grand Inversion".
 * 
 * To Do:
 * 
 * a) Add the following methods from the old version (RupsInFaultSystemInversion), which Morgan
 *    used with Matlab (these needed??):
 * 
 * 		writeInfoToFiles(ArrayList<ArrayList<Integer>> rupList) - Morgan used in Matlab
 * 		loadMatLabInversionSolution()
 * 		writeInversionIngredientsToFiles(DoubleMatrix2D, double[], double[])
 *
 * b) Make the MFD constraints an inequality constraint?
 * 
 * c) Implement the multiple mfdConstraints and apply them to the associated regions
 * 
 * 
 * @author Field, Page, Milner, & Powers
 *
 */
public class InversionFaultSystemSolution extends SimpleFaultSystemSolution {
	
	protected final static boolean D = true;  // for debugging
	
	FaultSystemRupSet faultSystemRupSet;
	boolean weightSlipRates;
	double relativeSegRateWt, relativeMagDistWt, relativeRupRateConstraintWt, relativeMinimizationConstraintWt;
	int numIterations;
	ArrayList<SegRateConstraint> segRateConstraints;
	
	double[] aPrioriRupConstraint;
	double[] minimizationConstraint;
	double[] initialRupModel;
		
	ArrayList<MFD_InversionConstraint> mfdConstraints;
	
	DoubleMatrix2D A; // A matrix for inversion
	double[] d;	// data vector d for inversion

	/**
	 * Constructor.
	 * 
	 * @param faultSystemRupSet
	 * @param relativeSegRateWt
	 * @param relativeMagDistWt
	 * @param relativeRupRateConstraintWt
	 * @param relativeMinimizationConstraintWt
	 * @param numIterations
	 * @param segRateConstraints
	 * @param aPrioriRupConstraint
	 * @param initialRupModel
	 * @param mfdConstraints
	 * @param minimizationConstraint
	 */
	public InversionFaultSystemSolution(FaultSystemRupSet faultSystemRupSet, boolean weightSlipRates, double relativeSegRateWt, 
			double relativeMagDistWt, double relativeRupRateConstraintWt, double relativeMinimizationConstraintWt, int numIterations,
			ArrayList<SegRateConstraint> segRateConstraints, double[] aPrioriRupConstraint,
			double[] initialRupModel, ArrayList<MFD_InversionConstraint> mfdConstraints, double[] minimizationConstraint) {
		super(faultSystemRupSet, null);
		this.faultSystemRupSet=faultSystemRupSet;
		this.weightSlipRates=weightSlipRates;
		this.relativeSegRateWt=relativeSegRateWt;
		this.relativeMagDistWt=relativeMagDistWt;
		this.relativeRupRateConstraintWt=relativeRupRateConstraintWt;
		this.relativeMinimizationConstraintWt=relativeMinimizationConstraintWt;
		this.numIterations=numIterations;
		this.segRateConstraints=segRateConstraints;
		this.aPrioriRupConstraint=aPrioriRupConstraint;
		this.initialRupModel=initialRupModel;
		this.mfdConstraints=mfdConstraints;
		this.minimizationConstraint=minimizationConstraint;
		
		doInversion();
	}
	
	
	
	
	private void doInversion() {
		
		int numSections = getNumSections();
		int numRuptures = getNumRuptures();
		double[] sectSlipRateReduced = faultSystemRupSet.getSlipRateForAllSections();
		double[] sectSlipRateStdDevReduced = faultSystemRupSet.getSlipRateStdDevForAllSections();
		double[] rupMeanMag = faultSystemRupSet.getMagForAllRups();
		
		// Compute number of slip-rate constraints
		int numSlipRateConstraints = 0;
		for(int i=0; i<sectSlipRateReduced.length;i++)
			if(!Double.isNaN(sectSlipRateReduced[i]))
				numSlipRateConstraints+=1;
		
		// Find number of rows in A matrix (equals the total number of constraints)
		if(D) System.out.println("\nNumber of slip-rate constraints:    " + numSlipRateConstraints);
		if(D) System.out.println("Number of segment-rate constraints: " + (int)Math.signum(relativeSegRateWt)*segRateConstraints.size());
		if(D) System.out.println("Number of rupture-rate constraints: " + (int)Math.signum(relativeRupRateConstraintWt)*numRuptures);
		if(D) System.out.println("Number of minimization constraints: " + (int)Math.signum(relativeMinimizationConstraintWt)*numRuptures);
		int numRows = numSlipRateConstraints + (int)Math.signum(relativeSegRateWt)*segRateConstraints.size() + 
				(int)Math.signum(relativeRupRateConstraintWt)*numRuptures + (int)Math.signum(relativeMinimizationConstraintWt)*numRuptures;  // number of rows used for slip-rate and paleo-rate constraints
		IncrementalMagFreqDist targetMagFreqDist=null;
		if (relativeMagDistWt > 0.0) {
			// RIGHT NOW THIS ASSUMES THE MAGNITUDE CONSTRAINT WEIGHT MUST BE THE SAME FOR ALL MAG-DIST CONSTRAINTS
			int totalNumMagFreqConstraints = 0;
			for (int i=0; i < mfdConstraints.size(); i++) {
				targetMagFreqDist=mfdConstraints.get(i).getMagFreqDist();
				totalNumMagFreqConstraints += targetMagFreqDist.getNum();
				numRows=numRows+targetMagFreqDist.getNum(); // add number of rows used for magnitude distribution constraint
			}
			if(D) System.out.println("Number of magnitude-distribution constraints: " + totalNumMagFreqConstraints);
		}
		
		
		// Components of matrix equation to invert (Ax=d)
		A = new SparseDoubleMatrix2D(numRows,numRuptures); // A matrix
//		A = new SparseRCDoubleMatrix2D(numRows,numRuptures);
		d = new double[numRows];	// data vector d
		rupRateSolution = new double[numRuptures]; // final solution (x of Ax=d)
		
		if(D) System.out.println("Total number of constraints (rows): " + numRows);
		if(D) System.out.println("\nNumber of fault sections: " + numSections + ". Number of ruptures (columns): " + numRuptures + ".");
		
		
		// Put together "A" Matrix and data vector
		
		// NEED TO REVISE NEXT SECTION TO HANDLE CASE WHERE SOME SLIP RATES ARE NaN *******************
		// Make sparse matrix of slip in each rupture & data vector of section slip rates
		int numNonZeroElements = 0;  
		if(D) System.out.println("\nAdding slip per rup to A matrix ...");
		long startTime = System.currentTimeMillis();
		for (int rup=0; rup<numRuptures; rup++) {
			double[] slips = getSlipOnSectionsForRup(rup);
			List<Integer> sects = getSectionsIndicesForRup(rup);
			for (int i=0; i < slips.length; i++) {
				int row = sects.get(i);
				int col = rup;
				if (weightSlipRates == false) {
					A.set(row, col, slips[i]);
				} else A.set(row, col, slips[i]/sectSlipRateReduced[row]); // Normalize by slip rate
				if(D) numNonZeroElements++;
			}
		}
		for (int sect=0; sect<numSlipRateConstraints; sect++) {
			if (weightSlipRates == false) {
				d[sect] = sectSlipRateReduced[sect];	
			} else d[sect] = 1; // Normalize by slip rate
		}
		long runTime = System.currentTimeMillis()-startTime;
		if(D) System.out.println("Adding Slip-Rate Constraints took " + (runTime/1000.) + " seconds.");
		if(D) System.out.println("Number of nonzero elements in A matrix = "+numNonZeroElements);
		
		
		// Make sparse matrix of paleo event probs for each rupture & data vector of mean event rates
		if (relativeSegRateWt > 0.0) {
			numNonZeroElements = 0;
			startTime = System.currentTimeMillis();
			if(D) System.out.println("\nAdding event rates to A matrix ...");
			for (int i=numSlipRateConstraints; i<numSlipRateConstraints+segRateConstraints.size(); i++) {
				SegRateConstraint constraint = segRateConstraints.get(i-numSlipRateConstraints);
				d[i]=relativeSegRateWt * constraint.getMean() / constraint.getStdDevOfMean();
				for (int rup=0; rup<numRuptures; rup++) {
					double getVal = A.get(constraint.getSegIndex(), rup);
					if (getVal>0) {
						double setVal = (relativeSegRateWt * getProbPaleoVisible(rupMeanMag[rup]) / constraint.getStdDevOfMean());
						A.set(i, rup, setVal);
						if(D) numNonZeroElements++;			
					}
				}
			}
			runTime = System.currentTimeMillis()-startTime;
			if(D) System.out.println("Adding Segment-Rate Constraints took " + (runTime/1000.) + " seconds.");
			if(D) System.out.println("Number of new nonzero elements in A matrix = "+numNonZeroElements);
		}

			
		// Constrain Solution MFD to equal the Target MFD 
		// CHANGE THIS TO AN INEQUALITY CONSTRAINT? *****************************
		int rowIndex = numSlipRateConstraints + (int)Math.signum(relativeSegRateWt)*segRateConstraints.size(); // number of rows used for slip-rate and paleo-rate constraints
		if (relativeMagDistWt > 0.0) {	
			startTime = System.currentTimeMillis();
			numNonZeroElements = 0;
			if(D) System.out.println("\nAdding " + mfdConstraints.size()+ " magnitude constraints to A matrix ...");	
			// first make matrix of the fraction of each section inside each region
			// (this could be faster if we could assume non-overlapping regions)
			double[][] fractSectInsideRegion = new double[mfdConstraints.size()][getNumSections()];
			int[] numPtsInSection = new int[getNumSections()];
			double gridSpacing=1; // km; this will be faster if this is increased, or if we used the section trace rather than the whole surface
			boolean aseisReducesArea = true;
			for(int r=0;r<mfdConstraints.size(); r++) {
				Region region = mfdConstraints.get(r).getRegion();
				for(int s=0;s<getNumSections(); s++) {
					StirlingGriddedSurface surf = new StirlingGriddedSurface(getFaultSectionData(s).getSimpleFaultData(aseisReducesArea), gridSpacing);
					fractSectInsideRegion[r][s] = RegionUtils.getFractionInside(region, surf.getLocationList());
					if(r==0) // set only once
						numPtsInSection[s] = surf.getNumCols()*surf.getNumRows();
				}
			}
			for (int i=0; i < mfdConstraints.size(); i++) {  // Loop over all MFD constraints in different regions
				targetMagFreqDist=mfdConstraints.get(i).getMagFreqDist();	
				for(int rup=0; rup<numRuptures; rup++) {
					double mag = rupMeanMag[rup];
					List<Integer> sectionsIndicesForRup = getSectionsIndicesForRup(rup);
					double fractionRupInRegion=0;
					int totNumPts = 0;
					for(Integer s:sectionsIndicesForRup) {
						fractionRupInRegion += fractSectInsideRegion[i][s]*numPtsInSection[s];
						totNumPts += numPtsInSection[s];
					}
					fractionRupInRegion /= totNumPts;
					if (fractionRupInRegion > 0) {
						A.set(rowIndex+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagDistWt * fractionRupInRegion);
						numNonZeroElements++;
					}
				}		
				for (double m=targetMagFreqDist.getMinX(); m<=targetMagFreqDist.getMaxX(); m=m+targetMagFreqDist.getDelta()) {
					d[rowIndex]=targetMagFreqDist.getY(m)*relativeMagDistWt;
					rowIndex++; 
				}	
			}
			runTime = System.currentTimeMillis()-startTime;
			if(D) System.out.println("Adding MFD Constraints took " + (runTime/1000.) + " seconds.");
			if(D) System.out.println("Number of new nonzero elements in A matrix = "+numNonZeroElements);
		}
		
		
		// Constrain Rupture Rate Solution to approximately equal aPrioriRupConstraint
		if (relativeRupRateConstraintWt > 0.0) {
			startTime = System.currentTimeMillis();
			if(D) System.out.println("\nAdding rupture-rate constraint to A matrix ...");
			numNonZeroElements = 0;
			for(int rup=0; rup<numRuptures; rup++) {
				A.set(rowIndex,rup,relativeRupRateConstraintWt);
				d[rowIndex]=aPrioriRupConstraint[rup]*relativeRupRateConstraintWt;
				numNonZeroElements++; rowIndex++;
			}
			runTime = System.currentTimeMillis()-startTime;
			if(D) System.out.println("Adding rupture-rate Constraints took " + (runTime/1000.) + " seconds.");
			if(D) System.out.println("Number of new nonzero elements in A matrix = "+numNonZeroElements);
		}
		
		
		// Penalize Ruptures with small Coulomb weights
		if (relativeMinimizationConstraintWt > 0.0) {
			startTime = System.currentTimeMillis();
			if(D) System.out.println("\nAdding minimization constraints to A matrix ...");
			numNonZeroElements = 0;
			for(int rup=0; rup<numRuptures; rup++) {
				A.set(rowIndex,rup,relativeMinimizationConstraintWt*minimizationConstraint[rup]);
				d[rowIndex]=0;
				numNonZeroElements++; rowIndex++;
			}
			runTime = System.currentTimeMillis()-startTime;
			if(D) System.out.println("Adding Minimization Constraints took " + (runTime/1000.) + " seconds.");
			if(D) System.out.println("Number of new nonzero elements in A matrix = "+numNonZeroElements);
		}
		
		
		
		
		// OPTIONAL: Write out A and d to binary files (to give to Kevin to run on cluster)
		try {
			MatrixIO.doubleArrayToFile(d,new File("dev/scratch/UCERF3/preComputedData/d.bin"));
			MatrixIO.saveSparse(A,new File("dev/scratch/UCERF3/preComputedData/A.bin"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	

		
		// Transform A matrix to different type that's fast for multiplication
		if (numIterations > 0 && A instanceof SparseDoubleMatrix2D) {
			// if we're annealing, and A is in the SparseDoubleMatrix2D format then
			// we should convert it for faster matrix multiplications
			if (D) System.out.println("\nConverting A matrix to SparseRCDoubleMatrix2D ...");
			startTime = System.currentTimeMillis();
			SparseRCDoubleMatrix2D Anew = new SparseRCDoubleMatrix2D(A.rows(), A.columns());
			IntArrayList rows = new IntArrayList();
			IntArrayList cols = new IntArrayList();
			DoubleArrayList vals = new DoubleArrayList();
			long startTime1 = System.currentTimeMillis();
			A.getNonZeros(rows, cols, vals);
			if (D) System.out.println("Non-zero entries stored after " + ((System.currentTimeMillis()-startTime1)/1000.) + " seconds.\n");
			startTime1 = System.currentTimeMillis();
			for (int i=0; i<rows.size(); i++)
				Anew.set(rows.get(i), cols.get(i), vals.get(i));
			if (D) System.out.println("Anew formed after " + ((System.currentTimeMillis()-startTime1)/1000.) + " seconds.\n");
			startTime1 = System.currentTimeMillis();
			A = Anew;
			if (D) System.out.println("New A matrix formed after " + ((System.currentTimeMillis()-startTime1)/1000.) + " seconds.\n");
			if (D) System.out.println("Done after " + ((System.currentTimeMillis()-startTime)/1000.) + " seconds.\n");
		}
		
		// SOLVE THE INVERSE PROBLEM
		// Run Simulated Annealing
		SimulatedAnnealing sa = new SerialSimulatedAnnealing(A, d, initialRupModel);
		sa.iterate(numIterations);
		rupRateSolution = sa.getBestSolution();
		
		
//		ThreadedSimulatedAnnealing tsa = new ThreadedSimulatedAnnealing(A, d, initialRupModel, 4);
//		try {
//			rupRateSolution = tsa.getSolution(numIterations, 2500);
//		} catch (InterruptedException e) {
//			throw new RuntimeException(e);
//		}
//		
//		rupRateSolution = SimulatedAnnealing.getSolution(A,d,initialRupModel, numIterations);    		
		
	}
	
//	public void plotStuff(ArrayList<ArrayList<Integer>> rupList, DoubleMatrix2D A, double[] d, double[] rupRateSolution, double relativeMagDistWt, FindEquivUCERF2_Ruptures findUCERF2_Rups) {
	/**

	 * THIS ASSUMES THAT mfdConstraints ONLY HAS ONE CONSTRAINT THAT APPLIES TO THE ENTIRE REGION
	 */
	public void plotStuff() {
		
		int numSections = faultSystemRupSet.getNumSections();
		int numRuptures = faultSystemRupSet.getNumRuptures();
		double[] rupMeanMag = faultSystemRupSet.getMagForAllRups();
		
		List<FaultSectionPrefData> faultSectionData = faultSystemRupSet.getFaultSectionDataList();
		
		
		// Plot the rupture rates
		if(D) System.out.println("Making plot of rupture rates . . . ");
		ArrayList funcs = new ArrayList();		
		EvenlyDiscretizedFunc ruprates = new EvenlyDiscretizedFunc(0,(double)rupRateSolution.length-1,rupRateSolution.length);
		for(int i=0; i<rupRateSolution.length; i++)
			ruprates.set(i,rupRateSolution[i]);
		funcs.add(ruprates); 	
		GraphiWindowAPI_Impl graph = new GraphiWindowAPI_Impl(funcs, "Inverted Rupture Rates"); 
		graph.setX_AxisLabel("Rupture Index");
		graph.setY_AxisLabel("Rate");
		
		
		// Plot the slip rate data vs. synthetics
		if(D) System.out.println("Making plot of slip rate misfit . . . ");
		ArrayList funcs2 = new ArrayList();		
		EvenlyDiscretizedFunc syn = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		EvenlyDiscretizedFunc data = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		for (int i=0; i<numSections; i++) {
			data.set(i, faultSystemRupSet.getSlipRateForSection(i));
			syn.set(i,0);
		}
		
		for (int rup=0; rup<numRuptures; rup++) {
			double[] slips = getSlipOnSectionsForRup(rup);
			List<Integer> sects = getSectionsIndicesForRup(rup);
			for (int i=0; i < slips.length; i++) {
				int row = sects.get(i);
				syn.add(row,slips[i]*rupRateSolution[rup]);
			}
		}
		for (int i=0; i<numSections; i++) data.set(i, faultSystemRupSet.getSlipRateForSection(i));
		
		funcs2.add(syn);
		funcs2.add(data);
		GraphiWindowAPI_Impl graph2 = new GraphiWindowAPI_Impl(funcs2, "Slip Rate Synthetics (blue) & Data (black)"); 
		graph2.setX_AxisLabel("Fault Section Index");
		graph2.setY_AxisLabel("Slip Rate");
		
		
		// Plot the slip rate data vs. synthetics - Averaged over parent sections
//		System.out.println("\n\nratioSR\tsynSR\tdataSR\tparentSectName");
		String info = "index\tratio\tpredSR\tdataSR\tParentSectionName\n";
		String parentSectName = "";
		double aveData=0, aveSyn=0, numSubSect=0;
		ArrayList<Double> aveDataList = new ArrayList<Double>();
		ArrayList<Double> aveSynList = new ArrayList<Double>();
		for (int i = 0; i < numSections; i++) {
			if(!faultSectionData.get(i).getParentSectionName().equals(parentSectName)) {
				if(i != 0) {
					double ratio  = aveSyn/aveData;
					aveSyn /= numSubSect;
					aveData /= numSubSect;
					info += aveSynList.size()+"\t"+(float)ratio+"\t"+(float)aveSyn+"\t"+(float)aveData+"\t"+faultSectionData.get(i-1).getParentSectionName()+"\n";
//					System.out.println(ratio+"\t"+aveSyn+"\t"+aveData+"\t"+faultSectionData.get(i-1).getParentSectionName());
					aveSynList.add(aveSyn);
					aveDataList.add(aveData);
				}
				aveSyn=0;
				aveData=0;
				numSubSect=0;
				parentSectName = faultSectionData.get(i).getParentSectionName();
			}
			aveSyn +=  syn.getY(i);
			aveData +=  data.getY(i);
			numSubSect += 1;
		}
		ArrayList funcs5 = new ArrayList();		
		EvenlyDiscretizedFunc aveSynFunc = new EvenlyDiscretizedFunc(0,(double)aveSynList.size()-1,aveSynList.size());
		EvenlyDiscretizedFunc aveDataFunc = new EvenlyDiscretizedFunc(0,(double)aveSynList.size()-1,aveSynList.size());
		for(int i=0; i<aveSynList.size(); i++ ) {
			aveSynFunc.set(i, aveSynList.get(i));
			aveDataFunc.set(i, aveDataList.get(i));
		}
		aveSynFunc.setName("Predicted ave slip rates on parent section");
		aveDataFunc.setName("Original (Data) ave slip rates on parent section");
		aveSynFunc.setInfo(info);
		funcs5.add(aveSynFunc);
		funcs5.add(aveDataFunc);
		GraphiWindowAPI_Impl graph5 = new GraphiWindowAPI_Impl(funcs5, "Average Slip Rates on Parent Sections"); 
		graph5.setX_AxisLabel("Parent Section Index");
		graph5.setY_AxisLabel("Slip Rate");


		
		
		// Plot the paleo segment rate data vs. synthetics
		if(D) System.out.println("Making plot of event rate misfit . . . ");
		ArrayList funcs3 = new ArrayList();		
		EvenlyDiscretizedFunc finalEventRateFunc = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);
		EvenlyDiscretizedFunc finalPaleoVisibleEventRateFunc = new EvenlyDiscretizedFunc(0,(double)numSections-1,numSections);	
		for (int r=0; r<numRuptures; r++) {
			List<Integer> rup= getSectionsIndicesForRup(r);
			for (int i=0; i<rup.size(); i++) {			
				finalEventRateFunc.add(rup.get(i),rupRateSolution[r]);  
				finalPaleoVisibleEventRateFunc.add(rup.get(i),this.getProbPaleoVisible(rupMeanMag[r])*rupRateSolution[r]);  			
			}
		}	
		finalEventRateFunc.setName("Total Event Rates oer Section");
		finalPaleoVisibleEventRateFunc.setName("Paleo Visible Event Rates oer Section");
		funcs3.add(finalEventRateFunc);
		funcs3.add(finalPaleoVisibleEventRateFunc);	
		int num = segRateConstraints.size();
		ArbitrarilyDiscretizedFunc func;
		ArrayList obs_er_funcs = new ArrayList();
		SegRateConstraint constraint;
		for (int c = 0; c < num; c++) {
			func = new ArbitrarilyDiscretizedFunc();
			constraint = segRateConstraints.get(c);
			int seg = constraint.getSegIndex();
			func.set((double) seg - 0.0001, constraint.getLower95Conf());
			func.set((double) seg, constraint.getMean());
			func.set((double) seg + 0.0001, constraint.getUpper95Conf());
			func.setName(constraint.getFaultName());
			funcs3.add(func);
		}			
		GraphiWindowAPI_Impl graph3 = new GraphiWindowAPI_Impl(funcs3, "Synthetic Event Rates (total - black & paleo visible - blue) and Paleo Data (red)");
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		plotChars.add(new PlotCurveCharacterstics(
				PlotLineType.SOLID, 2f, Color.BLACK));
		plotChars.add(new PlotCurveCharacterstics(
				PlotLineType.SOLID, 2f, Color.BLUE));
		for (int c = 0; c < num; c++)
			plotChars.add(new PlotCurveCharacterstics(
					PlotLineType.SOLID, 1f, PlotSymbol.FILLED_CIRCLE, 4f, Color.RED));
		graph3.setPlottingFeatures(plotChars);
		graph3.setX_AxisLabel("Fault Section Index");
		graph3.setY_AxisLabel("Event Rate (per year)");
		
		
		
		// plot magnitude histogram for final rupture rates
		if(D) System.out.println("Making plots of final magnitude distribution . . . ");
		for (int i=0; i<mfdConstraints.size(); i++) {  // Loop over each MFD constraint 	
			if(D) System.out.println("MFD Constraint #" + (i+1) + " of " + mfdConstraints.size());
			IncrementalMagFreqDist magHist = new IncrementalMagFreqDist(5.05,40,0.1);
			magHist.setTolerance(0.2);	// this makes it a histogram
			for(int r=0; r<getNumRuptures();r++) {
				double fractionRupInRegion = mfdConstraints.get(i).getFractionInRegion(this.getFaultSectionDataForRupture(r));  // percentage of each rupture that is in region for that MFD
				magHist.add(rupMeanMag[r], fractionRupInRegion*rupRateSolution[r]);
			}
			ArrayList funcs4 = new ArrayList();
			magHist.setName("Magnitude Distribution of SA Solution");
			magHist.setInfo("(number in each mag bin)");
			funcs4.add(magHist);
			// If the magnitude constraint is used, add a plot of the target MFD
			if (relativeMagDistWt > 0.0) {		
				IncrementalMagFreqDist targetMagFreqDist = mfdConstraints.get(i).getMagFreqDist();; 
				targetMagFreqDist.setTolerance(0.1); 
				targetMagFreqDist.setName("Target Magnitude Distribution");
				targetMagFreqDist.setInfo("UCERF2 Solution minus background (with aftershocks added back in)");
				funcs4.add(targetMagFreqDist);
			}
			GraphiWindowAPI_Impl graph4 = new GraphiWindowAPI_Impl(funcs4, "Magnitude Histogram for Final Rates"); 
			graph4.setX_AxisLabel("Magnitude");
			graph4.setY_AxisLabel("Frequency (per bin)");
		}
	}

}
