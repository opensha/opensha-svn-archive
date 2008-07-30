package org.opensha.nshmp.sha.calc;

import java.rmi.RemoteException;

import org.opensha.data.function.ArbitrarilyDiscretizedFunc;

public class SRSsS1Calculator {
	//---------------------------------------------------------------------------
	// Member Variables
	//---------------------------------------------------------------------------

	//---------------------------- Constant Members ---------------------------//

	//----------------------------  Static Members  ---------------------------//

	//---------------------------- Instance Members ---------------------------//

	//---------------------------------------------------------------------------
	// Constructors/Initializers
	//---------------------------------------------------------------------------

	//---------------------------------------------------------------------------
	// Public Methods
	//---------------------------------------------------------------------------

	//------------------------- Public Setter Methods  ------------------------//

	//------------------------- Public Getter Methods  ------------------------//

	//------------------------- Public Utility Methods ------------------------//
	public ArbitrarilyDiscretizedFunc calculateSRSsS1(
			ArbitrarilyDiscretizedFunc function, float fa, float fv,
			String siteClass) throws RemoteException {
		function.setInfo("SRSsS1 method not yet implemented.");
		return function;
	}
	//---------------------------------------------------------------------------
	// Private Methods
	//---------------------------------------------------------------------------
}
