package scratch.UCERF3.utils;

import java.util.Arrays;

import cern.colt.matrix.tdouble.DoubleMatrix1D;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix1D;

/**
 * 
 * @author Morgan Page
 *
 */
public class SimulatedAnnealing {

	protected final static boolean D = true;  // for debugging
	
	public static double[] getSolution(DoubleMatrix2D A, double[] d, double[] initial_state, int numiter) {
		
		long runTime = System.currentTimeMillis();
		long multTime = 0;
		long setupTime = System.currentTimeMillis();

		int nRow = A.rows();
		int nCol = A.columns();

		if(D) System.out.println("nRow = " + nRow);
		if(D) System.out.println("nCol = " + nCol);
		
		double[] x = Arrays.copyOf(initial_state, nCol); // current model
		double[] xbest = Arrays.copyOf(initial_state, nCol);  // best model seen so far
		double[] xnew; // new perturbed model
		double[] perturb = new double[nCol]; // perturbation to current model
//		double[] syn = new double[nRow]; // data synthetics
		DoubleMatrix1D syn = new DenseDoubleMatrix1D(nRow);
		double[] misfit = new double[nRow]; // mifit between data and synthetics

		double E, Enew, Ebest, T, P;
		int i, j, iter, index;
		// int numiter = 100000;
		
		if(D) System.out.println("\nSolving inverse problem with simulated annealing ... \n");
		
		if(D) System.out.println("Total number of iterations = " + numiter);
		
//		Random r = new Random(System.currentTimeMillis());
		
		// now done above via Arrays.copyOf(...)
//		for (j = 0; j < nCol; j++) {
//			x[j] = initial_state[j]; // starting model
//		}
		// x=initial_state.clone();  // not sure why this doesn't work in lieu of above code
		
		// Initial "best" solution & its Energy
		// now done above via Arrays.copyOf(...)
//		for (j = 0; j < nCol; j++) {
//			xbest[j] = x[j];  
//		}
		
		E = 0;
		for (i = 0; i < nRow; i++) {
//			syn[i] = 0;
			for (j = 0; j < nCol; j++) {
				syn.set(i, syn.get(i) + A.get(i,j) * x[j]); // compute predicted data
//				syn[i] += A.get(i,j) * x[j]; // compute predicted data
			}
			misfit[i] = syn.get(i) - d[i];  // misfit between synthetics and data
			E += Math.pow(misfit[i], 2);  // L2 norm of misfit vector
		}
		//E = Math.sqrt(E);
		Ebest = E;
		if (D) {
			System.out.println("Starting energy = " + Ebest);
			setupTime = System.currentTimeMillis()-setupTime;
			System.out.println("Setup time: "+(setupTime/1000d)+" seconds");
		}
		
		for (iter = 1; iter <= numiter; iter++) {
			
			
			// Simulated annealing "temperature"
			// T = 1 / (double) iter; 
			// T = 1/Math.log( (double) iter);
			// T = Math.pow(0.999,iter-1);
			T = Math.exp(-( (double) iter - 1));
			
//			if ((double) iter / 1000 == Math.floor(iter / 1000)) {
			if (iter % 1000 == 0) { // this is equivalent and faster
				if(D) System.out.println("Iteration # " + iter);
			//	if(D) System.out.println("T = " + T);
				if(D) System.out.println("Lowest energy found = " + Ebest);
			}
					
			
			// Pick neighbor of current model
			xnew = Arrays.copyOf(x, nCol);
//			for (j = 0; j < nCol; j++) {
//				xnew[j]=x[j];
//			}
			
			// Index of model to randomly perturb
			// don't need to call Math.floor() here, casting to int automatically does that
			index = (int)(Math.random() * (double)nCol); 
			
			// How much to perturb index (can be a function of T)	
			// perturb[index] = (Math.random()-0.5) * 0.25 * initial_state[index];  // Only use with non-zero starting model!
			perturb[index] = (Math.random()-0.5) * 0.001;
			// perturb[index] =  (1/Math.sqrt(T)) * r.nextGaussian() * 0.0001 * Math.exp(1/(2*T)); 
			// perturb[index] = T * 0.001 * Math.tan(Math.PI*Math.random() - Math.PI/2);		
			// r = Math.random();
			// perturb[index] = Math.signum(r-0.5) * T * 0.001 * (Math.pow(1+1/T,Math.abs(2*r-1))-1);
			
			// Nonnegativity constraint
			// while (x[index] + perturb[index] < 0) {
			// perturb[index] = (Math.random()-0.5)*0.001;
			// }		
			if (xnew[index] + perturb[index] < 0) {
				perturb[index] = -xnew[index];
			}
			xnew[index] += perturb[index];
			
			// RealMatrix doesn't work for some reason... using OpenMapRealMatrix instead.
			// RealMatrix xnew_clone1 = new RealMatrix(1,nCol); // duplicate of double[] xnew in RealMatrix form (for multiplication with sparse matrix A)
			DoubleMatrix1D xnew_clone = new DenseDoubleMatrix1D(xnew);
//			OpenMapRealMatrix xnew_clone = new OpenMapRealMatrix(nCol,1); 
//			for (i=0; i<nCol; i++) {
//				xnew_clone.set(i, 0, xnew[i]);
////				xnew_clone.setEntry(i, 0, xnew[i]);
//			}
			
			
			// Calculate "energy" of new model (high misfit -> high energy)
			
			/*  // Old NON-SPARSE CODE
			Enew = 0;
			for (i = 0; i < nRow; i++) {
				syn[i] = 0;
				for (j = 0; j < nCol; j++) {
					syn[i] += A.getEntry(i,j) * xnew[j]; // compute predicted data
				}
				misfit[i] = syn[i] - d[i];  // misfit between synthetics and data
				Enew += Math.pow(misfit[i], 2);  // L2 norm of misfit vector
			}
			*/ 
			
			// SPARSE IMPLEMENTATION (at least for multiplication of A and xnew -- rest is still clumsy)
			// lets try a new matrix package...
			
//			SparseDoubleMatrix2D xnew_clone_new = new SparseDoubleMatrix2D(xnew_clone.getData());
			
			long startMult = System.currentTimeMillis();
//			OpenMapRealMatrix syn_clone = A.multiply(xnew_clone);
			
			// this stores the results in syn
			DoubleMatrix1D results = A.zMult(xnew_clone, syn);
			multTime += System.currentTimeMillis() - startMult;
			Enew = 0;
			for (i = 0; i < nRow; i++) {
//				syn[i] = syn_clone.getEntry(i,0);
				misfit[i] = syn.get(i) - d[i];  // misfit between synthetics and data
				Enew += Math.pow(misfit[i], 2);  // L2 norm of misfit vector
			}
			
			//Enew = Math.sqrt(Enew);
			
			// Is this a new best?
			if (Enew < Ebest) {
				xbest = Arrays.copyOf(xnew, nCol);
//				for (j = 0; j < nCol; j++) {
//					xbest[j] = xnew[j];
//				}
				Ebest = Enew;
			}

			// Change state? Calculate transition probability P
			if (Enew < E) {
				P = 1; // Always keep new model if better
			} else {
			
				// Sometimes keep new model if worse (depends on T)
				P = Math.exp((E - Enew) / (double) T); 
			}
			
			if (P > Math.random()) {
				x = Arrays.copyOf(xnew, nCol);
//				for (j = 0; j < nCol; j++) {
//					x[j]=xnew[j];
//				}
				E = Enew;
				//if(D) System.out.println("New soluton kept! E = " + E + ". P = " + P);
				
			}
			
		}

		// Preferred model is best model seen during annealing process
		if(D) {
			System.out.println("Annealing schedule completed.");
			double runSecs = (System.currentTimeMillis() - runTime) / 1000d;
			double multSecs = multTime / 1000d;
			System.out.println("Done with Inversion after " + runSecs + " seconds.");
			System.out.println("Mult time: "+multSecs+" secs ("+(multSecs/runSecs*100d)+" % )");
		}
		return xbest;
	}


	
}
