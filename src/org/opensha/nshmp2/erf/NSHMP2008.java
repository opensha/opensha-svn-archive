package org.opensha.nshmp2.erf;

import static org.opensha.nshmp2.util.SourceRegion.*;

import java.util.EnumSet;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.param.impl.EnumParameter;
import org.opensha.nshmp2.erf.source.GridERF;
import org.opensha.nshmp2.erf.source.NSHMP_ERF;
import org.opensha.nshmp2.erf.source.Sources;
import org.opensha.nshmp2.util.FaultType;
import org.opensha.nshmp2.util.FocalMech;
import org.opensha.sha.earthquake.EpistemicListERF;

import com.google.common.collect.Lists;

/**
 * Wrapper for NSHMP western US earthquake sources.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public class NSHMP2008 extends NSHMP_ListERF {

	private static final String BASE_NAME = "USGS NSHMP 2008";
	
	private NSHMP2008(String name) {
		super(name == null ? BASE_NAME : BASE_NAME + ": " + name);
		System.out.println("Loading sources...");
	}

	// want to get param group/list for each source type and add

	private void initParams() {

	}

	private static final String NSHMP_FAULT_TYPE_FILTER_PARAM_NAME = "Fault Type Filter";
	private static final String NSHMP_FOCAL_MECH_FILTER_PARAM_NAME = "Focal Mech Filter";

	private void initFaultParams() {

		EnumParameter<FaultType> faultTypeFilter = new EnumParameter<FaultType>(
			"Fault Type Filter", EnumSet.allOf(FaultType.class), null, "All");
		faultTypeFilter.addParameterChangeListener(this);

		EnumParameter<FocalMech> focalMechFilter = new EnumParameter<FocalMech>(
			"Focal Mech Filter", EnumSet.allOf(FocalMech.class), null, "All");
		focalMechFilter.addParameterChangeListener(this);

		
	}

	/**
	 * Returns an NHSMP {@code ERF} that includes all US sources.
	 * @return an NSHMP {@code ERF}
	 */
	public static NSHMP2008 create() {
		NSHMP2008 erf = new NSHMP2008(null);

		erf.addERFs(Sources.getGridList(CEUS));
		erf.addERFs(Sources.getFaultList(CEUS));
		erf.addERFs(Sources.getClusterList(CEUS));
		
		erf.addERFs(Sources.getGridList(WUS));
		erf.addERFs(Sources.getFaultList(WUS));
		erf.addERFs(Sources.getGridList(CA));
		erf.addERFs(Sources.getFaultList(CA));
		erf.addERFs(Sources.getSubductionList(CASC));
		
		return erf;
	}
	
	/**
	 * Returns an NSHMP {@code ERF} that includes only the single named source.
	 * @param name of source
	 * @return an NSHMP {@code ERF}
	 */
	public static NSHMP2008 createSingleSource(String name) {
		NSHMP2008 erf = new NSHMP2008(name);
		erf.addERF(Sources.get(name));
		return erf;
	}
	
	/**
	 * Returns an NSHMP {@code ERF} that includes California (i.e. UCERF2)
	 * sources. Note that this excludes the non-CA bFaults that are included
	 * in MeanUCERF2 as well as the CAdeep grid sources.
	 * @return an NSHMP {@code ERF}
	 */
	public static NSHMP2008 createCalifornia() {
		NSHMP2008 erf = new NSHMP2008(null);
		List<GridERF> gridERFs = Sources.getGridList(CA);
		// remove unwanted sources
		List<GridERF> removals = Lists.newArrayList();
		for (GridERF gerf : gridERFs) {
			if (gerf.getName().equals("CAdeep.in")) {
				removals.add(gerf);
			}
		}
		gridERFs.removeAll(removals);
		erf.addERFs(gridERFs);
		erf.addERFs(Sources.getFaultList(CA));
		// add additional WUS gridded sources
		erf.addERF(Sources.get("EXTmap.ch.in"));
		erf.addERF(Sources.get("EXTmap.gr.in"));
		erf.addERF(Sources.get("WUSmap.ch.in"));
		erf.addERF(Sources.get("WUSmap.gr.in"));
		return erf;
	}
	
	// should be able to filter sources of erf via custom iterators
	
	private void init() {
		
		addERFs(Sources.getGridList(CEUS));
		System.out.println("CEUS_GRD " + getSourceCount());
		addERFs(Sources.getFaultList(CEUS));
		System.out.println("CEUS_FLT " + getSourceCount());
		addERFs(Sources.getClusterList(CEUS));
		System.out.println("CEUS_CLU " + getSourceCount());
		
		addERFs(Sources.getGridList(WUS));
		System.out.println("WUS_GRD  " + getSourceCount());
		addERFs(Sources.getFaultList(WUS));
		System.out.println("WUS_FLT  " + getSourceCount());
		addERFs(Sources.getGridList(CA));
		System.out.println("CA_GRD   " + getSourceCount());
		addERFs(Sources.getFaultList(CA));
		System.out.println("CA_FLT   " + getSourceCount());
		addERFs(Sources.getSubductionList(CASC));
		System.out.println("WUS_SUB  " + getSourceCount());
		
//		addERFs(Sources.getFaultList(CA));
//		addERF(Sources.getFault("aFault_aPriori_D2.1.in"));
//		addERF(Sources.getFault("aFault_MoBal.in"));
//		addERF(Sources.getFault("aFault_unseg.in"));
//		addERF(Sources.getFault("bFault.ch.in"));
//		addERF(Sources.getFault("bFault.gr.in"));

//		for (NSHMP_ERF erf : this) {
//			System.out.println(erf.getName());
//		}

//		aFault_aPriori_D2.1.in
//		aFault_MoBal.in
//		aFault_unseg.in
//		bFault.ch.in
//		bFault.gr.in

		// north of Memphis on New Mdrid fault
//		addERF(GridERF.getTestGrid(new Location(35.6,-90.4)));
		
//		addERF(Sources.get("CEUS.2007all8.J.in"));

//		addERF(Sources.get("bFault.gr.in"));
		
//		addERF(Sources.get("bFault.ch.in"));
//		addERF(Sources.get("CAmap.21.gr.in"));
//		addERF(Sources.get("impext.ch.in"));
//		addERF(Sources.get("CAmap.24.gr.in"));

//		addERF(Sources.get("pnwdeep.in"));
//		addERF(Sources.get("CEUSchar.71.in"));
		
//		addERF(Sources.get("sangorg.in")); // fixed strike source results not great
			
//		addERF(Sources.get("newmad.1500.cluster.in"));
//		addERF(Sources.get("NMSZnocl.1000yr.5branch.in"));
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (NSHMP_ERF src : this) {
			sb.append(StringUtils.rightPad(src.getName(), 30));
			sb.append(StringUtils.rightPad(src.getSourceRegion().name(), 6));
			sb.append(	StringUtils.rightPad(src.getSourceType().name(), 14));
			sb.append(	StringUtils.rightPad(src.getSourceIMR().name(), 12));
			sb.append(	StringUtils.rightPad(String.valueOf(src.getSourceWeight()), 12));
			sb.append(	StringUtils.rightPad(String.valueOf(src.getRuptureCount()), 10));
			sb.append(	StringUtils.leftPad(String.valueOf(src.getMaxDistance()), 10));
			sb.append(IOUtils.LINE_SEPARATOR);
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		EpistemicListERF erf = createCalifornia();
		System.out.println(erf.toString());
//
//		WUS_ERF wus = new WUS_ERF();
//		wus.updateForecast();
//		System.out.println(wus);
		
		

//		while (true) {
//			try {
//				Thread.sleep(5000);
//			} catch (InterruptedException e) {
//					e.printStackTrace();
//			}
//				
//		}
	}

}
