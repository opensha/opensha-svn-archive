package scratch.peter.nga;

import static java.lang.Double.NaN;
import static scratch.peter.nga.FaultStyle.UNKNOWN;

import java.util.Collection;

import org.opensha.sha.util.TectonicRegionType;

import com.google.common.base.Joiner;

import scratch.peter.newcalc.ScalarGroundMotion;

/**
 * @author Peter Powers
 * @version $Id:$
 */
public class ASK_2013_Transitional implements TransitionalGMPE {

	private ASK_2013 impl = new ASK_2013();

	private IMT imt = null;
	
	private double Mw = NaN;
	private double rJB = NaN;
	private double rRup = NaN;
	private double rX = NaN;
	private double rY0 = -1.0;
	private double dip = NaN;
	private double width = NaN;
	private double zTop = NaN;
	private double vs30 = NaN;
	private boolean vsInf = true;
	private double z1p0 = NaN;
	private FaultStyle style = UNKNOWN;
	
	@Override
	public ScalarGroundMotion calc() {
//		System.out.println(Joiner.on('\t').join(Mw, rJB, rRup, rX, dip, width, zTop, vs30, z1p0, style));
		return impl.calc(imt, Mw, rJB, rRup, rX, rY0, dip, width, zTop, vs30,
			vsInf, z1p0, style);
	}
	
	@Override public String getName() { return ASK_2013.NAME; }

	@Override public void set_IMT(IMT imt) { this.imt = imt; }

	@Override public void set_Mw(double Mw) { this.Mw = Mw; }
	
	@Override public void set_rJB(double rJB) { this.rJB = rJB; }
	@Override public void set_rRup(double rRup) { this.rRup = rRup; }
	@Override public void set_rX(double rX) { this.rX = rX; }
	
	@Override public void set_dip(double dip) { this.dip = dip; }
	@Override public void set_width(double width) { this.width = width; }
	@Override public void set_zTop(double zTop) { this.zTop = zTop; }
	@Override public void set_zHyp(double zHyp) {} // not used
	
	@Override public void set_vs30(double vs30) { this.vs30 = vs30; }
	@Override public void set_vsInf(boolean vsInf) { this.vsInf = vsInf; }
	@Override public void set_z2p5(double z2p5) {} // not used
	@Override public void set_z1p0(double z1p0) { this.z1p0 = z1p0; }

	@Override public void set_fault(FaultStyle style) { this.style = style; }

	@Override
	public TectonicRegionType get_TRT() {
		return TectonicRegionType.ACTIVE_SHALLOW;
	}

	@Override
	public Collection<IMT> getSupportedIMTs() {
		return impl.getSupportedIMTs();
	}

}
