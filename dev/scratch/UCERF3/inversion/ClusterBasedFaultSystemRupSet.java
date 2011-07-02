package scratch.UCERF3.inversion;

import java.util.List;

import scratch.UCERF3.FaultSystemRupSet;

public interface ClusterBasedFaultSystemRupSet extends FaultSystemRupSet {
	
	/**
	 * 
	 * @return the number of clusters
	 */
	public int getNumClusters();
	
	/**
	 * 
	 * @param index index of the cluster to get
	 * @return number of ruptures in the given cluster
	 */
	public int getNumRupturesForCluster(int index);
	
	/**
	 * 
	 * @param index index of the cluster to get
	 * @return list of section IDs in the cluster at the given index
	 */
	public List<Integer> getSectionsForCluster(int index);
	
	/**
	 * 
	 * @param index index of the cluster to get
	 * @return list of rupture indexes for the cluster at the given index
	 * @throws IndexOutOfBoundsException if the index is invalid
	 */
	public List<Integer> getRupturesForCluster(int index) throws IndexOutOfBoundsException;

}
