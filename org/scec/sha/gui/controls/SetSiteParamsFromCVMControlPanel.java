package org.scec.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.util.Vector;
import java.util.Iterator;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import org.scec.sha.gui.beans.IMR_GuiBean;
import org.scec.sha.gui.beans.Site_GuiBean;
import org.scec.sha.imr.AttenuationRelationshipAPI;
import org.scec.sha.util.SiteTranslator;
import org.scec.data.Site;
import org.scec.data.Location;
import org.scec.param.ParameterAPI;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class SetSiteParamsFromCVMControlPanel extends JFrame {
  JComboBox imrComboBox = new JComboBox();
  JButton setButton = new JButton();
  GridBagLayout gridBagLayout1 = new GridBagLayout();

  // options to be displayed in the combo box
  public static String SET_ALL_IMRS = "Set Site Params for all IMRs";
  public static String SET_SELECTED_IMR = "Set Site Params for Selected IMR";

  // min and max limits of lat and lin for which CVM can work
  private static final double MIN_CVM_LAT = 32.0;
  private static final double MAX_CVM_LAT = 36.0;
  private static final double MIN_CVM_LON = -121.0;
  private static final double MAX_CVM_LON = -114.0;

  // site translator
  SiteTranslator siteTranslator = new SiteTranslator();

  // save the imr gui bean  and  site gui bean
  private IMR_GuiBean imrGuiBean;
  private Site_GuiBean siteGuiBean;

  /**
   * This will set the site params from the CVM
   *
   * @param parent : parent frame on which to show this control panel
   * @param imrGuiBean : object of IMR_GuiBean to get the selected IMR or IMR list
   */
  public SetSiteParamsFromCVMControlPanel(Component parent, IMR_GuiBean imrGuiBean,
                                           Site_GuiBean siteGuiBean) {
    try {
      // fill the otions in the pick list
      this.imrComboBox.addItem(SET_ALL_IMRS);
      this.imrComboBox.addItem(SET_SELECTED_IMR);
      // show the window at center of the parent component
      this.setLocation(parent.getX()+parent.getWidth()/2,
                       parent.getY()+parent.getHeight()/2);
      // save the imr object list and IMR Gui Bean object
      this.imrGuiBean = imrGuiBean;
      this.siteGuiBean = siteGuiBean;
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(gridBagLayout1);
    imrComboBox.setBackground(new Color(200, 200, 230));
    imrComboBox.setForeground(new Color(80, 80, 133));
    setButton.setBackground(new Color(200, 200, 230));
    setButton.setForeground(new Color(80, 80, 133));
    setButton.setText("Set Params From CVM");
    setButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setButton_actionPerformed(e);
      }
    });
    this.getContentPane().add(imrComboBox,  new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(9, 5, 0, 6), 87, 4));
    this.getContentPane().add(setButton,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 23, 8, 23), 33, 5));
  }


  /**
   * This method is called when user presses the button to set the params from CVM
   *
   * @param e
   */
  void setButton_actionPerformed(ActionEvent e) {

    // get latitude and longitude
   Double lonMin = (Double)siteGuiBean.getParameterListEditor().getParameterList() .getParameter(Site_GuiBean.LONGITUDE).getValue();
   Double lonMax = new Double(lonMin.doubleValue());
   Double latMin = (Double)siteGuiBean.getParameterListEditor().getParameterList() .getParameter(Site_GuiBean.LATITUDE).getValue();
   Double latMax = new Double(latMin.doubleValue());

   // check that it lies within the constraints of southern california
   if(lonMin.doubleValue()<this.MIN_CVM_LON ||
      lonMax.doubleValue()>this.MAX_CVM_LON ||
      latMin.doubleValue()<this.MIN_CVM_LAT ||
      latMax.doubleValue()>this.MAX_CVM_LAT) {

     JOptionPane.showMessageDialog(this, "CVM can not get params for this site\n"+
                                   "Constraints are:\n "+
                                   MIN_CVM_LON+" < Longitude < "+MAX_CVM_LON +"\n"+
                                   MIN_CVM_LAT+" < Latitude < "+MAX_CVM_LAT);
     return;
   }

   // get the vs 30 and basin depth from cvm
   double vs30 = getVS30FromCVM(lonMin,lonMax,latMin,latMax);
   double basinDepth = getBasinDepthFromCVM(lonMin,lonMax,latMin,latMax);

   // now set the paramerts in the IMR
   if(this.imrComboBox.getSelectedItem().equals(this.SET_SELECTED_IMR)) { // do for selected IMR
       AttenuationRelationshipAPI imr =   this.imrGuiBean.getSelectedIMR_Instance();
       setSiteParamsInIMR(imr, lonMin.doubleValue(), latMin.doubleValue(),
                          vs30, basinDepth);
    } else { // do for all IMRS
       Vector imrObjects = this.imrGuiBean.getIMR_Objects();
       int num = imrObjects.size();
       for(int i=0; i<num; ++i)
         setSiteParamsInIMR((AttenuationRelationshipAPI)imrObjects.get(i),
                            lonMin.doubleValue(), latMin.doubleValue(),
                            vs30, basinDepth);
    }
    // reflect the new parameter value in GUI
    this.siteGuiBean.getParameterListEditor().synchToModel();
  }


  /**
   * set the site params in IMR according to basin Depth and vs 30
   * @param imr
   * @param lon
   * @param lat
   * @param vs30
   * @param basinDepth
   */
  private void setSiteParamsInIMR(AttenuationRelationshipAPI imr, double lon,
                             double lat, double vs30, double basinDepth) {
    // make the site object
    Site site = new Site(new Location(lat, lon));
    Iterator it = imr.getSiteParamsIterator(); // get site params for this IMR
    while(it.hasNext())  site.addParameter((ParameterAPI)it.next());
    this.siteTranslator.setSiteParams(site, vs30, basinDepth);
  }

  /**
   * Gets the VS30 from the CVM servlet
   */
  private double getVS30FromCVM(Double lonMin,Double lonMax,Double latMin,Double latMax) {

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
      Double gridSpacing = new Double(0.05);
      outputToServlet.writeObject(gridSpacing);

      outputToServlet.flush();
      outputToServlet.close();

      // now read the connection again to get the vs30 as sent by the servlet
      ObjectInputStream ois=new ObjectInputStream(servletConnection.getInputStream());
      // vector for vs30
      Vector vs30Vector=(Vector)ois.readObject();
      double vs30 = ((Double)vs30Vector.get(0)).doubleValue();
      ois.close();

      System.out.println("Vs30 is:"+vs30);
      JOptionPane.showMessageDialog(this,"We have got the vs30 from SCEC CVM");
      return vs30;
    }catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this,"Check the values in longitude and latitude");
    }catch (Exception exception) {
      System.out.println("Exception in connection with servlet:" +exception);
    }
    return -1;
  }

  /**
   * Gets the Basin Depth from the CVM servlet
   */
  private double getBasinDepthFromCVM(Double lonMin,Double lonMax,Double latMin,Double latMax) {

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
      Double gridSpacing = new Double(0.05);
      outputToServlet.writeObject("BasinDepth");
      outputToServlet.writeObject(lonMin);
      outputToServlet.writeObject(lonMax);
      outputToServlet.writeObject(latMin);
      outputToServlet.writeObject(latMax);
      outputToServlet.writeObject(gridSpacing);

      outputToServlet.flush();
      outputToServlet.close();

      // now read the connection again to get the basin depth as sent by the servlet
      ObjectInputStream ois=new ObjectInputStream(servletConnection.getInputStream());

      // vector for basin depth
      Vector basinDepthVector=(Vector)ois.readObject();
      double basinDepth = ((Double)basinDepthVector.get(0)).doubleValue();
      ois.close();

      System.out.println("basindepth is:"+ basinDepth );
      JOptionPane.showMessageDialog(this,"We have got the basin depth from SCEC CVM");
      return basinDepth;

    }catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this,"Check the values in longitude and latitude");
    }catch (Exception exception) {
      System.out.println("Exception in connection with servlet:" +exception);
    }
    return -1;
  }
}