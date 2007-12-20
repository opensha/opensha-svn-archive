package scratchJavaDevelopers.martinez.LossCurveSandbox.math.distribution;

import org.apache.commons.math.distribution.BinomialDistribution;
import org.apache.commons.math.distribution.ChiSquaredDistribution;
import org.apache.commons.math.distribution.DistributionFactoryImpl;
import org.apache.commons.math.distribution.ExponentialDistribution;
import org.apache.commons.math.distribution.FDistribution;
import org.apache.commons.math.distribution.GammaDistribution;
import org.apache.commons.math.distribution.HypergeometricDistribution;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.PoissonDistribution;
import org.apache.commons.math.distribution.TDistribution;

/**
 * An <code>AdvDistributionFactory</code> is a simple extension of the apache
 * commons 
 * <code>org.apache.commons.math.distribution.DistributionFactoryImpl</code>
 * factory class. It provides the same create methods with wrapper acessors
 * such that all arguments take their object-equivalent form. This allows for
 * easier introspection and dynamic invocation. In addition to these wrappers,
 * this also provides a create method for a <code>LogNormalDistribution</code>.
 * 
 * @author   
 * <a href="mailto:emartinez@usgs.gov?subject=NSHMP%20Application%20Question">
 * Eric Martinez
 * </a>
 */
public class AdvDistributionFactory extends DistributionFactoryImpl {

	/**
	 * The constructor is made private such that instantiation should happen from
	 * a call to the <code>newInstance</code> method. This is a factory
	 * convention and implementation is as such for no other reason.
	 */
	private AdvDistributionFactory() {
		super();
	}
	
	/**
	 * Create an instance of an <code>AdvDistributionFactory</code>.
	 * @return A new factory.
	 */
	public static AdvDistributionFactory newInstance() {
		return new AdvDistributionFactory();
	}
	
	/**
	 * Create a new log normal distribution with the given mean and standard
	 * deviation (in linear space).
	 * 
	 * @param mean The mean of the distribution (in linear space).
	 * @param sd The standard deviation (in linear space).
	 * @return A new normal distribution.
	 */
	public LogNormalDistribution createLogNormalDistribution(double mdf,
			double cov) {
		double sd   = Math.sqrt(Math.log(1 + Math.pow(cov, 2)));
		double mean = Math.log(mdf) - 0.5 * Math.pow(sd, 2);		
		return new LogNormalDistribution(mean, sd);
	}
	
	//----------- Wrapper Methods with "Objectified" Parameters ---------------//
	
	/**
	 * This method simple wraps the <code>create</code> method of the same name
	 * that takes primitive types. We wrap the <code>create</code> method with
	 * another method here that accepts the object versions of the arguments.
	 * This is done so we can inspect the class to find the required method.
	 */
	public LogNormalDistribution createLogNormalDistribution(Double mdf,
			Double cov) {
		return createLogNormalDistribution((double) mdf, (double) cov);
	}
	
	/**
	 * This method simple wraps the <code>create</code> method of the same name
	 * that takes primitive types. We wrap the <code>create</code> method with
	 * another method here that accepts the object versions of the arguments.
	 * This is done so we can inspect the class to find the required method.
	 */
	public BinomialDistribution createBinomialDistribution(
			Integer numberOfTrials, Double probabilityOfSuccess) {
		return super.createBinomialDistribution((int) numberOfTrials,
				(double) probabilityOfSuccess);
	}
	
	/**
	 * This method simple wraps the <code>create</code> method of the same name
	 * that takes primitive types. We wrap the <code>create</code> method with
	 * another method here that accepts the object versions of the arguments.
	 * This is done so we can inspect the class to find the required method.
	 */
	public ChiSquaredDistribution createChiSquareDistribution(
			Double degreesOfFreedom) {
		return super.createChiSquareDistribution((double) degreesOfFreedom);
	}
	
	/**
	 * This method simple wraps the <code>create</code> method of the same name
	 * that takes primitive types. We wrap the <code>create</code> method with
	 * another method here that accepts the object versions of the arguments.
	 * This is done so we can inspect the class to find the required method.
	 */
	public ExponentialDistribution createExponentialDistribution(
			Double mean) {
		return super.createExponentialDistribution((double) mean);
	}
	
	/**
	 * This method simple wraps the <code>create</code> method of the same name
	 * that takes primitive types. We wrap the <code>create</code> method with
	 * another method here that accepts the object versions of the arguments.
	 * This is done so we can inspect the class to find the required method.
	 */
	public FDistribution createFDistribution(Double numeratorDegreesOfFreedom,
			Double denominatorDegreesOfFreedom) {
		return super.createFDistribution((double) numeratorDegreesOfFreedom,
				(double) denominatorDegreesOfFreedom);
	}
	
	/**
	 * This method simple wraps the <code>create</code> method of the same name
	 * that takes primitive types. We wrap the <code>create</code> method with
	 * another method here that accepts the object versions of the arguments.
	 * This is done so we can inspect the class to find the required method.
	 */
	public GammaDistribution createGammaDistribution(Double alpha, Double beta) {
		return super.createGammaDistribution((double) alpha, (double) beta);
	}
	
	/**
	 * This method simple wraps the <code>create</code> method of the same name
	 * that takes primitive types. We wrap the <code>create</code> method with
	 * another method here that accepts the object versions of the arguments.
	 * This is done so we can inspect the class to find the required method.
	 */
	public HypergeometricDistribution createHypergeometricDistribution(
			Integer populationSize, Integer numberOfSuccesses, Integer sampleSize){
		return super.createHypergeometricDistribution((int) populationSize,
				(int) numberOfSuccesses, (int) sampleSize);
	}
	
	/**
	 * This method simple wraps the <code>create</code> method of the same name
	 * that takes primitive types. We wrap the <code>create</code> method with
	 * another method here that accepts the object versions of the arguments.
	 * This is done so we can inspect the class to find the required method.
	 */
	public NormalDistribution createNormalDistribution(Double mean, Double sd) {
		return super.createNormalDistribution((double) mean, (double) sd);
	}
	
	/**
	 * This method simple wraps the <code>create</code> method of the same name
	 * that takes primitive types. We wrap the <code>create</code> method with
	 * another method here that accepts the object versions of the arguments.
	 * This is done so we can inspect the class to find the required method.
	 */
	public PoissonDistribution createPoissonDistribution(Double lambda) {
		return super.createPoissonDistribution((double) lambda);
	}
	
	/**
	 * This method simple wraps the <code>create</code> method of the same name
	 * that takes primitive types. We wrap the <code>create</code> method with
	 * another method here that accepts the object versions of the arguments.
	 * This is done so we can inspect the class to find the required method.
	 */
	public TDistribution createTDistribution(Double degreesOfFreedom) {
		return super.createTDistribution((double) degreesOfFreedom);
	}
}
