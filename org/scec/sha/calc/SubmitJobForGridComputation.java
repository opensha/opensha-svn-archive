package org.scec.sha.calc;

import java.text.DecimalFormat;
import java.io.*;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.util.RunScript;

/**
 * <p>Title: SubmitJobForGridComputation.java </p>
 * <p>Description: This class will accept the filenames of the IMR, ERF,
 * GRIDDEDREGION  and it will create condor submit files and DAG needed for
 * grid computation </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Ned Field, Nitin Gupta, Vipin Gupta
 * @date Mar 15, 2004
 * @version 1.0
 */

public class SubmitJobForGridComputation {

  private static boolean D = false;
  // parent directory on almaak.usc.edu where computations will take place
  private static String REMOTE_DIR = "/home/rcf-71/vgupta/pool/";

  // tar file which will conatin all the hazard curves
  private static String OUTPUT_TAR_FILE_NAME="outputfiles.tar";

  //tar file which contain all the submit files and Dag.
  private static String SUBMIT_TAR_FILES="submitfiles.tar";
  private DecimalFormat decimalFormat=new DecimalFormat("0.00##");
  private static int SUGGESTED_NUM_SITES_IN_WORK_UNIT = 100;

  //private static String REMOTE_EXECUTABLE_NAME = "HazardMapCalculator.pl";
  private static String REMOTE_EXECUTABLE_NAME = "CondorHazardMapCalculator.class";

  //Hazard Map Jar file using which executable will be executed
  private static String HAZARD_MAP_JAR_FILE_NAME= "hazardmapcondor.jar";

  // this executable will create a new directory at the machine
  private static String PRE_PROCESSOR_EXECUTABLE = "HazardMapPreProcessor.sh";
  // name of condor submit file which will submit the pre processor job
  private static String PRE_PROCESSOR_CONDOR_SUBMIT = "preparation";
  // name of Job that will be used in DAG
 // private static String PRE_PROCESSOR_JOB_NAME = "PREPARATION";

  // this script will be executed after all the hazard map dataset is created
  private static String POST_PROCESSOR_EXECUTABLE = "HazardMapPostProcessor.sh";
  // name of condor submit file which will submit the post processor job
  private static String POST_PROCESSOR_CONDOR_SUBMIT = "finish";
  // name of Job that will be used in DAG
  private static String FINISH_JOB_NAME = "FINISH";

  //name of the condor submit script to run the Dag and untar the submit files on the remote machine
  private static String DAG_CONDOR_SUBMIT = "run_dag";

  private static String DAG_FILE_NAME  = "pathway1.dag";
  private static String LOG_FILE_NAME  = "pathway1.log";
  private static String SUBMIT_DAG_SHELL_SCRIPT_NAME = "submitdag.sh";
  private static String GET_CURVES_FROM_REMOTE_MACHINE = "getCurves.sh";
  private static String PUT_SUBMIT_FILES_TO_REMOTE_MACHINE = "putSubmitFiles.sh";


  /**
   *
   * @param imrFileName FileName in which IMR is saved as a serialized object
   * @param erfFileName Filename in which ERF is saved as a serialized object
   * @param regionFileName FileName in which Region is saved as a serialized object
   * @param outputDir directory where condor submit files will be created
   * @param remoteMachineSubdir subdirectory on remote machine where computations will take
   * place. So, compuatations will take place in directory /home/rcf-71/vgupta/pool/remoteMachineSubdirName
   */
  public SubmitJobForGridComputation(String imrFileName, String erfFileName,
                                     String regionFileName, String outputDir,
                                     int remoteMachineSubdirName,
                                     SitesInGriddedRegion griddedSites) {
    if(!outputDir.endsWith("/")) outputDir = outputDir+"/";

    // some standard lines that will be written to all the condor submit files
    String remoteDir = REMOTE_DIR+remoteMachineSubdirName+"/";

    String fileDataPrefix = "universe = globus\n" +
        "globusscheduler=almaak.usc.edu/jobmanager-fork\n" +
        "initialdir=" + outputDir + "\n";
    String remoteInitDir = "remote_initialdir=" + remoteDir + "\n";
    String fileDataSuffix = "notification=error\n" +
        "transfer_executable=false\n" +
        "queue\n";

     try {
    // file in which DAG will be written
    FileWriter frmap = new FileWriter(outputDir + this.DAG_FILE_NAME);

    // this will create  a new directory for each run on the remote machine
    String condorSubmit = createCondorScript(fileDataPrefix, fileDataSuffix, ""+remoteMachineSubdirName,
                        outputDir, PRE_PROCESSOR_CONDOR_SUBMIT, remoteDir, PRE_PROCESSOR_EXECUTABLE);
    //frmap.write("Job "+this.PRE_PROCESSOR_JOB_NAME+" " +condorSubmit+"\n");

    //creating the directory using the condor_submit on the remote machine
    FileWriter fr = new FileWriter("makeDir.sh");
    fr.write("#!/bin/csh\n");
    fr.write("cd " + outputDir+ "\n");
    fr.write("condor_submit "+ condorSubmit);
    fr.close();
    RunScript.runScript(new String[]{"sh", "-c", "sh "+outputDir+"makeDir.sh"});

    // a post processor which will tar all the files on remote machine after
    // all hazard map calculations are done
    condorSubmit = createCondorScript(fileDataPrefix, fileDataSuffix, "",
                       outputDir, POST_PROCESSOR_CONDOR_SUBMIT, remoteDir, POST_PROCESSOR_EXECUTABLE);
    frmap.write("Job "+this.FINISH_JOB_NAME+" " +condorSubmit+"\n");

    //create shell script to ftp hazard curve tar file from remote machine
    ftpCurvesFromRemoteMachine(outputDir, remoteDir);
    frmap.write("Script POST "+FINISH_JOB_NAME+" "+GET_CURVES_FROM_REMOTE_MACHINE+"\n");



    int numSites = griddedSites.getNumGridLocs(); // num grid locs
    int numLats = griddedSites.getNumGridLats(); //num lats
    int numLons = griddedSites.getNumGridLons(); // num Lons
    int numSitesInWorkUnit = getNumSitesInWorkUnit(numSites, numLats, numLons);

    if (D) System.out.println("num locations:" + numSites);
    int index = 1;
    int endSite = 0;
    int startSite = 0;

    //Name of the main executable file to be executed for the Hazard Maps Calulation.
    String executable =  "executable = "+remoteDir+REMOTE_EXECUTABLE_NAME+"\n";
    //
    fileDataSuffix = "jar_files = " +this.HAZARD_MAP_JAR_FILE_NAME+
                     "transfer_executable=false"+
                     "should_transfer_files=YES"+
                     "WhenToTransferOutput = ON_EXIT"+
                     "transfer_input_files="+regionFileName+","+erfFileName+","+imrFileName+
                     "queue";

    for (int site = 0; site < numSites; site += numSitesInWorkUnit) {
      startSite = site;
      if ( (numSitesInWorkUnit < numLons) &&( (numLons * index - site) < numSitesInWorkUnit)) {
        endSite = numLons * index;
        site = numLons * index - numSitesInWorkUnit;
        ++index;
      } else  endSite = site + numSitesInWorkUnit;

      String arguments = startSite + " " + endSite + " " + regionFileName
          + " " + erfFileName + " " + imrFileName;
      String fileNamePrefix = "HazardCurve"+site;
      String condorSubmitScript =
              createCondorScript(fileDataPrefix, fileDataSuffix, arguments,
                       outputDir, fileNamePrefix + "_" + startSite, remoteDir, executable);
          //write to DAG file
          String jobName = "CurveX"+startSite;
          frmap.write("Job " + jobName + " " + condorSubmitScript+"\n");
          frmap.write("PARENT "+jobName+" CHILD "+this.FINISH_JOB_NAME+"\n");
          //frmap.write("PARENT "+this.PRE_PROCESSOR_JOB_NAME+" CHILD "+jobName+"\n");
     }
        // close the DAG files
        frmap.close();
        // submit the DAG for execution
        submitDag(outputDir,remoteDir);

        //running the dag on the remote from the local machine using the condor_submit
        condorSubmit = createCondorScript(fileDataPrefix, fileDataSuffix, null,
                        outputDir, DAG_CONDOR_SUBMIT, remoteDir, SUBMIT_DAG_SHELL_SCRIPT_NAME);

        //creating the directory using the condor_submit on the remote machine
        fr = new FileWriter("rundag.sh");
        fr.write("#!/bin/csh\n");
        fr.write("cd " + outputDir+ "\n");
        fr.write("condor_submit "+ condorSubmit);
        fr.close();
        RunScript.runScript(new String[]{"sh", "-c", "sh "+outputDir+"rundag.sh"});

      }
      catch (Exception ex) { ex.printStackTrace(); }
    }



    /**
     * This method will create a condor submit script
     */
    private String createCondorScript(String fileDataPrefix,
                                      String fileDataSuffix,
                                      String arguments, String outputDir,
                                      String condorFileNamePrefix,
                                      String remoteDir, String executableName) {
      try {
        String fileName = condorFileNamePrefix + ".sub";
        // make the preprocessor submit script to make new directory for each run
        FileWriter fileWriter = new FileWriter(outputDir + fileName);
        fileWriter.write("executable = " + executableName + "\n");
        fileWriter.write(fileDataPrefix);
        fileWriter.write("remote_initialdir=" + remoteDir + "\n");
        fileWriter.write("arguments = " + arguments + "\n");
        fileWriter.write("Output = " + condorFileNamePrefix + "." + "out\n");
        fileWriter.write("Error = " + condorFileNamePrefix + ".err\n");
        fileWriter.write("Log = " + LOG_FILE_NAME + "\n");
        fileWriter.write(fileDataSuffix);
        fileWriter.close();
        return fileName;
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      return null;
    }


    /**
     * creates the shell to Grid FTP the submit files and Dag to remote machine.
     * It also grid FTP a script that untar the submit files tar and executes the DAG.
     * @param remoteDir
     * @param outputDir
     */
    private void ftpSubmitFilesAndDagToRemoteMachine(String remoteDir,String outputDir){
      try{

        //Shell script to untar the submit files and execute the dag on the remote machine
        FileWriter frScript = new FileWriter(outputDir + SUBMIT_DAG_SHELL_SCRIPT_NAME);
        frScript.write("#!/bin/csh\n");
        frScript.write("cd " +remoteDir+" \n");
        frScript.write("tar -xf "+SUBMIT_TAR_FILES);
        frScript.write("condor_submit_dag " + this.DAG_FILE_NAME + "\n");
        frScript.close();

        // write the post script.
        //When all jobs are finished, grid ftp files from almaak to gravity
        FileWriter frFTP = new FileWriter(outputDir + PUT_SUBMIT_FILES_TO_REMOTE_MACHINE);

        frFTP.write("#!/bin/csh\n");
        frFTP.write("cd " + outputDir+ "\n");
        frFTP.write("tar -cf *.sub "+DAG_FILE_NAME);
        frFTP.write("globus-url-copy gsiftp://gravity.usc.edu" + outputDir +
                    SUBMIT_TAR_FILES +
                    " file:" + remoteDir + SUBMIT_TAR_FILES + "\n");
        frFTP.write("globus-url-copy gsiftp://gravity.usc.edu" + outputDir +
                    SUBMIT_DAG_SHELL_SCRIPT_NAME +
                    " file:" + remoteDir + SUBMIT_DAG_SHELL_SCRIPT_NAME + "\n");
        frFTP.close();
        }catch (Exception e) { e.printStackTrace(); }
    }


    //create shell script to ftp hazard curve tar file from remote machine
   private void ftpCurvesFromRemoteMachine(String outputDir, String remoteDir) {
     try {
       // write the post script.
       //When all jobs are finished, grid ftp files from almaak to gravity
       FileWriter frFTP = new FileWriter(outputDir +
                                         this.GET_CURVES_FROM_REMOTE_MACHINE);
       frFTP.write("#!/bin/csh\n");
       frFTP.write("cd " + outputDir + "\n");
       frFTP.write("globus-url-copy gsiftp://almaak.usc.edu" + remoteDir +
                   OUTPUT_TAR_FILE_NAME +
                   " file:" + outputDir + OUTPUT_TAR_FILE_NAME + "\n");

       frFTP.write("tar xf " + OUTPUT_TAR_FILE_NAME + "\n");
       frFTP.close();
     }catch (Exception e) { e.printStackTrace(); }
   }



   // creates a shell script that will submit the DAG
   private void submitDag(String outputDir, String remoteDir) {
     try {
       ftpCurvesFromRemoteMachine(outputDir, remoteDir);
       RunScript.runScript(new String[]{"sh", "-c", "sh "+outputDir+SUBMIT_DAG_SHELL_SCRIPT_NAME});



       /*FileWriter condorShell = new FileWriter(outputDir +
                                               this.SUBMIT_DAG_SHELL_SCRIPT_NAME);
       condorShell.write("cd " + outputDir + "\n");
       condorShell.write("chmod +x " + this.GET_CURVES_FROM_REMOTE_MACHINE +
                         "\n");
       condorShell.write("condor_submit_dag " + this.DAG_FILE_NAME + "\n");
       condorShell.close();*/

     }catch(Exception e) { e.printStackTrace();}
   }


    /**
     *
     * @param numSites Total number of sites in the region
     * @param numLats Number of Lats
     * @param numLons Number of Lons
     * @param suggestedNumSitesInWorkUnit Suggested number of sites in a workunit
     * @return
     */
    private int getNumSitesInWorkUnit(int numSites, int numLats, int numLons) {
      if(numLons > SUGGESTED_NUM_SITES_IN_WORK_UNIT) {
        // if numlons  are greater
        int numDivs = (int)Math.ceil((double)numLons/(double)SUGGESTED_NUM_SITES_IN_WORK_UNIT);
        return (int)Math.ceil((double)numLons/(double)numDivs);
      } else { //if numLons are less
        int num = (int)Math.floor((double)SUGGESTED_NUM_SITES_IN_WORK_UNIT/(double)numLons);
        return num*numLons;
      }
    }



}