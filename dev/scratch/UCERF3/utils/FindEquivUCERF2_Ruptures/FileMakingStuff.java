/**
 * 
 */
package scratch.UCERF3.utils.FindEquivUCERF2_Ruptures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;
import org.opensha.commons.util.ExceptionUtils;

/**
 * @author field
 *
 */
public class FileMakingStuff {
	
	
	public static void mkNewFileBySubstitutingNames() {
		
		// read the name changes file
		File nameChangeFile = new File("dev/scratch/UCERF3/utils/FindEquivUCERF2_Ruptures/scratchFiles/FM2to3_sectionNameChanges.txt");
		Hashtable<String,String> namesMap = new Hashtable<String,String>();
		ArrayList<String> toRemoveList = new ArrayList<String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(nameChangeFile.getPath()));
			int l=-1;
			String line;
			while ((line = reader.readLine()) != null) {
				l+=1;
				String[] st = StringUtils.split(line,"\t");
				String oldName = st[0];
				String newName = st[1];
				if(newName.equals("REMOVED")) {
					System.out.println(oldName+" no longer exists (removed)");
					toRemoveList.add(oldName);
					continue;
				}
				else if(newName.equals("MULTIPLE:")) {
					newName=st[2];
					for(int i=3;i<st.length;i++) {
						newName+="\t"+st[i];
					}
				}
				namesMap.put(oldName, newName);

			}
		} catch (Exception e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}
		
		

		
		// now create the old and new files
		File prevFile = new File("dev/scratch/UCERF3/preComputedData/FindEquivUCERF2_Ruptures/FM2_SectionsForUCERF2_Sources.txt");
		File newFile = new File("dev/scratch/UCERF3/preComputedData/FindEquivUCERF2_Ruptures/FM3_SectionsForUCERF2_Sources.txt");

		System.out.println("Reading file: "+prevFile.getPath());
		try {
			BufferedReader reader = new BufferedReader(new FileReader(prevFile.getPath()));
			FileWriter fw = new FileWriter(newFile);
			
			int l=-1;
			String line, newLine="";
			while ((line = reader.readLine()) != null) {
				l+=1;
				String[] st = StringUtils.split(line,"\t");
				int srcIndex = Integer.valueOf(st[0]);  // note that this is the index for  ModMeanUCERF2, not ModMeanUCERF2_FM2pt1
				if(srcIndex != l)
					throw new RuntimeException("problem with source index");
				String srcName = st[1];
				int faultModelForSource = Integer.valueOf(st[2]);
				newLine = srcIndex+"\t"+srcName+"\t"+faultModelForSource;
				boolean addLine = true;
				for(int i=3;i<st.length;i++) {
					String sectName = st[i];
					if(toRemoveList.contains(sectName)) {
						addLine = false;
					}
					else if(namesMap.containsKey(sectName)) {	// name has changed
						newLine += "\t"+namesMap.get(sectName);
					}
					else {	// no name change
						newLine += "\t"+sectName;
					}
				}
				if(addLine)
					fw.write(newLine+"\n");
			}
			
			fw.close();

		} catch (Exception e) {
			ExceptionUtils.throwAsRuntimeException(e);
		}

		System.out.println("Done reading file");
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FileMakingStuff.mkNewFileBySubstitutingNames();

	}

}
