package org.scec.sha.gui.beans;

import java.util.*;
import java.lang.reflect.*;
import java.net.*;
import java.io.*;


import org.scec.param.*;
import org.scec.param.editor.*;
import org.scec.param.event.*;
import org.scec.sha.imr.AttenuationRelationshipAPI;
import org.scec.data.Site;
import org.scec.data.Location;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import org.scec.sha.util.SiteTranslator;

/**
 * <p>Title:SiteParamListEditor </p>
 * <p>Description: this class will make the site parameter editor.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date Oct 29, 2002
 * @version 1.0
 */



public class Site_GuiBean extends JPanel implements
    ParameterChangeListener, ParameterChangeFailListener {

  // for debug purposes
 protected final static String C = "SiteParamList";

  /**
   * Latitude and longitude are added to the site paraattenRelImplmeters
   */
  public final static String LONGITUDE = "Longitude";
  public final static String LATITUDE = "Latitude";


  //VS30 and Basin Depth String Names
  protected final static String VS30_STRING = "Vs30";
  protected final static String BASIN_DEPTH_STRING = "Basin Depth (Phase III)";

  //stores the Vs30 from CVM
  double vs30 ;
  //stores the basin depth from CVM
  double basinDepth ;

  // min and max limits of lat and lin for which CVM can work
  private static final double MIN_CVM_LAT = 32.0;
  private static final double MAX_CVM_LAT = 36.0;
  private static final double MIN_CVM_LON = -121.0;
  private static final double MAX_CVM_LON = -114.0;

  // site translator
  SiteTranslator siteTranslator = new SiteTranslator();

  /**
   * Site object
   */
  private Site site;

  // title for site paramter panel
  protected final static String SITE_PARAMS = "Set Site Params";

  private ParameterList parameterList = new ParameterList();
  private ParameterListEditor parameterEditor;

  /**
   * Longitude and Latitude paramerts to be added to the site params list
   */
  private DoubleParameter longitude = new DoubleParameter(LONGITUDE,
      new Double(-360), new Double(360),new Double(-118));
  private DoubleParameter latitude = new DoubleParameter(LATITUDE,
      new Double(-90), new Double(90), new Double(34.0));
  private JCheckBox cvmCheckBox = new JCheckBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  /**
   * constuctor which builds up mapping between IMRs and their related sites
   */
  public Site_GuiBean() {
    // Build package names search path
    String[] searchPaths = new String[1];
    searchPaths[0] = ParameterListEditor.getDefaultSearchPath();

    // add the longitude and latitude paramters
    parameterList.addParameter(longitude);
    parameterList.addParameter(latitude);
    latitude.addParameterChangeListener(this);
    longitude.addParameterChangeListener(this);
    latitude.addParameterChangeFailListener(this);
    longitude.addParameterChangeFailListener(this);

    // maake the new site object
    site= new Site(new Location(((Double)latitude.getValue()).doubleValue(),
                                ((Double)longitude.getValue()).doubleValue()));
    parameterEditor = new ParameterListEditor(parameterList,searchPaths);
    parameterEditor.setTitle(SITE_PARAMS);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    this.add(parameterEditor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
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
   while(it.hasNext()) {
     tempParam = (Parameter)it.next();
     if(!parameterList.containsParameter(tempParam)) { // if this does not exist already
       parameterList.addParameter(tempParam);
       if(tempParam instanceof StringParameter) { // if it Stringparamter, set its initial values
         StringParameter strConstraint = (StringParameter)tempParam;
         tempParam.setValue(strConstraint.getAllowedStrings().get(0));
        }
     }
     if(!site.containsParameter(tempParam))
       site.addParameter(tempParam);
   }

  this.remove(parameterEditor);
  parameterEditor= new ParameterListEditor(parameterList);
  parameterEditor.setTitle(SITE_PARAMS);
  this.add(parameterEditor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0,0));
  if(this.cvmCheckBox.isSelected())   setSiteParamsFromCVM();
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
   while(it.hasNext()) {
     tempParam = (Parameter)it.next();
     if(!parameterList.containsParameter(tempParam)) { // if this does not exist already
       Parameter cloneParam = (Parameter)tempParam.clone();
       if(tempParam instanceof StringParameter) {
         StringParameter strConstraint = (StringParameter)tempParam;
         cloneParam.setValue(strConstraint.getAllowedStrings().get(0));
        }
       parameterList.addParameter(cloneParam);
       site.addParameter(cloneParam);
     }
   }
   this.remove(parameterEditor);
   parameterEditor= new ParameterListEditor(parameterList);
   parameterEditor.setTitle(SITE_PARAMS);
   this.add(parameterEditor,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
      ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0,0));
   if(this.cvmCheckBox.isSelected())   setSiteParamsFromCVM();

 }

 /**
  * This function removes the previous site parameters and adds as passed in iterator
  *
  * @param it
  */
 public void replaceSiteParams(Iterator it) {

   // make the new site object
   site= new Site(new Location(((Double)latitude.getValue()).doubleValue(),
                                ((Double)longitude.getValue()).doubleValue()));
   // first remove all the parameters ewxcept latitude and longitude
   Iterator siteIt = parameterList.getParameterNamesIterator();
   while(siteIt.hasNext()) { // remove all the parameters except latitdue and longitude
     String paramName = (String)siteIt.next();
     if(!paramName.equalsIgnoreCase(LATITUDE) &&
        !paramName.equalsIgnoreCase(LONGITUDE)){
       parameterList.removeParameter(paramName);
     }
   }
   // now add all the new params
   addSiteParams(it);

 }

 /**
  * Display the site params based on the site passed as the parameter
  */
 public void setSite(Site site) {
   this.site = site;
   Iterator it  = site.getParametersIterator();
   replaceSiteParams(it);
 }


  /**
   * get the site object from the site params
   *
   * @return
   */
  public Site getSite() {
    return site;
  }


  /**
   * get the clone of site object from the site params
   *
   * @return
   */
  public Site getSiteClone() {
    Site newSite = new Site(new Location(((Double)latitude.getValue()).doubleValue(),
        ((Double)longitude.getValue()).doubleValue()));
    Iterator it  = site.getParametersIterator();

    // clone the paramters
    while(it.hasNext())
      newSite.addParameter( (ParameterAPI) ((ParameterAPI)it.next()).clone());
    return site;
  }

  /**
   * this function when longitude or latitude are updated
   * So, we update the site object as well
   *
   * @param e
   */
  public void parameterChange(ParameterChangeEvent e) {
    site.setLocation(new Location(((Double)latitude.getValue()).doubleValue(),
                                  ((Double)longitude.getValue()).doubleValue()));
    if(this.cvmCheckBox.isSelected())   setSiteParamsFromCVM();

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
  private void jbInit() throws Exception {
    cvmCheckBox.setBackground(Color.white);
    cvmCheckBox.setFont(new java.awt.Font("Dialog", 1, 10));
    cvmCheckBox.setForeground(new Color(80, 80, 133));
    cvmCheckBox.setText("Set Site from CVM");
    cvmCheckBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cvmCheckBox_actionPerformed(e);
      }
    });
    this.setLayout(gridBagLayout1);
    this.setBackground(Color.white);
    this.add(cvmCheckBox,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 4, 4, 4), 0, 0));
  }

  /**
   * If the user selects to choose set the site params from the CVM
   * @param e
   */
  void cvmCheckBox_actionPerformed(ActionEvent e) {
    if(this.cvmCheckBox.isSelected())   setSiteParamsFromCVM();
  }

  /**
   * set the Site Params from the CVM
   */
  private void setSiteParamsFromCVM(){

    // give latitude and longitude to the servlet
      Double lonMin = (Double)parameterList.getParameter(LONGITUDE).getValue();
      Double lonMax = new Double(lonMin.doubleValue());
      Double latMin = (Double)parameterList.getParameter(LATITUDE).getValue();
      Double latMax = new Double(latMin.doubleValue());
      if(lonMin.doubleValue()<this.MIN_CVM_LON ||
         lonMax.doubleValue()>this.MAX_CVM_LON ||
         latMin.doubleValue()<this.MIN_CVM_LAT ||
         latMax.doubleValue()>this.MAX_CVM_LAT) {

        JOptionPane.showMessageDialog(this, "CVM can not get params for this site\n"+
                                      "Constraints are:\n "+
                                      MIN_CVM_LON+" < Longitude < "+MAX_CVM_LON +"\n"+
                                      MIN_CVM_LAT+" < Latitude < "+MAX_CVM_LAT);
        this.cvmCheckBox.setSelected(false);
        return;
      }

      Double gridSpacing = new Double(0.05);

      // if values in longitude and latitude are invalid
      if(lonMin == null || latMin == null) {
        JOptionPane.showMessageDialog(this,"Check the values in longitude and latitude");
        this.cvmCheckBox.setSelected(false);
        return ;
      }
      getVS30FromCVM(lonMin,lonMax,latMin,latMax,gridSpacing);
      getBasinDepthFromCVM(lonMin,lonMax,latMin,latMax,gridSpacing);
      siteTranslator.setSiteParams(this.getSite(),vs30, basinDepth);
      this.parameterEditor.synchToModel();

  }


  /**
   * Gets the VS30 from the CVM servlet
   */
  private void getVS30FromCVM(Double lonMin,Double lonMax,Double latMin,Double latMax,
                              Double gridSpacing) {

    // if we want to the paramter from the servlet
    try{

      // make connection with servlet
      URL cvmServlet = new URL("http://scec.usc.edu:9999/examples/servlet/Vs30BasinDepthServlet");
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
      Vector vslatVector=(Vector)ois.readObject();
      Vector vslonVector=(Vector)ois.readObject();
      Vector vs30Vector=(Vector)ois.readObject();
      vs30 = ((Double)vs30Vector.get(0)).doubleValue();
      ois.close();

      System.out.println("Vs30 is:"+vs30);
      JOptionPane.showMessageDialog(this,"We have got the Basin Depth from SCEC CVM");

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
      URL cvmServlet = new URL("http://scec.usc.edu:9999/examples/servlet/Vs30BasinDepthServlet");
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
      Vector bdlatVector=(Vector)ois.readObject();
      Vector bdlonVector=(Vector)ois.readObject();
      Vector basinDepthVector=(Vector)ois.readObject();
      this.basinDepth = ((Double)basinDepthVector.get(0)).doubleValue();
      ois.close();

      System.out.println("basindepth is:"+ this.basinDepth );
      JOptionPane.showMessageDialog(this,"We have got the Vs30 from SCEC CVM");

    }catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this,"Check the values in longitude and latitude");
    }catch (Exception exception) {
      System.out.println("Exception in connection with servlet:" +exception);
    }
  }


  /**
   *
   * @returns the site ParamListEditor
   */
  public ParameterListEditor getParameterListEditor(){
    return parameterEditor;
  }
}