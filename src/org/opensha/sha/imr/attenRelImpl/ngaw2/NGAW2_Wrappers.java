package org.opensha.sha.imr.attenRelImpl.ngaw2;

import org.opensha.commons.param.event.ParameterChangeWarningListener;

@SuppressWarnings("javadoc")
public class NGAW2_Wrappers {
	public static class ASK_2013_Wrapper extends NGAW2_Wrapper {
		
		public ASK_2013_Wrapper() {
			this(null);
		}

		public ASK_2013_Wrapper(ParameterChangeWarningListener l) {
			super("ASK2013", new ASK_2013());
			this.listener = l;
		}
		
	}
	public static class BSSA_2013_Wrapper extends NGAW2_Wrapper {
		
		public BSSA_2013_Wrapper() {
			this(null);
		}

		public BSSA_2013_Wrapper(ParameterChangeWarningListener l) {
			super("BSSA2013", new BSSA_2013());
			this.listener = l;
		}
		
	}
	public static class CB_2013_Wrapper extends NGAW2_Wrapper {
		
		public CB_2013_Wrapper() {
			this(null);
		}

		public CB_2013_Wrapper(ParameterChangeWarningListener l) {
			super("CB2013", new CB_2013());
			this.listener = l;
		}
		
	}
	public static class CY_2013_Wrapper extends NGAW2_Wrapper {
		
		public CY_2013_Wrapper() {
			this(null);
		}

		public CY_2013_Wrapper(ParameterChangeWarningListener l) {
			super("CY2013", new CY_2013());
			this.listener = l;
		}
		
	}
	public static class GK_2013_Wrapper extends NGAW2_Wrapper {
		
		public GK_2013_Wrapper() {
			this(null);
		}

		public GK_2013_Wrapper(ParameterChangeWarningListener l) {
			super("GK2013", new GK_2013());
			this.listener = l;
		}
		
	}
	public static class Idriss_2013_Wrapper extends NGAW2_Wrapper {
		
		public Idriss_2013_Wrapper() {
			this(null);
		}

		public Idriss_2013_Wrapper(ParameterChangeWarningListener l) {
			super("Idriss2013", new Idriss_2013());
			this.listener = l;
		}
		
	}
}
