package scratch.UCERF3.inversion;

import java.util.List;

import org.dom4j.Element;

import scratch.UCERF3.FaultSystemRupSet;
import scratch.UCERF3.utils.SimpleFaultSystemSolution;

public class SimpleClusterBasedFaultSystemSolution extends
		SimpleFaultSystemSolution implements ClusterBasedFaultSystemRupSet {

	private ClusterBasedFaultSystemRupSet rupSet;
	
	public SimpleClusterBasedFaultSystemSolution(ClusterBasedFaultSystemRupSet rupSet,
			double[] rupRateSolution) {
		super(rupSet, rupRateSolution);
		this.rupSet = rupSet;
	}

	@Override
	public int getNumClusters() {
		return rupSet.getNumClusters();
	}

	@Override
	public int getNumRupturesForCluster(int index) {
		return rupSet.getNumRupturesForCluster(index);
	}

	@Override
	public List<Integer> getSectionsForCluster(int index) {
		return rupSet.getSectionsForCluster(index);
	}

	@Override
	public List<Integer> getRupturesForCluster(int index)
			throws IndexOutOfBoundsException {
		return rupSet.getRupturesForCluster(index);
	}
	
	public static SimpleClusterBasedFaultSystemSolution fromXMLMetadata(Element solutionEl) {
		return (SimpleClusterBasedFaultSystemSolution)SimpleFaultSystemSolution.fromXMLMetadata(solutionEl);
	}

}
