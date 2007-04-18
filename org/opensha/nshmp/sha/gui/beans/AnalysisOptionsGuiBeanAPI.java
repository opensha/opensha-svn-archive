package org.opensha.nshmp.sha.gui.beans;

import javax.swing.JPanel;

/**
 * <p>Title: AnalysisOptionsGuiBeanAPI</p>
 *
 * <p>Description: This interface is implemented by all the Analysis Option
 * Gui Beans. For eg: Gui Bean(NEHRP_GuiBean) for the NEHRP analysis option selection
 * implements this GUI bean. This has been done becuase application can show the
 * GUIbean in the main application without knowing which GUI bean it is actually
 * calling. As all GUI beans for analysis option implements this interface, so
 * application just create instance the instance of this interface and contact
 * the GUI beans using this interface</p>
 *
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public interface AnalysisOptionsGuiBeanAPI {

  /**
   * Gets the panel for the Gui Bean for the selected analysis option in the
   * application.
   */
  public JPanel getGuiBean();

  /**
   * Clears the Data window
   */
  public void clearData();

  /**
   *
   * @return String
   */
  public String getData();

  /**
   * Returns the selected Region
   * @return String
   */
  public String getSelectedRegion();

  /**
   * Returns the selected data edition
   * @return String
   */
  public String getSelectedDataEdition();


}
