package org.opensha.refFaultParamDb.calc.sectionDists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

import org.opensha.commons.util.FileUtils;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.data.finalReferenceFaultParamDb.DeformationModelPrefDataFinal;
import org.opensha.sha.faultSurface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.faultSurface.FrankelGriddedSurface;
import org.opensha.sha.faultSurface.SimpleFaultData;

public class FaultSectDistCalculator implements Runnable {
	
	private ArrayList<FaultSectionPrefData> data;
	private ArrayList<EvenlyGriddedSurfaceAPI> surfaces;
	
	private HashMap<Pairing, FaultSectDistRecord> records;
	
	// these are only used for threaded calcs
	private Stack<FaultSectDistRecord> calcStack;
	private boolean fast;
	
	private double calcTimeSecs;
	private double pairTimeSecs;
	
	public FaultSectDistCalculator(
			double disc, boolean fast,
			DeformationModelPrefDataFinal deformationModelPrefDB,
			int deformationModelId) {
		this(disc, fast, deformationModelPrefDB.getAllFaultSectionPrefData(deformationModelId));
	}
	
	public FaultSectDistCalculator(
			double disc, boolean fast,
			ArrayList<FaultSectionPrefData> data) {
		this.data = data;
		this.fast = fast;
		
		createSurfaces(disc);
	}
	
	private void createSurfaces(double disc) {
		this.surfaces = new ArrayList<EvenlyGriddedSurfaceAPI>();
		
		for (FaultSectionPrefData section : data) {
			SimpleFaultData simpleFaultData = section.getSimpleFaultData(false);
			FrankelGriddedSurface surface = new FrankelGriddedSurface(simpleFaultData, disc);
			
			surfaces.add(surface);
		}
	}
	
	public void calcDistances() {
		long start = System.currentTimeMillis();
		for (FaultSectDistRecord record : records.values()) {
			record.calcDistances(fast);
		}
		calcTimeSecs = (System.currentTimeMillis() - start) / 1000d;
	}
	
	public void calcDistances(int numThreads) throws InterruptedException {
		if (numThreads <= 1) {
			calcDistances();
			return;
		}
		long start = System.currentTimeMillis();
		calcStack = new Stack<FaultSectDistRecord>();
		for (FaultSectDistRecord record : records.values()) {
			calcStack.push(record);
		}
		// create the threads
		ArrayList<Thread> threads = new ArrayList<Thread>();
		for (int i=0; i<numThreads; i++) {
			Thread t = new Thread(this);
			threads.add(t);
		}
		
		// start the threads
		for (Thread t : threads) {
			t.start();
		}
		
		// join the threads
		for (Thread t : threads) {
			t.join();
		}
		calcTimeSecs = (System.currentTimeMillis() - start) / 1000d;
	}
	
	public void createPairings() {
		createPairings(null, -1.0);
	}
	
	public void createPairings(SurfaceFilter filter, double filterDist) {
		long start = System.currentTimeMillis();
		records = new HashMap<Pairing, FaultSectDistRecord>();
		for (int i=0; i<surfaces.size(); i++) {
			EvenlyGriddedSurfaceAPI surface1 = surfaces.get(i);
			for (int j=0; j<surfaces.size(); j++) {
				EvenlyGriddedSurfaceAPI surface2 = surfaces.get(j);
				if (surface1 == surface2)
					continue;
				int id1 = data.get(i).getSectionId();
				int id2 = data.get(j).getSectionId();
				if (id1 >= id2)
					continue;
				FaultSectDistRecord record = new FaultSectDistRecord(id1, surface1, id2, surface2);
				if (filter != null && record.calcMinCornerMidptDist(fast) > filter.getCornerMidptFilterDist())
					continue;
				if (filter != null && filterDist > 0) {
					record.calcDistances(filter, fast);
					if (record.getMinDist() > filterDist)
						continue;
				}
				records.put(record.getPairing(), record);
			}
		}
		System.out.println("Created " + records.size() + " pairings!");
		pairTimeSecs = (System.currentTimeMillis() - start) / 1000d;
	}
	
	public HashMap<Pairing, FaultSectDistRecord> getRecords() {
		return records;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		int deformationModelId = 82;
		DeformationModelPrefDataFinal deformationModelPrefDB = new DeformationModelPrefDataFinal();
		double disc = 1.0;
		double filterDist = 15;
		double cornerMidptFilterDist = 50;
		int outlineModulus = 4;
		int internalModulus = 5;
		SurfaceFilter filter = new SmartSurfaceFilter(outlineModulus, internalModulus, cornerMidptFilterDist);
//		double disc = 3.0;
		FaultSectDistCalculator calc = new FaultSectDistCalculator(disc, true,
				deformationModelPrefDB, deformationModelId);
		calc.createPairings(filter, filterDist);
		System.out.println("Pair time: " + calc.getPairTimeSecs());
		int threads = Runtime.getRuntime().availableProcessors();
//		if (filterDist > 0)
//			calc.filterOutByCornerMidptDistance(filterDist, true);
//		if (filterDist > 0)
//			calc.filterRecords(new SmartSurfaceFilter(outlineModulus, internalModulus), filterDist, true);
		System.out.println("Disc: " + disc + ", filter dist: " + filterDist
				+ ", outline modulus: " + outlineModulus + ", internal modulus: " + internalModulus);
		System.out.println("Calculating with " + threads + " threads.");
		try {
			calc.calcDistances(threads);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Calc time: " + calc.getCalcTimeSecs());
		
		FileUtils.saveObjectInFile("faultSectDistances.obj", calc.getRecords());
		
		int count = 0;
		for (FaultSectDistRecord record : calc.getRecords().values()) {
			if (record.getMinDist() < 10)
				count++;
		}
		System.out.println("Found " + count + " under cutoff!");
		long end = System.currentTimeMillis();
		System.out.println("Total time: " + ((end - start) / 1000d) + " secs");
	}
	
	private synchronized FaultSectDistRecord getRecordToCalc() throws EmptyStackException {
		return calcStack.pop();
	}

	@Override
	public void run() {
		while (true) {
			try {
				FaultSectDistRecord record = getRecordToCalc();
				record.calcDistances(fast);
			} catch (EmptyStackException e) {
				break;
			}
		}
	}
	
	public void filterOutByCornerMidptDistance(double maxDist, boolean fast) {
		ArrayList<Pairing> toBeRemoved = new ArrayList<Pairing>();
		for (Pairing pairing : records.keySet()) {
			FaultSectDistRecord record = records.get(pairing);
			double minDist = record.calcMinCornerMidptDist(fast);
			if (minDist > maxDist)
				toBeRemoved.add(pairing);
		}
		System.out.println("filtered out " + toBeRemoved.size() + "/" + records.size());
		for (Pairing remove : toBeRemoved) {
			records.remove(remove);
		}
	}
	
	public void filterRecords(SurfaceFilter filter, double maxDist, boolean fast) {
		ArrayList<Pairing> toBeRemoved = new ArrayList<Pairing>();
		for (Pairing pairing : records.keySet()) {
			FaultSectDistRecord record = records.get(pairing);
			double minDist = record.calcMinDist(filter, fast);
			if (minDist > maxDist)
				toBeRemoved.add(pairing);
		}
		System.out.println("filtered out " + toBeRemoved.size() + "/" + records.size());
		for (Pairing remove : toBeRemoved) {
			records.remove(remove);
		}
	}
	
	public double getCalcTimeSecs() {
		return calcTimeSecs;
	}
	
	public double getPairTimeSecs() {
		return pairTimeSecs;
	}

}
