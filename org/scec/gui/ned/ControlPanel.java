package org.scec.gui.ned;

import java.awt.*;
import java.awt.event.*;


/**
 * <b>Title:</b> ControlPanel<br>
 * <b>Description:</b> <br>
 * <b>Copyright:</b>    Copyright (c) 2001<br>
 * <b>Company:</b>      <br>
 * @author Ned Field
 * @version 1.0
 */

class ControlPanel extends Panel  implements ActionListener, ItemListener {

	Field2000PlotApplet controller;
	TextField  	textFieldParam;		//  This is where the Dist or Mag is enterd
	TextField	textFieldDepth;
	Choice  	siteTypeChoice;
	Choice	   	faultTypeChoice;
	Choice	 	gmpChoice;		//ground motion parameter
	Choice		xAxisChoice;	// mag or dist
	Choice		sigmaChoice;	// the uncertainty bounds
	Button		addTraceButton, clearPlotButton;
	String		distString = new String("Distance (Rjb) in km");		// make this final!
	String		magString = new String("Magnitude");			// make this final!
	Label		paramLabel;

	public ControlPanel(Field2000PlotApplet controller) {

		this.controller = controller;
		setLayout(new GridLayout(8,2)); // 8 rows, 2 columns

      	add(new Label("  Ground Motion Param. (GMP):"));
		gmpChoice = new Choice();
      	gmpChoice.addItem("PGA");  					// This first one should be the intialized value in the parent
       	gmpChoice.addItem("0.3-sec SA");
      	gmpChoice.addItem("1.0-sec SA");
      	gmpChoice.addItem("3.0-sec SA");
      	gmpChoice.addItemListener(this);
      	add(gmpChoice);
gmpChoice.setForeground(Color.red);

   		add(new Label("  X axis (Magnitude or Distance):"));
		xAxisChoice = new Choice();
		xAxisChoice.addItem(distString);
      	xAxisChoice.addItem(magString);
      	xAxisChoice.addItemListener(this);
      	add(xAxisChoice);
xAxisChoice.setForeground(Color.red);

		add(new Label("  Basin Depth (km) ['null' if NA]"));
		textFieldDepth = new TextField();
		add(textFieldDepth);
		textFieldDepth.addActionListener(this);
 textFieldDepth.setForeground(Color.red);

		paramLabel = new Label("  "+magString);
		add(paramLabel);
		textFieldParam = new TextField();
		add(textFieldParam);
		textFieldParam.addActionListener(this);
 textFieldParam.setForeground(Color.red);


		add(new Label("  Site type:"));
		siteTypeChoice = new Choice();
      	siteTypeChoice.addItem("B");  // This first one should be the intialized value in the parent
       	siteTypeChoice.addItem("BC");
       	siteTypeChoice.addItem("C");
       	siteTypeChoice.addItem("CD");
       	siteTypeChoice.addItem("D");
       	siteTypeChoice.addItem("DE");
      	siteTypeChoice.addItemListener(this);
      	add(siteTypeChoice);

 siteTypeChoice.setForeground(Color.red);

		add(new Label("  Fault type:"));
		faultTypeChoice = new Choice();
       	faultTypeChoice.addItem("strike slip");
      	faultTypeChoice.addItem("reverse slip");
      	faultTypeChoice.addItem("unknown/other");
      	faultTypeChoice.addItemListener(this);
      	add(faultTypeChoice);

 faultTypeChoice.setForeground(Color.red);
// System.out.println(faultTypeChoice.getForeground());

      	add(new Label("  Uncertainties:"));
		sigmaChoice = new Choice();
      	sigmaChoice.addItem("none");  // This first one should be the intialized value in the parent
       	sigmaChoice.addItem("1 sigma");
      	sigmaChoice.addItem("2 sigma");
      	sigmaChoice.addItemListener(this);
      	add(sigmaChoice);
  sigmaChoice.setForeground(Color.red);

      	addTraceButton = new Button("Add Trace");
      	add(addTraceButton);
      	addTraceButton.addActionListener(this);

      	clearPlotButton = new Button("Clear Plot");
      	add(clearPlotButton);
      	clearPlotButton.addActionListener(this);
	}

	public void setTextFields(float param) { textFieldParam.setText(Float.toString(param)); }

	public void setDepthField(Double d) {
		if (d == null)
			textFieldDepth.setText("null");
		else
			textFieldDepth.setText(d.toString());
	}


	// This monitors changes in the Choice items:
	public void itemStateChanged(ItemEvent evt) {
		Object source = evt.getItemSelectable();

		if(source == faultTypeChoice) {
			controller.changeFaultType(faultTypeChoice.getSelectedItem());
		}
		else if(source == siteTypeChoice) {
			controller.changeSiteType(siteTypeChoice.getSelectedItem());
		}
		else if(source == xAxisChoice)
			if(xAxisChoice.getSelectedItem() == magString) {
				paramLabel.setText("  "+distString);
				textFieldParam.setText(new String());
				controller.changXaxisData(magString);
			}
			else {
				paramLabel.setText("  "+magString);
				textFieldParam.setText(new String());
				controller.changXaxisData(distString);
			}
		else if(source == gmpChoice) {					//  period_index: 0=PGA and 3=0.3, 10=1, and 30=3 sec
			String period = gmpChoice.getSelectedItem();
			if (period.equals("PGA")) 					// period_index = 0;
				controller.changeGmp((int) 0);
			else if (period.equals("0.3-sec SA")) 		// period_index = 3;
            	controller.changeGmp((int) 3);
            else if (period.equals("1.0-sec SA")) 		// period_index = 10;
                controller.changeGmp((int) 10);
            else // the 3.0 sec case; period_index = 30;
            	controller.changeGmp((int) 30);
        }
        else if(source == sigmaChoice) {					//  sigma choice
			String choice = sigmaChoice.getSelectedItem();
			if (choice.equals("none"))
				controller.changeSigmaFactor((int) 0);
            else if (choice.equals("1 sigma"))
            	controller.changeSigmaFactor((int) 1);
            else // it's 2 sigma;
            	controller.changeSigmaFactor((int) 2);
//	System.out.println();

		}
	}

	// This monitors changes in the textField items:
	public void actionPerformed(ActionEvent evt) {
		Object source = evt.getSource();			//returns object where event occurred
		if (source == addTraceButton) {
			try {
				float param = (float) Float.valueOf(textFieldParam.getText()).floatValue();
				if(xAxisChoice.getSelectedItem() == distString)  // then the param is Magnitude
					controller.changeMag(param);
				else if(xAxisChoice.getSelectedItem() == magString)
					controller.changeDist(param);
			} catch (Exception e1) {
				if(xAxisChoice.getSelectedItem() == distString)  // then the param is Magnitude
					controller.printMessage("Error: Bad Magnitude");
				else if(xAxisChoice.getSelectedItem() == magString)
					controller.printMessage("Error: Bad Distance");
				return;
			}

			if (textFieldDepth.getText().equals("null"))
				controller.changeDepth(null);
			else {
				try {
					controller.changeDepth( Double.valueOf(textFieldDepth.getText()) );
				} catch (Exception e1) {
					controller.printMessage("Error: Bad Basin Depth!");
					return;
				}
			}

			controller.printMessage(null);
			controller.addTrace();
			controller.updatePlot();
		}
		else if (source == clearPlotButton) {
			controller.printMessage(null);
			controller.clearData();
			controller.updatePlot();
		}
	}
}




