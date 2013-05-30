package scratch.kevin;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DefaultXY_DataSet;
import org.opensha.commons.data.function.XY_DataSet;
import org.opensha.commons.data.function.XY_DataSetList;
import org.opensha.commons.gui.plot.GraphPanel;
import org.opensha.commons.gui.plot.GraphPanelAPI;
import org.opensha.sha.gui.infoTools.PlotControllerAPI;

public class ScatterGraphTest implements GraphPanelAPI, PlotControllerAPI {
	
	private GraphPanel gp;
	private Random r = new Random();
	
	public ScatterGraphTest() {
		gp = new GraphPanel(this);
		
		gp.drawGraphPanel("X", "Y", getFuncList(), false, false, true, "Title", this);
		this.gp.setVisible(true);
		
		this.gp.togglePlot(null);
		
		this.gp.validate();
		this.gp.repaint();
		
		JFrame frame = new JFrame();
		frame.setSize(600, 600);
		frame.setContentPane(gp);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		frame.validate();
	}
	
	private ArrayList<XY_DataSet> getFuncList() {
		ArrayList<XY_DataSet> funcs = new ArrayList<XY_DataSet>();
		
		ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
		DefaultXY_DataSet scatter = new DefaultXY_DataSet();
		for (double x=0; x<getUserMaxX(); x++) {
			double y = x * 0.8;
			y += r.nextDouble() - 0.5d;
			System.out.println("Adding " + x + ", " + y);
			func.set(x, y);
			
			scatter.set(x, y + r.nextDouble()*0.5);
			scatter.set(x, y - r.nextDouble()*0.5);
		}
		
		funcs.add(func);
		funcs.add(scatter);
		
		return funcs;
	}
	
	public static void main(String[] args) {
		new ScatterGraphTest();
	}

	@Override
	public double getUserMaxX() {
		return 10;
	}

	@Override
	public double getUserMaxY() {
		return 10;
	}

	@Override
	public double getUserMinX() {
		return 0;
	}

	@Override
	public double getUserMinY() {
		return 0;
	}

	@Override
	public int getAxisLabelFontSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPlotLabelFontSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTickLabelFontSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setXLog(boolean flag) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setYLog(boolean flag) {
		// TODO Auto-generated method stub
		
	}

}
