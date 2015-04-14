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
		username = "username"; //This is your username in oracle
		password = "password"; //This is your password in oracle

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
			while (!timeToQuit) {

			}
			System.out.println("\nGoodbye!");
		}
		catch(Exception Ex)  {
			System.out.println("Error connecting to database.  Machine Error: " + Ex.toString());
		}
		finally {
			/*
			 * NOTE: the connection should be created once and used through out the whole project;
			 * Is very expensive to open a connection therefore you should not close it after every operation on database
			 */
			connection.close();
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
		String login;
		Scanner scan = new Scanner(System.in);

		exitLoop = true;
		System.out.print("Welcome to the Auction House!\n");
		while (!exitLoop) {
			System.out.print("1. Login\n" + "2. Exit\n\n" + "Please select an option (1-2): ");
			option = scan.nextInt();
			if (option == 1) {
				String username, password;
				System.out.print("\nUsername: ");
				username = scan.nextLine();
				System.out.print("\nPassword: ");
				password = scan.nextLine();

				PreparedStatement getAdmin = null;
				PreparedStatement getCustomer = null;

				try {
					ResultSet rs;
					isAdmin = false;
					String queryAdmin = "select count(*) from administrator where username = ? and password = ?";
					String queryCustomer = "select count(*) from customer where username = ? and password = ?";
					getAdmin = connection.prepareStatement(queryAdmin);
					getCustomer = connection.prepareStatement(queryCustomer);
					getAdmin.setString(1, username);
					getAdmin.setString(2, password);
					getCustomer.setString(1, username);
					getCustomer.setString(2, password);

					int a = getAdmin.executeUpdate();
					if (a > 0) {
						login = username;
						isAdmin = true;
						exitLoop = true;
					}
					else {
						int c = getCustomer.executeUpdate();
						if (c > 0) {
							login = username;
							exitLoop = true;
						}
					}
				}
				catch (SQLException e) {
					System.out.println("Error running queries. Machine Error: " + e.toString());
				}
				catch (ParseException e) {
					System.out.println("Error parsing data. Machine error: " + e.toString());
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

}