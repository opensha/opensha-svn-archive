package scratchJavaDevelopers.kevin.hazMapApplet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opensha.gui.UserAuthDialog;
import org.opensha.util.http.HTTPAuthenticator;
import org.opensha.util.http.InstallSSLCert;
import org.opensha.util.http.StaticPasswordAuthenticator;

public class CreateDataManager extends StepManager {
	
	boolean useSSL = false;
	
	private String username = "";
	private char[] password = null;
	
	private Step hazardStep;
	private Step regionStep;
	private Step gridStep;
	private Step submitStep;
	
	public CreateDataManager(HazardMapApplet parent) {
		super(parent, null, parent.getConsole());
		
		// SSL's not working from within an applet, at least for now. Just use it if it's an application.
		if (!parent.isApplet())
			useSSL = true;
		
		this.createSteps();
		
		this.init();
		
		try {
			this.authenticate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createSteps() {
		hazardStep = this.createHazardStep();
		regionStep = this.createRegionStep();
		gridStep = this.createGridStep();
		submitStep = this.createSubmitStep();
		
		steps.add(hazardStep);
		steps.add(regionStep);
		steps.add(gridStep);
		steps.add(submitStep);
	}
	
	private Step createHazardStep() {
		JPanel hazardPanel = new JPanel();
		hazardPanel.setLayout(new BorderLayout());
		hazardPanel.add(new JLabel("ERF/IMR Params"), BorderLayout.CENTER);
		
		return new Step(hazardPanel, "Hazard Calculation Settings");
	}
	
	private Step createRegionStep() {
		JPanel regionPanel = new JPanel();
		regionPanel.setLayout(new BorderLayout());
		regionPanel.add(new JLabel("Region Settings"), BorderLayout.CENTER);
		
		return new Step(regionPanel, "Region Settings");
	}
	
	private Step createGridStep() {
		JPanel gridPanel = new JPanel();
		gridPanel.setLayout(new BorderLayout());
		gridPanel.add(new JLabel("Grid Computing Settings"), BorderLayout.CENTER);
		
		return new Step(gridPanel, "Grid Computing Settings");
	}
	
	private Step createSubmitStep() {
		JPanel submitPanel = new JPanel();
		submitPanel.setLayout(new BorderLayout());
		submitPanel.add(new JLabel("Dataset ID/E-Mail/Submit"), BorderLayout.CENTER);
		
		return new Step(submitPanel, "Submit");
	}
	
	public static String intensityMD5 = "2a 74 46 db b4 47 64 db f2 26 9c 95 67 2a cb 57";
	
	private boolean authenticate() throws IOException {
		File keystore = null;
		URL url = null;
		if (useSSL) {
			InstallSSLCert installCert = new InstallSSLCert(intensityMD5, "intensity.usc.edu");
			keystore = installCert.getKeyStore();
			
			if (keystore == null)
				return false;
			
			System.out.println("Loading keystore from: " + keystore.getAbsolutePath());
			System.setProperty("javax.net.ssl.trustStore", keystore.getAbsolutePath());
			
			url = new URL("https://intensity.usc.edu/trac/opensha/wiki/");
		} else {
			url = new URL("http://intensity.usc.edu/trac/opensha/wiki/");
		}
		HTTPAuthenticator auth = new HTTPAuthenticator();
		UserAuthDialog dialog = auth.getDialog();
		Authenticator.setDefault(auth);
//		URL url = new URL("https://intensity.usc.edu/");
		boolean success = false;
		while (true) {
			try {
				InputStream ins = url.openConnection().getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
				String str;
				System.out.println("Authentication successful!");
				success = true;
				this.username = dialog.getUsername();
				this.password = dialog.getPassword();
				// lets use this good password from now on!
				Authenticator.setDefault(new StaticPasswordAuthenticator(this.username, this.password));
				break;
			} catch (java.net.ProtocolException e) {
				if (auth.getDialog().isCanceled()) {
					success = false;
					break;
				}
				System.out.println("Your password is incorrect!");
				continue;
			}
		}
		if (keystore != null)
			keystore.delete();
		return success;
	}

	public static void main(String args[]) {
		CreateDataManager creator = new CreateDataManager(null);
		
		JFrame frame = new JFrame();
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(creator.getPanel(), BorderLayout.CENTER);
		frame.setContentPane(panel);
		frame.setSize(new Dimension(700, 550));
		frame.setVisible(true);
	}
}
