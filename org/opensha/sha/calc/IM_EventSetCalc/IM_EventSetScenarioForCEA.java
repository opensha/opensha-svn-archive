package org.opensha.sha.calc.IM_EventSetCalc;


import java.util.*;
import java.lang.reflect.*;
import java.io.*;

import org.opensha.data.Location;
import org.opensha.data.LocationList;
import org.opensha.data.Site;
import org.opensha.data.region.SitesInGriddedRectangularRegion;
import org.opensha.sha.earthquake.EqkRupture;
import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.attenRelImpl.BA_2006_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2006_AttenRel;
import org.opensha.util.*;
import org.opensha.param.event.ParameterChangeWarningListener;
import org.opensha.param.ParameterAPI;
import org.opensha.param.WarningParameterAPI;
import org.opensha.param.event.ParameterChangeWarningEvent;
import org.opensha.sha.param.PropagationEffect;
import org.opensha.sha.param.SimpleFaultParameter;
import org.opensha.sha.surface.EvenlyGriddedSurfaceAPI;
import org.opensha.sha.util.SiteTranslator;
import org.opensha.sha.util.Vs30SiteTranslator;



/**
 * <p>Title: PagerShakeMapCalc</p>
 *
 * <p>Description: </p>
 *
 * @author Nitin Gupta, Vipin Gupta, and Ned Field
 * @version 1.0
 */
public class IM_EventSetScenarioForCEA implements ParameterChangeWarningListener{


	private EqkRupture eqkRupture;
	private SimpleFaultParameter faultParameter;
	private ArrayList attenRelList;
	private ArrayList vs30List ;
	private ArrayList distanceJBList;
	private ArrayList rupDistList;
	private ArrayList stationIds;
	private LocationList locList;
	private ArrayList imtSupported;
	private final static String EVENT_SET_FILE_CEA = "org/opensha/sha/calc/IM_EventSetCalc/eventSetFileCEA.txt";
	private final static String CB2006_TEST_FILE = "CB_2006_TestFile.txt";
	private final static String BA2006_TEST_FILE = "BA_2006_TestFile.txt";
	private final static String CY2006_TEST_FILE = "CY_2006_TestFile.txt";
	  // site translator
	private Vs30SiteTranslator siteTranslator = new Vs30SiteTranslator();
	
	private double minLat = Double.MAX_VALUE;
	private double maxLat = Double.MIN_VALUE;
	private double minLon = Double.MAX_VALUE;
	private double maxLon = Double.NEGATIVE_INFINITY;
	
	public SimpleFaultParameter createSimpleFaultParam(){
		faultParameter = new SimpleFaultParameter("Set Fault Surface");
		ArrayList lats = new ArrayList();
		lats.add(new Double(34.039));
		lats.add(new Double( (33.933+ 33.966)/2));
		lats.add(new Double(33.875));
		
		ArrayList lons = new ArrayList();
		lons.add(new Double(-118.334));
		lons.add(new Double((-118.124-118.135)/2));
		lons.add(new Double(-117.873));
		
		double dip = 27.5;
		ArrayList dips = new ArrayList();
		dips.add(new Double(dip));
		
		double dow = 27.00;
		double lowDepth = dow*Math.sin(dip);
		
		ArrayList depths = new ArrayList();
		depths.add(new Double(0.0));
		depths.add(new Double(lowDepth));
		faultParameter.initLatLonParamList();
		faultParameter.initDipParamList();
		faultParameter.initDepthParamList();
		
		faultParameter.setAll(SimpleFaultParameter.DEFAULT_GRID_SPACING, 
				lats, lons, dips, depths, SimpleFaultParameter.FRANKEL);
		faultParameter.setEvenlyGriddedSurfaceFromParams();
		return faultParameter;
	}
	
	private void createRuptureSurface(){
	
	    eqkRupture = new EqkRupture();
		createSimpleFaultParam();
		eqkRupture.setRuptureSurface((EvenlyGriddedSurfaceAPI)faultParameter.getValue());
		eqkRupture.setAveRake(90);
		eqkRupture.setMag(7.15);
	}
	
	
	private void readSiteFile(){
		ArrayList fileLines = null;
		vs30List = new ArrayList();
		stationIds = new ArrayList();
		rupDistList = new ArrayList();
		distanceJBList =  new ArrayList();
		locList = new LocationList();
		try {
			fileLines = FileUtils.loadFile(EVENT_SET_FILE_CEA);
			for(int i=1;i<fileLines.size();++i){
				String line = (String)fileLines.get(i);
				StringTokenizer st = new StringTokenizer(line);
				st.nextToken();
				stationIds.add(st.nextToken().trim());
				st.nextToken();
				st.nextToken();
				distanceJBList.add(Double.parseDouble(st.nextToken().trim()));
				rupDistList.add(Double.parseDouble(st.nextToken().trim()));
				vs30List.add(Integer.parseInt(st.nextToken().trim()));
				double lon = Double.parseDouble(st.nextToken().trim());
				double lat = Double.parseDouble(st.nextToken().trim());
				if(lat < this.minLat)
					minLat = lat;
				if(lat > this.maxLat)
					maxLat = lat;
				if(lon < this.minLon)
					minLon = lon;
				if(lon > this.maxLon)
					maxLon = lon;
					
				locList.addLocation(new Location(lat,lon));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("MinLat = "+minLat+"  MaxLat = "+maxLat);
		System.out.println("MinLon = "+minLon+" MaxLon = "+maxLon);
		
	}
	
	
	private void createAttenRelInstances(){
		attenRelList = new ArrayList();
		attenRelList.add(createIMRClassInstance("BA_2006_AttenRel"));
		attenRelList.add(createIMRClassInstance("CB_2006_AttenRel"));
		attenRelList.add(createIMRClassInstance("CY_2006_AttenRel"));
	}
	
	

	  /**
	  * Creates a class instance from a string of the full class name including packages.
	  * This is how you dynamically make objects at runtime if you don't know which\
	  * class beforehand. For example, if you wanted to create a BJF_1997_AttenRel you can do
	  * it the normal way:<P>
	  *
	  * <code>BJF_1997_AttenRel imr = new BJF_1997_AttenRel()</code><p>
	  *
	  * If your not sure the user wants this one or AS_1997_AttenRel you can use this function
	  * instead to create the same class by:<P>
	  *
	  * <code>BJF_1997_AttenRel imr =
	  * (BJF_1997_AttenRel)ClassUtils.createNoArgConstructorClassInstance("org.opensha.sha.imt.attenRelImpl.BJF_1997_AttenRel");
	  * </code><p>
	  *
	  */

	  private AttenuationRelationshipAPI createIMRClassInstance(String AttenRelClassName){
	    String attenRelClassPackage = "org.opensha.sha.imr.attenRelImpl.";
	      try {
	        Class listenerClass = Class.forName( "org.opensha.param.event.ParameterChangeWarningListener" );
	        Object[] paramObjects = new Object[]{ this };
	        Class[] params = new Class[]{ listenerClass };
	        Class imrClass = Class.forName(attenRelClassPackage+AttenRelClassName);
	        Constructor con = imrClass.getConstructor( params );
	        AttenuationRelationshipAPI attenRel = (AttenuationRelationshipAPI)con.newInstance( paramObjects );
	        //setting the Attenuation with the default parameters
	        attenRel.setParamDefaults();
	        return attenRel;
	      } catch ( ClassCastException e ) {
	        e.printStackTrace();
	      } catch ( ClassNotFoundException e ) {
	       e.printStackTrace();
	      } catch ( NoSuchMethodException e ) {
	       e.printStackTrace();
	      } catch ( InvocationTargetException e ) {
	        e.printStackTrace();
	      } catch ( IllegalAccessException e ) {
	        e.printStackTrace();
	      } catch ( InstantiationException e ) {
	        e.printStackTrace();
	      }
	      return null;
	  }
	
	  
	  private void createIMTList(){
		  imtSupported = new ArrayList();
		  imtSupported.add(AttenuationRelationship.PGA_NAME);
		  imtSupported.add(AttenuationRelationship.SA_NAME+" "+"0.2");
		  imtSupported.add(AttenuationRelationship.SA_NAME+" "+"1.0");
	  }

	  
	  /**
	   * set the site params in IMR according to basin Depth and vs 30
	   * @param imr
	   * @param lon
	   * @param lat
	   * @param willsClass
	   * @param basinDepth
	   */
	  private void setSiteParamsInIMR(AttenuationRelationshipAPI imr,
	                                  int vs30) {

	    Iterator it = imr.getSiteParamsIterator(); // get site params for this IMR
	    while (it.hasNext()) {
	      ParameterAPI tempParam = (ParameterAPI) it.next();
	      //adding the site Params from the CVM, if site is out the range of CVM then it
	      //sets the site with whatever site Parameter Value user has choosen in the application
	      boolean flag = siteTranslator.setSiteParams(tempParam, vs30,
	          Double.NaN);

	      if (!flag) {
	        String message = "cannot set the site parameter \"" + tempParam.getName() +
	            "\" from Vs30 = \"" + vs30 + "\"" +
	            "\n (no known, sanctioned translation - please set by hand)";
	      }
	    }
	  }
	  
	  
	  private void createMeanStdDevFile(){
		  PropagationEffect propEffect = new PropagationEffect();
		  propEffect.setEqkRupture(eqkRupture);
		  
		  try {
			FileWriter fw = new FileWriter("IM_MeanStdDevFile.txt");
			FileWriter fwTest;

			fw.write("AttenID,IMT,SiteID,Mean,Stdev\n");
			
			for(int i=0;i<this.attenRelList.size();++i){
				boolean writenTofile = false;
				AttenuationRelationshipAPI attenRel = (AttenuationRelationshipAPI)attenRelList.get(i);
				if(attenRel.getName().equals(BA_2006_AttenRel.NAME))
					fwTest = new FileWriter(this.BA2006_TEST_FILE);
				else if(attenRel.getName().equals(CB_2006_AttenRel.NAME))
					fwTest = new FileWriter(this.CB2006_TEST_FILE);
				else
					fwTest = new FileWriter(this.CY2006_TEST_FILE);
				Site site = new Site();
				Iterator it = attenRel.getSiteParamsIterator(); // get site params for this IMR
			    while (it.hasNext()) {
			      ParameterAPI tempParam = (ParameterAPI) it.next();
			      site.addParameter(tempParam);
			    }
				for(int j=0;j<imtSupported.size();++j){
					String imt = (String)imtSupported.get(j);
					double period = -1;
					if(imt.startsWith(AttenuationRelationship.SA_NAME)){
						StringTokenizer st = new StringTokenizer(imt);
						String  saName = st.nextToken().trim();
						period = Double.parseDouble(st.nextToken().trim());
						attenRel.setIntensityMeasure(saName);
						attenRel.getParameter(AttenuationRelationship.PERIOD_NAME).setValue(new Double(period));
					}
					
					if(period ==-1)
						attenRel.setIntensityMeasure(imt);
						
					for(int k=0;k<this.locList.size();++k){
						Location loc = locList.getLocationAt(k);
						site.setLocation(loc);
						setSiteParamsInIMR(attenRel,((Integer)vs30List.get(k)).intValue());
						propEffect.setSite(site);
						attenRel.setPropagationEffect(propEffect);
						double mean = Math.exp(attenRel.getMean());
						double stdDev = attenRel.getStdDev();
						fw.write(attenRel.getName()+","+imt+","+(String)this.stationIds.get(k)+
								","+mean+","+stdDev+"\n");
						if(!writenTofile){
							fwTest.write("Selected Site : " +attenRel.getSite().getLocation().toString()+"\n");
							fwTest.write("--------------\n");
							fwTest.write(attenRel.getName()+" Params:\n"+attenRel.getAllParamMetadata().replaceAll(";","\n")+"\n");
							fwTest.write("--------------\n");
						}
					}
					writenTofile = true;
					fwTest.close();
				}
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
	  }

	  
	  
   public static void main(String args[]){
	   IM_EventSetScenarioForCEA imEventSetScenario = new IM_EventSetScenarioForCEA();
	   imEventSetScenario.createAttenRelInstances();
	   imEventSetScenario.createIMTList();
	   imEventSetScenario.createRuptureSurface();
	   imEventSetScenario.readSiteFile();
	   imEventSetScenario.createMeanStdDevFile();
   }
  /**
   *  Function that must be implemented by all Listeners for
   *  ParameterChangeWarnEvents.
   *
   * @param  event  The Event which triggered this function call
   */
  public void parameterChangeWarning(ParameterChangeWarningEvent e) {

    String S = " : parameterChangeWarning(): ";

    WarningParameterAPI param = e.getWarningParameter();

    //System.out.println(b);
    param.setValueIgnoreWarning(e.getNewValue());

  }


}
