package scratch.peter.nga;


/**
 * Default wrapper for ground motion prediction equation (GMPE) results.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class DefaultGroundMotion implements ScalarGroundMotion {

	private double mean;
	private double sigma;
	
	/**
	 * Create a new ground motion container.
	 * 
	 * @param mean ground motion (in natural log units)
	 * @param sigma aleatory uncertainty
	 */
	public DefaultGroundMotion(double mean, double sigma) {
		this.mean = mean;
		this.sigma = sigma;
	}

	@Override public double mean() { return mean; }
	@Override public double stdDev() { return sigma; }

}
