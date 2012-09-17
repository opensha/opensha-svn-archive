package org.opensha.nshmp2.calc;

import java.io.IOException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opensha.commons.data.Site;
import org.opensha.commons.geo.LocationList;
import org.opensha.nshmp2.erf.NSHMP2008;
import org.opensha.nshmp2.util.Period;


/**
 * Class manages multithreaded NSHMP hazard calculations. Farms out
 * {@code HazardCalc}s to locally available cores and pipes results to a
 * supplied {@code Queue}.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class ThreadedHazardCalc {

	private LocationList locs;
	private Period period;
	private HazardResultWriter writer;

	/*
	 * Initializes a new threaded hazard calculation.
	 */
	ThreadedHazardCalc(LocationList locs, Period period,
			HazardResultWriter writer) {
		this.locs = locs;
		this.period = period;
		this.writer = writer;
	}
	
	/**
	 * Calculates hazard curves for the specified indices. Presently no index
	 * checking is performed. If {@code indices} is {@code null}, curves are
	 * calculated at all locations.
	 * 
	 * @param indices of locations to calculate curves for
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 */
	public void calculate(int[] indices) throws InterruptedException,
			ExecutionException, IOException {
		
		// set up to process all
		if (indices == null) indices = makeIndices(locs.size());
		
		// init thread mgr
		int numProc = Runtime.getRuntime().availableProcessors();
		ExecutorService ex = Executors.newFixedThreadPool(numProc);
		CompletionService<HazardResult> ecs = 
				new ExecutorCompletionService<HazardResult>(ex);

		// init erf
		NSHMP2008 erf = NSHMP2008.create();
		erf.updateForecast();
//		System.out.println(erf);

		for (int index : indices) {
			Site site = new Site(locs.get(index));
			HazardCalc2 hc = HazardCalc2.create(erf, site, period);
			ecs.submit(hc);
		}
		ex.shutdown();
		System.out.println("Jobs submitted: " + locs.size());

		// process results as they come in; ecs,take() blocks until result
		// is available
		for (int i = 0; i < locs.size(); i++) {
			writer.write(ecs.take().get());
			if (i % 10 == 0) System.out.println("Jobs completed: " + i);
		}
		
		writer.close();
	}
	
	private int[] makeIndices(int size) {
		int[] indices = new int[size];
		for (int i=0; i<size; i++) {
			indices[i] = i;
		}
		return indices;
	}

}
