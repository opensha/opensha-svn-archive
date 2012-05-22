package scratch.UCERF3.griddedSeismicity;

import java.util.List;

import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.geo.Location;

import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

import scratch.UCERF3.utils.DeformationModelOffFaultMoRateData;

/**
 * UCERF spatial seismicity pdfs.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
@SuppressWarnings("javadoc")
public enum SpatialSeisPDF {
	
	
	UCERF2 {
		@Override public double[] getPDF() {
			return new GridReader("SmoothSeis_UCERF2.txt").getValues();
		}
	},
	
	UCERF3 {
		@Override public double[] getPDF() {
			return new GridReader("SmoothSeis_KF_5-5-2012.txt").getValues();
		}
	},
	
	AVG_DEF_MODEL {
		@Override public double[] getPDF() {
			CaliforniaRegions.RELM_TESTING_GRIDDED region = 
					new CaliforniaRegions.RELM_TESTING_GRIDDED();
			GriddedGeoDataSet xyz = DeformationModelOffFaultMoRateData.getAveDefModelPDF();
			List<Double> vals = Lists.newArrayList();
			for (Location loc : region) {
				vals.add(xyz.get(loc));
			}
			return Doubles.toArray(vals);
		}
	};
	
	public abstract double[] getPDF();
	
}
