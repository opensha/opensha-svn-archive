package scratch.UCERF3.griddedSeismicity;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.magdist.IncrementalMagFreqDist;

import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;

/**
 * This class generates the gridded sources for the UCERF3 background seismicity.
 * @author field
 *
 */
public class UCERF3_GridSourceGenerator {
	
	// this class could extend the following
	final static CaliforniaRegions.RELM_TESTING_GRIDDED griddedRegion  = new CaliforniaRegions.RELM_TESTING_GRIDDED();
	
	final static int numLocs = griddedRegion.getNodeCount();
	
	IncrementalMagFreqDist totalOffFaultMFD;
	IncrementalMagFreqDist realOffFaultMFD;	// this has the sub-seismo fault section rupture removed
	IncrementalMagFreqDist[] subSeismoFaultSectMFD_Array;	// one for each fault section in the faultSystemSolution
	
	double totalMgt5_SeisRate;	// to
	
	double[] fractOfNodeOutsideFaultPolygons = new double[numLocs];	// fraction of each node that is outside all fault-section polygons
	double[] origSpatialPDF = new double[numLocs];		// from Karen or from UCERF2 (or maybe a deformation model)
	double[] revisedSpatialPDF = new double[numLocs];	// revised to cut fault-section-polygon areas out (and renormalized)
	
	FaultSystemSolution faultSystemSolution;
	
	double totalMgt5_Rate;
	
	
	/**
	 * Options:
	 * 
	 * 1) set a-values in fault-section polygons from moment-rate reduction or from smoothed seismicity
	 * 2) focal mechanism options, and finite vs point sources (cross hair, random strike, etc)?
	 * 
	 * 
	 * @param faultSystemSolution 
	 * @param totalOffFaultMFD - can this be obtained from the faultSystemSolution?
	 * @param spatialPDFofSeis - e.g., Karen's file
	 */
	public UCERF3_GridSourceGenerator(SimpleFaultSystemSolution faultSystemSolution, IncrementalMagFreqDist totalOffFaultMFD,
			double[] spatialPDFofSeis, double totalMgt5_Rate) {
		
		this.faultSystemSolution=faultSystemSolution;
		this.totalOffFaultMFD=totalOffFaultMFD;
		
		if(spatialPDFofSeis.length == numLocs)
			origSpatialPDF = spatialPDFofSeis;
		else
			throw new  RuntimeException("spatialPDFofSeis.length does not equal numLocs");
		
		this.totalMgt5_Rate=totalMgt5_Rate;
		
		
		/*
		 *  1) compute subSeismoFaultSectMFD_Array, the sub-seismo MFD for each fault section.  Do this by
		 *  duplicating totalOffFaultMFD to faultSectionMFD, setting all rates above Mmin for the fault to zero
		 *  (to avoid double counting), and then constraining the total rate by either 
		 *  
		 *  	a) fault-section moment rate reduction (need get method for this) or
		 *  
		 *  	b) total smoothed seismicity rate inside polygon; need get method for this, and
		 *         subtract total rate of supra-seismogenic ruptures from:
		 *         faultSystemSolution.calcNucleationMFD_forSect(sectIndex, minMag, maxMag, numMag)

		 *  
		 *  2) determine the fraction of each fault-section MFD that goes to each associated node 
		 *     (need an object that holds a list of node indices and their associated wts)
		 *  
		 *  3) determine the fraction of each node that is outside all fault-section polygons 
		 *  (fractOfNodeOutsideFaultPolygons[])
		 *  
		 *  4) compute realOffFaultMFD by subtracting all the fault-section MFDs from totalOffFaultMFD
		 *  
		 *  5) create revisedSpatialPDF by multiplying origSpatialPDF by fractOfNodeOutsideFaultPolygons[], and renormalize to
		 *  1.0.  Each value here multiplied by realOffFaultMFD is now the off-fault MFD for each node
		 *  
		 *  6) Each node also has some number of sub-seismo fault MFDs as determined in 2 (need to be able to query the
		 *     indices of fault sections associated with a given node, as well as the weight the node gets for each 
		 *     fault-section MFD).
		 *     
		 *  7) Summing all the MFDs for each node should give back totalOffFaultMFD
		 *  
		 *  Issue - if choice (a) in (1) is much higher on average than choice (b), we will have suppressed all truly off-fault
		 *  rates
		 */
		
		
		
		
	}

	/**
	 * For the location nodes that overlap with the given fault-section polygon, this returns the 
	 * fraction of the fault-section that applies to each node.
	 * 
	 * This should return a list of node indices and corresponding fractions (where the latter sum to 1.0).
	 * The sub-seismo rups for this fault section will be partitioned among the nodes according to the
	 * fractions given here
	 * 
	 */
	//TODO - finish
	private void getNodeFractionsForFaultSection(int sectIndex) {
		
		// Need to get the polygon for the section, and then compute 
		
	}
	
	
	//TODO finish
	public int getNumSources() {
		
		return 0;
	}
	
	
	//TODO - finish
	public ProbEqkSource getSource(int srcIndex) {
		
		return null;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	


}
