package org.scec.sha.earthquake.rupForecastImpl.WG02;

import org.scec.mapping.gmtWrapper.RunScript;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class WG02_FortranEqkRupForecast {

  public WG02_FortranEqkRupForecast() {
  }



  public void runFortranCode(){
    String CODE_PATH="/Users/gupta/wg99/wg99_source_v26/";


    try {

     //command to be executed during the runtime.
//       String[] command ={"sh","-c",GMT_PATH+"xyz2grd LatLonAmpData.txt -Gdata.grd -I0.05 "+ region +" -D/degree/degree/amp/=/=/= -V -:"};
//       RunScript.runScript(command);

//     xyz2grd LatLonAmpData.txt -GtestData.grd -I0.05 -R-121/-115/32.5/35.5 -D/degree/degree/amp/=/=/= -V -:

     //command to be executed during the runtime.
     String[] command ={"sh","-c","cat "+CODE_PATH+"base_mod_23_wgt_100.inp | "+CODE_PATH+"wg99 "};
     RunScript.runScript(command);
     command[2] ="cat "+CODE_PATH+"go | "+CODE_PATH+"wg99 ";
     RunScript.runScript(command);
     command[2] ="cat "+CODE_PATH+"go | "+CODE_PATH+"wg99 ";
     RunScript.runScript(command);

     //command[2] = CODE_PATH+"wg99"+" < base_mod_23_wgt_100.inp go go ";
     //RunScript.runScript(command);
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  public static void main(String[] args){
    WG02_FortranEqkRupForecast wg = new WG02_FortranEqkRupForecast();
    wg.runFortranCode();
  }
}