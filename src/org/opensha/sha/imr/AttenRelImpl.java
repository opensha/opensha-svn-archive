package org.opensha.sha.imr;

import static org.opensha.commons.util.DevStatus.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.opensha.commons.param.event.ParameterChangeWarningListener;
import org.opensha.commons.util.DevStatus;
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
import org.opensha.sha.imr.attenRelImpl.SEA_1999_AttenRel;
import org.opensha.sha.imr.attenRelImpl.SadighEtAl_1997_AttenRel;
import org.opensha.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.opensha.sha.imr.attenRelImpl.SiteSpecific_2006_AttenRel;
import org.opensha.sha.imr.attenRelImpl.USGS_Combined_2004_AttenRel;
import org.opensha.sha.imr.attenRelImpl.WC94_DisplMagRel;
import org.opensha.sha.imr.attenRelImpl.ZhaoEtAl_2006_AttenRel;

import com.sun.xml.rpc.processor.generator.writer.EnumerationSerializerWriter;

/**
 * This <code>enum</code> facilitates access to
 * <code>AttenuationRelationship</code> implementations. Each member can return
 * instances of the <code>AttenuationRelationship</code> it represents as well
 * as limited metadata such as the IMR's name and development status.
 * 
 * @author Peter Powers
 * @version $Id$
 */
public enum AttenRelImpl {

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
	DEHLE_1995(DahleEtAl_1995_AttenRel.class, DahleEtAl_1995_AttenRel.NAME,
			PRODUCTION),

	/** Cybershake fake attnuation relation */
	CYBERSHAKE(CyberShakeIMR.class, CyberShakeIMR.NAME, PRODUCTION),

	// DEVELOPMENT

	/** Interpolation between periods using BA. */
	BA_2008_INTERP(CY_2008_AttenRel.class, CY_2008_AttenRel.NAME, DEVELOPMENT),
	/** Average of 4 NGA's. */
	NGA_2008_4AVG(NGA_2008_Averaged_AttenRel.class,
			NGA_2008_Averaged_AttenRel.NAME, DEVELOPMENT),
	/** Average of 3 NGA's. */
	NGA_2008_3AVG(NGA_2008_Averaged_AttenRel_NoAS.class,
			NGA_2008_Averaged_AttenRel_NoAS.NAME, DEVELOPMENT),

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

	private AttenRelImpl(Class<? extends AttenuationRelationship> clazz,
		String name, DevStatus status) {
		this.clazz = clazz;
		this.name = name;
		this.status = status;
	}

	/**
	 * Returns a new instance of the attenuation relationship represented by
	 * this reference.
	 * @param listener may be <code>null</code>
	 * @return a new attenuation relationship instance
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
	 * Returns a <code>List</code> of <code>AttenuationRelationship</code> instances at a specified level of development.
	 * @param status 
	 * @param listener
	 * @param sorted whether to sort the list by name
	 * @return a <code>List</code> of <code>AttenuationRelationship</code>s
	 */
	public static List<AttenuationRelationship> instanceList(DevStatus status, ParameterChangeWarningListener listener, boolean sorted) {
		return buildInstanceList(get(status), listener, sorted);
	}
	
	/**
	 * @param listener
	 * @param sorted whether to sort the list by name
	 * @return a <code>List</code> of all non-deprecated <code>AttenuationRelationship</code>s
	 */
	public static List<AttenuationRelationship> instanceList(ParameterChangeWarningListener listener, boolean sorted) {
		return buildInstanceList(get(), listener, sorted);
	}
	
	private static List<AttenuationRelationship> buildInstanceList(Set<AttenRelImpl> ariSet, ParameterChangeWarningListener listener, boolean sorted) {
		List<AttenuationRelationship> arList = new ArrayList<AttenuationRelationship>();
		for (AttenRelImpl ari : ariSet) {
			arList.add(ari.instance(listener));
		}
		if (sorted) Collections.sort(arList);
		return arList;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Returns the development status of the referenced <code>AttenuationRelationship</code>.
	 * @return the development status
	 */
	public DevStatus status() {
		return status;
	}

	/**
	 * Convenience method to return references to <code>AttenuationRelationship</code>
	 * implementations at a specified level oof development.
	 * @param status of <code>AttenuationRelationship</code> references to be retreived
	 * @return a set of <code>AttenuationRelationship</code> references
	 * @see DevStatus
	 */
	public static Set<AttenRelImpl> get(DevStatus status) {
		EnumSet<AttenRelImpl> ariSet = EnumSet.allOf(AttenRelImpl.class);
		for (AttenRelImpl ari : ariSet) {
			if (ari.status != status) ariSet.remove(ari);
		}
		return ariSet;
	}

	/**
	 * Convenience method to return references for all <code>AttenuationRelationship</code>
	 * implementations that are currently production quality (i.e. fully tested
	 * and documented), under development, or experimental. The set of
	 * references returned does not, however, include deprecated references.
	 * @return reference <code>Set</code> of all non-deprecated <code>AttenuationRelationship</code>s
	 * @see DevStatus
	 */
	public static Set<AttenRelImpl> get() {
		EnumSet<AttenRelImpl> ariSet = EnumSet.allOf(AttenRelImpl.class);
		for (AttenRelImpl ari : ariSet) {
			if (ari.status == DEPRECATED) ariSet.remove(ari);
		}
		return ariSet;
	}

}
