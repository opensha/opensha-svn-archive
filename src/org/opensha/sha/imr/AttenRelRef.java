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

import org.apache.commons.lang.ArrayUtils;
import org.opensha.commons.param.event.ParameterChangeWarningListener;
import org.opensha.commons.util.DevStatus;
import org.opensha.commons.util.ServerPrefs;
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
	CB_2008(CB_2008_AttenRel.class, CB_2008_AttenRel.NAME, PRODUCTION),

	/** [NGA] Boore & Atkinson (2008) */
	BA_2008(BA_2008_AttenRel.class, BA_2008_AttenRel.NAME, PRODUCTION),

	/** [NGA] Abrahamson & Silva (2008) */
	AS_2008(AS_2008_AttenRel.class, AS_2008_AttenRel.NAME, PRODUCTION),

	/** [NGA] Chiou & Youngs (2008) */
	CY_2008(CY_2008_AttenRel.class, CY_2008_AttenRel.NAME, PRODUCTION),

	/** Goulet et al. (2006) */
	GOULET_2006(GouletEtAl_2006_AttenRel.class, GouletEtAl_2006_AttenRel.NAME,
			PRODUCTION),

	/** Zhao et al. (2006) */
	ZHAO_2006(ZhaoEtAl_2006_AttenRel.class, ZhaoEtAl_2006_AttenRel.NAME,
			PRODUCTION),

	/** Choi & Stewart (2005) */
	CS_2005(CS_2005_AttenRel.class, CS_2005_AttenRel.NAME, PRODUCTION),

	/** Bazzuro & Cornell (2004) */
	BC_2004(BC_2004_AttenRel.class, BC_2004_AttenRel.NAME, PRODUCTION),

	/** USGS combined */
	USGS_2004_COMBO(USGS_Combined_2004_AttenRel.class,
			USGS_Combined_2004_AttenRel.NAME, PRODUCTION),

	/** Baturay & Stewart (2003) */
	BS_2003(BS_2003_AttenRel.class, BS_2003_AttenRel.NAME, PRODUCTION),

	/** Campbell & Bozorgnia (2003) */
	CB_2003(CB_2003_AttenRel.class, CB_2003_AttenRel.NAME, PRODUCTION),

	/** ShakeMap */
	SHAKE_2003(ShakeMap_2003_AttenRel.class, ShakeMap_2003_AttenRel.NAME,
			PRODUCTION),

	/** Field (2000) */
	FIELD_2000(Field_2000_AttenRel.class, Field_2000_AttenRel.NAME, PRODUCTION),
	/** Abrahamson (2000) */
	ABRAHAM_2000(Abrahamson_2000_AttenRel.class, Abrahamson_2000_AttenRel.NAME,
			PRODUCTION),
	/** McVerry et al. (2000) */
	MCVERRY_2000(McVerryetal_2000_AttenRel.class,
			McVerryetal_2000_AttenRel.NAME, PRODUCTION),

	/** Sadigh et al. (1999) */
	SADIGH_1999(SEA_1999_AttenRel.class, SEA_1999_AttenRel.NAME, PRODUCTION),

	/** Abrahmson and Silva (1997) */
	AS_1997(AS_1997_AttenRel.class, AS_1997_AttenRel.NAME, PRODUCTION),

	/** Boore, Joyner & Fumal (1997) */
	BJF_1997(BJF_1997_AttenRel.class, BJF_1997_AttenRel.NAME, PRODUCTION),

	/** Campbell (1997) */
	CAMPBELL_1997(Campbell_1997_AttenRel.class, Campbell_1997_AttenRel.NAME,
			PRODUCTION),
	/** Sadigh et al. (1997) */
	SADIGH_1997(SadighEtAl_1997_AttenRel.class, SadighEtAl_1997_AttenRel.NAME,
			PRODUCTION),

	/** Dahle et al. (1995) */
	DAHLE_1995(DahleEtAl_1995_AttenRel.class, DahleEtAl_1995_AttenRel.NAME,
			PRODUCTION),

	// DEVELOPMENT

	/** Interpolation between periods using BA. */
	BA_2008_INTERP(CY_2008_AttenRel.class, CY_2008_AttenRel.NAME, DEVELOPMENT),

	/** Average of 4 NGA's. */
	NGA_2008_4AVG(NGA_2008_Averaged_AttenRel.class,
			NGA_2008_Averaged_AttenRel.NAME, DEVELOPMENT),

	/** Average of 3 NGA's. */
	NGA_2008_3AVG(NGA_2008_Averaged_AttenRel_NoAS.class,
			NGA_2008_Averaged_AttenRel_NoAS.NAME, DEVELOPMENT),

	NSHMP_2008(NSHMP_2008_CA.class, NSHMP_2008_CA.NAME, DEVELOPMENT),

	// EXPERIMENTAL
	
	/** Cybershake fake attnuation relation */
	CYBERSHAKE(CyberShakeIMR.class, CyberShakeIMR.NAME, EXPERIMENTAL),

	// DEPRECATED

	/** [NGA prelim] Campbell & Bozorgnia (2008) */
	CB_2006(CB_2006_AttenRel.class, CB_2006_AttenRel.NAME, DEPRECATED),

	/** [NGA prelim] Boore & Atkinson (2008) */
	BA_2006(BA_2006_AttenRel.class, BA_2006_AttenRel.NAME, DEPRECATED),

	/** [NGA prelim] Abrahamson & Silva (2008) */
	AS_2005(AS_2005_AttenRel.class, AS_2005_AttenRel.NAME, DEPRECATED),

	/** [NGA prelim] Chiou & Youngs (2008) */
	CY_2006(CY_2006_AttenRel.class, CY_2006_AttenRel.NAME, DEPRECATED),

	/** Site specific model */
	SITESPEC_2006(SiteSpecific_2006_AttenRel.class,
			SiteSpecific_2006_AttenRel.NAME, DEPRECATED),

	/** Wells & Coppersmith (1994) displacement model */
	WC_1994(WC94_DisplMagRel.class, WC94_DisplMagRel.NAME, DEPRECATED);

	private Class<? extends AttenuationRelationship> clazz;
	private String name;
	private DevStatus status;

	private AttenRelRef(Class<? extends AttenuationRelationship> clazz,
		String name, DevStatus status) {
		this.clazz = clazz;
		this.name = name;
		this.status = status;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Returns the development status of the referenced
	 * <code>AttenuationRelationship</code>.
	 * @return the development status
	 */
	public DevStatus status() {
		return status;
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
			// TODO init logging
			e.printStackTrace();
			return null;
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
	 * <code>AttenuationRelationship</code> implementations that should be included
	 * in applications with the given ServerPrefs. Production applications only include
	 * production IMRs, and development applications include everything but
	 * deprecated IMRs.
	 * 
	 * @param prefs
	 * @return
	 */
	public static Set<AttenRelRef> get(ServerPrefs prefs) {
		if (prefs == ServerPrefs.DEV_PREFS)
			return get(PRODUCTION, DEVELOPMENT, EXPERIMENTAL);
		else if (prefs == ServerPrefs.PRODUCTION_PREFS)
			return get(PRODUCTION);
		else
			throw new IllegalArgumentException("Unknown ServerPrefs instance: "+prefs);
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
	 * @return a <code>List</code> of all non-deprecated
	 *         <code>AttenuationRelationship</code>s
	 */
	public static List<AttenuationRelationship> instanceList(
			ParameterChangeWarningListener listener, boolean sorted, ServerPrefs prefs) {
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
