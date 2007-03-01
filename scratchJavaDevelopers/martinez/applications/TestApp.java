package scratchJavaDevelopers.martinez.applications;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JFrame;

import org.opensha.sha.gui.beans.ERF_GuiBean;

public class TestApp {
	public static void main(String[] args) {
		ArrayList<String> erfs = new ArrayList<String>();
		erfs.add("org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast");
		ERF_GuiBean erfBean = null;
		try {
			erfBean = new ERF_GuiBean(erfs);
		} catch (InvocationTargetException ex) {
			System.err.println(ex.getMessage());
		}
		JFrame frame = new JFrame();
		frame.add(erfBean);
		frame.pack();
		frame.setVisible(true);
	}
}
