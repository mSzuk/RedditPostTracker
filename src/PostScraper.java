import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * PostScraper is class allow user to interact with reddit
 * pages and scrap data from it, such as new posts or if 
 * subreddit of given name exist.
 * 
 * Requires: JSoup library (https://jsoup.org)
 * 
 * @author Marek Szukalski
 *
 */

/*
 * TODO: 
 * 		- Change methods to static, so class could be used without creating an object,
 * 		- Change user agent to appropriate web browser string
 */
public class PostScraper {
	private static final String USER_AGENT = "Tenczowy";
	
	public PostScraper() {	
	}
	
	/**
	 * Methor for searching trough subreddit page and scraping data
	 * about posts and links to posts
	 * 
	 * @param subredditName		name of subreddit to be searched trough
	 * @return					ArrayList containing ArrayList of Strings with post titles and links
	 */
	public ArrayList<ArrayList<String>> getRedditPosts(String subredditName) {
		ArrayList<ArrayList<String>> dataArray = new ArrayList<ArrayList<String>>();
		try {	
			Document doc = Jsoup.connect("https://www.reddit.com/r/" + subredditName + ".xml?limit=100")
					.userAgent(USER_AGENT).get();
			
			//Elements posts = doc.select("p.title > a");
			//Elements links = doc.select("li.first > a");
			Elements posts = doc.select("entry > title");
			Elements links = doc.select("entry > link");
			
			// get links and posts titles as lists of Strings
			ArrayList<String> titles = new ArrayList<>();
			ArrayList<String> hlinks = new ArrayList<>();
	
			for (Element link : posts)
			{
				titles.add(link.ownText());
			}
			for (Element link : links)
			{
				hlinks.add(link.attr("href"));
			}
			Collections.reverse(titles);
			Collections.reverse(hlinks);
			dataArray.add(titles);
			dataArray.add(hlinks);
		}
		catch(IOException ex){
			System.out.print(ex.getMessage());
		}
		
		return dataArray;
	}
	
	/**
	 * Static method for checking if given subreddit page exists
	 * 
	 * @param subredditName		subreddit page to be checked
	 * @return					<code>true<code/> if subreddit page exists
	 * 							<code>false</code> otherwise
	 */
	public static boolean subredditExist(String subredditName) {
		boolean result = false;
		try {
			Document doc = Jsoup.connect("https://www.reddit.com/r/" + subredditName).userAgent(USER_AGENT).get();
			Element check = doc.getElementById("noresults");
			if (check == null)
				result = true;
		}
		catch (IOException err) {
			System.out.print(err.getMessage());
		}
		return result;
	}
}
