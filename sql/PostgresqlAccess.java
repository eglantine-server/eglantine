/*
 * Copyright 2015 Christophe Drion
 * 
 * @author christophe.drion@gmail.com
 * @version 1.3
 * @since 2015-02-23
 */
package com.eglantine.sql;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.rowset.serial.SerialArray;

import org.postgresql.jdbc.PgArray;

/**
 *
 * @author postgresqltutorial.com
 */
public class PostgresqlAccess {

	private static String url = "jdbc:postgresql://localhost/eglantine";
	private static String user = "eglantine";
	private static String password = "banyuls2018";

	/**
	 * Connect to the PostgreSQL database
	 *
	 * @return a Connection object
	 */

	static public Connection connect() {
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection(url, user, password);
			//			System.out.println("Connected to the PostgreSQL server successfully.");
		}
		catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}        
		return connection;
	}

}