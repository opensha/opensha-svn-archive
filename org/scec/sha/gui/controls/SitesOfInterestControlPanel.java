package org.scec.sha.gui.controls;

import java.awt.*;
import javax.swing.*;
import java.util.Vector;

import org.scec.sha.gui.beans.Site_GuiBean;
import java.awt.event.*;
/**
 * <p>Title: SitesOfInterest </p>
 * <p>Description: It displays a list of interesting sites which user can choose </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class SitesOfInterestControlPanel extends JFrame {
  private JLabel jLabel1 = new JLabel();
  private JComboBox sitesComboBox = new JComboBox();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private Vector latVector;
  private Vector lonVector;
  private Site_GuiBean siteGuiBean;

  /**
   * Constructor
   *
   * @param parent : parent component which calls this control panel
   * @param siteGuiBean : site gui bean to set the lat and lon
   */
  public SitesOfInterestControlPanel(Component parent, Site_GuiBean siteGuiBean) {
    try {
      latVector = new Vector();
      lonVector = new Vector();
      this.siteGuiBean = siteGuiBean;


      /*
      * add interesting sites
      */

      // los angeles
      this.sitesComboBox.addItem("Los Angeles");
      latVector.add(new Double(34.053));
      lonVector.add(new Double(-118.243));

      // san francisco
      sitesComboBox.addItem("San Francisco");
      latVector.add(new Double(37.75));
      lonVector.add(new Double(-122.68));




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
    jLabel1.setText("Choose Site:");
    this.getContentPane().setLayout(gridBagLayout1);
    this.setTitle("Sites Of Interest");
    sitesComboBox.setBackground(new Color(200, 200, 230));
    sitesComboBox.setForeground(new Color(80, 80, 133));
    sitesComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        sitesComboBox_actionPerformed(e);
      }
    });
    this.getContentPane().add(sitesComboBox,  new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(14, 6, 10, 12), 149, 2));
    this.getContentPane().add(jLabel1,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(14, 5, 10, 0), 13, 11));
  }

  /**
   * whenever user selects an interesting site, this function is called
   * @param e
   */
  void sitesComboBox_actionPerformed(ActionEvent e) {
    setLatAndLon();
  }

  /**
   * to set lat and lon according to user selection
   */
  private void setLatAndLon() {
    int index = this.sitesComboBox.getSelectedIndex();
    // set the lat and lon in the editor
    siteGuiBean.getParameterList().getParameter(Site_GuiBean.LATITUDE).setValue(latVector.get(index));
    siteGuiBean.getParameterList().getParameter(Site_GuiBean.LONGITUDE).setValue(lonVector.get(index));
    siteGuiBean.synchToModel();
  }
}
