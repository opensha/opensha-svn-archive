package org.scec.sha.util;

import java.io.*;
import java.util.*;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Temp {


  public static void main(String[] args) {
    Temp t = new Temp();
    String file = new String("/Users/nitingupta/Desktop/peer/OpenSHA_peer_Set2_results_copy/");
    File f = new File(file);
    String[] list = f.list();
    ArrayList array = new ArrayList();
    for(int i=0;i<list.length;++i){
      if(list[i].endsWith(".txt")){
        try{
          System.out.println("File: "+list[i]);
          array.clear();
          FileReader fr = new FileReader("/Users/nitingupta/Desktop/peer/OpenSHA_peer_Set2_results_copy/"+list[i]);
          BufferedReader br = new BufferedReader(fr);
          String line  = br.readLine();

          while(line !=null){
            System.out.println("Line: "+line);
            StringTokenizer st = new StringTokenizer(line);
            st.nextToken();
            array.add(st.nextToken());
            line = br.readLine();
          }
          br.close();
          FileWriter fw = new FileWriter("/Users/nitingupta/Desktop/peer/OpenSHA_peer_Set2_results_copy/"+list[i]);
          for(int j=0;j<array.size();++j)
            fw.write((String)array.get(j)+"\n");
          fw.close();
        }catch(Exception e){
          e.printStackTrace();
        }
      }
    }
  }
}