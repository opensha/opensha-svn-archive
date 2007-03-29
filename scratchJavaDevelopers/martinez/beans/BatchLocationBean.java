package scratchJavaDevelopers.martinez.beans;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.opensha.data.Location;
import org.opensha.param.DoubleParameter;
import org.opensha.param.ParameterList;
import org.opensha.param.StringParameter;
import org.opensha.param.editor.DoubleParameterEditor;
import org.opensha.param.editor.StringParameterEditor;
import org.opensha.param.event.ParameterChangeEvent;
import org.opensha.param.event.ParameterChangeFailEvent;
import org.opensha.param.event.ParameterChangeFailListener;
import org.opensha.param.event.ParameterChangeListener;
import org.opensha.util.ImageUtils;

import scratchJavaDevelopers.martinez.util.BatchFileReader;

public class BatchLocationBean implements GuiBeanAPI, ParameterChangeListener, ParameterChangeFailListener {
	
	/////////////////// EMBEDED VISUALIZATION COMPONENETS /////////////////////
	
	/* Parameter Names */
	public static final String PARAM_BAT = "Batch File";
	public static final String PARAM_LAT = "Latitude";
	public static final String PARAM_LON = "Longitude";
	public static final String PARAM_ZIP = "5 Digit Zip Code";

	/* Parameter Editors */
	private StringParameterEditor batEditor = null;
	private DoubleParameterEditor latEditor = null;
	private DoubleParameterEditor lonEditor = null;
	private StringParameterEditor zipEditor = null;
	
	/* Parameters */
	private StringParameter batParam = new StringParameter(PARAM_BAT, "");
	private DoubleParameter latParam = new DoubleParameter(PARAM_LAT, -90, 90);
	private DoubleParameter lonParam = new DoubleParameter(PARAM_LON, -180, 180);
	private StringParameter zipParam = new StringParameter(PARAM_ZIP, "");

	// << END EMBEDED VISUALIZATION COMPONENTS >> //
	
	/* Visualization Components */
	private JTabbedPane panel = null;
	private String web = null;
	private JButton btnFileChooser = null;
	
	/* Tab Names */
	private static final String GEO_TAB = "Lat/Lon";
	private static final String ZIP_TAB = "Zip Code";
	private static final String BAT_TAB = "Batch File";
	
	/* Tab Indexes */
	public static final int GEO_MODE = 0;
	public static final int ZIP_MODE = 1;
	public static final int BAT_MODE = 2;
	
	/* Flags to determine which type of bean we are using */
	private static final int EMBED_FLAG = 0;
	private static final int WEB_FLAG = 1;
	private int currentMode;
	
	////////////////////////////////////////////////////////////////////////////////
	//                              PUBLIC FUNCTIONS                              //
	////////////////////////////////////////////////////////////////////////////////
	
	/* CONSTRUCTORS */
	public BatchLocationBean() {
		// Empty but that's okay too
	}
	
	public ArrayList<Location> getBatchLocations() {
		String fileName = (String) batParam.getValue();
		BatchFileReader bfr = new BatchFileReader(fileName);
		ArrayList<Location> locations = null;
		if(!bfr.ready()) return null;
		ArrayList<Double> lats = bfr.getColumnVals((short) 0, 0);
		ArrayList<Double> lons = bfr.getColumnVals((short) 1, 0);
		if(lats.size() != lons.size()) {
			ExceptionBean.showSplashException("File contained a different number of latitude an longitudes." +
					" These must be the same!", "Bad File Layout", null);
			return null;
		} else {
			locations = new ArrayList<Location>();
			for(int i = 0; i < lats.size(); ++i)
				locations.add(new Location(lats.get(i), lons.get(i)));
		}
		return locations;
	}
	
	public Location getSelectedLocation() {
		double lat = (Double) latParam.getValue();
		double lon = (Double) lonParam.getValue();
		return new Location(lat, lon);
	}
	
	/**
	 * Returns the current zip code stored in the zip <code>StringParameter</code>.
	 * This may be null or empty (&quot;&quot;).  Caller should check return status.
	 * @return The current zip code
	 */
	public String getZipCode() {
		return (String) zipParam.getValue();
	}
	
	public int getLocationMode() {
		int answer = -1;
		if(currentMode == EMBED_FLAG) {
			if(panel != null)
				answer = panel.getSelectedIndex();
		} else if (currentMode == WEB_FLAG) {
			answer = -2;
		}
		return answer;
	}
	
	public void updateGuiParams(double minLat, double maxLat, 
								double minLon, double maxLon, 
								boolean zipCodeSupported) {
		if(panel != null) {
			latParam = new DoubleParameter(PARAM_LAT, minLat, maxLat);
			lonParam = new DoubleParameter(PARAM_LON, minLon, maxLon);
			panel.setEnabledAt(ZIP_MODE, zipCodeSupported);
		}
	}
	
	public ParameterList getLocationParameters() {
		ParameterList list = new ParameterList();
		list.addParameter(latParam);
		list.addParameter(lonParam);
		list.addParameter(zipParam);
		list.addParameter(batParam);
		return list;
	}
	
	public void createNoLocationGUI() {
		if(panel != null) {
			panel.setEnabledAt(GEO_MODE, false);
			panel.setEnabledAt(ZIP_MODE, false);
			panel.setSelectedIndex(BAT_MODE);
			JOptionPane.showMessageDialog(null, "The current region only supports batch files.", 
					"Batch Mode Required", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	/**
	 * Always returns true.  Exists for legacy reasons only.
	 * 
	 * @return true
	 * @deprecated
	 */
	public boolean hasLocation() {
		return true;
	}
	
	/* MINIMUM FUNCTIONS TO IMPLEMENT GuiBeanAPI */
	/**
	 * See the general contract in <code>GuiBeanAPI</code>
	 * @see GuiBeanAPI
	 */
	public Object getVisualization(int type) throws IllegalArgumentException {
		if(type == GuiBeanAPI.EMBED) {
			return getEmbedVisualization();
		} else if (type == GuiBeanAPI.WEB) {
			return getWebVisualization();
		}
		throw new IllegalArgumentException("The given type is not currently supported");
	}
	/**
	 * See the general contract in <code>GuiBeanAPI</code>
	 * @see GuiBeanAPI
	 */
	public String getVisualizationClassName(int type) {
		if(type == GuiBeanAPI.EMBED)
			return "javax.swing.JTabbedPane";
		else if (type == GuiBeanAPI.WEB)
			return "java.lang.String";
		else
			return null;
	}
	/**
	 * See the general contract in <code>GuiBeanAPI</code>
	 * @see GuiBeanAPI
	 */
	public boolean isVisualizationSupported(int type) {
		return (type == GuiBeanAPI.EMBED || type == GuiBeanAPI.WEB);
	}

	/* MINIMUM FUNCTIONS TO IMPLEMENT ParameterChangeListener AND ParameterChangeFailListener */
	/**
	 * See the general contract in <code>ParameterChangeListener</code>.
	 * @see ParameterChangeListener
	 */
	public void parameterChange(ParameterChangeEvent event) {
		String paramName = event.getParameterName();
		if(paramName.equals(PARAM_ZIP)) {
			String zipCode = (String) event.getNewValue();
			if(zipCode.length() != 5 && zipCode.length() != 0) {
				ExceptionBean.showSplashException("Please enter a valid 5 digit Zip Code", 
						"Invalid Zip Code", null);
				zipParam.setValue("");
			}
		}
	}

	public void parameterChangeFailed(ParameterChangeFailEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	////////////////////////////////////////////////////////////////////////////////
	//                             PRIVATE FUNCTIONS                              //
	////////////////////////////////////////////////////////////////////////////////
	
	private JTabbedPane getEmbedVisualization() {
		if(panel == null) {
			// Create the parameters
			batParam = new StringParameter(PARAM_BAT, "");
			latParam = new DoubleParameter(PARAM_LAT, -90, 90);
			lonParam = new DoubleParameter(PARAM_LON, -180, 180);
			zipParam = new StringParameter(PARAM_ZIP, "");
			
			// Create the editors
			try {
				batEditor = new StringParameterEditor(batParam);
				latEditor = new DoubleParameterEditor(latParam);
				lonEditor = new DoubleParameterEditor(lonParam);
				zipEditor = new StringParameterEditor(zipParam);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			// Add some listeners
			batParam.addParameterChangeListener(this);
			batParam.addParameterChangeFailListener(this);
			zipParam.addParameterChangeListener(this);
			zipParam.addParameterChangeFailListener(this);
			
			btnFileChooser = new JButton(new ImageIcon(
					ImageUtils.loadImage("openFile.png")));
			btnFileChooser.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					btnFileChooser_actionPerformed();
				}
			});
			
			// The panels for the three types of input
			JPanel geoPanel = new JPanel(new GridBagLayout());
			JPanel zipPanel = new JPanel(new FlowLayout());
			JPanel batPanel = new JPanel(new GridBagLayout());
			
			// Set up the geoPanel
			geoPanel.add(latEditor, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 2, 0));
			geoPanel.add(lonEditor, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 2, 0));
			
			// Set up the zipPanel
			zipPanel.add(zipEditor, 0);
			
			// Set up the batPanel
			batPanel.add(batEditor, new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 2, 0));
			batPanel.add(btnFileChooser, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
					GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 2, 0));
			
			// Now add the panels to the tabs...
			panel = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
			panel.addTab(GEO_TAB, geoPanel);
			panel.addTab(ZIP_TAB, zipPanel);
			panel.addTab(BAT_TAB, batPanel);
		}
		currentMode = EMBED_FLAG;
		return panel;
	}
	
	private String getWebVisualization() {
		if(web == null) {
			web = "Visualization Not Implemented"; // Not really supported yet
		}
		currentMode = WEB_FLAG;
		return web;
	}
	
	private void btnFileChooser_actionPerformed() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileFilter() {
			public boolean accept(File arg0) {
				return (arg0.getAbsolutePath().endsWith("xls") || arg0.isDirectory());
			}
			public String getDescription() {
				return "Microsoft Excel File (*.xls)";
			}
		});
		JFrame frame = new JFrame("Select a batch file");
		int returnVal = chooser.showOpenDialog(frame);
		String newFileName = (String) batParam.getValue();
		if(returnVal == JFileChooser.APPROVE_OPTION)
			newFileName = chooser.getSelectedFile().getAbsolutePath();
		try {
			batParam.setValue(newFileName);
			( (JTextField) batEditor.getValueEditor()).setText(newFileName);
		} catch (Exception ex) {
			batParam.unableToSetValue(newFileName);
		}
	}


	

}
