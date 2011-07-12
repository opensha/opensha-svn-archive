package scratch.UCERF3.simulatedAnnealing;

import java.util.ArrayList;

import org.apache.commons.lang.time.StopWatch;

import cern.colt.matrix.tdouble.DoubleMatrix2D;

public class ThreadedSimulatedAnnealing {
	
	private static final boolean D = true;
	
	private int numThreads;
	private ArrayList<SimulatedAnnealing> sas;

	public ThreadedSimulatedAnnealing(DoubleMatrix2D A, double[] d,
			double[] initialState) {
		this(A, d, initialState, Runtime.getRuntime().availableProcessors());
	}
	
	public ThreadedSimulatedAnnealing(DoubleMatrix2D A, double[] d,
			double[] initialState, int numThreads) {
		
		this.numThreads = numThreads;
		sas = new ArrayList<SimulatedAnnealing>();
		for (int i=0; i<numThreads; i++)
			sas.add(new SimulatedAnnealing(A, d, initialState));
	}
	
	private class SAThread extends Thread {
		private SimulatedAnnealing sa;
		private int numSubIterations;
		private int startIter;
		
		public SAThread(SimulatedAnnealing sa, int startIter, int numSubIterations) {
			this.sa = sa;
			this.numSubIterations = numSubIterations;
			this.startIter = startIter;
		}
		
		@Override
		public void run() {
			sa.iterate(startIter, numSubIterations);
		}
	}

	public double[] getSolution(int numIterations, int numSubIterations) throws InterruptedException {
		double Ebest = Double.MAX_VALUE;
		double[] xbest = null;
		
		StopWatch watch = new StopWatch();
		watch.start();
		
		if (D) System.out.println("Threaded Simulated Annealing starting with "+numThreads
				+" threads, "+numIterations+" iterations, "+numSubIterations+" sub iterations");
		
		for (int iter=0; iter<numIterations; iter+=numSubIterations) {
			ArrayList<SAThread> threads = new ArrayList<ThreadedSimulatedAnnealing.SAThread>();
			
			// create the threads
			for (int i=0; i<numThreads; i++) {
				threads.add(new SAThread(sas.get(i), iter, numSubIterations));
			}
			
			// start the threads
			for (Thread t : threads) {
				t.start();
			}
			
			// join the threads
			for (Thread t : threads) {
				t.join();
			}
			
			for (int i=0; i<numThreads; i++) {
				SimulatedAnnealing sa = sas.get(i);
				double E = sa.getBestEnergy();
				if (E < Ebest) {
					Ebest = E;
					xbest = sa.getBestSolution();
				}
			}
			
			if (D) {
				double secs = watch.getTime() / 1000d;
				System.out.println("Threaded total iteration "+iter+" DONE after "
						+(float)secs+" seconds. Best energy: "+Ebest);
			}
			
			for (SimulatedAnnealing sa : sas)
				sa.setResults(Ebest, xbest);
		}
		
		watch.stop();
		
		if(D) {
			System.out.println("Threaded annealing schedule completed.");
			double runSecs = watch.getTime() / 1000d;
			System.out.println("Done with Inversion after " + (float)runSecs + " seconds.");
		}
		
		return xbest;
	}

}
