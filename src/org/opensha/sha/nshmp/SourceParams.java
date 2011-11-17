package org.opensha.sha.nshmp;

import static com.google.common.base.Preconditions.*;
import static org.opensha.sha.nshmp.SourceType.*;

import java.util.Collection;
import java.util.Map;

import org.opensha.commons.param.Parameter;
import org.opensha.commons.param.ParameterList;
import org.opensha.commons.param.event.ParameterChangeEvent;
import org.opensha.commons.param.event.ParameterChangeListener;
import org.opensha.commons.param.impl.BooleanParameter;

import com.google.common.collect.Maps;

/*
 * Package private class to manage parameter lists for different source types.
 * Implemmentations of MultiSourceERFs can access these parameter lists through
 * calls to MultiSourceERF.getSourceParamList(SourceType).
 * 
 * @author Peter Powers
 * @version $Id:$
 */
class SourceParams implements ParameterChangeListener {

	private Map<SourceType, ParameterList> sourceParamMap;
	private Map<SourceType, BooleanParameter> sourceToggleMap;
	
	SourceParams() {
		sourceParamMap = Maps.newEnumMap(SourceType.class);
		sourceToggleMap = Maps.newEnumMap(SourceType.class);
		initParamLists();
	}
	
	ParameterList getParameterList(Collection<SourceType> types) {
		checkNotNull(types, "Source types is null");
		checkArgument(!types.isEmpty(), "Source types is empty");
		ParameterList pList = new ParameterList();
		for (SourceType type : types) {
			pList.addParameter(sourceToggleMap.get(type));
			pList.addParameterList(sourceParamMap.get(type));
		}
		return pList;
	}
	
	void addSourceParam(SourceType type, Parameter<?> param) {
		checkNotNull(type, "Source type is null");
		checkNotNull(param, "Parameter is null");
		sourceParamMap.get(type).addParameter(param);
	}

	private void initParamLists() {
		for (SourceType type : SourceType.values()) {
			ParameterList pList = new ParameterList();
			BooleanParameter bp = new BooleanParameter(type.paramLabel(), true);
			bp.addParameterChangeListener(this);
			sourceToggleMap.put(type, bp);
			pList.addParameter(bp);
			sourceParamMap.put(type, pList);
		}
	}

	@Override
	public void parameterChange(ParameterChangeEvent e) {
		for(SourceType type : sourceToggleMap.keySet()) {
			BooleanParameter sourceParam = sourceToggleMap.get(type);
			if (e.getParameter() == sourceParam) {
				ParameterList pList = sourceParamMap.get(type);
				for (Parameter<?> p : pList) {
					p.getEditor().setEnabled(sourceParam.getValue());
				}
			}
		}
	}
		
}
