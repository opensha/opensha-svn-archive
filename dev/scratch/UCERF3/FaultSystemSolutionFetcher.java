package scratch.UCERF3;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.logicTree.LogicTreeBranch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * This abstract class is used to fetch solutions for a set of logic tree branches. Implementations can
 * load solutions on demand to reduce memory consumption.
 * @author kevin
 *
 */
public abstract class FaultSystemSolutionFetcher implements Iterable<FaultSystemSolution> {
	
	private boolean cacheCopying = true;
	// this is for copying caches from previous rup sets of the same fault model
	private Map<FaultModels, FaultSystemRupSet> rupSetCacheMap = Maps.newHashMap();
	
	private Map<LogicTreeBranch, Map<String, Double>> misfitsCache = Maps.newHashMap();
	
	public abstract Collection<LogicTreeBranch> getBranches();
	
	protected abstract FaultSystemSolution fetchSolution(LogicTreeBranch branch);
	
	public FaultSystemSolution getSolution(LogicTreeBranch branch) {
		FaultSystemSolution sol = fetchSolution(branch);
		if (cacheCopying) {
			synchronized (this) {
				FaultModels fm = sol.getFaultModel();
				if (rupSetCacheMap.containsKey(fm)) {
					sol.copyCacheFrom(rupSetCacheMap.get(fm));
				} else {
					if (sol instanceof SimpleFaultSystemSolution)
						rupSetCacheMap.put(fm, ((SimpleFaultSystemSolution)sol).getRupSet());
					else
						rupSetCacheMap.put(fm, sol);
				}
			}
		}
		return sol;
	}
	
	public double[] getRates(LogicTreeBranch branch) {
		return getSolution(branch).getRateForAllRups();
	}
	
	public double[] getMags(LogicTreeBranch branch) {
		return getSolution(branch).getMagForAllRups();
	}
	
	protected abstract Map<String, Double> fetchMisfits(LogicTreeBranch branch);
	
	/**
	 * Returns a map of misfit values, if available, else null
	 * 
	 * @param branch
	 * @return
	 */
	public synchronized Map<String, Double> getMisfits(LogicTreeBranch branch) {
		Map<String, Double> misfits = misfitsCache.get(branch);
		if (misfits == null) {
			misfits = fetchMisfits(branch);
			if (misfits != null)
				misfitsCache.put(branch, misfits);
		}
		return misfits;
	}

	public boolean isCacheCopyingEnabled() {
		return cacheCopying;
	}

	public void setCacheCopying(boolean cacheCopying) {
		this.cacheCopying = cacheCopying;
	}

	@Override
	public Iterator<FaultSystemSolution> iterator() {
		return new Iterator<FaultSystemSolution>() {
			
			private Iterator<LogicTreeBranch> branchIt = getBranches().iterator();

			@Override
			public boolean hasNext() {
				return branchIt.hasNext();
			}

			@Override
			public FaultSystemSolution next() {
				return getSolution(branchIt.next());
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported by this iterator");
			}
		};
	}
	
	public static double calcScaledAverage(double[] vals, double[] weights) {
		if (vals.length == 1)
			return vals[0];
		double tot = 0d;
		for (double weight : weights)
			tot += weight;
		
		double scaledAvg = 0;
		for (int i=0; i<vals.length; i++) {
			scaledAvg += vals[i] * (weights[i] / tot);
		}
	
		return scaledAvg;
	}

	public static FaultSystemSolutionFetcher getRandomSample(
			final FaultSystemSolutionFetcher fetch, int num) {
		List<LogicTreeBranch> origBranches = Lists.newArrayList();
		origBranches.addAll(fetch.getBranches());
		final List<LogicTreeBranch> branches = Lists.newArrayList();
		Random r = new Random();
		for (int i=0; i<num; i++) {
			branches.add(origBranches.get(r.nextInt(origBranches.size())));
		}
		return new FaultSystemSolutionFetcher() {
			
			@Override
			public Collection<LogicTreeBranch> getBranches() {
				return branches;
			}
			
			@Override
			protected FaultSystemSolution fetchSolution(LogicTreeBranch branch) {
				return fetch.fetchSolution(branch);
			}
			
			@Override
			protected Map<String, Double> fetchMisfits(LogicTreeBranch branch) {
				return fetch.fetchMisfits(branch);
			}
		};
	}

}
