package org.scec.sha.calc;

import java.text.DecimalFormat;
import java.io.*;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.util.RunScript;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * <p>Title: SubmitJobForGridComputation.java </p>
 * <p>Description: This class will accept the filenames of the IMR, ERF,
 * GRIDDEDREGION  and it will create condor submit files and DAG needed for
 * grid computation </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Nitin Gupta and Vipin Gupta
 * @date Mar 15, 2004
 * @version 1.0
 */

public class SubmitJobForGridComputation {

  private final static boolean D = false;
  // parent directory on almaak.usc.edu where computations will take place
  private final static String REMOTE_DIR = "/home/rcf-71/vgupta/pool/OpenSHA/";

  // tar file which will conatin all the hazard curves
  private final static String OUTPUT_TAR_FILE_NAME = "outputfiles.tar";

  //tar file which contain all the submit files and Dag.
  private final static String SUBMIT_TAR_FILES = "submitfiles.tar";
  private final DecimalFormat decimalFormat = new DecimalFormat("0.00##");
  private final static int SUGGESTED_NUM_SITES_IN_WORK_UNIT = 100;

  //private static String REMOTE_EXECUTABLE_NAME = "HazardMapCalculator.pl";
  private final static String REMOTE_EXECUTABLE_NAME = "GridHazardMapCalculator.class";
  // name of the perl executable which will accept a submit file to submit to condor
  private final static String PERL_EXECUTABLE = "OpenSHA_HazardMapCalculator.pl";

  //Hazard Map Jar file using which executable will be executed
  private final static String HAZARD_MAP_JAR_FILE_NAME =
      "opensha_hazardmapcondor.jar";

  // this executable will create a new directory at the machine
  private final static String PRE_PROCESSOR_EXECUTABLE = "HazardMapPreProcessor.sh";
  // name of condor submit file which will submit the pre processor job
  private final static String PRE_PROCESSOR_CONDOR_SUBMIT = "preparation";
  // name of Job that will be used in DAG
  private final static String PRE_PROCESSOR_JOB_NAME = "PREPARATION";

  // this script will be executed after all the hazard map dataset is created
  private final static String POST_PROCESSOR_EXECUTABLE = "HazardMapPostProcessor.sh";
  // name of condor submit file which will submit the post processor job
  private final static String POST_PROCESSOR_CONDOR_SUBMIT = "finish";
  // name of Job that will be used in DAG
  private final static String FINISH_JOB_NAME = "FINISH";

  // files for untarring the submit files on the remote machine
  private final static String UNTAR_CONDOR_SUBMIT = "untarSubmitFiles";
  private final static String UNTAR_CONDOR_SUBMIT_EXECUTABLE= "UntarSubmit.sh";
  private final static String UNTAR_CONDOR_SUBMIT_JOB_NAME="UNTARSUBMIT";



  private final static String DAG_FILE_NAME = "pathway1.dag";
  private final static String LOG_FILE_NAME = "pathway1.log";
  private final static String GET_CURVES_FROM_REMOTE_MACHINE = "getCurves.sh";
  private final static String PUT_SUBMIT_FILES_TO_REMOTE_MACHINE =
      "putSubmitFiles.sh";
  private final static String SUBMIT_DAG_SHELL_SCRIPT_NAME = "submitDag.sh";
  private final static String PERL_JOB_NAME = "CurveX";
  private final static String PERL_CONDOR_SUBMIT = "Job";
  private final static String HAZARD_CURVES_SUBMIT ="HazardCurves";

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
    if (!outputDir.endsWith("/"))
      outputDir = outputDir + "/";

      // some standard lines that will be written to all the condor submit files
    String remoteDir = REMOTE_DIR + remoteMachineSubdirName + "/";

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
      String condorSubmit = createCondorScript(fileDataPrefix, fileDataSuffix,
                                               "" + remoteMachineSubdirName,
                                               outputDir,
                                               PRE_PROCESSOR_CONDOR_SUBMIT,
                                               REMOTE_DIR,
                                               PRE_PROCESSOR_EXECUTABLE);
      frmap.write("Job "+this.PRE_PROCESSOR_JOB_NAME+" " +condorSubmit+"\n");


      //creates the shell script to gridftp the condor submit files(in tar format)
      //to almaak.usc.edu
      ftpSubmitFilesToRemoteMachine(outputDir, remoteDir, imrFileName, erfFileName,
                                     regionFileName);
      frmap.write("Script Post "+PRE_PROCESSOR_JOB_NAME+" "+
                  PUT_SUBMIT_FILES_TO_REMOTE_MACHINE+"\n");


      // now make a condor script which will untar the condor submit files on remote machine
      condorSubmit = createCondorScript(fileDataPrefix, fileDataSuffix, "",
                                        outputDir, UNTAR_CONDOR_SUBMIT,
                                        remoteDir, UNTAR_CONDOR_SUBMIT_EXECUTABLE);
      frmap.write("Job " + this.UNTAR_CONDOR_SUBMIT_JOB_NAME + " " + condorSubmit + "\n");
      frmap.write("PARENT "+PRE_PROCESSOR_JOB_NAME+" CHILD "+UNTAR_CONDOR_SUBMIT_JOB_NAME+"\n");


      // a post processor which will tar all the files on remote machine after
      // all hazard map calculations are done
      condorSubmit = createCondorScript(fileDataPrefix, fileDataSuffix, "",
                                        outputDir, POST_PROCESSOR_CONDOR_SUBMIT,
                                        remoteDir, POST_PROCESSOR_EXECUTABLE);
      frmap.write("Job " + this.FINISH_JOB_NAME + " " + condorSubmit + "\n");


      //create shell script to ftp hazard curve tar file from remote machine
      // to local machine and then untar them on the local machine
      ftpCurvesFromRemoteMachine(outputDir, remoteDir);
      frmap.write("Script POST " + FINISH_JOB_NAME + " " +
                  GET_CURVES_FROM_REMOTE_MACHINE + "\n");


      // make the submit files to submit the jobs
      LinkedList list  = getSubmitFileNames(imrFileName, erfFileName,
                                     regionFileName, outputDir, remoteDir,
                                     griddedSites);
      Iterator it = list.iterator();
      int i=0;
      while(it.hasNext()) {
        condorSubmit = createCondorScript(fileDataPrefix, fileDataSuffix,
                                          "" + (String) it.next(),
                                          outputDir,
                                          PERL_CONDOR_SUBMIT + "_" + i,
                                          remoteDir, PERL_EXECUTABLE);
        String jobName = PERL_JOB_NAME + i;
        frmap.write("Job " + jobName + " " + condorSubmit + "\n");
        frmap.write("PARENT " + jobName + " CHILD " + this.FINISH_JOB_NAME +
                    "\n");
        frmap.write("PARENT " + UNTAR_CONDOR_SUBMIT_JOB_NAME + " CHILD " +
                    jobName + "\n");
      }

      // close the DAG files
      frmap.close();

      // submit the DAG for execution
      submitDag(outputDir, remoteDir);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }


  /**
   * Generate the submit files which will be ftped to almmak and submitted from there
   * @param imrFileName
   * @param erfFileName
   * @param regionFileName
   * @param outputDir
   * @param remoteDir
   * @param griddedSites
   * @return
   */
  private LinkedList getSubmitFileNames(String imrFileName, String erfFileName,
                                     String regionFileName, String outputDir,
                                     String remoteDir,
                                     SitesInGriddedRegion griddedSites) {

    int numSites = griddedSites.getNumGridLocs(); // num grid locs
    int endSite = 0;
    int startSite = 0;

    // some lines needed in the condor submit file
    String fileDataPrefix = "universe = java\n" +
       "globusscheduler=almaak.usc.edu/jobmanager-fork\n" +
       "initialdir=" + remoteDir + "\n";
    String fileDataSuffix = "jar_files = " + this.HAZARD_MAP_JAR_FILE_NAME + "\n" +
        "transfer_executable=false" + "\n" +
        "should_transfer_files=YES" + "\n" +
        "WhenToTransferOutput = ON_EXIT" + "\n" +
        "transfer_input_files=" + HAZARD_MAP_JAR_FILE_NAME+","+
        REMOTE_EXECUTABLE_NAME+","+ regionFileName + "," + erfFileName + "," +
        imrFileName + "\n" +
        "notification=error\n"+
        "queue" + "\n";
    LinkedList list = new LinkedList();

    // snd start index and end index to each computer
    for (int site = 0; site < numSites; site += this.SUGGESTED_NUM_SITES_IN_WORK_UNIT) {
      startSite = site;
      endSite = site + SUGGESTED_NUM_SITES_IN_WORK_UNIT;

      String arguments = REMOTE_EXECUTABLE_NAME.substring(0,REMOTE_EXECUTABLE_NAME.indexOf('.')) +
          " " + startSite+" "+endSite + " " + regionFileName
          + " " + erfFileName + " " + imrFileName;
      String fileNamePrefix = HAZARD_CURVES_SUBMIT + site;
      String condorSubmitScript = createCondorScript(fileDataPrefix, fileDataSuffix, arguments,
                             outputDir, fileNamePrefix + "_" + startSite,
                             remoteDir, REMOTE_EXECUTABLE_NAME);
      list.add(condorSubmitScript);
    }

    return list;
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
   * creates the shell to Grid FTP the submit files to remote machine.
   *
   * @param remoteDir
   * @param outputDir
   */
  private void ftpSubmitFilesToRemoteMachine(String outputDir,
                                             String remoteDir, String imrFileName,
                                             String erfFileName,
                                             String regionFileName) {
    try {

      //When all jobs are finished, grid ftp files from almaak to gravity
      FileWriter frFTP = new FileWriter(outputDir +
                                        PUT_SUBMIT_FILES_TO_REMOTE_MACHINE);

      frFTP.write("#!/bin/csh\n");
      frFTP.write("cd " + outputDir + "\n");
      frFTP.write("tar -cf " + SUBMIT_TAR_FILES + " "+HAZARD_CURVES_SUBMIT+"*.sub "+imrFileName+" "+
                  erfFileName+" "+regionFileName+ "\n");
      frFTP.write("globus-url-copy file:" + outputDir +
                  SUBMIT_TAR_FILES +
                  " gsiftp://almaak.usc.edu" + remoteDir + SUBMIT_TAR_FILES +
                  "\n");
      frFTP.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
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
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  // creates a shell script that will submit the DAG
  private void submitDag(String outputDir, String remoteDir) {
    try {
       FileWriter fw = new FileWriter(outputDir+this.SUBMIT_DAG_SHELL_SCRIPT_NAME);
       fw.write("#!/bin/csh\n");
       fw.write("cd "+outputDir+"\n");
       fw.write("chmod +x "+GET_CURVES_FROM_REMOTE_MACHINE+"\n");
       fw.write("chmod +x "+PUT_SUBMIT_FILES_TO_REMOTE_MACHINE+"\n");
       fw.write("condor_submit_dag "+this.DAG_FILE_NAME+"\n");
       fw.close();
       RunScript.runScript(new String[]{"sh", "-c", "sh "+outputDir+SUBMIT_DAG_SHELL_SCRIPT_NAME});
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

}