package org.opensha.sha.earthquake.rupForecastImpl.GEM1.calc.gemLogicTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.opensha.sha.earthquake.rupForecastImpl.GEM1.SourceData.GEMSourceData;
import org.opensha.sha.earthquake.rupForecastImpl.GEM1.calc.gemHazardCalculator.GemComputeHazardLogicTree;


public class GemLogicTree<Element> implements GemLogicTreeAPI<Element>, Serializable {

	private ArrayList<GemLogicTreeBranchingLevel> branLevLst;
	protected HashMap<String,Element> ebMap;
	private String modelName;
	
	public GemLogicTree(){
		this.branLevLst = new ArrayList<GemLogicTreeBranchingLevel>();
		this.ebMap = new HashMap<String,Element>();
		this.modelName = "";
	}
	
	public GemLogicTree(String fileName) throws IOException, ClassNotFoundException{
		
	    URL data = GemComputeHazardLogicTree.class.getResource(fileName);
	    File file = new File(data.getFile());
        FileInputStream f_in = null;
		try {
			//f_in = new FileInputStream(fileName);
			f_in = new FileInputStream(file.getPath());
		} catch (FileNotFoundException e) {
			System.out.println(file.getPath()+" not found!!");
			e.printStackTrace();
			System.exit(0);
		}
		
	    // Read object using ObjectInputStream.
	    ObjectInputStream obj_in = new ObjectInputStream (f_in);
	
	    // Read an object.
	    Object obj = obj_in.readObject ();
	
	    GemLogicTree<Element> gemLogicTree = (GemLogicTree<Element>) obj;
	    
	    this.branLevLst = gemLogicTree.getBranchingLevelsList();
	    this.ebMap = (HashMap<String, Element>) gemLogicTree.getEBMap();
	    this.modelName = gemLogicTree.getModelName();
		
	}

	/**
	 * 
	 */
	public void addBranchingLevel(GemLogicTreeBranchingLevel branLev){
		this.branLevLst.add(branLev);
	}
	
	/**
	 * 
	 */
	public void addEBMapping(String str, Element obj){
		this.ebMap.put(str,obj);
	}
	
	/**
	 * 
	 */
	public ArrayList<GemLogicTreeBranchingLevel> getBranchingLevelsList(){
		return this.branLevLst;
	}
	
	/**
	 * 
	 */
	public GemLogicTreeBranchingLevel getBranchingLevel(int idx){
		return this.branLevLst.get(idx);
	}	

	/**
	 * 
	 */
	public void setModelName(String str){
		this.modelName = str;
	}
	
	/**
	 * 
	 */
	public String getModelName(){
		return this.modelName;
	}
	
	/**
	 * 
	 */
	public double getWeight(String lab){
		String[] strarr = lab.split("_");
		GemLogicTreeBranchingLevel brl = this.branLevLst.get(strarr.length-1);
		return brl.getBranch(Integer.valueOf(strarr[strarr.length-1]).intValue()).getWeight();
	}
	
	/**
	 * 
	 */
	public double getTotWeight(String lab){
		double weight = 1.0;
		String[] strarr = lab.split("_");
		for (int i=0; i<strarr.length;i++){
			GemLogicTreeBranchingLevel brl = this.branLevLst.get(i);
			GemLogicTreeBranch br = brl.getBranch(Integer.valueOf(strarr[i]).intValue()-1);
			weight = weight * br.getWeight();
		}
		return weight;
	}

	public HashMap<String,Element> getEBMap() {
		return ebMap;
	}

	public Iterator<Element> iterator() {
		return ebMap.values().iterator();
	}

	public void saveGemLogicTreeModel(String fileName) throws Exception {
		
		// Use a FileOutputStream to send data to a file
		FileOutputStream f_out = new FileOutputStream (fileName);

		// Use an ObjectOutputStream to send object data to the
		// FileOutputStream for writing to disk.
		ObjectOutputStream obj_out = new ObjectOutputStream (f_out);

		// Pass our object to the ObjectOutputStream's
		// writeObject() method to cause it to be written out
		// to disk.
		obj_out.writeObject (this);
	}
}
