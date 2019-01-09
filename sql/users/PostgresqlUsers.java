package com.eglantine.sql.users;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.postgresql.jdbc.PgArray;

import com.eglantine.secret.Secret;
import com.eglantine.sql.PostgresqlAccess;

/**
 *
 * @author postgresqltutorial.com
 */
public class PostgresqlUsers {


	static public String[] getUserRights(String login, String password) {
		
/*		
		
		String SQL = "SELECT * FROM getuserrights(?,?)";
*/
		String SQL = "SELECT password,iv,rights FROM users WHERE login = ?";

		String[] result = null;

		try (
				Connection conn = PostgresqlAccess.connect();
				PreparedStatement pstmt = conn.prepareStatement(SQL)) {

			pstmt.setString(1,login);

			ResultSet rs = pstmt.executeQuery();
			int rcount = 0;
			while(rs.next()) {
				rcount++;
				String pwd = rs.getString("password");
				String iv = rs.getString("iv");
				String[] rights = (String[])((PgArray)rs.getObject("rights")).getArray();
				String dpwd = Secret.stringDecrypt(pwd, iv, 0, 0);
				if(password.equals(dpwd)) {
					result = rights;
				}
			}
			if(rcount != 1) {
				result = null;
			}
			conn.close();
		} catch (SQLException e) {
			//            System.out.println(e.getMessage());
			result = null;
		}
		return result;
	}

	public static String getUserEmail(String login) {
		String SQL = "SELECT email FROM users WHERE login = ?";

		String result = null;

		try (
				Connection conn = PostgresqlAccess.connect();
				PreparedStatement pstmt = conn.prepareStatement(SQL)) {

			pstmt.setString(1,login);

			ResultSet rs = pstmt.executeQuery();
			int rcount = 0;
			while(rs.next()) {
				rcount++;
				Object o = rs.getObject(1);
				if (o instanceof String) {
					result = (String)o;
				}
			}
			if(rcount != 1) {
				result = null;
			}
			conn.close();
		} catch (SQLException e) {
			//            System.out.println(e.getMessage());
			result = null;
		}
		return result;
	}
}