package org.scec.gui.ned;

import java.applet.Applet;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * <b>Title:</b> Field2000PlotApplet<br>
 * <b>Description:</b> First Test Applet to show Seismic analysis<br>
 * <b>Copyright:</b>    Copyright (c) 2001
 * <b>Company:</b>
 * @author Ned Field
 * @version 1.0
 */

public class Field2000PlotApplet extends  Applet {

	ControlPanel controlPanel;
	InfoPanel	infoPanel;

	String	faultType, period, siteType;
	float	mag, dist, v30;
	Double	depth;			// This is Double so it can have null values
	int		period_index;	// set 0 for PGA, or 10*period for SA
	String  gmpString;
	String  xAxisString;	// "Distance (km)" or "Magnitude" to specify what to plot on the X axis
							// this is also used to label the axis
	String	yAxisString;		//  Y axis label
	String	titleString;		//  Title label


	float minDist =(float) 1, maxDist = (float) 100;
	float minMag =(float) 4,  maxMag = (float) 8.5;
	float minAmp = (float) 0.001, maxAmp = (float) 2.0;

	Vector	data, colors;
	Vector	traceInfo;

	AttenField2000 gmp;
	AttenBJF97  gmpBJF;
	XYplotCanvas plotCanvas;


    public static void main(String[] args) {
        Field2000PlotApplet applet = new Field2000PlotApplet();
        //applet.isStandalone = true;
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(3);
        frame.setTitle("Field 2000 Plot Applet");
        frame.getContentPane().add(applet, "Center");
        applet.init();
        applet.start();
        frame.setSize(400, 320);
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((d.width - frame.getSize().width) / 2,
                  (d.height - frame.getSize().height) / 2);
        frame.setVisible(true);
    }

	/* Create user interface needed by this applet. */
	public void init() {

        //setBackground(new Color((float)0.78, (float)0.78, (float)1));
        setBackground(Color.black);
        setForeground(Color.white);

		// Initialize values:
		mag = (float) 6.5;
		faultType = new String("strike slip");		// Changes of the intial values of these may also
		siteType = new String("B");					// need to be made in the control panel for the
		period_index = 0;		// PGA				// default trace to be consistent with initial values.
		depth = null;
		v30 = 1000;

		dist=(float) 1;
		gmpString = new String("PGA");
		xAxisString = new String("Distance (Rjb) in km");	// Plot the default as versus distance
		yAxisString = new String(gmpString);
		titleString = new String("Field (2000) Attenuation Relation");

		colors = new Vector();	// up to 12 colors
		colors.addElement(Color.blue);
		colors.addElement(Color.green);
		colors.addElement(Color.red);
		colors.addElement(Color.orange);
		colors.addElement(Color.magenta);
		colors.addElement(Color.cyan);
		colors.addElement(Color.pink);
		colors.addElement(Color.white);
		colors.addElement(Color.yellow);
		colors.addElement(Color.darkGray);
		colors.addElement(Color.gray);


		setLayout(new FlowLayout());

		// Make the control panel:
		controlPanel = new ControlPanel(this);
		add(controlPanel);
		controlPanel.setTextFields(mag);
		controlPanel.setDepthField(depth);
		controlPanel.setForeground(Color.orange);

		// calculate & set intial ground-motion values
		data = new Vector();
		traceInfo = new Vector();
		try {	// do this once to make access to class variables (MAXMAG ...)
		 	gmp = new AttenField2000(mag,dist,faultType,v30,period_index,depth);
		} catch (Exception excep) {
			System.out.println(excep.getMessage());
		}
		try {	// do this once to make access to class variables (MAXMAG ...)
		 	gmpBJF = new AttenBJF97(mag,dist,faultType,v30,period_index);
		} catch (Exception excep) {
			System.out.println(excep.getMessage());
		}		addTrace();

		// Make the plot canvas:
		plotCanvas = new XYplotCanvas (data);
		add(plotCanvas);
		plotCanvas.changeSize (400, 300);
		plotCanvas.setColors(colors);
//		plotCanvas.setAxes(minDist, maxDist, minAmp, maxAmp);
		plotCanvas.setScaling("log", "log");
		plotCanvas.addLabels (xAxisString, yAxisString, titleString);

		// Make the info Panel (actually this is a Canvas!)
		infoPanel = new InfoPanel();
		infoPanel.setSize(400,175);
		infoPanel.setTraceInfo(traceInfo, colors);
		add(infoPanel);
	}

	// Add a new trace to the data vector
	public void addTrace() {
		int maxTraces = 10;  // the max number of useful colors
		if (data.size() < maxTraces-1) {
			data.addElement (new Vector());
			int trace_number = data.size() - 1;
			String info;
			int i=0, N=600;  // N= number of points in the plot
			Float x, dx, y, dy;
			float d, m; 	// temp dist and mag
			Vector trace = (Vector) data.elementAt(trace_number);

			// Check the values and issue warnings if necessary
			if(xAxisString.equals("Distance (Rjb) in km)")) {
				if(mag < gmp.MINMAG)
					printWarning("Warning: Mag "+gmp.MINMAG+" is the lowest recommended magnitude for Field2000!");
				else if(mag > gmp.MAXMAG)
					printWarning("Warning: Mag "+gmp.MAXMAG+" is the highest recommended magnitude for Field2000!");
			}
			else 	// if(xAxisString.equals("Magnitude"))
				if(dist > gmp.MAXDIST)
					printWarning("Warning: Distances > "+gmp.MAXDIST+" km are not recommended for Field2000!");

			// Add info about this trace
			if(xAxisString.equals("Distance (Rjb) in km"))
				info = new String("Field2000: M="+mag+", Fault type = "+faultType+", Site type = "+siteType+", B-Depth = "+depth+" km");
			else 	// if(xAxisString.equals("Magnitude"))
				info = new String("Field2000: Dist="+dist+", Fault type = "+faultType+", Site type = "+siteType+", B-Depth = "+depth+" km");
        	traceInfo.addElement (info);

        	// Compute the amplitudes
       		for(i=0; i<=N; i++) {
				if(xAxisString.equals("Distance (Rjb) in km")) {
					d = (float)i*(gmp.MAXDIST-gmp.MINDIST)/((float)N) + gmp.MINDIST;		// The distance
					m = mag;
					x =  new Float(d);
				}
				else { 	// if (xAxisString.equals("Magnitude"))
					m = (float) i*(gmp.MAXMAG-gmp.MINMAG)/((float)N) + gmp.MINMAG;		// The distance
					d = dist;
					x =  new Float(m);
				}
				try {
					gmp = new AttenField2000(m,d,faultType,v30,period_index,depth);
		 		  	y =  new Float(gmp.amp());
        			dx = new Float(0);
        			dy = new Float(gmp.std());		// this is the stdev of ln(y)!
       				trace.addElement (new DataPoint(x,dx,y,dy));
				} catch (Exception excep) {
					data.removeElementAt(trace_number);
					traceInfo.removeElementAt(trace_number);
					printMessage(excep.getMessage());
					break;
				}
			}

/*			//  Add the BJF97 curve for comparison ***************************
			data.addElement (new Vector());
			trace_number = data.size() - 1;
			trace = (Vector) data.elementAt(trace_number);

			// Add info about this trace
			if(xAxisString.equals("Distance (Rjb) in km"))
				info = new String("BJF97: M="+mag+", Fault type = "+faultType+", Site type = "+siteType);
			else 	// if(xAxisString.equals("Magnitude"))
				info = new String("BJF97: Dist="+dist+", Fault type = "+faultType+", Site type = "+siteType);
        	traceInfo.addElement (info);

        	// Compute the amplitudes
       		for(i=0; i<=N; i++) {
				if(xAxisString.equals("Distance (Rjb) in km")) {
					d = (float)i*(gmpBJF.MAXDIST-gmpBJF.MINDIST)/((float)N) + gmpBJF.MINDIST;		// The distance
					m = mag;
					x =  new Float(d);
				}
				else { 	// if (xAxisString.equals("Magnitude"))
					m = (float) i*(gmpBJF.MAXMAG-gmpBJF.MINMAG)/((float)N) + gmpBJF.MINMAG;		// The distance
					d = dist;
					x =  new Float(m);
				}
				try {
		 		  	gmpBJF = new AttenBJF97(m,d,faultType,v30,period_index);
		 		  	y =  new Float(gmpBJF.amp());
        			dx = new Float(0);
        			dy = new Float(gmpBJF.std());		// this is the stdev of ln(y)!
       				trace.addElement (new DataPoint(x,dx,y,dy));
				} catch (Exception excep) {
					data.removeElementAt(trace_number);
					traceInfo.removeElementAt(trace_number);
					printMessage(excep.getMessage());
					break;
				}
			}
			// **************************************************************************
*/
        }
        else
			printMessage("No more Traces; (" + data.size() + " is the maximum)");
	}

	public void updatePlot() {
		plotCanvas.changeData(data);
		plotCanvas.addLabels (xAxisString, yAxisString, titleString);
		infoPanel.setTraceInfo(traceInfo, colors); 	//  Update the info panel
	}

	public void clearData() {
		data = new Vector();
		traceInfo = new Vector();
		}

	public void changeFaultType(String newFaultType) { faultType = newFaultType; }

	public void changeGmp(int new_period_index) {
		period_index = new_period_index;

		// Set period index string
		if (period_index == 0)
			gmpString = new String("PGA");
		else
			gmpString = new String((float) period_index/10 +"-sec SA");
		yAxisString = new String(gmpString);
		clearData();
		updatePlot();
	}
	public void changeMag(float new_mag) { mag = new_mag; }

	public void changeDepth(Double new_depth) { depth = new_depth; }

	public void changeDist(float new_dist) { dist = new_dist; }

	public void changeSiteType(String new_siteType) {
		siteType = new_siteType;
		if(siteType.equals("B")) v30 = 1000;
		if(siteType.equals("BC")) v30 = 760;
		if(siteType.equals("C")) v30 = 560;
		if(siteType.equals("CD")) v30 = 360;
		if(siteType.equals("D")) v30 = 270;
		if(siteType.equals("DE")) v30 = 180;
	}

	public void changeSigmaFactor(int factor) { plotCanvas.setSigmaFactor(factor); }

	public void changXaxisData(String s) {
		xAxisString =s;
		if(xAxisString.equals("Distance (Rjb) in km")){
			plotCanvas.setAxes(minDist, maxDist, (float) 0.01, (float) 3);
			plotCanvas.setScaling("log", "log");
		}
		else { 	// if(xAxisString.equals("Magnitude"))
			plotCanvas.setAxes(minMag, maxMag, (float) 0.01, (float) 3);
			plotCanvas.setScaling("linear", "log");
		}
		clearData();
		updatePlot();
	}

	void printMessage(String s) { infoPanel.setMessage(s); }

	void printWarning(String s) { infoPanel.setWarning(s); }
}


































