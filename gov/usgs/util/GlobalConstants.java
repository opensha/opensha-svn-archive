package gov.usgs.util;

import java.util.ArrayList ;

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
    public static final String NEHRP_1997 = "1997 NEHRP Seismic Design Provisions";
    public static final String NEHRP_2000 = "2000 NEHRP Seismic Design Provisions";
    public static final String NEHRP_2003 = "2003 NEHRP Seismic Design Provisions";
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
    public static final String FEMA_273_DATA = "FEMA 273, MCE Guidelines for the Seismic Rehab. of Bldgs";
    public static final String FEMA_310_DATA = "FEMA 310";
    public static final String FEMA_356_DATA = "FEMA 356, Prestandard for Siesmic Rehab of Bldgs";
    public static final String ASCE_PRESTANDARD = "ASCE PreStandard";
    public static final String IEBC_2003 = "2003 International Existing Building Code";


    //static declaration for the analysis choices
    public static final String PROB_HAZ_CURVES = "Probabilistic hazard curves";
    public static final String PROB_UNIFORM_HAZ_RES =
        "Probabilistic Uniform Hazard Response Spectra";
    public static final String NEHRP = "NEHRP Recommended Provisions for Seismic Regulations for New Buildings and Other Structure";
    public static final String FEMA_273 =
        "FEMA 273,MCE Guidelines for the Seismic Rehabilitation of Buildings";
    public static final String FEMA_356 = "FEMA 356,Prestandard and Commentary for the Seismic Rehabilitation of Buildings";
    public static final String INTL_BUILDING_CODE = "International Building Code";
    public static final String INTL_RESIDENTIAL_CODE =
        "International Residential Code";
    public static final String INTL_EXIST_CODE =
        "International Existing Building Code";
    public static final String NFPA_5000 =
        "NFPA 5000 Building construction and safety code";
    public static final String ASCE_7 =
        "ASCE 7 standard , Minimum Design Loads for Building and other structures";

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
      "Ss=0.75", "Ss=1.00", "Ss=1.25"};
    public final static Object[][] faData = {
      {"A", "0.8", "0.8", "0.8", "0.8", "0.8"},
      {"B", "1.0", "1.0", "1.0", "1.0", "1.0"},
      {"C", "1.2", "1.2", "1.1", "1.0", "1.0"},
      {"D", "1.6", "1.4", "1.2", "1.1", "1.0"},
      {"E", "2.5", "1.7", "1.2", "0.9", "0.9"},
      {"F", "a", "a", "a", "a", "a"}
    };

    // Fv table data for site coefficient window
    public final static String[] fvColumnNames = {
      "Site Class", "Ss<=0.10", "Ss=0.20",
      "Ss=0.30", "Ss=0.40", "Ss=0.50"};
    public final static Object[][] fvData = {
      {"A", "0.8", "0.8", "0.8", "0.8", "0.8"},
      {"B", "1.0", "1.0", "1.0", "1.0", "1.0"},
      {"C", "1.7", "1.6", "1.5", "1.4", "1.3"},
      {"D", "2.4", "2.0", "1.8", "1.6", "1.5"},
      {"E", "3.5", "3.2", "2.8", "2.4", "2.4"},
      {"F", "a", "a", "a", "a", "a"}
    };



    //Some constant declaration for data plotting and Metadata
    public final static String SA = "Sa (g)";
    public final static String SD = "Sd (in)";
    public final static String PERIOD_NAME = "T (sec)";
    public final static String MCE_SPECTRUM = "MCE Spectrum";
    public final static String SD_SPECTRUM = "Design Spectrum";
    public final static String SM_SPECTRUM = "Site Modified Spectrum";
    public final static String PERIOD_UNITS = "Sec";
    public final static String SA_UNITS = "g";
    public final static String SD_UNITS = "inches";
    public final static String SA_Vs_SD_GRAPH_NAME = "Sa Vs Sd";
    public final static String SA_Vs_T_GRAPH_NAME = "Sa Vs T";
    public final static String MCE_SPECTRUM_SA_Vs_T_GRAPH = "MCE Spectrum Sa Vs T";
    public final static String MCE_SPECTRUM_SA_Vs_SD_GRAPH = "MCE Spectrum Sa Vs Sd";
    public final static String MCE_SPECTRUM_SD_Vs_T_GRAPH = "MCE Spectrum Sd Vs T";
    public final static String SITE_MODIFIED_SA_Vs_T_GRAPH = "Site Modified Sa Vs T";
    public final static String SITE_MODIFIED_SA_Vs_SD_GRAPH = "Site Modified Sa Vs Sd";
    public final static String SITE_MODIFIED_SD_Vs_T_GRAPH = "Site Modified Sd Vs T";
    public final static String DESIGN_SPECTRUM_SA_Vs_T_GRAPH = "Design Spectrum Sa Vs T";
    public final static String DESIGN_SPECTRUM_SA_Vs_SD_GRAPH = "Design Spectrum Sa Vs Sd";
    public final static String DESIGN_SPECTRUM_SD_Vs_T_GRAPH = "Design Spectrum Sd Vs T";





    //some SA constants
    public final static String SA_DAMPING = "5% Damping";

    /**
     * Returns the supported Site Classes
     * @return ArrayList
     */
    public static ArrayList getSupportedSiteClasses(){
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
    public static ArrayList getSupportedAnalysisOptions(){
      ArrayList supportedAnalysisOption = new ArrayList();
      supportedAnalysisOption.add(PROB_HAZ_CURVES);
      supportedAnalysisOption.add(PROB_UNIFORM_HAZ_RES);
      supportedAnalysisOption.add(NEHRP);
      supportedAnalysisOption.add(FEMA_273);
      supportedAnalysisOption.add(FEMA_356);
      supportedAnalysisOption.add(INTL_BUILDING_CODE);
      supportedAnalysisOption.add(INTL_RESIDENTIAL_CODE);
      supportedAnalysisOption.add(INTL_EXIST_CODE);
      supportedAnalysisOption.add(NFPA_5000);
      supportedAnalysisOption.add(ASCE_7);
      return supportedAnalysisOption;
    }

    /**
     * Swaps the byte ordering for the integer value
     * @param value int
     * @return int
     */
    public static int swap(int value) {
      int b1 = (value >> 0) & 0xff;
      int b2 = (value >> 8) & 0xff;
      int b3 = (value >> 16) & 0xff;
      int b4 = (value >> 24) & 0xff;

      return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
    }

    /**
     * Swaps the byte prdering for the float value
     * @param value float
     * @return float
     */
    public static float swap(float value) {
      int intValue = Float.floatToIntBits(value);
      intValue = swap(intValue);
      return Float.intBitsToFloat(intValue);
    }

    /**
    * Byte swap a single short value.
    *
    * @param value  Value to byte swap.
    * @return       Byte swapped representation.
    */
   public static short swap (short value){

     int b1 = value & 0xff;
     int b2 = (value >> 8) & 0xff;

     return (short) (b1 << 8 | b2 << 0);
   }

    /**
      * Byte swap a single long value.
      *
      * @param value  Value to byte swap.
      * @return       Byte swapped representation.
      */
     public static long swap (long value)
     {
       long b1 = (value >>  0) & 0xff;
       long b2 = (value >>  8) & 0xff;
       long b3 = (value >> 16) & 0xff;
       long b4 = (value >> 24) & 0xff;
       long b5 = (value >> 32) & 0xff;
       long b6 = (value >> 40) & 0xff;
       long b7 = (value >> 48) & 0xff;
       long b8 = (value >> 56) & 0xff;

       return b1 << 56 | b2 << 48 | b3 << 40 | b4 << 32 |
              b5 << 24 | b6 << 16 | b7 <<  8 | b8 <<  0;
  }


}
