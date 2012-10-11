package scratch.UCERF3;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.logicTree.LogicTreeBranch;

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
		return sol;
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

}
