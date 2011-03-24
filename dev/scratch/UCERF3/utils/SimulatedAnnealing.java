package scratch.UCERF3.utils;

import org.apache.commons.math.linear.OpenMapRealMatrix;

public class SimulatedAnnealing {

	protected final static boolean D = true;  // for debugging

	
	public static double[] getSolution(OpenMapRealMatrix A, double[] d, double[] initial_state) {

		int nRow = A.getRowDimension();
		int nCol = A.getColumnDimension();

		if(D) System.out.println("nRow = " + nRow);
		if(D) System.out.println("nCol = " + nCol);
		
		double[] x = new double[nCol]; // current model
		double[] xbest = new double[nCol]; // best model seen so far
		double[] xnew = new double[nCol]; // new perturbed model
		double[] perturb = new double[nCol]; // perturbation to current model
		double[] syn = new double[nRow]; // data synthetics
		double[] misfit = new double[nRow]; // mifit between data and synthetics

		double E, Enew, Ebest, T, P;
		int i, j, iter, index;
		int numiter = 10000;
		
		long runTime = System.currentTimeMillis();
		
		if(D) System.out.println("Total number of iterations = " + numiter);
		
//		Random r = new Random(System.currentTimeMillis());
		
		for (j = 0; j < nCol; j++) {
			x[j] = initial_state[j]; // starting model
		}
		// x=initial_state.clone();  // not sure why this doesn't work in lieu of above code
		
		// Initial "best" solution & its Energy
		for (j = 0; j < nCol; j++) {
			xbest[j] = x[j];  
		}
		
		E = 0;
		for (i = 0; i < nRow; i++) {
			syn[i] = 0;
			for (j = 0; j < nCol; j++) {
				syn[i] += A.getEntry(i,j) * x[j]; // compute predicted data
			}
			misfit[i] = syn[i] - d[i];  // misfit between synthetics and data
			E += Math.pow(misfit[i], 2);  // L2 norm of misfit vector
		}
		//E = Math.sqrt(E);
		Ebest = E;
		if(D) System.out.println("Starting energy = " + Ebest);
		
		for (iter = 1; iter <= numiter; iter++) {
			
			
			// Simulated annealing "temperature"
			// T = 1 / (double) iter; 
			// T = 1/Math.log( (double) iter);
			// T = Math.pow(0.999,iter-1);
			T = Math.exp(-( (double) iter - 1));
			
			if ((double) iter / 1000 == Math.floor(iter / 1000)) {
				if(D) System.out.println("Iteration # " + iter);
			//	if(D) System.out.println("T = " + T);
				if(D) System.out.println("Lowest energy found = " + Ebest);
			}
					
			
			// Pick neighbor of current model
			for (j = 0; j < nCol; j++) {
				xnew[j]=x[j];
			}
			
			// Index of model to randomly perturb
			index = (int) Math.floor(Math.random() * nCol); 
			
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
			
			// Calculate "energy" of new model (high misfit -> high energy)
			Enew = 0;
			for (i = 0; i < nRow; i++) {
				syn[i] = 0;
				for (j = 0; j < nCol; j++) {
					syn[i] += A.getEntry(i,j) * xnew[j]; // compute predicted data
				}
				misfit[i] = syn[i] - d[i];  // misfit between synthetics and data
				Enew += Math.pow(misfit[i], 2);  // L2 norm of misfit vector
			}
			//Enew = Math.sqrt(Enew);
			
			// Is this a new best?
			if (Enew < Ebest) {
				for (j = 0; j < nCol; j++) {
					xbest[j] = xnew[j]; }
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
				for (j = 0; j < nCol; j++) {
					x[j]=xnew[j];
				}
				E = Enew;
				//if(D) System.out.println("New soluton kept! E = " + E + ". P = " + P);
				
			}
			
		}

		// Preferred model is best model seen during annealing process
		if(D) System.out.println("Annealing schedule completed.");
		
		if(D) runTime = (System.currentTimeMillis() - runTime) / 1000;
		if(D) System.out.println("Done with Inversion after " + runTime + " seconds.");
		
		return xbest;
	}


	
}
