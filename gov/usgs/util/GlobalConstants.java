package gov.usgs.util;

import java.util.*;
import gov.usgs.exceptions.AnalysisOptionNotSupportedException;

/**
 * <p>Title: GlobalConstants</p>
 *
 * <p>Description: This class provides the static declaration for some of the
 * variables that always remain constant.</p>
 *
 * @author  Ned Field, Nitin Gupta and E.V. Leyendecker
 * @version 1.0
 */
public final class GlobalConstants {

  //data files path
  public final static String DATA_FILE_PATH ="/opt/install/jakarta-tomcat-4.1.24/webapps/USGS/WEB-INF/datafiles/USGS_DataFiles/";
  public final static String registrationName = "rmi://gravity.usc.edu:1099/USGS_HazardDataCalc_FactoryServer";

  //static declaration for the supported geographic regions
  public static final String CONTER_48_STATES = "Conterminous 48 States";
  public static final String ALASKA = "Alaska";
  public static final String HAWAII = "Hawaii";
  public static final String PUERTO_RICO = "Puerto Rico";
  public static final String CULEBRA = "Culebra";
  public static final String ST_CROIX = "St. Croix";
  public static final String ST_JOHN = "St. John";
  public static final String ST_THOMAS = "St. Thomas";
  public static final String VIEQUES = "Vieques";
  public static final String TUTUILA = "Tutuila";
  public static final String GUAM = "Guam";

  //static declaration of the data editions suppported within this framework.
  public static final String data_1996 = "1996 Data";
  public static final String data_2002 = "2002 Data";
  public static final String data_1998 = "1998 Data";
  public static final String data_2003 = "2003 Data";
  public static final String NEHRP_1997 =
      "1997 NEHRP Seismic Design Provisions";
  public static final String NEHRP_2000 =
      "2000 NEHRP Seismic Design Provisions";
  public static final String NEHRP_2003 =
      "2003 NEHRP Seismic Design Provisions";
  public static final String ASCE_1998 = "1998 ASCE 7 Standard";
  public static final String ASCE_2002 = "2002 ASCE 7 Stanadard";
  public static final String ASCE_2005 = "2005 ASCE 7 Standard";
  public static final String IBC_2000 = "2000 International Building Code";
  public static final String IBC_2003 = "2003 International Building Code";
  public static final String IBC_2004 = "2004 International Building Code";
  public static final String IBC_2006 = "2006 International Building Code";
  public static final String IRC_2000 = "2000 International Residential Code";
  public static final String IRC_2003 = "2003 International Residential Code";
  public static final String IRC_2004 = "2004 International Residential Code";
  public static final String IRC_2006 = "2006 International Residential Code";
  public static final String FEMA_273_DATA =
      "FEMA 273, MCE Guidelines for the Seismic Rehab. of Bldgs";
  public static final String FEMA_310_DATA = "FEMA 310";
  public static final String FEMA_356_DATA =
      "FEMA 356, Prestandard for Siesmic Rehab of Bldgs";
  public static final String ASCE_PRESTANDARD = "ASCE PreStandard";
  public static final String IEBC_2003 =
      "2003 International Existing Building Code";

  //static declaration for the analysis choices
  public static final String PROB_HAZ_CURVES = "Probabilistic hazard curves";
  public static final String PROB_UNIFORM_HAZ_RES =
      "Probabilistic Uniform Hazard Response Spectra";
  public static final String NEHRP = "NEHRP Recommended Provisions for Seismic Regulations for New Buildings and Other Structure";
  //public static final String FEMA_273 =
    //  "FEMA 273,MCE Guidelines for the Seismic Rehabilitation of Buildings";
  //public static final String FEMA_356 = "FEMA 356,Prestandard and Commentary for the Seismic Rehabilitation of Buildings";
  public static final String INTL_BUILDING_CODE = "International Building Code";
  public static final String INTL_RESIDENTIAL_CODE =
      "International Residential Code";
  //public static final String INTL_EXIST_CODE =
    //  "International Existing Building Code";
  //public static final String NFPA_5000 =
    //  "NFPA 5000 Building construction and safety code";
  //public static final String ASCE_7 =
    //  "ASCE 7 standard , Minimum Design Loads for Building and other structures";

  public static final String ASCE_NFPA = "ASCE 7 and NFPA 5000 building construction and safety codes";

  public static final String FEMA_IEBC_2003 = "Fema(273 and 356) and Intl. existing building code";

  private static final String analysis_choices_info =
      "The User may perform an " +
      "analysis for a site by selecting from the options listed. The type of analysis " +
      "depends on the option selected. In all cases the site location may be specified " +
      "by latitude-longiude (recommended) or zip code. The brief description of the " +
      "options are intended to provide information for the knowledgeable user. The " +
      "description are not a substitute for technical knowledge of seismic design " +
      "and/or analysis.";

  public static final String SITE_DISCUSSION =
      "There are two sets of site coefficients in use, \n" +
      "depending on the analysis option selected, differing only \n" +
      "for Site Class E when Ss or S1 equals or exceeds 1.25 or 0.50 \n" +
      "respectively.  The most recent set of site coefficients\n " +
      "has values of Fa = 0.9 and Fv = 2.4 for Site Class E for\n " +
      "these conditions.  The older tables \n" +
      "of site coefficients referred the user to footnote a.\n " +
      "The new tables were introduced in the 2000\n " +
      "Edition of the NEHRP Recommended Provisions for Seismic Regulations\n " +
      "for New Buildings and other Structures. Recent editions of other design\n " +
      "documents have adopted the new tables.\n\n" +
      "This program automatically selects the appropriate site \n" +
      "coefficient tables depending on the analysis option selected. The user may\n" +
      "see the difference by comparing the tables for the 1997 and 2000 editions\n " +
      "of the NEHRP Provisions.";

  public final static String SITE_ERROR =
      "Check the Site Class selection. A Site Class \n" +
      "resulting in the 'Note a' message requires a site \n" +
      "specific study.  To proceed, enter site factors in\n " +
      "the text boxes for Fa and Fv based on a site specific\n " +
      "study or select a different Site Class.";

  public static final int DIVIDING_FACTOR_HUNDRED = 100;

  //SITE CLASS VARIABLES
  public static final String SITE_CLASS_A = "Site Class A";
  public static final String SITE_CLASS_B = "Site Class B";
  public static final String SITE_CLASS_C = "Site Class C";
  public static final String SITE_CLASS_D = "Site Class D";
  public static final String SITE_CLASS_E = "Site Class E";
  public static final String SITE_CLASS_F = "Site Class F";

  // Fa table data for site coefficient window
  public final static String[] faColumnNames = {
      "Site Class", "Ss<=0.25", "Ss=0.50",
      "Ss=0.75", "Ss=1.00", "Ss>=1.25"};
  public final static Object[][] faData = {
      {
      "A", "0.8", "0.8", "0.8", "0.8", "0.8"}, {
      "B", "1.0", "1.0", "1.0", "1.0", "1.0"}, {
      "C", "1.2", "1.2", "1.1", "1.0", "1.0"}, {
      "D", "1.6", "1.4", "1.2", "1.1", "1.0"}, {
      "E", "2.5", "1.7", "1.2", "0.9", "0.9"}, {
      "F", "a", "a", "a", "a", "a"}
  };

  // Fv table data for site coefficient window
  public final static String[] fvColumnNames = {
      "Site Class", "S1<=0.10", "S1=0.20",
      "S1=0.30", "S1=0.40", "S1>=0.50"};
  public final static Object[][] fvData = {
      {
      "A", "0.8", "0.8", "0.8", "0.8", "0.8"}, {
      "B", "1.0", "1.0", "1.0", "1.0", "1.0"}, {
      "C", "1.7", "1.6", "1.5", "1.4", "1.3"}, {
      "D", "2.4", "2.0", "1.8", "1.6", "1.5"}, {
      "E", "3.5", "3.2", "2.8", "2.4", "2.4"}, {
      "F", "a", "a", "a", "a", "a"}
  };

  //Some constant declaration for data plotting and Metadata
  public final static String SA = "Sa (g)";
  public final static String SD = "Sd (in)";
  public final static String PERIOD_NAME = "T (sec)";
  public final static String MCE_SPECTRUM = "MCE Spectrum";
  public final static String SD_SPECTRUM = "Design Spectrum";
  public final static String SM_SPECTRUM = "Site Modified Spectrum";
  public final static String PERIOD_UNITS = "sec";
  public final static String SA_UNITS = "g";
  public final static String SD_UNITS = "inches";
  public final static String SA_Vs_SD_GRAPH_NAME = "Sa Vs Sd";
  public final static String SA_Vs_T_GRAPH_NAME = "Sa Vs T";
  public final static String MCE_SPECTRUM_SA_Vs_T_GRAPH =
      "MCE Spectrum Sa Vs T";
  public final static String MCE_SPECTRUM_SA_Vs_SD_GRAPH =
      "MCE Spectrum Sa Vs Sd";
  public final static String MCE_SPECTRUM_SD_Vs_T_GRAPH =
      "MCE Spectrum Sd Vs T";
  public final static String SITE_MODIFIED_SA_Vs_T_GRAPH =
      "Site Modified Sa Vs T";
  public final static String SITE_MODIFIED_SA_Vs_SD_GRAPH =
      "Site Modified Sa Vs Sd";
  public final static String SITE_MODIFIED_SD_Vs_T_GRAPH =
      "Site Modified Sd Vs T";
  public final static String DESIGN_SPECTRUM_SA_Vs_T_GRAPH =
      "Design Spectrum Sa Vs T";
  public final static String DESIGN_SPECTRUM_SA_Vs_SD_GRAPH =
      "Design Spectrum Sa Vs Sd";
  public final static String DESIGN_SPECTRUM_SD_Vs_T_GRAPH =
      "Design Spectrum Sd Vs T";

  public final static String UNIFORM_HAZARD_SPECTRUM_NAME = "Uniform Hazard Spectrum";
  public final static String APPROX_UNIFORM_HAZARD_SPECTRUM_NAME = "Approx. Uniform Hazard Spectrum";

  public final static String ANNUAL_FREQ_EXCEED_UNITS = "per year";
  public final static String BASIC_HAZARD_CURVE = "Basic Hazard Curve";
  public final static String HAZARD_CURVE_X_AXIS_NAME = "Accelaration";
  public final static String HAZARD_CURVE_Y_AXIS_NAME = "Annual Frequency of Exceedance";

  public static final String UHS_PGA_FUNC_NAME = "UHS PGA Values";


  //some SA constants
  public final static String SA_DAMPING = "5% Damping";

  //IMT Periods supported
  public final static String PGA = "Hazard Curve for PGA";
  public final static String IMT_POINT_ONE_SEC = "Hazard Curve for 0.1sec";
  public final static String IMT_POINT_TWO_SEC = "Hazard Curve for 0.2sec";
  public final static String IMT_POINT_THREE_SEC = "Hazard Curve for 0.3sec";
  public final static String IMT_POINT_FOUR_SEC = "Hazard Curve for 0.4sec";
  public final static String IMT_POINT_FIVE_SEC = "Hazard Curve for 0.5sec";
  public final static String IMT_ONE_SEC = "Hazard Curve for 1.0sec";
  public final static String IMT_TWO_SEC = "Hazard Curve for 2.0sec";

  //supported Return periods
  public final static String PERIOD_10_YEARS = "10";
  public final static String PERIOD_20_YEARS = "20";
  public final static String PERIOD_30_YEARS = "30";
  public final static String PERIOD_40_YEARS = "40";
  public final static String PERIOD_50_YEARS = "50";
  public final static String PERIOD_72_YEARS = "72";
  public final static String PERIOD_100_YEARS = "100";
  public final static String PERIOD_200_YEARS = "200";
  public final static String PERIOD_224_YEARS = "224";
  public final static String PERIOD_475_YEARS = "475";
  public final static String PERIOD_500_YEARS = "500";
  public final static String PERIOD_975_YEARS = "975";
  public final static String PERIOD_1000_YEARS = "1000";
  public final static String PERIOD_1500_YEARS = "1500";
  public final static String PERIOD_2475_YEARS = "2475";
  public final static String PERIOD_2500_YEARS = "2500";
  public final static String PERIOD_4975_YEARS = "4975";
  public final static String PERIOD_5000_YEARS = "5000";
  public final static String PERIOD_10000_YEARS = "10000";

  //supported exceed probabilities
  public final static String PROB_EXCEED_1 = "1";
  public final static String PROB_EXCEED_2 = "2";
  public final static String PROB_EXCEED_3 = "3";
  public final static String PROB_EXCEED_5 = "5";
  public final static String PROB_EXCEED_10 = "10";
  public final static String PROB_EXCEED_20 = "20";
  public final static String PROB_EXCEED_50 = "50";

  //supported exposure time
  public final static String EXP_TIME_10 = "10";
  public final static String EXP_TIME_30 = "30";
  public final static String EXP_TIME_50 = "50";
  public final static String EXP_TIME_75 = "75";
  public final static String EXP_TIME_100 = "100";
  public final static String EXP_TIME_250 = "250";



  //supported Spectra types
  public static final String MCE_GROUND_MOTION = "MCE Ground Motion";
  public static final String PE_10 = "10 % PE in 50 years";
  public static final String PE_5 = "5 % PE in 50 years";
  public static final String PE_2 = "2 % PE in 50 years";





  /**
   * Supported Return Periods.
   * @return ArrayList
   */
  public static ArrayList getSupportedReturnPeriods() {
    ArrayList supportedReturnPeriods = new ArrayList();
    supportedReturnPeriods.add(PERIOD_10_YEARS);
    supportedReturnPeriods.add(PERIOD_20_YEARS);
    supportedReturnPeriods.add(PERIOD_30_YEARS);
    supportedReturnPeriods.add(PERIOD_40_YEARS);
    supportedReturnPeriods.add(PERIOD_50_YEARS);
    supportedReturnPeriods.add(PERIOD_72_YEARS);
    supportedReturnPeriods.add(PERIOD_100_YEARS);
    supportedReturnPeriods.add(PERIOD_200_YEARS);
    supportedReturnPeriods.add(PERIOD_224_YEARS);
    supportedReturnPeriods.add(PERIOD_475_YEARS);
    supportedReturnPeriods.add(PERIOD_500_YEARS);
    supportedReturnPeriods.add(PERIOD_975_YEARS);
    supportedReturnPeriods.add(PERIOD_1000_YEARS);
    supportedReturnPeriods.add(PERIOD_1500_YEARS);
    supportedReturnPeriods.add(PERIOD_2475_YEARS);
    supportedReturnPeriods.add(PERIOD_2500_YEARS);
    supportedReturnPeriods.add(PERIOD_4975_YEARS);
    supportedReturnPeriods.add(PERIOD_5000_YEARS);
    supportedReturnPeriods.add(PERIOD_10000_YEARS);
    return supportedReturnPeriods;
  }

  /**
   * Returns the list of the supported Exceedance Prob List
   * @return ArrayList
   */
  public static ArrayList getSupportedExceedanceProb() {
    ArrayList supportedExceedProbList = new ArrayList();
    supportedExceedProbList.add(PROB_EXCEED_1);
    supportedExceedProbList.add(PROB_EXCEED_2);
    supportedExceedProbList.add(PROB_EXCEED_3);
    supportedExceedProbList.add(PROB_EXCEED_5);
    supportedExceedProbList.add(PROB_EXCEED_10);
    supportedExceedProbList.add(PROB_EXCEED_20);
    supportedExceedProbList.add(PROB_EXCEED_50);
    return supportedExceedProbList;
  }

  /**
   * Returns the list of the supported Exposure time
   * @return ArrayList
   */
  public static ArrayList getSupportedExposureTime() {
    ArrayList supportedExposureProbList = new ArrayList();
    supportedExposureProbList.add(EXP_TIME_10);
    supportedExposureProbList.add(EXP_TIME_30);
    supportedExposureProbList.add(EXP_TIME_50);
    supportedExposureProbList.add(EXP_TIME_75);
    supportedExposureProbList.add(EXP_TIME_100);
    supportedExposureProbList.add(EXP_TIME_250);
    return supportedExposureProbList;
  }

  /**
   * Returns the supported Site Classes
   * @return ArrayList
   */
  public static ArrayList getSupportedSiteClasses() {
    ArrayList supportedSiteClasses = new ArrayList();
    supportedSiteClasses.add(SITE_CLASS_A);
    supportedSiteClasses.add(SITE_CLASS_B);
    supportedSiteClasses.add(SITE_CLASS_C);
    supportedSiteClasses.add(SITE_CLASS_D);
    supportedSiteClasses.add(SITE_CLASS_E);
    supportedSiteClasses.add(SITE_CLASS_F);
    return supportedSiteClasses;
  }

  /**
   * Returns the number of supported Analysis types
   * @return ArrayList
   */
  public static ArrayList getSupportedAnalysisOptions() {
    ArrayList supportedAnalysisOption = new ArrayList();
    supportedAnalysisOption.add(PROB_HAZ_CURVES);
    supportedAnalysisOption.add(PROB_UNIFORM_HAZ_RES);
    supportedAnalysisOption.add(NEHRP);
    supportedAnalysisOption.add(FEMA_IEBC_2003);
    supportedAnalysisOption.add(INTL_BUILDING_CODE);
    supportedAnalysisOption.add(INTL_RESIDENTIAL_CODE);
    //supportedAnalysisOption.add(INTL_EXIST_CODE);
    supportedAnalysisOption.add(ASCE_NFPA);
    return supportedAnalysisOption;
  }
}
