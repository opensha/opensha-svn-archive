package org.scec.gui.ned;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * Title: XYplotCanvas<br>
 * Description:  This class makes a clickable XY plot of data passed to it.  The input data is a Vector
 * of traces, which in turn are a vectors of pints defined in the DataPoint class (x,y,dx,dy).
 * <br>v
 * This plotting routine was adapted from Friedrich Schotte's Plot Applet (streamlined
 * and updated to Java 1.1 event handling (I also made the plotting a subclass of canvas).
 * <br><br>
 * The public methods are as follows:
 * <br><br>
 *    	setColors(Vector c)<br><br>
 *
 *					// Set plotting colors
 *  <br><br>
 *    				e.g. pass it:  Vector c = new Vector(); c.addElement(Color.blue);
 *  <br>
 *   				If only one element in vector, all traces given this color; add more
 * 					elements to give difference colors to each trace.
 *   				(default is blue).
 *  <br><br>
 *   	setSymbol(String sym, int size)<br>
 *   	setSymbol(Vector sym, int size)
 * <br><br>
 *					// Set plotting symbols (all different) and size.
 * <br><br>
 *					Options:	"none" 	(default)<br>
 *								"square"<br>
 *								"circle"<br>
 *								"triangle"<br>
 *								"cross"<br>
 *								"dot"<br>
 *								"bar"<br>
 *								"filled_square"<br>
 *								"filled_circle"<br>
 *								"filled_triangle"<br>
 * <br><br>
 *		setAxes(float xmn, float xmx, float ymn, float ymx)
 * <br><br>
 *					// Set axes limits
 * <br><br>
 *					setAxes() defaults back to scale by data.
 * <br><br><br>
 *
 *
 *   	setScaling(String xaxis, String yaxis)
 * <br><br>
 *   				// Set plotting to linear or log;
 * <br><br>
 *   				Options: "linear" or "log"
 * <br><br>
 *   				Note: typing errors default to linear (no exceptions thrown)
 * <br><br>
 *   	addLabels (String xaxis, String yaxis, String title)
 * <br><br>
 *   				// Add axes and title labels*
 * <br><br>
 *    	changeSize (int width, int height)
 * <br><br>
 *   				//Change the total canvas size (not plotting area)
 * <br><br>
 * NOTE:  I made some modifications in the version below to make it look better
 * (color changes; shifts in the area; hard-wire fix of a round() problem).
 * I also changed the treatment of uncertainties.  dx and dy are now the lower
 * and upper bounds of y plotted as a line
 * <br><br>
 *
 * Copyright:    Copyright (c) 2001<br>
 * Company:<br>
 * @author Ned Field
 * @version 1.0
 */

public class XYplotCanvas extends Canvas implements MouseListener {

	Vector data;
	Vector colors = new Vector();		// e.g.  colors.addElement(Color.red);
    Vector x_offsets = new Vector();
    Vector y_offsets = new Vector();
    Vector join = new Vector();
    Vector markers = new Vector();
    int tick_length;
    int minor_tick_length;		// Added in this version
    int tick_spacing;
    Color background;
    String x_axis_label = new String();			//  X axis label
    String y_axis_label = new String();			//  Y axis label
    String title_label = new String();			//  Title label
    final int linear = 0, log = 1;
    int x_scaling, y_scaling;
    Point mouse_down = null;
    float xmin, xmax, ymin, ymax;
	Float XMIN, YMIN, XMAX, YMAX;
	Vector x_ticks = new Vector();
	Vector y_ticks = new Vector();
	Rectangle area;
	Rectangle selection = null;
	int font_height, char_width;
	Font f, title_font;
	int symbolSize, outer_border;
	String  cursorLabelX, cursorLabelY;

	int sigmaFactor;  	// Added here for drawing upper and lower bounds

	public XYplotCanvas(Vector input_data) {			// The constructor

		data = input_data;

		// Set Default Attributes  ---------  I could create methods that modify these
		setSize(200,200);								//  Set the size of the canvas
		x_scaling = linear;  							//  "linear" or "log"
    	y_scaling = linear;
		f = new Font("Application", Font.PLAIN, 10);	//  Set Font
		tick_length = 6;
		minor_tick_length = 3;
		tick_spacing = 60;
		background = Color.black;
   	 	symbolSize = 8;									//  The symbol size
   	 	outer_border = 0;							// Outer border
   	 	sigmaFactor = 0;			// Default is no uncertainties

		// Set attributes for each trace:
		join.addElement (Boolean.TRUE);			// draw lines between points?
		markers.addElement (null);				// e.g. markers.addElement ("filled_square");
		colors.addElement(Color.blue);   		// default color is blue
    	y_offsets.addElement (new Float(0.0));	// must set at least first one
		x_offsets.addElement (new Float(0.0));  // must set at least first one

		calcArea();								// calculate plotting area

		// Add Cursor/Mouse Listener
		addMouseListener(this);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		cursorLabelX = new String();
		cursorLabelY = new String();
    }

    public void setSigmaFactor(int f) {
    	sigmaFactor=f;
    	repaint();
    }

    public void changeData(Vector new_data) {
    	data = new_data;
    	repaint();
    }

    // Set plotting colors:
    public void setColors(Vector c) { colors = c; }

    // Set plotting symbol (all the same) and sizes:
    public void setSymbol(String sym, int size) {
    	markers = new Vector();
    	markers.addElement (sym);
    	symbolSize = size;
    	}

    // Set plotting symbols (all different) and sizes:
    public void setSymbol(Vector sym, int size) {
    	markers = sym;
    	symbolSize = size;
    	}


    // Set axes limits
    public void setAxes(float xmn, float xmx, float ymn, float ymx) {
		XMIN = new Float(xmn);
		XMAX = new Float(xmx);
		YMIN = new Float(ymn);
		YMAX = new Float(ymx);
    }
    public void setAxes() {
		XMIN = null;		// set as null to get default
		XMAX = null;
		YMIN = null;
		YMAX = null;
    }

    // Set plotting to linear or log;  Note: typing errors default to linear (no exceptions thrown)
    public void setScaling(String xaxis, String yaxis) {
    	if(xaxis == "log")
    		x_scaling = log;
    	else
    		x_scaling = linear;
    	if(yaxis == "log")
    		y_scaling = log;
    	else
    		y_scaling = linear;
    }

    // Add axes and title labels
    public void addLabels (String xaxis, String yaxis, String title) {
    	x_axis_label = xaxis;			//  X axis label
 	  	y_axis_label = yaxis;			//  Y axis label
 	  	title_label = title;			//  Y axis label
    	calcArea();
	}

    public void changeSize (int width, int height) {
    	setSize(width,height);
    	calcArea();
	}


    public void paint (Graphics g)
    {

//Rectangle a = new Rectangle(size());
//g.drawRect(a.x+3,a.y+3,a.width-7,a.height-7);

		if (XMIN == null || XMAX == null || YMIN == null || YMAX == null) {
       		scale_by_data();
			round_scaling();
		}

    	if (XMIN != null) xmin = XMIN.floatValue(); if (XMAX != null) xmax = XMAX.floatValue();
      	if (YMIN != null) ymin = YMIN.floatValue(); if (YMAX != null) ymax = YMAX.floatValue();
	  	if (xmin == xmax) { xmin -= 0.5 ; xmax += 0.5; }
	  	if (ymin == ymax) { ymin -= 0.5 ; ymax += 0.5; }

      	calculate_tick_positions();
      	draw_axes (g);
      	g.setColor(Color.red);
      	g.drawString("Click Location",area.x + (int)(0.7*area.width),area.y + font_height+3);
      	g.drawString("X: " + cursorLabelX,area.x + (int)(0.7*area.width),area.y + 2*font_height+3);
		g.drawString("Y: " + cursorLabelY,area.x + (int)(0.7*area.width),area.y + 3*font_height+3);
		g.setColor(Color.red);
      	plot_data (g);
    }

    //  I need all of these unless I use MouseAdapter which seems complex
    public void mouseEntered (MouseEvent event) {}
	public void mouseExited  (MouseEvent event) {}
	public void mousePressed (MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseClicked(MouseEvent evt) {
		int x = evt.getX();
		int y = evt.getY();
		if(x >= area.x && x <= area.x + area.width && y >= area.y && y <= area.y + area.height) {
//			System.out.println("x value: "+ x_value(x) + ";  y value: " + y_value(y));
			cursorLabelX = String.valueOf(x_value(x));
			cursorLabelY = String.valueOf(y_value(y));
			repaint();
		}
	}


    private void calcArea() {
        area = new Rectangle (getSize());
		area.x += outer_border; area.width -= 2*outer_border;
      	area.y += outer_border; area.height -= 2*outer_border;
      	setFont(f);
      	FontMetrics font = getFontMetrics(f);
	  	font_height = font.getHeight();
	  	char_width = font.getMaxAdvance();
	  	area.height -= font_height; 								// leave space for x scaling marks

	  	area.y += font_height/2; area.height -= font_height; 		// for y scaling marks
      	if (x_axis_label != null) area.height -= font_height; 	 	// leave space for x axis label
      	if (y_axis_label != null) {
      		area.y += font_height/2;
      		area.height -= font_height;
      	}
      	area.x += 7*char_width; area.width -= 7*char_width; 		// space for y scaling marks
      	area.width -= 3*char_width; 								// space for x scaling marks
// Add an arbitray shift to make if fit better
area.x -= 23; area.y += 20;	area.height -=20;
    }

    private void scale_by_data()
    {
	  first: for (int i=0; i < data.size(); i++)
	  {
	    Vector trace = (Vector) data.elementAt(i);
	    if (trace.size() == 0) continue;

        float xo = ((Float) x_offsets.elementAt (i % x_offsets.size())).floatValue();
        float yo = ((Float) y_offsets.elementAt (i % y_offsets.size())).floatValue();
	    for (int j=0; j < trace.size(); j++)
	    {
	      float x = ((DataPoint) trace.elementAt(j)).x;
	      float y = ((DataPoint) trace.elementAt(j)).y;
	      x = inv_x_scale (x_scale(x+xo)); y = inv_y_scale (y_scale(y+yo));
	      if (Float.isNaN(x) || Float.isNaN(y)) continue;
	      xmin = xmax = x; ymin = ymax = y;
	      break first;
	    }
	  }
	  for (int i=0; i < data.size(); i++)
	  {
	    Vector trace = (Vector) data.elementAt(i);
        float xo = ((Float) x_offsets.elementAt (i % x_offsets.size())).floatValue();
        float yo = ((Float) y_offsets.elementAt (i % y_offsets.size())).floatValue();

	    for (int j=0; j < trace.size(); j++)
	    {
	      float x = ((DataPoint) trace.elementAt(j)).x;
	      float y = ((DataPoint) trace.elementAt(j)).y;
	      x = inv_x_scale (x_scale(x+xo)); y = inv_y_scale (y_scale(y+yo));
	      if (x < xmin) xmin = x; if (x > xmax) xmax = x;
	      if (y < ymin) ymin = y; if (y > ymax) ymax = y;
	    }
	  }
	}

    private void round_scaling()
    {
      float x1 = x_value (x_pixel(xmin)-tick_spacing);
      float x2 = x_value (x_pixel(xmax)+tick_spacing);
      float y1 = y_value (y_pixel(ymin)+tick_spacing);
      float y2 = y_value (y_pixel(ymax)-tick_spacing);

      xmin = round (xmin,x1-xmin); xmax = round (xmax,x2-xmax);
      ymin = round (ymin,y1-ymin); ymax = round (ymax,y2-ymax);
    }

        // This calculates the tick positions
    private void calculate_tick_positions()
    {


        x_ticks.setSize(0);
		if (x_scaling == log) {
			int i, j;
			int inthigh  = (int)lg(x_value(area.width+area.x));
			int intlow = (int)lg(x_value(area.x));
			for (i=intlow-1;i <= inthigh;i++) {
				int px = x_pixel((float)Math.pow (10,i));
				if(x_value(px) >= x_value(area.x) && x_value(px) <= x_value(area.x+area.width))
        			x_ticks.addElement (new Float((float)Math.pow (10,i)));
			}
		}
		else {
			float x1,x2;
     	 	x1 = x_value (area.x);
      		x2 = x_value (area.x+tick_spacing);
      		x1 = round (x1,x2-x1);
      		if (x_pixel(x1) <= area.x+area.width) x_ticks.addElement (new Float(x1));

      		x2 = x_value (x_pixel(x1)+tick_spacing);
      		x2 = round (x2,x1-x2);
      		while (x_pixel(x2) <= area.x+area.width)
      		{
       			x_ticks.addElement (new Float(x2));
        		x1 = x2;
        		x2 = x_value (x_pixel(x1)+tick_spacing);
        		x2 = round (x2,x1-x2);
      		}
		}

		y_ticks.setSize(0);
		if (y_scaling == log) {
			int i, j;
			int intlow  = (int)lg(y_value(area.height+area.y));
			int inthigh = (int)lg(y_value(area.y));
			for (i=intlow-1;i <= inthigh;i++) {
				int py = y_pixel((float)Math.pow (10,i));
				if(y_value(py) >= y_value(area.height+area.y) && y_value(py) <= y_value(area.y))
        			y_ticks.addElement (new Float((float)Math.pow (10,i)));
			}
		}
		else {
			float y1,y2;
      		y1 = y_value (area.y);
      		y2 = y_value (area.y+tick_spacing);
      		y1 = round (y1,y2-y1);
      		if (y_pixel(y1) <= area.y+area.height) y_ticks.addElement (new Float(y1));
      		y2 = y_value (y_pixel(y1)+tick_spacing);
      		y2 = round (y2,y1-y2);

      		while (y_pixel(y2) <= area.y+area.height)
      		{
        		y_ticks.addElement (new Float(y2));
        		y1 = y2;
        		y2 = y_value (y_pixel(y1)+tick_spacing);
        		y2 = round (y2,y1-y2);
      		}
		}
    }

    /** Draws a frame arround the plotting area, scaling ticks and labels and axis labels */

    private void draw_axes (Graphics g)
    {
	  g.setPaintMode();
	  g.setColor (background);
	  g.fillRect (area.x,area.y,area.width,area.height);

      g.setColor(getForeground());
	  g.drawRect (area.x,area.y,area.width,area.height); // Frame

	  FontMetrics font = g.getFontMetrics();

	  for (int i=0; i < x_ticks.size(); i++) // x axis ticks and labels
	  {
		float x = ((Float) x_ticks.elementAt(i)).floatValue();
		int px = x_pixel(x), py = area.y+area.height;
        g.drawLine (px,py,px,py-tick_length);
        int width = g.getFontMetrics().stringWidth (x+"");
        int height = g.getFontMetrics().getAscent();
        int space = g.getFontMetrics().getHeight()/2; // to avoid overlap with  y axis labels
        g.drawString (x+"",px-width/2,py+height+space);
		py = area.y;
        g.drawLine (px,py,px,py+tick_length);
	  }
	  for (int i=0; i < y_ticks.size(); i++) // y axis ticks and labels
	  {
		float y = ((Float) y_ticks.elementAt(i)).floatValue();
		int px = area.x, py = y_pixel(y);
        g.drawLine (px,py,px+tick_length,py);
        int width = g.getFontMetrics().stringWidth (y+"");
        int height = g.getFontMetrics().getAscent();
        g.drawString (y+"",px-width-2,py+height/2);
        px = area.x+area.width;
        g.drawLine (px,py,px-tick_length,py);
	  }
	  if (x_axis_label != null) // x axis description
	  {
        int s = font.getHeight(), h = font.getAscent(), w = font.stringWidth (x_axis_label);
        int x = area.x+area.width/2-w/2 , y = area.y+area.height+s/2+s+h;
        g.drawString (x_axis_label,x,y);
      }
	  if (y_axis_label != null) // y axis description
	  {
        int s = font.getHeight(), w = font.stringWidth (y_axis_label);
        int x = Math.max (area.x - w - 2,0), y = area.y - s/2;
//        g.drawString (y_axis_label,x+10,y);
		g.drawString (y_axis_label,1,y);
		g.drawString ("(g)",1,y+s);
      }
      if (title_label != null) // title description
	  {
        g.setColor(Color.blue);
        Font ftemp1 = getFont();
		Font ftemp2 = new Font("Serif", Font.BOLD, 14);
		g.setFont(ftemp2);
		FontMetrics font2 = getFontMetrics(ftemp2);
		int s = font2.getHeight(), h = font2.getAscent(), w = font2.stringWidth (title_label);
        int x = area.x+area.width/2-w/2 , y = s+2;
        g.drawString (title_label,x,y+5);
        g.setFont(ftemp1);
      	g.setColor(getForeground());
      }

//  This added here in this version
//  Add minor ticks if it's log mode
		if (y_scaling == log) {
			int i, j;
			int intlow  = (int)lg(y_value(area.height+area.y));
			int inthigh = (int)lg(y_value(area.y));
			for (i=intlow-1;i <= inthigh;i++)
				for(j=1; j<10; j++) {
					int py = y_pixel((float)j*(float)Math.pow (10,i));
					if(y_value(py) > y_value(area.height+area.y) && y_value(py) < y_value(area.y)) {
        				g.drawLine (area.x,py,area.x+minor_tick_length,py);
        				g.drawLine (area.x+area.width-minor_tick_length,py,area.x+area.width,py);
        			}
				}
		}
		if (x_scaling == log) {
			int i, j;
			int inthigh  = (int)lg(x_value(area.width+area.x));
			int intlow = (int)lg(x_value(area.x));
			for (i=intlow-1;i <= inthigh;i++)
				for(j=1; j<10; j++) {
					int px = x_pixel((float)j*(float)Math.pow (10,i));
					if(x_value(px) > x_value(area.x) && x_value(px) < x_value(area.x+area.width)) {
        				g.drawLine (px,area.y,px,area.y+minor_tick_length);
        				g.drawLine (px,area.y+area.height,px,area.y+area.height-minor_tick_length);
        			}
				}
		}
    }



    /** Draws the traces inside the "plotting area".
     * Side effect: sets the clipping mask to the size of the plotting area so
     * everything outside sould be drawn before this procedure is called. */

    private void plot_data (Graphics g)
    {
      g.setPaintMode();
	  g.clipRect (area.x,area.y,area.width,area.height);

	  for (int i=0; i < data.size(); i++)
	  {
        Vector trace = (Vector) data.elementAt (i);

        g.setColor ((Color) colors.elementAt (i % colors.size()));
        boolean join_points = ((Boolean) join.elementAt (i % join.size())).booleanValue();
        String marker = (String) markers.elementAt (i % markers.size());
        if (join_points == false && marker == null) marker = "dot";
        float xo = ((Float) x_offsets.elementAt (i % x_offsets.size())).floatValue();
        float yo = ((Float) y_offsets.elementAt (i % y_offsets.size())).floatValue();

	    Point p1 = null, p2;

	    if (join_points) for (int j=0; j < trace.size(); j++) // draw trace
	    {
          DataPoint p = (DataPoint) trace.elementAt(j);
	      p2 = pixel (p.x+xo,p.y+yo);
	      if (p1 == null) p1 = p2;
          g.drawLine (p1.x,p1.y,p2.x,p2.y);
          p1 = p2;
	    }

// Added in this version --- Draw upper and lower bounds as lines
// I added the int "sigmaFactor" for this to be more flexible


		if(sigmaFactor != 0) {
			float lny, upper, lower, factor;
			p1 = null;
			for (int j=0; j < trace.size(); j++) // draw upper bound
	    	{
        	  DataPoint p = (DataPoint) trace.elementAt(j);
        	  factor = (float)sigmaFactor * p.dy;
        	  lny = (float) Math.log((double) p.y);
        	  upper = (float) Math.exp((double) (lny + factor));
	    	  p2 = pixel (p.x+xo,upper+yo);
	    	  if (p1 == null) p1 = p2;
        	  g.drawLine (p1.x,p1.y,p2.x,p2.y);
        	  p1 = p2;
	  		}
	  		p1 = null;
	  		for (int j=0; j < trace.size(); j++) // draw lower bound
	    	{
        	  DataPoint p = (DataPoint) trace.elementAt(j);
        	  factor = (float)sigmaFactor * p.dy;
        	  lny = (float) Math.log((double) p.y);
        	  lower = (float) Math.exp((double) (lny - factor));
	    	  p2 = pixel (p.x+xo,lower+yo);
	    	  if (p1 == null) p1 = p2;
        	  g.drawLine (p1.x,p1.y,p2.x,p2.y);
        	  p1 = p2;
	  		}
	    }

/*	Took out the old error bar plotting
	    for (int j=0; j < trace.size(); j++) // draw error bars (before traces?)
	    {
          DataPoint p = (DataPoint) trace.elementAt(j);
          if (p.dx != 0)
          {
	        p1 = pixel (p.x-p.dx/2+xo,p.y+yo); p2 = pixel (p.x+p.dx/2+xo,p.y+yo);
            g.drawLine (p1.x,p1.y,p2.x,p2.y);
          }
          if (p.dy != 0)
          {
	        p1 = pixel (p.x+xo,p.y-p.dy/2+yo); p2 = pixel (p.x+xo,p.y+p.dy/2+yo);
            g.drawLine (p1.x,p1.y,p2.x,p2.y);
          }
	    }
*/
	    if (marker != null) for (int j=0; j < trace.size(); j++) // draw markers
	    {
          DataPoint p = (DataPoint) trace.elementAt(j);
	      p1 = pixel (p.x+xo,p.y+yo);
	      draw_marker (marker, p1.x, p1.y,g);
	    }
	  }
    }

    // Draw the markers if desired
    private void draw_marker (String name, int x, int y, Graphics g)
    {
      if (name.equals ("none")) return;

      if (name.equals ("dot")) g.drawLine (x,y,x,y);
      else if (name.equals ("bar")) g.drawLine (x,y-symbolSize/2,x,y+symbolSize/2); // short vertical line
      else if (name.equals ("cross")) { g.drawLine (x,y-symbolSize/2,x,y+symbolSize/2); g.drawLine (x-symbolSize/2,y,x+symbolSize/2,y); }
      else if (name.equals ("triangle"))
      {
        int[] px = {x-symbolSize/2,x+symbolSize/2,x}, py = {y-(int)(symbolSize*0.432),y-(int)(symbolSize*0.432),y+(int)(symbolSize*0.432)};
        g.drawPolygon (px,py,3);
      }
      else if (name.equals ("filled_triangle"))
      {
        int[] px = {x-symbolSize/2,x+symbolSize/2,x}, py = {y-(int)(symbolSize*0.432),y-(int)(symbolSize*0.432),y+(int)(symbolSize*0.432)};
        g.fillPolygon (px,py,3);
      }
      else if (name.equals ("square")) g.drawRect (x-symbolSize/2,y-symbolSize/2,symbolSize,symbolSize);
      else if (name.equals ("filled_square")) g.fillRect (x-symbolSize/2,y-symbolSize/2,symbolSize,symbolSize);
      else if (name.equals ("circle")) g.drawOval (x-symbolSize/2,y-symbolSize/2,symbolSize,symbolSize);
      else if (name.equals ("filled_circle")) g.fillOval (x-symbolSize/2,y-symbolSize/2,symbolSize,symbolSize);
      else g.drawLine (x,y,x,y); // make a dot if name not understood
    }

    /** Convert data coordinates to screen coordinates (0,0 is top left corner) */

    private Point pixel (float x,float y) { return new Point (x_pixel(x),y_pixel(y)); }

    private int x_pixel (float x)
    {
      float fraction = (x_scale(x)-x_scale(xmin))/(x_scale(xmax)-x_scale(xmin));
      return (area.x + Math.round (fraction*area.width));
    }

    private int y_pixel (float y)
    {
      float fraction = (y_scale(y)-y_scale(ymin))/(y_scale(ymax)-y_scale(ymin));
      return (area.y + area.height - Math.round (fraction*area.height));
    }

    /** x axis scaling function, log or linear */

    private float x_scale (float x)
    {
      if (x_scaling == log) { if(x>0) return lg(x); else return Float.NaN; }
      else return x;
    }

    private float y_scale (float y)
    {
      if (y_scaling == log) { if(y>0) return lg(y); else return Float.NaN; }
      else return y;
    }

    /** Convert screen coordinates to data coordinates */

    private float x_value (int x)
    {
      float fraction = (float) (x-area.x) / (float) area.width;
      return inv_x_scale (x_scale(xmin) + (x_scale(xmax)-x_scale(xmin)) * fraction);
    }

    private float y_value (int y)
    {
      float fraction = (float) (area.y+area.height-y) / (float) area.height;
      return inv_y_scale (y_scale(ymin) + (y_scale(ymax)-y_scale(ymin)) * fraction);
    }

    /** inverse function of x axis scaling function, e.g. needed for zoom */

    private float inv_x_scale (float x)
    {
      if (x_scaling == log) return (float) Math.pow (10,x);
      else return x;
    }

    private float inv_y_scale (float y)
    {
      if (y_scaling == log) return (float) Math.pow (10,y);
      else return y;
    }

    /** used for axis labeling */

        private float round (float v, float r)
    {
      double value=v, range=r, step;
      if (r == 0) return v;
      boolean round_up = true; if (range < 0) round_up = false;
      range = Math.abs(range);
      step = Math.pow (10,Math.floor((double)lg((float)range)));
      if (step*5 <= range) step*=5;
      if (step*2 <= range) step*=2;
      if (round_up) value = Math.ceil (value/step) * step;
      else value = Math.floor (value/step) * step;

      return (float)value;
    }

    /** Decadic logarithm, used for rounding and log scale */

    private float lg (float x)
    {
      return (float) (Math.log(x)/Math.log(10.0));
    }
}

