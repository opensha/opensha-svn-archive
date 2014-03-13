package scratch.kevin.nga;

import org.opensha.commons.param.event.ParameterChangeWarningListener;

import scratch.peter.nga.ASK_2013;
import scratch.peter.nga.ASK_2013_Transitional;
import scratch.peter.nga.BSSA_2013;
import scratch.peter.nga.BSSA_2013_Transitional;
import scratch.peter.nga.CB_2013;
import scratch.peter.nga.CB_2013_Transitional;
import scratch.peter.nga.CY_2013;
import scratch.peter.nga.CY_2013_Transitional;
import scratch.peter.nga.GK_2013;
import scratch.peter.nga.GK_2013_Transitional;
import scratch.peter.nga.Idriss_2013;
import scratch.peter.nga.Idriss_2013_Transitional;
import scratch.peter.nga.TransitionalGMPE;

public class NGAWrappers {
	public static class ASK_2013_Wrapper extends TransitionalGMPEWrapper {
		
		public ASK_2013_Wrapper() {
			this(null);
		}

		public ASK_2013_Wrapper(ParameterChangeWarningListener l) {
			super(ASK_2013.SHORT_NAME, new ASK_2013_Transitional());
			this.listener = l;
		}
		
	}
	public static class BSSA_2013_Wrapper extends TransitionalGMPEWrapper {
		
		public BSSA_2013_Wrapper() {
			this(null);
		}

		public BSSA_2013_Wrapper(ParameterChangeWarningListener l) {
			super(BSSA_2013.SHORT_NAME, new BSSA_2013_Transitional());
			this.listener = l;
		}
		
	}
	public static class CB_2013_Wrapper extends TransitionalGMPEWrapper {
		
		public CB_2013_Wrapper() {
			this(null);
		}

		public CB_2013_Wrapper(ParameterChangeWarningListener l) {
			super(CB_2013.SHORT_NAME, new CB_2013_Transitional());
			this.listener = l;
		}
		
	}
	public static class CY_2013_Wrapper extends TransitionalGMPEWrapper {
		
		public CY_2013_Wrapper() {
			this(null);
		}

		public CY_2013_Wrapper(ParameterChangeWarningListener l) {
			super(CY_2013.SHORT_NAME, new CY_2013_Transitional());
			this.listener = l;
		}
		
	}
	public static class GK_2013_Wrapper extends TransitionalGMPEWrapper {
		
		public GK_2013_Wrapper() {
			this(null);
		}

		public GK_2013_Wrapper(ParameterChangeWarningListener l) {
			super(GK_2013.SHORT_NAME, new GK_2013_Transitional());
			this.listener = l;
		}
		
	}
	public static class Idriss_2013_Wrapper extends TransitionalGMPEWrapper {
		
		public Idriss_2013_Wrapper() {
			this(null);
		}

		public Idriss_2013_Wrapper(ParameterChangeWarningListener l) {
			super(Idriss_2013.SHORT_NAME, new Idriss_2013_Transitional());
			this.listener = l;
		}
		
	}
}
