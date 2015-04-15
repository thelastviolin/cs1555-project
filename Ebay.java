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