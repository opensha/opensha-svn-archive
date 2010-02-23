package scratch.kevin;

import Jama.Matrix;

public class MatrixSpeedTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long start = System.currentTimeMillis();
		int imls = 200;
		Matrix mafe = new Matrix(611309, imls);
		for (int i=0; i<611309; i++) {
			for (int j=0; j<imls; j++) {
//				mafe.set(i, j, Math.random());
				mafe.set(i, j, i * j);
			}
		}
		Matrix pe = new Matrix(imls, 1);
		for (int i=0; i<imls; i++) {
			pe.set(i, 0, imls - i);
		}
		
		long mult = System.currentTimeMillis();
		mafe.times(pe);
		long end = System.currentTimeMillis();
		
		System.out.println("total: " + ((end - start) / 1000d) + " secs");
		System.out.println("populate: " + ((mult - start) / 1000d) + " secs");
		System.out.println("mult: " + ((end - mult) / 1000d) + " secs");
	}

}
