package scratchJavaDevelopers.martinez;

import javax.swing.*;

public class Visual {
	public static void main(String args[]) {
		BenefitCostBean bean = new BenefitCostBean();
		JPanel panel = (JPanel) bean.getVisualization(GuiBeanAPI.APPLICATION);
		JFrame frame = new JFrame("Test Frame");
		frame.add(panel);
		frame.setLocation(500, 500);
		frame.pack();
		frame.setVisible(true);	
	}

}
