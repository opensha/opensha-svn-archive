package scratchJavaDevelopers.martinez.LossCurveSandbox.math.distribution;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;

/**
 * <p>
 * This class serves as a wrapper class to the
 * <code>org.apache.commons.math.distribution.NormalDistributionImpl</code>
 * class. The underlying math for this class is handed off to the apache
 * implementation. The purpose of this class is to convert values provided by
 * the user from linear space into log space. Essentially a call to any method
 * in this class does the following:
 * </p>
 * <pre>
 * public Object foo(double val1, double val2) {
 * 	return super(java.lang.Math.log(val1), java.lang.Math.log(val2));
 * }
 * </pre>
 * <p>
 * As such, all values passed as arguments any method in this class should be
 * specified in linear space rather than log space. The conversion from linear
 * space to log space is done internally in this class and does
 * <strong>not</strong> need to be done by the user. Additionally, return values
 * will be specified back into linear space by taking <code>Math.exp(...)</code>
 * before returning.
 * </p>
 * 
 * <p><strong>ALL PARAMETERS SHOULD BE GIVEN IN LINEAR SPACE</strong></p>
 * 
 * @see org.apache.commons.math.distribution.NormalDistribution
 * @author <a href="mailto:emartinez@usgs.gov">Eric Martinez</a>
 */
public class LogNormalDistribution 
		extends NormalDistributionImpl implements NormalDistribution {

	// Variable used for static serialization
	private static final long	serialVersionUID	= 0x0180B68;	
	
	/**
	 * Instantiates a new instance of a <code>LogNormalDistribution</code>. Even
	 * though this distribution is defined to exist in log space, parameters to
	 * this constructor should be specified in linear space; the constructor will
	 * internally convert the values to log space using 
	 * <code>Math.log(...)</code>.
	 * 
	 * @param mean The mean (&mu;) parameter (in linear space).
	 * @param sd The standard deviation (&sigma;) parameter (in linear space).
	 */
	public LogNormalDistribution(double mean, double sd) {
		super(Math.log(mean), Math.log(sd));
	}
	
	/**
	 * For this distribution, X, this method returns P[X < x].
	 * @param The value at which the CDF is evaluated (in linear space).
	 * @return The CDF evaluated at <code>x</code>.
	 * @throws MathException If the algorithm fails to converge.
	 */
	public double cumulativeProbability(double x) throws MathException {
		return super.cumulativeProbability(Math.log(x));
	}

	/**
	 * <p>
	 * For a random variable X whose values are distributed according to this
	 * distribution, this method returns P[x0 <= X <= x1].
	 * </p>
	 * <p>The default implementation uses the identity</p>
	 * <p>P[x0 <= X <= x1] = P[X <= x1] - P[X <= x0]</p>
	 * 
	 * @param x0 The (inclusive) lower bound (in linear space).
	 * @param x1 The (inclusive) upper bound (in linear space).
	 * @return The probability that a random variable with this distribution will
	 * take a value between <code>x0</code> and <code>x1</code>, including the
	 * endpoints.
	 * @throws MathException If the cumulative probability can not be computed
	 * due to convergence or other numerical errors.
	 * @throws IllegalArgumentException If log(x0) > log(x1).
	 */
	public double cumulativeProbability(double x0, double x1)
			throws MathException {
		return super.cumulativeProbability(Math.log(x0), Math.log(x1));
	}
	
	/*
	// Not sure if these three methods need to be overridden for log normal...
	protected double getDomainLowerBound(double p) {
		return super.getDomainLowerBound(Math.log(p));
	}
	
	protected double getDomainUpperBound(double p) {
		return super.getDomainUpperBound(Math.log(p));
	}
	
	protected double getInitialDomain(double p) {
		return super.getInitialDomain(Math.log)
	}
	*/
	
	/**
	 * Access the mean (in linear space).
	 * @return The mean (&mu;) for this distribution.
	 */
	public double getMean() {
		return Math.exp(super.getMean());
	}
	
	/**
	 * Access the standard deviation (in linear space).
	 * @return The standard deviation (&sigma;) for this distribution.
	 */
	public double getStandardDeviation() {
		return Math.exp(super.getStandardDeviation());
	}
	
	/*
	// Not sure if we need (or even can do) this. Hmm....
	public double inverseCumulativeProbability(double p) {
		return super.inverseCumulativeProbability(Math.log(p);
	}
	*/
	
	/**
	 * Modify the mean.
	 * @param mean The new mean (&mu;) for this distribution (in linear space).
	 */
	public void setMean(double mean) {
		super.setMean(Math.log(mean));
	}
	
	/**
	 * Modify the standard deviation.
	 * @param sd The new standard deviation (&sigma;) for this distribution (in 
	 * linear space).
	 * @throws IllegalArgumentException If the log of <code>sd</code> is not
	 * positive.
	 */
	public void setStandardDeviation(double sd) {
		super.setStandardDeviation(Math.log(sd));
	}
}