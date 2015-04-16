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
				
				if(auctionHouse.adminLogin){
					
					
					
				}
				
				else{
					
					
					
				}
				
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

				try {
					
					Statement getAdmin = connection.createStatement();
					Statement getCustomer = connection.createStatement();
					// ResultSet rs;
					isAdmin = false;
					String queryAdmin = "select * from administrator where login = \'" + username + "\' and password = \'" + password + "\'";
					String queryCustomer = "select * from customer where login = \'" + username + "\' and password = \'" + password + "\'";
					ResultSet adminInfo = getAdmin.executeQuery(queryAdmin);
					ResultSet customerInfo = getCustomer.executeQuery(queryCustomer);
					
					while(adminInfo.next()){
						login = username;
						isAdmin = true;
						exitLoop = true;
					}
					if(exitLoop == false){
						while(customerInfo.next()) {
							login = username;
							exitLoop = true;
						}
					}
					if(exitLoop == false){
						System.out.println("Username does not exist or wrong password, please contact an administrator to create a new user");
					}
					
					adminInfo.close();
					getCustomer.close();
					
					getAdmin.close();
					getCustomer.close();
					
				}
				catch (SQLException e) {
					System.out.println("Error running queries. Machine Error: " + e.toString());
				}
				// finally {
				// 	try {
				// 		if (getAdmin != null) getAdmin.close();
				// 		if (getCustomer != null) getCustomer.close();
				// 	}
				// 	catch (SQLException e) {
				// 		System.out.println("Cannot close statement. Machine error: " + e.toString());
				// 	}
				// }
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
				insertNewUser.close();
			}
			else{
				System.out.println("Your login name has been taken, please register with a new login name");
			}
			
			currentCust.close();
			currentAdmin.close();
			
			statement1.close();
			statement2.close();
			
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
					updateSDate.close();
				}
				
			}
			
			result.close)();
			statement.close();
			
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
					
					soldProductInfo.close();
					soldAuction.close();
				}
				
				// if product is sold, display its buyer and the highest amount
				if(allProductInfo.getString(3).equals("sold")){
					System.out.println("\tamount: " + allProductInfo.getInt(4) + "\tbuyer: " + allProductInfo.getString(5));
				}
				
			}
			
			if(recordExists == false){
				System.out.println("No records of any product statistics, you have no products yet!");
			}
			
			allProductInfo.close();
			notSold.close();
			
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
					
					soldProductInfo.close();
					soldAuction.close();
				}
				
				// if product is sold, display its buyer and the highest amount
				if(allProductInfo.getString(3).equals("sold")){
					System.out.println("\tamount: " + allProductInfo.getInt(4) + "\tbuyer: " + allProductInfo.getString(5));
				}
				
			}
			
			if(recordExists == false){
				System.out.println("The user you selected haven't put up anything in the auction yet or does not exist.");
			}
			
			allProductInfo.close();
			notSold.close();
			
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
			
			allCatInfo.close();
			allCat.close();
			
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
			
			allCatInfo.close();
			allCat.close();
			
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
			
			bidderInfo.close();
			topBidders.close();
			
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
			
			buyerInfo.close();
			topBuyers.close();
			
		}
		catch (SQLException e) {
			System.out.println("Error running queries. Machine Error: " + e.toString());
		}
		
	}

}