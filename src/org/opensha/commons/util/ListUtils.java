package org.opensha.commons.util;

import java.util.ArrayList;

import org.opensha.commons.data.NamedObjectAPI;

public class ListUtils {
	
	public static NamedObjectAPI getObjectByName(ArrayList<? extends NamedObjectAPI> objects, String name) {
		for (NamedObjectAPI object : objects) {
			if (object.getName().equals(name))
				return object;
		}
		return null;
	}

}
