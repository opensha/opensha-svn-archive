package org.opensha.sha.earthquake.rupForecastImpl.Point2MultVertSS_Fault;

import java.rmi.RemoteException;

import org.opensha.sha.earthquake.ProbEqkRupture;
import org.opensha.sha.earthquake.rupForecastImpl.Point2MultVertSS_FaultSource;
import org.opensha.sha.earthquake.rupForecastImpl.remote.RemoteERF_Client;
import org.opensha.sha.earthquake.rupForecastImpl.remote.RemoteEqkRupForecastAPI;
import org.opensha.sha.earthquake.rupForecastImpl.remoteERF_Clients.SingleFaultRuptureERF_Client;


/**
 * <p>Title: Point2MultVertSS_FaultServerBasedERF_List</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Point2MultVertSS_FaultServerBasedERF_List extends  Point2MultVertSS_FaultERF_List{


  /**
   * update the source based on the paramters (only if a parameter value has changed)
   */
  public void updateForecast(){
    String S = C + "updateForecast::";

    if(parameterChangeFlag) {

      double lat = ((Double) srcLatParam.getValue()).doubleValue();
      double lon = ((Double) srcLonParam.getValue()).doubleValue();
      double mag = ((Double) magParam.getValue()).doubleValue();
      double prob = 1.0;
      double maxRupOffset = ((Double) maxRupOffsetParam.getValue()).doubleValue();
      double deltaStrike = ((Double) deltaStrikeParam.getValue()).doubleValue();

      source = new Point2MultVertSS_FaultSource(lat, lon, mag, prob, magLengthRel,upperSeisDepth,
                                                lowerSeisDepth, maxRupOffset,  deltaStrike);

      int numRups = source.getNumRuptures();
      ProbEqkRupture eqkRup;
      for(int i=0; i< numRups; i++) {
        eqkRup = source.getRupture(i);
        try{
          this.addERF(new SingleFaultRuptureERF_Client(eqkRup, 1.0),eqkRup.getProbability());
        }catch(RemoteException e){
          e.printStackTrace();
        }
      }
      parameterChangeFlag = false;
    }
  }


  /**
   *
   * @param index
   * @returns the instance of the remotely existing ERF in the ERF List
   * on the server given the index. It also sets the timespan in the returned ERF.
   * **NOTE: All the functionality in this functionlity remains same as that of getERF but only differs
   * when returning each ERF from the ERF List. getERF() return the instance of the
   * EqkRupForecastAPI which is transferring the whole object on to the user's machine, but this functin
   * return back the RemoteEqkRupForecastAPI. This is useful becuase whole ERF object does not
   * get transfer to the users machine, just a stub of the remotely existing ERF gets
   * transferred.
   *
   * This function returns null, but if anyone needs to host his ERF as the remote
   * then he will have to implement this method.
   */
  public RemoteEqkRupForecastAPI getRemoteERF(int index){
   RemoteEqkRupForecastAPI remoteERF =  ((RemoteERF_Client)erf_List.get(index)).getERF_Server();
   try{
     remoteERF.setTimeSpan(this.timeSpan);
     return remoteERF;
   }catch(RemoteException e){
     e.printStackTrace();
   }
   return null;
  }

}
