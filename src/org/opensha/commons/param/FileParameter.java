package org.opensha.commons.param;

import java.io.File;

import org.dom4j.Element;
import org.opensha.commons.param.editor.FileParameterEditor;
import org.opensha.commons.param.editor.ParameterEditorAPI;

public class FileParameter extends Parameter<File> {
	
	private FileParameterEditor editor;
	
	public FileParameter(String name) {
		this(name, null);
	}
	
	public FileParameter(String name, File file) {
		super(name, null, null, file);
	}

	@Override
	public boolean setValueFromXMLMetadata(Element el) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int compareTo(Object parameter) throws ClassCastException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ParameterEditorAPI getEditor() {
		if (editor == null)
			editor = new FileParameterEditor(this);
		return editor;
	}

	@Override
	public Object clone() {
		return new FileParameter(this.getName(), this.getValue());
	}

}
