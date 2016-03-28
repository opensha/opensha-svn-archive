package org.opensha.sha.simulators.iden;

import java.util.HashSet;
import java.util.List;

import org.opensha.sha.simulators.EQSIM_Event;
import org.opensha.sha.simulators.SimulatorElement;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

/**
 * This is a simple rupture identifier implementation - it defines a match as any rupture that includes
 * any part of any of the given section(s).
 * @author kevin
 *
 */
public class SectionIDIden extends AbstractRuptureIdentifier {
	
	private String name;
	private HashSet<Integer> elementIDs;
	
	public static SectionIDIden getNSAF(List<SimulatorElement> elems) {
		// NOTE: no creeping
		return new SectionIDIden("N. SAF", elems, parseNames(elems, "SAF-Mendo_Offs", "SAF-N_Coast_Of",
				"SAF-N_Coast_On", "SAF-N_Mendocin", "SAF-N_Mid_Peni", "SAF-S_Cruz_Mts", "SAF-S_Mid_Peni"));
	}
	
	public static SectionIDIden getSSAF(List<SimulatorElement> elems) {
		// NOTE: no parkfield or creeping
//		return new SectionIDIden("S. SAF", elems, parseNames(elems, "SAF-Carrizo", "SAF-Cholame",
//				"SAF-Coachella", "SAF-Mojave", "SAF-San_Bernar"));
		// NOTE: no parkfield or creeping or Cholame
		return new SectionIDIden("S. SAF", elems, parseNames(elems, "SAF-Carrizo",
				"SAF-Coachella", "SAF-Mojave", "SAF-San_Bernar"));
	}
	
	public static SectionIDIden getSanJacinto(List<SimulatorElement> elems) {
		return new SectionIDIden("San Jacinto", elems, parseNames(elems, "Anza", "San_Bernardino", "San_Jacinto"));
	}
	
	public static SectionIDIden getElsinore(List<SimulatorElement> elems) {
		return new SectionIDIden("Elsinore", elems, parseNames(elems, "Coyote_Mt.", "Glen_Ivy", "Julian", "Temecula" ,"Whittier"));
	}
	
	public SectionIDIden(String name, List<SimulatorElement> elems, int sectionID) {
		this(name, elems, Lists.newArrayList(sectionID));
	}
	
	public SectionIDIden(String name, List<SimulatorElement> elems, int... sectionIDs) {
		this(name, elems, Ints.asList(sectionIDs));
	}
	
	public SectionIDIden(String name, List<SimulatorElement> elems, List<Integer> sectionIDs) {
		this.name = name;
		elementIDs = new HashSet<Integer>(getElemIDs(elems, sectionIDs));
	}
	
	private static List<Integer> getElemIDs(List<SimulatorElement> elems, List<Integer> sectionIDs) {
		HashSet<Integer> sectIDs = new HashSet<Integer>(sectionIDs);
		List<Integer> elemIDs = Lists.newArrayList();
		for (SimulatorElement elem : elems) {
			if (sectIDs.contains(elem.getSectionID()))
				elemIDs.add(elem.getID());
		}
		return elemIDs;
	}
	
	public static List<Integer> parseNames(List<SimulatorElement> elems, String... sectionNames) {
		return parseNames(elems, Lists.newArrayList(sectionNames));
	}
	
	public static List<Integer> parseNames(List<SimulatorElement> elems, List<String> sectionNames) {
		List<Integer> ids = Lists.newArrayList();
		
		for (String sectionName : sectionNames) {
			Integer id = null;
			for (SimulatorElement elem : elems) {
				if (elem.getSectionName().equals(sectionName)) {
					id = elem.getSectionID();
					break;
				}
			}
			Preconditions.checkArgument(id != null, "Section ID not found for: "+sectionName);
			ids.add(id);
		}
		return ids;
	}

	@Override
	public boolean isMatch(EQSIM_Event event) {
		for (int elementID : event.getAllElementIDs())
			if (elementIDs.contains(elementID))
				return true;
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

}
