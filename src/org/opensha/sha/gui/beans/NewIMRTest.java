package org.opensha.sha.gui.beans;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.opensha.sha.earthquake.rupForecastImpl.Frankel96.Frankel96_AdjustableEqkRupForecast;
import org.opensha.sha.earthquake.rupForecastImpl.NewZealand.NewZealandERF0909;
import org.opensha.sha.imr.ScalarIntensityMeasureRelationshipAPI;
import org.opensha.sha.imr.attenRelImpl.BA_2008_AttenRel;
import org.opensha.sha.imr.attenRelImpl.CB_2008_AttenRel;

public class NewIMRTest extends JPanel implements ActionListener {
	
	private JButton noERFButton = new JButton("No ERF");
	private JButton frankelERFButton = new JButton("Frankel 96");
	private JButton nzERFButton = new JButton("New Zealand");
	
	private Frankel96_AdjustableEqkRupForecast frankel = new Frankel96_AdjustableEqkRupForecast();
	private NewZealandERF0909 nz = new NewZealandERF0909();
	
	private IMR_MultiGuiBean bean;
	
	public NewIMRTest() {
		super(new BorderLayout());
		
		ArrayList<ScalarIntensityMeasureRelationshipAPI> attenRels = new ArrayList<ScalarIntensityMeasureRelationshipAPI>();
		attenRels.add(new CB_2008_AttenRel(null));
		attenRels.add(new BA_2008_AttenRel(null));
		bean = new IMR_MultiGuiBean(attenRels);
		
		this.add(bean, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
		buttons.add(noERFButton);
		buttons.add(frankelERFButton);
		buttons.add(nzERFButton);
		
		noERFButton.addActionListener(this);
		frankelERFButton.addActionListener(this);
		nzERFButton.addActionListener(this);
		
		this.add(buttons, BorderLayout.SOUTH);
		
		JFrame window = new JFrame();
		window.setSize(300, 600);
		
		window.setContentPane(this);
		
		window.setVisible(true);
		window.setLocationRelativeTo(null);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new NewIMRTest();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == noERFButton) {
			bean.setTectonicRegions(null);
		} else if (e.getSource() == frankelERFButton) {
			bean.setTectonicRegions(frankel.getIncludedTectonicRegionTypes());
		} else if (e.getSource() == nzERFButton) {
			bean.setTectonicRegions(nz.getIncludedTectonicRegionTypes());
		}
	}

}
