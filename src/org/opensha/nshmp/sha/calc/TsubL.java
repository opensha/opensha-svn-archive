package org.opensha.nshmp.sha.calc;

import gov.usgs.db.DBHazardConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * This class is meant to be a lightweight data miner class for fetching T sub L
 * values from the database. This can be run stand-alone on the command line or
 * programatically with a latitude and longitude.
 * 
 * @author emartinez
 */
public class TsubL {
	/**
	 * Simple test method wrapper. Provides command line access to T sub L data
	 * mining functionality.
	 * 
	 * @param args Command line arguments. Should be two arguments, first is
	 *             latitude (decimal degrees) second is longitude (decimal
	 *             degrees).
	 * @throws Exception If an exception occurs. Generally a number format
	 *                   exception (from poorly formatted input), or an SQL
	 *                   exception from the database.
	 */
	public static void main(String [] args) throws Exception {
		if (args.length != 2) {
			System.err.println("Usage: java TsubL <latitude> <longitude>");
			System.exit(-1);
		}

		double latitude = Double.parseDouble(args[0]);
		double longitude = Double.parseDouble(args[1]);

		TsubL calc = new TsubL((new DBHazardConnection()).getConnection());
		int tl = calc.get(latitude, longitude);

		System.out.println(tl);
	}

	// Connection to the database
	private Connection conn = null;
	// Statement to get TsubL from database
	private PreparedStatement query = null;
	// SQL used in prepared statement
	private static final String STUB = 
		"SELECT HC_OWNER.TSUBLRETURN(?, ?) AS TL FROM DUAL";

	/**
	 * Constructor for the TsubL data miner class. This constructor requires an
	 * active database connection.
	 * 
	 * @param conn An active database connection
	 * @throws Exception If an exception occurs
	 */
	public TsubL(Connection conn) throws Exception {
		this.conn = conn;
		this.query = this.conn.prepareStatement(TsubL.STUB);
	}

	/**
	 * Queries the database using the prepared statement based on the STUB. The
	 * latitude value is inserted into the first STUB placeholder and the
	 * longitude value is inserted into the second STUB placeholder.
	 * 
	 * @param latitude  Latitude (decimal degrees) for the target location
	 * @param longitude Longitude (decimal degrees) for the target location
	 * @return The T sub L value for the target location
	 * @throws Exception If an SQLException occurs or if the query fails to
	 *                   return data.
	 */
	public int get(double latitude, double longitude) throws Exception {
		this.query.setDouble(1, latitude);
		this.query.setDouble(2, longitude);
		int tl;

		ResultSet results = this.query.executeQuery();
		if (results.next()) { tl = results.getInt("TL"); }
		else { 
			results.close(); 
			throw new Exception("Query failed to return results.");
		}
		results.close();
		return tl;
	}
}
