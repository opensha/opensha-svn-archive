package scratch.UCERF3.erf.ETAS;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import org.opensha.commons.util.ExceptionUtils;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;


/**
 * Cache for cube samplers which keeps uses the number of forthcoming events for eviction.
 * When the cache is full and a new value is loaded into memory then each get command will
 * evict the first entry whose num forthcoming events is lower than the newly loaded entry.
 * If no such entry is found, the new value is not cached.
 * 
 * @author kevin
 *
 */
public class CubeSamplerCache implements LoadingCache<Integer, IntegerPDF_FunctionSampler> {
	
	private static final boolean D = true;
	private static final boolean DD = D && false;
	
	private int size;
	private CacheLoader<Integer, IntegerPDF_FunctionSampler> loader;
	private Map<Integer, Integer> numForthcomingMap;
	
	private Map<Integer, IntegerPDF_FunctionSampler> cache;
	
	private int getCount = 0;
	private int loadCount = 0;
	private int regenCount = 0;
	
	private HashSet<Integer> regenTracker;
	
	public CubeSamplerCache(int size, CacheLoader<Integer, IntegerPDF_FunctionSampler> loader,
			Map<Integer, Integer> numForthcomingMap) {
		this.size = size;
		this.loader = loader;
		this.numForthcomingMap = numForthcomingMap;
		
		cache = Maps.newHashMap();
		
		regenTracker = new HashSet<Integer>();
	}
	
	public synchronized IntegerPDF_FunctionSampler get(Integer index) {
		getCount++;
		IntegerPDF_FunctionSampler ret = cache.get(index);
		if (ret == null) {
			if (DD) System.out.println("Cache miss for "+index+" size="+cache.size());
			try {
				ret = loader.load(index);
			} catch (Exception e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
			Preconditions.checkNotNull(ret);
			loadCount++;
			if (regenTracker.contains(index))
				regenCount++;
			else
				regenTracker.add(index);
			while (cache.size() >= size) {
				// try to free up space
				int myPriority = getNumForthcoming(index);
				int toRemoveIndex = -1;
				for (int key : cache.keySet()) {
					int oPriority = getNumForthcoming(key);
					if (oPriority <= myPriority) {
						toRemoveIndex = key;
						if (oPriority < myPriority)
							// if it's less, stop search
							break;
					}
				}
				if (toRemoveIndex >= 0) {
					if (DD) System.out.println("Evicting "+toRemoveIndex+" with p="
							+getNumForthcoming(toRemoveIndex)+" <= "+myPriority);
					Preconditions.checkNotNull(cache.remove(toRemoveIndex));
				} else {
					// nothing evictable, don't cache current result
					break;
				}
			}
			if (cache.size() < size) {
				if (DD) System.out.println("Caching "+index);
				cache.put(index, ret);
			}
		}
		if (D && getCount % 100 == 0)
			System.out.println("***CACHE get="+getCount+", load="+loadCount+", regen="+regenCount);
		return ret;
	}
	
	private int getNumForthcoming(int index) {
		Integer num = numForthcomingMap.get(index);
		if (num == null)
			num = 0;
		return num;
	}

	public void invalidateAll() {
		cache.clear();
	}

	@Override
	public void cleanUp() {}

	@Override
	public synchronized IntegerPDF_FunctionSampler get(Integer key,
			Callable<? extends IntegerPDF_FunctionSampler> valueLoader)
			throws ExecutionException {
		throw new UnsupportedOperationException("Use get(kev)");
	}

	@Override
	public ImmutableMap<Integer, IntegerPDF_FunctionSampler> getAllPresent(
			Iterable<?> keys) {
		throw new UnsupportedOperationException("");
	}

	@Override
	public IntegerPDF_FunctionSampler getIfPresent(Object key) {
		return cache.get(key);
	}

	@Override
	public void invalidate(Object key) {
		cache.remove(key);
	}

	@Override
	public void invalidateAll(Iterable<?> keys) {
		for (Object key : keys)
			invalidate(key);
	}

	@Override
	public void put(Integer key, IntegerPDF_FunctionSampler value) {
		cache.put(key, value);
	}

	@Override
	public void putAll(
			Map<? extends Integer, ? extends IntegerPDF_FunctionSampler> m) {
		cache.putAll(m);
	}

	@Override
	public long size() {
		return cache.size();
	}

	@Override
	public CacheStats stats() {
		return null;
	}

	@Override
	public IntegerPDF_FunctionSampler apply(Integer key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ConcurrentMap<Integer, IntegerPDF_FunctionSampler> asMap() {
		throw new UnsupportedOperationException("Use get(kev)");
	}

	@Override
	public ImmutableMap<Integer, IntegerPDF_FunctionSampler> getAll(
			Iterable<? extends Integer> keys) throws ExecutionException {
		throw new UnsupportedOperationException("Use get(kev)");
	}

	@Override
	public IntegerPDF_FunctionSampler getUnchecked(Integer key) {
		return get(key);
	}

	@Override
	public void refresh(Integer key) {
		// do nothing
	}

}
