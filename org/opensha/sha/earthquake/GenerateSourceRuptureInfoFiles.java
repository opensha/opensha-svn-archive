package org.opensha.sha.earthquake;

import org.opensha.sha.earthquake.EqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.WGCEP_UCERF1.WGCEP_UCERF1_EqkRupForecast;
import java.io.*;
import org.opensha.sha.surface.GriddedSurfaceAPI;
import org.opensha.sha.surface.PointSurface;


/**
 * <p>Title: GenerateSourceRuptureInfoFiles</p>
 *
 * <p>Description: This allows the user to create the Metadata files for source,
 * rupture and ERF.</p>
 *
 * @author Nitin Gupta
 * @since March 1, 2006
 * @version 1.0
 */
public class GenerateSourceRuptureInfoFiles {






  public GenerateSourceRuptureInfoFiles() {
    super();

  }


  /**
   * Create the Directory where all source and rupture information will be dumped.
   * @param dirName String
   * @return String
   */
  private String createDirectory(String dirName){
    File f = new File(dirName);
    boolean success = f.mkdir();
    if(success)
      return f.getAbsolutePath();
    return null;
  }


  /**
   * Creates the EqkRupForecast Metadata file
   * @param directoryPath String path to the directory where files are to be created
   * @param forecast EqkRupForecast : Eqk Rup Forecast
   */
  public void createERF_MetadataFile(String directoryPath,EqkRupForecast forecast){
    String forecastMetadata = forecast.adjustableParams.getParameterListMetadataString();
    String timeSpanMetadata = forecast.getTimeSpan().getAdjustableParams().getParameterListMetadataString();
    FileWriter fw = null;
    try {
      fw = new FileWriter(directoryPath+"/ERF_Metadata.txt");
      fw.write(forecastMetadata+"\n");
      fw.write(timeSpanMetadata);
      fw.close();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Creates the source list file, writes out the metadata for each source in a file
   * in each line of file
   * @param directoryPath String path to the directory where files are to be created
   * @param forecast EqkRupForecast : Earthquake Rupture Forecast
   * @returns the path to the source directory, where al the information about the
   * ruptures will be stored
   */
  public void createSourceListFile(String directoryPath,EqkRupForecast forecast) {
    String sourceDirName = "source";
    FileWriter fw = null;
    try {
      fw = new FileWriter(directoryPath+"/sourceList.txt");
      int numSources = forecast.getNumSources();
      //System.out.println("NumSources ="+numSources);
      fw.write("#Source-Index   NumRuptures    IsPoission    Total-Prob.   Src-Name\n");
      for(int i=0;i<numSources;++i){
        ProbEqkSource source = forecast.getSource(i);
        source.setSourceIndex(i);
        fw.write(source.getSourceMetadata()+"\n");
        File f = new File(directoryPath+"/"+sourceDirName + i);
        f.mkdir();
        //create the rupture list in one big file
        createRuptureListFile(f.getAbsolutePath(),source);

      }
      fw.close();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    return ;
  }


  /**
   * Creates the Rupture list file which gives the metadata for a rupture in each line of
   * file. It only displays the list of ruptures in a given source.
   * @param sourceDir String
   * @param source ProbEqkSource
   */
  public void createRuptureListFile(String sourceDir, ProbEqkSource source){
    FileWriter fw = null;
    int numRuptures = source.getNumRuptures();
    try{
      fw = new FileWriter(sourceDir+"/"+"ruptureList.txt");
      fw.write(
          "#Src-Index  Rup-Index  Mag  Prob.  Ave.Rake  Surface-Lat(conditional)  " +
          "Surface-Lon(conditional)   Surface-Depth(conditional)  Source-Name\n");
      for (int i = 0; i < numRuptures; ++i) {
        ProbEqkRupture rupture = source.getRupture(i);
        rupture.setRuptureIndexAndSourceInfo(source.getSourceIndex(),
                                             source.getName(), i);
        fw.write(rupture.getRuptureMetadata()+"\n");
        GriddedSurfaceAPI surface = rupture.getRuptureSurface();
        if(!(surface instanceof PointSurface))
          createRuptureSurfaceFile(sourceDir,rupture);
      }
      fw.close();
    }catch(IOException e){
      e.printStackTrace();
    }
  }


  /**
   * Creates the individual metadata file for each rupture surface.
   * @param sourceDir String
   * @param rupture ProbEqkRupture
   */
  public void createRuptureSurfaceFile(String sourceDir,
                                       ProbEqkRupture rupture){
    FileWriter fw = null;
    try{
      fw = new FileWriter(sourceDir+"/"+rupture.getRuptureIndex()+".txt");
      fw.write(
          "#Ave-Dip  RupSurface-Length  Rup-DownDipWidth  GridSpacing "+
         "NumRows   NumCols   NumPoints \n");
      fw.write(rupture.getRuptureSurface().getSurfaceMetadata());
      fw.close();

    }catch(IOException e){
      e.printStackTrace();
    }

  }


  /**
   * Creates the Readme file for user to understand how the Source-Rupture info.
   * has been structured and which information can be located in files. It also
   * explains how directories are presented and what metadata was used to create
   * these files.
   */
  public void createReadMeFile(){
    FileWriter fw = null;
    try{
      fw = new FileWriter("Readme.txt");
      fw.write(
          "This file explains how source and rupture files have been structured.\n");
      fw.write(
          "It also tells user what information is contained in each file\n");
      fw.write(
          "The program used to create these source rupture files takes the " +
          "directory name as the command line input where all the source and ruptures " +
          "file will be created, also referred to as root level directory");
      fw.write(
          "At the same level as this Readme file, it has 2 other files\n :");
      fw.write(
          "1) ERF_Metadata.txt - This file contains the information about the " +
          " Earthquake Rupture Forecast (ERF), what were parameters value for which " +
          "this ERF was instantiated.\n");
      fw.write(
          "2)SourceList.txt - This file contains information about each source in the " +
          "Earthquake Rupture Forecast model. Each source information is contained in single line " +
          " in the file.Each line tab delimited with first line being the comment line " +
          "contains following information:\n");
      fw.write("Source-Index   NumRuptures    IsPoission    Total-Prob.   Src-Name.\n");
      fw.write(
          "For each source in the ERF a directory is created that contains " +
          "the ruptures information for that source.\nEach source directory is named as " +
          "\"source\" appened with source number in the ERF. For eg: directory for "+
          "source 0 is labeled as \"source0\".\n");
      fw.write("Within each of these source directory is the file named :\n " +
               "\"ruptureList.txt\" that contains the following information about each " +
               "rupture defined on the given source :\n");
      fw.write(
          "#Src-Index  Rup-Index  Mag  Prob.  Ave.Rake  Surface-Lat(conditional)  " +
          "Surface-Lon(conditional)   Surface-Depth(conditional)  Source-Name\n");
      fw.write(
          "Each line the above file gives the information on each rupture defined " +
          "on the source.\nEach element is tab delimited. Elements mentioned as " +
          "\"conditional\" are only present if rupture surface is a point surface location, "+
          "they are discarded.\n");
      fw.write(
          "Each source directory also contains the Rupture Surface that gives the following info. "+
          "about the surface in a single line(tab demilited):\n");
      fw.write(
          "#Ave-Dip  RupSurface-Length  Rup-DownDipWidth  GridSpacing "+
          "NumRows   NumCols   NumPoints .\n");
      fw.write(
          "This file also gives the each point location on the surface with " +
          "each line defining the lat lon depth of a point location on the surface. " +
          "It is also a tab delimited file with location defined as:\n\t\t " +
          "Lat   Lon   Depth\n");
      fw.write(
          "Any file that contains \"#\" refers to comment line in the file " +
          "that describes the contents of the file below that line.\n");
      fw.write(
          "Rupture Surface files are created for a rupture if it is not a point surface, "+
          "otherwise it just write out the point surface locations of the rupture "+
          "in the \"ruptureList.txt\" file.");
      fw.close();

    }catch(IOException e){
      e.printStackTrace();
    }
  }


  /*
   * Main method to start the application
   * @param args String[]
   */
  public static void main(String[] args) {
    GenerateSourceRuptureInfoFiles generatesourceruptureinfofiles = new
        GenerateSourceRuptureInfoFiles();
    String directoryPath = generatesourceruptureinfofiles.createDirectory(args[0]);
    if(directoryPath !=null || !directoryPath.trim().equals("")){
      WGCEP_UCERF1_EqkRupForecast ucerf = null;

      ucerf = new
          WGCEP_UCERF1_EqkRupForecast();

      ucerf.getAdjustableParameterList().getParameter(
          WGCEP_UCERF1_EqkRupForecast.
          BACK_SEIS_NAME).setValue(WGCEP_UCERF1_EqkRupForecast.
                                   BACK_SEIS_EXCLUDE);

      ucerf.getAdjustableParameterList().getParameter(
          WGCEP_UCERF1_EqkRupForecast.BACK_SEIS_RUP_NAME).
          setValue(WGCEP_UCERF1_EqkRupForecast.BACK_SEIS_RUP_POINT);

      ucerf.getAdjustableParameterList().getParameter(
          WGCEP_UCERF1_EqkRupForecast.FAULT_MODEL_NAME).setValue(
              WGCEP_UCERF1_EqkRupForecast.FAULT_MODEL_STIRLING);
      ucerf.getAdjustableParameterList().getParameter(
          WGCEP_UCERF1_EqkRupForecast.RUP_OFFSET_PARAM_NAME).setValue(
              new Double(5.0));

      ucerf.getTimeSpan().setDuration(5.0);
      ucerf.updateForecast();

      generatesourceruptureinfofiles.createERF_MetadataFile(directoryPath,
          ucerf);
      generatesourceruptureinfofiles.createSourceListFile(directoryPath, ucerf);
      generatesourceruptureinfofiles.createReadMeFile();
    }
  }
}
