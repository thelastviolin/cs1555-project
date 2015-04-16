import java.util.Scanner;
import java.sql.*;
import java.text.ParseException;
import oracle.jdbc.driver.OracleDriver;

public class Ebay {

	private static Connection connection; //used to hold the jdbc connection to the DB
    private boolean adminLogin;
    private boolean timeToQuit;


	public static void main(String[] args) {
		boolean timeToQuit;
		String username, password;
		username = "zhw28"; //This is your username in oracle
		password = "3665394"; //This is your password in oracle

		try {
			// Register the oracle driver.  
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

			//This is the location of the database.  This is the database in oracle
			//provided to the class
			String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass"; 

			//create a connection to DB on class3.cs.pitt.edu
			connection = DriverManager.getConnection(url, username, password);
			Ebay auctionHouse = new Ebay();

			String login = auctionHouse.handleLogin();
			while (!auctionHouse.timeToQuit) {
				auctionHouse.topKCategoriesRoot(0, 2);
				auctionHouse.timeToQuit = true;
			}
			System.out.println("\nGoodbye!");

			connection.close();
		}
		catch(Exception Ex)  {
			System.out.println("Error connecting to database.  Machine Error: " + Ex.toString());
		}
	}

	// Constructor class
	public Ebay() {
		timeToQuit = false;
		adminLogin = false;
	}

	// Returns the 'login' of the current user
	public String handleLogin() {
		int option;
		boolean exitLoop, isAdmin;
		String login = "";
		Scanner scan = new Scanner(System.in);
		isAdmin = false;
		exitLoop = false;
		System.out.print("Welcome to the Auction House!\n");
		while (!exitLoop) {
			System.out.print("1. Login\n" + "2. Exit\n\n" + "Please select an option (1-2): ");
			option = scan.nextInt();
			if (option == 1) {
				String username, password;
				System.out.print("\nUsername: ");
				username = scan.next();
				System.out.print("Password: ");
				password = scan.next();

				PreparedStatement getAdmin = null;
				PreparedStatement getCustomer = null;

				try {
					ResultSet rs;
					isAdmin = false;
					String queryAdmin = "select count(*) from administrator where login = ? and password = ?";
					String queryCustomer = "select count(*) from customer where login = ? and password = ?";
					getAdmin = connection.prepareStatement(queryAdmin);
					getCustomer = connection.prepareStatement(queryCustomer);
					getAdmin.setString(1, username);
					getAdmin.setString(2, password);
					getCustomer.setString(1, username);
					getCustomer.setString(2, password);

					rs = getAdmin.executeQuery();

					int rsSize = 0;
					if (rs.last()) {
						rsSize = rs.getRow();
						rs.beforeFirst();
					}

					if (rsSize > 0) {
						login = username;
						isAdmin = true;
						exitLoop = true;
					}
					else {
						rs = getCustomer.executeQuery();
						if (rs.last()) {
							rsSize = rs.getRow();
							rs.beforeFirst();
						}
						if (rsSize > 0) {
							login = username;
							exitLoop = true;
						}
					}

					if (exitLoop == false) {
						System.out.println("Invalid username or password.");
					}

				}
				catch (SQLException e) {
					System.out.println("Error running queries. Machine Error: " + e.toString());
				}
				finally {
					try {
						if (getAdmin != null) getAdmin.close();
						if (getCustomer != null) getCustomer.close();
					}
					catch (SQLException e) {
						System.out.println("Cannot close statement. Machine error: " + e.toString());
					}
				}
			}
			else if (option == 2) {
				this.timeToQuit = true;
				exitLoop = true;
				login = "";
			}
			
			else {
				System.out.println("Please enter a legal option.");
			}
		}
		this.adminLogin = isAdmin;
		return login;
	}

	// Customer Methods
	public void browseProducts() {
		int option;
		Scanner scan = new Scanner(System.in);
		System.out.println("\n1. List All Products");
		System.out.println("2. List by Category");
		System.out.println("3. List by Highest Bid");
		System.out.println("4. Search Using Keywords");
		System.out.print("Please select an option (1-4): ");
		option = scan.nextInt();


		// Display all auctions, no sorting
		if (option == 1) {
			try {
				Statement getProducts = connection.createStatement();
				String query = "select auction_id, name, amount, seller, status from Product";
				ResultSet rs = getProducts.executeQuery(query);
				System.out.printf("%-15s  %-12s  %-12s  %-12s  %-12s\n", "auction_id", "name", "current_bid", "seller", "status");
				System.out.println("---------------------------------------------------------------\n");
				while (rs.next()) {
					int auction_id = rs.getInt("auction_id");
					String name = rs.getString("name");
					int amount = rs.getInt("amount");
					String seller = rs.getString("seller");
					String status = rs.getString("status");
					System.out.printf("%15d  %-12s  %12d  %-12s  %-12s\n", auction_id, name, amount, seller, status);
				}

				if (getProducts != null) getProducts.close();

			}
			catch (SQLException e) {
				System.out.println("Failed to display deals. Machine error: " + e.toString());
			}
		}

		// Display auctions based upon selected category
		else if (option == 2) {
			try {
				int categoryNum = 1;
				int selectedCategoryNum;
				String selectedCategory = "";
				Statement statement = connection.createStatement();
				String query = "select * from category where parent_category is null";
				ResultSet rs = statement.executeQuery(query);
				while (rs.next()) {
					String categoryName = rs.getString("name");
					System.out.println(categoryNum + ". " + categoryName);
					categoryNum++;
				}
				System.out.print("Please select a category (1-" + categoryNum + "): ");
				selectedCategoryNum = scan.nextInt();

				// Get root category selected
				categoryNum = 1;
				rs.beforeFirst();
				while (rs.next()) {
					if (categoryNum == selectedCategoryNum) {
						selectedCategory = rs.getString("name");
						break;
					}
					categoryNum++;
				}

				boolean leaf = false;

				// While the category selected is not a leaf going down the category tree
				while (!leaf) {

					if (selectedCategory.equals("")) {
						break;
					}

					categoryNum = 1;

					// Get set of categories that have parent of category
					query = "select * from category where parent_category='" + selectedCategory + "'";
					rs = statement.executeQuery(query);

					// Check to see if there are results, if none, it's a leaf
					if (rs.last()) {
						int rsLength = rs.getRow();
						rs.beforeFirst();
						if (rsLength == 0) {
							leaf = true;
						}
					}

					// If leaf, display the auctions
					if (leaf) {
						query = "select BelongsTo.auction_id, Product.name, BelongsTo.category, Product.amount, Product.seller, Product.status from BelongsTo inner join on BelongsTo.auction_id=Product.auction_id where BelongsTo.category='" + selectedCategory + "'";
						rs = statement.executeQuery(query);
						System.out.printf("%-15s  %-12s  %-20s  %-12s  %-12s  %-12s\n", "auction_id", "name", "category", "current_bid", "seller", "status");
						System.out.println("---------------------------------------------------------------\n");
						while (rs.next()) {
							int auction_id = rs.getInt("BelongsTo.auction_id");
							String name = rs.getString("Product.name");
							String cat = rs.getString("BelongsTo.category");
							int amount = rs.getInt("Product.amount");
							String seller = rs.getString("Product.seller");
							String status = rs.getString("Product.status");
							System.out.printf("%15d  %-12s  %-20s  %12d  %-12s  %-12s\n", auction_id, cat, name, amount, seller, status);
						}
					}

					// Else, make a new list for user to select from and do the whole thing again
					else {
						while (rs.next()) {
							String categoryName = rs.getString("name");
							System.out.println(categoryNum + ". " + categoryName);
							categoryNum++;
						}
						System.out.print("Please select a category (1-" + categoryNum + "): ");
						selectedCategoryNum = scan.nextInt();

						// Get branch category selected
						categoryNum = 1;
						rs.beforeFirst();
						while (rs.next()) {
							if (categoryNum == selectedCategoryNum) {
								selectedCategory = rs.getString("name");
								break;
							}
							categoryNum++;
						}
					}
				}

				if (statement != null) statement.close();

			}
			catch (SQLException e) {
				System.out.println("Something went wrong when we tried to dispaly categorized deals. Machine error: " + e.toString());
			}
		}
		else if (option == 3) {
			try {
				Statement getProducts = connection.createStatement();
				String query = "select auction_id, name, amount, seller, status from Product order by amount desc";
				ResultSet rs = getProducts.executeQuery(query);
				System.out.printf("%-15s  %-12s  %-12s  %-12s  %-12s\n", "auction_id", "name", "current_bid", "seller", "status");
				System.out.println("---------------------------------------------------------------\n");
				while (rs.next()) {
					int auction_id = rs.getInt("auction_id");
					String name = rs.getString("name");
					int amount = rs.getInt("amount");
					String seller = rs.getString("seller");
					String status = rs.getString("status");
					System.out.printf("%15d  %-12s  %12d  %-12s  %-12s\n", auction_id, name, amount, seller, status);
				}

				if (getProducts != null) getProducts.close();

			}
			catch (SQLException e) {
				System.out.println("Failed to display auctions. Machine error: " + e.toString());
			}
		}
		else if (option == 4) {
			try {
				String keywordsInput;
				System.out.print("Please enter up to two keywords separated by a space: ");	
				keywordsInput = scan.next();

				String[] keywords = keywordsInput.split(" ");

				String query = "";
				PreparedStatement statement = null;
				ResultSet rs = null;	

				// If keywords.length is less than 3, so only 2 are allowed
				if (keywords.length < 3) {
					if (keywords.length == 1) {
						query = "select auction_id, name, amount, seller, status, description from Product where description like %?%";
						statement = connection.prepareStatement(query);
						statement.setString(1, keywords[0]);
					}
					else {
						query = "select auction_id, name, amount, seller, status, description from Product where description like %?% and description like %?%";
						statement = connection.prepareStatement(query);
						statement.setString(1, keywords[0]);
						statement.setString(2, keywords[1]);
					}

					rs = statement.executeQuery();
					System.out.printf("%-15s  %-12s  %-12s  %-12s  %-12s  %-s\n", "auction_id", "name", "current_bid", "seller", "status", "description");
					System.out.println("---------------------------------------------------------------------------------------------\n");
					while (rs.next()) {
						int auction_id = rs.getInt("auction_id");
						String name = rs.getString("name");
						int amount = rs.getInt("amount");
						String seller = rs.getString("seller");
						String status = rs.getString("status");
						String description = rs.getString("description");
						System.out.printf("%15d  %-12s  %12d  %-12s  %-12s  %-s\n", auction_id, name, amount, seller, status, description);
					}
				}
				else {
					System.out.println("Too many keywords. Please try again.");
				}

				if (statement != null) statement.close();

			}
			catch (SQLException e) {
				System.out.println("Failed to display auctions. Machine error: " + e.toString());
			}
		}
	}


	// Admin Methods
	
	/*
	
		ADMINISTRATOR'S FUNCTIONS
	
	*/
	
	// New Customer Registration
	public void registerCustomer(String loginName, String pword, String name, String addr, String email, int newAdmin){
		
		try{
			
			Statement statement1 = connection.createStatement();
			Statement statement2 = connection.createStatement();
			String checkExistingCust = "SELECT login FROM Customer";
			String checkExistingAdmin = "SELECT login FROM Administrator";
			ResultSet currentCust = statement1.executeQuery(checkExistingCust);
			ResultSet currentAdmin = statement2.executeQuery(checkExistingAdmin);
			
			boolean noMatch = true;
			// since we always have an admin entry, make sure this login name doesn't exist in the admin table
			while(currentAdmin.next()){
				if(currentAdmin.getString(1).equals(loginName)){
					noMatch = false;
					break;
				}
			}
			
			// if it's not in admin table, make sure it's not in the customer table either
			if(noMatch == true){
				while(currentCust.next()){
					if(currentCust.getString(1).equals(loginName)){
						noMatch = false;
						break;
					}
				}
			}
			
			// if we can confirm login name is unique, insert new user into the appropriate table
			if(noMatch == true){
				String insertNewUserQuery = "";
				
				if(newAdmin == 1){
					insertNewUserQuery = "insert into Administrator values(?,?,?,?,?)";
				}
				else{
					insertNewUserQuery = "insert into Customer values(?,?,?,?,?)";
				}
				
				PreparedStatement insertNewUser = connection.prepareStatement(insertNewUserQuery);
				insertNewUser.setString(1, loginName);
				insertNewUser.setString(2, pword);
				insertNewUser.setString(3, name);
				insertNewUser.setString(4, addr);
				insertNewUser.setString(5, email);
				insertNewUser.executeUpdate();
				System.out.println("New user has been added");
			}
			else{
				System.out.println("Your login name has been taken, please register with a new login name");
			}
			
		}
		catch (SQLException e) {
			System.out.println("Error running queries. Machine Error: " + e.toString());
		}
		
	}
	
	// Update System Time
	public void updateSysDate(String newDate){
		
		try{
			
			java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd-MMM-yyyy/hh:mm:ssaa");
			java.sql.Date new_date = new java.sql.Date (df.parse(newDate).getTime());
			
			Statement statement = connection.createStatement();
			String compareSysDateQuery = "SELECT c_date FROM ourSysDate";
			ResultSet result = statement.executeQuery(compareSysDateQuery);
			
			while(result.next()){
				
				if(result.getDate(1).compareTo(new_date) == 0){
					System.out.println("New time is the same as old time, no update required");
				}
				else if(result.getDate(1).compareTo(new_date) > 0){
					System.out.println("System Date cannot be set back into the past.");
				}
				else{
					System.out.println("System time is smaller than new time, update system time");
					String updateSysDateQuery = "update ourSysDate set c_date = to_date(?,\'DD-MON-YYYY/HH:MI:SSAM\')";
					PreparedStatement updateSDate = connection.prepareStatement(updateSysDateQuery);
					updateSDate.setString(1, newDate);
					updateSDate.executeUpdate();
				}
				
			}
			
		}
		catch (SQLException e) {
			System.out.println("Error running queries. Machine Error: " + e.toString());
		}
		catch (ParseException e) {
			System.out.println("Error parsing data. Machine error: " + e.toString());
		}
		
	}
	
	// Product Statistics for all product
	// Report should contain product name, its status, current highest bid amount and corresponding bidder's login name if not sold, highest bid amount and buyer's login name if it was sold
	public void productStat(){
		
		try{
			
			Statement notSold = connection.createStatement();
			String allProductQuery = "SELECT auction_id, name, status, amount, buyer FROM Product";
			ResultSet allProductInfo = notSold.executeQuery(allProductQuery);
			boolean recordExists = false;
			
			while(allProductInfo.next()){
				recordExists = true;
				System.out.print("name: " + allProductInfo.getString(2) + "\tstatus: " + allProductInfo.getString(3));
				
				// if the product haven't been sold yet, search for the corresponding highest bidder
				if(!allProductInfo.getString(3).equals("sold")){
					int a_id = allProductInfo.getInt(1);
					Statement soldAuction = connection.createStatement();
					String soldProductQuery = "SELECT s.bidder, s.amount FROM (SELECT auction_id, bidder, amount FROM Bidlog WHERE auction_id = " + a_id + " ORDER BY amount desc) s WHERE rownum = 1";
					ResultSet soldProductInfo = soldAuction.executeQuery(soldProductQuery);
					
					while(soldProductInfo.next()){
						System.out.print("\thighest bidder: " + soldProductInfo.getString(1) + "\tamount: " + soldProductInfo.getInt(2));
					}
					System.out.println();
				}
				
				// if product is sold, display its buyer and the highest amount
				if(allProductInfo.getString(3).equals("sold")){
					System.out.println("\tamount: " + allProductInfo.getInt(4) + "\tbuyer: " + allProductInfo.getString(5));
				}
				
			}
			
			if(recordExists == false){
				System.out.println("No records of any product statistics, you have no products yet!");
			}
			
		}
		catch (SQLException e) {
			System.out.println("Error running queries. Machine Error: " + e.toString());
		}
		
	}
	
	// Product Statistics for a specific Customer
	public void productStat(String loginName){
		
		try{
			
			Statement notSold = connection.createStatement();
			String allProductQuery = "SELECT auction_id, name, status, amount, buyer FROM Product WHERE seller = \'" + loginName + "\'";
			ResultSet allProductInfo = notSold.executeQuery(allProductQuery);
			boolean recordExists = false;
			
			while(allProductInfo.next()){
				recordExists = true;
				System.out.print("name: " + allProductInfo.getString(2) + "\tstatus: " + allProductInfo.getString(3));
				
				// if the product haven't been sold yet, search for the corresponding highest bidder
				if(!allProductInfo.getString(3).equals("sold")){
					int a_id = allProductInfo.getInt(1);
					Statement soldAuction = connection.createStatement();
					String soldProductQuery = "SELECT s.bidder, s.amount FROM (SELECT auction_id, bidder, amount FROM Bidlog WHERE auction_id = " + a_id + " ORDER BY amount desc) s WHERE rownum = 1";
					ResultSet soldProductInfo = soldAuction.executeQuery(soldProductQuery);
					
					while(soldProductInfo.next()){
						System.out.print("\thighest bidder: " + soldProductInfo.getString(1) + "\tamount: " + soldProductInfo.getInt(2));
					}
					System.out.println();
				}
				
				// if product is sold, display its buyer and the highest amount
				if(allProductInfo.getString(3).equals("sold")){
					System.out.println("\tamount: " + allProductInfo.getInt(4) + "\tbuyer: " + allProductInfo.getString(5));
				}
				
			}
			
			if(recordExists == false){
				System.out.println("The user you selected haven't put up anything in the auction yet or does not exist.");
			}
			
		}
		catch (SQLException e) {
			System.out.println("Error running queries. Machine Error: " + e.toString());
		}
		
	}
	
	// top-k categories in terms of highest count of products sold, only counting subcategories
	// not complete yet
	public void topKCategoriesLeaf(int months, int k){
		
		try{
			
			Statement allCat = connection.createStatement();
			String allCatQuery = "SELECT distinct(name), parent_category, Product_Count(name, " + months + ") FROM Category WHERE parent_category IS NOT NULL AND Product_Count(name, " + months + ") > 0 AND rownum <= " + k + " ORDER BY Product_Count(name, " + months + ") desc";
			ResultSet allCatInfo = allCat.executeQuery(allCatQuery);
			boolean hasData = false;
			
			while(allCatInfo.next()){
				hasData = true;
				System.out.println("category name: " + allCatInfo.getString(1) + "\tnumber sold: " + allCatInfo.getInt(3));
			}
			
			if(hasData == false){
				System.out.println("No products sold in the last " + months + " month(s) falls in subcategories only");
			}
			
		}
		catch (SQLException e) {
			System.out.println("Error running queries. Machine Error: " + e.toString());
		}
		
	}
	
	// top-k categories in terms of highest count of products sold, only counting root categories
	// not complete yet
	public void topKCategoriesRoot(int months, int k){
		
		try{
			
			Statement allCat = connection.createStatement();
			String allCatQuery = "SELECT distinct(name), parent_category, Product_Count(name, " + months + ") FROM Category WHERE parent_category IS NULL AND Product_Count(name, " + months + ") > 0 AND rownum <= " + k + " ORDER BY Product_Count(name, " + months + ") desc";
			ResultSet allCatInfo = allCat.executeQuery(allCatQuery);
			boolean hasData = false;
			
			while(allCatInfo.next()){
				hasData = true;
				System.out.println("category name: " + allCatInfo.getString(1) + "\tnumber sold: " + allCatInfo.getInt(3));
			}
			
			if(hasData == false){
				System.out.println("No products sold in the last " + months + " month(s) falls in root categories only");
			}
			
		}
		catch (SQLException e) {
			System.out.println("Error running queries. Machine Error: " + e.toString());
		}
		
	}
	
	// top-k most active bidder in past months
	public void topKBidder(int months, int k){
		
		try{
			
			Statement topBidders = connection.createStatement();
			// String allCatQuery = "SELECT a.name, count(a.status = 'sold') as NumSold FROM (SELECT Category.name, Category.parent_category, BelongsTo.auction_id, Product.status FROM Category inner join BelongsTo on Category.name = BelongsTo.category inner join Product on BelongsTo.auction_id = Product.auction_id WHERE parent_category IS NOT NULL AND Product.status = 'sold' ORDER BY Category.name) a GROUP BY a.name";
			String bidderQuery = "SELECT bidder, Bid_Count(bidder, " + months + ") FROM Bidlog WHERE rownum <= " + k + " GROUP BY bidder ORDER BY Bid_Count(bidder, " + months +") desc";
			ResultSet bidderInfo = topBidders.executeQuery(bidderQuery);
			boolean hasData = false;
			
			while(bidderInfo.next()){
				hasData = true;
				System.out.println("Bidder: " + bidderInfo.getString(1) + "\tNumber of bids: " + bidderInfo.getInt(2));
			}
			
			if(hasData == false){
				System.out.println("No data in the last " + months + " month(s) on the most active bidder");
			}
			
		}
		catch (SQLException e) {
			System.out.println("Error running queries. Machine Error: " + e.toString());
		}
		
	}
	
	// top-k most active buyer in past months
	public void topKBuyer(int months, int k){
		
		try{
			
			Statement topBuyers = connection.createStatement();
			// String allCatQuery = "SELECT a.name, count(a.status = 'sold') as NumSold FROM (SELECT Category.name, Category.parent_category, BelongsTo.auction_id, Product.status FROM Category inner join BelongsTo on Category.name = BelongsTo.category inner join Product on BelongsTo.auction_id = Product.auction_id WHERE parent_category IS NOT NULL AND Product.status = 'sold' ORDER BY Category.name) a GROUP BY a.name";
			String buyerQuery = "SELECT buyer, Buying_Amount(buyer, " + months + ") FROM Product WHERE rownum <= " + k + " GROUP BY buyer ORDER BY Buying_Amount(buyer, " + months +") desc";
			ResultSet buyerInfo = topBuyers.executeQuery(buyerQuery);
			boolean hasData = false;
			
			while(buyerInfo.next()){
				hasData = true;
				System.out.println("Buyer: " + buyerInfo.getString(1) + "\tPurchases in dollars: " + buyerInfo.getInt(2));
			}
			
			if(hasData == false){
				System.out.println("No data in the last " + months + " month(s) on the most active buyer");
			}
			
		}
		catch (SQLException e) {
			System.out.println("Error running queries. Machine Error: " + e.toString());
		}
		
	}

}