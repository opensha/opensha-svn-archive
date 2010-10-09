package org.opensha.commons.data;

public class NameIDPairing implements NamedObjectAPI {
	
	private int id;
	private String name;
	
	public NameIDPairing(String name, int id) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public int getID() {
		return id;
	}

}
