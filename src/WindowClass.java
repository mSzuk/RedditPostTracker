import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JButton;
//import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

/**
 * WindowClass is responsible for preparing user interface
 * and interacting with user.
 *  
 * @author Marek Szukalski
 *
 */


/*
 * TODO:
 * 
 */

public class WindowClass implements ActionListener{	
	private int height=480;
	private int width=640;
	
	private JFrame mainWindow;
	private JPanel panelLeft, panelRight, panelTop;
	private ArrayList<PostList> postsList = new ArrayList<PostList>();
	
	protected JButton button1, button2, AddTab;
	protected JTabbedPane tabPanel;
	
	private JTextField dbInstanceNameField;
	private JTextField serverNameField;
	private String dbName;
	private String serverName;
	AppConfiguration config;

	
	public void setRes(int h, int w){
		height=h;
		width=w;
	}
	
	public WindowClass(){
		prepareGUI();
		config = new AppConfiguration();
		if(config.loadConfiguration()) {
			serverName = config.getServerName();
			dbName = config.getDatabaseName();
			serverNameField.setText(serverName);
			dbInstanceNameField.setText(dbName);
			
			Set<String> s = new LinkedHashSet<>(config.getRedditList());	// convert to Set to remove duplicates, in case someone edits config file
			ArrayList<String> tempList = new ArrayList<String>(s);
			for (int i = 0; i < tempList.size(); i++)
				addNewtab(tempList.get(i));
			tabPanel.setSelectedIndex(0);
		}		
	}
	public AppConfiguration getConfiguration() {
		return config;
	}
	
	public void prepareGUI(){
		mainWindow = new JFrame();
		Toolkit tool = mainWindow.getToolkit();
		Dimension sizes = tool.getScreenSize();
		
		//Main Window configuration
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setBounds(sizes.width/4, sizes.height/4, width, height);
		mainWindow.setLayout(new BorderLayout());
		mainWindow.setTitle("Reddit post tracker");
		
		//top panel (can be used for menu)
		panelTop = new JPanel();
		
		//left panel
		panelLeft = new JPanel();
		panelLeft.setLayout(new BoxLayout(panelLeft, BoxLayout.Y_AXIS));
		panelLeft.setPreferredSize(new Dimension((int)(width*0.25), (int)(height*0.8)));
		panelLeft.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		
		//right panel - subreddits tabs and link lists
		panelRight = new JPanel();
		panelRight.setLayout(new BorderLayout());
		panelRight.setPreferredSize(new Dimension((int)(width*0.75), (int)(height*0.8)));
		
		tabPanel = new JTabbedPane(JTabbedPane.TOP);
		JPanel addPanel = new JPanel(new BorderLayout());
		tabPanel.addTab("+", addPanel);
		AddTab = new JButton("+");
		AddTab.setBorderPainted(false);
		AddTab.setContentAreaFilled(false);
		addPanel.add(AddTab);
		tabPanel.setTabComponentAt(0, AddTab);
	
		AddTab.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {		
				String subredditName = JOptionPane.showInputDialog("Enter subreddit name:");
				if (subredditName != null) {				// check if name was confirmed on canceled
					boolean tabExist = false;
					for(PostList x : postsList) {			// check for duplicate
						if (x.getRedditName().equals(subredditName))
							tabExist = true;
					}
					if(tabExist)
						JOptionPane.showMessageDialog(null, "Subreddit o podanej nazwie zosta³ ju¿ dodany.");
					else {
						boolean result = addNewtab(subredditName);			
						if(!result)
							JOptionPane.showMessageDialog(null, "Subreddit o podanej nazwie nie istnieje.");
					}
				}
			}
		});
		
		button1 = new JButton("Add subreddit");
		button2 = new JButton("Remove subreddit");
		button1.addActionListener(this);
		button2.addActionListener(this);
		
		JPanel tabManage = new JPanel(new FlowLayout());
		tabManage.add(button1);
		tabManage.add(button2);
		
		JLabel serverLabel = new JLabel("Server Name:");
		JLabel dbLabel = new JLabel("Database Name:");
		serverNameField = new JTextField();
		serverNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, serverNameField.getPreferredSize().height) );
		dbInstanceNameField = new JTextField();
		dbInstanceNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, dbInstanceNameField.getPreferredSize().height) );
		
		panelLeft.add(serverLabel);
		panelLeft.add(serverNameField);
		panelLeft.add(dbLabel);
		panelLeft.add(dbInstanceNameField);
			
		panelRight.add(tabPanel, BorderLayout.CENTER);
		panelRight.add(tabManage, BorderLayout.SOUTH);
		
		mainWindow.getContentPane().add(panelLeft, BorderLayout.WEST);
		mainWindow.getContentPane().add(panelRight, BorderLayout.CENTER);
		mainWindow.pack();
		mainWindow.setVisible(true);
		
		// on main window close event, in the future may be replaced with shutdown hook
		// todo: read what is happening with running threads on app closing, check if any sort of wait for execution end is needed
		mainWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				AppConfiguration saveData = new AppConfiguration();
				ArrayList<String> subs = new ArrayList<String>();
				for (int i = 0; i < postsList.size(); i++)
					subs.add(postsList.get(i).getRedditName());
				saveData.setRedditList(subs);
				String sName = serverNameField.getText();
				saveData.setServerName(sName);
				String dbName = dbInstanceNameField.getText();
				saveData.setDatabaseName(dbName);
				saveData.saveConfiguration();
			}
		});
	}
	
	public void actionPerformed(ActionEvent a){
		if(a.getSource()==button1){		
			String subredditName = JOptionPane.showInputDialog("Enter subreddit name:");
			if (subredditName != null) {				// check if name was confirmed on canceled
				boolean tabExist = false;
				for(PostList x : postsList) {			// check for duplicate
					if (x.getRedditName().equals(subredditName))
						tabExist = true;
				}
				if(tabExist)
					JOptionPane.showMessageDialog(null, "Subreddit o podanej nazwie zosta³ ju¿ dodany.");
				else {
					boolean result = addNewtab(subredditName);			
					if(!result)
						JOptionPane.showMessageDialog(null, "Subreddit o podanej nazwie nie istnieje.");
				}
			}
		}
		if(a.getSource()==button2){
			if(tabPanel.getComponentCount()>1){
				int index = tabPanel.getSelectedIndex();
				tabPanel.removeTabAt(index);
				postsList.remove(index);
			}
		}	
	}
	
	public Dimension getmainWindowresolution(){
		return mainWindow.getSize();
	}
	public void setmainWindowresulution(Dimension d){
		height = d.height;
		width = d.width;
	}
	public String getSubredditsNames(int index) throws IndexOutOfBoundsException {
		if (index > postsList.size())
			throw new IndexOutOfBoundsException();
		return postsList.get(index).getRedditName();
	}
	public int getSubredditsCount() {
		return postsList.size();
	}
	public PostList getPostsList(int index) {	// TODO: add indexOutofBoundException and handling
		return postsList.get(index);
	}
	
	public boolean addNewtab(String subredditName) {
		JPanel newTab = new JPanel();
		boolean subExist = PostScraper.subredditExist(subredditName);
		if(subExist == true) {	
			int tabCnt = tabPanel.getTabCount();
			tabPanel.insertTab(subredditName, null, newTab, null, (tabCnt-1));
			tabPanel.setSelectedIndex((tabCnt-1));
			newTab.setLayout(new BorderLayout());				
			
			// get post from database, TODO: add try-catch block 
			DatabaseHandler dbWorker = new DatabaseHandler();
			try {
				boolean tableExist = dbWorker.TableCheck(dbName, subredditName);
				if (tableExist == false)
					dbWorker.createTable(dbName, subredditName);	
			}
			catch(SQLException se) {
				se.printStackTrace();
			}
				
			ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
			try {
				list = dbWorker.readfromDB(dbName, subredditName);
			}
			catch(SQLException se) {
				System.out.print("Error connecting to database or table\n");
			}
			
			PostList linkList = new PostList(subredditName);
			if (list.isEmpty() == false) {
				linkList.addRecord(list.get(0), list.get(1));
				linkList.getTitles().setSelectedIndex(0);
			}
			linkList.getTitles().addMouseListener(linkList);
			JScrollPane scrollpane = new JScrollPane(linkList.getTitles());
			newTab.add(scrollpane);
			postsList.add(linkList);
		}
		return subExist;
	}
	
	public void updateList(ArrayList<ArrayList<String>> newData, int index) {
		postsList.get(index).addRecord(newData.get(0), newData.get(1));
	}

}
