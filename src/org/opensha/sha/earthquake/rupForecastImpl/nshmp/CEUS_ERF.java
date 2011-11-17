package org.opensha.sha.earthquake.rupForecastImpl.nshmp;


import static org.opensha.sha.nshmp.SourceRegion.*;
import static org.opensha.sha.nshmp.SourceType.*;

import org.opensha.sha.earthquake.rupForecastImpl.nshmp.source.GridERF;
import org.opensha.sha.earthquake.rupForecastImpl.nshmp.source.Sources;
import org.opensha.sha.nshmp.MultiSourceERF;

/**
 * Add comments here
 *
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class CEUS_ERF extends MultiSourceERF {

	public CEUS_ERF() {
		super("NSHMP East");
		
		GridERF erf = Sources.getGrid(CEUS, GRIDDED, "CEUS.2007all8.AB.in");
		addERF(GRIDDED, erf);
//		 List<GridERF> list = Sources.getGridList(CEUS);
		 //System.out.println(getNumSources());
		//addERFs(GRIDDED, Sources.getGridList(CEUS));
		
	}
	
	
//	private static double minLat = 24.6;
//	private static double maxLat = 50.0;
//	private static double dLat = 0.1;
//	
//	private static double minLon = -115.0;
//	private static double maxLon = -65.0;
//	private static double dLon = 0.1;
	
	
	public static void main(String[] args) {
		CEUS_ERF erf = new CEUS_ERF();
		
		//		System.out.println(SourceImrWeight.CHARLESTON.imrWeights());
//		Map<String, GridSource> ceusgrd = Sources.getGridSourcesCEUS();
		
		// default iterates over all sources
		// should be able to pick an individual source [FaultERF | GridERF]
		// should be able to filter sources by type, subtype
		
		// for single ceus grid source
		// grid source triggers initialization of tables for AB | J 
		// if they don't alrady exist - calculator needs to set source type in
		// in imr and Mw conversion
	}
	
	
	
	public enum SourceCEUS {
		GRID,
		GRID_FIXED, // Charleston
		FAULT;
	}
	

}
