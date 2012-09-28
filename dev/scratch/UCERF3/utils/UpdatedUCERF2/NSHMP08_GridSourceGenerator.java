package scratch.UCERF3.utils.UpdatedUCERF2;

import java.util.Arrays;
import java.util.List;

import org.opensha.nshmp2.erf.NSHMP2008;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.Point2Vert_FaultPoisSource;
import org.opensha.sha.magdist.SummedMagFreqDist;

import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class NSHMP08_GridSourceGenerator {
	
	private NSHMP2008 gridListERF;
	
	// array of summed erf source counts to facilitate
	// indexing of nested erfs
	private int[] erfIndices;
	
	public NSHMP08_GridSourceGenerator() {
		gridListERF = NSHMP2008.createCaliforniaGridded();
		int prevValue = 0;
		List<Integer> indexList = Lists.newArrayList();
		for (ERF erf : gridListERF) {
			int currentTotal = erf.getNumSources() + prevValue;
			indexList.add(currentTotal);
			prevValue = currentTotal;
		}
		erfIndices = Ints.toArray(indexList);
	}
	
	public ProbEqkSource getSource(int i) {
		int erfIdx = Arrays.binarySearch(erfIndices, i);
		if (erfIdx < 0) erfIdx = -(i+2);
		int srcIdx = i - erfIndices[erfIdx];
		return gridListERF.getERF(erfIdx).getSource(srcIdx);
	}
	
	public int getNumSources() {
		return gridListERF.getSourceCount();
	}
	
	public void setForecastDuration(double duration) {
		gridListERF.getTimeSpan().setDuration(duration);
	}

}
