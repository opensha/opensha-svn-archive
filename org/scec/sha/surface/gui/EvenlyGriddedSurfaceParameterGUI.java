package org.scec.sha.surface.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import org.scec.param.ParameterAPI;
import org.scec.sha.surface.parameter.*;
import org.scec.sha.surface.gui.*;

/**
 * <p>Title: EvenlyGriddedSurfaceParameterGUI</p>
 * <p>Description: Creates a GUI interface to the EvelyGriddedSurface Parameter</p>
 * @author : Edward Field, Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class EvenlyGriddedSurfaceParameterGUI extends JFrame{
  private JPanel evenlyGriddedSurfacePanel = new JPanel();
  private JScrollPane evenlyGriddedParamsScroll = new JScrollPane();
  private JButton button = new JButton();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel parameterPanel = new JPanel();
  private GridBagLayout gridBagLayout2 = new GridBagLayout();

  // Object for the EvenlyGriddedSurfaceParameter Editor
  EvenlyGriddedSurfaceParameterEditor gridSurface;

  public EvenlyGriddedSurfaceParameterGUI(EvenlyGriddedSurfaceParameter surfaceParam) {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }

    //creating the object for the EvenlyGriddedSurfaceParameter Editor
    gridSurface = new EvenlyGriddedSurfaceParameterEditor();
    gridSurface.setParameter(surfaceParam);
    gridSurface.setUpdateButtonVisible(false);
    parameterPanel.add(gridSurface,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 0, 0));
  }

  public static void main(String[] args) {
    EvenlyGriddedSurfaceParameter surfaceParam = new EvenlyGriddedSurfaceParameter("Fault-1",null);
    EvenlyGriddedSurfaceParameterGUI evenlyGriddedSurfaceParameterGUI = new EvenlyGriddedSurfaceParameterGUI(surfaceParam);
    evenlyGriddedSurfaceParameterGUI.show();
    evenlyGriddedSurfaceParameterGUI.pack();
  }

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    evenlyGriddedSurfacePanel.setLayout(gridBagLayout1);
    parameterPanel.setLayout(gridBagLayout2);
    this.getContentPane().add(evenlyGriddedSurfacePanel, BorderLayout.CENTER);
    evenlyGriddedSurfacePanel.add(evenlyGriddedParamsScroll,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 4, 4), 200, 10));
    evenlyGriddedSurfacePanel.add(button,  new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
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

    this.setTitle("EvenlyGriddedSurface Parameter");
  }

  void button_actionPerformed(ActionEvent e) {
    gridSurface.setEvenlyGriddedSurfaceFromParams();
    this.dispose();
  }

}