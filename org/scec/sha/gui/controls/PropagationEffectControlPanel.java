package org.scec.sha.gui.controls;

import java.awt.*;
import javax.swing.JFrame;

import org.scec.param.ParameterList;
import org.scec.param.editor.ParameterListEditor;
import org.scec.sha.gui.controls.PropagationEffectControlPanelAPI;

/**
 * <p>Title: PropagationEffectControlPanel</p>
 * <p>Description: This class takes the adjustable parameters from the calculators
 * like ScenarioshakeMapCalc and HazardMapCalc and show it in the control panel. </p>
 * @author : Ned Field, Nitin Gupta and Vipin  Gupta
 * @created : June 16, 2004
 * @version 1.0
 */

public class PropagationEffectControlPanel extends JFrame {

  //declaring the instance of the parameterlist and editor.
  private ParameterList paramList;
  private ParameterListEditor editor;
  private BorderLayout borderLayout1 = new BorderLayout();
  //instance of the class implementing PropagationEffectControlPanelAPI interface.
  private PropagationEffectControlPanelAPI application;

  /**
   *
   * @param api : Instance of the class using this control panel and implmenting
   * the PropagationEffectControlPanelAPI.
   */
  public PropagationEffectControlPanel(Component parentComponent,PropagationEffectControlPanelAPI api) {
    application = api;
    paramList = api.getCalcAdjustableParams();
    editor = new ParameterListEditor(paramList);
    try {
      // show the window at center of the parent component
      setLocation(parentComponent.getX()+parentComponent.getWidth()/2,
                     parentComponent.getY()+parentComponent.getHeight()/2);
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    this.setSize(300,350);
    this.setTitle("Calculation Settings");
    this.getContentPane().setLayout(new GridBagLayout());
    this.getContentPane().add(editor,new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
  }


}