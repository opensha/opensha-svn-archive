package org.opensha.sha.calc.IM_EventSetCalc;

import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.opensha.sha.imr.AttenuationRelationship;
import org.opensha.sha.imr.AttenuationRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.USGS_Combined_2004_AttenRel;
import org.opensha.util.FileUtils;
import org.opensha.util.SystemPropertiesUtils;

import java.util.ArrayList;
import java.util.StringTokenizer;
import org.opensha.calc.GaussianDistCalc;
import org.opensha.data.function.ArbitrarilyDiscretizedFunc;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2002</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class IM_EventSetCalcTest extends IM_EventSetCalc{

  private String dirName ;
  private float[] rupRates;
  private float[] meanVals;
  private float[] sigVals;
  
  private ArbitrarilyDiscretizedFunc averagedFunction;
  //checks to see if this teh first IMR/IMT it is reading.
  private boolean first = true;

  public IM_EventSetCalcTest(String inputFileName,String dirName) {
	super(inputFileName,dirName);  
    this.dirName = dirName;
    try {
        parseFile();
      }
      catch (FileNotFoundException ex) {
        ex.printStackTrace();
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }

      createSiteList();
      getMeanSigma();
  }



  double[] imlVals = {.005,.007,.0098,.0137,.0192,.0269,.0376,.0527,.0738,.103,
		  .145,.203,.284,.397,.556,.778,1.09,1.52,2.13};


  private void readFile(String fileName) throws FileNotFoundException, IOException {
    ArrayList fileLines = FileUtils.loadFile(fileName);
    int numEntries = fileLines.size();
    if(first){
    	  rupRates = new float[numEntries];
    	  meanVals = new float[numEntries];
      sigVals = new float[numEntries];
    }
    //getting the Mean and Sigma for each rup from the output file
    for(int i=0;i<numEntries;++i){
      StringTokenizer st = new StringTokenizer((String)fileLines.get(i));
      st.nextToken();
      st.nextToken();
      meanVals[i] = Float.parseFloat(st.nextToken().trim());
      sigVals[i] = Float.parseFloat(st.nextToken().trim());
    }
    fileLines = null;
	if(first){
	    fileLines = FileUtils.loadFile(dirName+"/"+"src_rup_metadata.txt");
	    //getting the event rates for each rup from Src-Rup file
	    for(int i=0;i<numEntries;++i){
	      StringTokenizer st = new StringTokenizer((String)fileLines.get(i));
	      st.nextToken();
	      st.nextToken();
	      rupRates[i] = Float.parseFloat(st.nextToken().trim());
	     }
	}
	first = false;
    fileLines = null;
  }

  
  /**
   * Reads each Attenuation Relationship file and IMT file to get the
   * averaged annualized rates 
   */
  private void getAverageAnnualizedRates(){
	  int numIMTs = supportedIMTs.size();
	  int numIMRs = supportedAttenuationsList.size();
	  initArbFunction();
	  String fileName="";
	    for (int i = 0; i < numIMRs; ++i) {
	      AttenuationRelationshipAPI attenRel = (AttenuationRelationshipAPI)
	          supportedAttenuationsList.get(i);
	      attenRel.setUserMaxDistance(sourceCutOffDistance);
	      for (int j = 0; j < numIMTs; ++j) {
	        String imtLine = (String) supportedIMTs.get(j);
	        String fileNamePrefixCommon = dirName +
		    SystemPropertiesUtils.getSystemFileSeparator() + attenRel.getShortName();
		
		    // opens the files for writing
		    StringTokenizer st = new StringTokenizer(imtLine);
		    int numTokens = st.countTokens();
		    String imt = st.nextToken().trim();
		      
		    String pd = "";
		    if (numTokens == 2) {
		      pd = st.nextToken().trim();
		      fileName = fileNamePrefixCommon + "_" +imt + "_" + pd + ".txt";
		    }
		    else
		    	  fileName = fileNamePrefixCommon + "_" +imt + ".txt";
		    try {
				readFile(fileName);
				createArbFuncForEachIML();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      }	
	    }
	    averageArbFunction();
	    System.out.println(averagedFunction.toString());
  }
  
  
  /**
   * Creates a class instance from a string of the full class name including packages.
   * This is how you dynamically make objects at runtime if you don't know which\
   * class beforehand. For example, if you wanted to create a BJF_1997_AttenRel you can do
   * it the normal way:<P>
   *
   * <code>BJF_1997_AttenRel imr = new BJF_1997_AttenRel()</code><p>
   *
   * If your not sure the user wants this one or AS_1997_AttenRel you can use this function
   * instead to create the same class by:<P>
   *
   * <code>BJF_1997_AttenRel imr =
   * (BJF_1997_AttenRel)ClassUtils.createNoArgConstructorClassInstance("org.opensha.sha.imt.attenRelImpl.BJF_1997_AttenRel");
   * </code><p>
   *
   */
  protected void createIMRClassInstance(String AttenRelClassName) {
    String attenRelClassPackage = "org.opensha.sha.imr.attenRelImpl.";
    try {
      Class listenerClass = Class.forName(
          "org.opensha.param.event.ParameterChangeWarningListener");
      Object[] paramObjects = new Object[] {
          this};
      Class[] params = new Class[] {
          listenerClass};
      Class imrClass = Class.forName(attenRelClassPackage + AttenRelClassName);
      Constructor con = imrClass.getConstructor(params);
      AttenuationRelationshipAPI attenRel = (AttenuationRelationshipAPI) con.newInstance(paramObjects);
      if(attenRel.getName().equals(USGS_Combined_2004_AttenRel.NAME))
    	  	throw new RuntimeException("Cannot use "+USGS_Combined_2004_AttenRel.NAME+" in calculation of Mean and Sigma");
      //setting the Attenuation with the default parameters
      attenRel.setParamDefaults();
      attenRel.getParameter(AttenuationRelationship.SIGMA_TRUNC_TYPE_NAME).
          setValue(AttenuationRelationship.SIGMA_TRUNC_TYPE_1SIDED);
      attenRel.getParameter(AttenuationRelationship.SIGMA_TRUNC_LEVEL_NAME).
          setValue(new Double(3.0));
      attenRel.getParameter(AttenuationRelationship.COMPONENT_NAME).
      setValue(AttenuationRelationship.COMPONENT_AVE_HORZ);
      supportedAttenuationsList.add(attenRel);
    }
    catch (ClassCastException e) {
      e.printStackTrace();
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    catch (InstantiationException e) {
      e.printStackTrace();
    }
  }
  
  
  /**
   * Initializes the function with the 0.0
   *
   */
  private void initArbFunction(){
	  averagedFunction = new ArbitrarilyDiscretizedFunc();
	  int numIMLs = imlVals.length;
	   for(int i=0;i<numIMLs;++i){
		   averagedFunction.set(imlVals[i],0.0);	
	   }
  }
  
  /**
   * Averages the annualized rates values in the function with the number of AttennuationRelationships for which 
   * calcualtion was done. 
   *
   */
  private void averageArbFunction(){
	  int numIMLs = imlVals.length;
	  int numIMRs = supportedAttenuationsList.size();
	   for(int i=0;i<numIMLs;++i)
		   averagedFunction.set(i,averagedFunction.getY(i)/numIMRs);	
	  
  }

  private void createArbFuncForEachIML(){
	
    int numIMLs = imlVals.length;
    int numMeanVals = meanVals.length;
    for(int i=0;i<numIMLs;++i){
      double imlExceedRate = 0 ;
      for(int j=0;j<numMeanVals;++j){
        double stRndVar = (Math.log(imlVals[i]) - meanVals[j]) / sigVals[j];
        imlExceedRate += (GaussianDistCalc.getExceedProb(stRndVar,1,3.0)*rupRates[j]);
      }
      //double val = 1-Math.exp(-imlExceedRate*50.0);
      averagedFunction.set(imlVals[i],averagedFunction.getY(i)+imlExceedRate);	
    }

   }

  public static void main(String[] args) {
    if (args.length != 2) {
      System.out.println("Usage :\n\t" +
          "java -jar [jarfileName] [inputFileName] [output directory name]\n\n");
      System.out.println("jarfileName : Name of the executable jar file, by default it is MeanSigmaCalc.jar");
      System.out.println("inputFileName :Name of the input file, this input file should contain only 3 columns" +
                         " \"Lon Lat Vs30\", For eg: see \"Im_EventSetCalcTest_InputFile.txt\". ");
      System.out.println("output directory name : Name of the output directory where all the output files will be generated");
      System.exit(0);
    }

    IM_EventSetCalcTest meansigmacalctestcomparedtosrl = new
        IM_EventSetCalcTest(args[0],args[1]);
    
     meansigmacalctestcomparedtosrl.getAverageAnnualizedRates();
  }
}
