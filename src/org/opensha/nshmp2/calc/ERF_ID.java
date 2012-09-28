package org.opensha.nshmp2.calc;

import org.opensha.commons.data.TimeSpan;
import org.opensha.nshmp2.erf.NSHMP2008;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.AbstractEpistemicListERF;
import org.opensha.sha.earthquake.BaseERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2_FM2pt1;

import scratch.UCERF3.erf.UCERF2_Mapped.UCERF2_FM2pt1_FaultSysSolERF;
import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2_FM2pt1;
import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2_FM2pt1_wOutAftershocks;
import scratch.UCERF3.utils.UpdatedUCERF2.MeanUCERF2update;

/**
 * Add comments here
 * 
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public enum ERF_ID {

	NSHMP08() {
		public EpistemicListERF instance() {
			return NSHMP2008.create();
		}
	},
	NSHMP08_CA() {
		public EpistemicListERF instance() {
			return NSHMP2008.createCalifornia();
		}
	},
	MEAN_UCERF2() {
		public EpistemicListERF instance() {
			return getMeanUC2();
		}
	},
	MEAN_UCERF2_FM2P1() {
		public EpistemicListERF instance() {
			return getMeanUC2_FM2P1();
		}
	},
	MOD_MEAN_UCERF2_FM2P1() {
		public EpistemicListERF instance() {
			return getModMeanUC2_FM2P1();
		}
	},
	UCERF2_FM2P1_FSS_ERF() {
		public EpistemicListERF instance() {
			return getUC2_FM2P1_FSS();
		}
	};

	// scratch.UCERF3.erf.UCERF2_Mapped.UCERF2_FM2pt1_FaultSysSolERF

	public abstract EpistemicListERF instance();

	private static EpistemicListERF getMeanUC2() {
		final MeanUCERF2update erf = new MeanUCERF2update();
		setParams(erf);
		return wrapInList(erf);
	}

	private static EpistemicListERF getMeanUC2_FM2P1() {
		final MeanUCERF2_FM2pt1 erf = new MeanUCERF2_FM2pt1();
		setParams(erf);
		return wrapInList(erf);
	}

	private static EpistemicListERF getModMeanUC2_FM2P1() {
		final ModMeanUCERF2_FM2pt1 erf = new ModMeanUCERF2_FM2pt1_wOutAftershocks();
		setParams(erf);
		return wrapInList(erf);
	}

	private static EpistemicListERF getUC2_FM2P1_FSS() {
		final UCERF2_FM2pt1_FaultSysSolERF erf = new UCERF2_FM2pt1_FaultSysSolERF();
		erf.getTimeSpan().setDuration(1.0);
		return wrapInList(erf);
	}

	
	private static EpistemicListERF wrapInList(final AbstractERF erf) {
		EpistemicListERF listERF = new AbstractEpistemicListERF() {
			{
				addERF(erf, 1.0);
			}
		};
		TimeSpan timeSpan = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		timeSpan.setDuration(1);
		listERF.setTimeSpan(timeSpan);
		return listERF;
	}
	
	private static void setParams(ERF uc2) {
		uc2.setParameter(MeanUCERF2.RUP_OFFSET_PARAM_NAME, 1.0);
		uc2.setParameter(UCERF2.PROB_MODEL_PARAM_NAME,
			UCERF2.PROB_MODEL_POISSON);
		uc2.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_INCLUDE);
		uc2.setParameter(UCERF2.BACK_SEIS_RUP_NAME, UCERF2.BACK_SEIS_RUP_POINT);
		uc2.setParameter(UCERF2.FLOATER_TYPE_PARAM_NAME,
			UCERF2.FULL_DDW_FLOATER);
		uc2.getTimeSpan().setDuration(1.0);
	}

}
