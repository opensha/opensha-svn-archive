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



}
