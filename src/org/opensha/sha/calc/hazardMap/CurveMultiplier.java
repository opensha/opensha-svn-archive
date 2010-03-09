package org.opensha.sha.calc.hazardMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.opensha.commons.data.Location;
import org.opensha.commons.data.function.ArbitrarilyDiscretizedFunc;
import org.opensha.commons.data.function.DiscretizedFuncAPI;

public class CurveMultiplier {
	
	private String inputDir;
	private String outputDir;
	
	public CurveMultiplier(String inputDir, String outputDir) {
		File outputDirFile = new File(outputDir);
		
		if (!outputDirFile.exists())
			outputDirFile.mkdirs();
		
		// make sure they all end in File.separator
		if (!outputDir.endsWith(File.separator))
			outputDir += File.separator;
		if (!inputDir.endsWith(File.separator))
			inputDir += File.separator;
		
		this.inputDir = inputDir;
		this.outputDir = outputDir;
	}
	
	public void multiplyCurves(double factor) throws IOException {
		// we use the first curve dir to specify all of the points
		
		File masterDir = new File(inputDir);
		File[] dirList = masterDir.listFiles();
		
		// for each file in the list
		for(File dir : dirList){
			// make sure it's a subdirectory
			if (dir.isDirectory() && !dir.getName().endsWith(".")) {
				File[] subDirList=dir.listFiles();
				for(File file : subDirList) {
					String fileName = file.getName();
					
					File outSubDirFile = new File(outputDir + dir.getName());
					if (!outSubDirFile.exists())
						outSubDirFile.mkdir();
					//files that ends with ".txt"
					if(fileName.endsWith(".txt")){
						Location loc = MakeXYZFromHazardMapDir.decodeFileName(fileName);
						if (loc != null) {
							String relativePath = dir.getName() + File.separator + fileName;
							System.out.println(relativePath);
							
							DiscretizedFuncAPI func = ArbitrarilyDiscretizedFunc.loadFuncFromSimpleFile(inputDir + relativePath);
							
							DiscretizedFuncAPI aveCurve = multiplyCurve(func, factor);
							
							ArbitrarilyDiscretizedFunc.writeSimpleFuncFile(aveCurve, outputDir + relativePath);
						}
					}
				}
			}
		}
	}
	
	public static DiscretizedFuncAPI multiplyCurve(DiscretizedFuncAPI curve, double factor) {
		ArbitrarilyDiscretizedFunc multFunc = new ArbitrarilyDiscretizedFunc();
		
		int numPoints = curve.getNum();
		
		for (int i=0; i<numPoints; i++) {
			double x = curve.getX(i);
			double y = curve.getY(x) * factor;
			
			multFunc.set(x, y);
		}
		
		return multFunc;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayList<String> dirs = new ArrayList<String>();
		String outputDir = null;
		if (args.length > 3) {
			for (int i=0; i<(args.length-1); i++) {
				dirs.add(args[i]);
			}
			outputDir = args[args.length-1];
		} else {
			System.out.println("USAGE: CurveAverager dir1 dir2 dir3 [... dirN] outputDir");
			System.exit(2);
		}
		
		CurveAverager ave = new CurveAverager(dirs, outputDir);
		try {
			ave.averageDirs();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
