package org.scec.sha.surface.gui;

import java.awt.*;
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

public class EvenlyGriddedSurfaceParameterGUI extends JFrame {
  private JPanel evenlyGriddedSurfacePanel = new JPanel();
  private JScrollPane evenlyGriddedParamsScroll = new JScrollPane();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();

  public EvenlyGriddedSurfaceParameterGUI(EvenlyGriddedSurfaceParameter surfaceParam) {
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    EvenlyGriddedSurfaceParameterEditor gridSurface = new EvenlyGriddedSurfaceParameterEditor();
    gridSurface.setParameter(surfaceParam);
    evenlyGriddedParamsScroll.getViewport().add(gridSurface);
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
    this.getContentPane().add(evenlyGriddedSurfacePanel, BorderLayout.CENTER);
    evenlyGriddedSurfacePanel.add(evenlyGriddedParamsScroll,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 1, 5, 3), 426, 320));
    this.setSize(200,350);
    this.setTitle("EvenlyGriddedSurface Parameter");
  }
}