package org.scec.sha.magdist;

import java.util.Vector;

import org.scec.data.DataPoint2D;
import org.scec.data.function.DiscretizedFuncList;
import org.scec.exceptions.DiscretizedFunction2DException;
import org.scec.exceptions.DataPoint2DException;





/**
 * <p>Title:SummedMagFreqDist.java </p>
 * <p>Description: This class is for summing the various Magnitude Frequency distributions</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Vipin Gupta  date: Aug 8, 2002
 * @version 1.0
 */



public class SummedMagFreqDist extends IncrementalMagFreqDist {

  private boolean saveMagFreqDists=false;     // whether you want to store each distribution
  private boolean saveAllInfo=true;          // whether you want to save info for each distribution
  private DiscretizedFuncList savedMagFreqDists;  // to save the each distribution
  private Vector savedInfoList;     // to save the info strings only

  private String C = "SummedMagFreqDist";




  /**
    * constructor : this is same as parent class constructor
    * @param min
    * @param num
    * @param delta
    * using the parameters we call the parent class constructors to initialise the parent class variables
    */

   public SummedMagFreqDist(double min,int num,double delta){
     super(min,num,delta);
   }




   /**
    * constructor: this is sameas parent class constructor
    * @param min
    * @param max
    * @param num
    * using the min, max and num we calculate the delta
    */

   public  SummedMagFreqDist(double min,double max,int num) {
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
                            boolean saveMagFreqDist,boolean saveAllInfo){
     super(min,num,delta);
     this.saveMagFreqDists=saveMagFreqDist;
     this.saveAllInfo = saveAllInfo;

     if(saveMagFreqDists)     // if complete distribution needs to be saved
       savedMagFreqDists = new DiscretizedFuncList();
     else if(saveAllInfo)     // to save info
       savedInfoList = new Vector();
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
                             boolean saveMagFreqDist,boolean saveAllInfo) {
     super(min,max,num);
     this.saveMagFreqDists=saveMagFreqDist;
     this.saveAllInfo = saveAllInfo;
     if(saveMagFreqDists)     // if complete distribution needs to be saved
       savedMagFreqDists = new DiscretizedFuncList();
     else if(saveAllInfo)     // to save info
       savedInfoList = new Vector();
   }




   /**
    * this function adds the new magnitude frequency distribution
    * the min, num and delta(or max) of new distribution should match min, max
    * and num as specified in the constructor
    * @param magFreqDist new Magnitude Frequency distribution to be added
    */

   public void addIncrementalMagFreqDist(IncrementalMagFreqDist magFreqDist)
               throws DiscretizedFunction2DException,DataPoint2DException {

     /* check whether mun,num and delta of new distribution matches
        the min, num and delta in  the constructor */

     if(magFreqDist.getMinX()!=minX || magFreqDist.getDelta()!=delta
                                    || magFreqDist.getNum()!=num)
        throw new DiscretizedFunction2DException(C+":addIncrementalMagFreqDist"+
                  "invalid value of min, num or delta of new distribution");


     for (int i=0;i<num;++i)      // add the y values from this new distribution
       set(i,this.getY(i)+ magFreqDist.getY(i));

    if(saveMagFreqDists)         // save this distribution in the list
       savedMagFreqDists.add(magFreqDist);
    else if(saveAllInfo)         // if only info is desired to be saved
       savedInfoList.add(magFreqDist.getInfo());
   }




   /**
    * removes this distribution from the list
    * @param magFreqDist
    */

   public void removeIncrementalMagFreqDist(IncrementalMagFreqDist magFreqDist)
                          throws DiscretizedFunction2DException,DataPoint2DException {

     if(saveMagFreqDists) {    // check if this distribution exists
       int index = savedMagFreqDists.indexOf(magFreqDist);

       if(index==-1)  // if this distribution is not found in the list
         throw new DiscretizedFunction2DException("this distribution does not exist");
       else           // remove the distribution if it is found
         savedMagFreqDists.remove(magFreqDist);
     }

     else if(saveAllInfo)  {  // check if this distribution exists
       int index = savedInfoList.indexOf(magFreqDist.getInfo());

       if(index==-1)  // if this distribution is not found in the list
         throw new DiscretizedFunction2DException("this distribution does not exist");
       else          // remove the distribution if it is found
         savedInfoList.remove(magFreqDist.getInfo());
     }
     else
        throw new DiscretizedFunction2DException("Distributions are not saved");

     for(int i=0;i<num;++i)      // remove the rates associated with the removed distribution
       set(i,this.getY(i) - magFreqDist.getY(i));
   }



   /**
     * this function will throw an exception if the given x-value is not
     * within tolerance of one of the x-values in the function
     */
    public void set(DataPoint2D point) throws DataPoint2DException {
        super.set(point);
    }

    /**
     * this function will throw an exception if the given x-value is not
     * within tolerance of one of the x-values in the function
     */
    public void set(double x, double y) throws DataPoint2DException {
       super.set(x,y);
    }

    /**
     * this function will throw an exception if the index is not
     * within the range of 0 to num -1
     */
    public void set(int index, double y) throws DataPoint2DException {
        super.set(index,y);
    }


   /**
    *
    * @return : returns the vector of Strings of Info about each added distribution
    */

  public Vector getAllInfo() {

    if(saveMagFreqDists) {
      // construct the info vector on fly from saved distributions
      Vector infoVector = new Vector();
      for(int i=0; i< savedMagFreqDists.size();++i)
        infoVector.add(savedMagFreqDists.get(i).getInfo());
      return infoVector;
    }

    else if(saveAllInfo) {  // return the info Vector
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

  public String getName() {
    return "SummedMagFreqDist";
  }




  /**
   * this function returns String for drawing Legen in JFreechart
   * @return : returns the String which is needed for Legend in graph
   */

  public String getInfo() {
    return ("Sum of several Incremental Mag-Freq Dists");
  }

}