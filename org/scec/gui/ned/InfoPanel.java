package org.scec.gui.ned;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * <b>Title:</b> InfoPanel<br>
 * <b>Description:</b> This is the Information Canvas Class<br>
 * <b>Copyright:</b>    Copyright (c) 2001<br>
 * <b>Company:</b>      <br>
 * @author Ned Field
 * @version 1.0
 */

public class InfoPanel extends Canvas {

	String 	message = new String();
	Vector  traceInfo, traceColors;
	Rectangle area;
	Color messageColor;

	public InfoPanel() {
		setMessage(null);
	}

	public void setMessage(String s) {
		if (s == null) {
			message = new String("****  Current Plots:  ****");
			messageColor = Color.white;
		}
		else {
			message = new String("****  "+s+"  ****");
			messageColor = Color.red;
		}
		repaint();
	}

	public void setWarning(String s) {
		message = new String("****  "+s+"  ****");
		messageColor = Color.green;
		repaint();
	}

	public void setTraceInfo(Vector trInfo, Vector colors) {
		traceInfo = trInfo;
		traceColors = colors;

		repaint();

		}

	public void paint (Graphics g) {

		area = new Rectangle (getSize());
		FontMetrics font = g.getFontMetrics();
	  	int s = font.getHeight(), h = font.getAscent();  // Ascent is from base to top of most characters

	  	// Plot message at the top:
	  	int w = font.stringWidth (message);
        int x = area.x+area.width/2-w/2 , y = s;
        g.setColor(messageColor);
        g.drawString (message,x,y);
        g.drawLine((area.width-w)/2,s+h/3,w+(area.width-w)/2,s+h/3);

        // Then plot out the trace info
        if(traceInfo.size() != 0) {
			int i = 0;
			w = font.stringWidth ((String)traceInfo.elementAt(i));
			x = area.x+area.width/2-w/2;
			while(i<traceInfo.size()) {
				g.setColor((Color) traceColors.elementAt(i));
				g.drawString ((String) traceInfo.elementAt(i),x,(traceInfo.size()-i+1)*s+h);
				i++;
			}
		}
//		g.drawRect(area.x,area.y,area.width-1,area.height-1);
	}
}

