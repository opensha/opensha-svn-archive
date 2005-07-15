package org.opensha.refFaultParamDb.gui;

import javax.swing.*;
import org.opensha.param.editor.ParameterListEditor;
import org.opensha.param.*;
import java.util.ArrayList;
import java.awt.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class PaleoSiteApp extends JFrame {
  private final static String SITE_PARAM_NAME = "Site Name";
  private final static String LOCATION_PARAM_NAME = "Site Location";
  private StringParameter siteNameParam;
  private LocationParameter locationParameter;
  private ParameterListEditor editor;
  private ParameterList paramList;
  private JPanel mainPanel = new JPanel();
  private JLabel headerLabel = new JLabel();
  private GridBagLayout gridBagLayout1 = new GridBagLayout();
  private BorderLayout borderLayout1 = new BorderLayout();


  public PaleoSiteApp() {
    initParamListAndEditor();
    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * Initialize the parameters for the GUI
   */
  private void initParamListAndEditor() {
    paramList = new ParameterList();
    siteNameParam = new StringParameter(SITE_PARAM_NAME,getSiteNames());
    paramList.addParameter(siteNameParam);
    locationParameter = new LocationParameter(LOCATION_PARAM_NAME);
    paramList.addParameter(locationParameter);
    editor = new ParameterListEditor(paramList);
  }

  /**
   * this is JUST A FAKE IMPLEMENTATION. IT SHOULD GET ALL SITE NAMES FROM
   * the DATABASE
   * @return
   */
  private ArrayList getSiteNames() {
     ArrayList list = new ArrayList();
     list.add("Site 1");
     list.add("Site 2");
     return list;
  }

  public static void main(String[] args) {
    PaleoSiteApp paleoSiteApp = new PaleoSiteApp();
    paleoSiteApp.show();
  }

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(borderLayout1);
    mainPanel.setLayout(gridBagLayout1);
    headerLabel.setFont(new java.awt.Font("Dialog", 0, 20));
    headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
    headerLabel.setText("Paleo Site");
    this.getContentPane().add(mainPanel, BorderLayout.CENTER);
    mainPanel.add(headerLabel,  new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5 ), 0, 0));
    mainPanel.add(editor,  new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
        ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    }
}