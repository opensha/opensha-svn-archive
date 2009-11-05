package org.opensha.sha.cybershake.bombay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartUtilities;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFuncAPI;
import org.opensha.sha.cybershake.db.CybershakeIM;
import org.opensha.sha.cybershake.db.Cybershake_OpenSHA_DBApplication;
import org.opensha.sha.cybershake.db.DBAccess;
import org.opensha.sha.cybershake.db.HazardCurveComputation;
import org.opensha.sha.cybershake.db.PeakAmplitudesFromDB;
import org.opensha.sha.cybershake.plot.HazardCurvePlotCharacteristics;
import org.opensha.sha.gui.controls.CyberShakePlotFromDBControlPanel;
import org.opensha.sha.gui.infoTools.GraphPanel;
import org.opensha.sha.gui.infoTools.GraphPanelAPI;
import org.opensha.sha.gui.infoTools.PlotControllerAPI;

public class SingleCurveDebugTest implements GraphPanelAPI, PlotControllerAPI {
	
	GraphPanel gp;
	
	public SingleCurveDebugTest() {
		gp = new GraphPanel(this);
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		DBAccess db = Cybershake_OpenSHA_DBApplication.db;
		
		double increaseMultFactor = 1000;
		
		Div365ProbModifier div365 = new Div365ProbModifier();
		BombayBeachHazardCurveCalc bombay = new BombayBeachHazardCurveCalc(db, increaseMultFactor);
		
		HazardCurveComputation calc = new HazardCurveComputation(db);
		
		ArbitrarilyDiscretizedFunc func = CyberShakePlotFromDBControlPanel.createUSGS_PGA_Function();
		
		ArrayList<Double> imlVals = new ArrayList<Double>();
		
		for (int i=0; i<func.getNum(); i++) {
			imlVals.add(func.getX(i));
		}
		
		int runID = 215;
		
		CybershakeIM imType = new PeakAmplitudesFromDB(db).getIMForPeriod(3d, runID);
		
		calc.setRupProbModifier(div365);
		DiscretizedFuncAPI origCurve = calc.computeHazardCurve(imlVals, 215, imType);
		calc.setRupProbModifier(bombay);
		DiscretizedFuncAPI modCurve = calc.computeHazardCurve(imlVals, 215, imType);
		
		SingleCurveDebugTest plot = new SingleCurveDebugTest();
		
		plot.plot(origCurve, modCurve);
	}
	
	public void plot(DiscretizedFuncAPI origCurve, DiscretizedFuncAPI modCurve) throws IOException {
		ArrayList<DiscretizedFuncAPI> curves = new ArrayList<DiscretizedFuncAPI>();
		curves.add(origCurve);
		curves.add(modCurve);
		
		boolean xLog = true;
		boolean yLog = true;
		
		boolean customAxis = true;
		
		String title = "CyberShake Curves";
		
		this.gp.drawGraphPanel("3s SA", "Probability Rate (1/yr)", curves, xLog, yLog, customAxis, title, this);
		this.gp.setVisible(true);
		
		this.gp.togglePlot(null);
		
		this.gp.validate();
		this.gp.repaint();
		
		String outFile = "/tmp/test_curves.png";
		System.out.println("Saving PNG to: " + outFile);
		ChartUtilities.saveChartAsPNG(new File(outFile), gp.getCartPanel().getChart(), 600, 500);
	}

	public double getMaxX() {
		return 2;
	}

	public double getMaxY() {
		return 1;
	}

	public double getMinX() {
		return 0;
	}

	public double getMinY() {
		return Double.parseDouble("1.0E-6");
	}

	public int getAxisLabelFontSize() {
		return 12;
	}

	public int getPlotLabelFontSize() {
		return 14;
	}

	public int getTickLabelFontSize() {
		return 12;
	}

	public void setXLog(boolean flag) {
		// TODO Auto-generated method stub
		
	}

	public void setYLog(boolean flag) {
		// TODO Auto-generated method stub
		
	}

}
