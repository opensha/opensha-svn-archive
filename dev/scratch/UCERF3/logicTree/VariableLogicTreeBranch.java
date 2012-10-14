package scratch.UCERF3.logicTree;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;


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
			if (sub.endsWith("_sol.zip"))
				sub = sub.substring(0, name.indexOf("_sol.zip"));
			if (sub.contains("_Run"))
				sub = sub.substring(0, sub.indexOf("_Run"));
			if (sub.contains("_Var"))
				sub = sub.substring(0, sub.indexOf("_Var"));
			vars.add(sub);
//			System.out.println("VARIATION: "+sub);
		}
		return vars;
	}
	
	public static LogicTreeBranch fromStringValues(List<String> strings) {
		return fromFileName(Joiner.on("_").join(strings));
	}
	
	public static VariableLogicTreeBranch fromFileName(String name) {
		List<String> variations = parseVariations(name);
		LogicTreeBranch branch = LogicTreeBranch.fromFileName(name);
		return new VariableLogicTreeBranch(branch, variations);
	}
	
	@Override
	public String buildFileName() {
		String name = super.buildFileName();
		if (variations != null)
			for (String variation : variations)
				name += "_Var"+variation;
		return name;
	}

	public static void main(String[] args) {
		String name = "FM3_1_ZENG_HB08_DsrUni_CharConst_M5Rate8.7_MMaxOff7.6_NoFix_SpatSeisU3_VarPaleo10_VarSectNuclMFDWt0.01";
//		for (String var : parseVariations(name))
//			System.out.println(var);
		VariableLogicTreeBranch branch = VariableLogicTreeBranch.fromFileName(name);
		for (String var : branch.getVariations())
			System.out.println(var);
	}
	
}