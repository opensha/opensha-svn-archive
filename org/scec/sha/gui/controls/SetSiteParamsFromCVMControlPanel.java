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
import org.scec.sha.gui.infoTools.ConnectToCVM;

/**
 * <p>Title:SetSiteParamsFromCVMControlPanel </p>
 * <p>Description: Get the Site Params from the CVM</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
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
   String vs30 = "NA";
   double basinDepth = Double.NaN;
   try{
   // get the vs 30 and basin depth from cvm
   vs30 = (String)(ConnectToCVM.getWillsSiteTypeFromCVM(lonMin.doubleValue(),lonMax.doubleValue(),
                                                      latMin.doubleValue(),latMax.doubleValue(),
                                                      0.5)).get(0);
   basinDepth = ((Double)(ConnectToCVM.getBasinDepthFromCVM(lonMin.doubleValue(),lonMax.doubleValue(),
                                                           latMin.doubleValue(),latMax.doubleValue(),
                                                           0.5)).get(0)).doubleValue();
   }catch(Exception ee){
     JOptionPane.showMessageDialog(this,"Server is down for maintenance, please try again later","Server Problem",JOptionPane.INFORMATION_MESSAGE);
     return;
   }

   System.out.println("Vs30: "+vs30+"  BasinDepth: "+basinDepth);
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
    this.siteGuiBean.getParameterListEditor().refreshParamEditor();
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
                             double lat, String vs30, double basinDepth) {
    // make the site object
    Site site = new Site(new Location(lat, lon));
    Iterator it = imr.getSiteParamsIterator(); // get site params for this IMR
    while(it.hasNext()) {
      ParameterAPI tempParam = (ParameterAPI)it.next();
      //adding the site Params from the CVM, if site is out the range of CVM then it
      //sets the site with whatever site Parameter Value user has choosen in the application
      boolean flag = siteTranslator.setParameterValue(tempParam,vs30,basinDepth);
      site.addParameter(tempParam);
    }

  }
}