package org.scec.util;


import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * <b>Title:</b>FileUtils<p>
 *
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
    public static ArrayList loadFile(String fileName)
        throws
            FileNotFoundException,
            IOException
    {

        // Debugging
        String S = C + ": loadFile(): ";
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

    /**
     *
     * @param url : URL of file to be read
     * @return : arrayList containing the lines in file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static ArrayList loadFile(URL url)
        throws  FileNotFoundException, IOException {
      try {
        URLConnection uc = url.openConnection();
        ArrayList list = new ArrayList();
        BufferedReader tis =
            new BufferedReader(new InputStreamReader((InputStream) uc.getContent()));
        String str = tis.readLine();
        while(str != null) {
          list.add(str);
          str = tis.readLine();
        }
        tis.close();
        return list;
      }
      catch (Exception e) {
        e.printStackTrace(System.err);
      }
     return null;
    }
}







