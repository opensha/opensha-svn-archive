package scratch.UCERF3.erf;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	FSS_ERF_ParamTestSuite.class
})

public class UCERF3_ERF_TestSuite {
	
	public static void main(String args[]) {
		org.junit.runner.JUnitCore.runClasses(UCERF3_ERF_TestSuite.class);
	}

}
