/**
 * 
 */
package org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_1.gui;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.opensha.data.function.EvenlyDiscretizedFunc;
import org.opensha.sha.gui.controls.PlotColorAndLineTypeSelectorControlPanel;
import org.opensha.sha.gui.infoTools.GraphWindow;
import org.opensha.sha.gui.infoTools.GraphWindowAPI;
import org.opensha.sha.gui.infoTools.PlotCurveCharacterstics;

/**
 * @author vipingupta
 *
 */
public class CreateHistogramsFromSegRecurIntvFile implements GraphWindowAPI {
	private final static String X_AXIS_LABEL = "Ratio of Mean Recur Intv and Calculated Recur Intv";
	private final static String Y_AXIS_LABEL = "Count";
	private final static String PLOT_LABEL = "Recurrence Interval Ratio";
	private ArrayList funcs;
	private final static  String[] names = {"Mean Recurrence Interval", "Min Recurrence Interval", 
		"Max Recurrence Interval", 
		"Characteristic", 
		"Ellsworth-A_UniformBoxcar", "Ellsworth-A_WGCEP-2002", "Ellsworth-A_Tapered",
		"Ellsworth-B_UniformBoxcar", "Ellsworth-B_WGCEP-2002", "Ellsworth-B_Tapered",
		"Hanks & Bakun (2002)_UniformBoxcar", "Hanks & Bakun (2002)_WGCEP-2002", "Hanks & Bakun (2002)_Tapered",
		"Somerville (2006)_UniformBoxcar", "Somerville (2006)_WGCEP-2002", "Somerville (2006)_Tapered"};

	private final PlotCurveCharacterstics PLOT_HISTOGRAM = new PlotCurveCharacterstics(PlotColorAndLineTypeSelectorControlPanel.HISTOGRAM,
		      new Color(0,0,0), 2); // black

	
	public CreateHistogramsFromSegRecurIntvFile(ArrayList funcList) {
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
		 list.add(this.PLOT_HISTOGRAM);
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
		//return 5.0;
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxX()
	 */
	public double getMaxX() {
		//return 9.255;
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMinY()
	 */
	public double getMinY() {
		//return 1e-4;
		throw new UnsupportedOperationException("Method not implemented yet");
	}

	/* (non-Javadoc)
	 * @see org.opensha.sha.gui.infoTools.GraphWindowAPI#getMaxY()
	 */
	public double getMaxY() {
		//return 10;
		throw new UnsupportedOperationException("Method not implemented yet");
	}
	
	/**
	 * Create the histograms from segment recurrence interval file. 
	 * It creates a plot for each slip Model and Mag Area Relationship combination.
	 * Plot can be created for 1 segment or for all segments.
	 * It creates plots and saves PDFs in A_FaultRupRatesPlots_2_1 subdirectory in masterDirectory
	 * @param masterDirName MasterDirectoty where A_FaultRupRatesPlots_2_1 will be created
	 * @param excelSheetName Absolute pathname to excel file
	 */
	public static void createHistogramPlots(String masterDirName, String excelSheetName, String segName) {
		try {
			// directory to save the PDF files. Directory will be created if it does not exist already
			String dirName = masterDirName+"/A_FaultSegRecurIntvHistograms_2_1/";
			File file = new File(dirName);
			if(!file.isDirectory()) { // create directory if it does not exist already
				file.mkdir();
			}
			//System.out.println(excelSheetName);
			ArrayList funcList = new ArrayList();
			for(int k=3; k<names.length; ++k) {
				EvenlyDiscretizedFunc func = new EvenlyDiscretizedFunc(0.20, 2.5, 24);
				func.setTolerance(func.getDelta());
				func.setName(names[k]);
				funcList.add(func);
			}
			
			// read the recurrence interval file
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(excelSheetName));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			double ratio;
			int xIndex;
			for(int i=0; i<wb.getNumberOfSheets(); ++i) {
				HSSFSheet sheet = wb.getSheetAt(i);
				int lastIndex = sheet.getLastRowNum();
				int r = 3;
				// read data for each row
				for(; r<=lastIndex; ++r) {
					HSSFRow row = sheet.getRow(r);
					HSSFCell cell = null;
					String rupName ="";
					if(row!=null)  cell = row.getCell( (short) 0);
					// segment name
					if(cell!=null) rupName = cell.getStringCellValue().trim();
					if(segName!=null && !rupName.equalsIgnoreCase(segName)) continue;
					if(row==null || cell==null || 
							cell.getCellType()==HSSFCell.CELL_TYPE_BLANK || rupName.equalsIgnoreCase("")) {
						r=r+4;
						continue;
					}
					double mean = Double.NaN;
					cell = row.getCell((short)1);
					if(cell == null) continue;
					else mean = cell.getNumericCellValue();
					for(int col=4; col<=16; ++col) {
						ratio = row.getCell( (short) col).getNumericCellValue()/mean;	
						//System.out.println(rupName+","+mean+","+ratio);
						EvenlyDiscretizedFunc func = (EvenlyDiscretizedFunc)funcList.get(col-4);
						xIndex = func.getXIndex(ratio);
						func.add(xIndex, 1.0);
					}
				}
			}
			for(int k=3, i=0; k<names.length; ++k, ++i) {
				ArrayList list = new ArrayList();
				list.add(funcList.get(i));
				CreateHistogramsFromSegRecurIntvFile plot = new CreateHistogramsFromSegRecurIntvFile(list);
				GraphWindow graphWindow= new GraphWindow(plot);
				graphWindow.setPlotLabel(PLOT_LABEL);
				graphWindow.plotGraphUsingPlotPreferences();
				graphWindow.setTitle(names[k]);
				graphWindow.pack();
				graphWindow.setVisible(true);
				//graphWindow.setAxisRange(-0.5,graphWindow.getMaxX() , graphWindow.getMinY(), graphWindow.getMaxY());
				graphWindow.saveAsPDF(dirName+"/"+names[k]+".pdf");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
