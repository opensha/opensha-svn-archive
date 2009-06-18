package org.opensha.commons.util.cpt;

import java.awt.Color;

public interface Blender {
	public Color blend(Color small, Color big, float bias);
}
