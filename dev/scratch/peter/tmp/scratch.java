package scratch.peter.tmp;

import org.opensha.nshmp.NEHRP_TestCity;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class scratch {

	
	public static void main(String[] args) {
		for (NEHRP_TestCity city : NEHRP_TestCity.values()) {
			System.out.println(city);
		}
	}
}
