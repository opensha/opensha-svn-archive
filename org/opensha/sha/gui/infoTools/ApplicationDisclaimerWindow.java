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
import java.awt.Rectangle;

/**
 * <p>Title: ApplicationDisclaimerWindow</p>
 *
 * <p>Description: This class displays the disclaimer message for the application in a window
 * If user accepts it then he/she can proceed with the application, else application quits.
 * </p>
 * @author Ned Field, Nitin Gupta
 * @version 1.0
 */
public class ApplicationDisclaimerWindow
    extends JDialog {

  JPanel msgPanel = new JPanel();
  JTextArea msgPane = new JTextArea();
  Border border1 = BorderFactory.createMatteBorder(6, 6, 6, 6, Color.white);
  Border border2 = BorderFactory.createBevelBorder(BevelBorder.RAISED,
      Color.white, Color.white, new Color(124, 124, 124),
      new Color(178, 178, 178));
  JScrollPane disclaimerPane = new JScrollPane();
  TitledBorder titledBorder1 = new TitledBorder("");
  Border border3 = BorderFactory.createLineBorder(Color.lightGray, 2);
  Border border4 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED,
      Color.white, new Color(178, 178, 178));
  Border border5 = BorderFactory.createEtchedBorder(EtchedBorder.RAISED,
      Color.white, new Color(178, 178, 178));
  JEditorPane updateVersionInfo = new JEditorPane();
  JButton understandButton = new JButton();
  JButton quitButton = new JButton();
  BorderLayout borderLayout1 = new BorderLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  //URL string to the application
  private String urlToDisclaimerMsgPage;
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  BorderLayout borderLayout2 = new BorderLayout();

  /**
   *
   * @param appURL String : String URL to the application
   * @param updatePageURL String URL to the page that has info for the version update of the application
   * @param title String Message Title
   * @param parent Component Application from which this window is launched
   */
  public ApplicationDisclaimerWindow(String disclaimerMsgURL) {
	urlToDisclaimerMsgPage = disclaimerMsgURL;

    try {
      jbInit();
      setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
    this.setTitle("Disclaimer Message");
    // show the window at center of the parent component
    this.setVisible(true);
  }


  private void jbInit() throws Exception {
    this.setModal(true);
    this.getContentPane().setLayout(borderLayout2);
    msgPanel.setLayout(gridBagLayout2);
    msgPane.setBackground(SystemColor.window);
    msgPane.setBorder(border2);
    disclaimerPane.getViewport().setBackground(SystemColor.desktop);
    disclaimerPane.setBorder(border5);
    understandButton.setText("I Understand");
    understandButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        yesButton_actionPerformed(actionEvent);
      }
    });

    quitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        noButton_actionPerformed(actionEvent);
      }
    });
    quitButton.setText("Quit");
    
    disclaimerPane.getViewport().add(updateVersionInfo);

    msgPanel.add(disclaimerPane, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(4, 4, 0, 4), 0, 0));
    msgPanel.add(understandButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(0, 15, 7, 0), 26, 0));
    msgPanel.add(quitButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(0, 0, 7, 47), 59, 0));
    this.getContentPane().add(msgPanel, java.awt.BorderLayout.CENTER);

    updateVersionInfo.setContentType("text/html");
    updateVersionInfo.setPage(urlToDisclaimerMsgPage);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    this.setSize(200,100);
    setLocation( ( d.width - getSize().width ) / 2, ( d.height - getSize().height ) / 2 );
    this.pack();
  }


  public void noButton_actionPerformed(ActionEvent actionEvent) {
    System.exit(0);
  }

  public void yesButton_actionPerformed(ActionEvent actionEvent) {
	  this.dispose();
  }
}
