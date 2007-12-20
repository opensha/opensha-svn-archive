package scratchJavaDevelopers.martinez.LossCurveSandbox;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import org.opensha.data.Location;

import scratchJavaDevelopers.martinez.LossCurveSandbox.beans.VulnerabilityBean;
import scratchJavaDevelopers.martinez.LossCurveSandbox.calculators.SiteClassException;
import scratchJavaDevelopers.martinez.LossCurveSandbox.calculators.WillsSiteClassCalculator;
import scratchJavaDevelopers.martinez.LossCurveSandbox.vulnerability.VulnerabilityModel;

public class FooClass {
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

	public static void main(String [] args) {
		
		/**
		VulnerabilityBean bean = VulnerabilityBean.getSharedInstance();
		
		Vector<VulnerabilityModel> models = bean.getAvailableModels();
		
		try {
			for(int i = 0; i < models.size(); ++i) {
				VulnerabilityModel model = models.get(i);
				String outFileName = "/Users/emartinez/Desktop/matlabFiles/" +
					model.getName() + ".dat";
				
				FileWriter writer = new FileWriter(outFileName);
				writer.write(model.toString());
				writer.close();
						
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Done!");
		*/
		
		try {
			WillsSiteClassCalculator calc = new WillsSiteClassCalculator();
			Location loc = new Location(35.0, -118.11, 0.0);
			String siteClass = calc.getSiteClassName(loc);
			System.out.println( "(" + loc.getLatitude() + ", " + 
					loc.getLongitude() + ") :: " + 
					calc.getSiteClassDescription(siteClass) + " (" + siteClass + ")"
				);
		} catch (SiteClassException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	//---------------------------------------------------------------------------
	// Private Methods
	//---------------------------------------------------------------------------
}
