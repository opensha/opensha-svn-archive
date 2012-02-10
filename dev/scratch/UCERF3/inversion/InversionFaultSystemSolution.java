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
import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
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
	boolean weightSlipRates, addMinimumRuptureRateConstraint;
	double relativeSegRateWt, relativeMagnitudeEqualityConstraintWt, relativeMagnitudeInequalityConstraintWt, relativeRupRateConstraintWt, relativeMinimizationConstraintWt, relativeSmoothnessWt;
	int numIterations;
	ArrayList<SegRateConstraint> segRateConstraints;
	
	double[] aPrioriRupConstraint;
	double[] minimizationConstraint;
	double[] initialRupModel;
	double[] minimumRuptureRates;
		
	ArrayList<MFD_InversionConstraint> mfdEqualityConstraints, mfdInequalityConstraints;
	
	DoubleMatrix2D A; // A matrix for inversion
	double[] d;	// data vector d for inversion
	
	DoubleMatrix2D A_MFD; // "A matrix" for MFD Inequality constraint
	double[] d_MFD;	// "data vector" for MFD Inequality constraint

	/**
	 * Constructor.
	 * 
	 * @param faultSystemRupSet
	 * @param relativeSegRateWt
	 * @param relativeMagnitudeEqualityConstraintWt
	 * @param relativeMagnitudeInequalityConstraintWt
	 * @param relativeRupRateConstraintWt
	 * @param relativeMinimizationConstraintWt
	 * @param numIterations
	 * @param segRateConstraints
	 * @param aPrioriRupConstraint
	 * @param initialRupModel
	 * @param mfdEqualityConstraints
	 * @param mfdInequalityConstraints
	 * @param minimizationConstraint
	 * @param relativeSmoothnessWt
	 */
	public InversionFaultSystemSolution(FaultSystemRupSet faultSystemRupSet, boolean weightSlipRates, double relativeSegRateWt, 
			double relativeMagnitudeEqualityConstraintWt, double relativeMagnitudeInequalityConstraintWt, double relativeRupRateConstraintWt, 
			double relativeMinimizationConstraintWt, int numIterations,
			ArrayList<SegRateConstraint> segRateConstraints, double[] aPrioriRupConstraint, double[] initialRupModel, 
			ArrayList<MFD_InversionConstraint> mfdEqualityConstraints, ArrayList<MFD_InversionConstraint> mfdInequalityConstraints, 
			double[] minimizationConstraint, double relativeSmoothnessWt, 
			boolean addMinimumRuptureRateConstraint, double[] minimumRuptureRates) {
		super(faultSystemRupSet, null);
		this.faultSystemRupSet=faultSystemRupSet;
		this.weightSlipRates=weightSlipRates;
		this.relativeSegRateWt=relativeSegRateWt;
		this.relativeMagnitudeEqualityConstraintWt=relativeMagnitudeEqualityConstraintWt;
		this.relativeMagnitudeInequalityConstraintWt=relativeMagnitudeInequalityConstraintWt;
		this.relativeRupRateConstraintWt=relativeRupRateConstraintWt;
		this.relativeMinimizationConstraintWt=relativeMinimizationConstraintWt;
		this.numIterations=numIterations;
		this.segRateConstraints=segRateConstraints;
		this.aPrioriRupConstraint=aPrioriRupConstraint;
		this.initialRupModel=initialRupModel;
		this.mfdEqualityConstraints=mfdEqualityConstraints;
		this.mfdInequalityConstraints=mfdInequalityConstraints;
		this.minimizationConstraint=minimizationConstraint;
		this.relativeSmoothnessWt=relativeSmoothnessWt;
		this.addMinimumRuptureRateConstraint=addMinimumRuptureRateConstraint;
		this.minimumRuptureRates=minimumRuptureRates;
		
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
		if (relativeMagnitudeEqualityConstraintWt > 0.0) {
			// RIGHT NOW THIS ASSUMES THE MAGNITUDE CONSTRAINT WEIGHT MUST BE THE SAME FOR ALL MAG-DIST CONSTRAINTS
			int totalNumMagFreqConstraints = 0;
			for (int i=0; i < mfdEqualityConstraints.size(); i++) {
				targetMagFreqDist=mfdEqualityConstraints.get(i).getMagFreqDist();
				totalNumMagFreqConstraints += targetMagFreqDist.getNum();
				numRows=numRows+targetMagFreqDist.getNum(); // add number of rows used for magnitude distribution constraint
			}
			if(D) System.out.println("Number of magnitude-distribution equality constraints: " + totalNumMagFreqConstraints);
		}
		
		
		// Components of matrix equation to invert (Ax=d)
		A = new SparseDoubleMatrix2D(numRows,numRuptures); // A matrix
		d = new double[numRows];	// data vector d
		rupRateSolution = new double[numRuptures]; // final solution (x of Ax=d)
		
		// MFD inequality constraint matrix and data vector (A_MFD * x <= d_MFD)
		// to be passed to SA algorithm
		int numMFDRows=0;
		if (relativeMagnitudeInequalityConstraintWt > 0.0) {
			int totalNumMagFreqConstraints = 0;
			for (int i=0; i < mfdInequalityConstraints.size(); i++) {
				targetMagFreqDist=mfdInequalityConstraints.get(i).getMagFreqDist();
				numMFDRows=numMFDRows+targetMagFreqDist.getNum(); // add number of rows used for magnitude distribution constraint
			}
		}
		System.out.println("numMFDrows = "+numMFDRows);
		A_MFD = new SparseDoubleMatrix2D(numMFDRows,numRuptures); // (A_MFD * x <= d_MFD)
		d_MFD = new double[numMFDRows];							
		if(D) System.out.println("Number of magnitude-distribution inequality constraints: " + mfdInequalityConstraints.size());
		
		
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
		// This is for equality constraints only -- inequality constraints must be passed to SA algorithm since they are nonlinear
		int rowIndex = numSlipRateConstraints + (int)Math.signum(relativeSegRateWt)*segRateConstraints.size(); // number of rows used for slip-rate and paleo-rate constraints
		if (relativeMagnitudeEqualityConstraintWt > 0.0) {	
			startTime = System.currentTimeMillis();
			numNonZeroElements = 0;
			if(D) System.out.println("\nAdding " + mfdEqualityConstraints.size()+ " magnitude distribution equality constraints to A matrix ...");	
			
			// make the matrix of the fraction of each rupture inside each region: fractRupsInsideMFD_Regions
			computeFractRupsInsideMFD_Regions(mfdEqualityConstraints);
			
			for (int i=0; i < mfdEqualityConstraints.size(); i++) {  // Loop over all MFD constraints in different regions
				targetMagFreqDist=mfdEqualityConstraints.get(i).getMagFreqDist();	
				for(int rup=0; rup<numRuptures; rup++) {
					double mag = rupMeanMag[rup];
					double fractRupInside = fractRupsInsideMFD_Regions[i][rup];
					if (fractRupInside > 0) {
						A.set(rowIndex+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeEqualityConstraintWt * fractRupInside);
						numNonZeroElements++;
					}
				}		
				for (double m=targetMagFreqDist.getMinX(); m<=targetMagFreqDist.getMaxX(); m=m+targetMagFreqDist.getDelta()) {
					d[rowIndex]=targetMagFreqDist.getY(m)*relativeMagnitudeEqualityConstraintWt;
					rowIndex++; 
				}	
			}
			runTime = System.currentTimeMillis()-startTime;
			if(D) System.out.println("Adding MFD Equality Constraints took " + (runTime/1000.) + " seconds.");
			if(D) System.out.println("Number of new nonzero elements in A matrix = "+numNonZeroElements);
		}
		
		
		// Prepare MFD Inequality Constraint (not added to A matrix directly since it's nonlinear)
		if (relativeMagnitudeInequalityConstraintWt > 0.0) {	
			startTime = System.currentTimeMillis();
			rowIndex = 0;
			if(D) System.out.println("\nPreparing " + mfdInequalityConstraints.size()+ " magnitude inequality constraints ...");	
			
			// make the matrix of the fraction of each rupture inside each region: fractRupsInsideMFD_Regions
			computeFractRupsInsideMFD_Regions(mfdInequalityConstraints);
			
			for (int i=0; i < mfdInequalityConstraints.size(); i++) {  // Loop over all MFD constraints in different regions
				targetMagFreqDist=mfdInequalityConstraints.get(i).getMagFreqDist();	
				for(int rup=0; rup<numRuptures; rup++) {
					double mag = rupMeanMag[rup];
					double fractRupInside = fractRupsInsideMFD_Regions[i][rup];
					if (fractRupInside > 0) {
	//					A_MFD.set(rowIndex+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeInequalityConstraintWt * fractRupInside);
						A_MFD.set(rowIndex+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeInequalityConstraintWt * fractRupInside / targetMagFreqDist.getClosestY(mag));
					}
				}		
				for (double m=targetMagFreqDist.getMinX(); m<=targetMagFreqDist.getMaxX(); m=m+targetMagFreqDist.getDelta()) {
	//				d_MFD[rowIndex]=targetMagFreqDist.getY(m)*relativeMagnitudeInequalityConstraintWt;
					d_MFD[rowIndex]=relativeMagnitudeInequalityConstraintWt;
					rowIndex++; 
				}	
			}	
			runTime = System.currentTimeMillis()-startTime;
			if(D) System.out.println("Preparing MFD Inequality Constraints took " + (runTime/1000.) + " seconds.");
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
/*		try {
			MatrixIO.doubleArrayToFile(d,new File("dev/scratch/UCERF3/preComputedData/d.bin"));
			MatrixIO.saveSparse(A,new File("dev/scratch/UCERF3/preComputedData/A.bin"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	*/

		
		
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
			if (D) System.out.println("Non-zero entries stored after " + ((System.currentTimeMillis()-startTime1)/1000.) + " seconds.");
			startTime1 = System.currentTimeMillis();
			for (int i=0; i<rows.size(); i++)
				Anew.set(rows.get(i), cols.get(i), vals.get(i));
			A = Anew;
			if (D) System.out.println("Done after " + ((System.currentTimeMillis()-startTime)/1000.) + " seconds.");
		}
		
		// Transform A_MFD matrix to different type that's fast for multiplication
		if (numIterations > 0 && A_MFD instanceof SparseDoubleMatrix2D && relativeMagnitudeInequalityConstraintWt > 0.0) {
			// if we're annealing, and A is in the SparseDoubleMatrix2D format then
			// we should convert it for faster matrix multiplications
			if (D) System.out.println("\nConverting A_MFD matrix to SparseRCDoubleMatrix2D ...");
			startTime = System.currentTimeMillis();
			SparseRCDoubleMatrix2D A_MFDnew = new SparseRCDoubleMatrix2D(A_MFD.rows(), A_MFD.columns());
			IntArrayList rows = new IntArrayList();
			IntArrayList cols = new IntArrayList();
			DoubleArrayList vals = new DoubleArrayList();
			long startTime1 = System.currentTimeMillis();
			A_MFD.getNonZeros(rows, cols, vals);
			if (D) System.out.println("Non-zero entries stored after " + ((System.currentTimeMillis()-startTime1)/1000.) + " seconds.");
			startTime1 = System.currentTimeMillis();
			for (int i=0; i<rows.size(); i++)
				A_MFDnew.set(rows.get(i), cols.get(i), vals.get(i));
			A_MFD = A_MFDnew;
			if (D) System.out.println("Done after " + ((System.currentTimeMillis()-startTime)/1000.) + " seconds.\n");
		}
		
		
		// SOLVE THE INVERSE PROBLEM
		// Run Simulated Annealing - with or without waterlevel
		if (addMinimumRuptureRateConstraint == false) {
			SimulatedAnnealing sa = new SerialSimulatedAnnealing(A, d, initialRupModel, relativeSmoothnessWt, relativeMagnitudeInequalityConstraintWt, A_MFD, d_MFD);
			sa.iterate(numIterations);
			rupRateSolution = sa.getBestSolution();
		} else {
			double[] d_offset = new double[d.length];  // This is the offset data vector: d_offset = d-A*minimumRuptureRates
			double[] d_MFD_offset = new double[d_MFD.length];  // This is the offset data vector for MFD inequality constraint: d_MFD_offset = d_MFD-A*minimumRuptureRates
			for (int i=0; i<A.rows(); i++) {
				d_offset[i] = d[i];
				for (int j=0; j<A.columns(); j++){
					d_offset[i] -= A.get(i, j) * minimumRuptureRates[j];
				}
			}
			for (int i=0; i<d_MFD.length ; i++) {
				d_MFD_offset[i] = d_MFD[i];
				for (int j=0; j<A_MFD.columns(); j++){
					d_MFD_offset[i] -= A_MFD.get(i, j) * minimumRuptureRates[j];
				}
			}
			SimulatedAnnealing sa = new SerialSimulatedAnnealing(A, d_offset, initialRupModel, relativeSmoothnessWt, relativeMagnitudeInequalityConstraintWt, A_MFD, d_MFD_offset);
			sa.iterate(numIterations);
			rupRateSolution = sa.getBestSolution();
			for (int i=0; i<rupRateSolution.length; i++) 
				rupRateSolution[i] = rupRateSolution[i] + minimumRuptureRates[i];
		}
		
		
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



}
