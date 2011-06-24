/**
 * 
 */
package scratch.UCERF3;

import java.util.ArrayList;

import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;


/**
 * 
 * @author Field, Milner, Page, & Powers
 *
 */
public interface FaultSystemRupSet {
	
	public int getNumRupRuptures();
	
	public int getNumSections();
	
	public ArrayList<Integer> getSectionsIndicesForRup(int rupIndex);

	public double[] getMagForAllRups();

	public double getMagForRup(int rupIndex);

	public double[] getAveSlipForAllRups();
	
	public double getAveSlipForRup(int rupIndex);
	
	public ArrayList<double[]> getSlipOnSectionsForAllRups();
	
	public double[] getSlipOnSectionsForRup(int rthRup);
	
	public double getAveRakeForRup(int rupIndex);
	
	/**
	 * @param rupIndex
	 * @return Area (SI units: sq-m)
	 */
	public double getAreaForRup(int rupIndex);
	
	/**
	 * @param sectIndex
	 * @return Area (SI units: sq-m)
	 */
	public double getAreaForSection(int sectIndex);
	
	public FaultSectionPrefData getFaultSectionData(int sectIndex);

	public String getInfoString();
	
}
