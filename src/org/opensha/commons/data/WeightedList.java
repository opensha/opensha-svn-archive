package org.opensha.commons.data;

import java.util.ArrayList;

public class WeightedList<E> {
	
	private ArrayList<E> objects;
	private ArrayList<Double> weights;
	
	public WeightedList() {
		this(new ArrayList<E>(), new ArrayList<Double>());
	}
	
	public WeightedList(ArrayList<E> objects, ArrayList<Double> weights) {
		set(objects, weights);
	}
	
	/**
	 * This checks that the weight and object lists have the same number of items, and that the
	 * weights sum to 1.
	 * 
	 * @param objects
	 * @param weights
	 * @throws IllegalStateException
	 */
	private static void validate(ArrayList<?> objects, ArrayList<Double> weights) throws IllegalStateException {
		if (objects == null && weights != null)
			throw new IllegalStateException("objects are null but weights are not!");
		else if (objects != null && weights == null)
			throw new IllegalStateException("weights are null but objects are not!");
		else if (objects == null && weights == null)
			return;
		
		if (objects.size() != weights.size())
			throw new IllegalStateException("object and weight lists must be the same size!");
		
		if (weights.size() > 0) {
			double sum = 0;
			for (double weight : weights)
				sum += weight;
			if ((float)sum != 1f)
				throw new IllegalStateException("wights must sum to 1");
		}
	}
	
	/**
	 * Adds the given object to the list, and sets the weights (which must be adjusted for the new
	 * object).
	 * 
	 * @param object
	 * @param newWeights
	 * @throws IllegalStateException if the weights are invalid
	 */
	public void add(E object, ArrayList<Double> newWeights) throws IllegalStateException {
		this.objects.add(object);
		setWeights(newWeights);
	}
	
	/**
	 * Sets and validates the weights
	 * 
	 * @param newWeights
	 * @throws IllegalStateException if the weights are invalid
	 */
	public void setWeights(ArrayList<Double> newWeights) throws IllegalStateException {
		set(objects, newWeights);
	}
	
	/**
	 * Set the list of objects
	 * 
	 * @param objects
	 * @throws IllegalStateException if the objects and weights are invalid
	 */
	public void setObjects(ArrayList<E> objects) throws IllegalStateException {
		set(objects, weights);
	}
	
	/**
	 * Set both the objects and the weights
	 * 
	 * @param objects
	 * @param weights
	 * @throws IllegalStateException if the objects and weights are invalid
	 */
	public void set(ArrayList<E> objects, ArrayList<Double> weights) throws IllegalStateException {
		validate(objects, weights);
		
		this.objects = objects;
		this.weights = weights;
	}
	
	public int size() {
		return objects.size();
	}
	
	public double getWeight(int i) {
		return weights.get(i);
	}
	
	public double getWeight(E object) {
		return getWeight(objects.indexOf(object));
	}
	
	public E get(int i) {
		return objects.get(i);
	}

}
