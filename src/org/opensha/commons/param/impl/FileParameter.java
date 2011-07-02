package org.opensha.commons.param.impl;

import java.io.File;

import org.dom4j.Element;
import org.opensha.commons.param.AbstractParameter;
import org.opensha.commons.param.editor.ParameterEditor;
import org.opensha.commons.param.editor.impl.FileParameterEditor;

public class FileParameter extends AbstractParameter<File> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private FileParameterEditor editor;
	
	public FileParameter(String name) {
		this(name, null);
	}
	
	public FileParameter(String name, File file) {
		super(name, null, null, file);
	}

	@Override
	public ParameterEditor<File> getEditor() {
		if (editor == null)
			editor = new FileParameterEditor(this);
		return editor;
	}

	@Override
	public Object clone() {
		return new FileParameter(this.getName(), this.getValue());
	}

	@Override
	public boolean setIndividualParamValueFromXML(Element el) {
		return false;
	}

}
