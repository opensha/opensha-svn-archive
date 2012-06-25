package scratch.UCERF3.enumTreeBranches;

import java.util.ArrayList;
import java.util.List;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

import scratch.UCERF3.analysis.DeformationModelsCalc;
import scratch.UCERF3.griddedSeismicity.GridReader;
import scratch.UCERF3.logicTree.LogicTreeBranchNode;
import scratch.UCERF3.utils.DeformationModelOffFaultMoRateData;
import scratch.UCERF3.utils.RELM_RegionUtils;

/**
 * UCERF spatial seismicity pdfs.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
@SuppressWarnings("javadoc")
public enum SpatialSeisPDF implements LogicTreeBranchNode<SpatialSeisPDF> {
	
	// TODO set weights differently for GR
	UCERF2("UCERF2",										"U2",		0.5d) { // TODO: 0.25 for GR
		@Override public double[] getPDF() {
			return new GridReader("SmoothSeis_UCERF2.txt").getValues();
		}
	},
	
	UCERF3("UCERF3",										"U3",		0.5d) { // TODO: 0.25 for GR
		@Override public double[] getPDF() {
			return new GridReader("SmoothSeis_KF_5-5-2012.txt").getValues();
		}
	},
	
	/**
	 * This does not include the geologic off-fault deformation model
	 */
	AVG_DEF_MODEL_OFF("Average Deformation Model Off Fault",	"AveDM_off",	0d) { // TODO: 0.5 for GR
		@Override public double[] getPDF() {
			CaliforniaRegions.RELM_TESTING_GRIDDED region = RELM_RegionUtils.getGriddedRegionInstance();
			GriddedGeoDataSet xyz = DeformationModelOffFaultMoRateData.getInstance().getAveDefModelPDF(false);
			List<Double> vals = Lists.newArrayList();
			for (Location loc : region) {
				vals.add(xyz.get(loc));
			}
			return Doubles.toArray(vals);
		}
	},
	
	/**
	 * This includes the geologic deformation model, for which the off-fault s
	 * patial distribution is uniform
	 */
	AVG_DEF_MODEL_ALL("Average Deformation Model Including Faults",	"AveDM_all",	0d) {
		@Override public double[] getPDF() {
			CaliforniaRegions.RELM_TESTING_GRIDDED region = RELM_RegionUtils.getGriddedRegionInstance();
			GriddedGeoDataSet xyz = DeformationModelsCalc.getAveDefModSpatialPDF_WithFaults(false);
			List<Double> vals = Lists.newArrayList();
			for (Location loc : region) {
				vals.add(xyz.get(loc));
			}
			return Doubles.toArray(vals);
		}
	};

	
	private String name, shortName;
	private double weight;
	
	private SpatialSeisPDF(String name, String shortName, double weight) {
		this.name = name;
		this.shortName = shortName;
		this.weight = weight;
	}
	
	public abstract double[] getPDF();
	
	/**
	 * This returns the total sum of values inside the given gridded region
	 * @param region
	 * @return
	 */
	public double getFractionInRegion(GriddedRegion region) {
		double[] vals = this.getPDF();
		double sum=0;
		CaliforniaRegions.RELM_TESTING_GRIDDED relmRegion = RELM_RegionUtils.getGriddedRegionInstance();
		for(int i=0; i<region.getNumLocations(); i++) {
			int iLoc = relmRegion.indexForLocation(region.getLocation(i));
			if(iLoc != -1)
				sum += vals[iLoc];
		}
		return sum;
	}
	
	public static void testSums() {
		ArrayList<SpatialSeisPDF> testPDF_List = new ArrayList<SpatialSeisPDF>();
		testPDF_List.add(SpatialSeisPDF.UCERF3);
		testPDF_List.add(SpatialSeisPDF.UCERF2);
		testPDF_List.add(SpatialSeisPDF.AVG_DEF_MODEL_OFF);
		for(SpatialSeisPDF testPDF : testPDF_List) {
			double[] vals = testPDF.getPDF();
			double sum=0;
			for(int i=0; i<vals.length;i++) sum += vals[i];
			System.out.println(testPDF+" sum = "+(float)sum);
		}
	}
	
	@Override
	public String getShortName() {
		return shortName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public double getRelativeWeight() {
		return weight;
	}

	@Override
	public String encodeChoiceString() {
		return "SpatSeis"+getShortName();
	}
	
	/**
	 * This tests getFractionInRegion(GriddedRegion region)
	 * @param args
	 */
	public static void main(String[] args) {
		testSums();
		GriddedRegion noCalGrid = RELM_RegionUtils.getNoCalGriddedRegionInstance();
		GriddedRegion soCalGrid = RELM_RegionUtils.getSoCalGriddedRegionInstance();
		double noFrac, soFrac;
		ArrayList<SpatialSeisPDF> testPDF_List = new ArrayList<SpatialSeisPDF>();
		testPDF_List.add(SpatialSeisPDF.UCERF3);
		testPDF_List.add(SpatialSeisPDF.UCERF2);
		testPDF_List.add(SpatialSeisPDF.AVG_DEF_MODEL_OFF);
		for(SpatialSeisPDF testPDF : testPDF_List) {
			noFrac = testPDF.getFractionInRegion(noCalGrid);
			soFrac = testPDF.getFractionInRegion(soCalGrid);
			System.out.println(testPDF+"\tnoFrac="+(float)noFrac+"\tsoFrac="+(float)soFrac+"\tsum="+(float)(noFrac+soFrac));
		}
	}
}
