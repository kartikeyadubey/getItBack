User Interface Lab
Assignment #4: README file

by Kartikeya Dubey
on November 29, 2011

For the term project I created an application that enables users to track 
shared expenses with friends. It uses the facebook api and AChartEngine
to create a list of friends and plot graphs.

Files are distributed according to packages:
Description of important files

	ssui.project:   Contains the core of the application
			
			StartPage: Starts the application, checks whether the user
			is signed in or not and based on that it lets them login, 
			enter application or logout
			
			TabDisplay: Initializes the 3-tab display

			AddBill: User can add bills here, does an HTTP GET to retrieve
			data to populate the friends list. Submits bills using HTTP POST
			
			ViewList: Base class to view bills/returns
	
			ViewBills: View who owes you here. Uses an Async task
			to update UI so there is no lag, uses a ListView. Implements
			lazy loading of images. Has a button for users to view a plot
			of who owes them

			ViewReturns: View who you owe here. Uses an Async task
			to update UI so there is no lag, uses a ListView. Implements
			lazy loading of images. Has a button for users to view a plot
			of who they owe
			
	session:  Stores information about the current user session
	facebook: Contains all the files needed to use the facebook api
	charting: Contains all the files needed to make the pie chart
	
Problems I faced:
Charting API: The programmer needs to generate colors for the pie chart and adjust the size so it
			  fits the screen, this was unexpected as I thought the API would automatically take care of it.
Facebook API: If the user has two facebook sessions open and signs out of one then their access token
			  is invalidated, there are many cases where this needed to be checked otherwise the application
			  might crash
			  
Sources: 
http://code.google.com/p/achartengine/
http://codehenge.net/blog/2011/06/android-development-tutorial-asynchronous-lazy-loading-and-caching-of-listview-images/
http://www.helloandroid.com/tutorials/connecting-mysql-database
Android Developer