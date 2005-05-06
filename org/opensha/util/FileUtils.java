package org.opensha.util;


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
        if (D) System.out.println(S + fileName);
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
                      //omit the blank line
                      if(str.trim().equals(""))
                        continue;

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
    public static ArrayList loadFile(URL url) throws Exception {
      if(D) System.out.println("url="+url);
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


    /**
     * load from Jar file
     * @param fileName : File name to be read from Jar file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static ArrayList loadJarFile(String fileName)
       throws  FileNotFoundException, IOException {
      try {
        if(D) System.out.println("FileUtils:filename="+fileName);
        return loadFile(FileUtils.class.getResource("/"+fileName));
      }catch(Exception e) {
        e.printStackTrace();
        return null;
      }
   }

   /**
    * save the serialized object into the specified file
    * @param fileName
    * @param obj
    */
   public static void saveObjectInFile(String fileName, Object obj) {
     try {
       // write  object to the file
       FileOutputStream fileOut = new FileOutputStream(fileName);
       ObjectOutputStream objectStream = new ObjectOutputStream(fileOut);
       objectStream.writeObject(obj);
       objectStream.close();
       fileOut.close();
     }catch(Exception e ) { e.printStackTrace(); }
   }

   /**
    * return a object read from the URL
    * @param url
    * @return
    */
   public static Object loadObjectFromURL(String url) {
     try {
       URL forecastURL = new URL(url);
       URLConnection uc = forecastURL.openConnection();
       ObjectInputStream tis = new ObjectInputStream( (InputStream) uc.
           getContent());
       Object obj = tis.readObject();
       tis.close();
       return obj;
     }catch(Exception e) { e.printStackTrace(); }
     return null;
   }


   /**
    * @param fileName File from where object needs to be read
    * @return Object object read from the file
    */
   public static Object loadObject(String fileName)
   {
     if(D) System.out.println("fileName="+fileName);
     try {
       FileInputStream fin = new FileInputStream(fileName);
       ObjectInputStream tis = new ObjectInputStream( fin);
       Object obj =  tis.readObject();
       tis.close();
       fin.close();
       return obj;
       }catch(Exception e) { e.printStackTrace(); }
       return null;
   }

}







