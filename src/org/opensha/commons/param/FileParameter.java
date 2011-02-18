package org.opensha.commons.param;

import java.io.File;

import org.dom4j.Element;
import org.opensha.commons.param.editor.FileParameterEditor;
import org.opensha.commons.param.editor.ParameterEditorAPI;

@Deprecated
public class FileParameter extends Parameter<File> {
	
	private FileParameterEditor editor;
	
	public FileParameter(String name) {
		this(name, null);
	}
	
	public FileParameter(String name, File file) {
		super(name, null, null, file);
	}

	@Override
	public int compareTo(ParameterAPI<File> param) {
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

	@Override
	public boolean setIndividualParamValueFromXML(Element el) {
		return false;
	}

}
