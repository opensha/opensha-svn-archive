package org.scec.sha.gui;


import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.text.DecimalFormat;
import org.scec.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
//import org.scec.sha.earthquake.rupForecastImpl.NorthridgeForecast;
import org.scec.sha.imr.attenRelImpl.Field_2000_AttenRel;
import org.scec.param.event.ParameterChangeWarningListener;
import org.scec.param.event.ParameterChangeWarningEvent;
import org.scec.param.ParameterAPI;
import org.scec.param.WarningParameterAPI;
import org.scec.data.Location;
import org.scec.data.Site;
import org.scec.sha.earthquake.*;
import org.scec.data.function.ArbitrarilyDiscretizedFunc;
import org.scec.sha.calc.HazardCurveCalculator;
import org.scec.sha.earthquake.EqkRupForecast;
import org.scec.util.FileUtils;
import org.scec.data.function.DiscretizedFuncAPI;
import org.scec.sha.imr.AttenuationRelationship;
import org.scec.sha.util.SiteTranslator;
import org.scec.exceptions.ParameterException;
import org.scec.data.Location;

public class HazCurvGenerator implements ParameterChangeWarningListener {
        private final static String DELTA_RATES_FILE_NAME = "http://www.relm.org/models/step/SoCalDeltaRates.txt";
        // VS 30 value to be set in the IMR
        private final Double VS_30 = new Double(760);
        private final double GRID_SPACING = .1;
        private String METADATA = "";
/*	private static final String STEP_BACKGROUND_FILE = "backGround.txt";
        private static final String STEP_ADDON_FILE_SUFFIX = "_addon.txt";
        private static final String STEP_COMBINED_FILE_SUFFIX = "_both.txt";
        private static final String METADATA_FILE_SUFFIX = "_metadata.dat";
        private static final String VS30_FILE_NAME = "usgs_cgs_geology_60s_mod.txt";
        private static final String BASIN_DEPTH_FILE_NAME = "basindepth_OpenSHA.txt";
        private static final String SCEC_BASIN_DEPTH_SERVLET = "SCEC_BasinDepthServlet";
        private static final double IML_VALUE = Math.log(0.126);
        private static final double POE_VALUE = 0.50; */
        private ArrayList latVals = new ArrayList();
        private ArrayList lonVals = new ArrayList();
        //	holds the ArbitrarilyDiscretizedFunc
        //private ArbitrarilyDiscretizedFunc function;
        private HazardCurveCalculator calc;
        //vector to store the vs30 values
        private ArrayList vs30Vals = new ArrayList();
        DecimalFormat format = new DecimalFormat("0.00##");

        // stores data of last run shakeMap Calculation
        private String output;

        // stores results of calculations
        private ArrayList results;

        // list of sites from the file
        private ArrayList sites;

        public static void main(String [] args) {
                HazCurvGenerator test = new HazCurvGenerator();
        }

        public HazCurvGenerator() {
                //runHazardCurveCalculation();
                //runShakeMapDataCalculation();
        }

        public void runShakeMapDataCalculation(AttenuationRelationship imr, String siteFile, ArrayList siteList, EqkRupForecast forecast, int source, int rupture, boolean isProbAtIml ,double iml_prob, String selectedIMT_MD) {

                // Setting all parameters to default
                imr.setParamDefaults();
                ArrayList CVMlist = null, BDlist = null;

                String md = ((AttenuationRelationship)imr).getAllParamMetadata();

                // Create Sites from XY file
                if ( siteList != null )
                        sites = siteList;
                else {
                        sites = getSiteValuesFromFile(latVals, lonVals, imr, siteFile);

                        // Create Locations
                        ArrayList locations = new ArrayList();

                        for ( int i = 0; i < sites.size(); i++) {
                                locations.add( ((Site)sites.get(i)).getLocation() );
                        }

                        // Will Site Class
                        //CVMShakeMap wsc = new CVMShakeMap("/home/vijeshme/projects/tools/usgs_cgs_geology_60s_mod.txt","/home/vijeshme/projects/tools/basindepth_OpenSHA.txt",locations);

                      //  CVMlist = wsc.getWillsSiteClass();

                      //  BDlist = wsc.getBasinDepth();
                }
                // Creating probability values
                forecast.updateForecast();

                METADATA = "Global Values:\n" + "Attenuation Relationship=" + imr.getName() + "\n" +
                                   "ARClass=" + imr.getClass() + "\n" +
                                   "ERF=" + forecast.getName() + "\n" +
                                   selectedIMT_MD + "\n" +
                                   "OriginalXYZFile=" + siteFile + "\n\n" +
                                   "Metadata for Each Site:\n";

                String MD = forecast.getAdjustableParameterList().getParameterListMetadataString();

                // run shake map calculation
                results = scenarioShakeMapDataCalc(sites, imr, forecast.getSource(source).getRupture(rupture), isProbAtIml, iml_prob, CVMlist, BDlist, MD, false);

                output = new String();
                Site temp;
                for ( int i = 0; i < results.size(); i++ ) {
                        temp = (Site)sites.get(i);
                        output += temp.getLocation().getLatitude() + " " + temp.getLocation().getLongitude() + " "
                                                + results.get(i) + "\n";
                }
        }

        public ArrayList getResults() {
                return results;
        }

        public String getMetadata() {
                return METADATA;
        }

        public ArrayList getSites() {
                return sites;
        }

/*	private void runHazardCurveCalculation() {
//		 Create a funciton with default X-Values
                function = new ArbitrarilyDiscretizedFunc();
                xValuesFromFile("/home/vijeshme/projects/data/xvaluesdefault.txt",function);

                // make the forecast
                Frankel96_AdjustableEqkRupForecast forecast = new Frankel96_AdjustableEqkRupForecast();
                forecast.getParameter(forecast.BACK_SEIS_NAME).setValue(forecast.BACK_SEIS_INCLUDE);

                // make the imr - Field 2000
                Field_2000_AttenRel imr = new Field_2000_AttenRel(this);

                // Setting all parameters to default
                imr.setParamDefaults();

                ((AttenuationRelationship)imr).getAllParamMetadata();

                // set the im as PGA
                imr.setIntensityMeasure(imr.PGA_NAME);

                // set the vs30
                imr.getParameter(imr.VS30_NAME).setValue(VS_30);
                Iterator it = imr.getSiteParamsIterator();

                // Create Sites from XY file
                ArrayList sites = getSiteValuesFromFile(latVals, lonVals, imr, "/home/vijeshme/projects/data/xyz_removeddups.txt");

                // Creating probability values
                forecast.updateForecast();

                // imr.getParameter(imr.EXCEED_PROB_NAME).setValue(new Double(this.POE_VALUE));
                // stepAddonProbVals = getProbVals(imr, sites, (EqkRupForecast) forecast);
                // generate output file
                // createFile(stepAddonProbVals, "/home/vijeshme/outputDATA");
                calc = new HazardCurveCalculator();
                output = new String();
                ArbitrarilyDiscretizedFunc result;
                for ( int i = 0; i < sites.size(); i++ ) {
                        Site temp = (Site)sites.get(i);
                        result = HazCurveCalc(temp, imr, forecast);
                        // Output values to console
                        output += temp.getLocation().getLongitude() + " "
                                        + temp.getLocation().getLatitude() + " "
                                        + result.getFirstInterpolatedX(0.5) + "\n";

                        System.out.println("--------------------------------");
                        System.out.println(temp.getLocation().getLatitude() + " "
                                                                + temp.getLocation().getLongitude()
                                                                + " " + result.getFirstInterpolatedX(0.5));
                        for ( int j = 0; j < result.getNum(); j++ ) {
                                System.out.println(result.getX(j) + " " + result.getY(j));
                        }
                }

                outputDataFile("/home/vijeshme/hazOutput50");
        }

        /**
         * craetes the output xyz files
         *
         * @param probVals :
         *            Probablity values ArrayList for each Lat and Lon
         * @param fileName :
         *            File to create
         */
        private void createFile(ArrayList probVals, String fileName) {
                int size = probVals.size();
                // System.out.println("Size of the Prob ArrayList is:"+size);
                try {
                        FileWriter fr = new FileWriter(fileName);
                        for (int i = 0; i < size; ++i)
                                fr.write(latVals.get(i) + "  " + lonVals.get(i) + "  "
                                                + probVals.get(i) + "\n");
                        fr.close();
                } catch (IOException ee) {
                        ee.printStackTrace();
                }
        }

        /**
         * Creates the metadata file for the dataSet
         *
         * @param metadata :
         *            String that contains the metadata info
         * @param fileName :
         *            Name of the metadataFile
         */
        public void outputDataFile(String fileName) {
                try {
                        FileWriter file = new FileWriter(fileName);
                        file.write(output);
                        file.close();
                } catch (Exception e) {
                        e.printStackTrace();
                }
                try {
                        FileWriter file = new FileWriter(fileName + "_metadata");
                        file.write(METADATA);
                        file.close();
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        /**
         * Function that must be implemented by all Listeners for
         * ParameterChangeWarnEvents.
         *
         * @param event
         *            The Event which triggered this function call
         */
        public void parameterChangeWarning(ParameterChangeWarningEvent e) {
                String S = " : parameterChangeWarning(): ";
                WarningParameterAPI param = e.getWarningParameter();
                //System.out.println(b);
                param.setValueIgnoreWarning(e.getNewValue());
        }


        /** Gets X,Y values from a file and creates a List of Sites */
        private ArrayList getSiteValuesFromFile(ArrayList latt, ArrayList longitude,
                        AttenuationRelationship imr, String fileName) {
                ArrayList sites = new ArrayList();
                try {
                        String lat, lon;
                        ArrayList fileLines = FileUtils.loadFile(fileName);
                        ListIterator it = fileLines.listIterator();
                        while (it.hasNext()) {
                                StringTokenizer st = new StringTokenizer((String) it.next());
                                lat = st.nextToken();
                                lon = st.nextToken();
                                latt.add(lat);
                                longitude.add(lon);
                                Site site = new Site(new Location(Double.parseDouble(lat),
                                                Double.parseDouble(lon)));

                                Iterator ParamIt = imr.getSiteParamsIterator();
                                while( ParamIt.hasNext() ) {
                                        site.addParameter( (ParameterAPI)ParamIt.next() );
                                }

                                sites.add(site);
                        }
                } catch (Exception e) {
                        e.printStackTrace();
                }
                return sites;
        }


        /*private ArbitrarilyDiscretizedFunc HazCurveCalc(Site site, Field_2000_AttenRel imr, EqkRupForecast eqkRupForecast) {

                //if(distanceControlPanel!=null)
                // calc.setMaxSourceDistance(distanceControlPanel.getDistance());
                // initialize the values in condProbfunc with log values as passed in
                // hazFunction
                // intialize the hazard function
                ArbitrarilyDiscretizedFunc hazFunction = new ArbitrarilyDiscretizedFunc();
                // Initialize X Values
                initX_Values(hazFunction);
                try {
                        // calculate the hazard curve
                        //eqkRupForecast =
                        // (EqkRupForecastAPI)FileUtils.loadObject("erf.obj");
                        calc.getHazardCurve(hazFunction, site, imr, eqkRupForecast);
                        hazFunction = toggleHazFuncLogValues(hazFunction);
                        //hazFunction.setInfo(getParametersInfo());
                } catch (RuntimeException e) {
                        // JOptionPane.showMessageDialog(this, e.getMessage(),
                        //                               "Parameters Invalid", JOptionPane.INFORMATION_MESSAGE);
                        e.printStackTrace();
                        return null;
                }

                return hazFunction;
        }

        /**
         * set x values in log space for Hazard Function to be passed to IMR if the
         * selected IMT are SA , PGA , PGV or FaultDispl It accepts 1 parameters
         *
         * @param originalFunc :
         *            this is the function with X values set
         */
/*	private void initX_Values(DiscretizedFuncAPI arb) {
                // if not using custom values get the function according to IMT.
                //if(!useCustomX_Values)
                // function =
                // imtInfo.getDefaultHazardCurve(imtGuiBean.getSelectedIMT());
                //  if (imtInfo.isIMT_LogNormalDist(imtGuiBean.getSelectedIMT())) {
                //for (int i = 0; i < function.getNum(); ++i)
                //	arb.set(Math.log(function.getX(i)), 1);
                //    }
                //    else
                //      throw new RuntimeException("Unsupported IMT");
                for ( int i = 0; i < function.getNum(); i++ ) {
                        arb.set(Math.log(function.get(i).getX()),1);
                }

        }

        private void xValuesFromFile(String filename, DiscretizedFuncAPI arb) {
//		 Read defaults from file:
                try {
                        FileReader fr = new FileReader(filename);
                        BufferedReader fread = new BufferedReader(fr);

                        String line = fread.readLine();
                        while( line != null ) {
                                arb.set(Double.parseDouble(line),1.0);
                                line = fread.readLine();
                        }
                }
                catch ( Exception e ) {
                        System.err.println("Error opening default x values file");
                }
        }

        /** reset's y values for a function */
/*	private void resetYValues(DiscretizedFuncAPI arb) {
                int total = arb.getNum();
                for ( int i = 0; i < total; i++ ) {
                        arb.set(i,1);
                }
        }

         /**
           * set x values back from the log space to the original linear values
           * for Hazard Function after completion of the Hazard Calculations
           * if the selected IMT are SA , PGA or PGV
           * It accepts 1 parameters
           *
           * @param hazFunction :  this is the function with X values set
           */
        /*  private ArbitrarilyDiscretizedFunc toggleHazFuncLogValues(ArbitrarilyDiscretizedFunc hazFunc){
            int numPoints = hazFunc.getNum();
            DiscretizedFuncAPI tempFunc = hazFunc.deepClone();
            hazFunc = new ArbitrarilyDiscretizedFunc();
            // take log only if it is PGA, PGV ,SA or FaultDispl

              for(int i=0; i<numPoints; ++i)
                hazFunc.set(function.getX(i), tempFunc.getY(i));

              return hazFunc;
          }

          /**
           *
           * This function computes a Scenario ShakeMap Data for the given Region, IMR, and ERF.
           * The computed  data in the form of X, Y and Z is place XYZ_DataSetAPI object.
           * @param griddedRegionSites : Gridded Region Object
           * @param imr : selected IMR object
           * @param rupture : selected EarthquakeRupture Object.
           * @param isProbAtIML : if true the prob at the specified IML value (next param) will
           * be computed; if false the IML at the specified Prob value (next param) will be computed.
           * @param value : the IML or Prob to compute the map for.
           * @param sitesLat : ArrayList containing  gridded Locations Latitudes Values
           * @param sitesLon : ArrayList containing  gridded Locations Longitudes Values
           * @returns the XYZ_DataSetAPI  : ArbDiscretized XYZ dataset
           * @throws ParameterException
           */
          private ArrayList scenarioShakeMapDataCalc(ArrayList sites, AttenuationRelationship imr, EqkRupture rupture,
                                              boolean isProbAtIML,double value, ArrayList cvmList, ArrayList bdList, String forecastMD, boolean useCVM) throws ParameterException {

            //Instance of the site TransLator class
            SiteTranslator siteTranslator = new SiteTranslator();
            Site site;
            int numSites = sites.size();
            //Declaring the arrayList instance to store the site value for each site
            //ArrayList containing IML or Prob value for each site
            ArrayList sitesValue = new ArrayList();

            // set the ProbEQkRup in the IMR
            try {
              imr.setEqkRupture((EqkRupture)rupture);
            } catch (ParameterException ex) {
              throw new ParameterException("Rupture not allowed for the chosen IMR: "+ex.getMessage());
            }

            for(int i=0;i<numSites;++i) {
              site = (Site)sites.get(i);

              if ( useCVM && cvmList != null ) {
                      Iterator it = site.getParametersIterator();

                      while(it.hasNext()){
                         ParameterAPI tempParam = (ParameterAPI)it.next();

                         //Setting the value of each site Parameter from the CVM and translating them into the Attenuation related site
                         boolean flag = siteTranslator.setParameterValue(tempParam,(String)cvmList.get(i),
                                                                         ((Double)bdList.get(i)).doubleValue());
                      }
              }
              else {
              }

              imr.setSite(site);

              String siteMDlatLon = " Latitude=" + site.getLocation().getLatitude() + " Longitude="+ site.getLocation().getLongitude();
              METADATA += "---------------------\n" +
                                                  "IMR Metadata:\n" +
                                                imr.getAllParamMetadata() + "\n" +
                                                "Site Metadata:\n" +site.getParameterListMetadataString() + siteMDlatLon + "\n" +
                                                "ERF Metadata:\n" + "Source=" + 283 + "\n" +
                                                ((ProbEqkRupture)rupture).getInfo() + "\n" +
                                                forecastMD + "\n";

              if(isProbAtIML)
                sitesValue.add( new Double(imr.getExceedProbability(Math.log(value))));
              else{
                try{
                  //if IML@Prob then Prob value should be between 0 and 1.
                  if(value<0 || value >1)
                    throw new ParameterException("Probability can only between 0 and 1");
                  imr.getParameter(imr.EXCEED_PROB_NAME).setValue(new Double(value));
                  Double result = new Double(StrictMath.exp(imr.getIML_AtExceedProb()));
                  //result = new Double(Math.exp(Math.E));
                  sitesValue.add(result);
                }catch(ParameterException e){
                  throw new ParameterException(e.getMessage());
                }
              }
            }

            return sitesValue;

          }

          public static ArrayList getZValues(String path, double mag) {
                  try {
                          ArrayList result = new ArrayList();
                          ArrayList fileLines = FileUtils.loadFile(path);
                        ListIterator it = fileLines.listIterator();
                        String lat,lon,z;
                        while (it.hasNext()) {
                                StringTokenizer st = new StringTokenizer((String) it.next());
                                lat = st.nextToken();
                                lon = st.nextToken();
                                z = st.nextToken();
                                z = (new Double(Math.abs(Double.parseDouble(z) * mag))).toString(); // take absolute value

                                result.add(z);
                        }
                        return result;
                  }
                  catch (Exception e) {
                          return null;
                  }
          }

          /** translates the VS30 lettering to numbers
           *
           * @param result the vs30 lettering
           * @return an integer for the value
           */
/*	  private int translateVS30(String result) {

                  if ( result.equals("E") ) return 163;
        else if ( result.equals("DE") ) return 180;
        else if ( result.equals("D") ) return 270;
        else if ( result.equals("CD") ) return 360;
        else if ( result.equals("C") ) return 560;
        else if ( result.equals("BC") ) return 760;
        else if ( result.equals("B") ) return 1000;
        else if ( result.equals("NA") ) return 180;
        else return 0;

          }

          /** tranlates basin depth to a proper value: divided by 1000
           *
           * @param value the original value
           * @return the value translated
           */
        /*  private Double translateBasinDepth(Double value) {

                  return new Double(value.doubleValue() / 1000);
          }
          */
}
