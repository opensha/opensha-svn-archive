package scratch.UCERF3.simulatedAnnealing;

import java.util.Arrays;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.time.StopWatch;

import scratch.UCERF3.simulatedAnnealing.completion.CompletionCriteria;
import scratch.UCERF3.simulatedAnnealing.completion.IterationCompletionCriteria;

import com.google.common.base.Preconditions;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;

/**
 * 
 * @author Morgan Page and Kevin Milner
 *
 */


public class SimulatedAnnealing {

	public enum CoolingScheduleType {
		CLASSICAL_SA,  // classical SA cooling schedule (Geman and Geman, 1984) (slow but ensures convergence)
		FAST_SA,	   // fast SA cooling schedule (Szu and Hartley, 1987)
		VERYFAST_SA;   // very fast SA cooling schedule (Ingber, 1989) (recommended)
	}

	public enum GenerationFunctionType { // how rates are perturbed each SA algorithm iteration
		UNIFORM_NO_TEMP_DEPENDENCE, // recommended (box-car distribution of perturbations, no dependence on SA temperature)
		GAUSSIAN,  
		TANGENT,
		POWER_LAW;
	}

	public enum NonnegativityConstraintType {
		TRY_ZERO_RATES_OFTEN, // sets rate to zero if they are perturbed to negative values (recommended - anneals much faster!)
		LIMIT_ZERO_RATES;     // re-perturb rates if they are perturbed to negative values 
	}
	
	protected static final String XML_METADATA_NAME = "SimulatedAnnealing";

	protected final static boolean D = true;  // for debugging

	private static CoolingScheduleType COOLING_FUNC_DEFAULT = CoolingScheduleType.VERYFAST_SA;
	private CoolingScheduleType coolingFunc = COOLING_FUNC_DEFAULT;
	
	private static NonnegativityConstraintType NONNEGATIVITY_CONST_DEFAULT =
		NonnegativityConstraintType.TRY_ZERO_RATES_OFTEN;
	private NonnegativityConstraintType nonnegativeityConstraintAlgorithm = NONNEGATIVITY_CONST_DEFAULT;
	
	private static GenerationFunctionType PERTURB_FUNC_DEFAULT = GenerationFunctionType.UNIFORM_NO_TEMP_DEPENDENCE;
	private GenerationFunctionType perturbationFunc = PERTURB_FUNC_DEFAULT;;
	
	private DoubleMatrix2D A;
	private double[] d;
	
	private int nCol;
	private int nRow;
	
	private double[] x; // current model
	private double[] xbest;  // best model seen so far
	private double[] perturb; // perturbation to current model
	private DoubleMatrix1D syn;  // data synthetics
	private double[] misfit; // misfit between data and synthetics
	
	private double Ebest;

	public SimulatedAnnealing(DoubleMatrix2D A, double[] d, double[] initialState) {
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

		x = Arrays.copyOf(initialState, nCol); // current model
		xbest = Arrays.copyOf(initialState, nCol);  // best model seen so far
		perturb = new double[nCol]; // perturbation to current model
		syn = new DenseDoubleMatrix1D(nRow);  // data synthetics
		misfit = new double[nRow]; // misfit between data and synthetics
		
		Ebest = calculateMisfit(x);
	}
	
	public void setCalculationParams(CoolingScheduleType coolingFunc,
			NonnegativityConstraintType nonnegativeityConstraintAlgorithm,
			GenerationFunctionType perturbationFunc) {
		this.coolingFunc = coolingFunc;
		this.nonnegativeityConstraintAlgorithm = nonnegativeityConstraintAlgorithm;
		this.perturbationFunc = perturbationFunc;
	}
	
	public CoolingScheduleType getCoolingFunc() {
		return coolingFunc;
	}

	public void setCoolingFunc(CoolingScheduleType coolingFunc) {
		this.coolingFunc = coolingFunc;
	}

	public NonnegativityConstraintType getNonnegativeityConstraintAlgorithm() {
		return nonnegativeityConstraintAlgorithm;
	}

	public void setNonnegativeityConstraintAlgorithm(
			NonnegativityConstraintType nonnegativeityConstraintAlgorithm) {
		this.nonnegativeityConstraintAlgorithm = nonnegativeityConstraintAlgorithm;
	}

	public GenerationFunctionType getPerturbationFunc() {
		return perturbationFunc;
	}

	public void setPerturbationFunc(GenerationFunctionType perturbationFunc) {
		this.perturbationFunc = perturbationFunc;
	}

	public double[] getBestSolution() {
		return xbest;
	}
	
	public double getBestEnergy() {
		return Ebest;
	}
	
	public void setResults(double Ebest, double[] xbest) {
		this.Ebest = Ebest;
		this.xbest = xbest;
	}
	
	protected synchronized double calculateMisfit(double[] solution) {
		// Do forward problem for new perturbed model (calculate synthetics)
		
		// needs to be converted to a DoubleMatrix1D
		DoubleMatrix1D sol_clone = new DenseDoubleMatrix1D(solution);
		
		// Sparse Matrix Multiplication: syn=A*sol
		A.zMult(sol_clone, syn);
		
		double Enew = 0;
		for (int i = 0; i < nRow; i++) {
			misfit[i] = syn.get(i) - d[i];  // misfit between synthetics and data
			Enew += Math.pow(misfit[i], 2);  // L2 norm of misfit vector
		}
		return Enew;
	}
	
	public synchronized void iterate(long numIterations) {
		iterate(new IterationCompletionCriteria(numIterations));
	}
	
	public synchronized void iterate(CompletionCriteria completion) {
		iterate(0, completion);
	}

	protected synchronized void iterate(long startIter, CompletionCriteria criteria) {
		StopWatch watch = new StopWatch();
		watch.start();

		if(D) System.out.println("Solving inverse problem with simulated annealing ... \n");
		if(D) System.out.println("Cooling Function: " + coolingFunc.name());
		if(D) System.out.println("Perturbation Function: " + perturbationFunc.name());
		if(D) System.out.println("Nonnegativity Constraint: " + nonnegativeityConstraintAlgorithm.name());
		if(D) System.out.println("Completion Criteria: " + criteria);
		
		double[] xnew;
		double Enew;
		double P;
		long iter=startIter+1;
		int index;
		double E = Ebest;
		double T;

		while (!criteria.isSatisfied(watch, iter, Ebest)) {

			// Find current simulated annealing "temperature" based on chosen cooling schedule
			switch (coolingFunc) {
			case CLASSICAL_SA:
				T = 1/Math.log( (double) iter); // classical SA cooling schedule (Geman and Geman, 1984) (slow but ensures convergence)
				break;
			case FAST_SA:
				T = 1 / (double) iter;  // fast SA cooling schedule (Szu and Hartley, 1987)
				break;
			case VERYFAST_SA:
				T = Math.exp(-( (double) iter - 1)); // very fast SA cooling schedule (Ingber, 1989) (recommended)
				break;
			default:
				throw new IllegalStateException("It's impossible to get here, as long as all cooling schedule enum cases are stated above!");
			}

			if (D) {  // print out convergence info every so often
				if (iter % 1000 == 0) { 
					System.out.println("Iteration # " + iter);
					System.out.println("Lowest energy found = " + Ebest);
				}
			}


			// Pick neighbor of current model
			xnew = Arrays.copyOf(x, nCol);  // This does xnew=x for an array

			// Index of model to randomly perturb
			index = (int)(Math.random() * (double)nCol); // casting as int takes the floor


			// How much to perturb index (some perturbation functions are a function of T)	
			perturb[index] = getPerturbation(perturbationFunc, T);  

			// Apply then nonnegativity constraint -- make sure perturbation doesn't make the rate negative
			switch (nonnegativeityConstraintAlgorithm) {
			case TRY_ZERO_RATES_OFTEN: // sets rate to zero if they are perturbed to negative values 
				// This way will result in many zeros in the solution, 
				// which may be desirable since global minimum is likely near a boundary
				if (xnew[index] == 0) { // if that rate was already zero do not keep it at zero
					while (x[index] + perturb[index] < 0) 
						perturb[index] = getPerturbation(perturbationFunc,T);
				} else { // if that rate was not already zero, and it goes negative, set it equal to zero
					if (xnew[index] + perturb[index] < 0) 
						perturb[index] = -xnew[index];
				}
				break;
			case LIMIT_ZERO_RATES:    // re-perturb rates if they are perturbed to negative values 
				// This way will result in not a lot of zero rates (none if numIterations >> length(x)),
				// which may be desirable if we don't want a lot of zero rates
				while (x[index] + perturb[index] < 0) 
					perturb[index] = getPerturbation(perturbationFunc,T);	
				break;
			}
			xnew[index] += perturb[index]; 

			// Calculate "energy" of new model (high misfit -> high energy)
			Enew = calculateMisfit(xnew);

			// Is this a new best?
			if (Enew < Ebest) {
				xbest = Arrays.copyOf(xnew, nCol);
				Ebest = Enew;
			}

			// Change state? Calculate transition probability P
			if (Enew < E) {
				P = 1; // Always keep new model if better
			} else {
				// Sometimes keep new model if worse (depends on T)
				P = Math.exp((E - Enew) / (double) T); 
			}

			// Use transition probability to determine (via random number draw) if solution is kept
			if (P > Math.random()) {
				x = Arrays.copyOf(xnew, nCol);
				E = Enew;				
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
	}

	public static double getPerturbation(GenerationFunctionType perturbationFunc, double T) {

		double perturbation;
		Random r1 = new Random();
		double r2;

		switch (perturbationFunc) {
		case UNIFORM_NO_TEMP_DEPENDENCE:
			perturbation = (Math.random()-0.5) * 0.001; // (recommended)
			break;
		case GAUSSIAN:
			perturbation =  (1/Math.sqrt(T)) * r1.nextGaussian() * 0.0001 * Math.exp(1/(2*T)); 
			break;
		case TANGENT:
			perturbation = T * 0.001 * Math.tan(Math.PI*Math.random() - Math.PI/2);	
			break;
		case POWER_LAW:
			r2 = Math.random();  
			perturbation = Math.signum(r2-0.5) * T * 0.001 * (Math.pow(1+1/T,Math.abs(2*r2-1))-1);
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
			nonnegativeityConstraintAlgorithm = NonnegativityConstraintType.valueOf(cmd.getOptionValue("nonneg"));
		}
	}

}







