package org.opensha.sha.calc.hazardMap;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CalculationStatus implements Serializable {
	
	String message;
	int total;
	int ip;
	int done;
	int retrieved;
	Date date;
	
	public CalculationStatus(String message, Date date, int total, int ip, int done, int retrieved) {
		this.message = message;
		this.total = total;
		this.ip = ip;
		this.done = done;
		this.retrieved = retrieved;
		this.date = date;
	}

	public String getMessage() {
		return message;
	}

	public int getTotal() {
		return total;
	}

	public int getIp() {
		return ip;
	}

	public int getDone() {
		return done;
	}

	public int getRetrieved() {
		return retrieved;
	}

	public Date getDate() {
		return date;
	}
	
	public String toString() {
		SimpleDateFormat format = HazardMapJobCreator.LINUX_DATE_FORMAT;
		return "Message: " + message + "\n"
				+ "Total: " + total + "\n"
				+ "In Progress: " + ip + "\n"
				+ "Done: " + done + "\n"
				+ "Retrieved: " + retrieved + "\n"
				+ "Date: " + format.format(date) + "\n";
	}
}
