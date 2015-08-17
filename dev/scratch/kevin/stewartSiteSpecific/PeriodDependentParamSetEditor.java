package scratch.kevin.stewartSiteSpecific;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.jfree.ui.ExtensionFileFilter;
import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.editor.AbstractParameterEditor;
import org.opensha.commons.param.event.ParameterChangeEvent;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

public class PeriodDependentParamSetEditor<E extends Enum<E>>
extends AbstractParameterEditor<PeriodDependentParamSet<E>> implements ActionListener, TableModelListener {
	
	private static final boolean D = true;
	
	private PeriodDepTableModel tableModel;
	private JTable table;

	private JButton addButton;
	private JButton removeButton;
	private JButton importButton;
	private JButton exportButton;
	
	private JFileChooser chooser;

	private JTextField periodField;

	private boolean isFocusListenerForX = false;

//	private ArbitrarilyDiscretizedFuncTableCellRenderer renderer;

	private JPanel widgetPanel;
	
	public PeriodDependentParamSetEditor(Parameter<PeriodDependentParamSet<E>> param) {
		super(param);
	}

	@Override
	public boolean isParameterSupported(
			Parameter<PeriodDependentParamSet<E>> param) {
		if (param == null)
			return false;
		
		if (param.getValue() != null && !(param.getValue() instanceof PeriodDependentParamSet<?>))
			return false;
		
		return true;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.table.setEnabled(enabled);
		this.tableModel.setEnabled(enabled);
		this.addButton.setEnabled(enabled);
		this.removeButton.setEnabled(enabled);
	}

	@Override
	protected JComponent buildWidget() {
		PeriodDependentParamSet<E> data = getValue();
		Preconditions.checkNotNull(data);
		
		JPanel buttonPanel = new JPanel(new BorderLayout());
		JPanel topButtonPanel = new JPanel(new BorderLayout());
		JPanel topLeftButtonPanel = new JPanel();
		topLeftButtonPanel.setLayout(new BoxLayout(topLeftButtonPanel, BoxLayout.X_AXIS));
		JPanel topRightButtonPanel = new JPanel();
		topRightButtonPanel.setLayout(new BoxLayout(topRightButtonPanel, BoxLayout.X_AXIS));
		JPanel bottomButtonPanel = new JPanel();
		bottomButtonPanel.setLayout(new BoxLayout(bottomButtonPanel, BoxLayout.X_AXIS));

		if (addButton == null) {
			addButton = new JButton("+");
			addButton.addActionListener(this);
		}
		if (removeButton == null) {
			removeButton = new JButton("-");
			removeButton.addActionListener(this);
		}
		if (periodField == null) {
			periodField = new JTextField();
			//			xField.addFocusListener(focusListener);
//			periodField.getDocument().addDocumentListener(this);
			periodField.setColumns(4);
		}
		topLeftButtonPanel.add(new JLabel("period: "));
		topLeftButtonPanel.add(periodField);
		topLeftButtonPanel.add(addButton);
		topRightButtonPanel.add(removeButton);
		topButtonPanel.add(topLeftButtonPanel, BorderLayout.WEST);
		topButtonPanel.add(topRightButtonPanel, BorderLayout.EAST);

		buttonPanel.add(topButtonPanel, BorderLayout.NORTH);
		
		if (importButton == null) {
			importButton = new JButton("Import");
			importButton.addActionListener(this);
		}
		if (exportButton == null) {
			exportButton = new JButton("Export");
			exportButton.addActionListener(this);
		}
		
		bottomButtonPanel.add(importButton);
		bottomButtonPanel.add(exportButton);

		JPanel bottomWrapper = new JPanel();
		bottomWrapper.add(bottomButtonPanel);
		buttonPanel.add(bottomWrapper, BorderLayout.SOUTH);

		tableModel = new PeriodDepTableModel(data);
		table = new JTable(tableModel);
		tableModel.addTableModelListener(this);
		//		table.setDefaultEditor(Double.class, new ArbitrarilyDiscretizedFuncTableCellEditor());
		TableCellRenderer renderer = tableModel.getRenderer();
		table.setDefaultRenderer(Double.class, renderer);

		JScrollPane scroll = new JScrollPane(table);
		scroll.setPreferredSize(new Dimension(100, 200));

		widgetPanel = new JPanel(new BorderLayout());
		widgetPanel.add(scroll, BorderLayout.CENTER);
		widgetPanel.add(buttonPanel, BorderLayout.SOUTH);

		widgetPanel.setBackground(null);
		widgetPanel.validate();
		widgetPanel.repaint();

		return widgetPanel;
	}

	@Override
	protected JComponent updateWidget() {
		PeriodDependentParamSet<E> data = getValue();
		if (D) System.out.println("Update called!");
		tableModel.updateData(data);

		widgetPanel.validate();
		widgetPanel.repaint();
		return widgetPanel;
	}
	
	private final static Color disabledColor = new Color(210, 210, 210);
	
	private class PeriodDepTableModel extends AbstractTableModel {
		
		private PeriodDependentParamSet<E> data;
		private E[] params;
		
		private TableCellRenderer renderer;
		
		public PeriodDepTableModel(PeriodDependentParamSet<E> data) {
			updateData(data);
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return params.length+1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return data.getPeriod(rowIndex);
			int paramIndex = columnIndex-1;
			return data.get(params[paramIndex], rowIndex);
		}
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			double val = (Double)aValue;
			if (D) System.out.println("Setting value at ("+rowIndex+","+columnIndex+") to: "+val);
			if (columnIndex > 0) {
				// we're changing a Y...easy
				data.set(rowIndex, params[columnIndex-1], val);
			} else {
				// we're changing a period...harder
				double[] vals = data.getValues(rowIndex);
				removePeriod(rowIndex);
				data.set(val, vals);
			}
			this.fireTableDataChanged();
		}

		@Override
		public Class<?> getColumnClass(int c) {
			return Double.class;
		}
		
		@Override
		public boolean isCellEditable(int row, int col) {
			return true;
		}
		
		public void setEnabled(boolean isEnabled) {
			TableCellRenderer renderer = getRenderer();
			if (isEnabled)
				renderer.setBackground(Color.WHITE);
			else
				renderer.setBackground(disabledColor);
		}
		
		@Override
		public String getColumnName(int column) {
			if (column == 0)
				return "Period";
			else
				return params[column-1].toString();
		}
		
		public TableCellRenderer getRenderer() {
			if (renderer == null)
				renderer = new TableCellRenderer();
			return renderer;
		}
		
		public void updateData(PeriodDependentParamSet<E> data) {
			this.data = data;
			this.params = data.getParams();
			fireTableDataChanged();
		}
		
		public void addPeriod(double period) {
			addPeriod(period, new double[params.length]);
		}
		
		public void addPeriod(double period, double[] vals) {
			this.data.set(period, vals);
			fireTableDataChanged();
		}
		
		public void removePeriod(int index) {
			this.data.remove(index);
			this.fireTableDataChanged();
		}
		
	}
	
	private static DecimalFormat format;
	
	static {
		format = new DecimalFormat();
		format.setMaximumFractionDigits(10);
	}
	
	// Based on JTable.DoubleRenderer with modifications to the formatter
	// I have to reimplement some of it because JTable.DoubleRenderer isn't visible
	private class TableCellRenderer extends DefaultTableCellRenderer.UIResource {

		public TableCellRenderer() {
			super();
			setHorizontalAlignment(JLabel.RIGHT);
			this.setPreferredSize(new Dimension(20, 8));
		}



		public void setValue(Object value) {
			setText((value == null) ? "" : format.format(value));
		}

		//			public Dimension getPreferredSize() {
		//				return new Dimension(20, 8);
		//			}
		//			
		//			public int getWidth() {
		//				return 20;
		//			}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == addButton) {
			String periodStr = periodField.getText();
			try {
				double period = Double.parseDouble(periodStr);
				tableModel.addPeriod(period);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(widgetPanel, "Must supply valid period",
						"Must supply period", JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getSource() == removeButton) {
			int selected[] = table.getSelectedRows();
			if (selected == null || selected.length == 0)
				return;
			List<Integer> rowsSorted = Lists.newArrayList();
			for (int index : selected)
				rowsSorted.add(index);
			if (selected.length > 1) {
				// remove in reverse order
				Collections.sort(rowsSorted);
				Collections.reverse(rowsSorted);
			}
			for (int index : rowsSorted)
				tableModel.removePeriod(index);
		} else if (e.getSource() == importButton) {
			if (chooser == null) {
				chooser = new JFileChooser();
				chooser.setFileFilter(new ExtensionFileFilter("CSV File", "csv"));
			}
			int ret = chooser.showOpenDialog(widgetPanel);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File csvFile = chooser.getSelectedFile();
				try {
					PeriodDependentParamSet.loadCSV(getValue(), csvFile);
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(widgetPanel, "Error reading CSV",
							"Exception: "+e1.getMessage(), JOptionPane.ERROR_MESSAGE);
				} catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(widgetPanel, "Error loading CSV",
							"Exception parsing CSV:\n"+e1.getMessage(), JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (e.getSource() == exportButton) {
			if (chooser == null) {
				chooser = new JFileChooser();
				chooser.setFileFilter(new ExtensionFileFilter("CSV File", "csv"));
			}
			int ret = chooser.showSaveDialog(widgetPanel);
			if (ret == JFileChooser.APPROVE_OPTION) {
				File csvFile = chooser.getSelectedFile();
				try {
					getValue().writeCSV(csvFile);
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(widgetPanel, "Error writing CSV",
							"Exception: "+e1.getMessage(), JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		if (D) System.out.println("Table event...");
//		setValue(tableModel.getFunction().deepClone());
		if (D) {
			Joiner j = Joiner.on(",");
			PeriodDependentParamSet<E> val = getValue();
			System.out.println("Data:\t"+j.join(val.getParams()));
			for (int i=0; i<val.size(); i++)
				System.out.println("\t"+val.getPeriod(i)+": "+j.join(Doubles.asList(val.getValues(i))));
		}
		Parameter<PeriodDependentParamSet<E>> param = getParameter();
		PeriodDependentParamSet<E> val = getValue();
		param.firePropertyChange(new ParameterChangeEvent(param, param.getName(), val, val));
		if (D) System.out.println("Table event DONE");
	}
	
	private enum TestEnum {
		P1,
		P2,
		P3;
	}
	
	public static void main(String[] args) {
		PeriodDependentParamSet<TestEnum> data = new PeriodDependentParamSet<TestEnum>(TestEnum.values());
		PeriodDependentParamSetParam<TestEnum> param = new PeriodDependentParamSetParam<TestEnum>("Test", data);
		PeriodDependentParamSetEditor<TestEnum> editor = new PeriodDependentParamSetEditor<TestEnum>(param);
		
		JFrame frame = new JFrame();
		frame.setSize(500, 500);
		frame.setContentPane(editor);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		editor.validate();
		editor.refreshParamEditor();
	}

}
