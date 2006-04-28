package org.opensha.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.opensha.util.ImageUtils;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;

/**
 * <p>Title: ApplicationVersionInfoWindow</p>
 *
 * <p>Description: This class display link to the new version of the application,
 * if the version of application that user is running out of date.
 * </p>
 * @author Ned Field, Nitin Gupta
 * @version 1.0
 */
public class ApplicationVersionInfoWindow
    extends JDialog {

  private final static String message = "A new version of application exists on website,"+
      "Do you want to quit the current application and download the new version ?";
  JPanel msgPanel = new JPanel();
  JLabel imgLabel ;
  JEditorPane msgPane = new JEditorPane();
  Border border1 = BorderFactory.createMatteBorder(6, 6, 6, 6, Color.white);
  Border border2 = BorderFactory.createBevelBorder(BevelBorder.RAISED,
      Color.white, Color.white, new Color(124, 124, 124),
      new Color(178, 178, 178));
  JScrollPane versionUpdateText = new JScrollPane();
  TitledBorder titledBorder1 = new TitledBorder("");
  Border border3 = BorderFactory.createLineBorder(Color.lightGray, 2);
  Border border4 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED,
      Color.white, new Color(178, 178, 178));
  Border border5 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED,
      Color.white, new Color(178, 178, 178));
  JEditorPane updateVersionInfo = new JEditorPane();
  JButton yesButton = new JButton();
  JButton noButton = new JButton();
  BorderLayout borderLayout1 = new BorderLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  private String title = "Application version";
  //URL string to the application
  private String urlToApp;
  //URL to the page that has info for the version update of the application
  private String urlToVersionUpdatePage;

  /**
   *
   * @param appURL String : String URL to the application
   * @param updatePageURL String URL to the page that has info for the version update of the application
   * @param title String Message Title
   * @param parent Component Application from which this window is launched
   */
  public ApplicationVersionInfoWindow(String appURL, String updatePageURL,String title,Component parent) {
    urlToApp = appURL;
    urlToVersionUpdatePage = updatePageURL;

    try {
      jbInit();
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
    this.title = title;
    this.setTitle(title);
    // show the window at center of the parent component
    setLocation(parent.getX() + parent.getWidth() / 2,
                     parent.getY() + parent.getHeight() / 2);
  }


  private void jbInit() throws Exception {
    this.setModal(true);
    this.getContentPane().setLayout(borderLayout2);
    msgPanel.setLayout(gridBagLayout2);
    msgPane.setBackground(SystemColor.window);
    imgLabel = new JLabel(new ImageIcon(ImageUtils.loadImage("info_icon.jpg")));
    msgPane.setBorder(border2);
    versionUpdateText.getViewport().setBackground(SystemColor.desktop);
    versionUpdateText.setBorder(border5);
    yesButton.setText("Yes");
    yesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        yesButton_actionPerformed(actionEvent);
      }
    });

    noButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        noButton_actionPerformed(actionEvent);
      }
    });

    noButton.setText("No");
    versionUpdateText.getViewport().add(updateVersionInfo);
    msgPanel.add(imgLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                  , GridBagConstraints.WEST,
                                                  GridBagConstraints.NONE,
                                                  new Insets(2, 2, 2, 2), 0,
                                                  0));
    msgPanel.add(msgPane, new GridBagConstraints(1, 0, 2, 1, 1.0, 1.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(4, 4,4, 4), 0, 0));
    msgPanel.add(noButton, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(0, 6, 12, 38), 26, 0));
    msgPanel.add(yesButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(0, 91, 12, 0), 26, 0));
    msgPanel.add(versionUpdateText, new GridBagConstraints(0, 1, 3, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(8, 6, 0, 3), 0, 260));
    this.getContentPane().add(msgPanel, java.awt.BorderLayout.CENTER);
    msgPane.setText(this.message);
    updateVersionInfo.setContentType("text/html");
    updateVersionInfo.setPage(urlToVersionUpdatePage);
    this.setSize(450,200);
    this.pack();
  }


  public void noButton_actionPerformed(ActionEvent actionEvent) {
    this.dispose();
  }

  public void yesButton_actionPerformed(ActionEvent actionEvent) {
    try {
      org.opensha.util.BrowserLauncher.openURL(urlToApp);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    System.exit(0);

  }
}
