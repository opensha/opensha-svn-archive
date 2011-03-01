package org.opensha.commons.mapping.gmt.elements;

import java.io.IOException;

import org.opensha.commons.util.cpt.CPT;

/**
 * Enum for GMT CPT files stored in the src/resources/cpt folder. These are used by the
 * GMT map plotter.
 * 
 * @author kevin
 *
 */
public enum GMT_CPT_Files {
	
	BLUE_YELLOW_RED("BlueYellowRed.cpt"),
	GMT_cool("GMT_cool.cpt"),
	MAX_SPECTRUM("MaxSpectrum.cpt"),
	RELM("relm_color_map.cpt"),
	SHAKEMAP("Shakemap.cpt"),
	STEP("STEP.cpt"),
	GMT_COPPER("GMT_copper.cpt"),
	GMT_CYCLIC("GMT_cyclic.cpt"),
	GMT_DRYWET("GMT_drywet.cpt"),
	GMT_GEBCO("GMT_gebco.cpt"),
	GMT_GLOBE("GMT_globe.cpt"),
	GMT_GRAY("GMT_gray.cpt"),
	GMT_HAXBY("GMT_haxby.cpt"),
	GMT_HOT("GMT_hot.cpt"),
	GMT_JET("GMT_jet.cpt"),
	GMT_NO_GREEN("GMT_no_green.cpt"),
	GMT_OCEAN("GMT_ocean.cpt"),
	GMT_PANOPLY("GMT_panoply.cpt"),
	GMT_POLAR("GMT_polar.cpt"),
	GMT_RAINBOW("GMT_rainbow.cpt"),
	GMT_RED_2_GREEN("GMT_red2green.cpt"),
	GMT_RELIEF("GMT_relief.cpt"),
	GMT_SEALAND("GMT_sealand.cpt"),
	GMT_SEIS("GMT_seis.cpt"),
	GMT_SPLIT("GMT_split.cpt"),
	GMT_TOPO("GMT_topo.cpt"),
	GMT_WYSIWYG("GMT_wysiwyg.cpt");
	
	private String fname;
	
	private GMT_CPT_Files(String fname) {
		this.fname = fname;
	}
	
	public String getFileName() {
		return fname;
	}
	
	public CPT instance() throws IOException {
		return CPT.loadFromStream(this.getClass().getResourceAsStream("/resources/cpt/"+fname));
	}

}