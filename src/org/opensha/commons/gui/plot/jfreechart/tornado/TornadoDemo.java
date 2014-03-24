package org.opensha.commons.gui.plot.jfreechart.tornado;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.opensha.commons.gui.plot.GraphWindow;
import org.opensha.commons.gui.plot.PlotSpec;

public class TornadoDemo {

	public static void main(String[] args) throws IOException {
		TornadoDiagram t = new TornadoDiagram("Test Tornado", "X", "Category", 1d);
		
		t.addTornadoValue("Category 1", "C1", 0.5+Math.random());
		t.addTornadoValue("Category 1", "C2", 0.5+Math.random());
		t.addTornadoValue("Category 1", "C3", 0.5+Math.random());
		
		t.addTornadoValue("Category 2", "C1", 1d + 0.8*(Math.random()-0.5));
		t.addTornadoValue("Category 2", "C2", 1d + 0.8*(Math.random()-0.5));
		t.addTornadoValue("Category 2", "C3", 1d + 0.8*(Math.random()-0.5));
		
		t.addTornadoValue("Category 3", "C1", 1d + 0.3*(Math.random()-0.5));
		t.addTornadoValue("Category 3", "C2", 1d + 0.3*(Math.random()-0.5));
		t.addTornadoValue("Category 3", "C3", 1d + 0.3*(Math.random()-0.5));
		
		GraphWindow gw = t.displayPlot();
		gw.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		t.getHeadlessPlot(500, 400).saveAsPNG(new File("/tmp/tornado.png").getAbsolutePath());
	}

}
