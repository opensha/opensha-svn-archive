package scratchJavaDevelopers.martinez.LossCurveSandbox;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JWindow;

import scratchJavaDevelopers.martinez.LossCurveSandbox.ui.gui.AbstractGuiEditor;
import scratchJavaDevelopers.martinez.LossCurveSandbox.ui.gui.VulnerabilityGuiEditor;
import scratchJavaDevelopers.martinez.LossCurveSandbox.util.MenuMaker;

public class LossCurveGui {
	public static void main(String [] args) {
		
		JLabel splashLabel = new JLabel(new ImageIcon(LossCurveGui.class.getResource("/etc/img/lossCurveSplash.png")));
		JWindow splashWindow = new JWindow();
		splashWindow.getContentPane().add(splashLabel);
		splashWindow.pack();
		splashWindow.setLocation(
			(int) (AbstractGuiEditor.screenSize.width - splashWindow.getWidth()) / 2,
			(int) (AbstractGuiEditor.screenSize.height - splashWindow.getHeight()) / 2
		);
		
		splashWindow.setVisible(true);
		
		VulnerabilityGuiEditor editor = new VulnerabilityGuiEditor();
		
		JFrame appWindow = editor.getWindowEditor();
		appWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		JMenu adv_menu = new JMenu("Advanced");
		adv_menu.add(editor.getMenuOptions().get(MenuMaker.ADV_MENU));
		menuBar.add(adv_menu);
		appWindow.setJMenuBar(menuBar);
		
		appWindow.pack();
		
		splashWindow.setVisible(false);
		splashWindow.dispose();
		
		appWindow.setVisible(true);
		
	}
}
