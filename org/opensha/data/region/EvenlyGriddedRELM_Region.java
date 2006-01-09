package org.opensha.data.region;

import org.opensha.data.LocationList;
import javaDevelopers.vipin.erf.CreateRELM_GriddedRegion;
import org.opensha.util.FileUtils;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.opensha.data.Location;

/**
 * <p>Title: EvenlyGriddedRELM_Region.java </p>
 * <p>Description: Creates a region specified by the RELM file</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class EvenlyGriddedRELM_Region extends EvenlyGriddedGeographicRegion {
  private final static double GRID_SPACING = 0.1;

  public EvenlyGriddedRELM_Region() {
    try {
      // read the file containing the location list
      ArrayList list = FileUtils.loadFile(CreateRELM_GriddedRegion.GRIDDED_REGION_OUT_FILENAME);
      LocationList locList = new LocationList();
      double lat, lon;
      // populate the location list
      for(int i=0; i<list.size(); ++i ) {
        String line = (String)list.get(i);
        StringTokenizer tokenizer = new StringTokenizer(line);
        lat = Double.parseDouble(tokenizer.nextToken());
        lon =  Double.parseDouble(tokenizer.nextToken());
        locList.addLocation(new Location(lat,lon));
      }
      // make polygon from the location list
      createEvenlyGriddedGeographicRegion(locList, GRID_SPACING);
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  public EvenlyGriddedRELM_Region(LocationList locList, double gridSpacing) {
    super(locList, gridSpacing);
  }

  public EvenlyGriddedRELM_Region(LocationList locList, double gridSpacing, EvenlyGriddedGeographicRegionAPI region) {
    super(locList, gridSpacing, region);
  }
  public static void main(String[] args) {
    EvenlyGriddedRELM_Region evenlyGriddedRELM_Region1 = new EvenlyGriddedRELM_Region();
  }
}