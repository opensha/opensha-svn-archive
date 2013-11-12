package org.opensha.sha.earthquake.param;

/**
 * Magnitude-dependent aperiodicity options for the discrete magnitude ranges given by aperMagThreshArray.
 * @author Ned Field
 * @version $Id:$
 */
@SuppressWarnings("javadoc")
public enum MagDependentAperiodicityOptions {
	LOW_VALUES("0.4,0.3,0.2,0.1", new double[] {0.4,0.3,0.2,0.1}),
	MID_VALUES("0.5,0.4,0.3,0.2", new double[] {0.5,0.4,0.3,0.2}),
	HIGH_VALUES("0.6,0.5,0.4,0.3",new double[] {0.6,0.5,0.4,0.3});
	
	private String label;
	private static double[] aperMagThreshArray = {6.7,7.2,7.7};
	
	private double[] aperValuesArray;
	private MagDependentAperiodicityOptions(String label, double[] aperValuesArray) {
		this.label = label;
		this.aperValuesArray=aperValuesArray;
	}
	
	@Override public String toString() {
		return label;
	}
	
	public double[] getAperValuesArray(){
		return aperValuesArray;
	}
	
	public static double[] getAperMagThreshArray(){
		return aperMagThreshArray;
	}

}
