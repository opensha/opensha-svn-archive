package org.scec.sha.calc;

import java.text.DecimalFormat;
import java.io.*;
import java.util.LinkedList;
import java.util.Iterator;
import org.scec.data.region.SitesInGriddedRegion;
import org.scec.util.RunScript;
import org.scec.cme.SRBDrop.SRBDrop;
import org.scec.sha.gui.servlets.HazardMapCalcServlet;

/**
 * <p>Title: SubmitJobForMultiprocessorComputation</p>
 * <p>Description: This creates the script to submit the job on the multiprocessor
 * machine. It submits the job on the multi-processor shared memory using the PBS
 * script.</p>
 * @author : Nitin Gupta , Vipin Gupta
 * @created : May 03, 2004
 * @version 1.0
 */

public class SubmitJobForMultiprocessorComputation extends SubmitJobForGridComputation {

  private final static boolean D = false;


  //number of processors requested to run this job
  public final static int NUM_OF_PROCESSORS_AVAILABLE =8;

  //maximum wall time that we are requesting the processors for (in minutes)
  public final static double MAX_WALL_TIME =180;



  private final static String REMOTE_EXECUTABLE_NAME = "MapCalcUsingMultiprocessor.sh";

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


  /**
   *
   * @param imrFileName FileName in which IMR is saved as a serialized object
   * @param erfFileName Filename in which ERF is saved as a serialized object
   * @param regionFileName FileName in which Region is saved as a serialized object
   * @param outputDir directory where condor submit files will be created
   * @param remoteMachineSubdir subdirectory on remote machine where computations will take
   * place. So, compuatations will take place in directory /home/rcf-71/vgupta/pool/remoteMachineSubdirName
   */
  public SubmitJobForMultiprocessorComputation(String imrFileName, String erfFileName,
                                     String regionFileName,
                                     String xValuesFileName,
                                     double maxDistance,
                                     String outputDir,
                                     long remoteMachineSubdirName,
                                     SitesInGriddedRegion griddedSites,
                                     String emailAddr) {
    if (!outputDir.endsWith("/"))
      outputDir = outputDir + "/";

    //creating the directory for arranging the hazard map data files in a
    //organized manner.
    createDirectoriesForHazardMapData(outputDir);

    // some standard lines that will be written to all the condor submit files
    String remoteDir = REMOTE_DIR + remoteMachineSubdirName + "/";

    String fileDataPrefix = "universe = globus\n" +
        "globusscheduler=almaak.usc.edu/jobmanager-fork\n" +
        "initialdir=" + outputDir+SUBMIT_FILES_DIR+ "\n";
    String remoteInitDir = "remote_initialdir=" + remoteDir + "\n";
    String fileDataSuffix = "notification=error\n" +
        "transfer_executable=false\n" +
        "queue\n";

    try {
      // file in which DAG will be written
      FileWriter frmap = new FileWriter(outputDir+SUBMIT_FILES_DIR+ this.DAG_FILE_NAME);

      // this will create  a new directory for each run on the remote machine
      String condorSubmit = createCondorScript(fileDataPrefix, fileDataSuffix,
                                               "" + remoteMachineSubdirName,
                                               outputDir,outputDir+SUBMIT_FILES_DIR,
                                               PRE_PROCESSOR_CONDOR_SUBMIT,
                                               REMOTE_DIR,
                                               PRE_PROCESSOR_EXECUTABLE);
      frmap.write("Job "+this.PRE_PROCESSOR_JOB_NAME+" " +condorSubmit+"\n");


      // a post processor which will tar all the files on remote machine after
      // all hazard map calculations are done
      condorSubmit = createCondorScript(fileDataPrefix, fileDataSuffix, remoteDir+" "+
                                        new String(DIRECTORY_PATH_FOR_SRB+remoteMachineSubdirName),
                                        outputDir,outputDir+SUBMIT_FILES_DIR, POST_PROCESSOR_CONDOR_SUBMIT,
                                        remoteDir, POST_PROCESSOR_EXECUTABLE);
      frmap.write("Job " + this.FINISH_JOB_NAME + " " + condorSubmit + "\n");


      //create shell script to ftp hazard curve tar file from remote machine
      // to local machine and then untar them on the local machine
      ftpCurvesFromRemoteMachine(outputDir, remoteDir,
                                 griddedSites.getNumGridLocs(),
                                 emailAddr,
                                 remoteMachineSubdirName);

      frmap.write("Script POST " + FINISH_JOB_NAME + " " +
                  outputDir+SCRIPT_FILES_DIR+GET_CURVES_FROM_REMOTE_MACHINE + "\n");


      // make the submit files to submit the jobs
      getSubmitFileNames(imrFileName, erfFileName,
                         regionFileName, xValuesFileName,
                         maxDistance,
                         outputDir+SUBMIT_FILES_DIR, remoteDir, outputDir,
                         griddedSites);

      // close the DAG files
      frmap.close();

      // submit the DAG for execution
      submitDag(outputDir, remoteDir);

      //putting the information related to this hazard map in the SRB.
      SRBDrop srb = new SRBDrop(true);
      //putting the whole eventID containing all the files to the SRB
      srb.directoryPut(outputDir,new String(DIRECTORY_PATH_FOR_SRB+remoteMachineSubdirName),true);
      String localPathtoMetadataFile = outputDir+DATA_DIR+HazardMapCalcServlet.METADATA_FILE_NAME;
      String remotePathToMetadataFile = DIRECTORY_PATH_FOR_SRB+remoteMachineSubdirName+
                                        DATA_DIR+HazardMapCalcServlet.METADATA_FILE_NAME;
      srb.addMDToCollection(localPathtoMetadataFile,remotePathToMetadataFile,"=");
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
  protected void getSubmitFileNames(String imrFileName, String erfFileName,
                                     String regionFileName,
                                     String xValuesFileName,
                                     double maxDistance,
                                     String submitFilesDir, String remoteDir,
                                     String outputDir,
                                     SitesInGriddedRegion griddedSites) {


    // some lines needed in the condor submit file
    String fileDataPrefix = "universe = universe\n" +
                            "globusscheduler=almaak.usc.edu/jobmanager-pbs\n" +
                            "initialdir=" + outputDir + "\n"+
                            "globusrsl = (count="+NUM_OF_PROCESSORS_AVAILABLE+") (hostcount=1) "+
                            " (jobtype=mpi) (max_wall_time="+MAX_WALL_TIME+")"+"\n";

    String fileDataSuffix ="WhenToTransferOutput = ON_EXIT" + "\n" +
                           "notification=error\n"+
                           "queue" + "\n";

    String arguments = imrFileName+" "+regionFileName + " " + erfFileName;
    String condorSubmitScript = createCondorScript(fileDataPrefix, fileDataSuffix,
        arguments,
        outputDir, submitFilesDir, HAZARD_CURVES_SUBMIT,
        remoteDir, REMOTE_EXECUTABLE_NAME);
  }



  // creates a shell script that will submit the DAG
  protected void submitDag(String outputDir, String remoteDir) {
    try {
      FileWriter fw = new FileWriter(outputDir+SUBMIT_FILES_DIR+this.SUBMIT_DAG_SHELL_SCRIPT_NAME);
      fw.write("#!/bin/csh\n");
      fw.write("cd "+outputDir+SUBMIT_FILES_DIR+"\n");
      fw.write("chmod +x "+outputDir+SCRIPT_FILES_DIR+GET_CURVES_FROM_REMOTE_MACHINE+"\n");
      fw.write("condor_submit_dag "+DAG_FILE_NAME+"\n");
      fw.close();
      RunScript.runScript(new String[]{"sh", "-c", "sh "+outputDir+SUBMIT_FILES_DIR+SUBMIT_DAG_SHELL_SCRIPT_NAME});
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
