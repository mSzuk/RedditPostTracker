import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ArrayList;

/**
 * AppConfiguration is a class for loading application configurations
 * and database user information from files.
 * 
 * @author Marek Szukalski
 *
 */
public class AppConfiguration {
	private Properties configurationFile;
	private ArrayList<String> subreddits;
	private String dbInstance;
	private String dbName;
	
	public AppConfiguration() {
		configurationFile = new Properties();
		subreddits = new ArrayList<String>();
		dbInstance = new String();
		dbName = new String();
	}
	
	public boolean setRedditList(ArrayList<String> list) {
		boolean result = true;
		if(list.isEmpty())
			result = false;
		else {
			subreddits = new ArrayList<String>(list);
		}
		return result;
	}
	
	public void setServerName(String instanceName) {
		dbInstance = instanceName;
	}
	
	public void setDatabaseName(String databaseName) {
		dbName = databaseName;
	}
	
	public ArrayList<String> getRedditList() {
		return subreddits;
	}
	
	public String getServerName() {
		return dbInstance;
	}
	
	public String getDatabaseName() {
		return dbName;
	}
	
	public String getDBUser() {
		String user = "";
		try {	
			File conf = new File("dbData.xml");
			FileInputStream fStream = new FileInputStream(conf);
			configurationFile.loadFromXML(fStream);
			user = configurationFile.getProperty("dbuser");
			fStream.close();
		}
		catch(FileNotFoundException error) {
			System.out.print("Error - file with database credentials not found.");
		}
		catch(IOException loadError) {
			System.out.print("Error - loading file content failed.");
		}
		return user;
	}
	
	public String getDBPassword() {
		String password = "";
		try {	
			File conf = new File("dbData.xml");
			FileInputStream fStream = new FileInputStream(conf);
			configurationFile.loadFromXML(fStream);
			fStream.close();
			password = configurationFile.getProperty("dbpass");
		}
		catch(FileNotFoundException error) {
			System.out.print("Error - file with database credentials not found.");
		}
		catch(IOException loadError) {
			System.out.print("Error - loading file content failed.");
		}
		return password;
	}
	
	public boolean saveConfiguration() {
		boolean result = true;
		FileOutputStream confFile;
		for (int i = 0; i < subreddits.size(); i++)
			configurationFile.setProperty("subreddit" + i, subreddits.get(i));	
		configurationFile.setProperty("dbinstance", dbInstance);
		configurationFile.setProperty("dbname", dbName);

		try {
			confFile = new FileOutputStream("config.xml");
			configurationFile.storeToXML(confFile, "ConfigurationFile - reddit crawler");
			confFile.close();
		}catch(FileNotFoundException error) {
			System.out.print("Error creating config file.");
			result = false;
		}
		catch(IOException saveError) {
			System.out.print("Error saving configuration file.");
			result = false;
		}
		
		return result;
	}
	
	public boolean loadConfiguration() {
		boolean result = true;
		File conf = new File("config.xml");
		try {
			FileInputStream fStream = new FileInputStream(conf);
			configurationFile.loadFromXML(fStream);
			fStream.close();
			
			Enumeration<Object> properties = configurationFile.keys(); //propertyNames();
			while (properties.hasMoreElements()) {
				String keyName = (String)properties.nextElement();
				if (keyName.contains("subreddit")) {
					String subName = configurationFile.getProperty(keyName);
					subreddits.add(subName);
				}
				else if(keyName.contains("dbinstance"))
					dbInstance = configurationFile.getProperty(keyName);
				else if (keyName.contains("dbname"))
					dbName = configurationFile.getProperty(keyName);
			}
			
			
			if (subreddits.isEmpty()) 
				System.out.print("No subreddits saved.");
			else {
				for (String string : subreddits) {
					System.out.println(string);
				}
			}	
		}
		catch(FileNotFoundException error) {
			error.printStackTrace();
			result = false;
		}
		catch(IOException IOerror) {
			IOerror.printStackTrace();
			result = false;
		}
		
		return result;
	}
}
