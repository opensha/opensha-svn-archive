package org.opensha.sha.imr;

import static org.opensha.commons.util.DevStatus.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.opensha.commons.param.event.ParameterChangeWarningListener;
import org.opensha.commons.util.DevStatus;
import org.opensha.commons.util.ServerPrefs;
import org.opensha.nshmp2.imr.NSHMP08_CEUS;
import org.opensha.nshmp2.imr.NSHMP08_WUS;
import org.opensha.nshmp2.imr.impl.AB2006_140_AttenRel;
import org.opensha.nshmp2.imr.impl.AB2006_200_AttenRel;
import org.opensha.nshmp2.imr.impl.Campbell_2003_AttenRel;
import org.opensha.nshmp2.imr.impl.FrankelEtAl_1996_AttenRel;
import org.opensha.nshmp2.imr.impl.SilvaEtAl_2002_AttenRel;
import org.opensha.nshmp2.imr.impl.SomervilleEtAl_2001_AttenRel;
import org.opensha.nshmp2.imr.impl.TP2005_AttenRel;
import org.opensha.nshmp2.imr.impl.ToroEtAl_1997_AttenRel;
import org.opensha.sha.cybershake.openshaAPIs.CyberShakeIMR;
import org.opensha.sha.imr.attenRelImpl.AS_1997_AttenRel;
import org.opensha.sha.imr.attenRelImpl.AS_2005_AttenRel;
import org.opensha.sha.imr.attenRelImpl.AS_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.Abrahamson_2000_AttenRel;
import org.opensha.sha.imr.attenRelImpl.BA_2006_AttenRel;
import org.opensha.sha.imr.attenRelImpl.BA_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.BC_2004_AttenRel;
import org.opensha.sha.imr.attenRelImpl.BJF_1997_AttenRel;
import org.opensha.sha.imr.attenRelImpl.BS_2003_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2003_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2006_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CS_2005_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CY_2006_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CY_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.Campbell_1997_AttenRel;
import org.opensha.sha.imr.attenRelImpl.DahleEtAl_1995_AttenRel;
import org.opensha.sha.imr.attenRelImpl.Field_2000_AttenRel;
import org.opensha.sha.imr.attenRelImpl.GouletEtAl_2006_AttenRel;
import org.opensha.sha.imr.attenRelImpl.McVerryetal_2000_AttenRel;
import org.opensha.sha.imr.attenRelImpl.NGA_2008_Averaged_AttenRel;
import org.opensha.sha.imr.attenRelImpl.NGA_2008_Averaged_AttenRel_NoAS;
import org.opensha.sha.imr.attenRelImpl.NSHMP_2008_CA;
import org.opensha.sha.imr.attenRelImpl.SEA_1999_AttenRel;
import org.opensha.sha.imr.attenRelImpl.SadighEtAl_1997_AttenRel;
import org.opensha.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.opensha.sha.imr.attenRelImpl.SiteSpecific_2006_AttenRel;
import org.opensha.sha.imr.attenRelImpl.USGS_Combined_2004_AttenRel;
import org.opensha.sha.imr.attenRelImpl.WC94_DisplMagRel;
import org.opensha.sha.imr.attenRelImpl.ZhaoEtAl_2006_AttenRel;
import org.opensha.sha.imr.attenRelImpl.SA_InterpolatedWrapperAttenRel.InterpolatedBA_2008_AttenRel;

import scratch.kevin.nga.NGAWrappers.ASK_2013_Wrapper;
import scratch.kevin.nga.NGAWrappers.BSSA_2013_Wrapper;
import scratch.kevin.nga.NGAWrappers.CB_2013_Wrapper;
import scratch.kevin.nga.NGAWrappers.CY_2013_Wrapper;
import scratch.kevin.nga.NGAWrappers.GK_2013_Wrapper;
import scratch.kevin.nga.NGAWrappers.Idriss_2013_Wrapper;

import com.sun.xml.rpc.processor.generator.writer.EnumerationSerializerWriter;

/**
 * This <code>enum</code> supplies references to
 * <code>AttenuationRelationship</code> implementations. Each reference can
 * return instances of the <code>AttenuationRelationship</code> it represents as
 * well as limited metadata such as the IMR's name and development status.
 * Static methods are provided to facilitate retrieval of specific
 * <code>Set</code>s of references and <code>List</code>s of instances.
 * 
 * @author Peter Powers
 * @version $Id$
 */
public enum AttenRelRef {

	// PRODUCTION

	/** [NGA] Campbell & Bozorgnia (2008) */
	CB_2008(CB_2008_AttenRel.class, CB_2008_AttenRel.NAME, CB_2008_AttenRel.SHORT_NAME, PRODUCTION),

	/** [NGA] Boore & Atkinson (2008) */
	BA_2008(BA_2008_AttenRel.class, BA_2008_AttenRel.NAME, BA_2008_AttenRel.SHORT_NAME, PRODUCTION),

	/** [NGA] Abrahamson & Silva (2008) */
	AS_2008(AS_2008_AttenRel.class, AS_2008_AttenRel.NAME, AS_2008_AttenRel.SHORT_NAME, PRODUCTION),

	/** [NGA] Chiou & Youngs (2008) */
	CY_2008(CY_2008_AttenRel.class, CY_2008_AttenRel.NAME, CY_2008_AttenRel.SHORT_NAME, PRODUCTION),

	/** Goulet et al. (2006) */
	GOULET_2006(GouletEtAl_2006_AttenRel.class, GouletEtAl_2006_AttenRel.NAME,
			GouletEtAl_2006_AttenRel.SHORT_NAME, PRODUCTION),

	/** Zhao et al. (2006) */
	ZHAO_2006(ZhaoEtAl_2006_AttenRel.class, ZhaoEtAl_2006_AttenRel.NAME,
			ZhaoEtAl_2006_AttenRel.SHORT_NAME, PRODUCTION),

	/** Choi & Stewart (2005) */
	CS_2005(CS_2005_AttenRel.class, CS_2005_AttenRel.NAME, CS_2005_AttenRel.SHORT_NAME, PRODUCTION),

	/** Bazzuro & Cornell (2004) */
	BC_2004(BC_2004_AttenRel.class, BC_2004_AttenRel.NAME, BC_2004_AttenRel.SHORT_NAME, PRODUCTION),

	/** USGS combined */
	USGS_2004_COMBO(USGS_Combined_2004_AttenRel.class,
			USGS_Combined_2004_AttenRel.NAME, USGS_Combined_2004_AttenRel.SHORT_NAME, PRODUCTION),

	/** Baturay & Stewart (2003) */
	BS_2003(BS_2003_AttenRel.class, BS_2003_AttenRel.NAME, BS_2003_AttenRel.SHORT_NAME, PRODUCTION),

	/** Campbell & Bozorgnia (2003) */
	CB_2003(CB_2003_AttenRel.class, CB_2003_AttenRel.NAME, CB_2003_AttenRel.SHORT_NAME, PRODUCTION),

	/** ShakeMap */
	SHAKE_2003(ShakeMap_2003_AttenRel.class, ShakeMap_2003_AttenRel.NAME,
			ShakeMap_2003_AttenRel.SHORT_NAME, PRODUCTION),

	/** Field (2000) */
	FIELD_2000(Field_2000_AttenRel.class, Field_2000_AttenRel.NAME, Field_2000_AttenRel.SHORT_NAME, PRODUCTION),
	/** Abrahamson (2000) */
	ABRAHAM_2000(Abrahamson_2000_AttenRel.class, Abrahamson_2000_AttenRel.NAME,
			Abrahamson_2000_AttenRel.SHORT_NAME, PRODUCTION),
	/** McVerry et al. (2000) */
	MCVERRY_2000(McVerryetal_2000_AttenRel.class,
			McVerryetal_2000_AttenRel.NAME, McVerryetal_2000_AttenRel.SHORT_NAME, PRODUCTION),

	/** Sadigh et al. (1999) */
	SADIGH_1999(SEA_1999_AttenRel.class, SEA_1999_AttenRel.NAME, SEA_1999_AttenRel.SHORT_NAME, PRODUCTION),

	/** Abrahmson and Silva (1997) */
	AS_1997(AS_1997_AttenRel.class, AS_1997_AttenRel.NAME, AS_1997_AttenRel.SHORT_NAME, PRODUCTION),

	/** Boore, Joyner & Fumal (1997) */
	BJF_1997(BJF_1997_AttenRel.class, BJF_1997_AttenRel.NAME, BJF_1997_AttenRel.SHORT_NAME, PRODUCTION),

	/** Campbell (1997) */
	CAMPBELL_1997(Campbell_1997_AttenRel.class, Campbell_1997_AttenRel.NAME,
			Campbell_1997_AttenRel.SHORT_NAME, PRODUCTION),
	/** Sadigh et al. (1997) */
	SADIGH_1997(SadighEtAl_1997_AttenRel.class, SadighEtAl_1997_AttenRel.NAME,
			SadighEtAl_1997_AttenRel.SHORT_NAME, PRODUCTION),

	/** Dahle et al. (1995) */
	DAHLE_1995(DahleEtAl_1995_AttenRel.class, DahleEtAl_1995_AttenRel.NAME,
			DahleEtAl_1995_AttenRel.SHORT_NAME, PRODUCTION),

	// DEVELOPMENT

	/** Interpolation between periods using BA. */
	BA_2008_INTERP(InterpolatedBA_2008_AttenRel.class,
			InterpolatedBA_2008_AttenRel.NAME, InterpolatedBA_2008_AttenRel.SHORT_NAME, DEVELOPMENT),

	/** Average of 4 NGA's. */
	NGA_2008_4AVG(NGA_2008_Averaged_AttenRel.class,
			NGA_2008_Averaged_AttenRel.NAME, NGA_2008_Averaged_AttenRel.SHORT_NAME, DEVELOPMENT),

	/** Average of 3 NGA's. */
	NGA_2008_3AVG(NGA_2008_Averaged_AttenRel_NoAS.class,
			NGA_2008_Averaged_AttenRel_NoAS.NAME, NGA_2008_Averaged_AttenRel_NoAS.SHORT_NAME, DEVELOPMENT),

	/** Average of 3 NGA's used in the 20008 NSHMP */
	NSHMP_2008(NSHMP_2008_CA.class, NSHMP_2008_CA.NAME, NSHMP_2008_CA.SHORT_NAME, DEVELOPMENT),

	/** Multiple weighted attenuation relationships used in 20008 CEUS NSHMP */
	NSHMP_2008_CEUS(NSHMP08_CEUS.class, NSHMP08_CEUS.NAME, NSHMP08_CEUS.SHORT_NAME, ERROR), // TODO set to error, see ticket #435

	/** Atkinson and Booore (2006) with 140bar stress drop. For NSHMP CEUS. */
	AB_2006_140(AB2006_140_AttenRel.class, AB2006_140_AttenRel.NAME,
			AB2006_140_AttenRel.SHORT_NAME, DEVELOPMENT),

	/** Atkinson and Booore (2006) with 140bar stress drop. For NSHMP CEUS. */
	AB_2006_200(AB2006_200_AttenRel.class, AB2006_200_AttenRel.NAME,
			AB2006_200_AttenRel.SHORT_NAME, DEVELOPMENT),

	/** Campbell CEUS (2003). For NSHMP CEUS. */
	CAMPBELL_2003(Campbell_2003_AttenRel.class, Campbell_2003_AttenRel.NAME,
			Campbell_2003_AttenRel.SHORT_NAME, DEVELOPMENT),

	/** Frankel et al. (1996). For NSHMP CEUS. */
	FEA_1996(FrankelEtAl_1996_AttenRel.class, FrankelEtAl_1996_AttenRel.NAME,
			FrankelEtAl_1996_AttenRel.SHORT_NAME, ERROR), // TODO set to error because of ticket #366

	/** Somerville et al. (2001). For NSHMP CEUS. */
	SOMERVILLE_2001(SomervilleEtAl_2001_AttenRel.class,
			SomervilleEtAl_2001_AttenRel.NAME, SomervilleEtAl_2001_AttenRel.SHORT_NAME, DEVELOPMENT),

	/** Silva et al. (2002). For NSHMP CEUS. */
	SILVA_2002(SilvaEtAl_2002_AttenRel.class, SilvaEtAl_2002_AttenRel.NAME,
			SilvaEtAl_2002_AttenRel.SHORT_NAME, DEVELOPMENT),

	/** Toro et al. (1997). For NSHMP CEUS. */
	TORO_1997(ToroEtAl_1997_AttenRel.class, ToroEtAl_1997_AttenRel.NAME,
			ToroEtAl_1997_AttenRel.SHORT_NAME, DEVELOPMENT),

	/** Tavakoli and Pezeshk (2005). For NSHMP CEUS. */
	TP_2005(TP2005_AttenRel.class, TP2005_AttenRel.NAME, TP2005_AttenRel.SHORT_NAME, DEVELOPMENT),

	// EXPERIMENTAL

	/** Cybershake fake attnuation relation */
	CYBERSHAKE(CyberShakeIMR.class, CyberShakeIMR.NAME, CyberShakeIMR.SHORT_NAME, EXPERIMENTAL),
	
	ASK_2013(ASK_2013_Wrapper.class,scratch.peter.nga.ASK_2013.NAME,  scratch.peter.nga.ASK_2013.SHORT_NAME, EXPERIMENTAL),
	
	BSSA_2013(BSSA_2013_Wrapper.class, scratch.peter.nga.BSSA_2013.NAME, scratch.peter.nga.BSSA_2013.SHORT_NAME, EXPERIMENTAL),
	
	CB_2013(CB_2013_Wrapper.class,scratch.peter.nga.CB_2013.NAME, scratch.peter.nga.CB_2013.SHORT_NAME, EXPERIMENTAL),
	
	CY_2013(CY_2013_Wrapper.class, scratch.peter.nga.CY_2013.NAME, scratch.peter.nga.CY_2013.SHORT_NAME, EXPERIMENTAL),
	
	GK_2013(GK_2013_Wrapper.class, scratch.peter.nga.GK_2013.NAME, scratch.peter.nga.GK_2013.SHORT_NAME, EXPERIMENTAL),
	
	Idriss_2013(Idriss_2013_Wrapper.class, scratch.peter.nga.Idriss_2013.NAME, scratch.peter.nga.Idriss_2013.SHORT_NAME, EXPERIMENTAL),

	// DEPRECATED

	/** [NGA prelim] Campbell & Bozorgnia (2008) */
	CB_2006(CB_2006_AttenRel.class, CB_2006_AttenRel.NAME, CB_2006_AttenRel.SHORT_NAME, DEPRECATED),

	/** [NGA prelim] Boore & Atkinson (2008) */
	BA_2006(BA_2006_AttenRel.class, BA_2006_AttenRel.NAME, BA_2006_AttenRel.SHORT_NAME, DEPRECATED),

	/** [NGA prelim] Abrahamson & Silva (2008) */
	AS_2005(AS_2005_AttenRel.class, AS_2005_AttenRel.NAME, AS_2005_AttenRel.SHORT_NAME, DEPRECATED),

	/** [NGA prelim] Chiou & Youngs (2008) */
	CY_2006(CY_2006_AttenRel.class, CY_2006_AttenRel.NAME, CY_2006_AttenRel.SHORT_NAME, DEPRECATED),

	/** Site specific model */
	SITESPEC_2006(SiteSpecific_2006_AttenRel.class,
			SiteSpecific_2006_AttenRel.NAME, SiteSpecific_2006_AttenRel.SHORT_NAME, DEPRECATED),

	/** Wells & Coppersmith (1994) displacement model */
	WC_1994(WC94_DisplMagRel.class, WC94_DisplMagRel.NAME, WC94_DisplMagRel.SHORT_NAME, DEPRECATED);

	private Class<? extends AttenuationRelationship> clazz;
	private String name;
	private String shortName;
	private DevStatus status;

	private AttenRelRef(Class<? extends AttenuationRelationship> clazz,
		String name, String shortName, DevStatus status) {
		this.clazz = clazz;
		this.name = name;
		this.shortName = shortName;
		this.status = status;
	}

	@Override
	public String toString() {
		return name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return shortName;
	}

	/**
	 * Returns the development status of the referenced
	 * <code>AttenuationRelationship</code>.
	 * @return the development status
	 */
	public DevStatus status() {
		return status;
	}
	
	public Class<? extends AttenuationRelationship> getAttenRelClass() {
		return clazz;
	}

	/**
	 * Returns a new instance of the attenuation relationship represented by
	 * this reference.
	 * @param listener to initialize instances with; may be <code>null</code>
	 * @return a new <code>AttenuationRelationship</code> instance
	 */
	public AttenuationRelationship instance(
			ParameterChangeWarningListener listener) {
		try {
			Object[] args = new Object[] { listener };
			Class<?>[] params = new Class[] { ParameterChangeWarningListener.class };
			Constructor<? extends AttenuationRelationship> con = clazz
				.getConstructor(params);
			return con.newInstance(args);
		} catch (Exception e) {
			// now try a no arg constructor
			try {
				Object[] args = new Object[] {};
				Class<?>[] params = new Class[] {};
				Constructor<? extends AttenuationRelationship> con = clazz
					.getConstructor(params);
				return con.newInstance(args);
			} catch (Exception e1) {
				// TODO init logging
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Convenience method to return references for all
	 * <code>AttenuationRelationship</code> implementations that are currently
	 * production quality (i.e. fully tested and documented), under development,
	 * or experimental. The <code>Set</code> of references returned does not
	 * include deprecated references.
	 * @return reference <code>Set</code> of all non-deprecated
	 *         <code>AttenuationRelationship</code>s
	 * @see DevStatus
	 */
	public static Set<AttenRelRef> get() {
		return get(PRODUCTION, DEVELOPMENT, EXPERIMENTAL);
	}

	/**
	 * Convenience method to return references for all
	 * <code>AttenuationRelationship</code> implementations that should be
	 * included in applications with the given ServerPrefs. Production
	 * applications only include production IMRs, and development applications
	 * include everything but deprecated IMRs.
	 * 
	 * @param prefs <code>ServerPrefs</code> instance for which IMRs should be
	 *        selected
	 * @return
	 */
	public static Set<AttenRelRef> get(ServerPrefs prefs) {
		if (prefs == ServerPrefs.DEV_PREFS)
			return get(PRODUCTION, DEVELOPMENT, EXPERIMENTAL);
		else if (prefs == ServerPrefs.PRODUCTION_PREFS)
			return get(PRODUCTION);
		else
			throw new IllegalArgumentException(
				"Unknown ServerPrefs instance: " + prefs);
	}

	/**
	 * Convenience method to return references to
	 * <code>AttenuationRelationship</code> implementations at the specified
	 * levels of development.
	 * @param stati the development level(s) of the
	 *        <code>AttenuationRelationship</code> references to be retrieved
	 * @return a <code>Set</code> of <code>AttenuationRelationship</code>
	 *         references
	 * @see DevStatus
	 */
	public static Set<AttenRelRef> get(DevStatus... stati) {
		EnumSet<AttenRelRef> ariSet = EnumSet.allOf(AttenRelRef.class);
		for (AttenRelRef ari : ariSet) {
			if (!ArrayUtils.contains(stati, ari.status)) ariSet.remove(ari);
		}
		return ariSet;
	}

	/**
	 * Returns a <code>List</code> of <code>AttenuationRelationship</code>
	 * instances that are currently production quality (i.e. fully tested and
	 * documented), under development, or experimental. The list of
	 * <code>AttenuationRelationship</code>s returned does not include
	 * deprecated implementations.
	 * @param listener to initialize instances with; may be <code>null</code>
	 * @param sorted whether to sort the list by name
	 * @return a <code>List</code> of all non-deprecated
	 *         <code>AttenuationRelationship</code>s
	 */
	public static List<AttenuationRelationship> instanceList(
			ParameterChangeWarningListener listener, boolean sorted) {
		return buildInstanceList(get(), listener, sorted);
	}

	/**
	 * Returns a <code>List</code> of <code>AttenuationRelationship</code>
	 * instances that are appropriate for an application with the given
	 * <code>ServerPrefs</code>.
	 * @param listener to initialize instances with; may be <code>null</code>
	 * @param sorted whether to sort the list by name
	 * @param prefs <code>ServerPrefs</code> instance for which IMRs should be
	 *        selected
	 * @return a <code>List</code> of all non-deprecated
	 *         <code>AttenuationRelationship</code>s
	 */
	public static List<AttenuationRelationship> instanceList(
			ParameterChangeWarningListener listener, boolean sorted,
			ServerPrefs prefs) {
		return buildInstanceList(get(prefs), listener, sorted);
	}

	/**
	 * Returns a <code>List</code> of <code>AttenuationRelationship</code>
	 * instances specified by the supplied <code>Collection</code> of
	 * references.
	 * @param listener to initialize instances with; may be <code>null</code>
	 * @param sorted whether to sort the list by name
	 * @param refs to instances to retrieve
	 * @return a <code>List</code> of all non-deprecated
	 *         <code>AttenuationRelationship</code>s
	 */
	public static List<AttenuationRelationship> instanceList(
			ParameterChangeWarningListener listener, boolean sorted,
			Collection<AttenRelRef> refs) {
		return buildInstanceList(refs, listener, sorted);
	}

	/**
	 * Returns a <code>List</code> of <code>AttenuationRelationship</code>
	 * instances specified by the supplied references.
	 * @param listener to initialize instances with; may be <code>null</code>
	 * @param sorted whether to sort the list by name
	 * @param refs to instances to retrieve
	 * @return a <code>List</code> of all non-deprecated
	 *         <code>AttenuationRelationship</code>s
	 */
	public static List<AttenuationRelationship> instanceList(
			ParameterChangeWarningListener listener, boolean sorted,
			AttenRelRef... refs) {
		return buildInstanceList(Arrays.asList(refs), listener, sorted);
	}

	/**
	 * Returns a <code>List</code> of <code>AttenuationRelationship</code>
	 * instances at a specified level of development.
	 * @param listener to initialize instances with; may be <code>null</code>
	 * @param sorted whether to sort the list by name
	 * @param stati the development level(s) of the
	 *        <code>AttenuationRelationship</code> references to be retrieved
	 * @return a <code>List</code> of <code>AttenuationRelationship</code>s
	 */
	public static List<AttenuationRelationship> instanceList(
			ParameterChangeWarningListener listener, boolean sorted,
			DevStatus... stati) {
		return buildInstanceList(get(stati), listener, sorted);
	}

	private static List<AttenuationRelationship> buildInstanceList(
			Collection<AttenRelRef> arrSet,
			ParameterChangeWarningListener listener, boolean sorted) {
		List<AttenuationRelationship> arList = new ArrayList<AttenuationRelationship>();
		for (AttenRelRef arr : arrSet) {
			arList.add(arr.instance(listener));
		}
		if (sorted) Collections.sort(arList);
		return arList;
	}

}
