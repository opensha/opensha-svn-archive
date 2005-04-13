package org.opensha.sha.gui.beans;

import java.util.*;
import java.lang.reflect.*;
import javax.swing.*;
import java.net.*;
import java.io.Serializable;
import java.io.*;
import java.awt.*;
import java.awt.event.*;


import org.opensha.param.*;
import org.opensha.param.editor.*;
import org.opensha.param.event.*;
import org.opensha.sha.util.Vs30SiteTranslator;
import org.opensha.exceptions.ParameterException;
import org.opensha.sha.gui.infoTools.ConnectToCVM;
import org.opensha.sha.gui.ObservedVsPredictedApplet;
import org.opensha.data.Site;
import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.nga.*;

/**
 * <p>Title:AttenuationSiteTypeParamsGuiBean </p>
 * <p>Description: This creates the Gridded Region parameter Editor with Site Params
 * for the selected Attenuation Relationship in the Application.
 * </p>
 * @author Nitin Gupta & Vipin Gupta
 * @date March 11, 2003
 * @version 1.0
 */



public class AttenuationSiteTypeParamsGuiBean extends ParameterListEditor implements
     ParameterChangeListener, ParameterChangeFailListener {

  // for debug purposes
  protected final static String C = "AttenuationSiteTypeParamsGuiBean";


  public final static String SITE_PARAM_NAME = "Set Site Params";

  public final static String DEFAULT = "Default  ";


  // title for site paramter panel
  public final static String GRIDDED_SITE_PARAMS = "Set Attenuation Site Params";

  //Site Params ArrayList
  ArrayList siteParams ;

  //Static String for setting the site Params
  public final static String SET_ALL_SITES = "Apply same site parameter(s) to all locations";
  public final static String SET_SITE_USING_WILLS_SITE_TYPE = "Use the CGS Preliminary Site Conditions Map of CA (web service)";
  public final static String SET_SITES_USING_SCEC_CVM = "Use both CGS Map and SCEC Basin Depth (web services)";



  //StringParameter to set site related params
  private StringParameter siteParam;



  //List of Vs30 for selected sites
  ArrayList vs30List;

  //List of basin depth for the selected sites
  ArrayList basinDepthList;


  //SiteTranslator
  Vs30SiteTranslator siteTrans = new Vs30SiteTranslator();


  public static final String PGA_PGV_FILE = "/Users/nitingupta/PEER_NGA_Data/nga_pga_pgv_data.txt";
  public static final String SA_FILE = "/Users/nitingupta/PEER_NGA_Data/peer_nga_sa.txt";



  ObservedVsPredictedApplet application ;
  /**
   * constuctor which builds up mapping between IMRs and their related sites
   */
  public AttenuationSiteTypeParamsGuiBean(ObservedVsPredictedApplet api) throws ParameterException{

    application = api;

    //readSiteDataFile();

    //creating the String Param for user to select how to get the site related params
    ArrayList siteOptions = new ArrayList();
    siteOptions.add(SET_ALL_SITES);
    siteOptions.add(SET_SITE_USING_WILLS_SITE_TYPE);
    siteOptions.add(SET_SITES_USING_SCEC_CVM);
    siteParam = new StringParameter(SITE_PARAM_NAME,siteOptions,(String)siteOptions.get(0));
    siteParam.addParameterChangeListener(this);

    // add the longitude and latitude paramters
    parameterList = new ParameterList();
    parameterList.addParameter(siteParam);
    editorPanel.removeAll();
    addParameters();

    readIM_DataFile();
    try {
      jbInit();
    }catch(Exception e) {
      e.printStackTrace();
    }
  }



  /**
   * Reads the observed data for the selected IMT
   */
  public  void readIM_DataFile(){

    String selectedIMT = application.getSelectedIMT();

    if(selectedIMT.equals(AttenuationRelationship.PGA_NAME))
      readFileForPGA();
    else if(selectedIMT.equals(AttenuationRelationship.PGV_NAME))
      readFileForPGV();
    else if(selectedIMT.equals(AttenuationRelationship.SA_NAME))
      readFileForSA();

  }


  /**
   * reads the file for the PGA
   */
  private void readFileForPGA(){

    EqkRuptureFromNGA rupture = application.getSelectedRupture();
    String eqkId = rupture.getEqkId();
    try{
      FileReader fr_sites =  new FileReader(PGA_PGV_FILE);
      boolean readIMvalues = true;
      //reading all the sites data for the earthquake id
      //reading the file that provides the site specific about the rupture
      BufferedReader br_sites = new BufferedReader(fr_sites);
      String lineFromSiteFile = br_sites.readLine();
      //reading each line of the file to get all the sites info.
      //corresponding to the earthquake.
      while(lineFromSiteFile !=null && readIMvalues){
        if(lineFromSiteFile.startsWith("#") || lineFromSiteFile.equals("\n") || lineFromSiteFile.equals("")){
          lineFromSiteFile = br_sites.readLine();
          continue;
        }
        StringTokenizer st_sites = new StringTokenizer(lineFromSiteFile,",");
        String id =st_sites.nextToken().trim();
        //Id is the first thing in the line that we read from the site info file.
        if(id.equals(eqkId)){//if both id matches then create get the sites info for that quake
          ArrayList pgaList = new ArrayList();
          while(id.equals(eqkId)){
            //getting the PGA observed by the site, if not null
            String pgaString = st_sites.nextToken().trim();
            if(pgaString !=null && !pgaString.equals(""))
              pgaList.add(new Double(Double.parseDouble(pgaString)));
            else
              pgaList.add(new Double(0.0));
            lineFromSiteFile = br_sites.readLine();
            st_sites = new StringTokenizer(lineFromSiteFile,",");
            id =st_sites.nextToken().trim();
          }
          rupture.addIM_Values(pgaList);
          readIMvalues = false;
        }
        else{ //if id doesn't match the read the next line.
          lineFromSiteFile = br_sites.readLine();
          continue;
        }
      }
      br_sites.close();
      fr_sites.close();
    }catch(Exception e){
      e.printStackTrace();
    }

  }

  /**
   * reads the file for PGV
   */
  private void readFileForPGV(){
    EqkRuptureFromNGA rupture = application.getSelectedRupture();
    String eqkId = rupture.getEqkId();
    try{
      FileReader fr_sites =  new FileReader(PGA_PGV_FILE);
      boolean readIMvalues = true;
      //reading all the sites data for the earthquake id
      //reading the file that provides the site specific about the rupture
      BufferedReader br_sites = new BufferedReader(fr_sites);
      String lineFromSiteFile = br_sites.readLine();
      //reading each line of the file to get all the sites info.
      //corresponding to the earthquake.
      while(lineFromSiteFile !=null && readIMvalues){
        if(lineFromSiteFile.startsWith("#") || lineFromSiteFile.equals("\n") || lineFromSiteFile.equals("")){
          lineFromSiteFile = br_sites.readLine();
          continue;
        }
        StringTokenizer st_sites = new StringTokenizer(lineFromSiteFile,",");
        String id =st_sites.nextToken().trim();
        //Id is the first thing in the line that we read from the site info file.
        if(id.equals(eqkId)){//if both id matches then create get the sites info for that quake
          ArrayList pgvList = new ArrayList();
          while(id.equals(eqkId)){
            st_sites.nextToken();
            //getting the PGV observed by the site, if not null
            String pgvString = st_sites.nextToken().trim();
            if(pgvString !=null && !pgvString.equals(""))
              pgvList.add(new Double(Double.parseDouble(pgvString)));
            else
              pgvList.add(new Double(0.0));
            lineFromSiteFile = br_sites.readLine();
            st_sites = new StringTokenizer(lineFromSiteFile,",");
            id =st_sites.nextToken().trim();
          }
          rupture.addIM_Values(pgvList);
          readIMvalues = false;
        }
        else{ //if is doesn't match the read the next line.
          lineFromSiteFile = br_sites.readLine();
          continue;
        }
      }
      br_sites.close();
      fr_sites.close();
    }catch(Exception e){
      e.printStackTrace();
    }


  }

  /**
   * reads the file for SA
   */
  private void readFileForSA(){
    EqkRuptureFromNGA rupture = application.getSelectedRupture();
    String imlSelected = application.getSelectedSAPeriod();
    double iml = Double.parseDouble(imlSelected.trim());

    String eqkId = rupture.getEqkId();
    try{
      FileReader fr_sites =  new FileReader(SA_FILE);
      boolean readIMvalues = true;
      //reading all the sites data for the earthquake id
      //reading the file that provides the site specific about the rupture
      BufferedReader br_sites = new BufferedReader(fr_sites);
      String lineFromSiteFile = br_sites.readLine();
      //tells the index of the SA value that needs to be read
      int tokenNumber = 1;
      if(lineFromSiteFile.startsWith("#")){
        StringTokenizer st = new StringTokenizer(lineFromSiteFile,",");
        st.nextToken();
        while(st.hasMoreTokens()){
          double saFromFile = Double.parseDouble(st.nextToken());
          if(iml == saFromFile){
            lineFromSiteFile = br_sites.readLine();
            break;
          }
          else
            ++tokenNumber;
        }
      }
      //reading each line of the file to get all the sites info.
      //corresponding to the earthquake.
      while(lineFromSiteFile !=null && readIMvalues){
        if(lineFromSiteFile.equals("\n") || lineFromSiteFile.equals("")){
          lineFromSiteFile = br_sites.readLine();
          continue;
        }
        StringTokenizer st_sites = new StringTokenizer(lineFromSiteFile,",");
        String id =st_sites.nextToken().trim();
        //Id is the first thing in the line that we read from the site info file.
        if(id.equals(eqkId)){//if both id matches then create get the sites info for that quake
          ArrayList saList = new ArrayList();
          while(id.equals(eqkId)){
            int i=0;
            while(i<tokenNumber){
              st_sites.nextToken();
              ++i;
            }
            //getting the SA observed by the site, if not null
            String saString = st_sites.nextToken().trim();
            if(saString !=null && !saString.equals(""))
              saList.add(new Double(Double.parseDouble(saString)));
            else
              saList.add(new Double(0.0));
            lineFromSiteFile = br_sites.readLine();
            st_sites = new StringTokenizer(lineFromSiteFile,",");
            id =st_sites.nextToken().trim();
          }
          rupture.addIM_Values(saList);
          readIMvalues = false;
        }
        else{ //if is doesn't match the read the next line.
          lineFromSiteFile = br_sites.readLine();
          continue;
        }
      }
      br_sites.close();
      fr_sites.close();
    }catch(Exception e){
      e.printStackTrace();
    }

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
   siteParams = new ArrayList();
   while(it.hasNext()) {
     tempParam = (Parameter)it.next();
     if(!parameterList.containsParameter(tempParam)) { // if this does not exist already
       parameterList.addParameter(tempParam);
       siteParams.add(tempParam.clone());
     }
   }

  editorPanel.removeAll();
  addParameters();
  //addSiteParamsToSiteList();
  setSiteParamsVisible();
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
   siteParams = new ArrayList();
   while(it.hasNext()) {
     tempParam = (Parameter)it.next();
     if(!parameterList.containsParameter(tempParam)) { // if this does not exist already
       Parameter cloneParam = (Parameter)tempParam.clone();
       parameterList.addParameter(cloneParam);
       siteParams.add(cloneParam);
     }
   }
   editorPanel.removeAll();
   addParameters();
   //addSiteParamsToSiteList();
   setSiteParamsVisible();
 }



 /**
  * Adding the site Params to the SiteList
  */
 public void addSiteParamsToSiteList(){
   ArrayList siteList  = application.getSelectedRupture().getSiteList();
   int size = siteList.size();
   for(int i=0;i<size;++i){
     Site site  = (Site)siteList.get(i);
     int size1 = siteParams.size();
     for(int j=0;j<size1;++j){
       ParameterAPI tempParam = (ParameterAPI)((ParameterAPI)siteParams.get(j)).clone();
       if(!tempParam.getName().equals(SITE_PARAM_NAME) && !site.containsParameter(tempParam))
         site.addParameter(tempParam);
     }
   }
 }


 /**
  * This function removes the previous site parameters and adds as passed in iterator
  *
  * @param it
  */
 public void replaceSiteParams(Iterator it) {


   // first remove all the parameters ewxcept latitude and longitude
   Iterator siteIt = parameterList.getParameterNamesIterator();
   while(siteIt.hasNext()) { // remove all the parameters except latitdue and longitude
     String paramName = (String)siteIt.next();
     if(!paramName.equals(SITE_PARAM_NAME))
       parameterList.removeParameter(paramName);
   }
   // now add all the new params
   addSiteParams(it);

 }


 /**
  * This function is called when value a parameter is changed
  * @param e Description of the parameter
  */
 public void parameterChange(ParameterChangeEvent e){
   ParameterAPI param = ( ParameterAPI ) e.getSource();

   if(param.getName().equals(SITE_PARAM_NAME))
     setSiteParamsVisible();
 }



  /**
   * this function sets the siteList to get the site params value for those.
   * @param siteList : List of locations for which we need to get site type value.
   */
  public void setSites() throws ParameterException,RuntimeException{

    //adding the site params to each site in the list
    addSiteParamsToSiteList();

    if(!((String)siteParam.getValue()).equals(SET_ALL_SITES)){

      ArrayList defaultSiteParams = new ArrayList();
      for(int i=0;i<siteParams.size();++i){
        ParameterAPI tempParam = (ParameterAPI)((ParameterAPI)siteParams.get(i)).clone();
        tempParam.setValue(parameterList.getParameter(this.DEFAULT+tempParam.getName()).getValue());
        defaultSiteParams.add(tempParam);
      }

       //if the site Params needs to be set from the WILLS Site type and SCEC basin depth
      try{
        setSiteParamsFromCVM(defaultSiteParams);
      }catch(Exception e){
        e.printStackTrace();
        throw new RuntimeException("Server is down , please try again later");
      }

     }
  }



  /**
   * Make the site params visible depending on the choice user has made to
   * set the site Params
   */
  private void setSiteParamsVisible(){

    //getting the Gridded Region site Object ParamList Iterator
    Iterator it = parameterList.getParametersIterator();
    //if the user decides to fill the values from the CVM
    if(((String)siteParam.getValue()).equals(SET_SITES_USING_SCEC_CVM)||
       ((String)siteParam.getValue()).equals(SET_SITE_USING_WILLS_SITE_TYPE)){
      //editorPanel.getParameterEditor(this.VS30_DEFAULT).setVisible(true);
      while(it.hasNext()){
        //adds the default site Parameters becuase each site will have different site types and default value
        //has to be given if site lies outside the bounds of CVM
        ParameterAPI tempParam= (ParameterAPI)it.next();
        if(!tempParam.getName().equalsIgnoreCase(this.SITE_PARAM_NAME)){

          //removing the existing site Params from the List and adding the
          //new Site Param with site as being defaults
          parameterList.removeParameter(tempParam.getName());

          //creating the new Site Param, with "Default " added to its name, with existing site Params
          ParameterAPI newParam = (ParameterAPI)tempParam.clone();
          //If the parameterList already contains the site param with the "Default" name, then no need to change the existing name.
          if(!newParam.getName().startsWith(this.DEFAULT))
            newParam.setName(this.DEFAULT+newParam.getName());
          //making the new parameter to uneditable same as the earlier site Param, so that
          //only its value can be changed and not it properties
          newParam.setNonEditable();
          newParam.addParameterChangeFailListener(this);

          //adding the parameter to the List if not already exists
          if(!parameterList.containsParameter(newParam.getName()))
            parameterList.addParameter(newParam);
        }
      }
    }
    //if the user decides to go in with filling all the sites with the same site parameter,
    //then make that site parameter visible to te user
    else if(((String)siteParam.getValue()).equals(SET_ALL_SITES)){
      while(it.hasNext()){
        //removing the default Site Type Params if same site is to be applied to whole region
        ParameterAPI tempParam= (ParameterAPI)it.next();
        if(tempParam.getName().startsWith(this.DEFAULT))
          parameterList.removeParameter(tempParam.getName());
      }
      //Adding the Site related params to the ParameterList
      ListIterator it1 = siteParams.listIterator();
      while(it1.hasNext()){
        ParameterAPI tempParam = (ParameterAPI)it1.next();
        if(!parameterList.containsParameter(tempParam.getName()))
          parameterList.addParameter(tempParam);
      }
    }

    //creating the ParameterList Editor with the updated ParameterList
    editorPanel.removeAll();
    addParameters();
    editorPanel.validate();
    editorPanel.repaint();
    setTitle(GRIDDED_SITE_PARAMS);
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
   * set the Site Params from the CVM
   */
  private void setSiteParamsFromCVM(ArrayList defaultSiteParams){

    ArrayList siteList = application.getSelectedRupture().getSiteList();
    if(((String)siteParam.getValue()).equals(SET_SITES_USING_SCEC_CVM))
      //if we are setting the each site type using Wills site type and SCEC basin depth
      setSiteParamsUsing_WILLS_VS30_AndBasinDepth(siteList);
    else if(((String)siteParam.getValue()).equals(SET_SITE_USING_WILLS_SITE_TYPE))
      //if we are setting each site using the Wills site type. basin depth is taken as default.
      setSiteParamsUsing_WILLS_VS30(siteList);

    int size = siteList.size();

    for(int i=0;i<size;++i){
      Site site = (Site)siteList.get(i);
      Iterator it = site.getParametersIterator();
       //checking to see if we are getting the correct value for vs30 and basin depth.
       if(D){
         System.out.println(site.getLocation().toString()+"\t"+vs30List.get(i)+
                            "\t\t"+((Double)basinDepthList.get(i)).doubleValue());
       }
       while(it.hasNext()){
         ParameterAPI tempParam = (ParameterAPI)it.next();

         //Setting the value of each site Parameter from the CVM and translating them into the Attenuation related site
         boolean flag = siteTrans.setSiteParams(tempParam,((Double)vs30List.get(i)).doubleValue(),
                                                         ((Double)basinDepthList.get(i)).doubleValue());
         //If the value was outside the bounds of CVM
         //and site has no value from CVM then set its value to the default Site Params shown in the application.
         if(flag){
           //iterating over the default site parameters to set the Site Param if
           //no value has been obtained from the CVM for that site.
           Iterator it1 = defaultSiteParams.iterator();
           while(it1.hasNext()){
             ParameterAPI param = (ParameterAPI)it1.next();
             if(tempParam.getName().equals(param.getName()))
               tempParam.setValue(param.getValue());
           }
         }
       }
    }
  }

  /**
   * This function is called if the site Params need to be set using WILLS site type.
   * As Wills Site type provide no value for the Basin depth so we set it to Double.Nan
   */
  public void setSiteParamsUsing_WILLS_VS30(ArrayList siteList){

    int size = siteList.size();
    try{
     vs30List = application.getObservedRuptureVs30Values();
    }catch(Exception e){
      /*vs30 = ConnectToCVM.getWillsSiteType(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),
          getGridSpacing(),WILLS_SITE_CLASS_FILE);*/
      //throw new RuntimeException(e.getMessage());
    }

    basinDepthList = new ArrayList();
    for(int i=0;i<size;++i)
      basinDepthList.add(new Double(Double.NaN));
  }


  /**
   * This function is called if the site Params need to be set using WILLS site type
   * and basin depth from the SCEC basin depth values.
   */
  public void setSiteParamsUsing_WILLS_VS30_AndBasinDepth(ArrayList siteList){

    try{
      vs30List = application.getObservedRuptureVs30Values();
      int size = siteList.size();
      basinDepthList = new ArrayList();

      for(int i=0;i<size;++i){
        double lat = ((Site)siteList.get(i)).getLocation().getLatitude();
        double lon = ((Site)siteList.get(i)).getLocation().getLongitude();
        basinDepthList.add((ConnectToCVM.getBasinDepthFromCVM(lon,lon,lat,lat,0.5)).get(0));
      }
    }catch(Exception e){
      /*vs30 = ConnectToCVM.getWillsSiteType(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),
          getGridSpacing(),WILLS_SITE_CLASS_FILE);
      basinDepth = ConnectToCVM.getBasinDepth(getMinLon(),getMaxLon(),getMinLat(),getMaxLat(),
          getGridSpacing(),BASIN_DEPTH_FILE);*/
      e.printStackTrace();
      throw new RuntimeException(e.getMessage());
    }
  }
}
