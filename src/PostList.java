import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.JPopupMenu;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * PostList is class which displays held posts on specified
 * subreddit as list. It also implements pop-up menu for interacting
 * with items on list.
 * 
 * @author Marek Szukalski
 *
 */
public class PostList implements MouseListener, ActionListener
{
	private JList<String> titles;
	private JList<String> links;
	private String redditName;
	private DefaultListModel<String> titlesModel;
	private DefaultListModel<String> linksModel;
	final private JPopupMenu popMenu = new JPopupMenu();
	
	/**
	 * Default constructor for PostList object
	 * 
	 * @param redName	identification name of subreddit
	 */
	public PostList(String redName){
		redditName = redName;
		titlesModel = new DefaultListModel<String>();
		linksModel = new DefaultListModel<String>();
		titles = new JList<String>(titlesModel);
		links = new JList<String>(linksModel);
		
		titles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		titles.setLayoutOrientation(JList.VERTICAL);
		
		JMenuItem openLink = new JMenuItem("Open in browser");
		JMenuItem removeLink = new JMenuItem("Remove from list");
		popMenu.add(openLink);
		popMenu.add(removeLink);
		openLink.addActionListener(this);
		removeLink.addActionListener(this);
	}
	
	/**
	 * Adds new items at the beginning of the list.
	 * 
	 * @param title		list of strings with new items to be added
	 * @param link		list of strings with link to posts
	 * @return			<code>0</code> if successfully added
	 * 					<code><3</> if input lists sizes are different,
	 * 					meaning missing data
	 */
	public int addRecord(ArrayList<String> title, ArrayList<String> link){
		int errorCode = 0;
		if (title.size() != link.size()) {
			errorCode = 3;
		}
		else {
			int count = title.size();
			for (int i = 0; i < count; i++) {
				titlesModel.add(0, title.get(i));
				linksModel.add(0, link.get(i));
				//titlesModel.add(0, title.get(count - i - 1));
				//linksModel.add(0, link.get(count - i - 1));
			}
		}	
		return errorCode;
	}
	
	public JList<String> getTitles() {
		return this.titles;
	}
	
	public JList<String> getLinks() {
		return this.links;
	}
	
	/**
	 * Method of returning data held by PostList object
	 * 
	 * @return ArrayList of JLists holding titles and links to posts
	 */
	public ArrayList<JList<String>> getPostsData() {
		ArrayList<JList<String>> result = new ArrayList<JList<String>>();
		result.add(this.titles);
		result.add(this.links);
		return result;
	} 
	
	public String getRedditName() {
		return this.redditName;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		String name = ((JMenuItem)source).getActionCommand();
		int index = titles.getSelectedIndex();

		if(name == "Open in browser") {
			String linkStr = linksModel.get(index);
			try {
				// todo: add check if Desktop.getDesktop is available on user platform
				//		 consider other method of building URI instead using toURI (will prevent platform differences - read somewhere on stackoverflow)
				URL link = new URL(linkStr);
				Desktop defBrowser = Desktop.getDesktop();
				try {
					defBrowser.browse(link.toURI());
				}
				catch(Exception err) {
					// todo: add exception handling
					err.printStackTrace();
				}
						
			}
			catch(MalformedURLException err) {
				// todo: add exception handling
				err.printStackTrace();
			}
		}
		else if (name == "Remove from list") {
			titlesModel.remove(index);
			linksModel.remove(index);
		}
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		//if (e.isPopupTrigger()) {
			//postMenu dialog = new postMenu();
		if (SwingUtilities.isRightMouseButton(e)) {
			int index = titles.locationToIndex(e.getPoint());
			titles.setSelectedIndex(index);
			popMenu.show(titles, e.getX(), e.getY());	
		}
		//}	
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub	
	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub	
	}
	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub	
	}
}
