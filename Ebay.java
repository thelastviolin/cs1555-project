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