package org.scec.util;


import java.io.*;
import java.util.*;

/**
 * <b>Title:</b>FileUtils<p>
 * <b>Description:</b>Generic functions used in handling text files, such as
 * loading in the text data from a file.<p>
 *
 * @author Steven W. Rock
 * @version 1.0
 */

public class FileUtils {

    /** Class name used for debug strings */
    protected final static String C = "FileUtils";
    /** boolean that indicates if print out debug statements */
    protected final static boolean D = false;

    /**
     * Loads in each line to a text file into an ArrayList ( i.e. a vector ). Each
     * element in the ArrayList represents one line from the file.
     *
     * @param fileName                  File to load in
     * @return                          ArrayList each element one line from the file
     * @throws FileNotFoundException    If the filename doesn't exist
     * @throws IOException              Unable to read from the file
     */
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
