package scratch.UCERF3.utils;

import java.util.Arrays;
import java.util.Random;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;

/**
 * 
 * @author Morgan Page and Kevin Milner
 *
 */


public class SimulatedAnnealing {
	
	enum CoolingScheduleType {
		CLASSICAL_SA,  // classical SA cooling schedule (Geman and Geman, 1984) (slow but ensures convergence)
		FAST_SA,	   // fast SA cooling schedule (Szu and Hartley, 1987)
		VERYFAST_SA;   // very fast SA cooling schedule (Ingber, 1989) (recommended)
	}

	enum GenerationFunctionType { // how rates are perturbed each SA algorithm iteration
		UNIFORM_NO_TEMP_DEPENDENCE, // recommended (box-car distribution of perturbations, no dependence on SA temperature)
		GAUSSIAN,  
		TANGENT,
		POWER_LAW;
	}
	
	enum NonnegativityConstraintType {
		TRY_ZERO_RATES_OFTEN, // sets rate to zero if they are perturbed to negative values (recommended - anneals much faster!)
		LIMIT_ZERO_RATES;     // re-perturb rates if they are perturbed to negative values 
	}


	protected final static boolean D = true;  // for debugging
	
	public static double[] getSolution(DoubleMatrix2D A, double[] d, double[] initialState, int numIterations) {
		
		CoolingScheduleType coolingFunc = CoolingScheduleType.VERYFAST_SA;
		NonnegativityConstraintType nonnegativeityConstraintAlgorithm = NonnegativityConstraintType.TRY_ZERO_RATES_OFTEN;
		GenerationFunctionType perturbationFunc = GenerationFunctionType.UNIFORM_NO_TEMP_DEPENDENCE;
		
		long runTime = System.currentTimeMillis();
		long multTime = 0;
		long setupTime = System.currentTimeMillis();

		int nRow = A.rows();
		int nCol = A.columns();
		
		double[] x = Arrays.copyOf(initialState, nCol); // current model
		double[] xbest = Arrays.copyOf(initialState, nCol);  // best model seen so far
		double[] xnew; // new perturbed model
		double[] perturb = new double[nCol]; // perturbation to current model
		DoubleMatrix1D syn = new DenseDoubleMatrix1D(nRow);  // data synthetics
		double[] misfit = new double[nRow]; // misfit between data and synthetics

		double E, Enew, Ebest, T, P;
		int i, j, iter, index;
		
		
		if(D) System.out.println("Solving inverse problem with simulated annealing ... \n");
		if(D) System.out.println("Total number of iterations = " + numIterations);
		
		
		E = 0;
		for (i = 0; i < nRow; i++) {
			for (j = 0; j < nCol; j++) {
				syn.set(i, syn.get(i) + A.get(i,j) * x[j]); // compute predicted data
			}
			misfit[i] = syn.get(i) - d[i];  // misfit between synthetics and data
			E += Math.pow(misfit[i], 2);  // L2 norm of misfit vector
		}
		Ebest = E;
		
		if (D) {
			System.out.println("Starting energy = " + Ebest);
			setupTime = System.currentTimeMillis()-setupTime;
			System.out.println("Setup time: "+(setupTime/1000d)+" seconds\n");
		}
		
		for (iter = 1; iter <= numIterations; iter++) {
			
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
			
			
			
			// SPARSE IMPLEMENTATION (at least for multiplication of A and xnew -- rest is still clumsy)
			
			// Make copy of xnew variable of type DoubleMatrix1D
			// This is used in super-fast sparse matrix multiplication
			DoubleMatrix1D xnew_clone = new DenseDoubleMatrix1D(xnew);
			
			// Do forward problem for new perturbed model (calculate synthetics)
			long startMult = System.currentTimeMillis(); // start time of multiplication
			A.zMult(xnew_clone, syn); // Sparse Matrix Multiplication: syn=A*xnew
			multTime += System.currentTimeMillis() - startMult;  // keep track of computational time spent in multiplication step

			// Calculate "energy" of new model (high misfit -> high energy)
			Enew = 0;
			for (i = 0; i < nRow; i++) {
				misfit[i] = syn.get(i) - d[i];  // misfit between synthetics and data
				Enew += Math.pow(misfit[i], 2);  // L2 norm of misfit vector
			}
			
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
			
		}

		// Preferred model is best model seen during annealing process
		if(D) {
			System.out.println("Annealing schedule completed.");
			double runSecs = (System.currentTimeMillis() - runTime) / 1000d;
			double multSecs = multTime / 1000d;
			System.out.println("Done with Inversion after " + runSecs + " seconds.");
			System.out.println("Mult time: "+multSecs+" secs ("+(multSecs/runSecs*100d)+" %)");
		}
		return xbest;
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


}







