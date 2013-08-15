package scratch.peter.nga;

import static java.lang.Double.NaN;
import static scratch.peter.nga.FaultStyle.UNKNOWN;

import org.opensha.sha.util.TectonicRegionType;

import com.google.common.base.Joiner;

import scratch.peter.newcalc.ScalarGroundMotion;

/**
 * @author Peter Powers
 * @version $Id:$
 */
public class GK_2013_Transitional implements TransitionalGMPE {

	private GK_2013 impl = new GK_2013();

	private IMT imt = null;

	private double Mw = NaN;
	private double rRup = NaN;
	private double vs30 = NaN;
	private FaultStyle style = UNKNOWN;
	
	@Override
	public ScalarGroundMotion calc() {
//		System.out.println(Joiner.on('\t').join(Mw, rRup, vs30, style));
		return impl.calc(imt, Mw, rRup, vs30, style);
	}

	@Override public String getName() { return GK_2013.NAME; }

	@Override public void set_IMT(IMT imt) { this.imt = imt; }

	@Override public void set_Mw(double Mw) { this.Mw = Mw; }
	
	@Override public void set_rJB(double rJB) {} // not used
	@Override public void set_rRup(double rRup) { this.rRup = rRup; }
	@Override public void set_rX(double rX) {} // not used
	
	@Override public void set_dip(double dip) {} // not used
	@Override public void set_width(double width) {} // not used
	@Override public void set_zTop(double zTop) {} // not used
	@Override public void set_zHyp(double zHyp) {} // not used
	
	@Override public void set_vs30(double vs30) { this.vs30 = vs30; }
	@Override public void set_vsInf(boolean vsInf) {} // not used
	@Override public void set_z2p5(double z2p5) {} // not used
	@Override public void set_z1p0(double z1p0) {} // not used

	@Override public void set_fault(FaultStyle style) { this.style = style; }

	@Override
	public TectonicRegionType get_TRT() {
		return TectonicRegionType.ACTIVE_SHALLOW;
	}

}
