import java.util.Scanner;
import java.sql.*;
import java.text.ParseException;
import oracle.jdbc.driver.OracleDriver;
import java.util.ArrayList;

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

	public void makeNewAuction(String login) {
		try {
			Scanner scan = new Scanner(System.in);
			String prodName, description, categoryInput;
			int numDaysUp, minPrice;

			// Gather user input
			System.out.print("\nPlease enter a product name: ");
			prodName = scan.next();
			System.out.println("\nPlease enter a description for your product (optional): ");
			description = scan.next();
			System.out.println("\nPlease enter a category (or multiple categories separated by a space) for your product: ");
			categoryInput = scan.next();
			System.out.print("\nPlease enter the number of days you want your auction up: ");
			numDaysUp = scan.nextInt();
			System.out.print("\nPlease enter the minimum starting bid: ");
			minPrice = scan.nextInt();
			System.out.println("\n");

			// Split up categories input
			String[] categories = categoryInput.split(" ");

			// Set up query and statement
			String query;
			PreparedStatement statement = null;
			ResultSet rs;
			int resultSetLength = 0;

			// Check to see if given categories exist
			query = "select count(*) from category where ";
			for (int i = 0; i < categories.length; i++) {
				if (i == categories.length-1) {
					query += "name = ?";
				}
				else {
					query += "name = ? and ";
				}
			}

			statement = connection.prepareStatement(query);
			for (int i = 0; i < categories.length; i++) {
				statement.setString(i+1, categories[i]);
			} 

			rs = statement.executeQuery();
			while (rs.next()) {
				resultSetLength = rs.getInt("count");
			}

			boolean categoryNotLeaf = false;
			// If the categories don't exist, print error and do nothing else, continue
			if (resultSetLength != categories.length) {
				System.out.println("One or more given categories does not exist!");
			}
			else {	
				// Make sure categories are leaf nodes
				for (int i = 0; i < categories.length; i++) {
					query = "select count(*) from category where parent_category = ?";
					statement = connection.prepareStatement(query);
					statement.setString(1, categories[i]);
					rs = statement.executeQuery();
					while (rs.next()) {
						if (rs.getInt("count") > 0) {
							categoryNotLeaf = true;
							break;
						}
					}
				}
			}

			// Output message that says that the category isn't a leaf, else move on
			if (categoryNotLeaf) {
				System.out.println("One of the categories provided is not a leaf!");
			}
			else {

				// Call procedure
				CallableStatement putProduct = connection.prepareCall("{call Put_Product(?,?,?,?,?,?)}");
				putProduct.setString(1, prodName);
				putProduct.setString(2, description);
				putProduct.setString(3, login);
				putProduct.setInt(4, numDaysUp);
				putProduct.setInt(5, minPrice);
				putProduct.registerOutParameter(6, Types.INTEGER);
				putProduct.executeQuery();

				// Get back auction id
				int auction_id = putProduct.getInt(6);

				// Insert auction_id, category into BelongsTo
				for (int i = 0; i < categories.length; i++) {
					query = "insert into BelongsTo(auction_id,category) values(?,?)";
					statement = connection.prepareStatement(query);
					statement.setInt(1, auction_id);
					statement.setString(2, categories[i]);
					statement.executeUpdate();
				}

				// Close statements

				putProduct.close();
				statement.close();
			}
		}
		catch (SQLException e) {
			System.out.println("An error has occurred. Machine error: " + e.toString());
		}
	}


	public void bidOnAuction(String login) {
		try {
			int auction_id, bidAmount, currentBid;
			Scanner scan = new Scanner(System.in);
			System.out.print("\nPlease enter the auction ID of the product you want to bid on: ");
			auction_id = scan.nextInt();
			System.out.print("\nPlease enter your bid: ");
			bidAmount = scan.nextInt();

			String query = "";
			PreparedStatement statement = null;
			ResultSet rs;

			query = "select amount from Product where auction_id = ?";
			statement = connection.prepareStatement(query);
			statement.setInt(1, auction_id);
			rs = statement.executeQuery();

			// Find current highest bid
			currentBid = 0;
			while (rs.next()) {
				currentBid = rs.getInt("amount");
			}

			boolean bidTooLow = false;

			if (currentBid > bidAmount) {
				bidTooLow = true;
			}

			if (bidTooLow) {
				System.out.println("The bid you entered is not large enough.");
			}
			else {
				// Get new bidsn
				query = "select max(bidsn) from Bidlog";
				int highestBidsn = 0;
				statement = connection.prepareStatement(query);
				rs = statement.executeQuery();
				while (rs.next()) {
					highestBidsn = rs.getInt("max");
				}
				highestBidsn += 1;

				// Get current sysdate
				Date currentSysDate = null;
				query = "select c_date from ourSysDate";
				statement = connection.prepareStatement(query);
				rs = statement.executeQuery();
				while (rs.next()) {
					currentSysDate = rs.getDate("c_date");
				}

				// Add in new bid
				query = "insert into Bidlog(bidsn, auction_id, bidder, bid_time, amount) values(?,?,?,?,?)";
				statement = connection.prepareStatement(query);
				statement.setInt(1, highestBidsn);
				statement.setInt(2, auction_id);
				statement.setString(3, login);
				statement.setDate(4, currentSysDate);
				statement.setInt(5, bidAmount);
				statement.executeUpdate();
			}

			if (statement != null) statement.close();
		}
		catch (SQLException e) {
			System.out.println("There was an error inserting your new bid. Machine error: " + e.toString());
		}
	}

	public void sellAuctions(String login) {
		try {
			String query = "";
			PreparedStatement statement = null;
			ResultSet rs;

			Scanner scan = new Scanner(System.in);

			ArrayList<Integer> auction_ids = new ArrayList<Integer>();
			ArrayList<String> names = new ArrayList<String>();
			ArrayList<Integer> secondHighestBids = new ArrayList<Integer>();
			ArrayList<String> bidders = new ArrayList<String>();

			query = "select count(*) from Product where seller=? and status='under auction'";
			statement = connection.prepareStatement(query);
			statement.setString(1, login);

			int numAuctions = 0;
			rs = statement.executeQuery();
			while (rs.next()) {
				numAuctions = rs.getInt("count");
			}

			if (!(numAuctions > 0)) {
				System.out.println("You currently have no auctions you can end!");
			}
			else {
				// Populate array lists with auction_ids and names
				query = "select auction_id, name from Product where seller=? and status='under auction'";
				statement = connection.prepareStatement(query);
				statement.setString(1, login);
				rs = statement.executeQuery();
				while (rs.next()) {
					auction_ids.add(rs.getInt("auction_id"));
					names.add(rs.getString("name"));
				}

				// Find second highest bid for all auctions
				for (int i = 0; i < auction_ids.size(); i++) {
					query = "select count(*) from Bidlog where auction_id=?";
					statement = connection.prepareStatement(query);
					statement.setInt(1, auction_ids.get(i));
					rs = statement.executeQuery();
					int numBids = 0;
					while (rs.next()) {
						numBids = rs.getInt("count");
					}
					if (numBids == 0) {
						secondHighestBids.add(0);
					}
					else if (numBids == 1) {
						query = "select amount, bidder from Bidlog where auction_id=?";
						statement = connection.prepareStatement(query);
						statement.setInt(1, auction_ids.get(i));
						rs = statement.executeQuery();
						while (rs.next()) {
							secondHighestBids.add(rs.getInt("amount"));
							bidders.add(rs.getString("bidder"));
						}
					}
					else {
						query = "select amount, bidder from Bidlog where auction_id=? and rownum < 2 order by amount desc";
						statement = connection.prepareStatement(query);
						statement.setInt(1, auction_ids.get(i));
						rs = statement.executeQuery();
						rs.last();
						secondHighestBids.add(rs.getInt("amount"));
						bidders.add(rs.getString("bidder"));
					}
				}
			}

			numAuctions = 1;
			int auctionToClose = 0;
			// Display auctions available for close to user
			System.out.println("   Auction ID\tName\tAmount You Can Sell For");
			for (int i = 0; i < auction_ids.size(); i++) {
				System.out.println(numAuctions + ". " + auction_ids.get(i) + "\t" + names.get(i) + "\t" + secondHighestBids.get(i));
				numAuctions++;
			}
			System.out.print("Please choose which auction you want to close (1-" + numAuctions + "): ");
			auctionToClose = scan.nextInt();

			if (auctionToClose >= auction_ids.size() || auctionToClose < 0) {
				System.out.println("You failed to pick an auction correctly. Nice job.");
			}
			else {
				System.out.println("\n1. Withdraw Auction (The product will not be sold and the auction will be marked as closed.)");
				if (secondHighestBids.get(auctionToClose) != 0) {
					System.out.println("2. Sell Auction (The product will be sold at the price listed above.)");
				}
				System.out.print("Please select an option or enter 0 to cancel: ");
				int option = scan.nextInt();
				if (option == 1) {
					// Update product status to withdrawn
					query = "update Product set status='withdrawn' where auction_id=?";
					statement = connection.prepareStatement(query);
					statement.setInt(1, auction_ids.get(auctionToClose));
					statement.executeUpdate();
					System.out.println("Auction successfully withdrawn.");
				}
				else if (option == 2 && secondHighestBids.get(auctionToClose) != 0) {
					// Update product status to sold with buyer = bidder
					query = "update Product set status='sold', buyer=? where auction_id=?";
					statement = connection.prepareStatement(query);
					statement.setString(1, bidders.get(auctionToClose));
					statement.setInt(2, auction_ids.get(auctionToClose));
					statement.executeUpdate();
					System.out.println("Auction sold for " + secondHighestBids.get(auctionToClose) + " to " + bidders.get(auctionToClose) +".");
				}
			}
		}
		catch (SQLException e) {
			System.out.println("There was an error withdrawing or selling your auction. Machine error: " + e.toString());
		}
	}

	public void suggestAuctions(String login) {
		
	}


	// Admin Methods
	
	public void updateSysDate(String newDate){
		
		try{
			
			java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("DD-MON-YYYY/HH:MI:SSAM");
			java.sql.Date new_date = new java.sql.Date (df.parse(newDate).getTime());
			
			Statement statement = connection.createStatement();
			String compareSysDateQuery = "SELECT c_date FROM ourSysDate";
			ResultSet result = statement.executeQuery(compareSysDateQuery);
			int updateResult = 1;
			
			while(result.next()){
				
				System.out.println(result.getDate(1));
				// if(result.getDate(1).getTime() == new_date){
				// 	System.out.println("New time is the same as old time, you're crazy");
				// }
				// else if(result.getDate(1).getTime() > new_date){
				// 	System.out.println("System Date cannot be set back into the past.");
				// }
				// else{
				// 	System.out.println("System time is smaller than new time, update system time");
				// 	String updateSysDateQuery = "update ourSysDate set c_date = " + new_date;
				// 	updateResult = statement.executeUpdate(updateSysDateQuery);
				// }
				
			}
			
		}
		catch (SQLException e) {
			System.out.println("Error running queries. Machine Error: " + e.toString());
		}
		catch (ParseException e) {
			System.out.println("Error parsing data. Machine error: " + e.toString());
		}
		
	}

}