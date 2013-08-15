package scratch.peter.nga;

import org.opensha.sha.util.TectonicRegionType;

import scratch.peter.newcalc.ScalarGroundMotion;

/**
 * 
 * These are seom notes on efficiencies in the NGAW2 GMPE's that are not related
 * to this class directly.
 * 
 * CB13 is probably the most cumbersome as both mean and std depend on a pgaRock
 * value which requires looping through the calcMean() with PGA coeffs.
 * That said... pgaRock is only used if vs30 is below a certain value (c.k1)
 * and can/should be short-circuited; at 5Hz, 1Hz and 3s k1<760; not for PGA
 * though.
 * DONE
 * 
 * AS13 uses saRock, the use of which is conditiond on vs < c.Vlin, and which
 * is dependent only on 1 or two terms and can therefore be computed inline.
 * Hoever, it is required by std so mean and std should be combined.
 * DONE
 * 
 * CY13 precomputes a non-site-specific reference term and can't be optimized
 * further.
 * 
 * BSSA13 conditions on PGArock
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public interface TransitionalGMPE extends org.opensha.commons.data.Named {
	
	public ScalarGroundMotion calc();
	
	public void set_IMT(IMT imt);
	
	// order below should be followed in calc(args)
	
	public void set_Mw(double Mw);
	
	public void set_rJB(double rJB);
	public void set_rRup(double rRup);
	public void set_rX(double rX);
	
	public void set_dip(double dip);
	public void set_width(double width);
	public void set_zTop(double zTop);
	public void set_zHyp(double zHyp);

	public void set_vs30(double vs30);
	public void set_vsInf(boolean vsInf);
	public void set_z2p5(double z2p5);
	public void set_z1p0(double z1p0);
	
	public void set_fault(FaultStyle style);
	
	public TectonicRegionType get_TRT();
		
}
