package org.opensha.refFaultParamDb.servlets;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opensha.commons.util.MailUtil;
import org.opensha.commons.util.Preferences;

/**
 * <p>Title: RefFaultDB_UpdateEmailServlet.java </p>
 * <p>Description: This class will send an email whenever an addition is made to the
 * Ref Fault Param database </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class RefFaultDB_UpdateEmailServlet extends HttpServlet {
	public final static String SERVLET_ADDRESS = Preferences.OPENSHA_SERVLET_URL + "Fault_DB_EmailServlet";
	
	//static Strings to send the mails
	private String emailTo, smtpHost, emailSubject, emailFrom, emailEnabled;
	private boolean isEmailEnabled;
	private final static String CONFIG_NAME = "EmailConfig";

	public void init() throws ServletException {
		try {
			Properties p = new Properties();
			String fileName = getInitParameter(CONFIG_NAME);
			p.load(new FileInputStream(fileName));
			emailTo = (String) p.get("EmailTo");
			smtpHost = (String) p.get("SmtpHost");
			emailSubject =  (String) p.get("Subject");
			emailFrom =(String) p.get("EmailFrom");
			isEmailEnabled = Boolean.valueOf((String) p.get("EmailEnabled")).booleanValue();
			System.out.println(emailTo+","+smtpHost+","+smtpHost+","+emailSubject+","+emailSubject+","+isEmailEnabled);
		}
		catch (FileNotFoundException f) {f.printStackTrace();}
		catch (IOException e) {e.printStackTrace();}
	}


	//Process the HTTP Get request
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws
	ServletException, IOException {

		try {
			// get an input stream from the applet
			ObjectInputStream inputFromApplet = new ObjectInputStream(request.
					getInputStream());
			//getting the email content from the aplication
			String emailMessage = (String) inputFromApplet.readObject();
			inputFromApplet.close();
			if(isEmailEnabled) // send email to database curator
				MailUtil.sendMail(smtpHost,emailFrom,emailTo,this.emailSubject,emailMessage);
			// report to the user whether the operation was successful or not
			// get an ouput stream from the applet
			ObjectOutputStream outputToApplet = new ObjectOutputStream(response.
					getOutputStream());
			outputToApplet.writeObject("Email Sent");
			outputToApplet.close();

		}
		catch (Exception e) {
			// report to the user whether the operation was successful or not
			e.printStackTrace();
		}
	}



	//Process the HTTP Post request
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws
	ServletException, IOException {
		// call the doPost method
		doGet(request, response);
	}


}