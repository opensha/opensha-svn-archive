package org.opensha.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import org.opensha.sha.gui.beans.IMR_GuiBean;
import org.opensha.sha.gui.beans.Site_GuiBean;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.util.SiteTranslator;
import org.opensha.data.Site;
import org.opensha.data.Location;
import org.opensha.param.ParameterAPI;
import org.opensha.sha.gui.infoTools.ConnectToCVM;
import org.opensha.util.SystemPropertiesUtils;

/**
 * <p>Title:SetSiteParamsFromWebServicesControlPanel </p>
 * <p>Description: Get the Site Params from the CVM</p>
 * @author : Ned Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class SetSiteParamsFromWebServicesControlPanel extends JFrame {
  JButton setAllIMRButton = new JButton();

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
  private JTextPane siteInfoPane = new JTextPane();
  private JButton setSelectedIMRButton = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();

  /**
   * This will set the site params from the CVM
   *
   * @param parent : parent frame on which to show this control panel
   * @param imrGuiBean : object of IMR_GuiBean to get the selected IMR or IMR list
   */
  public SetSiteParamsFromWebServicesControlPanel(Component parent, IMR_GuiBean imrGuiBean,
                                           Site_GuiBean siteGuiBean) {
    try {
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
    setAllIMRButton.setText("Set Params For All IMRs");
    setAllIMRButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setAllIMRButton_actionPerformed(e);
      }
    });


    String info = new String("This uses both the CGS Preliminary Site "+
                             "Conditions Map of CA (Wills et al., 2000) "+
                             "and basin depth from the SCEC CVM.");


    siteInfoPane.setBackground(SystemColor.menu);
    siteInfoPane.setEnabled(false);
    siteInfoPane.setPreferredSize(new Dimension(300, 64));
    siteInfoPane.setEditable(false);
    siteInfoPane.setText(info);
    setSelectedIMRButton.setText("Set Params For Chosen IMR");
    setSelectedIMRButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setSelectedIMRButton_actionPerformed(e);
      }
    });
    this.getContentPane().add(siteInfoPane,  new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(12, 31, 18, 4), 0, 0));
    this.getContentPane().add(setAllIMRButton,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(27, 72, 0, 94), 23, 4));
    this.getContentPane().add(setSelectedIMRButton,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(7, 72, 0, 94), 1, 4));
    this.setTitle("Set Site Related Params" );
  }


  /**
   * This method is called when user presses the button to set the params from CVM
   * for all IMR's
   * @param e
   */
  void setAllIMRButton_actionPerformed(ActionEvent e) {

    // get latitude and longitude
   Double lonMin = (Double)siteGuiBean.getParameterListEditor().getParameterList() .getParameter(Site_GuiBean.LONGITUDE).getValue();
   Double lonMax = new Double(lonMin.doubleValue());
   Double latMin = (Double)siteGuiBean.getParameterListEditor().getParameterList() .getParameter(Site_GuiBean.LATITUDE).getValue();
   Double latMax = new Double(latMin.doubleValue());
   String willsClass = "NA";
   double basinDepth = Double.NaN;
   try{
   // get the vs 30 and basin depth from cvm
   willsClass = (String)(ConnectToCVM.getWillsSiteTypeFromCVM(lonMin.doubleValue(),lonMax.doubleValue(),
                                                      latMin.doubleValue(),latMax.doubleValue(),
                                                      0.5)).get(0);
   basinDepth = ((Double)(ConnectToCVM.getBasinDepthFromCVM(lonMin.doubleValue(),lonMax.doubleValue(),
                                                           latMin.doubleValue(),latMax.doubleValue(),
                                                           0.5)).get(0)).doubleValue();
   }catch(Exception ee){
     JOptionPane.showMessageDialog(this,"Server is down for maintenance, please try again later","Server Problem",JOptionPane.INFORMATION_MESSAGE);
     return;
   }

   // do for all IMRS
   ArrayList imrObjects = this.imrGuiBean.getSupportedIMRs();
   int num = imrObjects.size();
   for(int i=0; i<num; ++i)
     setSiteParamsInIMR((AttenuationRelationshipAPI)imrObjects.get(i),
                        lonMin.doubleValue(), latMin.doubleValue(),
                        willsClass, basinDepth);

   // reflect the new parameter value in GUI
   this.siteGuiBean.getParameterListEditor().refreshParamEditor();
   this.dispose();
  }


  /**
   * set the site params in IMR according to basin Depth and vs 30
   * @param imr
   * @param lon
   * @param lat
   * @param willsClass
   * @param basinDepth
   */
  private void setSiteParamsInIMR(AttenuationRelationshipAPI imr, double lon,
                             double lat, String willsClass, double basinDepth) {

    Iterator it = imr.getSiteParamsIterator(); // get site params for this IMR
    while(it.hasNext()) {
      ParameterAPI tempParam = (ParameterAPI)it.next();
      //adding the site Params from the CVM, if site is out the range of CVM then it
      //sets the site with whatever site Parameter Value user has choosen in the application
      boolean flag = siteTranslator.setParameterValue(tempParam,willsClass,basinDepth);
      if( !flag ) {
        String message = "cannot set the site parameter \""+tempParam.getName()+"\" from Wills class \""+willsClass+"\""+
                         "\n (no known, sanctioned translation - please set by hand)";
        JOptionPane.showMessageDialog(this,message,"Warning",JOptionPane.OK_OPTION);
      }
    }

  }

  /**
   * This method is called when user presses the button to set the params from CVM
   * for choosen IMR's
   * @param e
   */
  void setSelectedIMRButton_actionPerformed(ActionEvent e) {
    // get latitude and longitude
    Double lonMin = (Double)siteGuiBean.getParameterListEditor().getParameterList() .getParameter(Site_GuiBean.LONGITUDE).getValue();
    Double lonMax = new Double(lonMin.doubleValue());
    Double latMin = (Double)siteGuiBean.getParameterListEditor().getParameterList() .getParameter(Site_GuiBean.LATITUDE).getValue();
    Double latMax = new Double(latMin.doubleValue());
    String willsClass = "NA";
    double basinDepth = Double.NaN;
    try{
      // get the vs 30 and basin depth from cvm
      willsClass = (String)(ConnectToCVM.getWillsSiteTypeFromCVM(lonMin.doubleValue(),lonMax.doubleValue(),
          latMin.doubleValue(),latMax.doubleValue(),0.5)).get(0);
      basinDepth = ((Double)(ConnectToCVM.getBasinDepthFromCVM(lonMin.doubleValue(),lonMax.doubleValue(),
          latMin.doubleValue(),latMax.doubleValue(),0.5)).get(0)).doubleValue();
    }catch(Exception ee){
      //ee.printStackTrace();
      JOptionPane.showMessageDialog(this,"Server is down for maintenance, please try again later","Server Problem",JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    // do for selected IMR
    AttenuationRelationshipAPI imr =   this.imrGuiBean.getSelectedIMR_Instance();
    setSiteParamsInIMR(imr, lonMin.doubleValue(), latMin.doubleValue(),
                              willsClass, basinDepth);
    // reflect the new parameter value in GUI
   this.siteGuiBean.getParameterListEditor().refreshParamEditor();
   this.dispose();
  }
}
