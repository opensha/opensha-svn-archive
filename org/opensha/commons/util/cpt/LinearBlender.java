package org.opensha.commons.util.cpt;

import java.awt.Color;

public class LinearBlender implements Blender {

	/**
	 * Constructs a blender which interpolates linearly between the smallColor
	 * and BigColor
	 */
	public LinearBlender() {

	}

	/**
	 * Linearly interpolates a new color between smallColor and bigColor with a
	 * bias in range[0,1] For example bias = .5 means a value between half of
	 * smallColor and half of bigColor
	 */
	public int[] blend(int smallR, int smallG, int smallB, int bigR, int bigG,
			int bigB, float bias) {
		// TODO Auto-generated method stub
		int rgb[] = new int[3];
		
		rgb[0] = this.blend(smallR, bigR, bias);
		rgb[1] = this.blend(smallG, bigG, bias);
		rgb[2] = this.blend(smallB, bigB, bias);
		
		return rgb;
	}
	
	private int blend(int small, int big, float bias) {
		float blend = (float)big * bias + (1f - bias) * (float)small;
		return (int)(blend + 0.5);
	}

	public Color blend(Color small, Color big, float bias) {
		int rgb[] = this.blend(small.getRed(), small.getGreen(), small.getBlue(), big.getRed(), big.getGreen(), big.getBlue(), bias);
		return new Color(rgb[0], rgb[1], rgb[2]);
	}

}
