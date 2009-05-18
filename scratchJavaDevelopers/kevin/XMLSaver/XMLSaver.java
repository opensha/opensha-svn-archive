package scratchJavaDevelopers.kevin.XMLSaver;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.dom4j.Document;
import org.dom4j.Element;
import org.opensha.commons.util.XMLUtils;


public abstract class XMLSaver extends JFrame implements ActionListener {
	
	JPanel beanPanel;
	
	JPanel main = new JPanel(new BorderLayout());
	
	JPanel buttonPanel = new JPanel();
	
	JButton saveButton = new JButton("Save To XML");

	public XMLSaver() {
		super();
	}
	
	public void init() {
		this.beanPanel = this.getPanel();
		
		saveButton.addActionListener(this);
		
		buttonPanel.add(saveButton);
		
		main.add(this.beanPanel, BorderLayout.CENTER);
		main.add(buttonPanel, BorderLayout.SOUTH);
		
		this.setContentPane(main);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setSize(500, 700);
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == saveButton) {
			Document doc = XMLUtils.createDocumentWithRoot();
			Element el = getXML(doc.getRootElement());
			
			try {
				XMLUtils.writeDocumentToFile("output.xml", doc);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public abstract Element getXML(Element root);
	
	public abstract JPanel getPanel();
}
