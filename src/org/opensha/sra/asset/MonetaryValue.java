package org.opensha.sra.asset;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class MonetaryValue implements Value {

	
	// TODO get mean, 4th and 96th %ile
	
	private int valueBasisYear;
	private double value;
	
	// TODO constructor()
	
	public String getCurrencyCode() {
		return null;
	}
	
	@Override
	public int getValueBasisYear() {
		return valueBasisYear;
	}
	
	public double getValue() {
		return value;
	}
	
	
	
	
	
	
}
