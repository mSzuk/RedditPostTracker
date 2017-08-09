import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.microsoft.sqlserver.jdbc.*;


/**
 * DatabaseHandler is class which provides methods to connect
 * with MSSQL database, alter, write or read from it.
 * 
 * This class requires AppConfiguration class to function, as
 * username and password for MSSQL user are stored in XML file
 * and read when needed (no hard coding).
 * 
 * Requires: Microsoft JDBC Driver for SQL Server
 * 
 * @ author Marek Szukalski
 */

public class DatabaseHandler {
	//private final String url = "jdbc:sqlserver://localhost:1433";
	//private final String drive = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	SQLServerDataSource sqlDS;				// MSSQL data source object used for connection
	private String username = "guest";		//default user
	private String password = "";
	
	/**
	 * Databasehandler constructor, which also reads and sets 
	 * database user credendials from file
	 */
	public DatabaseHandler() {
		sqlDS = new SQLServerDataSource();
		AppConfiguration dbConf = new AppConfiguration();	
		sqlDS.setUser(dbConf.getDBUser());				
		sqlDS.setPassword(dbConf.getDBPassword());	
	}
	
	/**
	 *  
	 * @param userName	database user login
	 * @param pass		database user password
	 */
	public void setUser(String userName, String pass){
		username = userName;
		password = pass;
		sqlDS.setUser(this.username);
		sqlDS.setPassword(this.password);
	}
	
	// Function to check if the table for subreddit is set.
	/**
	 * Method to check if table exists in database
	 * 
	 * @param dbName		name of database to which application connects		
	 * @param tableName		name of table to be checked
	 * @return				<code>true</code> if table of given name already exists
	 * 						<code>false</code> when no table of given name was found
	 * @throws SQLException		
	 */
	public boolean TableCheck(String dbName, String tableName) throws SQLException{
		boolean tableState = false;
		Connection conn = null;
		Statement stat = null; 
		
		try {
			sqlDS.setDatabaseName(dbName);
			conn = sqlDS.getConnection();
			stat = conn.createStatement();
			
			DatabaseMetaData data = conn.getMetaData();
			ResultSet table = data.getTables(null, null, tableName, null);
			if (table.next()) {
				String name = table.getString("TABLE_NAME");
				if (name != null && name.equals(tableName))
					tableState=true;
			}
			else
				tableState=false;
		}catch(SQLException se) {
			throw se;
		}
		finally {
			try {
				if(stat!=null)
					stat.close();
				if(conn!=null)
					conn.close();
			}
			catch(SQLException cleanupException) {
				cleanupException.printStackTrace();
			}
		}
		return tableState;
	}
	
	/**
	 * Method creates table of given name in database. Before creation,
	 * table name is checked with reddit naming policy (which also prevents
	 * SQL injections)
	 * 	
	 * @param dbName			name of database
	 * @param tableName			name of table to be created
	 * @throws SQLException		
	 */
	public void createTable(String dbName, String tableName) throws SQLException {
		Connection conn = null;
		Statement stat = null;
		boolean tableNameValid = false;
		String pattern = "\\A[A-Za-z0-9_]{1,20}\\Z";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(tableName);
		tableNameValid = m.matches();
		
		if (tableNameValid) {
			try{
				sqlDS.setDatabaseName(dbName);
				conn = sqlDS.getConnection();
				stat = conn.createStatement();
				String sql = "CREATE TABLE " + tableName + 
						"(title NVARCHAR(MAX), " +
						"link NVARCHAR(MAX), " +
						"date DATE);";		
				stat.executeUpdate(sql);		
			}
			catch(SQLException se) {
				throw se;
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			finally{
				try{
					if(stat!=null)
						stat.close();
			        if(conn!=null)
			        	conn.close();
			    }
				catch(SQLException cleanupException){
					cleanupException.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Method for writing new data into table. Operation is done
	 * as one transaction and rolled back in case of errors.
	 * 
	 * @param titles			list of Strings containing titles of posts to be inserted
	 * @param links				list of String containing hyper links to posts 
	 * @param dbName			name of database
	 * @param tableName			name of table to be inserted into
	 * @throws SQLException
	 */
	public void writetoDB(ArrayList<String> titles, ArrayList<String> links, String dbName, String tableName)
	throws SQLException {	
		Connection conn = null;
		Statement stat = null; 
		
		try{
			sqlDS.setDatabaseName(dbName);
			conn = sqlDS.getConnection();
			stat = conn.createStatement();
			conn.setAutoCommit(false);
			
			Date myDate = new Date(System.currentTimeMillis());
			int elementCount = (titles.size() > links.size())? titles.size() : links.size();
			for(int rekord = 0; rekord < elementCount; rekord++) {
				String sql = "INSERT INTO " + tableName + " VALUES (?,?,?);";
				PreparedStatement sqlstatement = conn.prepareStatement(sql);
				sqlstatement.setNString(1, titles.get(rekord));
				sqlstatement.setNString(2, links.get(rekord));
				sqlstatement.setDate(3, myDate);
				sqlstatement.executeUpdate();	
			}
			conn.commit();
		}
		catch(SQLException blockException) {
			try {
				conn.rollback();
			}
			catch (SQLException rollbackException) {
				rollbackException.printStackTrace();
			}
			throw blockException;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
		      if(stat != null)
		    	  stat.close();
		      if(conn != null) {
		    	  conn.setAutoCommit(true);
		          conn.close();
		      }
			}
			catch(SQLException cleanupException) {
				cleanupException.printStackTrace();
			}
		}
	}	
	
	/**
	 * Method for reading from data not older than 1 day from 
	 * specified table. 
	 * @param dbName		name of database
	 * @param tableName		name of table to read from
	 * @return				ArrayList containing ArrayLists of Strings with titles 
	 * 						of posts and hyper links to posts 
	 * @throws SQLException
	 */
	public ArrayList<ArrayList<String>> readfromDB(String dbName, String tableName) throws SQLException {
		Connection conn = null;
		Statement stat = null;
		ResultSet table = null;
		ArrayList<String> ptitle = new ArrayList<String>();
		ArrayList<String> plinks = new ArrayList<String>();
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		
		try {
			//sqlDS.setServerName("KAISERBLEID");
			//sqlDS.setPortNumber(1433);
			sqlDS.setDatabaseName(dbName);	
			conn = sqlDS.getConnection();
			stat = conn.createStatement();
			
			String sql = "SELECT title, link FROM " + tableName +
					" WHERE date > DATEADD(day, -1, GETDATE());";
			table = stat.executeQuery(sql);
			
			while(table.next()) {
				String tytul = table.getString("title");
				String alink = table.getString("link");
				ptitle.add(tytul);
				plinks.add(alink);
			}
			result.add(ptitle);
			result.add(plinks);
			table.close();
		}
		catch(SQLException se) {
			//se.printStackTrace();
			throw se;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally{
			try{
				if(stat!=null)
					stat.close();
		        if(conn!=null)
		        	conn.close();
		    }
			catch(SQLException cleanupException){
				cleanupException.printStackTrace();
			}
		}				
		return result;
	}
	
	/*public void updateDB(ArrayList<String> tytuly, ArrayList<String> linki, String dbName, String tableName)
	throws SQLException {	
		Connection conn = null;
		Statement stat = null; 
		
		try{		
			//sqlDS.setUser(this.username);
			//sqlDS.setPassword(this.password);
			//sqlDS.setServerName("KAISERBLEID");
			//sqlDS.setPortNumber(1433);
			sqlDS.setDatabaseName(dbName);	
			conn = sqlDS.getConnection();
			stat = conn.createStatement();
			
			// turn on autocommit to start transaction
			conn.setAutoCommit(false);
			
			for(int rekord = 1; rekord<tytuly.size(); rekord++) {
				String sql = "UPDATE " + tableName + " SET "+ 
						"title = ? , link= ? WHERE id= ?;";
				PreparedStatement sqlstatement = conn.prepareStatement(sql);
				sqlstatement.setString(1, tytuly.get(rekord-1));
				sqlstatement.setString(2, linki.get(rekord-1));
				sqlstatement.setInt(3, rekord);
				sqlstatement.executeUpdate();		
			}	
			conn.commit();   	//commit transaction
		}
		catch(SQLException blockException) {
			//blockException.printStackTrace();
			try {
				conn.rollback();
			}
			catch (SQLException rollbackException) {
				rollbackException.printStackTrace();
			}
			throw blockException;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
		      if(stat != null)
		    	  stat.close();
		      if(conn != null) {
		    	  conn.setAutoCommit(true);
		          conn.close();
		      }
			}
			catch(SQLException cleanupException) {
				cleanupException.printStackTrace();
			}
		}
	}*/
	
	/*public void createDatabase(String databaseName) throws SQLException {
		Connection conn = null;
		Statement stat = null; 
		try {
			sqlDS.setDatabaseName(databaseName);
			conn = sqlDS.getConnection();
			stat = conn.createStatement();
			
			String sql = "CREATE DATABASE " + databaseName + ";" ;
			stat.executeUpdate(sql);
		}
		catch(SQLException se){
			//se.printStackTrace();
			throw se;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{
				if(stat!=null)
					stat.close();
				if(conn!=null)
					conn.close();
		    }
			catch(SQLException cleanupException) {
				cleanupException.printStackTrace();
		    }
	   }
	}*/
	
	/*public void deleteTable(String dbName, String tableName) throws SQLException {
		Connection conn = null;
		Statement stat = null;
		
		try{
			sqlDS.setDatabaseName(dbName);
			conn = sqlDS.getConnection();
			stat = conn.createStatement();
			String sql = "DROP TABLE " + tableName + ";";
			stat.executeUpdate(sql);		
		}
		catch(SQLException se){
			//se.printStackTrace();
			throw se;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally {
			try {
				if(stat!=null)
					stat.close();
		        if(conn!=null)
		            conn.close();
		    }
			catch(SQLException se) {
		         se.printStackTrace();
		    }
		}
	}*/
	
	/*public void truncateTable(String dbName, String tableName) throws SQLException{
		Connection conn = null;
		Statement stat = null;
		
		try{
			sqlDS.setDatabaseName(dbName);
			conn = sqlDS.getConnection();
			stat = conn.createStatement();
			String sql = "TRUNCATE TABLE " + tableName + ";";
			stat.executeUpdate(sql);
		}
		catch(SQLException se){
			//se.printStackTrace();
			throw se;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally {
			try {
				if(stat!=null)
					stat.close();
		        if(conn!=null)
		            conn.close();
		    }
			catch(SQLException se) {
		         se.printStackTrace();
		    }
		}
	}*/
}
