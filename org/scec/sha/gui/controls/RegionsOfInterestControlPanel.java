package org.scec.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.util.Vector;

import org.scec.sha.gui.beans.SitesInGriddedRegionGuiBean;
import java.awt.event.*;
/**
 * <p>Title: SitesOfInterest </p>
 * <p>Description: It displays a list of interesting sites which user can choose </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class RegionsOfInterestControlPanel extends JFrame {
  private JLabel jLabel1 = new JLabel();
  private JComboBox regionsComboBox = new JComboBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private Vector minLatVector = new Vector();
  private Vector maxLatVector = new Vector();
  private Vector minLonVector = new Vector();
  private Vector maxLonVector = new Vector();
  private SitesInGriddedRegionGuiBean regionGuiBean;

  /**
   * Constructor
   *
   * @param parent : parent component which calls this control panel
   * @param siteGuiBean : site gui bean to set the lat and lon
   */
  public RegionsOfInterestControlPanel(Component parent,
                                       SitesInGriddedRegionGuiBean regionGuiBean) {
    try {
      this.regionGuiBean = regionGuiBean;


      /*
      * add interesting regions
      */

      // san francisco
      regionsComboBox.addItem("SF Bay Area");
      minLatVector.add(new Double(37.3));
      maxLatVector.add(new Double(38.5));
      minLonVector.add(new Double(-123));
      maxLonVector.add(new Double(-121.5));

      //CVM region
      regionsComboBox.addItem("Greater LA Region");
      minLatVector.add(new Double(33.5));
      maxLatVector.add(new Double(34.7));
      minLonVector.add(new Double(-119.5));
      maxLonVector.add(new Double(-117.0));



      jbInit();
      // show the window at center of the parent component
      this.setLocation(parent.getX()+parent.getWidth()/2,
                       parent.getY()+parent.getHeight()/2);
      // set lat and lon
      this.setLatAndLon();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    jLabel1.setFont(new java.awt.Font("Dialog", 1, 12));
    jLabel1.setForeground(new Color(80, 80, 133));
    jLabel1.setText("Choose Region:");
    this.getContentPane().setLayout(gridBagLayout1);
    this.setTitle("Regions Of Interest");
    regionsComboBox.setBackground(new Color(200, 200, 230));
    regionsComboBox.setForeground(new Color(80, 80, 133));
    regionsComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        regionsComboBox_actionPerformed(e);
      }
    });
    this.getContentPane().add(regionsComboBox,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(14, 6, 10, 12), 149, 2));
    this.getContentPane().add(jLabel1,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(14, 5, 10, 0), 13, 11));
  }

  /**
   * whenever user selects an interesting site, this function is called
   * @param e
   */
  void regionsComboBox_actionPerformed(ActionEvent e) {
    setLatAndLon();
  }

  /**
   * to set lat and lon according to user selection
   */
  private void setLatAndLon() {
    int index = this.regionsComboBox.getSelectedIndex();
    // set the lat and lon in the editor
    regionGuiBean.getParameterList().getParameter(SitesInGriddedRegionGuiBean.MIN_LATITUDE).setValue(minLatVector.get(index));
    regionGuiBean.getParameterList().getParameter(SitesInGriddedRegionGuiBean.MAX_LATITUDE).setValue(maxLatVector.get(index));
    regionGuiBean.getParameterList().getParameter(SitesInGriddedRegionGuiBean.MIN_LONGITUDE).setValue(minLonVector.get(index));
    regionGuiBean.getParameterList().getParameter(SitesInGriddedRegionGuiBean.MAX_LONGITUDE).setValue(maxLonVector.get(index));
    regionGuiBean.refreshParamEditor();
  }


}
