package org.scec.util;

import java.io.*;

/**
 * <p>Title: Binary2Ascii</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Bin2Ascii4Floats {

  public Bin2Ascii4Floats() {
  }
  public static void main(String[] args) {
    Bin2Ascii4Floats binary2Ascii1 = new Bin2Ascii4Floats();

    if(args.length <1)
      System.out.println("Usage : Binary2Ascii <filename>");
    else{
      int i=0;
      FileWriter fw = null;
      FileInputStream fp =null;
      DataInputStream dis = null;
      try{
        fw = new FileWriter(args[0]+".asc");
        fp = new FileInputStream(args[0]);
        dis = new DataInputStream(fp);
        while(dis!=null){
          fw.write(dis.readFloat()+"\n");
          ++i;
        }
      }catch(IOException e){
        //e.printStackTrace();
        //System.out.println(args[0]);
      }
      finally{
        System.out.println("Rows: "+i);
        try{
        dis.close();
        fp.close();
        fw.close();
        }catch(Exception e){
          e.printStackTrace();
        }
      }

    }

  }
}