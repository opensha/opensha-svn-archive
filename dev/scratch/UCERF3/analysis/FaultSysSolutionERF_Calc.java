package scratch.UCERF3.analysis;

import java.io.File;
import java.io.IOException;

import org.opensha.sha.earthquake.param.AleatoryMagAreaStdDevParam;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;

import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.erf.UCERF2_FaultSysSol_ERF;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.griddedSeismicity.SmallMagScaling;
import scratch.UCERF3.griddedSeismicity.SpatialSeisPDF;
import scratch.UCERF3.inversion.InversionFaultSystemSolution;
import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2;

public class FaultSysSolutionERF_Calc {
	
	
	/**
	 * This returns and instance of a UCERF3_FaultSysSol_ERF where the duration 
	 * has been set as 1 year and the forecast has been updated
	 * @param faultSysSolZipFile
	 * @return
	 */
	public static UCERF3_FaultSysSol_ERF getUCERF3_ERF_Instance(File faultSysSolZipFile, SpatialSeisPDF spatialPDF, SmallMagScaling scalingMethod) {
		InversionFaultSystemSolution invFss;
		SimpleFaultSystemSolution tmp = null;
		try {
			tmp =  SimpleFaultSystemSolution.fromFile(faultSysSolZipFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		invFss = new InversionFaultSystemSolution(tmp);

		UCERF3_FaultSysSol_ERF erf = new UCERF3_FaultSysSol_ERF(invFss, spatialPDF, scalingMethod);

		erf.getParameter(AleatoryMagAreaStdDevParam.NAME).setValue(0.0);
		erf.getTimeSpan().setDuration(1d);
		erf.updateForecast();
		
		return erf;

	}
	
	public static void makePrelimReportPartPlots() {

		try {
			// UCERF2_FaultSysSol_ERF
//			UCERF2_FaultSysSol_ERF erf = new UCERF2_FaultSysSol_ERF();
//			erf.updateForecast();
//			GMT_CA_Maps.plotParticipationRateMap(erf, 6.7, 10d, "testUCERF3_ERF", "test", "testUCERF3_ERF");
			
//			ModMeanUCERF2 erf= new ModMeanUCERF2();
//			erf.setParameter(UCERF2.PROB_MODEL_PARAM_NAME, UCERF2.PROB_MODEL_POISSON);
//			erf.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME, UCERF2.FULL_DDW_FLOATER);
//			erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
//			erf.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
//			erf.getTimeSpan().setDuration(1d);
//			erf.updateForecast();
//			String fileName = "UCERF2";

//			File file = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/PrelimModelReport/Figures/Fig16_ERF_ParticipationMaps/zipFiles/FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.1_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip");
//			UCERF3_FaultSysSol_ERF erf = getUCERF3_ERF_Instance(file, SpatialSeisPDF.AVG_DEF_MODEL,SmallMagScaling.MO_REDUCTION);
//			String fileName = "UCERF3_CHAR_DefMod_MoBal";
//			UCERF3_FaultSysSol_ERF erf = getUCERF3_ERF_Instance(file, SpatialSeisPDF.AVG_DEF_MODEL,SmallMagScaling.SPATIAL);
//			String fileName = "UCERF3_CHAR_DefMod_Seis";
//			UCERF3_FaultSysSol_ERF erf = getUCERF3_ERF_Instance(file, SpatialSeisPDF.UCERF3,SmallMagScaling.SPATIAL);
//			String fileName = "UCERF3_CHAR_U3smSeis_Seis";
			
			File file = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/PrelimModelReport/Figures/Fig16_ERF_ParticipationMaps/zipFiles/FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_GR_VarAseis0.1_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip");
			UCERF3_FaultSysSol_ERF erf = getUCERF3_ERF_Instance(file, SpatialSeisPDF.AVG_DEF_MODEL,SmallMagScaling.MO_REDUCTION);
			String fileName = "UCERF3_GR_DefMod_MoBal";
//			UCERF3_FaultSysSol_ERF erf = getUCERF3_ERF_Instance(file, SpatialSeisPDF.AVG_DEF_MODEL,SmallMagScaling.SPATIAL);
//			String fileName = "UCERF3_GR_DefMod_Seis";
//			UCERF3_FaultSysSol_ERF erf = getUCERF3_ERF_Instance(file, SpatialSeisPDF.UCERF3,SmallMagScaling.SPATIAL);
//			String fileName = "UCERF3_GR_U3smSeis_Seis";


			
			GMT_CA_Maps.plotParticipationRateMap(erf, 5.0, 10d, fileName+"_Part5pt0", "test", fileName+"_Part5pt0");
			GMT_CA_Maps.plotParticipationRateMap(erf, 6.7, 10d, fileName+"_Part6pt7", "test", fileName+"_Part6pt7");
			GMT_CA_Maps.plotParticipationRateMap(erf, 7.7, 10d, fileName+"_Part7pt7", "test", fileName+"_Part7pt7");
			GMT_CA_Maps.plotM6_5_BulgeMap(erf, 6.5, 1.0, fileName+"_Bulge", "test", fileName+"_Bulge");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		makePrelimReportPartPlots();
		
		
//		File file = new File("/Users/field/Neds_Creations/CEA_WGCEP/UCERF3/PrelimModelReport/Figures/Fig16_ERF_ParticipationMaps/zipFiles/FM3_1_GLpABM_MaEllB_DsrTap_DrEllB_Char_VarAseis0.1_VarOffAseis0.5_VarMFDMod1_VarNone_sol.zip");
//		SimpleFaultSystemSolution tmp = null;
//		try {
//			tmp =  SimpleFaultSystemSolution.fromFile(file);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		System.out.println("numRups="+tmp.getNumRuptures());
//		System.out.println("numSect="+tmp.getNumSections());


	}

}
