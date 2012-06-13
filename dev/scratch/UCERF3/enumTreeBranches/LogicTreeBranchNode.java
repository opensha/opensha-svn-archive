package scratch.UCERF3.enumTreeBranches;

import org.opensha.commons.data.ShortNamed;

public interface LogicTreeBranchNode<E extends Enum<E>> extends ShortNamed {
	
	/**
	 * This returns the relative weight of the logic tree branch.
	 * @return
	 */
	public double getRelativeWeight();
	
	/**
	 * This encodes the choice as a string that can be used in file names
	 * @return
	 */
	public String encodeChoiceString();
	
	/**
	 * This just exposes the <code>Enum.name()</code> method
	 * @return
	 */
	public String name();

}
