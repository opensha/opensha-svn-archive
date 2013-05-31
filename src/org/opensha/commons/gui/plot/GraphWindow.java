package org.opensha.commons.gui.plot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.jfree.data.Range;
import org.opensha.commons.util.FileUtils;

import com.google.common.collect.Lists;

public class GraphWindow extends JFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private GraphWidget widget;
	
	protected JMenuBar menuBar = new JMenuBar();
	protected JMenu fileMenu = new JMenu();

	protected JMenuItem fileExitMenu = new JMenuItem();
	protected JMenuItem fileSaveMenu = new JMenuItem();
	protected JMenuItem filePrintMenu = new JCheckBoxMenuItem();
	protected JToolBar jToolBar = new JToolBar();

	protected JButton closeButton = new JButton();
	protected ImageIcon closeFileImage = new ImageIcon(FileUtils.loadImage("icons/closeFile.png"));

	protected JButton printButton = new JButton();
	protected ImageIcon printFileImage = new ImageIcon(FileUtils.loadImage("icons/printFile.jpg"));

	protected JButton saveButton = new JButton();
	protected ImageIcon saveFileImage = new ImageIcon(FileUtils.loadImage("icons/saveFile.jpg"));
	
	protected static int windowNumber = 1;
	protected final static String TITLE = "Plot Window - ";
	
	public GraphWindow(PlotElement elem, String plotTitle) {
		this(Lists.newArrayList(elem), plotTitle);
	}
	
	public GraphWindow(List<? extends PlotElement> elems, String plotTitle) {
		this(elems, plotTitle, generateDefaultChars(elems));
	}
	
	public GraphWindow(List<? extends PlotElement> elems, String plotTitle,
			List<PlotCurveCharacterstics> chars) {
		this(new PlotSpec(elems, chars, plotTitle, null, null));
	}
	
	public GraphWindow(List<? extends PlotElement> elems, String plotTitle,
			List<PlotCurveCharacterstics> chars, boolean display) {
		this(new GraphWidget(new PlotSpec(elems, chars, plotTitle, null, null)), display);
	}
	
	public GraphWindow(PlotSpec spec) {
		this(new GraphWidget(spec));
	}
	
	public GraphWindow(PlotSpec plotSpec, PlotPreferences plotPrefs, boolean xLog, boolean yLog, Range xRange, Range yRange) {
		this(new GraphWidget(plotSpec, plotPrefs, xLog, yLog, xRange, yRange));
	}
	
	public GraphWindow(GraphWidget widget) {
		this(widget, true);
	}
	
	public GraphWindow(GraphWidget widget, boolean display) {
		this.widget = widget;
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		fileMenu.setText("File");
		fileExitMenu.setText("Exit");
		fileSaveMenu.setText("Save");
		filePrintMenu.setText("Print");

		fileExitMenu.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileExitMenu_actionPerformed(e);
			}
		});

		fileSaveMenu.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileSaveMenu_actionPerformed(e);
			}
		});

		filePrintMenu.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filePrintMenu_actionPerformed(e);
			}
		});

		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				closeButton_actionPerformed(actionEvent);
			}
		});
		printButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				printButton_actionPerformed(actionEvent);
			}
		});
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				saveButton_actionPerformed(actionEvent);
			}
		});

		menuBar.add(fileMenu);
		fileMenu.add(fileSaveMenu);
		fileMenu.add(filePrintMenu);
		fileMenu.add(fileExitMenu);

		setJMenuBar(menuBar);
		closeButton.setIcon(closeFileImage);
		closeButton.setToolTipText("Close Window");
		Dimension d = closeButton.getSize();
		jToolBar.add(closeButton);
		printButton.setIcon(printFileImage);
		printButton.setToolTipText("Print Graph");
		printButton.setSize(d);
		jToolBar.add(printButton);
		saveButton.setIcon(saveFileImage);
		saveButton.setToolTipText("Save Graph as image");
		saveButton.setSize(d);
		jToolBar.add(saveButton);
		jToolBar.setFloatable(false);

		mainPanel.add(jToolBar, BorderLayout.NORTH);
		mainPanel.add(widget, BorderLayout.CENTER);
		
		this.setContentPane(mainPanel);
		
		// increasing the window number corresponding to the new window.
		this.setTitle(TITLE + windowNumber++);
		
		setSize(700, 800);
		
		if (display)
			this.setVisible(true);
	}
	
	public void setX_AxisLabel(String xAxisLabel) {
		widget.setXAxisLabel(xAxisLabel);
	}
	
	public void setY_AxisLabel(String yAxisLabel) {
		widget.setYAxisLabel(yAxisLabel);
	}
	
	public GraphWidget getGraphWidget() {
		return widget;
	}
	
	private static final PlotCurveCharacterstics PLOT_CHAR1 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.BLUE);
	private static final PlotCurveCharacterstics PLOT_CHAR2 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.BLACK);
	private static final PlotCurveCharacterstics PLOT_CHAR3 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.GREEN);
	private static final PlotCurveCharacterstics PLOT_CHAR4 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.MAGENTA);
	private static final PlotCurveCharacterstics PLOT_CHAR5 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.PINK);
	private static final PlotCurveCharacterstics PLOT_CHAR6 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.LIGHT_GRAY);
	private static final PlotCurveCharacterstics PLOT_CHAR7 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.RED);
	private static final PlotCurveCharacterstics PLOT_CHAR8 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.ORANGE);
	private static final PlotCurveCharacterstics PLOT_CHAR9 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.CYAN);
	private static final PlotCurveCharacterstics PLOT_CHAR10 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.DARK_GRAY);
	private static final PlotCurveCharacterstics PLOT_CHAR11 = new PlotCurveCharacterstics(PlotLineType.SOLID,
			2f, Color.GRAY);
	
	protected static ArrayList<PlotCurveCharacterstics> generateDefaultChars(List<? extends PlotElement> elems) {
		ArrayList<PlotCurveCharacterstics> list = new ArrayList<PlotCurveCharacterstics>();
		list.add(PLOT_CHAR1);
		list.add(PLOT_CHAR2);
		list.add(PLOT_CHAR3);
		list.add(PLOT_CHAR4);
		list.add(PLOT_CHAR5);
		list.add(PLOT_CHAR6);
		list.add(PLOT_CHAR7);
		list.add(PLOT_CHAR8);
		list.add(PLOT_CHAR9);
		list.add(PLOT_CHAR10);
		list.add(PLOT_CHAR11);
		
		if (elems == null)
			return list;
		
		int numChars = list.size();
		ArrayList<PlotCurveCharacterstics> plotChars = new ArrayList<PlotCurveCharacterstics>();
		for(int i=0; i<elems.size(); ++i)
			plotChars.add(list.get(i%numChars));
		return plotChars;
	}
	
	public static List<Color> generateDefaultColors() {
		ArrayList<Color> colors = new ArrayList<Color>();
		for (PlotCurveCharacterstics pchar : generateDefaultChars(null))
			colors.add(pchar.getColor());
		return colors;
	}
	
	/**
	 * File | Exit action performed.
	 *
	 * @param actionEvent ActionEvent
	 */
	protected  void fileExitMenu_actionPerformed(ActionEvent actionEvent) {
		this.dispose();
	}

	/**
	 * File | Exit action performed.
	 *
	 * @param actionEvent ActionEvent
	 */
	protected  void fileSaveMenu_actionPerformed(ActionEvent actionEvent) {
		try {
			widget.save();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Save File Error",
					JOptionPane.OK_OPTION);
			return;
		}
	}

	/**
	 * File | Exit action performed.
	 *
	 * @param actionEvent ActionEvent
	 */
	protected  void filePrintMenu_actionPerformed(ActionEvent actionEvent) {
		widget.print();
	}

	protected  void closeButton_actionPerformed(ActionEvent actionEvent) {
		this.dispose();
	}

	protected  void printButton_actionPerformed(ActionEvent actionEvent) {
		widget.print();
	}

	protected  void saveButton_actionPerformed(ActionEvent actionEvent) {
		try {
			widget.save();
		}
		catch (IOException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Save File Error",
					JOptionPane.OK_OPTION);
			return;
		}
	}

	public void saveAsPDF(String fileName) throws IOException {
		widget.saveAsPDF(fileName);
	}

	public void saveAsPNG(String fileName) throws IOException {
		widget.saveAsPNG(fileName);
	}
	
	/**
	 * Save a txt file
	 * @param fileName
	 * @throws IOException 
	 */
	public void saveAsTXT(String fileName) throws IOException {
		widget.saveAsTXT(fileName);
	}

	public void setXLog(boolean xLog) {
		widget.setX_Log(xLog);
	}

	public void setYLog(boolean yLog) {
		widget.setY_Log(yLog);
	}

	public void setAxisRange(double xMin, double xMax, double yMin, double yMax) {
		widget.setAxisRange(xMin, xMax, yMin, yMax);
	}

	public void setAxisRange(Range xRange, Range yRange) {
		widget.setAxisRange(xRange, yRange);
	}

	public void setPlotSpec(PlotSpec plotSpec) {
		widget.setPlotSpec(plotSpec);
	}

	public void setPlottingFeatures(
			List<PlotCurveCharacterstics> curveCharacteristics) {
		widget.setPlottingFeatures(curveCharacteristics);
	}

	public void togglePlot() {
		widget.togglePlot();
	}

	public void setPlotLabel(String plotTitle) {
		widget.setPlotLabel(plotTitle);
	}

	public void setPlotLabelFontSize(int fontSize) {
		widget.setPlotLabelFontSize(fontSize);
	}

	public void setTickLabelFontSize(int fontSize) {
		widget.setTickLabelFontSize(fontSize);
	}

	public void setAxisLabelFontSize(int fontSize) {
		widget.setAxisLabelFontSize(fontSize);
	}

	public void setX_AxisRange(double minX, double maxX) {
		widget.setX_AxisRange(minX, maxX);
	}

	public void setX_AxisRange(Range xRange) {
		widget.setX_AxisRange(xRange);
	}
	
	public Range getX_AxisRange() {
		return widget.getX_AxisRange();
	}

	public void setY_AxisRange(double minY, double maxY) {
		widget.setY_AxisRange(minY, maxY);
	}

	public void setY_AxisRange(Range yRange) {
		widget.setY_AxisRange(yRange);
	}
	
	public Range getY_AxisRange() {
		return widget.getY_AxisRange();
	}

	public void setAllLineTypes(PlotLineType line, PlotSymbol symbol) {
		for (PlotCurveCharacterstics pchar : widget.getPlottingFeatures()) {
			pchar.setLineType(line);
			pchar.setSymbol(symbol);
		}
	}

	public void setAutoRange() {
		widget.setAutoRange();
	}

}
