package scratchJavaDevelopers.martinez.LossCurveSandbox;

import scratchJavaDevelopers.martinez.LossCurveSandbox.vulnerability.
		DiscreteVulnerabilityFactory;

public class TestClass {
	public static void main(String [] args) throws Exception {
		String fileName = "/Users/emartinez/Desktop/" +
				"CCSmallHouseTypical_LogNormal.xml";
		
		// Simple test, debug printed in factory...
		DiscreteVulnerabilityFactory.createVulnerability(fileName);
	}
}
