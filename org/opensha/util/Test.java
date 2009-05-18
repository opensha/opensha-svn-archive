package org.opensha.util;

import java.io.BufferedReader;
import java.io.FileReader;
/**
 * <p>Title: Test</p>
 * <p>Description: Following program compares the 2 text files with line by line
 * and check if float value at each line on both files are same.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class Test {

  public Test() {
  }
  public static void main(String[] args) {
    Test test1 = new Test();
    try{
    FileReader fr = new FileReader("/Users/nitingupta/Desktop/CAagrid927_temp.asc");
    BufferedReader br = new BufferedReader(fr);
    FileReader fr1 = new FileReader("/Users/nitingupta/projects/frankel02/CAagrid927.asc");
    BufferedReader br1 = new BufferedReader(fr1);

    String line = br.readLine();
    String line1 = br1.readLine();

    while(line !=null || line1 !=null){
      float one  = Float.parseFloat(line);
      float two = Float.parseFloat(line1);
      if(Math.abs(one - two) > .0001)
        System.out.println(one + ": "+two);
      line = br.readLine();
      line1 = br1.readLine();
    }
    br.close();
    fr.close();
    fr1.close();
    br1.close();
    }catch(Exception e){
      e.printStackTrace();
      System.out.println("File Not Found");
    }
  }
}
