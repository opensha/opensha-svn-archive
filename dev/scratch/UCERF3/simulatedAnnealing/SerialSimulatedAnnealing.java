package scratch.UCERF3.simulatedAnnealing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.time.StopWatch;
import org.opensha.commons.util.DataUtils;

import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.IterationCompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.params.CoolingScheduleType;
import scratch.UCERF3.simulatedAnnealing.params.GenerationFunctionType;
import scratch.UCERF3.simulatedAnnealing.params.NonnegativityConstraintType;
import scratch.UCERF3.utils.MFD_InversionConstraint;

import com.google.common.base.Preconditions;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;
import cern.colt.matrix.tdouble.impl.SparseCCDoubleMatrix2D;
import edu.emory.mathcs.csparsej.tdouble.Dcs_common.Dcs;

/**
 * 
 * @author Morgan Page and Kevin Milner
 *
 */


public class SerialSimulatedAnnealing implements SimulatedAnnealing {

	protected static final String XML_METADATA_NAME = "SimulatedAnnealing";

	protected final static boolean D = true;  // for debugging
	private final static boolean COLUMN_MULT_SPEEDUP_DEBUG = false;

	private static CoolingScheduleType COOLING_FUNC_DEFAULT = CoolingScheduleType.FAST_SA;
	private CoolingScheduleType coolingFunc = COOLING_FUNC_DEFAULT;
	
	private static NonnegativityConstraintType NONNEGATIVITY_CONST_DEFAULT =
		NonnegativityConstraintType.LIMIT_ZERO_RATES;
	private NonnegativityConstraintType nonnegativityConstraintAlgorithm = NonnegativityConstraintType.PREVENT_ZERO_RATES;
	
	private static GenerationFunctionType PERTURB_FUNC_DEFAULT = GenerationFunctionType.UNIFORM_NO_TEMP_DEPENDENCE;
	private GenerationFunctionType perturbationFunc = PERTURB_FUNC_DEFAULT;
	
	private DoubleMatrix2D A, A_MFD;
	private double[] d, d_MFD;
	private double relativeSmoothnessWt, relativeMagnitudeInequalityConstraintWt;
	
	private int nCol;
	private int nRow;
	
	private double[] xbest;  // best model seen so far
	private double[] perturb; // perturbation to current model
	private double[] misfit_best, misfit_ineq_best; // misfit between data and synthetics
	
	private double Ebest;
	
	private Random r = new Random();

	public SerialSimulatedAnnealing(DoubleMatrix2D A, double[] d, double[] initialState) {
		this(A, d, initialState, 0, 0, null, null);
	}
	
	public SerialSimulatedAnnealing(DoubleMatrix2D A, double[] d, double[] initialState, double relativeSmoothnessWt, 
			double relativeMagnitudeInequalityConstraintWt, DoubleMatrix2D A_MFD,  double[] d_MFD) {
		this.relativeSmoothnessWt=relativeSmoothnessWt;
		this.relativeMagnitudeInequalityConstraintWt=relativeMagnitudeInequalityConstraintWt;
		this.A_MFD=A_MFD;
		this.d_MFD=d_MFD;
		
		setup(A, d, initialState);
	}
	
	private void setup(DoubleMatrix2D A, double[] d, double[] initialState) {
		Preconditions.checkNotNull(A, "A matrix cannot be null");
		Preconditions.checkNotNull(d, "d matrix cannot be null");
		Preconditions.checkNotNull(initialState, "initial state cannot be null");
		
		nRow = A.rows();
		nCol = A.columns();
		Preconditions.checkArgument(nRow > 0, "nRow of A must be > 0");
		Preconditions.checkArgument(nCol > 0, "nCol of A must be > 0");
		
		Preconditions.checkArgument(d.length == nRow, "d matrix must be same lenth as nRow of A");
		Preconditions.checkArgument(initialState.length == nCol, "initial state must be same lenth as nCol of A");
		
		this.A = A;
		this.d = d;
		

		xbest = Arrays.copyOf(initialState, nCol);  // best model seen so far
		perturb = new double[nCol]; // perturbation to current model
		
		misfit_best = new double[nRow];
		calculateMisfit(A, d, null, xbest, -1, Double.NaN, misfit_best);
		if (relativeMagnitudeInequalityConstraintWt > 0.0) {
			misfit_ineq_best = new double[d_MFD.length];
			calculateMisfit(A_MFD, d_MFD, null, xbest, -1, Double.NaN, misfit_ineq_best);
		}
		
		Ebest = calculateEnergy(xbest, misfit_best, misfit_ineq_best);
	}
	
	@Override
	public void setCalculationParams(CoolingScheduleType coolingFunc,
			NonnegativityConstraintType nonnegativeityConstraintAlgorithm,
			GenerationFunctionType perturbationFunc) {
		this.coolingFunc = coolingFunc;
		this.nonnegativityConstraintAlgorithm = nonnegativeityConstraintAlgorithm;
		this.perturbationFunc = perturbationFunc;
	}
	
	@Override
	public CoolingScheduleType getCoolingFunc() {
		return coolingFunc;
	}

	@Override
	public void setCoolingFunc(CoolingScheduleType coolingFunc) {
		this.coolingFunc = coolingFunc;
	}

	@Override
	public NonnegativityConstraintType getNonnegativeityConstraintAlgorithm() {
		return nonnegativityConstraintAlgorithm;
	}

	@Override
	public void setNonnegativeityConstraintAlgorithm(
			NonnegativityConstraintType nonnegativeityConstraintAlgorithm) {
		this.nonnegativityConstraintAlgorithm = nonnegativeityConstraintAlgorithm;
	}

	@Override
	public GenerationFunctionType getPerturbationFunc() {
		return perturbationFunc;
	}

	@Override
	public void setPerturbationFunc(GenerationFunctionType perturbationFunc) {
		this.perturbationFunc = perturbationFunc;
	}

	@Override
	public double[] getBestSolution() {
		return xbest;
	}
	
	@Override
	public double getBestEnergy() {
		return Ebest;
	}
	
	@Override
	public double[] getBestMisfit() {
		return misfit_best;
	}

	@Override
	public double[] getBestInequalityMisfit() {
		return misfit_ineq_best;
	}
	
	@Override
	public void setResults(double Ebest, double[] xbest, double[] misfit_best, double[] misfit_ineq_best) {
		this.Ebest = Ebest;
		this.xbest = Arrays.copyOf(xbest, xbest.length);
		this.misfit_best = Arrays.copyOf(misfit_best, misfit_best.length);
		if (misfit_ineq_best == null)
			this.misfit_ineq_best = null;
		else
			this.misfit_ineq_best = Arrays.copyOf(misfit_ineq_best, misfit_ineq_best.length);
	}
	
	@Override
	public void setResults(double Ebest, double[] xbest) {
		setResults(Ebest, xbest, null, null);
	}
	
	private static void calculateMisfit(DoubleMatrix2D mat, double[] data, double[] prev_misfit,
			double[] solution, int perturbCol, double perturbation, double[] misfit) {
		if (mat instanceof SparseCCDoubleMatrix2D && perturbCol >= 0 && prev_misfit != null) {
//			misfit = Arrays.copyOf(prev_misfit, prev_misfit.length);
			System.arraycopy(prev_misfit, 0, misfit, 0, prev_misfit.length);
			Dcs dcs = ((SparseCCDoubleMatrix2D)mat).elements();
			final int[] rowIndexesA = dcs.i;
			final int[] columnPointersA = dcs.p;
			final double[] valuesA = dcs.x;
			
			int low = columnPointersA[perturbCol];
			for (int k = columnPointersA[perturbCol + 1]; --k >= low;) {
				int row = rowIndexesA[k];
				double value = valuesA[k];
				misfit[row] += value * perturbation;
			}
		} else {
			DoubleMatrix1D sol_clone = new DenseDoubleMatrix1D(solution);
			
			DenseDoubleMatrix1D syn = new DenseDoubleMatrix1D(mat.rows());
			mat.zMult(sol_clone, syn);
			
			for (int i = 0; i < mat.rows(); i++) {
				misfit[i] = syn.get(i) - data[i];  // misfit between synthetics and data
			}
		}
	}
	
	protected synchronized double calculateEnergy(double[] solution, double[] misfit, double[] misfit_ineq) {
		
		// Do forward problem for new perturbed model (calculate synthetics)
		
		double Enew = 0;
		for (int i = 0; i < nRow; i++) {
			// NOTE: it is important that we loop over nRow and not the actual misfit array
			// as it may be larger than nRow (for efficiency and less array copies)
			Enew += Math.pow(misfit[i], 2);  // L2 norm of misfit vector
		}
		Preconditions.checkState(!Double.isNaN(Enew), "energy is NaN!");
		
		// Add smoothness constraint misfit (nonlinear) to energy (this is the entropy-maximization constraint)
		if (relativeSmoothnessWt > 0.0) { 
			double totalEntropy=0;
			double entropyConstant=500;
			for (int rup=0; rup<nCol; rup++) {
				if (solution[rup]>0)
					totalEntropy -= entropyConstant*solution[rup]*Math.log(entropyConstant*solution[rup]);
			}
			if (totalEntropy==0) {
				System.out.println("ZERO ENTROPY!");
				totalEntropy=0.0001;
			}
			if (totalEntropy<0) {
				throw new IllegalStateException("NEGATIVE ENTROPY!");
			}
			Enew += relativeSmoothnessWt * (1 / totalEntropy); // High entropy => low misfit
		}
		
		
		// Add MFD inequality constraint misfit (nonlinear) to energy 
		if (relativeMagnitudeInequalityConstraintWt > 0.0) {
			for (int i = 0; i < d_MFD.length; i++) {
				// NOTE: it is important that we loop over d_MFD.length and not the actual misfit array
				// as it may be larger than nRow (for efficiency and less array copies)
				if (misfit_ineq[i] > 0.0) // This makes it an INEQUALITY constraint (Target MFD is an UPPER bound)
					Enew += Math.pow(misfit_ineq[i], 2);  // L2 norm of misfit vector
			}
		}
		
		Preconditions.checkState(!Double.isNaN(Enew), "Enew is NaN!");
		
		return Enew;
	}
	
	@Override
	public synchronized long iterate(long numIterations) {
		return iterate(new IterationCompletionCriteria(numIterations));
	}
	
	@Override
	public synchronized long iterate(CompletionCriteria completion) {
		return iterate(0, completion);
	}

	@Override
	public synchronized long iterate(long startIter, CompletionCriteria criteria) {
		StopWatch watch = new StopWatch();
		watch.start();
		
		if(D) System.out.println("Solving inverse problem with simulated annealing ... \n");
		if(D) System.out.println("Cooling Function: " + coolingFunc.name());
		if(D) System.out.println("Perturbation Function: " + perturbationFunc.name());
		if(D) System.out.println("Nonnegativity Constraint: " + nonnegativityConstraintAlgorithm.name());
		if(D) System.out.println("Completion Criteria: " + criteria);
		
		double Enew;
		double P;
		long iter=startIter+1;
		int index;
		double[] x = Arrays.copyOf(xbest, xbest.length);
		double E = Ebest;
		double T;
		// this is where we store previous misfits
		double[] misfit = Arrays.copyOf(misfit_best, misfit_best.length);
		// this is where we store new candidate misfits
		double[] misfit_new = new double[misfit_best.length];
		double[] misfit_ineq = null;
		double[] misfit_ineq_new = null;
		if (relativeMagnitudeInequalityConstraintWt > 0) {
			misfit_ineq = Arrays.copyOf(misfit_ineq_best, misfit_ineq_best.length);
			misfit_ineq_new = new double[misfit_ineq_best.length];
		}

		// we do iter-1 because iter here is 1-based, not 0-based
		while (!criteria.isSatisfied(watch, iter-1, Ebest)) {

			// Find current simulated annealing "temperature" based on chosen cooling schedule
			switch (coolingFunc) {
			case CLASSICAL_SA:
				T = 1/Math.log( (double) iter); // classical SA cooling schedule (Geman and Geman, 1984) (slow but ensures convergence)
				break;
			case FAST_SA:
				T = 1 / (double) iter;  // fast SA cooling schedule (Szu and Hartley, 1987) (recommended)
				break;
			case VERYFAST_SA:
				T = Math.exp(-( (double) iter - 1)); // very fast SA cooling schedule (Ingber, 1989)  (= 0 to machine precision for high iteration #)
				break;
			case LINEAR:
//				T = 1 - (iter / numIterations);
				T = 1 - (iter / 100000);  // need to fix this -- for now just putting in numIterations by hand
				break;
			default:
				throw new IllegalStateException("It's impossible to get here, as long as all cooling schedule enum cases are stated above!");
			}

			if (D) {  // print out convergence info every so often
				if ((iter-1) % 1000 == 0) { 
					System.out.println("Iteration # " + iter);
					System.out.println("Lowest energy found = " + Ebest);
//					System.out.println("Current energy = " + E);
				}
			}

			// Index of model to randomly perturb
			index = (int)(r.nextDouble() * (double)nCol); // casting as int takes the floor


			// How much to perturb index (some perturbation functions are a function of T)	
			perturb[index] = getPerturbation(perturbationFunc, T);  

			// Apply then nonnegativity constraint -- make sure perturbation doesn't make the rate negative
			switch (nonnegativityConstraintAlgorithm) {
			case TRY_ZERO_RATES_OFTEN: // sets rate to zero if they are perturbed to negative values 
				// This way will result in many zeros in the solution, 
				// which may be desirable since global minimum is likely near a boundary
				if (x[index] == 0) { // if that rate was already zero do not keep it at zero
					while (x[index] + perturb[index] < 0) 
						perturb[index] = getPerturbation(perturbationFunc,T);
				} else { // if that rate was not already zero, and it goes negative, set it equal to zero
					if (x[index] + perturb[index] < 0) 
						perturb[index] = -x[index];
				}
				break;
			case LIMIT_ZERO_RATES:    // re-perturb rates if they are perturbed to negative values 
				// This way will result in not a lot of zero rates (none if numIterations >> length(x)),
				// which may be desirable if we don't want a lot of zero rates
				while (x[index] + perturb[index] < 0) {
					perturb[index] = getPerturbation(perturbationFunc,T);	
				}
				break;
			case PREVENT_ZERO_RATES:    // Only perturb rates to positive values; any perturbations of zero rates MUST be accepted.
				// Final model will only have zero rates if rate was never selected to be perturbed AND starting model contains zero rates.
				if (x[index]!=0) {
					perturb[index] = (r.nextDouble() -0.5) * 2 * x[index]; 	
					}
				else {
					perturb[index] = (r.nextDouble()) * 0.00000001;
				}
				break;
			default:
				throw new IllegalStateException("You missed a Nonnegativity Constraint Algorithm type.");
			}
			x[index] += perturb[index];
			
			// calculate new misfit vectors
			calculateMisfit(A, d, misfit, x, index, perturb[index], misfit_new);
			if (relativeMagnitudeInequalityConstraintWt > 0)
				calculateMisfit(A_MFD, d_MFD, misfit_ineq, x, index, perturb[index], misfit_ineq_new);

			// Calculate "energy" of new model (high misfit -> high energy)
//			Enew = calculateMisfit(xnew);
			Enew = calculateEnergy(x, misfit_new, misfit_ineq_new);
			
			if (D && COLUMN_MULT_SPEEDUP_DEBUG && (iter-1) % 100000 == 0 && iter > 1) {
				// lets make sure that the energy calculation was correct with the column speedup
				// only do this if debug is enabled, and do it every 100 iterations
				
				// calculate it the "slow" way
				double[] comp_misfit_new = new double[misfit_new.length];
				calculateMisfit(A, d, null, x, -1, Double.NaN, comp_misfit_new);
				double[] comp_misfit_ineq_new = null;
				if (relativeMagnitudeInequalityConstraintWt > 0) {
					comp_misfit_ineq_new = new double[misfit_ineq_new.length];
					calculateMisfit(A_MFD, d_MFD, null, x, -1, Double.NaN, comp_misfit_ineq_new);
				}
				double Enew_temp = calculateEnergy(x, comp_misfit_new, comp_misfit_ineq_new);
				double pDiff = DataUtils.getPercentDiff(Enew, Enew_temp);
				System.out.println("Pdiff: "+(float)pDiff+" %");
				double pDiffThreshold = 0.0001;
				Preconditions.checkState(pDiff < pDiffThreshold,
						"they don't match within "+pDiffThreshold+"%! "+Enew+" != "+Enew_temp+" ("+(float)pDiff+" %)");
			}
		
			// Is this a new best?
			if (Enew < Ebest) {
				xbest = Arrays.copyOf(x, nCol);
				System.arraycopy(misfit_new, 0, misfit_best, 0, misfit_best.length);
//				misfit_best = Arrays.copyOf(misfit_new, misfit_new.length);
				if (relativeMagnitudeInequalityConstraintWt > 0) {
//					misfit_ineq_best = Arrays.copyOf(misfit_ineq_new, misfit_ineq_new.length);
					System.arraycopy(misfit_ineq_new, 0, misfit_ineq_best, 0, misfit_ineq_best.length);
				}
				Ebest = Enew;
			}

			// Change state? Calculate transition probability P
			switch (nonnegativityConstraintAlgorithm) {
			case PREVENT_ZERO_RATES:  
				if (Enew < E || x[index]==0) {
					P = 1; // Always keep new model if better OR if element was originally zero
				} else {
					// Sometimes keep new model if worse (depends on T)
					P = Math.exp((E - Enew) / (double) T); 
				}
			break;
			default:
				if (Enew < E) {
					P = 1; // Always keep new model if better
				} else {
					// Sometimes keep new model if worse (depends on T)
					P = Math.exp((E - Enew) / (double) T); 
				}
			}
			
			
			// Use transition probability to determine (via random number draw) if solution is kept
			if (P > r.nextDouble()) {
				E = Enew;
				misfit = Arrays.copyOf(misfit_new, misfit.length);
				if (relativeMagnitudeInequalityConstraintWt > 0)
					misfit_ineq = Arrays.copyOf(misfit_ineq_new, misfit_ineq.length);
			} else {
				// undo the perturbation
				x[index] -= perturb[index];
			}
			iter++;
		}
		
		watch.stop();
		
		// Preferred model is best model seen during annealing process
		if(D) {
			System.out.println("Annealing schedule completed.");
			double runSecs = watch.getTime() / 1000d;
			System.out.println("Done with Inversion after " + runSecs + " seconds.");
		}
		
		// we added one to it before, remove it to make it zero-based
		return iter-1;
	}

	private double getPerturbation(GenerationFunctionType perturbationFunc, double T) {

		double perturbation;
		double r2;

		switch (perturbationFunc) {
		case UNIFORM_NO_TEMP_DEPENDENCE:
			perturbation = (r.nextDouble()-0.5)* 0.001;
			break;
		case GAUSSIAN:
			perturbation =  (1/Math.sqrt(T)) * r.nextGaussian() * 0.0001 * Math.exp(1/(2*T)); 
			break;
		case TANGENT:
			perturbation = T * 0.001 * Math.tan(Math.PI * r.nextDouble() - Math.PI/2);	
			break;
		case POWER_LAW:
			r2 = r.nextDouble();  
			perturbation = Math.signum(r2-0.5) * T * 0.001 * (Math.pow(1+1/T,Math.abs(2*r2-1))-1);
			break;
		case EXPONENTIAL:
			r2 = r.nextDouble();  
			perturbation = Math.pow(10, r2) * T * 0.001;
			break;
		default:
			throw new IllegalStateException("Oh dear.  You missed a Generation Function type.");
		}

		return perturbation;

	}
	
	private static String enumOptionsStr(Enum<?>[] values) {
		String str = null;
		
		for (Enum<?> e : values) {
			if (str == null)
				str = "";
			else
				str += ",";
			str += e.name();
		}
		
		return str;
	}
	
	protected static Options createOptions() {
		Options ops = new Options();
		
		Option coolingOption = new Option("cool", "cooling-schedule", true,
				"Cooling schedule. One of: "+enumOptionsStr(CoolingScheduleType.values())
				+". Default: "+COOLING_FUNC_DEFAULT);
		coolingOption.setRequired(false);
		ops.addOption(coolingOption);
		
		Option perturbOption = new Option("perturb", "perturbation-function", true,
				"Cooling schedule. One of: "+enumOptionsStr(GenerationFunctionType.values())
				+". Default: "+PERTURB_FUNC_DEFAULT);
		perturbOption.setRequired(false);
		ops.addOption(perturbOption);
		
		Option nonNegOption = new Option("nonneg", "nonnegativity-const", true,
				"Cooling schedule. One of: "+enumOptionsStr(NonnegativityConstraintType.values())
				+". Default: "+NONNEGATIVITY_CONST_DEFAULT);
		nonNegOption.setRequired(false);
		ops.addOption(nonNegOption);
		
		return ops;
	}
	
	protected void setCalculationParamsFromOptions(CommandLine cmd) {
		if (cmd.hasOption("cool")) {
			coolingFunc = CoolingScheduleType.valueOf(cmd.getOptionValue("cool"));
		}
		
		if (cmd.hasOption("perturb")) {
			perturbationFunc = GenerationFunctionType.valueOf(cmd.getOptionValue("perturb"));
		}
		
		if (cmd.hasOption("nonneg")) {
			nonnegativityConstraintAlgorithm = NonnegativityConstraintType.valueOf(cmd.getOptionValue("nonneg"));
		}
	}

}







