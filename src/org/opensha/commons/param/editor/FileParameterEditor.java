package org.opensha.commons.param.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import org.opensha.commons.param.FileParameter;

public class FileParameterEditor extends ParameterEditor implements ActionListener {
	
	private JButton browseButton;
	private JFileChooser chooser;
	
	public FileParameterEditor(FileParameter param) {
		super(param);
		
		browseButton.addActionListener(this);
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		browseButton.setEnabled(isEnabled);
		super.setEnabled(isEnabled);
	}

	private JButton getBrowseButton() {
		if (browseButton == null)
			browseButton = new JButton("Browse");
		return browseButton;
	}
	
	@Override
	protected void addWidget() {
		valueEditor = getBrowseButton();
		widgetPanel.add(valueEditor, ParameterEditor.WIDGET_GBC);
	}

	@Override
	protected void removeWidget() {
		if (valueEditor != null)
			widgetPanel.remove(valueEditor);
	}

	@Override
	protected void setWidgetObject(String name, Object obj) {
		super.setWidgetObject(name, obj);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == browseButton) {
			if (chooser == null)
				chooser = new JFileChooser();
			int retVal = chooser.showOpenDialog(this);
			if (retVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				model.setValue(file);
			}
		}
	}

}