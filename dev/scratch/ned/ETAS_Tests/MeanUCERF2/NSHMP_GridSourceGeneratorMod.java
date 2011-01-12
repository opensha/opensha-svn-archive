/**
 * 
 */
package scratch.ned.ETAS_Tests.MeanUCERF2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.opensha.commons.calc.magScalingRelations.magScalingRelImpl.WC1994_MagLengthRelationship;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.geo.GriddedRegion;
import org.opensha.commons.geo.Location;
import org.opensha.commons.geo.Region;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.ProbEqkSource;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.NSHMP_GridSourceGenerator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.griddedSeis.Point2Vert_FaultPoisSource;
import org.opensha.sha.magdist.GutenbergRichterMagFreqDist;
import org.opensha.sha.magdist.IncrementalMagFreqDist;
import org.opensha.sha.magdist.SummedMagFreqDist;




/**
 * This simply overrides one method of parent parent in order to go down to M 2.5 in background seismicity
 */
public class NSHMP_GridSourceGeneratorMod extends NSHMP_GridSourceGenerator {

	public NSHMP_GridSourceGeneratorMod() {
		super();
	}

	
	/**
	 * Here I simply replace occurrences of "5.0" in parent's version with "2.5".
	 */
	public SummedMagFreqDist getTotMFD_atLoc(int locIndex, boolean includeC_zones, 
			boolean applyBulgeReduction, boolean applyMaxMagGrid, boolean includeFixedRakeSources, 
			boolean include_agrd_deeps_out) {


		// find max mag among all contributions
		double maxMagAtLoc = C_ZONES_MAX_MAG-UCERF2.DELTA_MAG/2;

		// create summed MFD
		int numMags = (int)Math.round((maxMagAtLoc-2.55)/DELTA_MAG) + 1;
		SummedMagFreqDist mfdAtLoc = new SummedMagFreqDist(2.55, maxMagAtLoc, numMags);

		// create and add each contributing MFD
		if(includeFixedRakeSources) {
			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, 6.5, agrd_brawly_out[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, 7.3, agrd_mendos_out[locIndex], B_VAL, false), true);	
			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, 6.0, agrd_creeps_out[locIndex], B_VAL_CREEPING, false), true);			
		}
		
		if(include_agrd_deeps_out)
			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, 7.2, agrd_deeps_out[locIndex], B_VAL, false), true);
		
		mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, fltmmaxAll21ch_out6[locIndex], 0.667*agrd_impext_out[locIndex], B_VAL, applyBulgeReduction), true);
		mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, fltmmaxAll21gr_out6[locIndex], 0.333*agrd_impext_out[locIndex], B_VAL, applyBulgeReduction), true);
		if(applyMaxMagGrid) {	 // Apply Max Mag from files

			// 50% weight on the two different Mmax files:
			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, fltmmaxAll21ch_out6[locIndex], 0.5*0.667*agrd_cstcal_out[locIndex], B_VAL, applyBulgeReduction), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, fltmmaxAll21gr_out6[locIndex], 0.5*0.333*agrd_cstcal_out[locIndex], B_VAL, applyBulgeReduction), true);

			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, fltmmaxAll24ch_out6[locIndex], 0.5*0.667*agrd_cstcal_out[locIndex], B_VAL, applyBulgeReduction), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, fltmmaxAll24gr_out6[locIndex], 0.5*0.333*agrd_cstcal_out[locIndex], B_VAL, applyBulgeReduction), true);

			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, fltmmaxAll21ch_out6[locIndex], 0.667*agrd_wuscmp_out[locIndex], B_VAL, applyBulgeReduction), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, fltmmaxAll21gr_out6[locIndex], 0.333*agrd_wuscmp_out[locIndex], B_VAL, applyBulgeReduction), true);

			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, fltmmaxAll21ch_out6[locIndex], 0.667*agrd_wusext_out[locIndex], B_VAL, applyBulgeReduction), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, fltmmaxAll21gr_out6[locIndex], 0.333*agrd_wusext_out[locIndex], B_VAL, applyBulgeReduction), true);
		} else { // Apply default Mag Max
			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, DEFAULT_MAX_MAG, agrd_cstcal_out[locIndex], B_VAL, applyBulgeReduction), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, DEFAULT_MAX_MAG, agrd_wuscmp_out[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(2.5, DEFAULT_MAX_MAG, agrd_wusext_out[locIndex], B_VAL, applyBulgeReduction), true);
		}
		if(includeC_zones && includeFixedRakeSources) { // Include C-Zones
			mfdAtLoc.addResampledMagFreqDist(getMFD(6.5, C_ZONES_MAX_MAG, area1new_agrid[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(6.5, C_ZONES_MAX_MAG, area2new_agrid[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(6.5, C_ZONES_MAX_MAG, area3new_agrid[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(6.5, C_ZONES_MAX_MAG, area4new_agrid[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(6.5, C_ZONES_MAX_MAG, mojave_agrid[locIndex], B_VAL, false), true);
			mfdAtLoc.addResampledMagFreqDist(getMFD(6.5, C_ZONES_MAX_MAG, sangreg_agrid[locIndex], B_VAL, false), true);	
		}	

		return mfdAtLoc;
	}
	

}
