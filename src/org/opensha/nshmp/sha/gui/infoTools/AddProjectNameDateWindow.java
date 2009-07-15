package org.opensha.nshmp.sha.gui.infoTools;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
//import java.awt.event.ItemListener;
//import java.awt.event.ItemEvent;
import java.util.GregorianCalendar;

/**
 * <p>Title: AddProjectNameDateWindow</p>
 *
 * <p>Description: This class allows the user to add the project name and date
 * to added to the metadata being shown to the user.</p>
 *
 * @author Ned Field, Nitin Gupta and E.V. Leyendecker
 * @version 1.0
 */
public class AddProjectNameDateWindow
    extends JFrame {

  JPanel panel = new JPanel();
  JLabel nameLabel = new JLabel();
  JTextField dataName = new JTextField();
  JCheckBox dateCheckBox = new JCheckBox();
  BorderLayout borderLayout1 = new BorderLayout();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JButton okButton = new JButton();

  public AddProjectNameDateWindow() {
    try {
      jbInit();
    }
    catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  static {
    try {
	   //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		 UIManager.setLookAndFeel("apple.laf/AquaLookAndFeel");
	 } catch (Exception e) {	
	 }
  }
	 	
  private void jbInit() throws Exception {
    getContentPane().setLayout(borderLayout1);
    panel.setLayout(gridBagLayout1);
    dateCheckBox.setText("Add Date");
	 okButton.setText("Okay");
	 okButton.addActionListener(new ActionListener() {
	 	public void  actionPerformed(ActionEvent e) {
			okButton_actionPerformed(e);
		}
	 });

    panel.add(dataName, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                                               , GridBagConstraints.WEST,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(35, 6, 0, 33), 196, 3));
    panel.add(nameLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                                , GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(35, 23, 0, 0), 16, 8));
    panel.add(dateCheckBox, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
        , GridBagConstraints.CENTER,
        GridBagConstraints.NONE,
        new Insets(21, 23, 17, 158),
        57, 5));

	 panel.add(okButton, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
	 	GridBagConstraints.CENTER, GridBagConstraints.NONE,
		new Insets(21, 200, 17, 23), 0, 0));

    getContentPane().add(panel, java.awt.BorderLayout.CENTER);
    nameLabel.setText("Name:");

    this.setName("Add Name and Date to calculated data");
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    setSize(300,120);

	 this.getRootPane().setDefaultButton(okButton);

    this.setLocation( (d.width - this.getSize().width) / 2,
                     (d.height - this.getSize().height) / 3);
  }


  private void okButton_actionPerformed(ActionEvent e) {
  	this.setVisible(false);
  }

  public String getProjectName(){
    return dataName.getText();
  }


  public String getDate(){
    if(dateCheckBox.isSelected()){
      GregorianCalendar calender = new GregorianCalendar();
      return calender.getTime().toString();
    }
    return null;
  }

}
