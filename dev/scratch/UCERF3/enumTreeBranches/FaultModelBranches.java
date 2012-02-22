package scratch.UCERF3.enumTreeBranches;

public enum FaultModelBranches {

	FM3_1("Fault Model 3.1"),
	FM3_2("Fault Model 3.1");
	
	private String modelName;
	
	private FaultModelBranches(String modelName) {
		this.modelName = modelName;
	}
	
	public String getName() {
		return modelName;
	}
	
}
