package org.opensha.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import javax.swing.border.*;

import org.opensha.sha.calc.DisaggregationCalculator;

/**
 * <p>Title: HazardCurveDisaggregationWindow</p>
 * <p>Description: This class shows the Disaggregation Result in a seperate window</p>
 * @author : Nitin Gupta & Vipin Gupta
 * Date :Feb,19,2003
 * @version 1.0
 */

public class HazardCurveDisaggregationWindow extends JFrame {
  private JPanel jMessagePanel = new JPanel();
  private JButton jMessageButton = new JButton();
  private String infoMessage;
  private JTextPane jMessagePane = new JTextPane();
  private SimpleAttributeSet setMessage;
  Border border1;
  JButton sourceListButton = new JButton();
  JButton plotButton = new JButton();
  private DisplayDataWindow dataWindow;

  private HazardCurveDisaggregationWindowAPI application;
  BorderLayout borderLayout1 = new BorderLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();

  public HazardCurveDisaggregationWindow(HazardCurveDisaggregationWindowAPI app,
                                         Component parent, String s) {

    this.infoMessage=s;
    application = app;

    try {
      jbInit();
      // show the window at center of the parent component
      this.setLocation(parent.getX()+parent.getWidth()/2,
                       parent.getY()+parent.getHeight()/2);

      jMessagePane.setEditable(false);
      Document doc = jMessagePane.getStyledDocument();
      doc.remove(0,doc.getLength());
      setMessage =new SimpleAttributeSet();
      StyleConstants.setFontSize(setMessage,13);
      StyleConstants.setForeground(setMessage,Color.black);
      doc.insertString(doc.getLength(),infoMessage,setMessage);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void jbInit() throws Exception {
    border1 = BorderFactory.createLineBorder(SystemColor.controlText,1);
    jMessagePanel.setLayout(gridBagLayout1);
    this.setTitle("Disaggregation Result Window");
    this.getContentPane().setLayout(borderLayout1);
    jMessageButton.setBackground(new Color(200, 200, 230));
    jMessageButton.setForeground(new Color(80, 80, 133));
    jMessageButton.setText("OK");
    jMessageButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jMessageButton_actionPerformed(e);
      }
    });
    jMessagePanel.setBackground(new Color(200, 200, 230));
    jMessagePanel.setForeground(new Color(80, 18, 133));
    jMessagePanel.setBorder(border1);
    this.setResizable(true);
    this.getContentPane().setBackground(new Color(200, 200, 230));
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    jMessagePane.setBorder(null);
    jMessagePane.setToolTipText("");
    jMessagePane.setEditable(false);
    sourceListButton.setBackground(new Color(200, 200, 230));
    sourceListButton.setForeground(new Color(80, 80, 133));
    sourceListButton.setText("Source List");
    sourceListButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        sourceListButton_actionPerformed(actionEvent);
      }
    });
    plotButton.setBackground(new Color(200, 200, 230));
    plotButton.setForeground(new Color(80, 80, 133));
    plotButton.setText("Disagg. Plot");
    plotButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionEvent) {
        plotButton_actionPerformed(actionEvent);
      }
    });
    this.getContentPane().add(jMessagePanel, java.awt.BorderLayout.CENTER);
    jMessagePanel.add(jMessageButton,
                      new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                                             , GridBagConstraints.CENTER,
                                             GridBagConstraints.NONE,
                                             new Insets(4, 4, 4, 4), 35, 0));
    jMessagePanel.add(sourceListButton,
                      new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
                                             , GridBagConstraints.CENTER,
                                             GridBagConstraints.NONE,
                                             new Insets(4, 4, 4, 4), 13, 0));
    jMessagePanel.add(plotButton, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0
        , GridBagConstraints.CENTER, GridBagConstraints.NONE,
        new Insets(4, 4, 4, 4), 6, 0));
    jMessagePanel.add(jMessagePane, new GridBagConstraints(0, 0, 3, 1, 1.0, 1.0
        , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
        new Insets(0, 0, 0, 0), 0, 0));
  }

  void jMessageButton_actionPerformed(ActionEvent e) {
   this.dispose();
  }

  public void sourceListButton_actionPerformed(ActionEvent actionEvent) {
    String sourceDisaggregationList = application.getSourceDisaggregationInfo();
    String title = "Source Disaggregation Result";
    if(dataWindow == null)
      dataWindow = new DisplayDataWindow(this,sourceDisaggregationList,title);
    else
      dataWindow.setDataInWindow(sourceDisaggregationList);
    dataWindow.show();
  }

  public void plotButton_actionPerformed(ActionEvent actionEvent) {
    String disaggregationPlotWebAddr = null;
    String metadata = null;
    try {
      disaggregationPlotWebAddr = application.getDisaggregationPlot();
      metadata = application.getMapParametersInfoAsHTML();
      metadata += "<br><p>Click:  " + "<a href=\"" + disaggregationPlotWebAddr +
           "\">" + "here" + "</a>" +" to download files. They will be deleted at midnight</p>";;
    }
    catch (RuntimeException e) {
      e.printStackTrace();
      JOptionPane.showMessageDialog(this, e.getMessage(), "Server Problem",
                                    JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    String imgName = disaggregationPlotWebAddr +
        DisaggregationCalculator.DISAGGREGATION_PLOT_IMG;
    //adding the image to the Panel and returning that to the applet
    ImageViewerWindow imgView = new ImageViewerWindow(imgName, metadata,true);
  }
}
