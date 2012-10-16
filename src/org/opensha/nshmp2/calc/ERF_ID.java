package org.opensha.nshmp2.calc;

import java.io.File;
import java.util.List;

import org.opensha.commons.data.TimeSpan;
import org.opensha.commons.param.Parameter;
import org.opensha.nshmp2.erf.NSHMP2008;
import org.opensha.sha.earthquake.AbstractERF;
import org.opensha.sha.earthquake.AbstractEpistemicListERF;
import org.opensha.sha.earthquake.BaseERF;
import org.opensha.sha.earthquake.ERF;
import org.opensha.sha.earthquake.EpistemicListERF;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeDependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2_FM2pt1;

import com.google.common.collect.Lists;

import scratch.UCERF3.CompoundFaultSystemSolution;
import scratch.UCERF3.FaultSystemSolution;
import scratch.UCERF3.SimpleFaultSystemSolution;
import scratch.UCERF3.erf.FaultSystemSolutionPoissonERF;
import scratch.UCERF3.erf.UCERF3_FaultSysSol_ERF;
import scratch.UCERF3.erf.UCERF2_Mapped.UCERF2_FM2pt1_FaultSysSolERF;
import scratch.UCERF3.logicTree.LogicTreeBranch;
import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2;
import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2_FM2pt1;
import scratch.UCERF3.utils.ModUCERF2.ModMeanUCERF2_FM2pt1_wOutAftershocks;
import scratch.UCERF3.utils.UpdatedUCERF2.GridSources;
import scratch.UCERF3.utils.UpdatedUCERF2.MeanUCERF2update;
import scratch.UCERF3.utils.UpdatedUCERF2.MeanUCERF2update_FM2p1;
import scratch.UCERF3.utils.UpdatedUCERF2.ModMeanUCERF2update_FM2p1;
import scratch.UCERF3.utils.UpdatedUCERF2.UCERF2_FM2pt1_FSS_ERFupdate;

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
	NSHMP08_CA_GRD() {
		public EpistemicListERF instance() {
			return NSHMP2008.createCaliforniaGridded();
		}
	},
	NSHMP08_CA_FIX() {
		public EpistemicListERF instance() {
			return NSHMP2008.createCaliforniaFixedStrk();
		}
	},
	NSHMP08_CA_PT() {
		public EpistemicListERF instance() {
			return NSHMP2008.createCaliforniaPointSrc();
		}
	},
	MEAN_UCERF2() {
		public EpistemicListERF instance() {
			return getMeanUC2();
		}
	},
	MEAN_UCERF2_GRD() {
		public EpistemicListERF instance() {
			return getMeanUC2_GRD();
		}
	},
	MEAN_UCERF2_FIX() {
		public EpistemicListERF instance() {
			return getMeanUC2_FIX();
		}
	},
	MEAN_UCERF2_PT() {
		public EpistemicListERF instance() {
			return getMeanUC2_PT();
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
	},
	UCERF2_TIME_INDEP() {
		public EpistemicListERF instance() {
			return getUC2_TI();
		}
	},
	UCERF3_REF_MEAN() {
		public EpistemicListERF instance() {
			return getUC3_REFmean();
		}
	},
	UCERF3_REF_0() { public EpistemicListERF instance() { return getUC3_REF(0); }},
	UCERF3_REF_1() { public EpistemicListERF instance() { return getUC3_REF(1); }},
	UCERF3_REF_2() { public EpistemicListERF instance() { return getUC3_REF(2); }},
	UCERF3_REF_3() { public EpistemicListERF instance() { return getUC3_REF(3); }},
	UCERF3_REF_4() { public EpistemicListERF instance() { return getUC3_REF(4); }},
	UCERF3_REF_5() { public EpistemicListERF instance() { return getUC3_REF(5); }},
	UCERF3_REF_6() { public EpistemicListERF instance() { return getUC3_REF(6); }},
	UCERF3_REF_7() { public EpistemicListERF instance() { return getUC3_REF(7); }},
	UCERF3_REF_8() { public EpistemicListERF instance() { return getUC3_REF(8); }},
	UCERF3_REF_9() { public EpistemicListERF instance() { return getUC3_REF(9); }},
	UCERF3_REF_10() { public EpistemicListERF instance() { return getUC3_REF(10); }},
	UCERF3_REF_11() { public EpistemicListERF instance() { return getUC3_REF(11); }},
	UCERF3_REF_12() { public EpistemicListERF instance() { return getUC3_REF(12); }},
	UCERF3_REF_13() { public EpistemicListERF instance() { return getUC3_REF(13); }},
	UCERF3_REF_14() { public EpistemicListERF instance() { return getUC3_REF(14); }},
	UCERF3_REF_15() { public EpistemicListERF instance() { return getUC3_REF(15); }},
	UCERF3_REF_16() { public EpistemicListERF instance() { return getUC3_REF(16); }},
	UCERF3_REF_17() { public EpistemicListERF instance() { return getUC3_REF(17); }},
	UCERF3_REF_18() { public EpistemicListERF instance() { return getUC3_REF(18); }},
	UCERF3_REF_19() { public EpistemicListERF instance() { return getUC3_REF(19); }};

	// scratch.UCERF3.erf.UCERF2_Mapped.UCERF2_FM2pt1_FaultSysSolERF

	public abstract EpistemicListERF instance();

	private static EpistemicListERF getMeanUC2() {
		final MeanUCERF2 erf = new MeanUCERF2update(GridSources.ALL);
		setParams(erf);
		return wrapInList(erf);
	}
	
	private static EpistemicListERF getMeanUC2_GRD() {
		final MeanUCERF2 erf = new MeanUCERF2update(GridSources.ALL);
		setParams(erf);
		erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_ONLY);
		return wrapInList(erf);
	}

	private static EpistemicListERF getMeanUC2_FIX() {
		final MeanUCERF2 erf = new MeanUCERF2update(GridSources.FIX_STRK);
		setParams(erf);
		erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_ONLY);
		return wrapInList(erf);
	}

	private static EpistemicListERF getMeanUC2_PT() {
		final MeanUCERF2 erf = new MeanUCERF2update(GridSources.PT_SRC);
		setParams(erf);
		erf.setParameter(UCERF2.BACK_SEIS_NAME, UCERF2.BACK_SEIS_ONLY);
		return wrapInList(erf);
	}

	private static EpistemicListERF getMeanUC2_FM2P1() {
		final MeanUCERF2 erf = new MeanUCERF2update_FM2p1();
		setParams(erf);
		return wrapInList(erf);
	}

	private static EpistemicListERF getModMeanUC2_FM2P1() {
		final ModMeanUCERF2 erf = new ModMeanUCERF2update_FM2p1();
		setParams(erf);
		return wrapInList(erf);
	}

	private static EpistemicListERF getUC2_FM2P1_FSS() {
		final FaultSystemSolutionPoissonERF erf = new UCERF2_FM2pt1_FSS_ERFupdate();
		erf.getTimeSpan().setDuration(1.0);
		return wrapInList(erf);
	}
	
	private static EpistemicListERF getUC2_TI() {
		final UCERF2_TimeIndependentEpistemicList erf = new UCERF2_TimeIndependentEpistemicList();
		Parameter bgSrcParam = erf.getParameter(UCERF2.BACK_SEIS_RUP_NAME);
		bgSrcParam.setValue(UCERF2.BACK_SEIS_RUP_POINT);
		Parameter floatParam = erf.getParameter(UCERF2.FLOATER_TYPE_PARAM_NAME);
		floatParam.setValue(UCERF2.FULL_DDW_FLOATER);
		TimeSpan ts = new TimeSpan(TimeSpan.NONE, TimeSpan.YEARS);
		ts.setDuration(1);
		erf.setTimeSpan(ts);		
		return erf;
	}

	public static EpistemicListERF wrapInList(final AbstractERF erf) {
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

	private static final String UC3_CONV_PATH =
			"/home/scec-00/pmpowers/UC3/src/conv/FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_mean_sol.zip";
	private static final String UC3_TREE_PATH =
			"/home/scec-00/pmpowers/UC3/src/tree/2012_10_14-fm3-logic-tree-sample-x5_run0_COMPOUND_SOL.zip";
	
	private static EpistemicListERF getUC3_REFmean() {
		try {
			File fssZip = new File(UC3_CONV_PATH);
			SimpleFaultSystemSolution fss = SimpleFaultSystemSolution.fromZipFile(fssZip);
			UCERF3_FaultSysSol_ERF erf = UC3_CalcWrapper.getUC3_ERF(fss);
			return wrapInList(erf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static EpistemicListERF getUC3_REF(int idx) {
		try {
			CompoundFaultSystemSolution cfss = UC3_CalcWrapper.getCompoundSolution(UC3_TREE_PATH);
			String ltbStr = refBranchListA.get(idx);
			LogicTreeBranch ltb = LogicTreeBranch.fromFileName(ltbStr);
			FaultSystemSolution fss = cfss.getSolution(ltb);
			UCERF3_FaultSysSol_ERF erf = UC3_CalcWrapper.getUC3_ERF(fss);
			return wrapInList(erf);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static final List<String> refBranchListA;
	private static final List<String> refBranchListB;

	static {
		refBranchListA = Lists.newArrayList(
			"FM3_1_ABM_EllB_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ABM_EllBsqrtLen_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ABM_HB08_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ABM_ShConStrDrp_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ABM_Shaw09Mod_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_GEOL_EllB_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_GEOL_EllBsqrtLen_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_GEOL_HB08_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_GEOL_ShConStrDrp_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_GEOL_Shaw09Mod_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_NEOK_EllB_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_NEOK_EllBsqrtLen_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_NEOK_HB08_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_NEOK_ShConStrDrp_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_NEOK_Shaw09Mod_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ZENG_EllB_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ZENG_EllBsqrtLen_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ZENG_HB08_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ZENG_ShConStrDrp_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ZENG_Shaw09Mod_DsrTap_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3");
		
		refBranchListB = Lists.newArrayList(
			"FM3_1_ABM_EllB_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ABM_EllBsqrtLen_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ABM_HB08_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ABM_ShConStrDrp_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ABM_Shaw09Mod_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_GEOL_EllB_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_GEOL_EllBsqrtLen_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_GEOL_HB08_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_GEOL_ShConStrDrp_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_GEOL_Shaw09Mod_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_NEOK_EllB_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_NEOK_EllBsqrtLen_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_NEOK_HB08_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_NEOK_ShConStrDrp_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_NEOK_Shaw09Mod_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ZENG_EllB_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ZENG_EllBsqrtLen_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ZENG_HB08_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ZENG_ShConStrDrp_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3",
			"FM3_1_ZENG_Shaw09Mod_DsrUni_CharConst_M5Rate7.6_MMaxOff7.6_NoFix_SpatSeisU3");
	}
	
}
