package org.scec.sha.gui.beans;

import java.util.*;
import java.lang.reflect.*;
import javax.swing.JOptionPane;


import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.sha.imr.AttenuationRelationshipAPI;
import org.scec.data.Site;
import org.scec.data.Location;
import org.scec.data.region.*;

/**
 * <p>Title:SiteParamListEditor </p>
 * <p>Description: this class will make the Gridded Region site parameter editor.
 * </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date March 11, 2003
 * @version 1.0
 */



public class SitesInGriddedRegionGuiBean extends ParameterListEditor implements
    ParameterChangeListener, ParameterChangeFailListener {

  // for debug purposes
  protected final static String C = "SiteParamList";

  /**
   * Latitude and longitude are added to the site paraattenRelImplmeters
   */
  public final static String MIN_LONGITUDE = "Min Longitude";
  public final static String MAX_LONGITUDE = "Max Longitude";
  public final static String MIN_LATITUDE =  "Min  Latitude";
  public final static String MAX_LATITUDE =  "Max  Latitude";
  public final static String GRID_SPACING =  "Grid Spacing";

  // title for site paramter panel
  protected final static String GRIDDED_SITE_PARAMS = "Set Gridded Region Params";

  /**
   * Longitude and Latitude paramerts to be added to the site params list
   */
  private DoubleParameter minLon = new DoubleParameter(MIN_LONGITUDE,
      new Double(-360), new Double(360),new Double(-122));
  private DoubleParameter maxLon = new DoubleParameter(MAX_LONGITUDE,
      new Double(-360), new Double(360),new Double(-121));
  private DoubleParameter minLat = new DoubleParameter(MIN_LATITUDE,
      new Double(-90), new Double(90), new Double(38.0));
  private DoubleParameter maxLat = new DoubleParameter(MAX_LATITUDE,
      new Double(-90), new Double(90), new Double(39.0));
  private DoubleParameter gridSpacing = new DoubleParameter(GRID_SPACING,
      new Double(.01),new Double(100.0),new String("Degrees"),new Double(.5));



  //instance of class EvenlyGriddedRectangularGeographicRegion
  private SitesInGriddedRegion gridRectRegion;

  /**
   * constuctor which builds up mapping between IMRs and their related sites
   */
  public SitesInGriddedRegionGuiBean() {
    // Build package names search path
    searchPaths = new String[1];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();
    // make the new griddedRegionSiteParamList
    parameterList  = new ParameterList();
    // add the longitude and latitude paramters
    parameterList.addParameter(minLon);
    parameterList.addParameter(maxLon);
    parameterList.addParameter(minLat);
    parameterList.addParameter(maxLat);
    parameterList.addParameter(gridSpacing);
    minLat.addParameterChangeListener(this);
    minLon.addParameterChangeListener(this);
    maxLat.addParameterChangeListener(this);
    maxLon.addParameterChangeListener(this);
    gridSpacing.addParameterChangeListener(this);
    minLat.addParameterChangeFailListener(this);
    minLon.addParameterChangeFailListener(this);
    maxLat.addParameterChangeFailListener(this);
    maxLon.addParameterChangeFailListener(this);
    gridSpacing.addParameterChangeFailListener(this);


    createAndUpdateSites();
    addParameters();
    setTitle(GRIDDED_SITE_PARAMS);

  }


  /**
   * This function adds the site params to the existing list.
   * Parameters are NOT cloned.
   * If paramter with same name already exists, then it is not added
   *
   * @param it : Iterator over the site params in the IMR
   */
 public void addSiteParams(Iterator it) {
   Parameter tempParam;
   Vector siteTempVector= new Vector();
   while(it.hasNext()) {
     tempParam = (Parameter)it.next();
     if(!parameterList.containsParameter(tempParam)) { // if this does not exist already
       parameterList.addParameter(tempParam);
       //adding the parameter to the vector,
       //VEctor is used to pass the add the site parameters to the gridded region sites.
       siteTempVector.add(tempParam);
       if(tempParam instanceof StringParameter) { // if it Stringparamter, set its initial values
         StringParameter strConstraint = (StringParameter)tempParam;
         tempParam.setValue(strConstraint.getAllowedStrings().get(0));
        }
     }

   }
   gridRectRegion.addSiteParams(siteTempVector.iterator());

  editorPanel.removeAll();
  addParameters();
 }

 /**
  * This function adds the site params to the existing list.
  * Parameters are cloned.
  * If paramter with same name already exists, then it is not added
  *
  * @param it : Iterator over the site params in the IMR
  */
 public void addSiteParamsClone(Iterator it) {
   Parameter tempParam;
   Vector v= new Vector();
   while(it.hasNext()) {
     tempParam = (Parameter)it.next();
     if(!parameterList.containsParameter(tempParam)) { // if this does not exist already
       Parameter cloneParam = (Parameter)tempParam.clone();
       if(tempParam instanceof StringParameter) {
         StringParameter strConstraint = (StringParameter)tempParam;
         cloneParam.setValue(strConstraint.getAllowedStrings().get(0));
        }
       parameterList.addParameter(cloneParam);
       //adding the cloned parameter in the siteList.
       v.add(cloneParam);
     }
   }
   gridRectRegion.addSiteParams(v.iterator());
   editorPanel.removeAll();
   addParameters();
 }

 /**
  * This function removes the previous site parameters and adds as passed in iterator
  *
  * @param it
  */
 public void replaceSiteParams(Iterator it) {


   createAndUpdateSites();
   // first remove all the parameters except latitude and longitude
   Iterator siteIt = parameterList.getParameterNamesIterator();
   while(siteIt.hasNext()) { // remove all the parameters except latitude and longitude and gridSpacing
     String paramName = (String)siteIt.next();
     if(!paramName.equalsIgnoreCase(MIN_LATITUDE) &&
        !paramName.equalsIgnoreCase(MIN_LONGITUDE) &&
        !paramName.equalsIgnoreCase(MAX_LATITUDE) &&
        !paramName.equalsIgnoreCase(MAX_LONGITUDE) &&
        !paramName.equalsIgnoreCase(GRID_SPACING))
       parameterList.removeParameter(paramName);
   }
   //removing the existing sites Params from the gridded Region sites
   Iterator it1=it;
   gridRectRegion.removeSiteParams();
   // now add all the new params
   addSiteParams(it);
 }


 /**
  * Display the site params based on the site passed as the parameter
  */
/* public void setSites(Site site) {
   this.site = site;
   Iterator it  = site.getParametersIterator();
   replaceSiteParams(it);
 }*/


  /**
   * get the site object from the site params
   *
   * @return
   */
  public Iterator getAllSites() {
    return gridRectRegion.getSitesIterator();
  }


  /**
   * get the clone of site object from the site params
   *
   * @return
   */
  public Iterator getSitesClone() {
    // make the new gridded site objects list
    EvenlyGriddedRectangularGeographicRegion newGridRectRegion= new EvenlyGriddedRectangularGeographicRegion(((Double)minLat.getValue()).doubleValue(),
        ((Double)maxLat.getValue()).doubleValue(),
        ((Double)minLon.getValue()).doubleValue(),
        ((Double)maxLon.getValue()).doubleValue(),
        ((Double)gridSpacing.getValue()).doubleValue());

    ListIterator lIt=gridRectRegion.getGridLocationsIterator();
    Vector newSiteVector=new Vector();
    while(lIt.hasNext())
      newSiteVector.add(new Site((Location)lIt.next()));


    ListIterator it  = parameterList.getParametersIterator();
    // clone the paramters
    while(it.hasNext()){
      ParameterAPI tempParam= (ParameterAPI)it.next();
      for(int i=0;i<newSiteVector.size();++i){
        if(!((Site)newSiteVector.get(i)).containsParameter(tempParam))
          ((Site)newSiteVector.get(i)).addParameter((ParameterAPI)tempParam.clone());
      }
    }
    return newSiteVector.iterator();
  }

  /**
   * this function when longitude or latitude are updated
   * So, we update the site object as well
   *
   * @param e
   */
  public void parameterChange(ParameterChangeEvent e) {


    Vector v= new Vector();
    createAndUpdateSites();
    //adding the siteParams to the temp Vector
    //getting the site params for the first element of the siteVector
    //becuase all the sites will be having the same site Parameter
    ListIterator it1=parameterList.getParametersIterator();
    while(it1.hasNext()){
      Parameter tempParam=(Parameter)it1.next();
      if(!tempParam.getName().equalsIgnoreCase(MIN_LONGITUDE) &&
         !tempParam.getName().equalsIgnoreCase(MAX_LATITUDE) &&
         !tempParam.getName().equalsIgnoreCase(MAX_LONGITUDE) &&
         !tempParam.getName().equalsIgnoreCase(GRID_SPACING))
        v.add(tempParam);
    }
    gridRectRegion.addSiteParams(v.iterator());
  }

  /**
   * Shown when a Constraint error is thrown on a ParameterEditor
   *
   * @param  e  Description of the Parameter
   */
  public void parameterChangeFailed( ParameterChangeFailEvent e ) {


    String S = C + " : parameterChangeWarning(): ";
    if(D) System.out.println(S + "Starting");


    StringBuffer b = new StringBuffer();

    ParameterAPI param = ( ParameterAPI ) e.getSource();


    ParameterConstraintAPI constraint = param.getConstraint();
    String oldValueStr = e.getOldValue().toString();
    String badValueStr = e.getBadValue().toString();
    String name = param.getName();

    b.append( "The value ");
    b.append( badValueStr );
    b.append( " is not permitted for '");
    b.append( name );
    b.append( "'.\n" );
    b.append( "Resetting to ");
    b.append( oldValueStr );
    b.append( ". The constraints are: \n");
    b.append( constraint.toString() );

    JOptionPane.showMessageDialog(
        this, b.toString(),
        "Cannot Change Value", JOptionPane.INFORMATION_MESSAGE
        );

    if(D) System.out.println(S + "Ending");

   }


   /**
    * This method returns the Vector of the gridded region sites
    * @return
    */
   private void createAndUpdateSites(){

     double minLatitude= ((Double)minLat.getValue()).doubleValue();
     double maxLatitude= ((Double)maxLat.getValue()).doubleValue();
     double minLongitude=((Double)minLon.getValue()).doubleValue();
     double maxLongitude=((Double)maxLon.getValue()).doubleValue();

     boolean flag=true;

     if(maxLatitude <= minLatitude){
       flag=false;
       JOptionPane.showMessageDialog(this,new String("Max Lat. must be greater than Min Lat"),"Input Error",
                                    JOptionPane.OK_OPTION);
     }

     if(maxLongitude <= minLongitude){
       flag=false;
      JOptionPane.showMessageDialog(this,new String("Max Lon. must be greater than Min Lon"),"Input Error",
                                      JOptionPane.OK_OPTION);
     }

     if(flag)
     gridRectRegion= new SitesInGriddedRegion(minLatitude,
                                      maxLatitude,minLongitude,maxLongitude,
                                      ((Double)gridSpacing.getValue()).doubleValue());



  }

  /**
   *
   * @return the object for the SitesInGriddedRegion class
   */
  public SitesInGriddedRegion getGriddedRegionSite(){
    return gridRectRegion;
  }

}