import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.lang.System;

/**
 * TaskManager is class which cyclicly connects to reddit and
 * searches for new data on subreddits. Any new data is then
 * send to database and post list.
 * 
 * @author Marek Szukalski
 * 
 */

public class TaskManager{
	private String dbName;
	private WindowClass guiObject;
	private final int initialDelay = 5; 
	private int checkPeriod;
	private int taskCounter;
	private final ScheduledExecutorService taskPicker = Executors.newScheduledThreadPool(1);
	
	public TaskManager(WindowClass wObj, int delay) { 
		guiObject = wObj;			// copy main frame object reference
		taskCounter = 0;			// set task counter
		checkPeriod = delay;		// set delay between checks
		dbName = wObj.getConfiguration().getDatabaseName();
		// activate task picker to periodically check subreddits
		taskPicker.scheduleWithFixedDelay(new Runnable() {
		//taskPicker.schedule(new Runnable() {
			@Override
			public void run() {
				String subredditName;
				try {
					if (guiObject.getSubredditsCount() > 0) {
						subredditName = guiObject.getSubredditsNames(taskCounter);
						PostObtainer backgroundWorker = new PostObtainer(subredditName);
						backgroundWorker.execute();
					}
				}
				catch(IndexOutOfBoundsException err) {
					err.printStackTrace();
				}	
			}
		//}, checkPeriod, TimeUnit.SECONDS);
		}, initialDelay, checkPeriod, TimeUnit.SECONDS); 
	}
	
	/**
	 * PostObtainer is private class extending SwingWorker to do background
	 * task of obtaining new data, updating database and Swing Object holding
	 * list of posts.
	 * 
	 * @author Marek
	 *
	 */
	private class PostObtainer extends SwingWorker<ArrayList<ArrayList<String>>, Void> {
		private String errorMessage;
		private int errorCode;
		private String subredName;
		
		public PostObtainer(String name) {
			errorCode = 0;		// set error code to noError
			subredName = name;
		}
		
		@Override
		public ArrayList<ArrayList<String>> doInBackground(){
			ArrayList<ArrayList<String>> redditPosts = new ArrayList<ArrayList<String>>();
			ArrayList<ArrayList<String>> dbPosts = new ArrayList<ArrayList<String>>();
			PostScraper newScraper = new PostScraper();
			redditPosts = newScraper.getRedditPosts(subredName);
			DatabaseHandler dbObj = new DatabaseHandler();

			try {
				dbPosts = dbObj.readfromDB(dbName, subredName);
			}
			catch (SQLException se) {
				this.errorCode = 1001;
				this.errorMessage = se.getMessage();
			}
			
			if (errorCode == 0) {	// no error in db reading so proceed
				// create collection of old posts and links and remove them from new list
				Collection<String> oldPosts = new ArrayList<String>(dbPosts.get(0));
				Collection<String> oldLinks = new ArrayList<String>(dbPosts.get(1));			
				redditPosts.get(0).removeAll(oldPosts);
				redditPosts.get(1).removeAll(oldLinks);
				
				boolean newDataReady = ((redditPosts.get(0).size() == redditPosts.get(1).size()) && redditPosts.get(0).size() > 0)? true : false;
				if (newDataReady) {
					try {
						dbObj.writetoDB(redditPosts.get(0), redditPosts.get(1), dbName, subredName);
					}
					catch(SQLException writeErr) {
						this.errorCode = 1002;
						this.errorMessage = writeErr.getMessage();
					}
				}
			}	
			return redditPosts;
		}
		
		//@Override
		public void done() {
			//ArrayList<ArrayList<String>> posts = new ArrayList<ArrayList<String>>();
			int count = guiObject.getSubredditsCount();
			if (errorCode == 0) {	// no error in background task, can proceed to update list
				try {
					final ArrayList<ArrayList<String>> posts = get();
					final int updateCounter = taskCounter;
					System.out.print("Worker for reddit " + subredName + " done\n");
					if (posts.get(0).size() > 0) {
						System.out.print("Liczba nowych postów: " + (posts.get(0).size()) + "\n");	
						/*for(String x : posts.get(0)) {
							System.out.println(x);
						}*/
						SwingUtilities.invokeLater(new Runnable() {
						    public void run() {				    	
						    	guiObject.updateList(posts, updateCounter);
						    }
						});
					}
				}
				catch(InterruptedException e) {
					System.out.print("interrupt error");
				}
				catch(ExecutionException ex){
					System.out.print("execution error");
				}
			}
			else {		// error during execution, show error code and message, printed out for test, in future put it to status bar
				System.out.print("Error Code: " + this.errorCode + ", Message: " + this.errorMessage + "\n");
				
			}
			if (taskCounter < (count - 1))
				taskCounter++;
			else
				taskCounter = 0;
		}
	};
}
