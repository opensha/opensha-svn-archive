package scratchJavaDevelopers.kevin;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CPTGen {

	public CPTGen(ArrayList<int[]> colors, double min, double max, String outFile) throws IOException {
		double step = (max - min) / ((double)colors.size() - 1d);
		FileWriter write = new FileWriter(outFile);
		int i=0;
		for (double curr = min; max-curr > 0.001; curr += step) {
			int color1[] = colors.get(i);
			int color2[] = colors.get(i+1);
			String line = round(curr) + "\t" + color1[0] + "\t" + color1[1] + "\t" + color1[2] + "\t" + round(curr + step) + "\t" + color2[0] + "\t" + color2[1] + "\t" + color2[2];
			System.out.println(line);
			write.write(line + "\n");
			i++;
		}
		int colB[] = colors.get(0);
		int colF[] = colors.get(colors.size() - 1);
		write.write("B " + colB[0] + "\t" + colB[1] + "\t" + colB[2] +"\n");
		write.write("F " + colF[0] + "\t" + colF[1] + "\t" + colF[2] +"\n");
		write.write("N 127	127	127\n");
		write.close();
	}
	
	public double round(double num) {
		int newNum = (int)(num * 100d + 0.5);
		return (double)newNum / 100d;
	}
	
	public static void main(String[] args) {
		ArrayList<int[]> colors = new ArrayList<int[]> ();
//		int color1[] = {0, 0, 170};
//		int color2[] = {0, 255, 255};
//		int color3[] = {0, 170, 30};
//		int color4[] = {200, 220, 15};
//		int color5[] = {255, 255, 0};
//		int color6[] = {255, 127, 0};
//		int color7[] = {255, 0, 0};
//		int color8[] = {255, 0, 255};
		
		int color1[] = {0, 0, 255};
		int color2[] = {0, 255, 255};
		int color3[] = {0, 255, 0};
		int color4[] = {255, 255, 0};
		int color5[] = {255, 127, 0};
		int color6[] = {255, 0, 0};
		int color7[] = {255, 0, 255};
		
		colors.add(color1);
		colors.add(color2);
		colors.add(color3);
		colors.add(color4);
		colors.add(color5);
		colors.add(color6);
		colors.add(color7);
//		colors.add(color8);
		try {
			new CPTGen(colors, 0.05, 0.65, "newcpt.cpt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
