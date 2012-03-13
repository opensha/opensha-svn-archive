package scratch.UCERF3.inversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		
		int numPaleoRows = (int)Math.signum(config.getRelativePaleoRateWt())*paleoRateConstraints.size();
		if(D) System.out.println("Number of paleo section-rate constraints: "+numPaleoRows);
		numRows += numPaleoRows;
		
		if (config.getRelativeRupRateConstraintWt() > 0.0) {
			double[] relativeRupRateConstraintWt = config.getA_PrioriRupConstraint();
			int numRupRateRows = 0;
			for (int i=0; i<numRuptures; i++) 
				if (relativeRupRateConstraintWt[i]>0) 	numRupRateRows++;
			if(D) System.out.println("Number of rupture-rate constraints: "+numRupRateRows);
			numRows += numRupRateRows;
		}
		
		int numMinimizationRows = (int)Math.signum(config.getRelativeMinimizationConstraintWt())*numRuptures;
		if(D) System.out.println("Number of minimization constraints: "+numMinimizationRows);
		numRows += numMinimizationRows;
		
		IncrementalMagFreqDist targetMagFreqDist=null;
		if (config.getRelativeMagnitudeEqualityConstraintWt() > 0.0) {
			// TODO: RIGHT NOW THIS ASSUMES THE MAGNITUDE CONSTRAINT WEIGHT MUST
			// BE THE SAME FOR ALL MAG-DIST CONSTRAINTS
			int totalNumMagFreqConstraints = 0;
			for (MFD_InversionConstraint constr : config.getMfdEqualityConstraints()) {
				targetMagFreqDist=constr.getMagFreqDist();
				totalNumMagFreqConstraints += targetMagFreqDist.getNum();
				// add number of rows used for magnitude distribution constraint
				numRows=numRows+targetMagFreqDist.getNum();
			}
			if(D) System.out.println("Number of magnitude-distribution equality constraints: "
					+totalNumMagFreqConstraints);
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
		}
		if (config.getRelativeMomentConstraintWt() > 0.0) {
			numRows++;
			if(D) System.out.println("Number of Moment constraints: 1");
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
				// add number of rows used for magnitude distribution constraint
				numMFDRows+=targetMagFreqDist.getNum();
			}
			A_ineq = buildMatrix(clazz, numMFDRows, numRuptures); // (A_MFD * x <= d_MFD)
			d_ineq = new double[numMFDRows];							
			if(D) System.out.println("Number of magnitude-distribution inequality constraints (not in A matrix): "
					+config.getMfdInequalityConstraints().size());
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

			
		// Constrain Solution MFD to equal the Target MFD 
		// This is for equality constraints only -- inequality constraints must be
		// encoded into the A_ineq matrix instead since they are nonlinear
		
		// number of rows used for slip-rate and paleo-rate constraints
		int rowIndex = numSlipRateConstraints + numPaleoRows;
		if (config.getRelativeMagnitudeEqualityConstraintWt() > 0.0) {
			double relativeMagnitudeEqualityConstraintWt = config.getRelativeMagnitudeEqualityConstraintWt();
			List<MFD_InversionConstraint> mfdEqualityConstraints = config.getMfdEqualityConstraints();
			numNonZeroElements = 0;
			if(D) System.out.println("\nAdding " + mfdEqualityConstraints.size()
					+ " magnitude distribution equality constraints to A matrix ...");	
			
			// make the matrix of the fraction of each rupture inside each region: fractRupsInsideMFD_Regions
			double[][] fractRupsInsideMFD_Regions =
				rupSet.computeFractRupsInsideMFD_Regions(mfdEqualityConstraints);
			
			// Loop over all MFD constraints in different regions
			for (int i=0; i < mfdEqualityConstraints.size(); i++) {
				targetMagFreqDist=mfdEqualityConstraints.get(i).getMagFreqDist();	
				for(int rup=0; rup<numRuptures; rup++) {
					double mag = rupMeanMag[rup];
					double fractRupInside = fractRupsInsideMFD_Regions[i][rup];
					if (fractRupInside > 0) {
//						A.setQuick(rowIndex+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeEqualityConstraintWt * fractRupInside);
						if (QUICK_GETS_SETS)
							A.setQuick(rowIndex+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeEqualityConstraintWt * fractRupInside / targetMagFreqDist.getClosestY(mag));
						else
							A.set(rowIndex+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeEqualityConstraintWt * fractRupInside / targetMagFreqDist.getClosestY(mag));
						numNonZeroElements++;
					}
				}		
				for (double m=targetMagFreqDist.getMinX(); m<=targetMagFreqDist.getMaxX(); m=m+targetMagFreqDist.getDelta()) {
//					d[rowIndex]=targetMagFreqDist.getY(m)*relativeMagnitudeEqualityConstraintWt;
					d[rowIndex]=relativeMagnitudeEqualityConstraintWt;
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
			
			// make the matrix of the fraction of each rupture inside each region: fractRupsInsideMFD_Regions
			double[][] fractRupsInsideMFD_Regions =
				rupSet.computeFractRupsInsideMFD_Regions(mfdInequalityConstraints);
			
			// Loop over all MFD constraints in different regions
			for (int i=0; i < mfdInequalityConstraints.size(); i++) {
				targetMagFreqDist=mfdInequalityConstraints.get(i).getMagFreqDist();	
				for(int rup=0; rup<numRuptures; rup++) {
					double mag = rupMeanMag[rup];
					double fractRupInside = fractRupsInsideMFD_Regions[i][rup];
					if (fractRupInside > 0) {
//						A_ineq.setQuick(rowIndex_MFD+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeInequalityConstraintWt * fractRupInside);
						if (QUICK_GETS_SETS)
							A_ineq.setQuick(rowIndex_ineq+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeInequalityConstraintWt * fractRupInside / targetMagFreqDist.getClosestY(mag));
						else
							A_ineq.set(rowIndex_ineq+targetMagFreqDist.getClosestXIndex(mag),rup,relativeMagnitudeInequalityConstraintWt * fractRupInside / targetMagFreqDist.getClosestY(mag));
					}
				}		
				for (double m=targetMagFreqDist.getMinX(); m<=targetMagFreqDist.getMaxX(); m=m+targetMagFreqDist.getDelta()) {
	//				d_ineq[rowIndex_MFD]=targetMagFreqDist.getY(m)*relativeMagnitudeInequalityConstraintWt;
					d_ineq[rowIndex_ineq]=relativeMagnitudeInequalityConstraintWt;
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
		
		
		// Constrain Rupture Rate Solution to approximately equal aPrioriRupConstraint
		if (config.getRelativeRupRateConstraintWt() > 0.0) {
			double relativeRupRateConstraintWt = config.getRelativeRupRateConstraintWt();
			if(D) System.out.println("\nAdding rupture-rate constraint to A matrix ...");
			double[] aPrioriRupConstraint = config.getA_PrioriRupConstraint();
			numNonZeroElements = 0;
			for(int rup=0; rup<numRuptures; rup++) {
				// Only apply if rupture-rate is greater than 0, this will keep ruptures on faults not in UCERF2 from being minimized
				if (aPrioriRupConstraint[rup]>0) { 
					if (QUICK_GETS_SETS)
						A.setQuick(rowIndex,rup,relativeRupRateConstraintWt);
					else
						A.set(rowIndex,rup,relativeRupRateConstraintWt);
					d[rowIndex]=aPrioriRupConstraint[rup]*relativeRupRateConstraintWt;
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
		
		
		// Constraint solution moment to equal deformation-model moment
		if (config.getRelativeMomentConstraintWt() > 0.0) {
			double relativeMomentConstraintWt = config.getRelativeMomentConstraintWt();
			double totalMomentTarget = rupSet.getTotalSubseismogenicReducedMomentRate();
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
	public void writeZipFile(File file, File storeDir, boolean cleanup) throws IOException {
		writeZipFile(file, storeDir, cleanup, A, d, initial, A_ineq, d_ineq, minimumRuptureRates);
	}
	
	public static void writeZipFile(File file, File storeDir, boolean cleanup,
			DoubleMatrix2D A, double[] d, double[] initial,
			DoubleMatrix2D A_ineq, double[] d_ineq)
					throws IOException {
		writeZipFile(file, storeDir, cleanup, A, d, initial, A_ineq, d_ineq, null);
	}
	
	public static void writeZipFile(
			File zipFile, File storeDir, boolean cleanup,
			DoubleMatrix2D A, double[] d, double[] initial,
			DoubleMatrix2D A_ineq, double[] d_ineq, double[] minimumRuptureRates)
					throws IOException {
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

}
