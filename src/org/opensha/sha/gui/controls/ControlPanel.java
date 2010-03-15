package org.opensha.sha.gui.controls;

import java.awt.Window;

import org.opensha.commons.data.NamedObjectAPI;

public abstract class ControlPanel implements NamedObjectAPI {
	
	private String name;
	private boolean initialized = false;
	
	public ControlPanel(String name) {
		this.name = name;
	}
	
	/**
	 * This method will be called the first time the control panel is displayed. All
	 * setup should be in this method and not in the constructor, so that the apps can
	 * start quickly.
	 */
	public abstract void doinit();
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public abstract Window getComponent();
	
	public final void init() {
		if (isInitialized())
			return;
		System.out.println(name + ": init()");
		initialized = true;
		doinit();
	}
	
	public String getName() {
		return name;
	}

}
