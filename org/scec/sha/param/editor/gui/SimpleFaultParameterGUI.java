package org.scec.sha.param.editor.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.scec.param.ParameterAPI;
import org.scec.sha.param.*;
import org.scec.sha.param.editor.*;
import org.scec.sha.fault.FaultTrace;

/**
 * <p>Title: SimpleFaultParameterGUI</p>
 * <p>Description: Creates a GUI interface to the EvelyGriddedSurface Parameter.
 * This GUI acts as the intermediatary between the SimpleFaultEditor which is just a
 * simple button and the SimpleFaultEditorPanel which shows the actual values for the parameters.
 * So SimpleFaultEditor gets the access to the values of the parameters using this GUI.</p>
 * @author : Edward Field, Nitin Gupta and Vipin Gupta
 * @created : Aug 3,2003
 * @version 1.0
 */

public class SimpleFaultParameterGUI extends JDialog{
  private JPanel evenlyGriddedSurfacePanel = new JPanel();
  private JScrollPane evenlyGriddedParamsScroll = new JScrollPane();
  private JButton button = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel parameterPanel = new JPanel();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();



  //Object for the SimpleFaultParameterEditorPanel
  SimpleFaultParameterEditorPanel faultEditorPanel;

  //Constructor that takes the Object for the SimpleFaultParameter
  public SimpleFaultParameterGUI(ParameterAPI surfaceParam) {
    this.setModal(true);
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    //creating the object for the SimpleFaultParameter Editor
    faultEditorPanel = new SimpleFaultParameterEditorPanel();
    faultEditorPanel.setParameter(surfaceParam);
    parameterPanel.add(faultEditorPanel,  new GridBagConstraints(0, 0, 0, 1, 1.0, 1.0
            ,GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
  }

  public static void main(String[] args) {
    SimpleFaultParameter surfaceParam = new SimpleFaultParameter("Fault-1",null);
    SimpleFaultParameterGUI simpleFaultParameterGUI = new SimpleFaultParameterGUI(surfaceParam);
    simpleFaultParameterGUI.show();
    simpleFaultParameterGUI.pack();
  }

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    evenlyGriddedSurfacePanel.setLayout(gridBagLayout1);
    parameterPanel.setLayout(gridBagLayout2);
    evenlyGriddedSurfacePanel.setPreferredSize(new Dimension(370, 450));
    evenlyGriddedParamsScroll.setPreferredSize(new Dimension(370, 450));
    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    this.getContentPane().add(evenlyGriddedSurfacePanel, BorderLayout.CENTER);
    evenlyGriddedSurfacePanel.add(evenlyGriddedParamsScroll,   new GridBagConstraints(0, 0, 0, 1, 1.0, 1.0
            ,GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
    evenlyGriddedSurfacePanel.add(button,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
    button.setText("Make Simple Fault");
    button.setForeground(new Color(80,80,133));
    button.setBackground(new Color(200,200,230));
    button.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(ActionEvent e) {
       button_actionPerformed(e);
     }
    });
    evenlyGriddedParamsScroll.getViewport().add(parameterPanel, null);
    this.setSize(300,450);
    this.setTitle("Simple Fault Parameter Settings");
  }

  void button_actionPerformed(ActionEvent e) {
    boolean disposeFlag = true;
    try{
      faultEditorPanel.setEvenlyGriddedSurfaceFromParams();
    }catch(RuntimeException ee){
      disposeFlag = false;
      ee.printStackTrace();
      JOptionPane.showMessageDialog(this,ee.getMessage(),"Incorrect Input Parameters",JOptionPane.OK_OPTION);
    }

    //donot close the application  if any exception has been thrown by the application
    if(disposeFlag)
      this.dispose();
  }

  /**
   * gets the fault trace for the griddedSurface
   * @return
   */
  public FaultTrace getFaultTrace(){
   return faultEditorPanel.getSimpleFaultParameter().getFaultTrace();
  }

  /**
   * gets the Upper Siesmogenic depth for the gridded surface
   * @return
   */
  public double getUpperSies(){
    return faultEditorPanel.getSimpleFaultParameter().getUpperSiesmogenicDepth();
  }

  /**
   * gets the lower Seismogenic depth for the gridded surface
   * @return
   */
  public double getLowerSies(){
    return faultEditorPanel.getSimpleFaultParameter().getLowerSiesmogenicDepth();
  }

  /**
   * gets the fault Name
   * @return
   */
  public String getFaultName(){
    return faultEditorPanel.getSimpleFaultParameter().getFaultName();
  }

  /**
   * Intermediate step to call the refreshParamEditor for the SimpleFaultEditorPanel
   * becuase the SimpleFaultEditorPanel is just a simple Editor that shows the
   * button in the window. But when you click the button only then the actual
   * parameter values comes up
   */
  public void refreshParamEditor(){
    faultEditorPanel.refreshParamEditor();
  }

  /**
   * Sets the Value of the SimpleFaultParameter
   * @param param
   */
  public void setParameter(ParameterAPI param){
    faultEditorPanel.setParameter(param);
  }

  /**
   *
   * @returns the Object for the SimpleFaultEditorPanel which actually contains the
   * values of the parameter for the SimpleFaultParameter
   */
  public SimpleFaultParameterEditorPanel getSimpleFaultEditorPanel(){
    return this.faultEditorPanel;
  }

}