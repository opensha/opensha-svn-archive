package scratch.peter.ucerf3.calc;

import java.io.File;

import org.opensha.nshmp2.util.Period;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class AggregateResults {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File dir = new File(args[0]);
		Period period = Period.valueOf(Period.class, args[1]);
		UC3_HazardCalcDriverMPJ.aggregateResults(dir, period);
	}

}
