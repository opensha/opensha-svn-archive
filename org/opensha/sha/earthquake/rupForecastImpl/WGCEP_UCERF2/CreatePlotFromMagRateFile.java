/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF2;

import java.awt.Color;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

/**
 * @author vipingupta
 *
 */
public class CreatePlotFromMagRateFile implements GraphWindowAPI {
	
	private final static String X_AXIS_LABEL = "Index";
	private final static String Y_AXIS_LABEL = "Rate";
	private final static String PLOT_LABEL = "Rates";
	private ArrayList funcs;
	
	private final PlotCurveCharacterstics PLOT_CHAR1 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLUE, 2);
	private final PlotCurveCharacterstics PLOT_CHAR2 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.DARK_GRAY, 2);
	private final PlotCurveCharacterstics PLOT_CHAR3 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.GREEN, 2);
	private final PlotCurveCharacterstics PLOT_CHAR4 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.MAGENTA, 2);
	private final PlotCurveCharacterstics PLOT_CHAR5 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.PINK, 2);
	private final PlotCurveCharacterstics PLOT_CHAR6 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.BLACK, 2);
	private final PlotCurveCharacterstics PLOT_CHAR7 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.SOLID_LINE,
		      Color.RED, 2);
	//private final PlotCurveCharacterstics PLOT_CHAR9 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.DASHED_LINE,
		//      Color.RED, 5);
	//private final PlotCurveCharacterstics PLOT_CHAR10 = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.CROSS_SYMBOLS,
		//      Color.RED, 5);

	
	public CreatePlotFromMagRateFile(ArrayList funcList) {
		funcs = funcList;
	}
	
	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getCurveFunctionList()
	 */
	public ArrayList getCurveFunctionList() {
		return funcs;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXLog()
	 */
	public boolean getXLog() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYLog()
	 */
	public boolean getYLog() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getXAxisLabel()
	 */
	public String getXAxisLabel() {
		return X_AXIS_LABEL;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getYAxisLabel()
	 */
	public String getYAxisLabel() {
		return Y_AXIS_LABEL;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getPlottingFeatures()
	 */
	public ArrayList getPlottingFeatures() {
		 ArrayList list = new ArrayList();
		 list.add(this.PLOT_CHAR1);
		 list.add(this.PLOT_CHAR2);
		 list.add(this.PLOT_CHAR3);
		 list.add(this.PLOT_CHAR4);
		 list.add(this.PLOT_CHAR5);
		 list.add(this.PLOT_CHAR6);
		 list.add(this.PLOT_CHAR7);
		 list.add(this.PLOT_CHAR1);
		 list.add(this.PLOT_CHAR2);
		 list.add(this.PLOT_CHAR3);
		 list.add(this.PLOT_CHAR4);
		 list.add(this.PLOT_CHAR5);
		 list.add(this.PLOT_CHAR6);
		 list.add(this.PLOT_CHAR7);
		 return list;
	}
	

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#isCustomAxis()
	 */
	public boolean isCustomAxis() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinX()
	 */
	public double getMinX() {
		return 5.0;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public double getMaxX() {
		return 9.255;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public double getMinY() {
		return 1e-4;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public double getMaxY() {
		return 10;
		//throw new UnsupportedOperationException("Method not implemented yet");
	}
	
	
	public static void main(String args[]) {
	try {
		String[] names = {"A-Priori Rates", "Char Rate", 
				"Ellsworth-A_Uniform/Boxcar", "Ellsworth-A_WGCEP-2002", "Ellsworth-A_Tapered",
				"Ellsworth-B_Uniform/Boxcar", "Ellsworth-B_WGCEP-2002", "Ellsworth-B_Tapered",
				"Hanks & Bakun (2002)_Uniform/Boxcar", "Hanks & Bakun (2002)_WGCEP-2002", "Hanks & Bakun (2002)_Tapered",
				"Somerville (2006)_Uniform/Boxcar", "Somerville (2006)_WGCEP-2002", "Somerville (2006)_Tapered"};
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream("EqkRateModel2_v2.xls"));
		HSSFWorkbook wb = new HSSFWorkbook(fs);
		String[] models = { "Min Rate", "Max Rate", "Geological Insight"};
		for(int i=0; i<wb.getNumberOfSheets(); ++i) {
			HSSFSheet sheet = wb.getSheetAt(i);
			String sheetName = wb.getSheetName(i);
			int lastIndex = sheet.getLastRowNum();
			int r = 4;
			int count=0;
			// read data for each row
			for(; r<=lastIndex; ++r) {
				double j=-1.0;
				String modelType = models[count++];
				ArrayList funcList = new ArrayList();
				for(int k=0; k<14; ++k) {
					ArbitrarilyDiscretizedFunc func = new ArbitrarilyDiscretizedFunc();
					func.setName(names[k]);
					funcList.add(func);
				}
				while(true) {
					++j;
					HSSFRow row = sheet.getRow(r);
					HSSFCell cell = row.getCell( (short) 0);
					// rup name
					String rupName = cell.getStringCellValue().trim();
					if(rupName.equalsIgnoreCase("Totals")) {
						r= r+6;
						GraphWindow graphWindow= new GraphWindow(new CreatePlotFromMagRateFile(funcList));
						graphWindow.setPlotLabel(PLOT_LABEL);
						graphWindow.plotGraphUsingPlotPreferences();
						graphWindow.setTitle(sheetName+" "+modelType);
						graphWindow.pack();
						graphWindow.setVisible(true);
						break;
					}
					System.out.println(r);
					((ArbitrarilyDiscretizedFunc)funcList.get(0)).set(j, row.getCell( (short) 1).getNumericCellValue());
					((ArbitrarilyDiscretizedFunc)funcList.get(1)).set(j, row.getCell( (short) 3).getNumericCellValue());
					((ArbitrarilyDiscretizedFunc)funcList.get(2)).set(j, row.getCell( (short) 5).getNumericCellValue());
					((ArbitrarilyDiscretizedFunc)funcList.get(3)).set(j, row.getCell( (short) 6).getNumericCellValue());
					((ArbitrarilyDiscretizedFunc)funcList.get(4)).set(j, row.getCell( (short) 7).getNumericCellValue());
					((ArbitrarilyDiscretizedFunc)funcList.get(5)).set(j, row.getCell( (short) 9).getNumericCellValue());
					((ArbitrarilyDiscretizedFunc)funcList.get(6)).set(j, row.getCell( (short) 10).getNumericCellValue());
					((ArbitrarilyDiscretizedFunc)funcList.get(7)).set(j, row.getCell( (short) 11).getNumericCellValue());
					((ArbitrarilyDiscretizedFunc)funcList.get(8)).set(j, row.getCell( (short) 13).getNumericCellValue());
					((ArbitrarilyDiscretizedFunc)funcList.get(9)).set(j, row.getCell( (short) 14).getNumericCellValue());
					((ArbitrarilyDiscretizedFunc)funcList.get(10)).set(j, row.getCell( (short) 15).getNumericCellValue());
					((ArbitrarilyDiscretizedFunc)funcList.get(11)).set(j, row.getCell( (short) 17).getNumericCellValue());
					((ArbitrarilyDiscretizedFunc)funcList.get(12)).set(j, row.getCell( (short) 18).getNumericCellValue());
					((ArbitrarilyDiscretizedFunc)funcList.get(13)).set(j, row.getCell( (short) 19).getNumericCellValue());
					++r;
				}
				
				
			}
		}
	}catch(Exception e) {
		e.printStackTrace();
	}
	}
}
