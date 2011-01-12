package org.opensha.commons.data;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class WeightedList<E> {
	
	private ArrayList<E> objects;
	private ArrayList<Double> weights;
	
	private boolean forceNormalization = false;
	
	public WeightedList() {
		this(new ArrayList<E>(), new ArrayList<Double>());
	}
	
	public WeightedList(ArrayList<E> objects, ArrayList<Double> weights) {
		set(objects, weights);
	}
	
	/**
	 * This checks that the weight and object lists are non null and have the same number of items.
	 * If normalization is forced, then it is checked here.
	 * 
	 * @param objects
	 * @param weights
	 * @throws IllegalStateException if lists are of different sizes
	 * @throws IllegalArgumentException if lists are null
	 */
	private void validate(ArrayList<?> objects, ArrayList<Double> weights)
	throws IllegalStateException, IllegalArgumentException {
		if (objects == null)
			throw new IllegalArgumentException("object list cannot be null!");
		if (weights == null)
			throw new IllegalArgumentException("weights list cannot be null!");
		
		if (objects.size() != weights.size())
			throw new IllegalStateException("object and weight lists must be the same size!");
		
		if (forceNormalization && weights.size() > 0) {
			if (!isNormalized(weights))
				throw new IllegalStateException("wights must sum to 1 (current sum: "+getWeightSum()+")");
		}
	}
	
	public void add(E object, double weight) throws IllegalStateException {
		this.objects.add(object);
		this.weights.add(weight);
		
		try {
			validate(objects, weights);
		} catch (RuntimeException e) {
			this.objects.remove(objects.size()-1);
			this.weights.remove(weights.size()-1);
			throw e;
		}
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
	
	public void setWeight(int i, double weight) {
		double orig = weights.get(i);
		this.weights.set(i, weight);
		try {
			validate(objects, weights);
		} catch (RuntimeException e) {
			this.weights.set(i, orig);
			throw e;
		}
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
		
//		System.out.println("***** Set called *****");
//		for (double weight : weights)
//			System.out.println(weight);
	}
	
	public int size() {
		return objects.size();
	}
	
	public double getWeight(int i) {
		return weights.get(i);
	}
	
	public double getWeight(E object) {
		int ind = objects.indexOf(object);
		if (ind < 0)
			throw new NoSuchElementException();
		return getWeight(ind);
	}
	
	public E get(int i) {
		return objects.get(i);
	}
	
	public boolean areWeightsEqual() {
		if (weights.size() == 0)
			return false;
		double wt0 = weights.get(0);
		
		for (double weight : weights)
			if (weight != wt0)
				return false;
		return true;
	}
	
	public void setWeightsEqual() {
		if (areWeightsEqual())
			return;
		
		double wt = 1d / (double)size();
		
		setWeightsToConstant(wt);
	}
	
	public void setWeightsToConstant(double weight) {
		ArrayList<Double> newWeights = new ArrayList<Double>();
		for (int i=0; i<size(); i++)
			newWeights.add(weight);
		
		setWeights(newWeights);
	}
	
	public void normalize() {
		if (isNormalized())
			return;
		
		double sum = getWeightSum();
		ArrayList<Double> newWeights = new ArrayList<Double>();
		
		for (double weight : weights) {
			double newWeight = weight / sum;
			newWeights.add(newWeight);
		}
		
		setWeights(newWeights);
	}
	
	
	public double getWeightSum() {
		return getWeightSum(weights);
	}
	
	private static double getWeightSum(ArrayList<Double> weights) {
		double sum = 0;
		for (double weight : weights)
			sum += weight;
		return sum;
	}
	
	public boolean isNormalized() {
		return isNormalized(weights);
	}
	
	private static boolean isNormalized(ArrayList<Double> weights) {
		float sum = (float)getWeightSum(weights);
		return sum == 1f;
	}
	
	public void setForceNormalization(boolean forceNormalization) {
		this.forceNormalization = forceNormalization;
		if (forceNormalization)
			normalize();
	}
	
	public boolean isForceNormalization() {
		return forceNormalization;
	}

}