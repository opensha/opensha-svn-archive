package org.scec.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;

import java.util.*;
import java.awt.event.*;
import org.scec.param.StringParameter;
import org.scec.param.editor.ConstrainedStringParameterEditor;
import org.scec.data.Location;
import org.scec.sha.earthquake.EqkRupture;
/**
 * <p>Title: HypocenterLocationWindow</p>
 * <p>Description: This class shows the Hypocenter locations for user to select from in a seperate
 * pop up window.</p>
 * @author :Nitin Gupta
 * @version 1.0
 */

public class HypocenterLocationWindow extends JDialog {
  private JPanel jPanel1 = new JPanel();

  private JLabel jLabel1 = new JLabel();
  private JButton doneButton = new JButton();
  private JButton cancelButton = new JButton();
  private ArrayList hypoLocList;

  //Eqk Rupture in which Hypocenter needs to be set.
  private EqkRupture rupture;

  //Parameter to set the Hypocenter Location
  public final static String HYPOCENTER_LOCATION_PARAM_NAME = "Hypocenter Location";
  private final static String HYPOCENTER_LOCATION_PARAM_INFO = "Sets the Hypocenter Location";
  private StringParameter hypocenterLocationParam;
  private ConstrainedStringParameterEditor hypocenterLocationParamEditor;
  private BorderLayout borderLayout1 = new BorderLayout();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  //checks if Hypocenter has been set in the eqkRupture object.
  private boolean isHypocenterLocationInEqkRuptureSet= false;
  /**
   * @param parent: Instance of the application using this window
   * @param hypocenterLocationList: Hypocenter Locations.
   * From the above location user can choose to select the Hypocenter location.
   * @param eqkRup : Earthquake Rupture in which Hypocenter location needs to be set
   */
  public HypocenterLocationWindow(Component parent,ArrayList hypocenterLocationList,EqkRupture eqkRup ) {
    setModal(true);
    hypoLocList = hypocenterLocationList;
    rupture = eqkRup;
    hypocenterLocationParam = new StringParameter(HYPOCENTER_LOCATION_PARAM_NAME,hypoLocList,
        (String)hypoLocList.get(0));
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    // show the window at center of the parent component
    this.setLocation(parent.getX()+parent.getWidth()/2,
                       parent.getY()+parent.getHeight()/2);
  }
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    jPanel1.setLayout(gridBagLayout1);
    jLabel1.setFont(new java.awt.Font("Arial", 1, 16));
    jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
    jLabel1.setHorizontalTextPosition(SwingConstants.CENTER);
    jLabel1.setText("Set Hypocenter Location");
    doneButton.setText("Done");
    doneButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        doneButton_actionPerformed(e);
      }
    });
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelButton_actionPerformed(e);
      }
    });

    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);


    hypocenterLocationParamEditor = new ConstrainedStringParameterEditor(hypocenterLocationParam);

    jPanel1.add(hypocenterLocationParamEditor,  new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(16, 5, 0, 8), 237, 1));
    jPanel1.add(cancelButton,  new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(22, 24, 33, 26), 11, 18));
    jPanel1.add(doneButton,  new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(22, 157, 33, 0), 14, 18));
    jPanel1.add(jLabel1,  new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 38, 0, 67), 120, 23));
    this.getContentPane().add(jPanel1, BorderLayout.CENTER);
    this.setSize(410,230);
  }


  /**
   *
   * @returns the Hypocenter Location if selected else return null
   */
  public Location getHypocenterLocation(){
    StringTokenizer token = new StringTokenizer(hypocenterLocationParam.getValue().toString());
    double lat= Double.parseDouble(token.nextElement().toString().trim());
    double lon= Double.parseDouble(token.nextElement().toString().trim());
    double depth= Double.parseDouble(token.nextElement().toString().trim());
    Location loc= new Location(lat,lon,depth);
    return loc;
  }

  /**
   * Sets the selected Hypocenter location in the Earthquake rupture
   * @param e
   */
  void doneButton_actionPerformed(ActionEvent e) {
    rupture.setHypocenterLocation(getHypocenterLocation());
    isHypocenterLocationInEqkRuptureSet = true;
    this.dispose();
  }

  /**
   *
   * @returns true if hypocenter location has been set in th EqkRupture Object.
   */
  public boolean isHypocenterLocationSetInEqkRupture(){
    return isHypocenterLocationInEqkRuptureSet;
  }

  /**
   *
   * @param hypocenterlocationFlaginEqkrupture : Set hypocenter location in EqkRupture to be false
   * if new EqkRupture is created.
   */
  public void setHypocenterLocationSetFlagInEqkRupture(boolean hypocenterlocationFlaginEqkrupture){
    isHypocenterLocationInEqkRuptureSet = hypocenterlocationFlaginEqkrupture;
  }

  /**
   * Just closes the window
   * @param e
   */
  void cancelButton_actionPerformed(ActionEvent e) {
    this.dispose();
  }

  /**
   * Shows user the list of Hypocenter locations(hypocenterLocs). Once he chooses
   * the location, and presses "Done" button that hypocenter location is set
   * in eqkRupture.
   * @param hypocenterLocs
   * @param eqkRupture
   */
  public void setHypocenterLocationListAndEqkRupture(ArrayList hypocenterLocs,EqkRupture eqkRupture){
    hypocenterLocationParam = new StringParameter(HYPOCENTER_LOCATION_PARAM_NAME,hypoLocList,
        (String)hypoLocList.get(0));
    hypocenterLocationParamEditor.setParameter(hypocenterLocationParam);
    hypocenterLocationParamEditor.refreshParamEditor();

    rupture = eqkRupture;
  }
}