package scratch.UCERF3.utils;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.data.region.CaliforniaRegions;
import org.opensha.commons.data.xyz.GriddedGeoDataSet;
import org.opensha.commons.geo.Location;
import org.opensha.commons.util.ExceptionUtils;
import org.opensha.sha.earthquake.calc.ERF_Calculator;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;

import scratch.UCERF3.analysis.DeformationModelsCalc;
import scratch.UCERF3.analysis.GMT_CA_Maps;
import scratch.UCERF3.enumTreeBranches.DeformationModels;
import scratch.UCERF3.enumTreeBranches.FaultModels;
import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2;

/**
 * This reads and provides the off-fault moment rates provided by Kaj Johnson 
 * for each deformation model
 * @author field
 *
 */
public class DeformationModelOffFaultMoRateData {
	
	private static DeformationModelOffFaultMoRateData data;
	/**
	 * Gets an instance of the DeformationModelOffFaultMoRateData (cached so that we don't wast time instantiating it multiple times)
	 * @return
	 */
	public static DeformationModelOffFaultMoRateData getInstance() {
		if (data == null)
			data = new DeformationModelOffFaultMoRateData();
		return data;
	}
	
	public static final String SUBDIR = "DeformationModels";
	public static final String FILENAME = "gridded_moment_latlon_3_21.txt";
	public static final double KAJ_SEISMO_THICKNESS = 15d;
	public static final double REVISED_SEISMO_THICKNESS = 12d;
	
	final static CaliforniaRegions.RELM_TESTING_GRIDDED griddedRegion  = new CaliforniaRegions.RELM_TESTING_GRIDDED();
	
	GriddedGeoDataSet neok_Fm3pt1_xyzData, zeng_Fm3pt1_xyzData, abm_Fm3pt1_xyzData, geobound_Fm3pt1_xyzData,
					  geol_Fm3pt1_xyzData, abmPlusGeol_Fm3pt1_xyzData;
	
	/**
	 * This is private so that we always use the cached version. use getInstance() instead.
	 */
	private DeformationModelOffFaultMoRateData() {
		readDefModelGridData();
		makeGeolData();
	}
	
	
	/**
	 * This makes the off-fault moment rate distributions assuming the total for the ABM model is correct.
	 * This is spread uniformly for the geologic model, and an average of this and the spatial ABM-model dist for
	 * the average Geol+ABM model.
	 */
	private void makeGeolData() {
		FaultModels fm = FaultModels.FM3_1;
		DeformationModels dm = DeformationModels.ABM;
		double assumedTotalMoRate = DeformationModelsCalc.calcFaultMoRateForDefModel(fm, dm, true)+getTotalOffFaultMomentRate(fm, dm);
		
		double geolMoRate = assumedTotalMoRate - DeformationModelsCalc.calcFaultMoRateForDefModel(fm, DeformationModels.GEOLOGIC, true);
		geol_Fm3pt1_xyzData = new GriddedGeoDataSet(griddedRegion, true);
		int numPts = geol_Fm3pt1_xyzData.size();
		for(int i=0;i<numPts;i++)
			geol_Fm3pt1_xyzData.set(i,geolMoRate/numPts);
		
		abmPlusGeol_Fm3pt1_xyzData = new GriddedGeoDataSet(griddedRegion, true);
		for(int i=0;i<numPts;i++)
			abmPlusGeol_Fm3pt1_xyzData.set(i,(geol_Fm3pt1_xyzData.get(i)+abm_Fm3pt1_xyzData.get(i))/2.0);
		
	}
	
	/**
	 * This returns the total off-fault moment rate
	 * @param fm
	 * @param dm
	 * @return
	 */
	public double getTotalOffFaultMomentRate(FaultModels fm, DeformationModels dm) {
		if (dm == DeformationModels.UCERF2_ALL) 
			return 0.54e19;	// this is the value for non CA B faults, background, and C Zones (see Table in report)
		double total=0;
		GriddedGeoDataSet data = getDefModSpatialOffFaultMoRates(fm, dm);
		for(int i=0;i<data.size();i++)
			total+=data.get(i);
		return total;
	}

	
	/**
	 * Important notes on Kaj's file (at least the one sent on 3/21/12):
	 * 
	 * 0) only data for FM 3.1 are yet available
	 * 
	 * 1) moment rates in his files assume seismo thickness is 15 km, and mu=30GPa 
	 * (seismo depth is changed to 12 below) 
	 * 
	 * 2) I added a header to the input (hopefully will provide this in the future)
	 * 
	 * 3) zero values are replaced by the average of non-zero values
	 */
	private void readDefModelGridData() {
		neok_Fm3pt1_xyzData = new GriddedGeoDataSet(griddedRegion, true);	// true makes X latitude
		zeng_Fm3pt1_xyzData = new GriddedGeoDataSet(griddedRegion, true);	// true makes X latitude
		abm_Fm3pt1_xyzData = new GriddedGeoDataSet(griddedRegion, true);	// true makes X latitude
		geobound_Fm3pt1_xyzData = new GriddedGeoDataSet(griddedRegion, true);	// true makes X latitude
		
		// to convert dyn-cm/yr to Nm/yr and seismo thickness of 15 to 12;
		double CONVERSION = (REVISED_SEISMO_THICKNESS/KAJ_SEISMO_THICKNESS)*1e-7;

		try {
			BufferedReader reader = new BufferedReader(UCERF3_DataUtils.getReader(SUBDIR, FILENAME));
			int l=-1;
			String line;
			while ((line = reader.readLine()) != null) {
				l+=1;
				if (l == 0)
					continue;
				String[] st = StringUtils.split(line,"\t");
				Location loc = new Location(Double.valueOf(st[0]),Double.valueOf(st[1]));
				int index = griddedRegion.indexForLocation(loc);
				neok_Fm3pt1_xyzData.set(index, Double.valueOf(st[2])*CONVERSION);
				zeng_Fm3pt1_xyzData.set(index, Double.valueOf(st[3])*CONVERSION);
				abm_Fm3pt1_xyzData.set(index, Double.valueOf(st[4])*CONVERSION);
				geobound_Fm3pt1_xyzData.set(index, Double.valueOf(st[5])*CONVERSION);
			}
		} catch (Exception e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		
		fillZeroMoRatesWithAve(neok_Fm3pt1_xyzData);
		fillZeroMoRatesWithAve(zeng_Fm3pt1_xyzData);
		fillZeroMoRatesWithAve(abm_Fm3pt1_xyzData);
		fillZeroMoRatesWithAve(geobound_Fm3pt1_xyzData);
		
		
//		double sum, min, max, val;
//		min=Double.MAX_VALUE; max=0;
//		sum=0;
//		for(int i=0;i<neoKin_Fm3pt1_xyzData.size();i++) {
//			val = neoKin_Fm3pt1_xyzData.get(i);
//			sum += val;
//			if(val < min) min = val;
//			if(val > max) max = val;
//		}
//		System.out.println("neoKin_xyzData totMoRate="+(float)sum);
//		sum=0;
//		for(int i=0;i<zeng_Fm3pt1_xyzData.size();i++) {
//			val = zeng_Fm3pt1_xyzData.get(i);
//			sum += val;
//			if(val < min) min = val;
//			if(val > max) max = val;
//		}
//		System.out.println("zeng_xyzData totMoRate="+(float)sum);
//		sum=0;
//		for(int i=0;i<aveBlockMod_Fm3pt1_xyzData.size();i++) {
//			val = aveBlockMod_Fm3pt1_xyzData.get(i);
//			sum += val;
//			if(val < min) min = val;
//			if(val > max) max = val;
//		}
//		System.out.println("aveBlockMod_xyzData totMoRate="+(float)sum);
//		sum=0;
//		for(int i=0;i<geoBlockMod_Fm3pt1_xyzData.size();i++) {
//			val = geoBlockMod_Fm3pt1_xyzData.get(i);
//			sum += val;
//			if(val < min) min = val;
//			if(val > max) max = val;
//		}
//		System.out.println("geoBlockMod_xyzData totMoRate="+(float)sum);
//		
//		System.out.println("min="+(float)min);
//		System.out.println("max="+(float)max);

		
	}
	
	
	/**
	 * This generates an average PDF of the off-fault deformation models.  The data is read here in order
	 * to deal with the zero values differently (they are not considered in the average).  Each off-fault
	 * model is normalized to a PDF before computing the average PDF.
	 * @return
	 */
	public static GriddedGeoDataSet getAveDefModelPDF() {
		GriddedGeoDataSet aveData = new GriddedGeoDataSet(griddedRegion, true);	// true makes X latitude
		GriddedGeoDataSet neok_Fm3pt1_xyzData = new GriddedGeoDataSet(griddedRegion, true);	// true makes X latitude
		GriddedGeoDataSet zeng_Fm3pt1_xyzData = new GriddedGeoDataSet(griddedRegion, true);	// true makes X latitude
		GriddedGeoDataSet abm_Fm3pt1_xyzData = new GriddedGeoDataSet(griddedRegion, true);	// true makes X latitude
		GriddedGeoDataSet geobound_Fm3pt1_xyzData = new GriddedGeoDataSet(griddedRegion, true);	// true makes X latitude
		
		// to convert dyn-cm/yr to Nm/yr and seismo thickness of 15 to 12;
		double CONVERSION = (REVISED_SEISMO_THICKNESS/KAJ_SEISMO_THICKNESS)*1e-7;

		try {
			BufferedReader reader = new BufferedReader(UCERF3_DataUtils.getReader(SUBDIR, FILENAME));
			int l=-1;
			String line;
			while ((line = reader.readLine()) != null) {
				l+=1;
				if (l == 0)
					continue;
				String[] st = StringUtils.split(line,"\t");
				Location loc = new Location(Double.valueOf(st[0]),Double.valueOf(st[1]));
				int index = griddedRegion.indexForLocation(loc);
				neok_Fm3pt1_xyzData.set(index, Double.valueOf(st[2])*CONVERSION);
				zeng_Fm3pt1_xyzData.set(index, Double.valueOf(st[3])*CONVERSION);
				abm_Fm3pt1_xyzData.set(index, Double.valueOf(st[4])*CONVERSION);
				geobound_Fm3pt1_xyzData.set(index, Double.valueOf(st[5])*CONVERSION);
			}
		} catch (Exception e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		
		neok_Fm3pt1_xyzData = getNormalizdeData(neok_Fm3pt1_xyzData);
		zeng_Fm3pt1_xyzData = getNormalizdeData(zeng_Fm3pt1_xyzData);
		abm_Fm3pt1_xyzData = getNormalizdeData(abm_Fm3pt1_xyzData);
		geobound_Fm3pt1_xyzData = getNormalizdeData(geobound_Fm3pt1_xyzData);
		
		for(int i=0;i<aveData.size();i++) {
			double val=0;
			int num =0;
			if(neok_Fm3pt1_xyzData.get(i) !=0) {
				val += neok_Fm3pt1_xyzData.get(i);
				num+=1;
			}
			if(zeng_Fm3pt1_xyzData.get(i) !=0) {
				val += zeng_Fm3pt1_xyzData.get(i);
				num+=1;
			}
			if(abm_Fm3pt1_xyzData.get(i) !=0) {
				val += abm_Fm3pt1_xyzData.get(i);
				num+=1;
			}
			if(geobound_Fm3pt1_xyzData.get(i) !=0) {
				val += geobound_Fm3pt1_xyzData.get(i);
				num+=1;
			}
			if(num !=0)
				aveData.set(i,val/num);
			else
				aveData.set(i,0.0);
		}
		
		return aveData;
	}
	
	
	/**
	 * This writes the total moment rates to system.out
	 */
	public void writeAllTotalMomentRates() {
		System.out.println(DeformationModels.ABM.getShortName()+"\t"+(float) getTotalOffFaultMomentRate(FaultModels.FM3_1, DeformationModels.ABM));
		System.out.println(DeformationModels.NEOKINEMA.getShortName()+"\t"+(float) getTotalOffFaultMomentRate(FaultModels.FM3_1, DeformationModels.NEOKINEMA));
		System.out.println(DeformationModels.ZENG.getShortName()+"\t"+(float) getTotalOffFaultMomentRate(FaultModels.FM3_1, DeformationModels.ZENG));
		System.out.println(DeformationModels.GEOBOUND.getShortName()+"\t"+(float) getTotalOffFaultMomentRate(FaultModels.FM3_1, DeformationModels.GEOBOUND));
		System.out.println(DeformationModels.GEOLOGIC.getShortName()+"\t"+(float) getTotalOffFaultMomentRate(FaultModels.FM3_1, DeformationModels.GEOLOGIC));
		System.out.println(DeformationModels.GEOLOGIC_PLUS_ABM.getShortName()+"\t"+(float) getTotalOffFaultMomentRate(FaultModels.FM3_1, DeformationModels.GEOLOGIC_PLUS_ABM));
	}

	
	public GriddedGeoDataSet getDefModSpatialOffFaultMoRates(FaultModels fm, DeformationModels dm) {
		
		if(fm != FaultModels.FM3_1)
			throw new RuntimeException("only FaultModels.FM3_1 is presently supported");
		
		GriddedGeoDataSet data=null;
		switch(dm) {
		case ABM:
			data = abm_Fm3pt1_xyzData;
			break;
		case NEOKINEMA:
			data = neok_Fm3pt1_xyzData;
			break;
		case ZENG:
			data = zeng_Fm3pt1_xyzData;
			break;
		case GEOBOUND:
			data = geobound_Fm3pt1_xyzData;
			break;
		case GEOLOGIC:
			data = geol_Fm3pt1_xyzData;
			break;
		case GEOLOGIC_PLUS_ABM:
			data = abmPlusGeol_Fm3pt1_xyzData;
			break;
		}
		return data;
	}

	
	
	/**
	 * This returns the spatial PDF of off-fault moment rate (values sum to 1.0)
	 * @param fm
	 * @param dm
	 * @return
	 */
	public GriddedGeoDataSet getDefModSpatialOffFaultPDF(FaultModels fm, DeformationModels dm) {
		return getNormalizdeData(getDefModSpatialOffFaultMoRates(fm, dm));
	}
	
	
	/**
	 * This fills in any zero values with the average of non-zero values
	 * @param data
	 */
	private static void fillZeroMoRatesWithAve(GriddedGeoDataSet data) {
		int numZeros = 0;
		double sum=0;
		for(int i=0; i<data.size();i++) {
			if(data.get(i) == 0) {
				numZeros += 1;
			}
			else{
				sum += data.get(i); 
			}
		}
		//System.out.println(numZeros+"\t"+sum);
		if(numZeros == 0) {
			return;
		}
		else {
			sum /= (double)(data.size()-numZeros);	// the average non-zero value
			for(int i=0; i<data.size();i++) {
				if(data.get(i) == 0) {
					data.set(i, sum);
				}
			}
		}
	}

	
	
	/**
	 * this normalizes the data so they sum to 1.0
	 * @param data
	 */
	private static GriddedGeoDataSet getNormalizdeData(GriddedGeoDataSet data) {
		GriddedGeoDataSet normData = new GriddedGeoDataSet(griddedRegion, true);;
		double sum=0;
		for(int i=0;i<data.size();i++) 
			sum += data.get(i);
		for(int i=0;i<data.size();i++) 
			normData.set(i, data.get(i)/sum);
		return normData;
	}
	
	private void testPlotMap() {
		try {
			GMT_CA_Maps.plotSpatialPDF_Map(getNormalizdeData(neok_Fm3pt1_xyzData), "NeoKinema PDF", "test meta data", "NeoKinemaPDF_Map");
			GMT_CA_Maps.plotSpatialPDF_Map(getNormalizdeData(zeng_Fm3pt1_xyzData), "Zeng PDF", "test meta data", "ZengPDF_Map");
			GMT_CA_Maps.plotSpatialPDF_Map(getNormalizdeData(abm_Fm3pt1_xyzData), "ABM PDF", "test meta data", "ABM_PDF_Map");
			GMT_CA_Maps.plotSpatialPDF_Map(getNormalizdeData(geobound_Fm3pt1_xyzData), "Geo Block Mod PDF", "test meta data", "GeoBlkModPDF_Map");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void plotAveDefModPDF_Map() {
		
		// get the UCERF PDF with C zones for comparison
		ModMeanUCERF2 erf= new ModMeanUCERF2();
		erf.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
		erf.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
		erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_ONLY);
		erf.updateForecast();
		GriddedGeoDataSet ucerf2_OffFaultMoRate = ERF_Calculator.getMomentRatesInRegion(erf, RELM_RegionUtils.getGriddedRegionInstance());
		GriddedGeoDataSet ucerf2_OffFaultSeis = ERF_Calculator.getNucleationRatesInRegion(erf, RELM_RegionUtils.getGriddedRegionInstance(),0d,10d);

		try {
			GMT_CA_Maps.plotSpatialPDF_Map(getAveDefModelPDF(), "Ave Def Mod PDF", "average of the 4 gps deformation models", "AveDefModPDF_Map");
			GMT_CA_Maps.plotSpatialPDF_Map(getNormalizdeData(ucerf2_OffFaultMoRate), "UCERF2 Off-Fault MoRate", "this includes the C zones", "UCERF2_OffFaultMoRatePDF_Map");
			GMT_CA_Maps.plotSpatialPDF_Map(getNormalizdeData(ucerf2_OffFaultSeis), "UCERF2 Off-Fault Seis", "this includes the C zones", "UCERF2_OffFaultSeisPDF_Map");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {

		plotAveDefModPDF_Map();
		
//		DeformationModelOffFaultMoRateData test = new DeformationModelOffFaultMoRateData();
//		test.writeAllTotalMomentRates();
////		test.testPlotMap();
	}

}