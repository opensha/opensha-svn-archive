package org.scec.gui.ned;

/**
 * <b>Title:</b>   AttenBJF97<br>
 * <b>Description:</b>  This calculates the BJF97 Attenuation Relationship (Boore et al. (1997), SRL, 128-153)<br>
 * <b>Copyright:</b>    Copyright (c) 2001
 * <b>Company:</b>
 * @author Ned Field
 * @version 1.0
 */

public class AttenBJF97 {

	private float mag, dist, v30;		// moment mag; distance Rjb; 30-meter shear-wave velocity
	private String faultType;			// "reverse slip", "strike slip", "unknown/other" only
	private int period_index;			// PSA-period*10 (set 0 for PGA)

	public final float MINMAG=(float)5.5, MAXMAG=(float)7.5, MINDIST=(float)0, MAXDIST=(float)80;


	private double b1, b1all, b1ss, b1rv, b2, b3, b5, bv, va, h, sigma;

	public AttenBJF97(float m, float d, String ft, float v, int period) throws Exception
	{
		mag=m;
		dist=d;
		v30=v;
		if(v30 <= 0)		// make sure v30 is positive
			throw new Exception("Error: Site Velocity must be positive");
		faultType=ft;
		period_index = period;

		// Set parameters according to the period
 		switch (period_index) {
 			case 0:	// PGA
				b1ss = -0.313;
				b1rv = -0.117;
 				b1all = -0.242;
 				b2=0.527;
				b3=0;
				b5=-0.778;
				bv=-0.371;
				va=1396;
				h=5.57;
				sigma = 0.52;
 				break;
 			case 1:	// 0.1-sec PSA
				b1ss = 1.006;
				b1rv = 1.087;
 				b1all = 1.059;
 				b2=0.753;
				b3=-0.226;
				b5=-0.934;
				bv=-0.212;
				va=1112;
				h=6.27;
				sigma = 0.479;
 				break;
 			case 3:	// 0.3-sec PSA
				b1ss = 0.598;
				b1rv = 0.803;
 				b1all = 0.70;
 				b2=0.769;
				b3=-0.161;
				b5=-0.893;
				bv=-0.401;
				va=2133;
				h=5.94;
				sigma = 0.522;
 				break;
 			 case 10:	// 1-sec PSA
				b1ss = -1.133;
				b1rv = -1.009;
 				b1all = -1.080;
				b2=1.036;
				b3=-0.032;
				b5=-0.798;
				bv=-0.698;
				va=1406;
				h=2.90;
				sigma = 0.613;
 				break;
 			default:
 				throw new Exception("Sorry, that PSA period is not available for BJF97");
 		}

 		// set fault type
 		if (faultType.equals("reverse slip"))
        	b1 = b1rv;
        else if (faultType.equals("strike slip"))
        	b1 = b1ss;
        else if (faultType.equals("unknown/other"))
        	b1 = b1all;
        else
        	throw new Exception("Unrecognized fault type");
 	}

	public float std() { return (float) sigma; }

	public float ln_amp()
	{
		double r = Math.sqrt(h*h + (float)(dist*dist));
		return (float) (b1 + b2*((double)mag-6)+b3*((double)mag-6)*((double)mag-6) + b5*Math.log(r) + bv*Math.log((double)v30/va));
	}

	public float amp()  { return (float) Math.exp((double)ln_amp()); }

	public float up95() { return (float)  Math.exp((double)(ln_amp()+(float)2*sigma)); }

	public float low95() { return (float) Math.exp((double)(ln_amp()-(float)2*sigma)); }
}
