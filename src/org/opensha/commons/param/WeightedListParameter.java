package org.opensha.commons.param;

import org.dom4j.Element;
import org.opensha.commons.data.NamedObjectAPI;
import org.opensha.commons.data.WeightedList;
import org.opensha.commons.param.editor.ParameterEditorAPI;

public class WeightedListParameter<E extends NamedObjectAPI> extends DependentParameter<WeightedList<? extends NamedObjectAPI>> {
	
	public WeightedListParameter(String name, WeightedList<E> value) {
		super(name, null, null, value);
	}

	@Override
	public int compareTo(Object parameter) throws ClassCastException {
		return 0;
	}

	@Override
	public ParameterEditorAPI getEditor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean setIndividualParamValueFromXML(Element el) {
		// TODO Auto-generated method stub
		return false;
	}

}
