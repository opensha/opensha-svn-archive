package scratch.UCERF3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.util.FileUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.SegRateConstraint;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.simulatedAnnealing.SerialSimulatedAnnealing;
import scratch.UCERF3.simulatedAnnealing.SimulatedAnnealing;
import scratch.UCERF3.utils.DeformationModelFetcher;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.MatrixIO;
import scratch.UCERF3.utils.PaleoProbabilityModel;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;

/**
 * This class represents the UCERF3 "Grand Inversion".
 * 
 * @author Field, Page, Milner, & Powers
 *
 */
public class InversionFaultSystemSolution extends SimpleFaultSystemSolution {
	
	protected final static boolean D = true;  // for debugging
	// if true, getQuick and setQuick will be used on the matrices for increased speed
	// but will disable range checks. set to false for debugging.
	private final static boolean QUICK_GETS_SETS = true;
	
	FaultSystemRupSet faultSystemRupSet;
	boolean weightSlipRates, addMinimumRuptureRateConstraint;
	double relativeSegRateWt, relativeMagnitudeEqualityConstraintWt, relativeMagnitudeInequalityConstraintWt, relativeRupRateConstraintWt, relativeParticipationSmoothnessConstraintWt, participationConstraintMagBinSize, relativeMinimizationConstraintWt, relativeSmoothnessWt;
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
	 * @param relativeParticipationSmoothnessConstraintWt
	 * @param participationConstraintMagBinSize
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
			double relativeParticipationSmoothnessConstraintWt, double participationConstraintMagBinSize, double relativeMinimizationConstraintWt, int numIterations,
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
		this.relativeParticipationSmoothnessConstraintWt=relativeParticipationSmoothnessConstraintWt;
		this.participationConstraintMagBinSize=participationConstraintMagBinSize;
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
//		double[] sectSlipRateStdDevReduced = faultSystemRupSet.getSlipRateStdDevForAllSections();  // CURRENTLY NOT USED
		double[] rupMeanMag = faultSystemRupSet.getMagForAllRups();
		
		
		// Compute number of slip-rate constraints
		/* int numSlipRateConstraints = 0;
		for(int i=0; i<sectSlipRateReduced.length;i++)
			if(!Double.isNaN(sectSlipRateReduced[i]))
				numSlipRateConstraints+=1; */
		int numSlipRateConstraints = numSections; // We have decided to treat NaN slip rates the same as 0 slip rates: Minimize the model slip rates on these sections
		
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
		if (relativeParticipationSmoothnessConstraintWt > 0.0) {
			int totalNumMagParticipationConstraints = 0;
			for (int sect=0; sect<numSections; sect++) { 
				List<Integer> rupturesForSection = getRupturesForSection(sect);
				// Find minimum and maximum rupture-magnitudes for that subsection
				double minMag = 10.0; double maxMag = 0.0;
				for (int rupIndex=0; rupIndex<rupturesForSection.size(); rupIndex++) {
					if (rupMeanMag[rupturesForSection.get(rupIndex)] < minMag)
							minMag = rupMeanMag[rupturesForSection.get(rupIndex)];
					if (rupMeanMag[rupturesForSection.get(rupIndex)] > maxMag)
						maxMag = rupMeanMag[rupturesForSection.get(rupIndex)];
				}
				// Find total number of section magnitude-bins
				for (double m=minMag; m<maxMag; m=m+participationConstraintMagBinSize) { 
					for (int rupIndex=0; rupIndex<rupturesForSection.size(); rupIndex++) {
						if (rupMeanMag[rupturesForSection.get(rupIndex)]>=m && rupMeanMag[rupturesForSection.get(rupIndex)]<m+participationConstraintMagBinSize) {
							totalNumMagParticipationConstraints++; 
							numRows++;
							break;
						}				
					}
				}
			}
			if(D) System.out.println("Number of MFD participation constraints: " + totalNumMagParticipationConstraints);
		}
		
		
		// Components of matrix equation to invert (Ax=d)
		// this one is faster but uses more memory
		A = new SparseDoubleMatrix2D(numRows,numRuptures); // A matrix
		// this one is slower, but might use less memory
//		A = new SparseRCDoubleMatrix2D(numRows,numRuptures); // A matrix
		d = new double[numRows];	// data vector d
		rupRateSolution = new double[numRuptures]; // final solution (x of Ax=d)
		
		// MFD inequality constraint matrix and data vector (A_MFD * x <= d_MFD)
		// to be passed to SA algorithm
		int numMFDRows=0;
		if (relativeMagnitudeInequalityConstraintWt > 0.0) {
			for (int i=0; i < mfdInequalityConstraints.size(); i++) {
				targetMagFreqDist=mfdInequalityConstraints.get(i).getMagFreqDist();
				numMFDRows+=targetMagFreqDist.getNum(); // add number of rows used for magnitude distribution constraint
			}
			A_MFD = new SparseDoubleMatrix2D(numMFDRows,numRuptures); // (A_MFD * x <= d_MFD)
			d_MFD = new double[numMFDRows];							
			if(D) System.out.println("Number of magnitude-distribution inequality constraints (not in A matrix): " + mfdInequalityConstraints.size());
		}
		
		
		if(D) System.out.println("Total number of constraints (rows): " + numRows);
		if(D) System.out.println("\nNumber of fault sections: " + numSections + ". Number of ruptures (columns): " + numRuptures + ".");
		
		
		// Put together "A" Matrix and data vector "d"
		
		
		// Make sparse matrix of slip in each rupture & data vector of section slip rates
		int numNonZeroElements = 0;  
		if(D) System.out.println("\nAdding slip per rup to A matrix ...");
		long startTime = System.currentTimeMillis();
		// A matrix component of slip-rate constraint (excludes sections with NaN slip rates from constraint)
		for (int rup=0; rup<numRuptures; rup++) {
			double[] slips = getSlipOnSectionsForRup(rup);
			List<Integer> sects = getSectionsIndicesForRup(rup);
			for (int i=0; i < slips.length; i++) {
				int row = sects.get(i);
				int col = rup;
				double val;
				if (weightSlipRates == false) 
					val = slips[i];
				else {  // Normalize by slip rate
					if (sectSlipRateReduced[row] == 0 || Double.isNaN(sectSlipRateReduced[row]))  // Treat NaN slip rates as 0 (minimize)
						val = slips[i]/0.001;  
					else {
						val = slips[i]/sectSlipRateReduced[row]; 
					/*		if (Double.isNaN(A.getQuick(row,col))) {
								System.out.println("\nrup # = "+rup+", mean mag = "+rupMeanMag[rup]);
								System.out.println("sects = "+sects);
								System.out.println("slips = "+slips);
								System.out.println("Slip["+i+"] = "+slips[i]); 
							}  */			// Creeping section slips are coming back NaN because aseismicity is 100% !
						}
				}
				if (Double.isNaN(val))
					throw new IllegalStateException("A["+row+"]["+col+"] is NaN! sectSlipRateReduced["+row
							+"] = "+sectSlipRateReduced[row]+" and slips["+i+"] = "+slips[i]);
				if (QUICK_GETS_SETS)
					A.setQuick(row, col, val);
				else
					A.set(row, col, val);
				if(D) numNonZeroElements++;
			}
		}
		// d vector component of slip-rate constraint
		for (int sect=0; sect<numSections; sect++) {
			
			if (weightSlipRates == false || sectSlipRateReduced[sect]==0) 
				d[sect] = sectSlipRateReduced[sect];			
			else {
				if (Double.isNaN(sectSlipRateReduced[sect])) d[sect] = 0;  // Treat NaN slip rates as 0 (minimize)
				else d[sect] = 1; // Normalize by slip rate
			}
			if (Double.isNaN(d[sect]))
				throw new IllegalStateException("d["+sect+"] is NaN!  sectSlipRateReduced["+sect+"] = "+sectSlipRateReduced[sect]);
		}
		long runTime = System.currentTimeMillis()-startTime;
		if(D) System.out.println("Adding Slip-Rate Constraints took " + (runTime/1000.) + " seconds.");
		if(D) System.out.println("Number of nonzero elements in A matrix = "+numNonZeroElements);
		
		
		// Make sparse matrix of paleo event probs for each rupture & data vector of mean event rates
		if (relativeSegRateWt > 0.0) {
			PaleoProbabilityModel UCERF3PaleoProbModel = null;
			try {  UCERF3PaleoProbModel = PaleoProbabilityModel.fromFile(new File("dev/scratch/UCERF3/preComputedData/pdetection2.txt"));
			} catch (IOException e1) {	e1.printStackTrace();  }	
			double[] rupMeanSlip = faultSystemRupSet.getAveSlipForAllRups();
			numNonZeroElements = 0;
			startTime = System.currentTimeMillis();
			if(D) System.out.println("\nAdding event rates to A matrix ...");
			for (int i=numSlipRateConstraints; i<numSlipRateConstraints+segRateConstraints.size(); i++) {
				SegRateConstraint constraint = segRateConstraints.get(i-numSlipRateConstraints);
				d[i]=relativeSegRateWt * constraint.getMean() / constraint.getStdDevOfMean();
				for (int rup=0; rup<numRuptures; rup++) {
					double getVal;
					if (QUICK_GETS_SETS)
						getVal = A.getQuick(constraint.getSegIndex(), rup);
					else
						getVal = A.get(constraint.getSegIndex(), rup);
					if (getVal>0) {
						double distAlongRup = getDistanceAlongRupture(getSectionsIndicesForRup(rup), (ArrayList<FaultSectionPrefData>) faultSystemRupSet.getFaultSectionDataList(), constraint); // Glenn's x/L
//						double probPaleoVisible = getProbPaleoVisible(rupMeanMag[rup]); // OLD UCERF2 version!
						double probPaleoVisible = UCERF3PaleoProbModel.getForSlip(rupMeanSlip[rup], distAlongRup); // UCERF3 version!	
						double setVal = (relativeSegRateWt * probPaleoVisible / constraint.getStdDevOfMean());
						if (QUICK_GETS_SETS)
							A.setQuick(i, rup, setVal);
						else
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
						if (QUICK_GETS_SETS)
							A.setQuick(rowIndex+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeEqualityConstraintWt * fractRupInside);
						else
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
			int rowIndex_MFD = 0; 
			if(D) System.out.println("\nPreparing " + mfdInequalityConstraints.size()+ " magnitude inequality constraints ...");	
			
			// make the matrix of the fraction of each rupture inside each region: fractRupsInsideMFD_Regions
			computeFractRupsInsideMFD_Regions(mfdInequalityConstraints);
			
			for (int i=0; i < mfdInequalityConstraints.size(); i++) {  // Loop over all MFD constraints in different regions
				targetMagFreqDist=mfdInequalityConstraints.get(i).getMagFreqDist();	
				for(int rup=0; rup<numRuptures; rup++) {
					double mag = rupMeanMag[rup];
					double fractRupInside = fractRupsInsideMFD_Regions[i][rup];
					if (fractRupInside > 0) {
	//					A_MFD.setQuick(rowIndex_MFD+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeInequalityConstraintWt * fractRupInside);
						if (QUICK_GETS_SETS)
							A_MFD.setQuick(rowIndex_MFD+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeInequalityConstraintWt * fractRupInside / targetMagFreqDist.getClosestY(mag));
						else
							A_MFD.set(rowIndex_MFD+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeInequalityConstraintWt * fractRupInside / targetMagFreqDist.getClosestY(mag));
					}
				}		
				for (double m=targetMagFreqDist.getMinX(); m<=targetMagFreqDist.getMaxX(); m=m+targetMagFreqDist.getDelta()) {
	//				d_MFD[rowIndex_MFD]=targetMagFreqDist.getY(m)*relativeMagnitudeInequalityConstraintWt;
					d_MFD[rowIndex_MFD]=relativeMagnitudeInequalityConstraintWt;
					rowIndex_MFD++; 
				}	
			}	
			runTime = System.currentTimeMillis()-startTime;
			if(D) System.out.println("Preparing MFD Inequality Constraints took " + (runTime/1000.) + " seconds.");
		}
		
		
		// MFD Smoothness Constraint - Constrain participation MFD to be uniform for each fault subsection
		if (relativeParticipationSmoothnessConstraintWt > 0.0) {	
			if(D) System.out.println("\nAdding MFD participation smoothness constraints to A matrix ...");
			startTime = System.currentTimeMillis();
			numNonZeroElements = 0;
			ArrayList<Integer> numRupsForMagBin = new ArrayList<Integer>();
			for (int sect=0; sect<numSections; sect++) {
				List<Integer> rupturesForSection = getRupturesForSection(sect);
				
				// Find minimum and maximum rupture-magnitudes for that subsection
				double minMag = 10.0; double maxMag = 0.0;
				for (int rupIndex=0; rupIndex<rupturesForSection.size(); rupIndex++) {
					if (rupMeanMag[rupturesForSection.get(rupIndex)] < minMag)
						minMag = rupMeanMag[rupturesForSection.get(rupIndex)];
					if (rupMeanMag[rupturesForSection.get(rupIndex)] > maxMag)
						maxMag = rupMeanMag[rupturesForSection.get(rupIndex)];
				}
				if (minMag == 10.0 || minMag == 0.0) 
					throw new IllegalStateException("No ruptures for section #"+sect);
				
				// Find number of ruptures for this section for each magnitude bin & total number of magnitude-bins with ruptures
				numRupsForMagBin.clear();
				int numNonzeroMagBins = 0;
				for (double m=minMag; m<maxMag; m=m+participationConstraintMagBinSize) {
					numRupsForMagBin.add(0);
					for (int rupIndex=0; rupIndex<rupturesForSection.size(); rupIndex++) {
						if (rupMeanMag[rupturesForSection.get(rupIndex)]>=m && rupMeanMag[rupturesForSection.get(rupIndex)]<m+participationConstraintMagBinSize) 
							numRupsForMagBin.set(numRupsForMagBin.size()-1, numRupsForMagBin.get(numRupsForMagBin.size()-1)+1); // numRupsForMagBin(end)++
					}
					if (numRupsForMagBin.get(numRupsForMagBin.size()-1)>0)
						numNonzeroMagBins++;
				}
				
				// Put together A matrix elements: A_avg_rate_per_mag_bin * x - A_rate_for_particular_mag_bin * x = 0
				// Each mag bin (that contains ruptures) for each subsection adds one row to A & d
				int magBinIndex=0;
				for (double m=minMag; m<maxMag; m=m+participationConstraintMagBinSize) {
					if (numRupsForMagBin.get(magBinIndex) > 0) {
						for (int rupIndex=0; rupIndex<rupturesForSection.size(); rupIndex++) {
							// Average rate per magnitude bin for this section
							int col = rupturesForSection.get(rupIndex);
							double val = relativeParticipationSmoothnessConstraintWt/numNonzeroMagBins;
//							A.setQuick(rowIndex,rupturesForSection.get(rupIndex),relativeParticipationSmoothnessConstraintWt/numNonzeroMagBins);	
							numNonZeroElements++;
							if (rupMeanMag[rupturesForSection.get(rupIndex)]>=m && rupMeanMag[rupturesForSection.get(rupIndex)]<m+participationConstraintMagBinSize) {
								// Subtract off rate for this mag bin (difference between average rate per mag bin & rate for this mag bin is set to 0)
								val -= relativeParticipationSmoothnessConstraintWt;
//								A.setQuick(rowIndex,rupturesForSection.get(rupIndex),A.getQuick(rowIndex,rupturesForSection.get(rupIndex)) - relativeParticipationSmoothnessConstraintWt);
							}
							if (QUICK_GETS_SETS)
								A.setQuick(rowIndex, col, val);
							else
								A.set(rowIndex, col, val);
						}
						d[rowIndex] = 0;
						rowIndex++;
					}	
					magBinIndex++;				
				}		
			}
			runTime = System.currentTimeMillis()-startTime;
			if(D) System.out.println("Adding Participation MFD Constraints took " + (runTime/1000.) + " seconds.");
			if(D) System.out.println("Number of new nonzero elements in A matrix = "+numNonZeroElements);			
		}
		
		
		// Constrain Rupture Rate Solution to approximately equal aPrioriRupConstraint
		if (relativeRupRateConstraintWt > 0.0) {
			startTime = System.currentTimeMillis();
			if(D) System.out.println("\nAdding rupture-rate constraint to A matrix ...");
			numNonZeroElements = 0;
			for(int rup=0; rup<numRuptures; rup++) {
				if (QUICK_GETS_SETS)
					A.setQuick(rowIndex,rup,relativeRupRateConstraintWt);
				else
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
				if (QUICK_GETS_SETS)
					A.setQuick(rowIndex,rup,relativeMinimizationConstraintWt*minimizationConstraint[rup]);
				else
					A.set(rowIndex,rup,relativeMinimizationConstraintWt*minimizationConstraint[rup]);
				d[rowIndex]=0;
				numNonZeroElements++; rowIndex++;
			}
			runTime = System.currentTimeMillis()-startTime;
			if(D) System.out.println("Adding Minimization Constraints took " + (runTime/1000.) + " seconds.");
			if(D) System.out.println("Number of new nonzero elements in A matrix = "+numNonZeroElements);
		}
		
		

		// Transform A matrix & A_MFD to different type that's SUPER FAST for multiplication !!!
		SparseCCDoubleMatrix2D Anew = ((SparseDoubleMatrix2D)A).getColumnCompressed(true);
		A = Anew;
		if (A_MFD != null) {
			SparseCCDoubleMatrix2D A_MFD_new = ((SparseDoubleMatrix2D)A_MFD).getColumnCompressed(true);
			A_MFD = A_MFD_new;
		}
		
		
		// SOLVE THE INVERSE PROBLEM
		// Run Simulated Annealing - with or without waterlevel
		if (addMinimumRuptureRateConstraint == false) {
			
			// OPTIONAL: write everything to a zip file for Kevin!
			try {
				writeZipFile(A, d, initialRupModel, A_MFD, d_MFD);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			SimulatedAnnealing sa = new SerialSimulatedAnnealing(A, d, initialRupModel, relativeSmoothnessWt, A_MFD, d_MFD);
			sa.iterate(numIterations);
			rupRateSolution = sa.getBestSolution();
		} else {
			double[] d_offset = new double[d.length];  // This is the offset data vector: d_offset = d-A*minimumRuptureRates
			for (int i=0; i<A.rows(); i++) {
				d_offset[i] = d[i];
				for (int j=0; j<A.columns(); j++){
					if (QUICK_GETS_SETS)
						d_offset[i] -= A.getQuick(i, j) * minimumRuptureRates[j];
					else
						d_offset[i] -= A.get(i, j) * minimumRuptureRates[j];
				}
			}
			double[] d_MFD_offset = null;
			if (d_MFD != null) {
				// This is the offset data vector for MFD inequality constraint: d_MFD_offset = d_MFD-A*minimumRuptureRates
				d_MFD_offset = new double[d_MFD.length];
				for (int i=0; i<d_MFD.length ; i++) {
					d_MFD_offset[i] = d_MFD[i];
					for (int j=0; j<A_MFD.columns(); j++){
						if (QUICK_GETS_SETS)
							d_MFD_offset[i] -= A_MFD.getQuick(i, j) * minimumRuptureRates[j];
						else
							d_MFD_offset[i] -= A_MFD.get(i, j) * minimumRuptureRates[j];
					}
				}
			}
			
			// OPTIONAL: write everything to a zip file for Kevin!
			try {
				writeZipFile(A, d_offset, initialRupModel, A_MFD, d_MFD_offset, minimumRuptureRates);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			SimulatedAnnealing sa = new SerialSimulatedAnnealing(A, d_offset, initialRupModel, relativeSmoothnessWt, A_MFD, d_MFD_offset);
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
	
	private static void writeZipFile(
			DoubleMatrix2D A, double[] d, double[] initial,
			DoubleMatrix2D A_ineq, double[] d_ineq)
					throws IOException {
		writeZipFile(A, d, initial, A_ineq, d_ineq, null);
	}
	
	private static void writeZipFile(
			DoubleMatrix2D A, double[] d, double[] initial,
			DoubleMatrix2D A_ineq, double[] d_ineq, double[] minimumRuptureRates)
					throws IOException {
		if(D) System.out.println("Saving to files...");
		File dir = new File("dev/scratch/UCERF3/preComputedData/");
		File zipFile = new File(dir, "inputs.zip");
		ArrayList<String> fileNames = new ArrayList<String>();
		
		fileNames.add("d.bin");			
		MatrixIO.doubleArrayToFile(d, new File(dir, "d.bin"));
		if(D) System.out.println("d.bin saved");
		
		fileNames.add("a.bin");			
		MatrixIO.saveSparse(A, new File(dir, "a.bin"));
		if(D) System.out.println("a.bin saved");
		
		fileNames.add("initial.bin");	
		MatrixIO.doubleArrayToFile(initial, new File(dir, "initial.bin"));
		if(D) System.out.println("initial.bin saved");
		
		if (d_ineq != null) {
			fileNames.add("d_ineq.bin");	
			MatrixIO.doubleArrayToFile(d_ineq, new File(dir, "d_ineq.bin"));
			if(D) System.out.println("d_ineq.bin saved");
		}
		
		if (A_ineq != null) {
			fileNames.add("a_ineq.bin");	
			MatrixIO.saveSparse(A_ineq,new File(dir, "a_ineq.bin"));
			if(D) System.out.println("a_ineq.bin saved");
		}
		
		if (minimumRuptureRates != null) {
			fileNames.add("minimumRuptureRates.bin");	
			MatrixIO.doubleArrayToFile(minimumRuptureRates,new File(dir, "minimumRuptureRates.bin"));
			if(D) System.out.println("minimumRuptureRates.bin saved");
		}
		
		FileUtils.createZipFile(zipFile.getAbsolutePath(), dir.getAbsolutePath(), fileNames);
		if(D) System.out.println("Zip file saved");
	}
	
//	public void plotStuff(ArrayList<ArrayList<Integer>> rupList, DoubleMatrix2D A, double[] d, double[] rupRateSolution, double relativeMagDistWt, FindEquivUCERF2_Ruptures findUCERF2_Rups) {



}
