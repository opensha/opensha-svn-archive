package org.scec.util;


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

public class FileUtils {

    protected final static String C = "FileUtils";
    protected final static boolean D = false;

    public static ArrayList loadInCharFile(String fileName)
        throws
            FileNotFoundException,
            IOException
    {

        // Debugging
        String S = C + ": loadInCharFile(): ";
        if( D ) System.out.println(S + "Starting");

        // Allocate variables
        ArrayList list = new ArrayList();
        File f = new File(fileName);

        // Read in data if it exists
        if( f.exists() ){

            if( D ) System.out.println(S + "Found " + fileName + " and loading.");

            boolean ok = true;
            int counter = 0;
            String str;
            FileReader in = new FileReader(fileName);
            LineNumberReader lin = new LineNumberReader(in);

            while(ok){
                try{
                    str = lin.readLine();
                    if(str != null) {
                        list.add(str);
                        if(D){
                            System.out.println(S + counter + ": " + str);
                            counter++;
                        }
                    }
                    else ok = false;
                }
                catch(IOException e){ok = false;}
            }
            lin.close();
            in.close();

            if( D ) System.out.println(S + "Read " + counter + " lines from " + fileName + '.');

        }
        else if(D) System.out.println(S + fileName + " does not exist.");

        // Done
        if( D ) System.out.println(S + "Ending");
        return list;

    }

}
