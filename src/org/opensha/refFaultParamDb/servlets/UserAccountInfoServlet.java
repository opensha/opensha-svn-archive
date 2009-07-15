package org.opensha.refFaultParamDb.servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import org.opensha.commons.util.MailUtil;

import java.util.Properties;

/**
 * <p>Title: UserAccountInfoServlet.java </p>
 * <p>Description: This class will send an email to user when user requests account
 * information </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class UserAccountInfoServlet extends HttpServlet {

	public final static String SERVLET_ADDRESS = "http://opensha.usc.edu:8080/OpenSHA/Fault_DB_AccountServlet";

	//static Strings to send the mails
	private String  smtpHost, emailSubject, emailFrom;
	private final static String CONFIG_NAME = "EmailConfig";

	public void init() throws ServletException {
		try {
			Properties p = new Properties();
			String fileName = getInitParameter(CONFIG_NAME);
			p.load(new FileInputStream(fileName));
			smtpHost = (String) p.get("SmtpHost");
			emailSubject =  (String) p.get("AccountInfoSubject");
			emailFrom =(String) p.get("EmailFrom");
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
			String emailTo = (String) inputFromApplet.readObject();
			//getting the email content from the aplication
			String emailMessage = (String) inputFromApplet.readObject();
			inputFromApplet.close();
			// send email to user
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
