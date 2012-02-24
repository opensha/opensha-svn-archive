package scratch.UCERF3.enumTreeBranches;

public enum FaultModelBranches {

	FM3_1("Fault Model 3.1", 101),
	FM3_2("Fault Model 3.2", 102);
	
	private String modelName;
	private int id;
	
	private FaultModelBranches(String modelName, int id) {
		this.modelName = modelName;
		this.id = id;
	}
	
	public String getName() {
		return modelName;
	}
	
	public int getID() {
		return id;
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
}
