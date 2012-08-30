/**
 * 
 */
package scratch.UCERF3.inversion;


/**
 * This interface contains methods needed by both InversionFaultSystemRupSet and
 * InversionFaultSystemSolution (needed because the latter does not extend the former)
 * 
 * @author field
 *
 */
public interface InversionFaultSystemSolutionInterface {
	
	/**
	 * This returns the final minimum mag for a given fault section
	 * (e.g., after filtering what's returned returned by getOrigMinMagForSection(*))
	 * @param sectIndex
	 * @return
	 */
	public double getFinalMinMagForSection(int sectIndex);
	
	
	/**
	 * This tells whether the given rup is below any of the final minimum magnitudes 
	 * of the sections utilized by the rup.	 
	 * @param rupIndex
	 * @return
	 */
	public boolean isRuptureBelowSectMinMag(int rupIndex);
	
	
	/**
	 * This returns the upper magnitude of sub-seismogenic ruptures (the bin-centered value)
	 * @param sectIndex
	 * @return
	 */
	public double getUpperMagForSubseismoRuptures(int sectIndex);


}
