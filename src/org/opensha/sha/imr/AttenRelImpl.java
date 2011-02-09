package org.opensha.sha.imr;

import static org.opensha.commons.util.DevStatus.*;

import org.opensha.commons.util.DevStatus;
import org.opensha.sha.cybershake.openshaAPIs.CyberShakeIMR;
import org.opensha.sha.imr.attenRelImpl.AS_1997_AttenRel;
import org.opensha.sha.imr.attenRelImpl.AS_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.Abrahamson_2000_AttenRel;
import org.opensha.sha.imr.attenRelImpl.BA_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.BC_2004_AttenRel;
import org.opensha.sha.imr.attenRelImpl.BJF_1997_AttenRel;
import org.opensha.sha.imr.attenRelImpl.BS_2003_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2003_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CS_2005_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CY_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.Campbell_1997_AttenRel;
import org.opensha.sha.imr.attenRelImpl.Field_2000_AttenRel;
import org.opensha.sha.imr.attenRelImpl.GouletEtAl_2006_AttenRel;
import org.opensha.sha.imr.attenRelImpl.McVerryetal_2000_AttenRel;
import org.opensha.sha.imr.attenRelImpl.NGA_2008_Averaged_AttenRel;
import org.opensha.sha.imr.attenRelImpl.SadighEtAl_1997_AttenRel;
import org.opensha.sha.imr.attenRelImpl.ShakeMap_2003_AttenRel;
import org.opensha.sha.imr.attenRelImpl.USGS_Combined_2004_AttenRel;
import org.opensha.sha.imr.attenRelImpl.ZhaoEtAl_2006_AttenRel;
import org.opensha.sha.imr.attenRelImpl.depricated.BA_2006_AttenRel;
import org.opensha.sha.imr.attenRelImpl.depricated.CB_2006_AttenRel;
import org.opensha.sha.imr.attenRelImpl.depricated.CY_2006_AttenRel;

/**
 * This <code>enum</code> facilitates access to
 * <code>AttenuationRelationship</code> implementations. Each member can return
 * instances of the <code>AttenuationRelationship</code> it represents as well
 * as limited metadata such as the IMR's name and development status.
 * 
 * @author Peter Powers
 * @version $Id:$
 */
public enum AttenRelImpl {

	/** Campbell & Bozorgnia (2008) */
	CB_2008(CB_2008_AttenRel.NAME, PRODUCTION),

	/** Boore & Atkinson (2008) */
	BA_2008(BA_2008_AttenRel.NAME, PRODUCTION),

	AS_2008(AS_2008_AttenRel.NAME, PRODUCTION),

	CY_2008(CY_2008_AttenRel.NAME, PRODUCTION);

	// CB_2008_AttenRel.class.getName());
	// BA_2008_AttenRel.class.getName());
	// AS_2008_AttenRel.class.getName());
	// CY_2008_AttenRel.class.getName());
	// NGA_2008_Averaged_AttenRel.class.getName());
	// // InterpolatedBA_2008_AttenRel.class.getName());
	// // 2007
	//
	// // 2006
	// BA_2006_AttenRel.class.getName());
	// CB_2006_AttenRel.class.getName());
	// CY_2006_AttenRel.class.getName());
	// GouletEtAl_2006_AttenRel.class.getName());
	// ZhaoEtAl_2006_AttenRel.class.getName());
	// // 2005
	// CS_2005_AttenRel.class.getName());
	// // 2004
	// BC_2004_AttenRel.class.getName());
	// USGS_Combined_2004_AttenRel.class.getName());
	// // 2003
	// BS_2003_AttenRel.class.getName());
	// CB_2003_AttenRel.class.getName());
	// ShakeMap_2003_AttenRel.class.getName());
	// // 2002
	//
	// // 2001
	//
	// // 2000
	// Field_2000_AttenRel.class.getName());
	// Abrahamson_2000_AttenRel.class.getName());
	// McVerryetal_2000_AttenRel.class.getName());
	// // 1999
	//
	// // 1998
	//
	// // 1997
	// AS_1997_AttenRel.class.getName());
	// BJF_1997_AttenRel.class.getName());
	// Campbell_1997_AttenRel.class.getName());
	// SadighEtAl_1997_AttenRel.class.getName());

	// OTHER
	// CyberShakeIMR.class.getName());

	private String name;
	private DevStatus status;

	private AttenRelImpl(String name, DevStatus status) {

		this.name = name;
		this.status = status;
	}

	public AttenuationRelationship instance() {
		return null;
	}
	
	
	@Override
	public String toString() {
		return name;
	}

	/**
	 * Returns the development status of this IMR.
	 * @return the development status
	 */
	public DevStatus status() {
		return status;
	}

}
