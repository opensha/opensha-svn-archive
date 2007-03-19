package scratchJavaDevelopers.martinez.applications;

import javax.swing.JFrame;

import scratchJavaDevelopers.martinez.beans.ExceptionBean;
import scratchJavaDevelopers.martinez.beans.GuiBeanAPI;

public class TestApp {
	public static void main(String[] args) {
		try {
			foo(null);
		} catch (Exception ex) {
			ExceptionBean eb = new ExceptionBean(ex.getMessage(), "Error", ex);
			((JFrame) eb.getVisualization(GuiBeanAPI.SPLASH)).setVisible(true);
		} finally {
			System.out.println("This comes after the exception bean is shown!");
		}
	}
	
	private static void foo(int[] a) {
		bar(a);
	}
	
	private static void bar(int[] b) {
		JFrame frame = null;
		frame.setVisible(true);
	}
}
