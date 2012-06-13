package scratch.UCERF3.enumTreeBranches;

public enum MaxMagOffFault implements LogicTreeBranchNode<MaxMagOffFault> {
		// TODO finalize values
		// TODO set weights
		MAG_7p65(7.65, 1d);
		
		private double mmax;
		private double weight;
		
		private MaxMagOffFault(double mmax, double weight) {
			this.mmax = mmax;
			this.weight = weight;
		}

		@Override
		public String getName() {
			String name = (float)mmax+"";
			if (!name.contains("."))
				name += ".0";
			return name;
		}
		
		@Override
		public String getShortName() {
			return getName();
		}

		@Override
		public double getRelativeWeight() {
			return weight;
		}
		
		public double getMaxMagOffFault() {
			return mmax;
		}

		@Override
		public String encodeChoiceString() {
			return "MMaxOff"+getShortName();
		}
}
