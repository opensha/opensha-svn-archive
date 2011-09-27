package org.opensha.commons.calc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.*;

/**
 * This class allows you to sample items randomly according to their rates
 * (or any arbitrary positive scalar). It can also generate a random series
 * of items. 
 * 
 * @author kevin
 *
 * @param <T>
 */
public class PoissonSampler<T> {
	
	// left as default for in-package testing
	List<Item> items;
	private Random r;
	
	class Item implements Comparable<Item> {
		T item;
		double rate;
		double cumRate;
		public Item(T item, double rate) {
			this.item = item;
			this.rate = rate;
		}
		@Override
		public int compareTo(Item o) {
			return Double.compare(rate, o.rate);
		}
	}
	
	/**
	 * Creates a PoissonSampler with the given list of objects and rates. Rates will be normalized
	 * and sorted internally.
	 * 
	 * @param objects list of objects of which to sample
	 * @param rates list of rates (or any arbitrary scalar) to be used for sampling. each rate must
	 * be >=0, and there must be at least one non zero rate.
	 * @throws NullPointerException if any arguments are null
	 * @throws IllegalArgumentException if lists of objects and rates are empty, or of different sizes.
	 * Will also be thrown if any rates are less than zero, or if all rates are equal to zero.
	 */
	public PoissonSampler(List<T> objects, List<Double> rates)
	throws NullPointerException, IllegalArgumentException {
		this(objects, rates, new Random());
	}
	
	/**
	 * Creates a PoissonSampler with the given list of objects and rates. Rates will be normalized
	 * and sorted internally.
	 * 
	 * @param objects list of objects of which to sample
	 * @param rates list of rates (or any arbitrary scalar) to be used for sampling. each rate must
	 * be >=0, and there must be at least one non zero rate.
	 * @param r random number generator to be used
	 * @throws NullPointerException if any arguments are null
	 * @throws IllegalArgumentException if lists of objects and rates are empty, or of different sizes.
	 * Will also be thrown if any rates are less than zero, or if all rates are equal to zero.
	 */
	public PoissonSampler(List<T> objects, List<Double> rates, Random r)
	throws NullPointerException, IllegalArgumentException {
		checkNotNull(objects, "objects cannot be null!");
		checkArgument(!objects.isEmpty(), "objects cannot be empty!");
		checkNotNull(rates, "rates cannot be null!");
		checkArgument(!rates.isEmpty(), "rates cannot be empty!");
		checkArgument(objects.size() == rates.size(), "items and rates must be of the same size");
		boolean hasNonZero = false;
		for (double rate : rates) {
			checkArgument(rate >= 0, "rates must be >= 0");
			if (rate > 0)
				hasNonZero = true;
		}
		checkArgument(hasNonZero, "must be at least one non zero rate");
		checkNotNull(r, "random cannot be null!");
		this.items = new ArrayList<Item>();
		for (int i=0; i<objects.size(); i++) {
			this.items.add(new Item(objects.get(i), rates.get(i)));
		}
		this.r = r;
		// sort it (least to greatest)
		Collections.sort(items);
		// revers it (greatest to least)
		Collections.reverse(items);
		
		// now normalize. it is important that this happens after sorting because we also set
		// the cumulative rates here
		normalizeRates();
	}
	
	private void normalizeRates() {
		double sum = 0d;
		for (Item item : items)
			sum += item.rate;
		double cum = 0d;
		for (Item item : items) {
			// normalize it
			item.rate = item.rate / sum;
			// set the cumulative rate at this point
			cum += item.rate;
			item.cumRate = cum;
		}
		// make it immutable as changing things after this would cause problems
		items = Collections.unmodifiableList(items);
	}
	
	Item next() {
		double rate = r.nextDouble();
		return getForCumRate(rate);
	}
	
	Item getForCumRate(double rate) {
		checkState(rate >= 0d && rate <= 1d, "rate must be within zero and one");
		Item lastNonZero = null;
		for (Item item : items) {
			if (rate <= item.cumRate)
				return item;
			if (item.rate > 0)
				lastNonZero = item;
			else
				// not worth iterating over items with zero rate
				break;
		}
		return lastNonZero;
//		throw new IllegalStateException("Cannot locate item for rate: "+rate
//				+" (max cum rate: "+items.get(items.size()-1).cumRate+")");
	}
	
	/**
	 * Returns the item at the given normalized (between 0 and 1) cumulative rate. Note that
	 * all items are sorted, so <code>getItemForNormCumRate(0)</code> would return the first item,
	 * and <code>getItemForNormCumRate(1)</code> would return the last item with a non-zero rate
	 * 
	 * @param rate
	 * @return
	 */
	public T getItemForNormCumRate(double rate) {
		return getForCumRate(rate).item;
	}
	
	/**
	 * 
	 * @return the next randomly selected item
	 */
	public T nextItem() {
		return next().item;
	}
	
	/**
	 * Generates a series of randomly selected items.
	 * 
	 * @param size the number of items in the generated series
	 * @return series of randomly selected items
	 * @throws IllegalArgumentException if size is less than or equal to zero
	 */
	public List<T> generateSeries(int size) throws IllegalArgumentException {
		checkArgument(size > 0, "size must be > 0!");
		ArrayList<T> series = new ArrayList<T>();
		
		while (series.size() < size)
			series.add(nextItem());
		
		return series;
	}

}
