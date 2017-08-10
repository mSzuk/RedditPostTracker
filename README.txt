Reddit Post Tracker Project
=============================
Reddit post tracker is an application which allows to track 
subpages of reddit.com page (known as subreddits) in search 
of new posts in hot section. The goals of this program are:
	* Allow user to input subreddits which will be tracked
	* Search for new posts and show them to user in list form
	* Allow user to open links to posts in his default browser
	from within application
	
Note: The purpose of this application was for author to learn
and try out few things. Code may contain bad designs, weird
constructions and is not optimized.   

Project Status
-----------------------------
Application is in alfa stage with only few features implemented.
Current restrictions are:
	* only last 100 posts on subpage will be included (mechanism
	of searching till finding post which is also last entry in 
	database not implemented)
	* on application start list is initially loaded with posts
	not older than 1 day (data loaded from database)
	* only one item on post list can be interacted with at once


Prerequisites
-----------------------------
- Alfa version uses local sql server database as means of data
storage and requires:
	* Microsoft SQL Server 
	* Microsoft JDBC Driver for SQL Server (tested with 4.0 version)
	* User with appropriate permissions (see dbDataComment.txt)
	* dbData.xml file with "dbuser" and "dbpass" entry keys
- JRE (Java Runtime Environment) version 5 or higher
	
	
Author
-----------------------------
Marek Szukalski
