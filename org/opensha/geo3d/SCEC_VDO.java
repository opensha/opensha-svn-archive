package org.opensha.geo3d;


import javax.swing.JDesktopPane;


import java.awt.BorderLayout;
import javax.swing.JFrame;

import org.scec.geo3d.gui.viewer.GlobeView;
import org.scec.geo3d.gui.ViewRange;
import org.scec.geo3d.tools.plugin.PluginHandlerImpl;

/**
 * This class shows the 3D SCEC-VDO developed by SCEC IT interns
 * 
 * @author vipingupta
 *
 */
public class SCEC_VDO extends JDesktopPane {
	private final static int DEFAULT_LOWER_LATITUDE = 32;
	private final static int DEFAULT_UPPER_LATITUDE = 36;
	private final static int DEFAULT_LEFT_LONGITUDE  = -122;
	private final static int DEFAULT_RIGHT_LONGITUDE = -114;
	public static final Integer DOCLAYER = new Integer(5);
		
	public SCEC_VDO() {
		setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
		ViewRange  viewRange = new ViewRange(DEFAULT_LOWER_LATITUDE, DEFAULT_UPPER_LATITUDE, DEFAULT_LEFT_LONGITUDE, DEFAULT_RIGHT_LONGITUDE);
		GlobeView globeView = new GlobeView(PluginHandlerImpl.getInstance(), viewRange);
		add(globeView, DOCLAYER);
		globeView.setVisible(true);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(new SCEC_VDO(), BorderLayout.CENTER);
		frame.show();
	}

}
