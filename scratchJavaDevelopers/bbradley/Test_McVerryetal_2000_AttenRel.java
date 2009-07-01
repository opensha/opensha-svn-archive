  package scratchJavaDevelopers.bbradley;
  
  import org.opensha.commons.data.Location;
  import org.opensha.commons.data.Site;
  import org.opensha.sha.earthquake.*;
  import org.opensha.sha.faultSurface.*;
  /**
   * This tests DistJB numerical precision with respect to the f_hngR term.  Looks OK now.
   * @param args
   */
  
  public class Test_McVerryetal_2000_AttenRel {
	  public static void main(String[] args) {

		  Location loc1 = new Location(-0.1, 0.0, 0);
		  Location loc2 = new Location(+0.1, 0.0, 0);
		  FaultTrace faultTrace = new FaultTrace("test");
		  faultTrace.addLocation(loc1);
		  faultTrace.addLocation(loc2);	  
		  StirlingGriddedSurface surface = new StirlingGriddedSurface(faultTrace, 45.0,0,10,1);
		  EqkRupture rup = new EqkRupture();
		  rup.setMag(7);
		  rup.setAveRake(90);
		  rup.setRuptureSurface(surface);
	  
		  McVerryetal_2000_AttenRel attenRel = new McVerryetal_2000_AttenRel(null);
		  attenRel.setParamDefaults();
		  attenRel.setIntensityMeasure("PGA");
		  attenRel.setEqkRupture(rup);
	  
		  Site site = new Site();
		  site.addParameter(attenRel.getParameter(attenRel.SITE_TYPE_NAME));
	  
		  Location loc;
		  for(double dist=-0.3; dist<=0.3; dist+=0.01) {
			  loc = new Location(0,dist);
			  site.setLocation(loc);
			  attenRel.setSite(site);
//		      System.out.print((float)dist+"\t");
			  attenRel.getMean();
		  }
	  }
	  
	  
  }