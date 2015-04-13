public class ebay {
	public static void main(String[] args) {
		boolean timeToQuit = false;
		String username, password;
		username = "username"; //This is your username in oracle
		password = "password"; //This is your password in oracle

		try {
			while (!timeToQuit) {
				// Register the oracle driver.  
				DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());

				//This is the location of the database.  This is the database in oracle
				//provided to the class
				String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass"; 

				//create a connection to DB on class3.cs.pitt.edu
				connection = DriverManager.getConnection(url, username, password); 
				TranDemo1 demo = new TranDemo1(Integer.parseInt(args[0]));
			}
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
}