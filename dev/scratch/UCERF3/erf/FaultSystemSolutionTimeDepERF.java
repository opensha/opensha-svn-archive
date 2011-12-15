package scratch.UCERF3.erf;

import java.io.File;
import java.util.List;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.eq.MagUtils;
import org.opensha.commons.param.impl.FileParameter;
import org.opensha.refFaultParamDb.vo.FaultSectionPrefData;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.param.FaultGridSpacingParam;
import org.opensha.sha.earthquake.rupForecastImpl.FaultRuptureSource;
import org.opensha.sha.magdist.GaussianMagFreqDist;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;

/**
 *
 */
public class FaultSystemSolutionTimeDepERF extends FaultSystemSolutionPoissonERF {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final boolean D = true;

	public static final String NAME = "Fault System Solution Time Dep ERF";
	
	
	/**
	 * This creates the ERF from the given file
	 * @param fullPathInputFile
	 */
	public FaultSystemSolutionTimeDepERF(FaultSystemSolution faultSysSolution) {
		super(faultSysSolution);
		initiateTimeSpan();
	}

	
	/**
	 * This creates the ERF from the given file
	 * @param fullPathInputFile
	 */
	public FaultSystemSolutionTimeDepERF(String fullPathInputFile) {
		super(fullPathInputFile);
		initiateTimeSpan();
	}

	
	/**
	 * This creates the ERF with a parameter for choosing the input file
	 */
	public FaultSystemSolutionTimeDepERF() {
		super();
		initiateTimeSpan();
	}
	
	protected void initiateTimeSpan() {
		timeSpan = new TimeSpan(TimeSpan.YEARS, TimeSpan.YEARS);
		timeSpan.setDuration(30.);
	}
	
	
	@Override
	public void updateForecast() {
		super.updateForecast();
	}
	

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getNumSources() {
		return nonZeroRateRupMapping.length;
	}

	@Override
	public ProbEqkSource getSource(int iSource) {
		
		int invRupIndex= nonZeroRateRupMapping[iSource];

		//		boolean isPoisson = true;
//		double prob = 1-Math.exp(-faultSysSolution.getRateForRup(invRupIndex)*timeSpan.getDuration());
//		FaultRuptureSource src = new FaultRuptureSource(faultSysSolution.getMagForRup(invRupIndex), 
//									  faultSysSolution.getCompoundGriddedSurfaceForRupupture(invRupIndex, faultGridSpacing), 
//									  faultSysSolution.getAveRakeForRup(invRupIndex), prob, isPoisson);
		
		double mag = faultSysSolution.getMagForRup(invRupIndex);
		double totMoRate = faultSysSolution.getRateForRup(invRupIndex)*MagUtils.magToMoment(mag);
		totMoRate *= getProbabilityGain(faultSysSolution.getFaultSectionDataForRupture(invRupIndex));
		GaussianMagFreqDist srcMFD = new GaussianMagFreqDist(5.05,8.65,37,mag,0.12,totMoRate,2.0,2);
		// NEED TO SET THE FOLLOWING AS NON-POISSON??
		FaultRuptureSource src = new FaultRuptureSource(srcMFD, 
				faultSysSolution.getCompoundGriddedSurfaceForRupupture(invRupIndex, faultGridSpacing),
				faultSysSolution.getAveRakeForRup(invRupIndex), timeSpan.getDuration());
		
		src.setName("Inversion Src #"+invRupIndex);
		return src;
	}
	
	public double getProbabilityGain(List<FaultSectionPrefData> dataList) {
		
		return 0;
	}

}
