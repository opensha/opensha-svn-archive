package scratch.UCERF3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opensha.commons.data.CSVFile;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.util.FileUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;

import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseRCDoubleMatrix2D;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.utils.MFD_InversionConstraint;
import scratch.UCERF3.utils.MatrixIO;
import scratch.UCERF3.utils.PaleoProbabilityModel;
import scratch.UCERF3.utils.paleoRateConstraints.PaleoRateConstraint;

/**
 * This class is used to generate inversion inputs (A/A_ineq matrices, d/d_ineq vectors) for a given
 * rupture set, inversion configuration, paleo rate constraints, improbability constraint, and paleo
 * probability model. It can also save these inputs to a zip file to be run on high performance
 * computing.
 * 
 * @author Kevin, Morgan, Ned
 *
 */
public class InversionInputGenerator {
	
	private static final boolean D = true;
	/**
	 * this enables use of the getQuick and setQuick methods on the sparse matrices.
	 * this comes with a performance boost, but disables range checks and is more prone
	 * to errors. this will always be true if debugging is disabled above. when debugging
	 * is enabled, the 2nd value of the following line is used to enable/disable.
	 */
	private static final boolean QUICK_GETS_SETS = !D || true;
	private boolean aPrioriConstraintForZeroRates = true;  // If true, a Priori rup-rate constraint is applied to zero rates (eg, rups not in UCERF2)
	private double aPrioriConstraintForZeroRatesWtFactor = 0.1; // Amount to multiply standard a-priori rup rate weight by when applying to zero rates (minimization constraint for rups not in UCERF2)
	private boolean excludeParkfieldRupsFromMfdEqualityConstraints = true; // If true, rates of Parkfield M~6 ruptures do not count toward MFD Equality Constraint misfit
	
	// inputs
	private FaultSystemRupSet rupSet;
	private InversionConfiguration config;
	private List<PaleoRateConstraint> paleoRateConstraints;
	private double[] improbabilityConstraint; // TODO may become an object of some sort
	private PaleoProbabilityModel paleoProbabilityModel;
	
	// outputs
	private DoubleMatrix2D A;
	private DoubleMatrix2D A_ineq;
	private double[] d;
	private double[] d_ineq;
	private double[] initial;
	private double[] minimumRuptureRates;
	
	private ArrayList<Integer> rangeEndRows;
	private ArrayList<String> rangeNames;
	
	public InversionInputGenerator(
			FaultSystemRupSet rupSet,
			InversionConfiguration config,
			List<PaleoRateConstraint> paleoRateConstraints,
			double[] improbabilityConstraint, // may become an object in the future
			PaleoProbabilityModel paleoProbabilityModel) {
		this.rupSet = rupSet;
		this.config = config;
		this.paleoRateConstraints = paleoRateConstraints;
		this.improbabilityConstraint = improbabilityConstraint;
		this.paleoProbabilityModel = paleoProbabilityModel;
	}
	
	/**
	 * Loads the default paleo probability model for UCERF3 (Glenn's file). Can be turned into
	 * an enum if we get alternatives
	 * @return
	 * @throws IOException 
	 */
	public static PaleoProbabilityModel loadDefaultPaleoProbabilityModel() throws IOException {
		return PaleoProbabilityModel.loadUCERF3PaleoProbabilityModel();
	}
	
	public void generateInputs() {
		generateInputs(null);
	}
	
	public void generateInputs(Class<? extends DoubleMatrix2D> clazz) {
		/*
		 * This is a very important part of our code. There are a few key rules we should abide by here
		 * to make sure it continues to operate correctly.
		 * 
		 * * ABSOLUTELY NO try/catch blocks that just print the stack trace and continue on.
		 * * No data file loading in here - data files should be loaded externally and passed in
		 */
		
		int numSections = rupSet.getNumSections();
		int numRuptures = rupSet.getNumRuptures();
		
		initial = config.getInitialRupModel();
		minimumRuptureRates = null;
		if (initial == null) {
			// all zeros
			initial = new double[numRuptures];
		}
		
		// now lets do a little input validation
		Preconditions.checkState(initial.length == numRuptures);
		Preconditions.checkState(config.getA_PrioriRupConstraint() == null
				|| config.getA_PrioriRupConstraint().length == numRuptures);
		Preconditions.checkState(config.getMinimumRuptureRateBasis() == null
				|| config.getMinimumRuptureRateBasis().length == numRuptures);
		
		double minimumRuptureRateFraction = config.getMinimumRuptureRateFraction();
		if (minimumRuptureRateFraction > 0) {
			// set up minimum rupture rates (water level)
			double[] minimumRuptureRateBasis = config.getMinimumRuptureRateBasis();
			Preconditions.checkNotNull(minimumRuptureRateBasis,
					"minimum rate fraction specified by no minimum rate basis given!");
			
			// first check to make sure that they're not all zeros
			boolean allZeros = true;
			for (int i=0; i<numRuptures; i++) {
				if (minimumRuptureRateBasis[i] > 0) {
					allZeros = false;
					break;
				}
			}
			Preconditions.checkState(!allZeros, "cannot set water level when water level rates are all zero!");
			
			minimumRuptureRates = new double[initial.length];
			for (int i=0; i < numRuptures; i++)
				minimumRuptureRates[i] = minimumRuptureRateBasis[i]*minimumRuptureRateFraction;
		}
		
		// now configure the minimum rupture rates
		
		double[] sectSlipRateReduced = rupSet.getSlipRateForAllSections();
//		double[] sectSlipRateStdDevReduced = getSlipRateStdDevForAllSections();  // CURRENTLY NOT USED
		double[] rupMeanMag = rupSet.getMagForAllRups();
		
		rangeEndRows = new ArrayList<Integer>();
		rangeNames = new ArrayList<String>();
		
		// Compute number of slip-rate constraints
		/* int numSlipRateConstraints = 0;
		for(int i=0; i<sectSlipRateReduced.length;i++)
			if(!Double.isNaN(sectSlipRateReduced[i]))
				numSlipRateConstraints+=1; */
		// We have decided to treat NaN slip rates the same as 0 slip rates:
		// 		Minimize the model slip rates on these sections
		int numSlipRateConstraints = numSections;
		
		// Find number of rows in A matrix (equals the total number of constraints)
		if(D) System.out.println("\nNumber of slip-rate constraints:    " + numSlipRateConstraints);
		int numRows = numSlipRateConstraints;
		if (numRows > 0) {
			rangeEndRows.add(numRows-1);
			rangeNames.add("Slip Rate");
		}
		
		int numPaleoRows = (int)Math.signum(config.getRelativePaleoRateWt())*paleoRateConstraints.size();
		if(D) System.out.println("Number of paleo section-rate constraints: "+numPaleoRows);
		if (numPaleoRows > 0) {
			numRows += numPaleoRows;
			rangeEndRows.add(numRows-1);
			rangeNames.add("Paleo");
		}
		
		if (config.getRelativeRupRateConstraintWt() > 0.0) {
			double[] relativeRupRateConstraintWt = config.getA_PrioriRupConstraint();
			int numRupRateRows = 0;
			for (int i=0; i<numRuptures; i++) 
				if (!aPrioriConstraintForZeroRates) {
					if (relativeRupRateConstraintWt[i]>0) 	numRupRateRows++;	}
				else
					numRupRateRows++;
			if(D) System.out.println("Number of rupture-rate constraints: "+numRupRateRows);
			numRows += numRupRateRows;
			rangeEndRows.add(numRows-1);
			rangeNames.add("Rupture Rates");
		}
		
		int numMinimizationRows = (int)Math.signum(config.getRelativeMinimizationConstraintWt())*numRuptures;
		if(D) System.out.println("Number of minimization constraints: "+numMinimizationRows);
		if (numMinimizationRows > 0) {
			numRows += numMinimizationRows;
			rangeEndRows.add(numRows-1);
			rangeNames.add("Minimization");
		}
		
		IncrementalMagFreqDist targetMagFreqDist=null;
		if (config.getRelativeMagnitudeEqualityConstraintWt() > 0.0) {
			int totalNumMagFreqConstraints = 0;
			for (MFD_InversionConstraint constr : config.getMfdEqualityConstraints()) {
				targetMagFreqDist=constr.getMagFreqDist();
				// Find number of rows used for MFD equality constraint - only include mag bins between minimum and maximum magnitudes in rupture set
				totalNumMagFreqConstraints += targetMagFreqDist.getClosestXIndex(rupSet.getMaxMag())-targetMagFreqDist.getClosestXIndex(rupSet.getMinMag())+1;
				// add number of rows used for magnitude distribution constraint
				numRows=numRows+targetMagFreqDist.getNum();
			}
			if(D) System.out.println("Number of magnitude-distribution equality constraints: "
					+totalNumMagFreqConstraints);
			rangeEndRows.add(numRows-1);
			rangeNames.add("MFD Equality");
		}
		if (config.getRelativeParticipationSmoothnessConstraintWt() > 0.0) {
			int totalNumMagParticipationConstraints = 0;
			for (int sect=0; sect<numSections; sect++) { 
				List<Integer> rupturesForSection = rupSet.getRupturesForSection(sect);
				// Find minimum and maximum rupture-magnitudes for that subsection
				double minMag = 10.0; double maxMag = 0.0;
				for (int rupIndex=0; rupIndex<rupturesForSection.size(); rupIndex++) {
					if (rupMeanMag[rupturesForSection.get(rupIndex)] < minMag)
							minMag = rupMeanMag[rupturesForSection.get(rupIndex)];
					if (rupMeanMag[rupturesForSection.get(rupIndex)] > maxMag)
						maxMag = rupMeanMag[rupturesForSection.get(rupIndex)];
				}
				// Find total number of section magnitude-bins
				for (double m=minMag; m<maxMag; m=m+config.getParticipationConstraintMagBinSize()) { 
					for (int rupIndex=0; rupIndex<rupturesForSection.size(); rupIndex++) {
						if (rupMeanMag[rupturesForSection.get(rupIndex)]>=m
								&& rupMeanMag[rupturesForSection.get(rupIndex)]
								              < m+config.getParticipationConstraintMagBinSize()) {
							totalNumMagParticipationConstraints++; 
							numRows++;
							break;
						}				
					}
				}
			}
			if(D) System.out.println("Number of MFD participation constraints: "
					+ totalNumMagParticipationConstraints);
			rangeEndRows.add(numRows-1);
			rangeNames.add("MFD Participation");
		}
		if (config.getRelativeMomentConstraintWt() > 0.0) {
			numRows++;
			if(D) System.out.println("Number of Moment constraints: 1");
			rangeEndRows.add(numRows-1);
			rangeNames.add("Moment");
		}
		if (config.getRelativeParkfieldConstraintWt() > 0.0) {
			numRows++;
			if(D) System.out.println("Number of Parkfield constraints: 1");
			rangeEndRows.add(numRows-1);
			rangeNames.add("Parkfield");
		}
		
		
		// Components of matrix equation to invert (Ax=d)
		A = buildMatrix(clazz, numRows, numRuptures); // A matrix
		d = new double[numRows];	// data vector d
		
		// MFD inequality constraint matrix and data vector (A_MFD * x <= d_MFD)
		// to be passed to SA algorithm
		int numMFDRows=0;
		if (config.getRelativeMagnitudeInequalityConstraintWt() > 0.0) {
			for (MFD_InversionConstraint constr : config.getMfdInequalityConstraints()) {
				targetMagFreqDist=constr.getMagFreqDist();
				// Add number of rows used for magnitude distribution constraint - only include mag bins between minimum and maximum magnitudes in rupture set				
				numMFDRows += targetMagFreqDist.getClosestXIndex(rupSet.getMaxMag())-targetMagFreqDist.getClosestXIndex(rupSet.getMinMag())+1;
			}
			A_ineq = buildMatrix(clazz, numMFDRows, numRuptures); // (A_MFD * x <= d_MFD)
			d_ineq = new double[numMFDRows];							
			if(D) System.out.println("Number of magnitude-distribution inequality constraints (not in A matrix): "
					+numMFDRows);
		}
		
		
		if(D) System.out.println("Total number of constraints (rows): " + numRows);
		if(D) System.out.println("\nNumber of fault sections: "
				+ numSections + ". Number of ruptures (columns): " + numRuptures + ".");
		
		
		// Put together "A" Matrix and data vector "d"
		Stopwatch watch_total = null;
		Stopwatch watch = null;
		if (D) {
			watch_total = new Stopwatch();
			watch_total.start();
			watch = new Stopwatch();
			watch.start();
		}
		
		// Make sparse matrix of slip in each rupture & data vector of section slip rates
		int numNonZeroElements = 0;  
		if(D) System.out.println("\nAdding slip per rup to A matrix ...");
		// A matrix component of slip-rate constraint 
		for (int rup=0; rup<numRuptures; rup++) {
			double[] slips = rupSet.getSlipOnSectionsForRup(rup);
			List<Integer> sects = rupSet.getSectionsIndicesForRup(rup);
			for (int i=0; i < slips.length; i++) {
				int row = sects.get(i);
				int col = rup;
				double val;
				if (!config.isWeightSlipRates()) 
					val = slips[i];
				else {  // Normalize by slip rate
					// Note that constraints for sections w/ slip rate < 0.1 mm/yr is not normalized by slip rate -- otherwise misfit will be huge (GEOBOUND model has 10e-13 slip rates that will dominate misfit otherwise)
					if (sectSlipRateReduced[row] < 1E-4 || Double.isNaN(sectSlipRateReduced[row]))  
						val = slips[i]/0.0001;  
					else {
						val = slips[i]/sectSlipRateReduced[row]; 
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
			
			if (!config.isWeightSlipRates() || sectSlipRateReduced[sect]==0) 
				d[sect] = sectSlipRateReduced[sect];			
			else {
				if (Double.isNaN(sectSlipRateReduced[sect]) || sectSlipRateReduced[sect]<1E-4)
					// Treat NaN slip rates as 0 (minimize)
					d[sect] = 0;
				if (sectSlipRateReduced[sect]<1E-4)
					// For very small slip rates, do not normalize by slip rate (normalize by 0.0001 instead) so they don't dominate misfit
					d[sect] = sectSlipRateReduced[sect]/0.0001;
				else
					// Normalize by slip rate
					d[sect] = 1;
			}
			if (Double.isNaN(d[sect]))
				throw new IllegalStateException("d["+sect
						+"] is NaN!  sectSlipRateReduced["+sect+"] = "+sectSlipRateReduced[sect]);
		}
		if (D) {
			System.out.println("Adding Slip-Rate Constraints took "+getTimeStr(watch)+".");
			watch.reset();
			watch.start();
			System.out.println("Number of nonzero elements in A matrix = "+numNonZeroElements);
		}
		
		
		// Make sparse matrix of paleo event probs for each rupture & data vector of mean event rates
		if (config.getRelativePaleoRateWt() > 0.0) {
			double relativePaleoRateWt = config.getRelativePaleoRateWt();
			double[] rupMeanSlip = rupSet.getAveSlipForAllRups();
			numNonZeroElements = 0;
			if(D) System.out.println("\nAdding event rates to A matrix ...");
			for (int i=numSlipRateConstraints; i<numSlipRateConstraints+paleoRateConstraints.size(); i++) {
				PaleoRateConstraint constraint = paleoRateConstraints.get(i-numSlipRateConstraints);
				d[i]=relativePaleoRateWt * constraint.getMeanRate() / constraint.getStdDevOfMeanRate();
				List<Integer> rupsForSect = rupSet.getRupturesForSection(constraint.getSectionIndex());
				for (int rupIndex=0; rupIndex<rupsForSect.size(); rupIndex++) {
					int rup = rupsForSect.get(rupIndex);
					// Glenn's x/L
					double distAlongRup = getDistanceAlongRupture(rupSet.getSectionsIndicesForRup(rup),
							rupSet.getFaultSectionDataList(), constraint);
//					double probPaleoVisible = getProbPaleoVisible(rupMeanMag[rup]); // OLD UCERF2 version!
					// UCERF3 version!
					double probPaleoVisible = paleoProbabilityModel.getForSlip(rupMeanSlip[rup], distAlongRup);	
					double setVal = (relativePaleoRateWt * probPaleoVisible / constraint.getStdDevOfMeanRate());
					if (QUICK_GETS_SETS)
						A.setQuick(i, rup, setVal);
					else
						A.set(i, rup, setVal);
					if(D) numNonZeroElements++;			
				}
			}
			if (D) {
				System.out.println("Adding Paleo-Rate Constraints took "+getTimeStr(watch)+".");
				watch.reset();
				watch.start();
				System.out.println("Number of nonzero elements in A matrix = "+numNonZeroElements);
			}
		}

		
		// Rupture-Rate Constraint
		int rowIndex = numSlipRateConstraints + numPaleoRows;  // current A matrix row index - number of rows used for slip-rate and paleo-rate constraints (previous 2 constraints)
		if (config.getRelativeRupRateConstraintWt() > 0.0) {
			double relativeRupRateConstraintWt = config.getRelativeRupRateConstraintWt();
			double zeroRupRateConstraintWt = config.getRelativeRupRateConstraintWt()*aPrioriConstraintForZeroRatesWtFactor;  // This is the RupRateConstraintWt for ruptures not in UCERF2 
			if(D) System.out.println("\nAdding rupture-rate constraint to A matrix ...");
			double[] aPrioriRupConstraint = config.getA_PrioriRupConstraint();
			numNonZeroElements = 0;
			for(int rup=0; rup<numRuptures; rup++) {
				// If aPrioriConstrintforZeroRates=false, Only apply if rupture-rate is greater than 0, this will keep ruptures on faults not in UCERF2 from being minimized
				if (aPrioriRupConstraint[rup]>0) { 
					if (QUICK_GETS_SETS)
						A.setQuick(rowIndex,rup,relativeRupRateConstraintWt);
					else
						A.set(rowIndex,rup,relativeRupRateConstraintWt);
					d[rowIndex]=aPrioriRupConstraint[rup]*relativeRupRateConstraintWt;
					numNonZeroElements++; rowIndex++;
				}
				else if (aPrioriConstraintForZeroRates) {
					if (QUICK_GETS_SETS) 
						A.setQuick(rowIndex,rup,zeroRupRateConstraintWt);
					else
						A.set(rowIndex,rup,zeroRupRateConstraintWt);
					d[rowIndex]=aPrioriRupConstraint[rup]*zeroRupRateConstraintWt;
					numNonZeroElements++; rowIndex++;
				}	
			}
			if (D) {
				System.out.println("Adding rupture-rate Constraints took "+getTimeStr(watch)+".");
				watch.reset();
				watch.start();
				System.out.println("Number of nonzero elements in A matrix = "+numNonZeroElements);
			}
		}
		
		// Penalize Ruptures with small Coulomb weights
		if (config.getRelativeMinimizationConstraintWt() > 0.0) {
			double relativeMinimizationConstraintWt = config.getRelativeMinimizationConstraintWt();
			if(D) System.out.println("\nAdding minimization constraints to A matrix ...");
			numNonZeroElements = 0;
			for(int rup=0; rup<numRuptures; rup++) {
				if (QUICK_GETS_SETS)
					A.setQuick(rowIndex,rup,relativeMinimizationConstraintWt*improbabilityConstraint[rup]);
				else
					A.set(rowIndex,rup,relativeMinimizationConstraintWt*improbabilityConstraint[rup]);
				d[rowIndex]=0;
				numNonZeroElements++; rowIndex++;
			}
			if (D) {
				System.out.println("Adding Minimization Constraints took "+getTimeStr(watch)+".");
				watch.reset();
				watch.start();
				System.out.println("Number of nonzero elements in A matrix = "+numNonZeroElements);
			}
		}
		
		
		// Constrain Solution MFD to equal the Target MFD 
		// This is for equality constraints only -- inequality constraints must be
		// encoded into the A_ineq matrix instead since they are nonlinear
		if (config.getRelativeMagnitudeEqualityConstraintWt() > 0.0) {
			double relativeMagnitudeEqualityConstraintWt = config.getRelativeMagnitudeEqualityConstraintWt();
			List<MFD_InversionConstraint> mfdEqualityConstraints = config.getMfdEqualityConstraints();
			numNonZeroElements = 0;
			if(D) System.out.println("\nAdding " + mfdEqualityConstraints.size()
					+ " magnitude distribution equality constraints to A matrix ...");	
			
			// Find Parkfield M~6 ruptures (if we're excluding them)
			List<Integer> parkfieldRups = new ArrayList<Integer>();
			if (config.getRelativeParkfieldConstraintWt() > 0.0) {
				int parkfieldParentSectID = 32;
				List<Integer> potentialRups = rupSet.getRupturesForParentSection(parkfieldParentSectID);
				rupLoop:
					for (int i=0; i<potentialRups.size(); i++) {
						List<Integer> sects = rupSet.getSectionsIndicesForRup(potentialRups.get(i));
						// Make sure there are 6-8 subsections
						if (sects.size()<6 || sects.size()>8)
							continue rupLoop;
						// Make sure each section in rup is in Parkfield parent section
						for (int s=0; s<sects.size(); s++) {
							int parent = rupSet.getFaultSectionData(sects.get(s)).getParentSectionId();
							if (parent != parkfieldParentSectID)
								continue rupLoop;
						}
						parkfieldRups.add(potentialRups.get(i));
					}
			}
			
			// Loop over all MFD constraints in different regions
			for (int i=0; i < mfdEqualityConstraints.size(); i++) {
				double[] fractRupsInside = rupSet.getFractRupsInsideRegion(mfdEqualityConstraints.get(i).getRegion(), false);
				targetMagFreqDist=mfdEqualityConstraints.get(i).getMagFreqDist();	
				for(int rup=0; rup<numRuptures; rup++) {
					double mag = rupMeanMag[rup];
					double fractRupInside = fractRupsInside[rup];
					if (fractRupInside > 0 && mag>targetMagFreqDist.getMinX()-targetMagFreqDist.getDelta()/2.0 && mag<targetMagFreqDist.getMaxX()+targetMagFreqDist.getDelta()/2.0) {
						if (excludeParkfieldRupsFromMfdEqualityConstraints==false || !parkfieldRups.contains(rup)) {		
//							A.setQuick(rowIndex+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeEqualityConstraintWt * fractRupInside);
							if (QUICK_GETS_SETS){
								A.setQuick(rowIndex+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeEqualityConstraintWt * fractRupInside / targetMagFreqDist.getClosestY(mag));
								if (targetMagFreqDist.getClosestY(mag)==0) 
									A.setQuick(rowIndex+targetMagFreqDist.getClosestXIndex(mag),rup,0);
							}
							else
								A.set(rowIndex+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeEqualityConstraintWt * fractRupInside / targetMagFreqDist.getClosestY(mag));
							numNonZeroElements++;
						}
					}
				}		
				for (int xIndex=targetMagFreqDist.getClosestXIndex(rupSet.getMinMag()); xIndex<=targetMagFreqDist.getClosestXIndex(rupSet.getMaxMag()); xIndex++) {
//					d[rowIndex]=targetMagFreqDist.getY(xIndex)*relativeMagnitudeEqualityConstraintWt;
					d[rowIndex]=relativeMagnitudeEqualityConstraintWt;
					if (targetMagFreqDist.getY(xIndex)==0) d[rowIndex]=0;
					rowIndex++; 
				}	
			}
			if (D) {
				System.out.println("Adding MFD Equality Constraints took "+getTimeStr(watch)+".");
				watch.reset();
				watch.start();
				System.out.println("Number of nonzero elements in A matrix = "+numNonZeroElements);
			}
		}
		
		
		// Prepare MFD Inequality Constraint (not added to A matrix directly since it's nonlinear)
		if (config.getRelativeMagnitudeInequalityConstraintWt() > 0.0) {	
			double relativeMagnitudeInequalityConstraintWt = config.getRelativeMagnitudeInequalityConstraintWt();
			List<MFD_InversionConstraint> mfdInequalityConstraints = config.getMfdInequalityConstraints();
			int rowIndex_ineq = 0; 
			if(D) System.out.println("\nPreparing " + mfdInequalityConstraints.size()
					+ " magnitude inequality constraints ...");	
			
			// Loop over all MFD constraints in different regions
			for (int i=0; i < mfdInequalityConstraints.size(); i++) {
				double[] fractRupsInside = rupSet.getFractRupsInsideRegion(mfdInequalityConstraints.get(i).getRegion(), false);
				targetMagFreqDist=mfdInequalityConstraints.get(i).getMagFreqDist();	
				for(int rup=0; rup<numRuptures; rup++) {
					double mag = rupMeanMag[rup];
					double fractRupInside = fractRupsInside[rup];
					if (fractRupInside > 0 && mag>targetMagFreqDist.getMinX()-targetMagFreqDist.getDelta()/2.0 && mag<targetMagFreqDist.getMaxX()+targetMagFreqDist.getDelta()/2.0) {
//						A_ineq.setQuick(rowIndex_MFD+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeInequalityConstraintWt * fractRupInside);
						if (QUICK_GETS_SETS) {
							A_ineq.setQuick(rowIndex_ineq+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeInequalityConstraintWt * fractRupInside / targetMagFreqDist.getClosestY(mag));
							if (targetMagFreqDist.getClosestY(mag)==0) 
								A.setQuick(rowIndex_ineq+targetMagFreqDist.getClosestXIndex(mag),rup,0);
						}
						else
							A_ineq.set(rowIndex_ineq+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeInequalityConstraintWt * fractRupInside / targetMagFreqDist.getClosestY(mag));
					}
				}		
				for (int xIndex=targetMagFreqDist.getClosestXIndex(rupSet.getMinMag()); xIndex<=targetMagFreqDist.getClosestXIndex(rupSet.getMaxMag()); xIndex++) {
//					d_ineq[rowIndex_ineq]=targetMagFreqDist.getY(xIndex)*relativeMagnitudeInequalityConstraintWt;
					d_ineq[rowIndex_ineq]=relativeMagnitudeInequalityConstraintWt;
					if (targetMagFreqDist.getY(xIndex)==0) d_ineq[rowIndex_ineq]=0;
					rowIndex_ineq++; 
				}	
			}	
			if (D) {
				System.out.println("Preparing MFD Inequality Constraints took "+getTimeStr(watch)+".");
				watch.reset();
				watch.start();
			}
		}
		
		
		// MFD Smoothness Constraint - Constrain participation MFD to be uniform for each fault subsection
		if (config.getRelativeParticipationSmoothnessConstraintWt() > 0.0) {
			double relativeParticipationSmoothnessConstraintWt =
				config.getRelativeParticipationSmoothnessConstraintWt();
			if(D) System.out.println("\nAdding MFD participation smoothness constraints to A matrix ...");
			numNonZeroElements = 0;
			ArrayList<Integer> numRupsForMagBin = new ArrayList<Integer>();
			for (int sect=0; sect<numSections; sect++) {
				List<Integer> rupturesForSection = rupSet.getRupturesForSection(sect);
				
				// Find minimum and maximum rupture-magnitudes for that subsection
				double minMag = 10.0; double maxMag = 0.0;
				for (int rupIndex=0; rupIndex<rupturesForSection.size(); rupIndex++) {
					if (rupMeanMag[rupturesForSection.get(rupIndex)] < minMag)
						minMag = rupMeanMag[rupturesForSection.get(rupIndex)];
					if (rupMeanMag[rupturesForSection.get(rupIndex)] > maxMag)
						maxMag = rupMeanMag[rupturesForSection.get(rupIndex)];
				}
				if (minMag == 10.0 || minMag == 0.0) {
					System.out.println("NO RUPTURES FOR SECTION #"+sect);  
					continue;  // Skip this section, go on to next section constraint
				}
				
				// Find number of ruptures for this section for each magnitude bin & total number
				// of magnitude-bins with ruptures
				numRupsForMagBin.clear();
				double participationConstraintMagBinSize = config.getParticipationConstraintMagBinSize();
				int numNonzeroMagBins = 0;
				for (double m=minMag; m<maxMag; m=m+participationConstraintMagBinSize) {
					numRupsForMagBin.add(0);
					for (int rupIndex=0; rupIndex<rupturesForSection.size(); rupIndex++) {
						if (rupMeanMag[rupturesForSection.get(rupIndex)]>=m
								&& rupMeanMag[rupturesForSection.get(rupIndex)]<m+participationConstraintMagBinSize) 
							numRupsForMagBin.set(numRupsForMagBin.size()-1,
									numRupsForMagBin.get(numRupsForMagBin.size()-1)+1); // numRupsForMagBin(end)++
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
							numNonZeroElements++;
							if (rupMeanMag[rupturesForSection.get(rupIndex)]>=m
									&& rupMeanMag[rupturesForSection.get(rupIndex)]
									              <m+participationConstraintMagBinSize) {
								// Subtract off rate for this mag bin (difference between average rate per mag bin
								// & rate for this mag bin is set to 0)
								val -= relativeParticipationSmoothnessConstraintWt;
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
			if (D) {
				System.out.println("Adding Participation MFD Constraints took "+getTimeStr(watch)+".");
				watch.reset();
				watch.start();
				System.out.println("Number of nonzero elements in A matrix = "+numNonZeroElements);
			}
		}
		
		
		// Constraint solution moment to equal deformation-model moment
		if (config.getRelativeMomentConstraintWt() > 0.0) {
			double relativeMomentConstraintWt = config.getRelativeMomentConstraintWt();
			double totalMomentTarget = rupSet.getTotalReducedMomentRate();
			numNonZeroElements = 0;
			for (int rup=0; rup<numRuptures; rup++)  {
				if (QUICK_GETS_SETS)
					A.setQuick(rowIndex,rup,relativeMomentConstraintWt * MagUtils.magToMoment(rupMeanMag[rup]));
				else
					A.set(rowIndex,rup,relativeMomentConstraintWt * MagUtils.magToMoment(rupMeanMag[rup]));
				numNonZeroElements++;
			}
			d[rowIndex]=relativeMomentConstraintWt * totalMomentTarget;
			rowIndex++;
			System.out.println("Adding Moment Constraint took "+getTimeStr(watch)+".");
			watch.reset();
			watch.start();
			System.out.println("Number of nonzero elements in A matrix = "+numNonZeroElements);
		}
		
		
		// Constraint rupture-rate for M~6 Parkfield earthquakes
		// The Parkfield eqs are defined as rates of 6, 7, and 8 subsection ruptures in the Parkfield parent section (which has 8 subsections in total)
		// THIS CONSTRAINT WILL NOT WORK IF SUBSECTIONS DRASTICALLY CHANGE IN SIZE OR IF PARENT-SECT-IDS CHANGE!
		if (config.getRelativeParkfieldConstraintWt() > 0.0) {
			if(D) System.out.println("\nAdding Parkfield rupture-rate constraints to A matrix ...");
			double relativeParkfieldConstraintWt = config.getRelativeParkfieldConstraintWt();
			double ParkfieldMeanRate = 1.0/25.0; // Bakun et al. (2005)
			int parkfieldParentSectID = 32;
			
			// Find Parkfield M~6 ruptures
			List<Integer> potentialRups = rupSet.getRupturesForParentSection(parkfieldParentSectID);
			List<Integer> parkfieldRups = new ArrayList<Integer>();
			rupLoop:
			for (int i=0; i<potentialRups.size(); i++) {
				List<Integer> sects = rupSet.getSectionsIndicesForRup(potentialRups.get(i));
				// Make sure there are 6-8 subsections
				if (sects.size()<6 || sects.size()>8)
					continue rupLoop;
				// Make sure each section in rup is in Parkfield parent section
				for (int s=0; s<sects.size(); s++) {
					int parent = rupSet.getFaultSectionData(sects.get(s)).getParentSectionId();
					if (parent != parkfieldParentSectID)
						continue rupLoop;
				}
				parkfieldRups.add(potentialRups.get(i));
			}
			if (D) System.out.println("Number of M~6 Parkfield rups = "+parkfieldRups.size());
			
			// Put together A, d elements
			numNonZeroElements = 0;
			for (int r=0; r<parkfieldRups.size(); r++)  {
				int rup = parkfieldRups.get(r);
				if (QUICK_GETS_SETS) 
					A.setQuick(rowIndex,rup,relativeParkfieldConstraintWt);
				else
					A.set(rowIndex,rup,relativeParkfieldConstraintWt);
				numNonZeroElements++;
			}
			d[rowIndex]=relativeParkfieldConstraintWt * ParkfieldMeanRate;
			rowIndex++;
			System.out.println("Adding Parkfield Constraint took "+getTimeStr(watch)+".");
			watch.reset();
			watch.start();
			System.out.println("Number of nonzero elements in A matrix = "+numNonZeroElements+"\n");
		}
		
		
		if (minimumRuptureRates != null) {
			// apply the minimum rupture rates
			if (D) System.out.println("Applying minimum rupture rates.");
			
			// This is the offset data vector: d = d-A*minimumRuptureRates
			A.forEachNonZero(new IntIntDoubleFunction() {
				
				@Override
				public synchronized double apply(int row, int col, double val) {
					d[row] -= val * minimumRuptureRates[col];
					return val;
				}
			});
			if (d_ineq != null) {
				// This is the offset data vector for MFD inequality constraint:
				// d_ineq = d_ineq-A*minimumRuptureRates
				
				A_ineq.forEachNonZero(new IntIntDoubleFunction() {
					
					@Override
					public synchronized double apply(int row, int col, double val) {
						d_ineq[row] -= val * minimumRuptureRates[col];
						return val;
					}
				});
			}
			
			// also adjust the initial solution by the minimum rates
			initial = Arrays.copyOf(initial, numRuptures);
			for (int i=0; i<numRuptures; i++) {
				double adjustedVal = initial[i] - minimumRuptureRates[i];
				if (adjustedVal < 0)
					adjustedVal = 0;
				initial[i] = adjustedVal;
			}
			
			if (D) {
				System.out.println("Applying minimum rupture rates took "+getTimeStr(watch)+".");
				watch.reset();
				watch.start();
			}
		}
		if (D) {
			watch.stop();
			watch_total.stop();
			System.out.println("Generating inputs took "+getTimeStr(watch_total)+".");
		}
	}
	
	public double[] adjustSolutionForMinimumRates(double[] solution) {
		return adjustSolutionForMinimumRates(solution, minimumRuptureRates);
	}
	
	public static double[] adjustSolutionForMinimumRates(double[] solution, double[] minimumRuptureRates) {
		solution = Arrays.copyOf(solution, solution.length);
		
		if (minimumRuptureRates != null) {
			Preconditions.checkState(minimumRuptureRates.length == solution.length,
					"minimum rates size mismatch!");
			for (int i=0; i<solution.length; i++) {
				solution[i] = solution[i] + minimumRuptureRates[i];
			}
		}
		
		return solution;
	}
	
	/**
	 * This returns the normalized distance along a rupture that a paleoseismic trench
	 * is located (Glenn's x/L).  It is between 0 and 0.5.
	 * This currently puts the trench in the middle of the subsection.
	 * We need this for the UCERF3 probability of detecting a rupture in a trench.
	 * @return
	 */
	public static double getDistanceAlongRupture(List<Integer> sectsInRup,
			List<FaultSectionPrefData> sectionDataList, PaleoRateConstraint constraint) {
		double distanceAlongRup = 0;
		
		int constraintIndex = constraint.getSectionIndex();
		double totalLength = 0;
		double lengthToRup = 0;
		boolean reachConstraintLoc = false;
		
		// Find total length (km) of fault trace and length (km) from one end to the paleo trench location
		for (int i=0; i<sectsInRup.size(); i++) {
			int sectIndex = sectsInRup.get(i);
			double sectLength = sectionDataList.get(i).getFaultTrace().getTraceLength();
			totalLength+=sectLength;
			if (sectIndex == constraintIndex) {
				reachConstraintLoc = true;
				// We're putting the trench in the middle of the subsection for now
				lengthToRup+=sectLength/2;
			}
			// We haven't yet gotten to the trench subsection so keep adding to lengthToRup
			if (reachConstraintLoc == false)
				lengthToRup+=sectLength;
		}
		
		if (!reachConstraintLoc) // check to make sure we came across the trench subsection in the rupture
			throw new IllegalStateException("Paleo site subsection was not included in rupture subsections");
		
		// Normalized distance along the rainbow (Glenn's x/L) - between 0 and 1
		distanceAlongRup = lengthToRup/totalLength;
		// Adjust to be between 0 and 0.5 (since rainbow is symmetric about 0.5)
		if (distanceAlongRup>0.5)
			distanceAlongRup=1-distanceAlongRup;
		
		return distanceAlongRup;
	}
	
	private static String getTimeStr(Stopwatch watch) {
		return (float)(watch.elapsedMillis() / 1000d)+" seconds";
	}
	
	private static DoubleMatrix2D buildMatrix(Class<? extends DoubleMatrix2D> clazz, int rows, int cols) {
		if (clazz == null || clazz.equals(SparseDoubleMatrix2D.class))
			// default
			return new SparseDoubleMatrix2D(rows, cols);
		else if (clazz.equals(SparseRCDoubleMatrix2D.class))
			return new SparseRCDoubleMatrix2D(rows, cols);
		else if (clazz.equals(SparseCCDoubleMatrix2D.class))
			return new SparseCCDoubleMatrix2D(rows, cols);
		else
			throw new IllegalArgumentException("Unknown matrix type: "+clazz);
	}
	
	public void columnCompress() {
		A = getColumnCompressed(A);
		if (A_ineq != null)
			A_ineq = getColumnCompressed(A_ineq);
	}
	
	private static SparseCCDoubleMatrix2D getColumnCompressed(DoubleMatrix2D mat) {
		if (mat instanceof SparseCCDoubleMatrix2D)
			return (SparseCCDoubleMatrix2D)mat;
		if (mat instanceof SparseRCDoubleMatrix2D)
			return ((SparseRCDoubleMatrix2D)mat).getColumnCompressed();
		if (mat instanceof SparseDoubleMatrix2D)
			return ((SparseDoubleMatrix2D)mat).getColumnCompressed(true);
		throw new RuntimeException("Can't column compress matrix: "+mat);
	}
	
	public void writeZipFile(File file) throws IOException {
		File tempDir = FileUtils.createTempDir();
		writeZipFile(file, FileUtils.createTempDir(), true);
		tempDir.delete();
	}
	
	/**
	 * Writes the inputs to the given zip file, storing the binary files
	 * in the given directory and optionally cleaning up (deleting them)
	 * when done.
	 * 
	 * @param file
	 * @param storeDir
	 * @param cleanup
	 */
	public void writeZipFile(File zipFile, File storeDir, boolean cleanup) throws IOException {
		if(D) System.out.println("Saving to files...");
		ArrayList<String> fileNames = new ArrayList<String>();
		
		fileNames.add("d.bin");			
		MatrixIO.doubleArrayToFile(d, new File(storeDir, "d.bin"));
		if(D) System.out.println("d.bin saved");
		
		fileNames.add("a.bin");			
		MatrixIO.saveSparse(A, new File(storeDir, "a.bin"));
		if(D) System.out.println("a.bin saved");
		
		fileNames.add("initial.bin");	
		MatrixIO.doubleArrayToFile(initial, new File(storeDir, "initial.bin"));
		if(D) System.out.println("initial.bin saved");
		
		if (d_ineq != null) {
			fileNames.add("d_ineq.bin");	
			MatrixIO.doubleArrayToFile(d_ineq, new File(storeDir, "d_ineq.bin"));
			if(D) System.out.println("d_ineq.bin saved");
		}
		
		if (A_ineq != null) {
			fileNames.add("a_ineq.bin");	
			MatrixIO.saveSparse(A_ineq,new File(storeDir, "a_ineq.bin"));
			if(D) System.out.println("a_ineq.bin saved");
		}
		
		if (minimumRuptureRates != null) {
			fileNames.add("minimumRuptureRates.bin");	
			MatrixIO.doubleArrayToFile(minimumRuptureRates,new File(storeDir, "minimumRuptureRates.bin"));
			if(D) System.out.println("minimumRuptureRates.bin saved");
		}
		
		CSVFile<String> rangeCSV = new CSVFile<String>(true);
		for (int i=0; i<rangeEndRows.size(); i++)
			rangeCSV.addLine(rangeEndRows.get(i)+"", rangeNames.get(i));
		fileNames.add("energyRanges.csv");
		rangeCSV.writeToFile(new File(storeDir, "energyRanges.csv"));
		if(D) System.out.println("energyRanges.csv saved");
		
		FileUtils.createZipFile(zipFile.getAbsolutePath(), storeDir.getAbsolutePath(), fileNames);
		if(D) System.out.println("Zip file saved");
		if (cleanup) {
			if(D) System.out.println("Cleaning up");
			for (String fileName : fileNames) {
				new File(storeDir, fileName).delete();
			}
		}
	}

	public FaultSystemRupSet getRupSet() {
		return rupSet;
	}

	public void setRupSet(FaultSystemRupSet rupSet) {
		this.rupSet = rupSet;
	}

	public InversionConfiguration getConfig() {
		return config;
	}

	public List<PaleoRateConstraint> getPaleoRateConstraints() {
		return paleoRateConstraints;
	}

	public double[] getImprobabilityConstraint() {
		return improbabilityConstraint;
	}

	public PaleoProbabilityModel getPaleoProbabilityModel() {
		return paleoProbabilityModel;
	}

	public DoubleMatrix2D getA() {
		return A;
	}

	public DoubleMatrix2D getA_ineq() {
		return A_ineq;
	}

	public double[] getD() {
		return d;
	}

	public double[] getD_ineq() {
		return d_ineq;
	}

	public double[] getInitial() {
		return initial;
	}

	public double[] getMinimumRuptureRates() {
		return minimumRuptureRates;
	}

	public List<Integer> getRangeEndRows() {
		return rangeEndRows;
	}

	public List<String> getRangeNames() {
		return rangeNames;
	}

	public boolean isAPrioriConstraintForZeroRates() {
		return aPrioriConstraintForZeroRates;
	}

	public void setAPrioriConstraintForZeroRates(
			boolean aPrioriConstraintForZeroRates) {
		this.aPrioriConstraintForZeroRates = aPrioriConstraintForZeroRates;
	}

}
