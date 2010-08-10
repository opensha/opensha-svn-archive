/*******************************************************************************
 * Copyright 2009 OpenSHA.org in partnership with
 * the Southern California Earthquake Center (SCEC, http://www.scec.org)
 * at the University of Southern California and the UnitedStates Geological
 * Survey (USGS; http://www.usgs.gov)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.opensha.commons.mapping.gmt.gui;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import org.opensha.commons.data.ArbDiscretizedXYZ_DataSet;
import org.opensha.commons.data.XYZ_DataSetAPI;
import org.opensha.commons.gui.DisclaimerDialog;
import org.opensha.commons.param.StringParameter;
import org.opensha.commons.param.editor.StringParameterEditor;
import org.opensha.commons.util.ApplicationVersion;
import org.opensha.commons.util.FileUtils;
import org.opensha.sha.gui.util.IconFetcher;



/**
 * <p>Title: GMT_MapGeneratorApplet</p>
 * <p>Description: this class displays the GMT Map generated by the class
 * GMT_MapGenerator as the image-i(.jpg) Label in the window</p>
 * @author :Nitin Gupta and Vipin Gupta
 * @version 1.0
 */

public class GMT_MapGeneratorApplet extends Applet{
	
	public static final String APP_NAME = "GMT Map Generator Application";
	public static final String APP_SHORT_NAME = "GMTMap";
	private static ApplicationVersion version;
	
	/**
	 * Returns the Application version
	 * @return ApplicationVersion
	 */
	public static ApplicationVersion getAppVersion(){
		if (version == null) {
			try {
				version = ApplicationVersion.loadBuildVersion();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return version;
	}

	private static final String C="GMT_MapGeneratorApplet";

	private static final boolean D= false;


	private boolean isStandalone = false;
	private JPanel mainPanel = new JPanel();
	private JSplitPane mainSplitPane = new JSplitPane();
	private JPanel buttonPanel = new JPanel();

	// default insets
	Insets defaultInsets = new Insets( 4, 4, 4, 4 );
	String mapFileName = null;

	//variables that determine the window size
	protected final static int W = 600;
	protected final static int H = 750;

	private JButton addButton = new JButton();

	private GMT_MapGuiBean gmtGuiBean=null;


	private final static String URL_NAME = "Enter URL";
	private StringParameter xyzFileName=
		new StringParameter(URL_NAME,"http://opensha.usc.edu/data/step/backGround.txt");
	private StringParameterEditor xyzFileEditor;
	private JPanel parameterPanel = new JPanel();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private GridBagLayout gridBagLayout2 = new GridBagLayout();
	private GridBagLayout gridBagLayout3 = new GridBagLayout();
	private BorderLayout borderLayout1 = new BorderLayout();
	//Get a parameter value
	public String getParameter(String key, String def) {
		return isStandalone ? System.getProperty(key, def) :
			(getParameter(key) != null ? getParameter(key) : def);
	}


	static {

		try { UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName()); }
		catch ( Exception e ) {}
	}

	//Construct the applet
	public GMT_MapGeneratorApplet() {
	}
	//Initialize the applet
	public void init() {
		try {
			jbInit();
			xyzFileEditor = new StringParameterEditor(xyzFileName);
			parameterPanel.add(xyzFileEditor,new GridBagConstraints( 0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
			gmtGuiBean =new GMT_MapGuiBean();
			//panel to display the GMT adjustable parameters
			parameterPanel.add(gmtGuiBean,new GridBagConstraints( 0, 1, 1, 1, 1.0, 1.0,
					GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, defaultInsets, 0, 0 ));
			parameterPanel.validate();
			parameterPanel.repaint();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	//Component initialization
	private void jbInit() throws Exception {
		this.setSize(new Dimension(492, 686));
		this.setLayout(borderLayout1);
		mainPanel.setLayout(gridBagLayout3);
		mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setLastDividerLocation(670);
		buttonPanel.setLayout(gridBagLayout1);
		addButton.setText("Make Map");
		addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addButton_actionPerformed(e);
			}
		});

		parameterPanel.setLayout(gridBagLayout2);
		this.add(mainPanel, BorderLayout.CENTER);
		mainPanel.add(mainSplitPane,  new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
				,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(4, 4, 6, 6), 0, 595));
		mainSplitPane.add(buttonPanel, JSplitPane.RIGHT);
		buttonPanel.add(addButton,    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
				,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(15, 47, 14, 300), 27, 9));
		mainSplitPane.add(parameterPanel, JSplitPane.LEFT);
		mainSplitPane.setDividerLocation(630);
	}


	//Start the applet
	public void start() {
	}
	//Stop the applet
	public void stop() {
	}
	//Destroy the applet
	public void destroy() {
	}
	//Get Applet information
	public String getAppletInfo() {
		return "MAPS using the GMT";
	}
	//Get parameter info
	public String[][] getParameterInfo() {
		return null;
	}
	//Main method
	public static void main(String[] args) throws IOException {
		new DisclaimerDialog(APP_NAME, APP_SHORT_NAME, getAppVersion());
		GMT_MapGeneratorApplet applet = new GMT_MapGeneratorApplet();
		applet.isStandalone = true;
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("Maps");
		frame.getContentPane().add(applet, BorderLayout.CENTER);
		applet.init();
		frame.setIconImages(IconFetcher.fetchIcons(APP_SHORT_NAME));
		applet.start();
		frame.setSize(W,H);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
		frame.setVisible(true);
	}

	//static initializer for setting look & feel
	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		}
		catch(Exception e) {
		}
	}

	void addButton_actionPerformed(ActionEvent e) {
		addButton();
	}


	/**
	 * this method calls the generate Map function to generate the jpg image file of the
	 * Map
	 */
	void addButton() {

		String fileName = (String)this.xyzFileName.getValue();
		XYZ_DataSetAPI xyzData = null;
		if(fileName != null){
			ArrayList xVals = new ArrayList();
			ArrayList yVals = new ArrayList();
			ArrayList zVals = new ArrayList();
			try{
				URL fileURL = new URL((String)this.xyzFileName.getValue());
				ArrayList fileLines =FileUtils.loadFile(fileURL);
				ListIterator it = fileLines.listIterator();
				while(it.hasNext()){
					StringTokenizer st = new StringTokenizer((String)it.next());
					xVals.add(new Double(st.nextToken().trim()));
					yVals.add(new Double(st.nextToken().trim()));
					zVals.add(new Double(st.nextToken().trim()));
				}
				xyzData = new ArbDiscretizedXYZ_DataSet(xVals,yVals,zVals);
			}catch(Exception ee){
				JOptionPane.showMessageDialog(this,new String("Please enter URL"),"Error", JOptionPane.OK_OPTION);
				ee.printStackTrace();
			}
		}
		String metadata = "You can download the jpg or postscript files for:\n\t"+
		fileName+"\n\n"+
		"From (respectively):";

		gmtGuiBean.makeMap(xyzData,metadata);
	}
}






