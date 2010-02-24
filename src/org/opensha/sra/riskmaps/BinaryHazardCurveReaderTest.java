package org.opensha.sra.riskmaps;

import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;

public class BinaryHazardCurveReaderTest {
	
	public static final String DEFAULT_FILE_NAME = 
		"/Users/emartinez/Desktop/out.bin";
	
	public static void main(String [] args) throws Exception{
		String filename = null;
		if ( args.length > 0 ) { filename = args[0]; }
		else { filename = DEFAULT_FILE_NAME; }
		
		BinaryHazardCurveReader reader = new BinaryHazardCurveReader(filename);
		
		for ( int i = 0; i < 10; i++ ) {
			ArbitrarilyDiscretizedFunc func = reader.nextCurve();
			double [] loc = reader.currentLocation();
			
			System.out.printf("Location: (%f, %f)\n", loc[0], loc[1]);
			System.out.println(func);
		}
	}
}
