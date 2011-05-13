package org.opensha.commons.param.impl;

import java.util.Collection;
import java.util.NoSuchElementException;

import org.dom4j.Element;
import org.opensha.commons.param.DependentParameter;
import org.opensha.commons.param.ListBasedConstraint;
import org.opensha.commons.param.ParameterAPI;
import org.opensha.commons.param.editor.ConstrainedCPTParameterEditor;
import org.opensha.commons.param.editor.ParameterEditorAPI;
import org.opensha.commons.util.cpt.CPT;

public class CPTParameter extends DependentParameter<CPT> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private transient ParameterEditorAPI<CPT> editor;
	
	public CPTParameter(String name) {
		this(name, null);
	}
	
	public CPTParameter(String name, CPT value) {
		super(name, null, null, value);
	}
	
	public CPTParameter(String name, Collection<CPT> allowed, CPT value) {
		this(name, new ListBasedConstraint<CPT>(allowed), value);
	}
	
	public CPTParameter(String name, ListBasedConstraint<CPT> constraint, CPT value) {
		super(name, constraint, null, value);
	}

	@Override
	public ParameterEditorAPI getEditor() {
		if (hasValidConstraint()) {
			if (editor == null)
				editor = new ConstrainedCPTParameterEditor(this);
			return editor;
		}
		return null;
	}

	@Override
	public int compareTo(ParameterAPI<CPT> o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected boolean setIndividualParamValueFromXML(Element el) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private boolean hasValidConstraint() {
		return getConstraint() != null && getConstraint() instanceof ListBasedConstraint;
	}
	
	/**
	 * Sets the value of the parameter by name, using the allowed items in the constraint.
	 * 
	 * @param name
	 * @throws NoSuchElementException if a constraint exists, but no CPT in the allowed list matches the name
	 * @throws UnsupportedOperationException if no valid constraint exists
	 */
	public void setByName(String name) throws NoSuchElementException, UnsupportedOperationException {
		if (hasValidConstraint()) {
			ListBasedConstraint<CPT> lconst = (ListBasedConstraint)getConstraint();
			for (CPT cpt : lconst.getAllowed()) {
				if (name.equals(cpt.getName())) {
					setValue(cpt);
					return;
				}
			}
			throw new NoSuchElementException("No CPT named '"+name+"'");
		}
		throw new UnsupportedOperationException("Can't set by name without a constraint");
	}

}
