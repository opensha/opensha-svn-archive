package org.scec.gui.ned;

/**
 * <b>Title:</b> AttenField2000<br>
 * <b>Description:</b> This calculates the Field200 Attenuation Relationship<br>
 * <b>Copyright:</b>    Copyright (c) 2001<br>
 * <b>Company:</b>      <br>
 * @author Ned Field
 * @version 1.0
 */
public class AttenField2000{

	private float mag, dist, v30;		// moment mag; distance Rjb; 30-meter shear-wave velocity
	private Double depth;
	private String faultType;			// "reverse slip", "strike slip", "unknown/other" only
	private int period_index;			// PSA-period*10 (set 0 for PGA)

	public final float MINMAG=(float)5, MAXMAG=(float)7.5, MINDIST=(float)0, MAXDIST=(float)150;


	private double b1, b1all, b1ss, b1rv, b2, b3, b5, bv, va, h, sigma, bd_slope, bd_intercept;

	public AttenField2000(float m, float d, String ft, float v, int period, Double dpth) throws Exception
	{
		mag=m;
		dist=d;
		v30=v;
		depth = dpth;
		if(v30 <= 0)		// make sure v30 is positive
			throw new Exception("Error: Site Velocity must be positive");
		faultType=ft;
		period_index = period;

		// Set parameters according to the period
 		switch (period_index) {
 			case 0:	// PGA
				b1ss = 0.853;
				b1rv = 0.872;
 				b1all = (b1ss+b1rv)/2;
 				b2=0.442;
				b3=-0.067;
				b5=-0.960;
				bv=-0.154;
				va=760;
				h=8.90;
				sigma = 0.52;
				bd_slope = 0.067;
				bd_intercept = -0.14;
 				break;
			case 3:	// 0.3-sec PSA
				b1ss = 0.995;
				b1rv = 1.096;
 				b1all =  (b1ss+b1rv)/2;
 				b2=0.501;
				b3=-0.112;
				b5=-0.841;
				bv=-0.350;
				va=760;
				h=7.20;
				sigma = 0.59;
				bd_slope = 0.057;
				bd_intercept = -0.12;
 				break;
 			 case 10:	// 1-sec PSA
				b1ss = -0.164;
				b1rv = -0.267;
 				b1all =  (b1ss+b1rv)/2;
				b2=0.903;
				b3=0.0;
				b5=-0.914;
				bv=-0.704;
				va=760;
				h=6.20;
				sigma = 0.57;
				bd_slope = 0.12;
				bd_intercept = -0.25;
 				break;
 			 case 30:	// 3-sec PSA
				b1ss = -2.267;
				b1rv = -2.681;
 				b1all =  (b1ss+b1rv)/2;
				b2=1.083;
				b3=0.0;
				b5=-0.720;
				bv=-0.674;
				va=760;
				h=3.0;
				sigma = 0.6;
				bd_slope = 0.11;
				bd_intercept = -0.18;
 				break;
 			default:
 				throw new Exception("Sorry, that PSA period is not available for Field2000");
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
		if  (depth == null)
			return (float) (b1 + b2*((double)mag-6)+b3*((double)mag-6)*((double)mag-6) + b5*Math.log(r) + bv*Math.log((double)v30/va));
		else
			return (float) (b1 + b2*((double)mag-6)+b3*((double)mag-6)*((double)mag-6) + b5*Math.log(r) + bv*Math.log((double)v30/va) + bd_slope*depth.doubleValue() + bd_intercept);
	}

	public float amp()  { return (float) Math.exp((double)ln_amp()); }

	public float up95() { return (float)  Math.exp((double)(ln_amp()+(float)2*sigma)); }

	public float low95() { return (float) Math.exp((double)(ln_amp()-(float)2*sigma)); }
}





