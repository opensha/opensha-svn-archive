package org.scec.sha.gui.beans;

import java.util.*;
import java.lang.reflect.*;
import javax.swing.*;
import java.net.*;
import java.io.Serializable;
import java.io.*;
import java.awt.*;
import java.awt.event.*;


import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.sha.imr.AttenuationRelationshipAPI;
import org.scec.data.Site;
import org.scec.data.Location;
import org.scec.data.region.*;
import org.scec.sha.gui.infoTools.CalcProgressBar;


/**
 * <p>Title:SiteParamListEditor </p>
 * <p>Description: this class will make the Gridded Region site parameter editor.
 * </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date March 11, 2003
 * @version 1.0
 */



public class SitesInGriddedRegionGuiBean extends JPanel implements
     ParameterChangeFailListener, Serializable {

  // for debug purposes
  protected final static String C = "SiteParamList";

  /**
   * Latitude and longitude are added to the site attenRelImplmeters
   */
  public final static String MIN_LONGITUDE = "Min Longitude";
  public final static String MAX_LONGITUDE = "Max Longitude";
  public final static String MIN_LATITUDE =  "Min  Latitude";
  public final static String MAX_LATITUDE =  "Max  Latitude";
  public final static String GRID_SPACING =  "Grid Spacing";
  public final static String VS30_DEFAULT =  "VS(30) Default";
  public final static String VS30_DEFAULT_INFO =  "VS(30) Value in Water(for site in Ocean)";

  // min and max limits of lat and lin for which CVM can work
  private static final double MIN_CVM_LAT = 32.0;
  private static final double MAX_CVM_LAT = 36.0;
  private static final double MIN_CVM_LON = -121.0;
  private static final double MAX_CVM_LON = -114.0;
  private static final double VS30_DEFAULT_VALUE = 500.00;

  //Vs30 vector: the values that return from the CVM
  private Vector vs30Vector;

  //BasinDepth vector: the values that return from the CVM
  private Vector basinDepthVector;

  // title for site paramter panel
  protected final static String GRIDDED_SITE_PARAMS = "Set Gridded Region Params";
  //ParameterListEditor Instance
  ParameterListEditor editorPanel;
  //ParameterList
  ParameterList parameterList = new ParameterList();

  //Static String for setting the site Params
  public final static String SET_ALL_SITES = "Apply site parameter to all locations";
  public final static String SET_SITES_USING_CVM = "Set site parameters from SCEC CVM";

  /**
   * Longitude and Latitude paramerts to be added to the site params list
   */
  private DoubleParameter minLon = new DoubleParameter(MIN_LONGITUDE,
      new Double(-360), new Double(360),new Double(-119));
  private DoubleParameter maxLon = new DoubleParameter(MAX_LONGITUDE,
      new Double(-360), new Double(360),new Double(-117));
  private DoubleParameter minLat = new DoubleParameter(MIN_LATITUDE,
      new Double(-90), new Double(90), new Double(34.0));
  private DoubleParameter maxLat = new DoubleParameter(MAX_LATITUDE,
      new Double(-90), new Double(90), new Double(35.0));
  private DoubleParameter gridSpacing = new DoubleParameter(GRID_SPACING,
      new Double(.01),new Double(100.0),new String("Degrees"),new Double(.1));
  private DoubleParameter defaultVs30 = new DoubleParameter(this.VS30_DEFAULT,181,4500,
      new Double(this.VS30_DEFAULT_VALUE));

  //instance of class EvenlyGriddedRectangularGeographicRegion
  private SitesInGriddedRegion gridRectRegion;
  private JComboBox siteParamCombo = new JComboBox();
  private JLabel siteParamLabel = new JLabel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  /**
   * constuctor which builds up mapping between IMRs and their related sites
   */
  public SitesInGriddedRegionGuiBean() {
    // Build package names search path
    String[] searchPaths = new String[1];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();

    // add the longitude and latitude paramters
    parameterList.addParameter(minLon);
    parameterList.addParameter(maxLon);
    parameterList.addParameter(minLat);
    parameterList.addParameter(maxLat);
    parameterList.addParameter(gridSpacing);
    defaultVs30.setInfo(this.VS30_DEFAULT_INFO);
    parameterList.addParameter(defaultVs30);
    minLat.addParameterChangeFailListener(this);
    minLon.addParameterChangeFailListener(this);
    maxLat.addParameterChangeFailListener(this);
    maxLon.addParameterChangeFailListener(this);
    gridSpacing.addParameterChangeFailListener(this);
    defaultVs30.addParameterChangeFailListener(this);
    editorPanel = new ParameterListEditor(parameterList);
    editorPanel.setTitle(GRIDDED_SITE_PARAMS);
    editorPanel.getParameterEditor(this.VS30_DEFAULT).setVisible(false);
    createAndUpdateSites();
    this.siteParamCombo.addItem(this.SET_ALL_SITES);
    this.siteParamCombo.addItem(this.SET_SITES_USING_CVM);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    this.add(editorPanel,  new GridBagConstraints(0, 0, 2, 1, 2.0, 1.0
      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0,0));
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
       //Vector is used to pass the add the site parameters to the gridded region sites.
       siteTempVector.add(tempParam);
     }
   }
   gridRectRegion.addSiteParams(siteTempVector.iterator());
   this.remove(editorPanel);
   editorPanel = new ParameterListEditor(parameterList);
   editorPanel.setTitle(GRIDDED_SITE_PARAMS);
   setSiteParamsVisible();
   this.add(editorPanel,  new GridBagConstraints(0, 0, 2, 1, 2.0, 1.0
      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0,0));
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
       parameterList.addParameter(cloneParam);
       //adding the cloned parameter in the siteList.
       v.add(cloneParam);
     }
   }
   gridRectRegion.addSiteParams(v.iterator());
   this.remove(editorPanel);
   editorPanel = new ParameterListEditor(parameterList);
   editorPanel.setTitle(GRIDDED_SITE_PARAMS);
   setSiteParamsVisible();
   this.add(editorPanel,  new GridBagConstraints(0, 0, 2, 1, 2.0, 1.0
      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0,0));
 }

 /**
  * This function removes the previous site parameters and adds as passed in iterator
  *
  * @param it
  */
 public void replaceSiteParams(Iterator it) {
   // first remove all the parameters except latitude and longitude
   Iterator siteIt = parameterList.getParameterNamesIterator();
   while(siteIt.hasNext()) { // remove all the parameters except latitude and longitude and gridSpacing
     String paramName = (String)siteIt.next();
     if(!paramName.equalsIgnoreCase(MIN_LATITUDE) &&
        !paramName.equalsIgnoreCase(MIN_LONGITUDE) &&
        !paramName.equalsIgnoreCase(MAX_LATITUDE) &&
        !paramName.equalsIgnoreCase(MAX_LONGITUDE) &&
        !paramName.equalsIgnoreCase(GRID_SPACING)  &&
        !paramName.equalsIgnoreCase(this.VS30_DEFAULT))
       parameterList.removeParameter(paramName);
   }
   //removing the existing sites Params from the gridded Region sites
   Iterator it1=it;
   gridRectRegion.removeSiteParams();
   // now add all the new params
   addSiteParams(it);
 }



  /**
   * gets the iterator of all the sites
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
   * this function updates the GriddedRegion object after checking with the latest
   * lat and lons and gridSpacing
   * So, we update the site object as well
   *
   */
  private void updateGriddedSiteParams() {

    Vector v= new Vector();
    createAndUpdateSites();
    //getting the site params for the first element of the siteVector
    //becuase all the sites will be having the same site Parameter
    ListIterator it1=parameterList.getParametersIterator();
    while(it1.hasNext()){
      Parameter tempParam=(Parameter)it1.next();
      if(!tempParam.getName().equalsIgnoreCase(MIN_LONGITUDE) &&
         !tempParam.getName().equalsIgnoreCase(MIN_LATITUDE) &&
         !tempParam.getName().equalsIgnoreCase(MAX_LATITUDE) &&
         !tempParam.getName().equalsIgnoreCase(MAX_LONGITUDE) &&
         !tempParam.getName().equalsIgnoreCase(GRID_SPACING) &&
         !tempParam.getName().equalsIgnoreCase(VS30_DEFAULT))
        v.add(tempParam);
      //interesting region parameter has been selected
    }
    gridRectRegion.addSiteParams(v.iterator());
  }



  /**
   * Shown when a Constraint error is thrown on a ParameterEditor
   *
   * @param  e  Description of the Parameter
   */
  public void parameterChangeFailed( ParameterChangeFailEvent e ) {


    String S = C + " : parameterChangeFailed(): ";



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
   }


   /**
    * This method creates the gridded region with the min -max Lat and Lon
    * It also checks if the Max Lat is less than Min Lat and
    * Max Lat is Less than Min Lonb then it throws an exception.
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

    updateGriddedSiteParams();
    //if the site Params needs to be set from the CVM
    if(this.siteParamCombo.getSelectedItem().toString().equalsIgnoreCase(this.SET_SITES_USING_CVM)){
      setSiteParamsFromCVM();
      //sets the default vs30 in the site lies in water
      gridRectRegion.setDefaultVs30(((Double)parameterList.getParameter(this.VS30_DEFAULT).getValue()).doubleValue());
      gridRectRegion.setSiteParamsFromCVM(true,this.vs30Vector,this.basinDepthVector);
    }
    else //if the site params does not need to be set from the CVM
      gridRectRegion.setSiteParamsFromCVM(false,null,null);
    return gridRectRegion;
  }


  /**
   *
   * @returns the ParameterListEditor for the Gridded Region
   */
  public ParameterListEditor getGriddedRegionParameterListEditor(){
    return editorPanel;
  }
  private void jbInit() throws Exception {
    this.setLayout(gridBagLayout1);
    siteParamLabel.setBackground(Color.white);
    siteParamLabel.setFont(new java.awt.Font("Dialog", 1, 12));
    siteParamLabel.setForeground(new Color(80, 80, 133));
    siteParamLabel.setText("Set Site Params:");
    siteParamCombo.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        siteParamCombo_actionPerformed(e);
      }
    });
    siteParamCombo.setBackground(new Color(200, 200, 230));
    siteParamCombo.setForeground(new Color(80, 80, 133));
    this.add(siteParamCombo,  new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(4, 4, 4, 4), 0, 0));
    this.add(siteParamLabel,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
  }


  /**
   * This function is called if the user decides to fills the site params after seleceting
   * from the Combo Box
   * @param e
   */
  void siteParamCombo_actionPerformed(ActionEvent e) {
    setSiteParamsVisible();
  }


  /**
   * Make the site params visible depending on the choice user has made to
   * set the site Params
   */
  private void setSiteParamsVisible(){

    //getting the Gridded Region site Object ParamList Iterator
    Iterator it = parameterList.getParametersIterator();
    //if the user decides to fill the values from the CVM
    if(((String)siteParamCombo.getSelectedItem()).equalsIgnoreCase(this.SET_SITES_USING_CVM)){
      editorPanel.getParameterEditor(this.VS30_DEFAULT).setVisible(true);
      while(it.hasNext()){
        //makes the site Parameters Invisible becuase each site will have different site types
        ParameterAPI tempParam= (ParameterAPI)it.next();
        if(!tempParam.getName().equalsIgnoreCase(this.MAX_LATITUDE) &&
           !tempParam.getName().equalsIgnoreCase(this.MIN_LATITUDE) &&
           !tempParam.getName().equalsIgnoreCase(this.MAX_LONGITUDE) &&
           !tempParam.getName().equalsIgnoreCase(this.MIN_LONGITUDE) &&
           !tempParam.getName().equalsIgnoreCase(this.GRID_SPACING) &&
           !tempParam.getName().equalsIgnoreCase(this.VS30_DEFAULT))
          editorPanel.getParameterEditor(tempParam.getName()).setVisible(false);
      }
    }
    //if the user decides to go in with filling all the sites with the same site parameter,
    //then make that site parameter visible to te user
    else if(((String)siteParamCombo.getSelectedItem()).equalsIgnoreCase(this.SET_ALL_SITES)){
      editorPanel.getParameterEditor(this.VS30_DEFAULT).setVisible(false);
      while(it.hasNext()){
        ParameterAPI tempParam= (ParameterAPI)it.next();
        if(!tempParam.getName().equalsIgnoreCase(this.VS30_DEFAULT))
          editorPanel.getParameterEditor((tempParam).getName()).setVisible(true);
      }
    }
  }

  /**
   * set the Site Params from the CVM
   */
  private void setSiteParamsFromCVM(){

    // give latitude and longitude to the servlet
    Double lonMin = (Double)parameterList.getParameter(this.MIN_LONGITUDE).getValue();
    Double lonMax = (Double)parameterList.getParameter(this.MAX_LONGITUDE).getValue();
    Double latMin = (Double)parameterList.getParameter(MIN_LATITUDE).getValue();
    Double latMax = (Double)parameterList.getParameter(MAX_LATITUDE).getValue();
    Double gridSpacing = (Double)parameterList.getParameter(GRID_SPACING).getValue();

    if(lonMin.doubleValue()<this.MIN_CVM_LON ||
       lonMax.doubleValue()>this.MAX_CVM_LON ||
       latMin.doubleValue()<this.MIN_CVM_LAT ||
       latMax.doubleValue()>this.MAX_CVM_LAT) {

        throw new RuntimeException("CVM can not get params for this site\n"+
                                    "Constraints are:\n "+
                                    MIN_CVM_LON+" < Longitude < "+MAX_CVM_LON +"\n"+
                                    MIN_CVM_LAT+" < Latitude < "+MAX_CVM_LAT);

    }

    // if values in longitude and latitude are invalid
    if(lonMin == null || latMin == null) {
      JOptionPane.showMessageDialog(this,"Check the values in longitude and latitude");
      return ;
    }

    CalcProgressBar calcProgress = new CalcProgressBar("Setting Gridded Region sites","Getting the site paramters from the CVM");
    getVS30FromCVM(lonMin,lonMax,latMin,latMax,gridSpacing);
    getBasinDepthFromCVM(lonMin,lonMax,latMin,latMax,gridSpacing);
    JOptionPane.showMessageDialog(this,"We have site Paramaters from SCEC CVM");
    calcProgress.dispose();
  }


  /**
   * Gets the VS30 from the CVM servlet
   */
  private void getVS30FromCVM(Double lonMin,Double lonMax,Double latMin,Double latMax,
                              Double gridSpacing) {

    // if we want to the paramter from the servlet
    try{

      // make connection with servlet
      URL cvmServlet = new URL("http://scec.usc.edu:9999/examples/servlet/Vs30BasinDepthCalcServlet");
      URLConnection servletConnection = cvmServlet.openConnection();

      servletConnection.setDoOutput(true);

      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches (false);
      servletConnection.setDefaultUseCaches (false);

      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty ("Content-Type", "application/octet-stream");

      // send the student object to the servlet using serialization
      ObjectOutputStream outputToServlet = new ObjectOutputStream(servletConnection.getOutputStream());

      outputToServlet.writeObject("Vs30");
      outputToServlet.writeObject(lonMin);
      outputToServlet.writeObject(lonMax);
      outputToServlet.writeObject(latMin);
      outputToServlet.writeObject(latMax);
      outputToServlet.writeObject(gridSpacing);

      outputToServlet.flush();
      outputToServlet.close();

      // now read the connection again to get the vs30 as sent by the servlet
      ObjectInputStream ois=new ObjectInputStream(servletConnection.getInputStream());
      //vectors of lat and lon for the Vs30
      vs30Vector=(Vector)ois.readObject();
      ois.close();
    }catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this,"Check the values in longitude and latitude");
    }catch (Exception exception) {
      System.out.println("Exception in connection with servlet:" +exception);
    }
  }


  /**
   * Gets the Basin Depth from the CVM servlet
   */
  private void getBasinDepthFromCVM(Double lonMin,Double lonMax,Double latMin,Double latMax,
                                    Double gridSpacing) {

    // if we want to the paramter from the servlet
    try{

      // make connection with servlet
      URL cvmServlet = new URL("http://scec.usc.edu:9999/examples/servlet/Vs30BasinDepthCalcServlet");
      URLConnection servletConnection = cvmServlet.openConnection();

      servletConnection.setDoOutput(true);

      // Don't use a cached version of URL connection.
      servletConnection.setUseCaches (false);
      servletConnection.setDefaultUseCaches (false);

      // Specify the content type that we will send binary data
      servletConnection.setRequestProperty ("Content-Type", "application/octet-stream");

      // send the student object to the servlet using serialization
      ObjectOutputStream outputToServlet = new ObjectOutputStream(servletConnection.getOutputStream());

      outputToServlet.writeObject("BasinDepth");
      outputToServlet.writeObject(lonMin);
      outputToServlet.writeObject(lonMax);
      outputToServlet.writeObject(latMin);
      outputToServlet.writeObject(latMax);
      outputToServlet.writeObject(gridSpacing);

      outputToServlet.flush();
      outputToServlet.close();

      // now read the connection again to get the vs30 as sent by the servlet
      ObjectInputStream ois=new ObjectInputStream(servletConnection.getInputStream());

      //vectors of lat and lon for the Basin Depth
      basinDepthVector=(Vector)ois.readObject();
      ois.close();
    }catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this,"Check the values in longitude and latitude");
    }catch (Exception exception) {
      System.out.println("Exception in connection with servlet:" +exception);
    }
  }


}



