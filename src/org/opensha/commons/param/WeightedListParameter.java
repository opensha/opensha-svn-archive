package org.opensha.commons.param;

import org.dom4j.Element;
import org.opensha.commons.data.NamedObjectAPI;
import org.opensha.commons.data.WeightedList;
import org.opensha.commons.param.editor.ParameterEditorAPI;
import org.opensha.commons.param.editor.WeightedListParameterEditor;

public class WeightedListParameter<E extends NamedObjectAPI> extends DependentParameter<WeightedList<? extends NamedObjectAPI>> {
	
	WeightedListParameterEditor paramEdit;
	
	public WeightedListParameter(String name, WeightedList<E> value) {
		super(name, null, null, value);
//		System.out.println(getMetadataString());
	}

	@Override
	public int compareTo(Object parameter) throws ClassCastException {
		return 0;
	}

	@Override
	public ParameterEditorAPI getEditor() {
		if (paramEdit == null)
			paramEdit = new WeightedListParameterEditor(this);
		return paramEdit;
	}

	@Override
	public boolean setIndividualParamValueFromXML(Element el) {
		if (value == null)
			return false;
		Element valEl = el.element(XML_COMPLEX_VAL_EL_NAME);
		Element listEl = valEl.element(WeightedList.XML_METADATA_NAME);
		value.setWeightsFromXMLMetadata(listEl);
		return true;
	}

	@Override
	public String getMetadataString() {
		WeightedList<? extends NamedObjectAPI> val = getValue();
		if(val !=null) {
			String str = name+" = [";
			
			for (int i=0; i<val.size(); i++) {
				if (i > 0)
					str += ", ";
				str += "'"+val.get(i).getName()+"': "+val.getWeight(i);
			}
			
			str += "]";
			return str;
		} else {
			return name+" = "+"null";
		}
	}

}
