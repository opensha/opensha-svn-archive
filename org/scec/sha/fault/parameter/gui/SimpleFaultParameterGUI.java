package org.scec.sha.fault.parameter.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.scec.param.ParameterAPI;
import org.scec.sha.fault.parameter.*;
import org.scec.sha.fault.parameter.gui.*;

/**
 * <p>Title: SimpleFaultParameterGUI</p>
 * <p>Description: Creates a GUI interface to the EvelyGriddedSurface Parameter</p>
 * @author : Edward Field, Nitin Gupta and Vipin Gupta
 * @created : Aug 3,2003
 * @version 1.0
 */

public class SimpleFaultParameterGUI extends JFrame{
  private JPanel evenlyGriddedSurfacePanel = new JPanel();
  private JScrollPane evenlyGriddedParamsScroll = new JScrollPane();
  private JButton button = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel parameterPanel = new JPanel();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();

  // Object for the SimpleFaultParameter Editor
  SimpleFaultParameterEditor gridSurface;

  public SimpleFaultParameterGUI(SimpleFaultParameter surfaceParam) {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    //creating the object for the SimpleFaultParameter Editor
    gridSurface = new SimpleFaultParameterEditor();
    gridSurface.setParameter(surfaceParam);
    gridSurface.setUpdateButtonVisible(false);
    parameterPanel.add(gridSurface,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
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
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.getContentPane().add(evenlyGriddedSurfacePanel, BorderLayout.CENTER);
    evenlyGriddedSurfacePanel.add(evenlyGriddedParamsScroll,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 200, 10));
    evenlyGriddedSurfacePanel.add(button,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
    button.setText("Update Surface Parameter");
    button.setForeground(new Color(80,80,133));
    button.setBackground(new Color(200,200,230));
    button.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(ActionEvent e) {
       button_actionPerformed(e);
     }
    });
    evenlyGriddedParamsScroll.getViewport().add(parameterPanel, null);

    this.setTitle("Simple Fault Parameter Settings");
  }

  void button_actionPerformed(ActionEvent e) {
    gridSurface.setEvenlyGriddedSurfaceFromParams();
    this.dispose();
  }
}