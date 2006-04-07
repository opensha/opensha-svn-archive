package org.opensha.sha.magdist;

import java.util.ArrayList;

import org.opensha.data.DataPoint2D;
import org.opensha.data.function.DiscretizedFuncList;
import org.opensha.exceptions.*;



/**
 * <p>Title:SummedMagFreqDist.java </p>
 * <p>Description: This class is for summing the various Magnitude Frequency distributions</p>
 *
 * @author Nitin Gupta & Vipin Gupta  date: Aug 8, 2002
 * @version 1.0
 */



public class SummedMagFreqDist extends IncrementalMagFreqDist {

  private boolean saveMagFreqDists=false;     // whether you want to store each distribution
  private boolean saveAllInfo=false;          // whether you want to save info for each distribution
  private DiscretizedFuncList savedMagFreqDists;  // to save the each distribution
  private ArrayList savedInfoList;     // to save the info strings only

  private static String NAME = "Summed Dist";




  /**
    * constructor : this is same as parent class constructor
    * @param min
    * @param num
    * @param delta
    * using the parameters we call the parent class constructors to initialise the parent class variables
    */

   public SummedMagFreqDist(double min,int num,double delta) throws InvalidRangeException{
     super(min,num,delta);
   }




   /**
    * constructor: this is sameas parent class constructor
    * @param min
    * @param max
    * @param num
    * using the min, max and num we calculate the delta
    */

   public  SummedMagFreqDist(double min,double max,int num)
                             throws DiscretizedFuncException,InvalidRangeException {
     super(min,max,num);
   }


   /**
    * constructor : this is same as parent class constructor
    * @param min
    * @param num
    * @param delta
    * @param saveMagFreqDist : whether you want to store each distribution
    * @param saveAllInfo     : whether you want to save info for each distribution
    * using the parameters we call the parent class constructors to initialise the parent class variables
    */

   public SummedMagFreqDist(double min,int num,double delta,
                            boolean saveMagFreqDist,boolean saveAllInfo)
                            throws InvalidRangeException{
     super(min,num,delta);
     this.saveMagFreqDists=saveMagFreqDist;
     this.saveAllInfo = saveAllInfo;

     if(saveMagFreqDists)     // if complete distribution needs to be saved
       savedMagFreqDists = new DiscretizedFuncList();
     else if(saveAllInfo)     // to save info
       savedInfoList = new ArrayList();
   }




   /**
    * constructor: this is same as parent class constructor
    * @param min
    * @param max
    * @param num
    * @param saveMagFreqDist : whether you want to store each distribution
    * @param saveAllInfo     : whether you want to save info for each distribution
    * using the min, max and num we calculate the delta
    */

   public  SummedMagFreqDist(double min,double max,int num,
                             boolean saveMagFreqDist,boolean saveAllInfo)
                             throws DiscretizedFuncException,InvalidRangeException {
     super(min,max,num);
     this.saveMagFreqDists=saveMagFreqDist;
     this.saveAllInfo = saveAllInfo;
     if(saveMagFreqDists)     // if complete distribution needs to be saved
       savedMagFreqDists = new DiscretizedFuncList();
     else if(saveAllInfo)     // to save info
       savedInfoList = new ArrayList();
   }


   /**
    * this function adds the new magnitude frequency distribution
    * the min, num and delta(or max) of new distribution should match min, max
    * and num as specified in the constructor
    * @param magFreqDist new Magnitude Frequency distribution to be added
    */

   public void addIncrementalMagFreqDist(IncrementalMagFreqDist magFreqDist)
               throws DiscretizedFuncException,DataPoint2DException {

     /* check whether mun,num and delta of new distribution matches
        the min, num and delta in  the constructor */

     if(magFreqDist.getMinX()!=minX || magFreqDist.getDelta()!=delta
                                    || magFreqDist.getNum()!=num)
        throw new DiscretizedFuncException("addIncrementalMagFreqDist "+
                  "invalid value of min, num or delta of new distribution");


     for (int i=0;i<num;++i)      // add the y values from this new distribution
       super.set(i,this.getY(i)+ magFreqDist.getY(i));

    if(saveMagFreqDists)         // save this distribution in the list
       savedMagFreqDists.add(magFreqDist);
    else if(saveAllInfo)         // if only info is desired to be saved
       savedInfoList.add(magFreqDist.getInfo());
   }

   /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of the SummedMagFreqDist class and calling the set functions of this from outside
   * @param point
   * @throws MagFreqDistException
   */
  public void set(DataPoint2D point) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the SummedMagFreqDist from outside this class");
  }

  /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of the SummedMagFreqDist class and calling the set functions of this from outside
   * @param x
   * @param y
   * @throws MagFreqDistException
   */
  public void set(double x,double y) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the SummedMagFreqDist from outside this class");
  }

  /**
   * Throws the exception if the set functions are called from outside the class
   * These have been made to prevent the access to the set functions of the EvenlyDiscretizedFunc class
   * by making a objects of the SummedMagFreqDist class and calling the set functions of this from outside
   * @param index
   * @param y
   * @throws MagFreqDistException
   */
  public void set(int index,double y) throws MagFreqDistException {
    throw new MagFreqDistException("Cannot Access the set function of the SummedMagFreqDist from outside this class");
  }






   /**
    * removes this distribution from the list
    * @param magFreqDist
    */

   public void removeIncrementalMagFreqDist(IncrementalMagFreqDist magFreqDist)
                          throws DiscretizedFuncException,DataPoint2DException {

     if(saveMagFreqDists) {    // check if this distribution exists
       int index = savedMagFreqDists.indexOf(magFreqDist);

       if(index==-1)  // if this distribution is not found in the list
         throw new DiscretizedFuncException("this distribution does not exist");
       else           // remove the distribution if it is found
         savedMagFreqDists.remove(magFreqDist);
     }

     else if(saveAllInfo)  {  // check if this distribution exists
       int index = savedInfoList.indexOf(magFreqDist.getInfo());

       if(index==-1)  // if this distribution is not found in the list
         throw new DiscretizedFuncException("this distribution does not exist");
       else          // remove the distribution if it is found
         savedInfoList.remove(magFreqDist.getInfo());
     }
     else
        throw new DiscretizedFuncException("Distributions are not saved");

     for(int i=0;i<num;++i)      // remove the rates associated with the removed distribution
       super.set(i,this.getY(i) - magFreqDist.getY(i));
   }



   /**
    *
    * @return : returns the vector of Strings of Info about each added distribution
    */

  public ArrayList getAllInfo() {

    if(saveMagFreqDists) {
      // construct the info vector on fly from saved distributions
      ArrayList infoVector = new ArrayList();
      for(int i=0; i< savedMagFreqDists.size();++i)
        infoVector.add(savedMagFreqDists.get(i).getInfo());
      return infoVector;
    }

    else if(saveAllInfo) {  // return the info ArrayList
      return savedInfoList;
    }

    else         // if distribution info is not saved
       return null;
  }




  /**
   *
   * @return the list of all distributions in this summed distribution
   */

  public DiscretizedFuncList getMagFreqDists() {
    return savedMagFreqDists;
  }




  /**
   * returns the name of this class
   * @return
   */

  public String getDefaultName() {
    return NAME;
  }




  /**
   * this function returns String for drawing Legen in JFreechart
   * @return : returns the String which is needed for Legend in graph
   */

  public String getDefaultInfo() {
    return ("Sum of these Incremental Mag-Freq Dists");
  }

  /**
   * this function returns String for info
   */

  public String toString() {
    return getInfo();
  }

  /**
   * this method (defined in parent) is deactivated here (name is finalized)

  public void setName(String name) throws  UnsupportedOperationException{
    throw new UnsupportedOperationException("setName not allowed for MagFreqDist.");

  }


   * this method (defined in parent) is deactivated here (name is finalized)

  public void setInfo(String info)throws  UnsupportedOperationException{
    throw new UnsupportedOperationException("setInfo not allowed for MagFreqDist.");

  }*/

}
