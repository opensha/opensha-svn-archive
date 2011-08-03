package org.opensha.sha.earthquake;

import static org.opensha.commons.util.DevStatus.DEPRECATED;
import static org.opensha.commons.util.DevStatus.DEVELOPMENT;
import static org.opensha.commons.util.DevStatus.EXPERIMENTAL;
import static org.opensha.commons.util.DevStatus.PRODUCTION;

import java.lang.reflect.Constructor;
import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.opensha.commons.util.DevStatus;
import org.opensha.commons.util.ServerPrefs;
import org.opensha.sha.cybershake.openshaAPIs.CyberShakeUCERFWrapper_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.FloatingPoissonFaultERF;
import org.opensha.sha.earthquake.rupForecastImpl.PointSourceERF;
import org.opensha.sha.earthquake.rupForecastImpl.PoissonFaultERF;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel02.Frankel02_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_EqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.GEM1SouthAmericaERF;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.GEM1_CEUS_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.GEM1_GSHAP_Africa_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.GEM1_GSHAP_SE_Asia_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.GEM1_NSHMP_SE_Asia_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.GEM1_WEUS_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.NSHMP_CEUS08.NSHMP08_CEUS_ERF;
import org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_AreaForecast;
import org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_LogicTreeERF_List;
import org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_MultiSourceForecast;
import org.opensha.sha.earthquake.rupForecastImpl.PEER_TestCases.PEER_NonPlanarFaultForecast;
import org.opensha.sha.earthquake.rupForecastImpl.Point2MultVertSS_Fault.Point2MultVertSS_FaultERF;
import org.opensha.sha.earthquake.rupForecastImpl.Point2MultVertSS_Fault.Point2MultVertSS_FaultERF_List;
import org.opensha.sha.earthquake.rupForecastImpl.WG02.WG02_ERF_Epistemic_List;
import org.opensha.sha.earthquake.rupForecastImpl.WG02.WG02_EqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF1.WGCEP_UCERF1_EqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.UCERF2_TimeIndependentEpistemicList;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_Final.MeanUCERF2.MeanUCERF2;
import org.opensha.sha.earthquake.rupForecastImpl.YuccaMountain.YuccaMountainERF;
import org.opensha.sha.earthquake.rupForecastImpl.YuccaMountain.YuccaMountainERF_List;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.FloatingPoissonFaultERF_Client;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.Frankel02_AdjustableEqkRupForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.Frankel96_AdjustableEqkRupForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_AreaForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_LogicTreeERF_ListClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_MultiSourceForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.PEER_NonPlanarFaultForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.Point2MultVertSS_FaultERF_Client;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.Point2MultVertSS_FaultERF_ListClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.PoissonFaultERF_Client;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.STEP_AlaskanPipeForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.WG02_FortranWrappedERF_EpistemicListClient;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.WGCEP_UCERF1_EqkRupForecastClient;
import org.opensha.sha.earthquake.rupForecastImpl.step.STEP_AlaskanPipeForecast;

import scratch.UCERF3.inversion.InversionSolutionERF;
import scratch.christine.URS.URS_MeanUCERF2;

public enum ERF_Ref {
	
	
	/* 
	 * ********************
	 * LOCAL ERFS
	 * ********************
	 */
	
	// PRODUCTION
	
	/** Frankel/USGS 1996 Adjustable ERF */
	FRANKEL_ADJUSTABLE_96(Frankel96_AdjustableEqkRupForecast.class,
			Frankel96_AdjustableEqkRupForecast.NAME, PRODUCTION, false, false),
	
	// should we include this?
			//			erf_Classes.add(POINT_SRC_TO_LINE_ERF_CLASS_NAME);
			//			erf_Classes.add(POINT_SRC_TO_LINE_ERF_LIST_TEST_CLASS_NAME);
	
	/** Frankel/USGS 1996 ERF */
	FRANKEL_96(Frankel96_EqkRupForecast.class,
			Frankel96_EqkRupForecast.NAME, PRODUCTION, false, false),

	/** Frankel/USGS 2002 Adjustable ERF */
	FRANKEL_02(Frankel02_AdjustableEqkRupForecast.class,
			Frankel02_AdjustableEqkRupForecast.NAME, PRODUCTION, false, false),
	
	/** WGCEP 2002 ERF */
	WGCEP_02(WG02_EqkRupForecast.class, WG02_EqkRupForecast.NAME, PRODUCTION, false, false),
	
	/** WGCEP 2002 ERF Epistemic List */
	WGCEP_02_LIST(WG02_ERF_Epistemic_List.class, WG02_ERF_Epistemic_List.NAME, PRODUCTION, false, true),
	
	/** WGCEP UCERF 1 */
	WGCEP_UCERF_1(WGCEP_UCERF1_EqkRupForecast.class, WGCEP_UCERF1_EqkRupForecast.NAME, PRODUCTION, false, false),

	/** PEER Area Forecast */
	PEER_AREA(PEER_AreaForecast.class, PEER_AreaForecast.NAME, PRODUCTION, false, false),

	/** PEER Non Planar Fault Forecast */
	PEER_NON_PLANAR_FAULT(PEER_NonPlanarFaultForecast.class, PEER_NonPlanarFaultForecast.NAME, PRODUCTION, false, false),

	/** PEER Multi Source Forecast */
	PEER_MULTI_SOURCE(PEER_MultiSourceForecast.class, PEER_MultiSourceForecast.NAME, PRODUCTION, false, false),

	/** PEER Logic Tree Forecast */
	PEER_LOGIC_TREE(PEER_LogicTreeERF_List.class, PEER_LogicTreeERF_List.NAME, PRODUCTION, false, false),

	// include this?
		//erf_Classes.add(STEP_FORECAST_CLASS_NAME);
	
	/** Floating Poisson Fault ERF */
	POISSON_FLOATING_FAULT(FloatingPoissonFaultERF.class, FloatingPoissonFaultERF.NAME, PRODUCTION, false, false),
	
	/** Poisson Fault ERF */
	POISSON_FAULT(PoissonFaultERF.class, PoissonFaultERF.NAME, PRODUCTION, false, false),
	
	/**  Point Source ERF */
	POINT_SOURCE(PointSourceERF.class, PointSourceERF.NAME, PRODUCTION, false, false),
	
	/**  Point Source Multi Vert ERF */
	POINT_SOURCE_MULTI_VERT(Point2MultVertSS_FaultERF.class, Point2MultVertSS_FaultERF.NAME, PRODUCTION, false, false),

	/**  Point Source Multi Vert ERF */
	POINT_SOURCE_MULTI_VERT_LIST(Point2MultVertSS_FaultERF_List.class,
			Point2MultVertSS_FaultERF_List.NAME, PRODUCTION, false, true),

	/** WGCEP UCERF 2 ERF */
	UCERF_2(UCERF2.class, UCERF2.NAME, PRODUCTION, false, false),
	
	/** WGCEP UCERF 2 Time Independent Epistemic List */
	UCERF_2_TIME_INDEP_LIST(UCERF2_TimeIndependentEpistemicList.class,
			UCERF2_TimeIndependentEpistemicList.NAME, PRODUCTION, false, true),
	
	/** WGCEP Mean UCERF 2 */
	MEAN_UCERF_2(MeanUCERF2.class, MeanUCERF2.NAME, PRODUCTION, false, false),

	/** Yucca Mountain ERF */
	YUCCA_MOUNTAIN(YuccaMountainERF.class, YuccaMountainERF.NAME, PRODUCTION, false, false),
	
	/** Yucca Mountain ERF List */
	YUCCA_MOUNTAIN_LIST(YuccaMountainERF_List.class, YuccaMountainERF_List.NAME, PRODUCTION, false, true),
	
	// DEVELOPMENT
	
	/** NSHMP CEUS 2008 ERF */
	NSHMP_CEUS_08(NSHMP08_CEUS_ERF.class, NSHMP08_CEUS_ERF.NAME, DEVELOPMENT, false, false),

	/** GEM1 South America ERF */
	GEM1_SOUTH_AMERICA(GEM1SouthAmericaERF.class, GEM1SouthAmericaERF.NAME, DEVELOPMENT, false, false),

	/** GEM1 Central/Eastern United States ERF */
	GEM1_CEUS(GEM1_CEUS_ERF.class, GEM1_CEUS_ERF.NAME, DEVELOPMENT, false, false),

	/** GEM1 Western United States ERF */
	GEM1_WEUS(GEM1_WEUS_ERF.class, GEM1_WEUS_ERF.NAME, DEVELOPMENT, false, false),

	/** GEM1 GSHAP Africa ERF */
	GEM1_GSHAP_AFRICA(GEM1_GSHAP_Africa_ERF.class, GEM1_GSHAP_Africa_ERF.NAME, DEVELOPMENT, false, false),

	/** GEM1 GSHAP South/East Asia ERF */
	GEM1_GSHAP_WE_ASIA(GEM1_GSHAP_SE_Asia_ERF.class, GEM1_GSHAP_SE_Asia_ERF.NAME, DEVELOPMENT, false, false),

	/** GEM1 NSHMP South/East Asia ERF */
	GEM1_NSHMP_WE_ASIA(GEM1_NSHMP_SE_Asia_ERF.class, GEM1_NSHMP_SE_Asia_ERF.NAME, DEVELOPMENT, false, false),
	
	/** STEP Alaska Forecast */
	STEP_ALASKA(STEP_AlaskanPipeForecast.class, STEP_AlaskanPipeForecast.NAME, DEVELOPMENT, false, false),
	
	// add NZ?
	
	// EXPERIMENTAL
	
	/** URS modified MeanUCERF2 */
	URS_MEAN_UCERF_2(URS_MeanUCERF2.class, URS_MeanUCERF2.NAME, EXPERIMENTAL, false, false),
	
	/** CyberShake ERF that wraps UCERF2 for use with the CyberShake Fake IMR */
	CYBERSHAKE_UCERF2_WRAPPER(CyberShakeUCERFWrapper_ERF.class,
			CyberShakeUCERFWrapper_ERF.NAME, EXPERIMENTAL, false, false),
	
	INVERSION_SOLUTION_ERF(InversionSolutionERF.class, InversionSolutionERF.NAME, EXPERIMENTAL, false, false),
	
	// DEPRECATED
	/** WGCEP UCERF 2 Version 2.3 ERF */
	UCERF_2_VER_2_3(
			org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2.class,
			org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.UCERF2.NAME,
			DEPRECATED, false, false),
	
	/** WGCEP UCERF 2 Version 2.3 */
	MEAN_UCERF_2_VER_2_3(
			org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.MeanUCERF2.MeanUCERF2.class,
			org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF_2_3.MeanUCERF2.MeanUCERF2.NAME,
			DEPRECATED, false, false),
	
	/* 
	 * ********************
	 * REMOTE ERFS
	 * ********************
	 */
	
	// PRODUCTION
	
	/** Frankel/USGS 1996 Adjustable Remote */
	FRANKEL_ADJUSTABLE_96_REMOTE(Frankel96_AdjustableEqkRupForecastClient.class,
			Frankel96_AdjustableEqkRupForecastClient.NAME, PRODUCTION, true, false),
	
	/** Frankel/USGS 2002 Remote */
	FRANKEL_02_REMOTE(Frankel02_AdjustableEqkRupForecastClient.class,
			Frankel02_AdjustableEqkRupForecastClient.NAME, PRODUCTION, true, false),
	
	/** WGCEP UCERF1 Remote */
	WGCEP_UCERF_1_REMOTE(WGCEP_UCERF1_EqkRupForecastClient.class,
			WGCEP_UCERF1_EqkRupForecastClient.NAME, PRODUCTION, true, false),
	
	// include?
			// erf_Classes.add(RMI_STEP_FORECAST_CLASS_NAME);
	
	/** Floating Poisson Fault Remote */
	POISSON_FLOATING_FAULT_REMOTE(FloatingPoissonFaultERF_Client.class,
			FloatingPoissonFaultERF_Client.NAME, PRODUCTION, true, false),
	
	/** Poisson Fault Remote */
	POISSON_FAULT_REMOTE(PoissonFaultERF_Client.class,
			PoissonFaultERF_Client.NAME, PRODUCTION, true, false),
	
	/** PEER Area Forecast */
	PEER_AREA_REMOTE(PEER_AreaForecastClient.class, PEER_AreaForecastClient.NAME, PRODUCTION, true, false),

	/** PEER Non Planar Fault Forecast */
	PEER_NON_PLANAR_FAULT_REMOTE(PEER_NonPlanarFaultForecastClient.class,
			PEER_NonPlanarFaultForecastClient.NAME, PRODUCTION, true, false),

	/** PEER Multi Source Forecast */
	PEER_MULTI_SOURCE_REMOTE(PEER_MultiSourceForecastClient.class, PEER_MultiSourceForecastClient.NAME, PRODUCTION, true, false),

	/** PEER Logic Tree Forecast */
	PEER_LOGIC_TREE_REMOTE(PEER_LogicTreeERF_ListClient.class, PEER_LogicTreeERF_ListClient.NAME, PRODUCTION, true, false),
	
	/**  Point Source Multi Vert ERF */
	POINT_SOURCE_MULTI_VERT_REMOTE(Point2MultVertSS_FaultERF_Client.class,
			Point2MultVertSS_FaultERF_Client.NAME, PRODUCTION, true, false),
	
	/**  Point Source Multi Vert ERF */
	POINT_SOURCE_MULTI_VERT_LIST_REMOTE(Point2MultVertSS_FaultERF_ListClient.class,
			Point2MultVertSS_FaultERF_ListClient.NAME, PRODUCTION, true, true),
	
	/** WGCEP 2002 Fortran Wrapped ERF */
	WGCEP_02_FORTRAN_WRAPPED(WG02_FortranWrappedERF_EpistemicListClient.class,
			WG02_FortranWrappedERF_EpistemicListClient.NAME, PRODUCTION, true, true),
			
	// DEVELOPMENT
	
	/** STEP Alaska Remote */
	STEP_ALASKA_REMOTE(STEP_AlaskanPipeForecastClient.class,
			STEP_AlaskanPipeForecastClient.NAME, DEVELOPMENT, true, false);
	
	// EXPERIMENTAL
	
	// DEPRECATED
	
	private Class<? extends BaseERF> clazz;
	private String name;
	private DevStatus status;
	private boolean remote;
	private boolean erfList;

	private ERF_Ref(Class<? extends BaseERF> clazz,
		String name, DevStatus status, boolean remote, boolean erfList) {
		this.clazz = clazz;
		this.name = name;
		this.status = status;
		this.remote = remote;
		this.erfList = erfList;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Returns the development status of the referenced
	 * <code>EqkRupForecastBaseAPI</code>.
	 * @return the development status
	 */
	public DevStatus status() {
		return status;
	}
	
	/**
	 * @return true if this is a remote (RMI based) ERF, false otherwise
	 */
	public boolean isRemote() {
		return remote;
	}
	
	/**
	 * @return true if this is an ERF Epistemic List, false otherwise
	 */
	public boolean isERFList() {
		return erfList;
	}

	/**
	 * Returns a new instance of the ERF represented by
	 * this reference.
	 * @return a new <code>EqkRupForecastBaseAPI</code> instance
	 */
	public BaseERF instance() {
		try {
			Constructor<? extends BaseERF> con = clazz
				.getConstructor();
			return con.newInstance();
		} catch (Exception e) {
			// TODO init logging
			RuntimeException re;
			if (e instanceof RuntimeException)
				re = (RuntimeException)e;
			else
				re = new RuntimeException(e);
			throw re;
		}
	}

	/**
	 * Convenience method to return references for all
	 * <code>EqkRupForecastBaseAPI</code> implementations that are currently
	 * production quality (i.e. fully tested and documented), under development,
	 * or experimental. The <code>Set</code> of references returned does not
	 * include deprecated references.
	 * @param remote switch for local or remote ERFs
	 * @param includeListERFs if true, Epistemic List ERFs will be included, otherwise
	 * they will be excluded
	 * @return reference <code>Set</code> of all non-deprecated
	 *         <code>EqkRupForecastBaseAPI</code>s
	 * @see DevStatus
	 */
	public static Set<ERF_Ref> get(boolean remote, boolean includeListERFs) {
		return get(remote, includeListERFs, PRODUCTION, DEVELOPMENT, EXPERIMENTAL);
	}
	
	/**
	 * Convenience method to return references for all
	 * <code>EqkRupForecastBaseAPI</code> implementations that should be included
	 * in applications with the given ServerPrefs. Production applications only include
	 * production IMRs, and development applications include everything but
	 * deprecated IMRs.
	 * @param remote switch for local or remote ERFs
	 * @param includeListERFs if true, Epistemic List ERFs will be included, otherwise
	 * they will be excluded
	 * @param prefs <code>ServerPrefs</code> instance for which IMRs should be selected
	 * @return
	 */
	public static Set<ERF_Ref> get(boolean remote, boolean includeListERFs, ServerPrefs prefs) {
		if (prefs == ServerPrefs.DEV_PREFS)
			return get(remote, includeListERFs, PRODUCTION, DEVELOPMENT, EXPERIMENTAL);
		else if (prefs == ServerPrefs.PRODUCTION_PREFS)
			return get(remote, includeListERFs, PRODUCTION);
		else
			throw new IllegalArgumentException("Unknown ServerPrefs instance: "+prefs);
	}

	/**
	 * Convenience method to return references to
	 * <code>EqkRupForecastBaseAPI</code> implementations at the specified
	 * levels of development.
	 * @param remote switch for local or remote ERFs
	 * @param includeListERFs if true, Epistemic List ERFs will be included, otherwise
	 * they will be excluded
	 * @param stati the development level(s) of the
	 *        <code>EqkRupForecastBaseAPI</code> references to be retrieved
	 * @return a <code>Set</code> of <code>EqkRupForecastBaseAPI</code>
	 *         references
	 * @see DevStatus
	 */
	public static Set<ERF_Ref> get(boolean remote, boolean includeListERFs, DevStatus... stati) {
		EnumSet<ERF_Ref> erfSet = EnumSet.allOf(ERF_Ref.class);
		for (ERF_Ref erf : erfSet) {
			if (!ArrayUtils.contains(stati, erf.status))
				erfSet.remove(erf);
			if (remote != erf.isRemote())
				erfSet.remove(erf);
			if (erf.isERFList() && !includeListERFs)
				erfSet.remove(erf);
		}
		return erfSet;
	}

}
