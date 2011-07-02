package org.opensha.commons.param.editor.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.editor.AbstractParameterEditor;
import org.opensha.commons.param.impl.FileParameter;

public class FileParameterEditor extends AbstractParameterEditor<File> implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JButton browseButton;
	private JFileChooser chooser;
	
	public FileParameterEditor(FileParameter param) {
		super(param);
		
		browseButton.addActionListener(this);
	}
	
	@Override
	public void setEnabled(boolean isEnabled) {
		browseButton.setEnabled(isEnabled);
	}

	private JButton getBrowseButton() {
		if (browseButton == null)
			browseButton = new JButton("Browse");
		return browseButton;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == browseButton) {
			if (chooser == null)
				chooser = new JFileChooser();
			int retVal = chooser.showOpenDialog(this);
			if (retVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				setValue(file);
			}
		}
	}

	@Override
	public boolean isParameterSupported(Parameter<File> param) {
		if (param == null)
			return false;
		return (param.getValue() == null && param.isNullAllowed()) || param.getValue() instanceof File;
	}

	@Override
	protected JComponent buildWidget() {
		return getBrowseButton();
	}

	@Override
	protected JComponent updateWidget() {
		return getBrowseButton();
	}

}
