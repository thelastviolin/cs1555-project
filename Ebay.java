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
			Scanner scan = new Scanner(System.in);

			String login = auctionHouse.handleLogin();
			while (!auctionHouse.timeToQuit) {
				if(auctionHouse.adminLogin){
					int option;
					System.out.println("1. Register User");
					System.out.println("2. Update System Time");
					System.out.println("3. Product Statistics for All Products");
					System.out.println("4. Product Statistics for Specific User");
					System.out.println("5. Top-k Leaf Categories in Past n Months ");
					System.out.println("6. Top-k Root Categories in Past n Months");
					System.out.println("7. Top-k Bidders in Past n Months");
					System.out.println("8. Top-k Buyers in Past n Months");
					System.out.println("9. Exit");
					System.out.print("Please choose an option (1-9): ");
					option = scan.nextInt();
					scan.nextLine();
					System.out.println();
					if (option == 1) {
						String loginName, pword, name, addr, email;
						int newAdmin;
						System.out.print("Please enter a login for the new user: ");
						loginName = scan.nextLine();
						System.out.print("Please enter a password for the new user: ");
						pword = scan.nextLine();
						System.out.print("Please enter a name for the new user: ");
						name = scan.nextLine();
						System.out.print("Please enter an address for the new user: ");
						addr = scan.nextLine();
						System.out.print("Please enter an e-mail for the new user: ");
						email = scan.nextLine();
						System.out.print("Is the new user an admin? (0-1): ");
						newAdmin = scan.nextInt();
						scan.nextLine();
						auctionHouse.registerCustomer(loginName, pword, name, addr, email, newAdmin);
						System.out.println();
					}
					else if (option == 2) {
						String date;
						System.out.print("Please enter a date with the format DD-MMM-YYYY/HH:MM:SSAM/PM: ");
						date = scan.nextLine();
						System.out.println();
						auctionHouse.updateSysDate(date);
						System.out.println();
					}
					else if (option == 3) {
						System.out.println();
						auctionHouse.productStat();
						System.out.println();
					}
					else if (option == 4) {
						String u;
						System.out.print("Please enter a username: ");
						u = scan.nextLine();
						System.out.println();
						auctionHouse.productStat(u);
						System.out.println();
					}
					else if (option == 5) {
						int months, k;
						System.out.print("Please enter number of past months: ");
						months = scan.nextInt();
						System.out.print("Please enter a number for k: ");
						k = scan.nextInt();
						scan.nextLine();
						System.out.println();
						auctionHouse.topKCategoriesLeaf(months, k);
						System.out.println();
					}
					else if (option == 6) {
						int months, k;
						System.out.print("Please enter number of past months: ");
						months = scan.nextInt();
						System.out.print("Please enter a number for k: ");
						k = scan.nextInt();
						scan.nextLine();
						System.out.println();
						auctionHouse.topKCategoriesRoot(months, k);
						System.out.println();
					}
					else if (option == 7) {
						int months, k;
						System.out.print("Please enter number of past months: ");
						months = scan.nextInt();
						System.out.print("Please enter a number for k: ");
						k = scan.nextInt();
						scan.nextLine();
						System.out.println();
						auctionHouse.topKBidder(months, k);
						System.out.println();
						
					}
					else if (option == 8) {
						int months, k;
						System.out.print("Please enter number of past months: ");
						months = scan.nextInt();
						System.out.print("Please enter a number for k: ");
						k = scan.nextInt();
						scan.nextLine();
						System.out.println();
						auctionHouse.topKBuyer(months, k);
						System.out.println();
					}
					else if (option == 9) {
						auctionHouse.timeToQuit = true;
					}
				}
				
				else{
					int option;
					System.out.println("1. Browse Products");
					System.out.println("2. Add New Auction");
					System.out.println("3. Bid on an Auction");
					System.out.println("4. Sell Product");
					System.out.println("5. Suggest Auctions for Bidding");
					System.out.println("6. Exit");
					System.out.print("Please choose an option (1-6): ");
					option = scan.nextInt();
					scan.nextLine();
					if (option == 1) {
						auctionHouse.browseProducts();
						System.out.println();
					}
					else if (option == 2) {
						auctionHouse.makeNewAuction(login);
						System.out.println();
					}
					else if (option == 3) {
						auctionHouse.bidOnAuction(login);
						System.out.println();
					}
					else if (option == 4) {
						auctionHouse.sellAuctions(login);
						System.out.println();
					}
					else if (option == 5) {
						auctionHouse.suggestAuctions(login);
						System.out.println();
					}
					else if (option == 6) {
						auctionHouse.timeToQuit = true;
					}
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
		int numberOfTries = 0;
		System.out.print("Welcome to the Auction House!\n");
		while (!exitLoop) {
			System.out.print("1. Login\n" + "2. Exit\n\n" + "Please select an option (1-2): ");
			option = scan.nextInt();
			scan.nextLine();
			if (option == 1) {
				String username, password;
				System.out.print("\nUsername: ");
				username = scan.nextLine();
				System.out.print("Password: ");
				password = scan.nextLine();

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
						numberOfTries++;
						
						if(numberOfTries == 3){
							System.out.println("Too many invalid attempts at login, program terminating");
							exitLoop = true;
							timeToQuit = true;
						}
						else{
							System.out.println("Username does not exist or wrong password, please contact an administrator to create a new user");
						}
					}
					
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
				rs.close();

				if (getProducts != null) getProducts.close();
				if (rs != null) rs.close();

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
				rs.close();
				categoryNum--;
				System.out.print("Please select a category (1-" + categoryNum + "): ");
				selectedCategoryNum = scan.nextInt();

				// Get root category selected
				categoryNum = 1;
				rs = statement.executeQuery(query);
				while (rs.next()) {
					if (categoryNum == selectedCategoryNum) {
						selectedCategory = rs.getString("name");
						break;
					}
					categoryNum++;
				}
				rs.close();

				boolean leaf = false;

				// While the category selected is not a leaf going down the category tree
				while (!leaf) {

					if (selectedCategory.equals("")) {
						break;
					}

					categoryNum = 1;

					// Get set of categories that have parent of category
					query = "select * from category where parent_category=\'" + selectedCategory + "\'";
					rs = statement.executeQuery(query);

					// Check to see if there are results, if none, it's a leaf
					int rsLength = 0;
					while (rs.next()) {
						rsLength++;
					}
					if (rsLength == 0) {
						leaf = true;
					}
					rs.close();

					// If leaf, display the auctions
					if (leaf) {
						query = "select BelongsTo.auction_id, Product.name, BelongsTo.category, Product.amount, Product.seller, Product.status from BelongsTo inner join Product on BelongsTo.auction_id=Product.auction_id where BelongsTo.category=\'" + selectedCategory + "\'";
						rs = statement.executeQuery(query);
						System.out.printf("%-15s  %-12s  %-20s  %-12s  %-12s  %-12s\n", "auction_id", "name", "category", "current_bid", "seller", "status");
						System.out.println("---------------------------------------------------------------\n");
						while (rs.next()) {
							int auction_id = rs.getInt(1);
							String name = rs.getString(2);
							String cat = rs.getString(3);
							int amount = rs.getInt(4);
							String seller = rs.getString(5);
							String status = rs.getString(6);
							System.out.printf("%15d  %-12s  %-20s  %12d  %-12s  %-12s\n", auction_id, name, cat, amount, seller, status);
						}
						rs.close();
					}

					// Else, make a new list for user to select from and do the whole thing again
					else {
						rs = statement.executeQuery(query);
						while (rs.next()) {
							String categoryName = rs.getString("name");
							System.out.println(categoryNum + ". " + categoryName);
							categoryNum++;
						}
						rs.close();
						categoryNum--;
						System.out.print("Please select a category (1-" + categoryNum + "): ");
						selectedCategoryNum = scan.nextInt();

						// Get branch category selected
						categoryNum = 1;
						rs = statement.executeQuery(query);
						while (rs.next()) {
							if (categoryNum == selectedCategoryNum) {
								selectedCategory = rs.getString("name");
								break;
							}
							categoryNum++;
						}
						rs.close();
					}
				}

				if (statement != null) statement.close();
				if (rs != null) rs.close();

			}
			catch (SQLException e) {
				System.out.println("Something went wrong when we tried to dispaly categorized deals. Machine error: " + e.toString());
			}
		}
		else if (option == 3) {
			try {
				Statement getProducts = connection.createStatement();
				String query = "select auction_id, name, amount, seller, status from Product order by amount desc nulls last";
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
				rs.close();

				if (getProducts != null) getProducts.close();
				if (rs != null) rs.close();

			}
			catch (SQLException e) {
				System.out.println("Failed to display auctions. Machine error: " + e.toString());
			}
		}
		else if (option == 4) {
			try {
				String keywordsInput;
				System.out.print("Please enter up to two keywords separated by a space: ");	
				scan.nextLine();
				keywordsInput = scan.nextLine();

				String[] keywords = keywordsInput.split(" ");

				String query = "";
				PreparedStatement statement = null;
				ResultSet rs = null;	

				// If keywords.length is less than 3, so only 2 are allowed
				if (keywords.length < 3) {
					if (keywords.length == 1) {
						query = "select auction_id, name, amount, seller, status, description from Product where description like \'%" + keywords[0] + "%\'";
						statement = connection.prepareStatement(query);
					}
					else {
						query = "select auction_id, name, amount, seller, status, description from Product where description like \'%" + keywords[0] + "%\' and description like \'%" + keywords[1] + "%\'";
						statement = connection.prepareStatement(query);
					}

					rs = statement.executeQuery();
					System.out.printf("%-15s  %-12s  %-12s  %-12s  %-12s  %-25s\n", "auction_id", "name", "current_bid", "seller", "status", "description");
					System.out.println("---------------------------------------------------------------------------------------------\n");
					while (rs.next()) {
						int auction_id = rs.getInt("auction_id");
						String name = rs.getString("name");
						int amount = rs.getInt("amount");
						String seller = rs.getString("seller");
						String status = rs.getString("status");
						String description = rs.getString("description");
						System.out.printf("%15d  %-12s  %12d  %-12s  %-12s  %-25s\n", auction_id, name, amount, seller, status, description);
					}
					rs.close();
				}
				else {
					System.out.println("Too many keywords. Please try again.");
				}

				if (statement != null) statement.close();
				if (rs != null) rs.close();

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
			prodName = scan.nextLine();
			System.out.println("\nPlease enter a description for your product (optional): ");
			description = scan.nextLine();
			System.out.println("\nPlease enter a category (or multiple categories separated by a comma) for your product: ");
			categoryInput = scan.nextLine();
			System.out.print("\nPlease enter the number of days you want your auction up: ");
			numDaysUp = scan.nextInt();
			System.out.print("\nPlease enter the minimum starting bid: ");
			scan.nextLine();
			minPrice = scan.nextInt();
			scan.nextLine();
			System.out.println("\n");

			// Split up categories input
			String[] categories = categoryInput.split(",");

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
				resultSetLength = rs.getInt(1);
			}
			rs.close();

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
						if (rs.getInt(1) > 0) {
							categoryNotLeaf = true;
							break;
						}
					}
					rs.close();
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

				System.out.println("Succesfully put up auction.");
			}

			if (rs != null) rs.close();

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
			System.out.print("Please enter your bid: ");
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
				currentBid = rs.getInt(1);
			}
			rs.close();

			boolean bidTooLow = false;

			if (currentBid >= bidAmount) {
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
					highestBidsn = rs.getInt(1);
				}
				rs.close();
				highestBidsn += 1;

				// Get current sysdate
				Date currentSysDate = null;
				query = "select c_date from ourSysDate";
				statement = connection.prepareStatement(query);
				rs = statement.executeQuery();
				while (rs.next()) {
					currentSysDate = rs.getDate(1);
				}
				rs.close();

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
			if (rs != null) rs.close();
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

			query = "select count(*) from Product where seller=? and status=\'underauction\'";
			statement = connection.prepareStatement(query);
			statement.setString(1, login);

			int numAuctions = 0;
			rs = statement.executeQuery();
			while (rs.next()) {
				numAuctions = rs.getInt(1);
			}
			rs.close();

			if (!(numAuctions > 0)) {
				System.out.println("You currently have no auctions you can end!");
			}
			else {
				// Populate array lists with auction_ids and names
				query = "select auction_id, name from Product where seller=? and status=\'underauction\'";
				statement = connection.prepareStatement(query);
				statement.setString(1, login);
				rs = statement.executeQuery();
				while (rs.next()) {
					auction_ids.add(rs.getInt("auction_id"));
					names.add(rs.getString("name"));
				}

				rs.close();

				// Find second highest bid for all auctions
				for (int i = 0; i < auction_ids.size(); i++) {
					query = "select count(*) from Bidlog where auction_id=?";
					statement = connection.prepareStatement(query);
					statement.setInt(1, auction_ids.get(i));
					rs = statement.executeQuery();
					int numBids = 0;
					while (rs.next()) {
						numBids = rs.getInt(1);
					}
					rs.close();
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
						rs.close();
					}
					else {
						query = "select * from (select amount, bidder from Bidlog where auction_id=? order by amount desc) t where rownum < 3";
						statement = connection.prepareStatement(query);
						statement.setInt(1, auction_ids.get(i));
						rs = statement.executeQuery();
						int tempAmount = 0;
						String tempBidder = "";
						while (rs.next()) {
							tempAmount = rs.getInt("amount");
							tempBidder = rs.getString("bidder");
						}	
						secondHighestBids.add(tempAmount);
						bidders.add(tempBidder);
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
				numAuctions--;
				System.out.print("Please choose which auction you want to close (1-" + numAuctions + "): ");
				auctionToClose = scan.nextInt();

				if (auctionToClose > auction_ids.size() || auctionToClose <= 0) {
					System.out.println("You failed to pick an auction correctly. Nice job.");
				}
				else {
					auctionToClose--;
					System.out.println("\n1. Withdraw Auction (The product will not be sold and the auction will be marked as closed.)");
					if (secondHighestBids.get(auctionToClose) != 0) {
						System.out.println("2. Sell Auction (The product will be sold at the price listed above.)");
					}
					System.out.print("Please select an option or enter 0 to cancel: ");
					int option = scan.nextInt();
					if (option == 1) {
						// Update product status to withdrawn
						query = "update Product set status=\'withdrawn\' where auction_id=?";
						statement = connection.prepareStatement(query);
						statement.setInt(1, auction_ids.get(auctionToClose));
						statement.executeUpdate();
						System.out.println("Auction successfully withdrawn.");
					}
					else if (option == 2 && secondHighestBids.get(auctionToClose) != 0) {
						// Update product status to sold with buyer = bidder
						query = "update Product set status=\'sold\', buyer=? where auction_id=?";
						statement = connection.prepareStatement(query);
						statement.setString(1, bidders.get(auctionToClose));
						statement.setInt(2, auction_ids.get(auctionToClose));
						statement.executeUpdate();
						System.out.println("Auction sold for " + secondHighestBids.get(auctionToClose) + " to " + bidders.get(auctionToClose) +".");
					}
				}
			}

			statement.close();
			rs.close();

		}
		catch (SQLException e) {
			System.out.println("There was an error withdrawing or selling your auction. Machine error: " + e.toString());
		}
	}

	public void suggestAuctions(String login) {
		try {
			String query = "";
			PreparedStatement statement = null;
			PreparedStatement print = null;
			ResultSet rs;

			ArrayList<Integer> auctions = new ArrayList<Integer>();

			query = "select count(bidder), auction_id from (select distinct Bidlog.bidder, Bidlog.auction_id from Bidlog, (select distinct Bidlog.auction_id from Bidlog inner join Product on Product.auction_id=Bidlog.auction_id where Product.status=\'underauction\' and Bidlog.bidder=?) uniqueAuctions where Bidlog.bidder != ? and Bidlog.auction_id = uniqueAuctions.auction_id) bidsAndUsers group by auction_id order by count(bidder) desc";
			statement = connection.prepareStatement(query);
			statement.setString(1, login);
			statement.setString(2, login);
			rs = statement.executeQuery();
			while (rs.next()) {
				auctions.add(rs.getInt("auction_id"));
				System.out.println(rs.getInt("auction_id"));
			}

			if (auctions.size() == 0) {
				System.out.println("There are no suggestions available at this time.");
			}
			else {
				System.out.printf("%-15s  %-12s  %-12s  %-12s  %-12s\n", "auction_id", "name", "current_bid", "seller", "status");
				System.out.println("---------------------------------------------------------------\n");
				for (int i = 0; i < auctions.size(); i++) {
					query = "select auction_id, name, amount, seller, status from Product where auction_id = ?";
					print = connection.prepareStatement(query);
					print.setInt(1, auctions.get(i));
					rs = print.executeQuery();
					while (rs.next()) {
						int auction_id = rs.getInt("auction_id");
						String name = rs.getString("name");
						int amount = rs.getInt("amount");
						String seller = rs.getString("seller");
						String status = rs.getString("status");
						System.out.printf("%15d  %-12s  %12d  %-12s  %-12s\n", auction_id, name, amount, seller, status);
					}
					rs.close();
				}
			}

			if (statement != null) statement.close();
			if (print != null) print.close();
			if (rs != null) rs.close();
		}
		catch (SQLException e) {
			System.out.println("There was an error getting possible suggestions. Machine error: " + e.toString());
		}

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
			
			result.close();
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