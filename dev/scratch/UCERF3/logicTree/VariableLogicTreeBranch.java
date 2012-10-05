package scratch.UCERF3.logicTree;

import java.util.ArrayList;
import java.util.List;


public class VariableLogicTreeBranch extends LogicTreeBranch {
	
	private List<String> variations;

	public VariableLogicTreeBranch(LogicTreeBranch branch, List<String> variations) {
		super(branch);
		this.variations = variations;
	}
	
	public List<String> getVariations() {
		return variations;
	}
	
	public boolean matchesVariation(VariableLogicTreeBranch branch) {
		if (variations != null) {
			List<String> o = branch.getVariations();
			if (o == null)
				return variations.isEmpty();
			if (variations.size()> o.size())
				return false;
			for (int i=0; i<variations.size(); i++) {
				String myVar = variations.get(i);
				if (myVar != null && !variations.get(i).equals(o.get(i)))
					return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((variations == null) ? 0 : variations.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariableLogicTreeBranch other = (VariableLogicTreeBranch) obj;
		if (variations == null) {
			if (other.variations != null)
				return false;
		} else if (!variations.equals(other.variations))
			return false;
		return true;
	}
	
	private static List<String> parseVariations(String name) {
		ArrayList<String> vars = null;
		while (name.contains("_Var")) {
			if (vars == null)
				vars = new ArrayList<String>();
			name = name.substring(name.indexOf("_Var")+4);
			String sub = name;
			if (sub.endsWith(".csv"))
				sub = sub.substring(0, name.indexOf(".csv"));
			if (sub.contains("_Run"))
				sub = sub.substring(0, sub.indexOf("_Run"));
			if (sub.contains("_Var"))
				sub = sub.substring(0, sub.indexOf("_Var"));
			vars.add(sub);
//			System.out.println("VARIATION: "+sub);
		}
		return vars;
	}
	
	public static VariableLogicTreeBranch fromName(String name) {
		List<String> variations = parseVariations(name);
		LogicTreeBranch branch = LogicTreeBranch.fromFileName(name);
		return new VariableLogicTreeBranch(branch, variations);
	}
	
}