package org.opensha.sha.magdist;

import java.util.*;
import org.opensha.sha.earthquake.AfterShockHypoMagFreqDistForecast;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupList;
import org.opensha.data.Location;
import org.opensha.sha.earthquake.observedEarthquake.ObsEqkRupture;
import org.opensha.data.LocationList;

/**
 * <p>Title: </p>
 *
 * <p> this calculates a guess at the fault length and direction
 *  based on the early seismicity. </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GuessFaultFromAftershocks {
  public GuessFaultFromAftershocks(AfterShockHypoMagFreqDistForecast aftershockModel) {
    AfterShockHypoMagFreqDistForecast aftershockmodel = new AfterShockHypoMagFreqDistForecast();
    faultGuess_Calc(aftershockmodel);
  }


  /**
   * faultGuess_Calc
   */
  public void faultGuess_Calc(AfterShockHypoMagFreqDistForecast aftershockModel) {
    ObsEqkRupList aftershocks = aftershockModel.getAfterShocks();
    ListIterator asIt = aftershocks.listIterator();
    int numEvents = aftershocks.size();
    //LocationList eventLoc = new LocationList[numEvents];
    Location eventLoc;
    double[] eventLat = new double[numEvents];
    double[] eventLong = new double[numEvents];

    int ind = 0;
    while (asIt.hasNext()){
      ObsEqkRupture event = (ObsEqkRupture)asIt.next();
      eventLoc = (Location)event.getHypocenterLocation();
      eventLat[ind] = (double)eventLoc.getLatitude();
      eventLong[ind++] = (double)eventLoc.getLongitude();
    }

    // eventLat and eventLong will themselves be sorted
    Arrays.sort(eventLat);
    Arrays.sort(eventLong);

    //minInd

  }

  /**
   * find the extremes in the obs data to get est. fault length
   * take the 1% and 99% distance to avoid extreme eq locations
   */

/*[latsort,lasi] = sort(newt2(:,2));
[longsort, losi] = sort(newt2(:,1));
mila = round(length(latsort)*.01);
mala = round(length(latsort)*.99);
milo = round(length(longsort)*.01);
malo = round(length(longsort)*.99);
if mila == 0
    mila = 1;
end
if milo == 0
    milo = 1;
end
minlat = latsort(mila);
maxlat = latsort(mala);
minlong = longsort(milo);
maxlong = longsort(malo);

latl = maxlat-minlat;
longl = maxlong-minlong;

if latl > longl
    tope = newt2(lasi(mala),1:2);
    bote = newt2(lasi(mila),1:2);
else
    tope = newt2(losi(malo),1:2);
    bote = newt2(losi(milo),1:2);
end

%%
% break fault into two segments around the mainshock
%%
seg1 = [tope(:,1) tope(:,2) maepi(:,1) maepi(:,2)];
seg2 = [maepi(:,1) maepi(:,2) bote(:,1) bote(:,2)];

%%
% find the midpoint along the lines to be used as the center of
% the aftershock zone (circle)
%%
MidPoint = FindFaultMidPoint(seg1,seg2);

%%
% now loop over all grid nodes to find the distance to fault
%%
[lno wno] = size(nodes);
for nLoop = 1:lno
    d1 = DistPointSegment([seg1(1) seg1(2)],[seg1(3),seg1(4)],[nodes(nLoop,1),nodes(nLoop,2)]);
    d2 = DistPointSegment([seg2(1) seg2(2)],[seg2(3),seg2(4)],[nodes(nLoop,1),nodes(nLoop,2)]);
    fd(nLoop) = min((d1),(d2)) {}

  end

%%
% convert to km's
%%
fdkmb = deg2km(fd);

%fdkm = fdkmb(gll);
*/



}
