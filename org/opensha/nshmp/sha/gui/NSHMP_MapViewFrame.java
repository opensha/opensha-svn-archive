package org.opensha.nshmp.sha.gui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * <p>Title: NSHMP_MapViewFrame</p>
 *
 * <p>Description: This class shows the listing of the maps that user can view and
 * print.</p>
 * @author Ned Field, Nitin Gupta and E.V.Leyendecker
 * @version 1.0
 */
public class NSHMP_MapViewFrame
    extends JFrame {

  JSplitPane mapButtonsSplitPane = new JSplitPane();
  JPanel mapListPanel = new JPanel();
  JList mapList = new JList();
  JPanel buttonPanel = new JPanel();
  JButton viewMapButton = new JButton();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  BorderLayout borderLayout1 = new BorderLayout();
  JScrollPane dataScrollPane = new JScrollPane();

  //Pdf map files
  private String[] mapFiles;

  /**
   * Constructor for the PDF Map Viewing Window.
   * It shows the list of maps for the user to choose from, then launches the Acrobat
   * Reader to view the selected map.
   * @param availableMapsList String[] : Info about the Maps, user selects one of the provided
   * choice and clicks on the "View Map" button. For the selected choice it then launches
   * the Acrobat PDF Viewer to view the map.
   * @param mapFiles String[] : URLs to the PDF map files corresponding to the
   * available map list.
   */
  public NSHMP_MapViewFrame(String[] availableMapsList, String[] mapFiles) {
    try {
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
    createListofAvailableMaps(availableMapsList,mapFiles);
  }

  private void jbInit() throws Exception {
    getContentPane().setLayout(borderLayout1);
    mapButtonsSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    mapListPanel.setLayout(gridBagLayout2);
    mapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    buttonPanel.setLayout(gridBagLayout1);
    viewMapButton.setText("View Map");
    buttonPanel.setMinimumSize(new Dimension(1, 1));
    mapButtonsSplitPane.add(buttonPanel, JSplitPane.BOTTOM);
    mapButtonsSplitPane.add(mapListPanel, JSplitPane.TOP);
    buttonPanel.add(viewMapButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(12, 250, 20, 254), 23, 0));
    mapListPanel.add(dataScrollPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(2, 3, 6, 4), 607, 410));
    dataScrollPane.getViewport().add(mapList, null);
    this.getContentPane().add(mapButtonsSplitPane, java.awt.BorderLayout.CENTER);
    mapButtonsSplitPane.setDividerLocation(420);
    viewMapButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        viewMapButton_actionPerformed(actionEvent);
      }
    });

    this.setTitle("PGA and SA Maps");
  }


  /*
   *
   * @param actionEvent ActionEvent
   */
  private void viewMapButton_actionPerformed(ActionEvent actionEvent) {

    int selectedIndex = mapList.getSelectedIndex();
    String fileToRead = mapFiles[selectedIndex];
    try{
       org.opensha.util.BrowserLauncher.openURL(fileToRead);
      }catch(Exception ex) { ex.printStackTrace(); }
  }

  /*
   * Create the List to be shown to the user for the available pdf maps
   * @param availableMapsList String[]
   * @param mapFiles String[]
   */
  private void createListofAvailableMaps(String[] availableMapsList,
                                         String[] mapFiles
      ) {
    this.mapFiles = mapFiles;
    mapList.setListData(availableMapsList);
    mapList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    mapList.setSelectedIndex(0);
  }
}

