package org.opensha.sha.gui.beans;

import org.opensha.commons.param.editor.ParameterListEditor;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;

public class IMR_ParamEditor extends ParameterListEditor {
	
	public IMR_ParamEditor() {
		this(null);
	}
	
	public IMR_ParamEditor(ScalarIntensityMeasureRelationshipAPI imr) {
		setTitle("IMR Params");
		this.setIMR(imr);
	}
	
	public void setIMR(ScalarIntensityMeasureRelationshipAPI imr) {
		if (imr == null) {
			this.setParameterList(null);
			return;
		}
		this.setParameterList(imr.getOtherParamsList());
	}

}
