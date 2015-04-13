/*
  Written by Thao N. Pham. 
  Updated by: Lory Al Moakar, Roxana Georghiu, Nick R. Katsipoulakis
  Purpose: Demo JDBC for CS1555 Class
  
  IMPORTANT (otherwise, your code may not compile)	
  Same as using sqlplus, you NEED TO SET oracle environment variables by 
  sourcing bash.env or tcsh.env
*/

import java.sql.*;  
import java.text.ParseException;
                    

public class TranDemo2 {
    private static Connection connection; 
    private Statement statement; 
    private PreparedStatement prepStatement; 
    private ResultSet resultSet; 
    private String query;  
    
    
    public TranDemo2(int example_no) {
	
	switch ( example_no) {
	case 1:
	    Example1();
	    break;
	case 2: 
	    Example2(2);
	    break;
	case 3:
	    Example2(3);
	    break;
	case 4: 
	    Example4();
	    break;
	case 5:
	    Example5();
	    break;
	default:
	    System.out.println("Example not found for your entry: " + example_no);
	    try {
		connection.close();
	    }
	    catch(Exception Ex)  {
		System.out.println("Error connecting to database.  Machine Error: " +
				   Ex.toString());
	    }
	    break;
	}
			
    }

    ///////////////////EXAMPLE 1////////////////////////
    public void Example1() {
	try {
	    connection.setAutoCommit(false); //the default is true and every statement executed is considered a transaction.
	    connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
	    statement = connection.createStatement();
	    
	    query = "SELECT * FROM class where classid = 1";
	    resultSet = statement.executeQuery(query);
	    
	    //note that there is no sleep here in this transaction

	    int counter=1;
	    while(resultSet.next()) {
		System.out.println("Record " + counter + ": \n      classID: " +
				   resultSet.getInt(1) + "\n      max_num_students: " +
				   resultSet.getInt(2) + "\n      cur_num_students: " +
				   resultSet.getInt(3));
		counter ++;
	    }
	    
	    resultSet.close();
	}	
	catch(Exception Ex)  
	{
		System.out.println("Machine Error: " +
				   Ex.toString());
	}
	finally{
		try {
			if (statement!=null) statement.close();
		} catch (SQLException e) {
			System.out.println("Cannot close Statement. Machine error: "+e.toString());
		}
	}
	
    }
    
    //////////EXAMPLE 2 + 3//////////////////////////////
    
    public void Example2(int mode ) {
	
	try {
	    connection.setAutoCommit(false); //the default is true and every statement executed is considered a transaction.
	    if ( mode == 2 ) 
		connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); //which is the default
	    else 
		connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE); 
	    statement = connection.createStatement();

	
	    //read the maximum and current number of students in the class
	    query = "SELECT max_num_students, cur_num_students FROM class where classid = 1";
	    resultSet = statement.executeQuery(query);
	    
	    //note that there is no sleep here in this transaction
	    
	    int max, cur;
	    if(resultSet.next()) {	
		max = resultSet.getInt(1);
		cur = resultSet.getInt(2);
		System.out.println( "Max is: " + max + " Cur is: " + cur);
		//sleep for 5 seconds, so that we have time to switch to the other transaction
		Thread.sleep(5000);
		
		if(cur<max) {
		    
		    query = "update class set cur_num_students = cur_num_students +1 where classid = 1";
		    int result = statement.executeUpdate(query); 
		    if (result == 1) 
			System.out.println("Update is successful " + result);
		    else 
			System.out.println("No rows were updated");
		}
		else { 
		    System.out.println("The class is full");
		}
		
	    }  
	    connection.commit();
	    resultSet.close();
	}	
	catch(Exception Ex)  {
	    System.out.println("Machine Error: " +
			       Ex.toString());
	}
	finally{
		try {
			if (statement!=null) statement.close();
		} catch (SQLException e) {
			System.out.println("Cannot close Statement. Machine error: "+e.toString());
		}
	}
    }

    ////// EXAMPLE 4 /////////////////////////////////////////////////////

    public void Example4() {
	try{
	    connection.setAutoCommit(false); 
	    connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); 
	    statement = connection.createStatement();
	    
	    //read the maximum and current number of students in the class
	    query = "SELECT max_num_students, cur_num_students FROM class where classid = 1 for update of cur_num_students";
	    resultSet = statement.executeQuery(query);
	   
	    int max, cur;
	    if(resultSet.next()) {	
		max = resultSet.getInt(1);
		cur = resultSet.getInt(2);
		System.out.println( "Max is: " + max + " Cur is: " + cur);
		//sleep for 5 seconds, so that we have time to switch to the other transaction
		Thread.sleep(5000);
		
		if(cur<max) {

		    query = "update class set cur_num_students = cur_num_students +1 where classid = 1";
		    int result = statement.executeUpdate(query); 
		    
		    if (result == 1) 
			System.out.println("Update is successful " + result);
		    else 
			System.out.println("No rows were updated");
		}
		else{ System.out.println("The class is full");}

	    }
	
	    //We need this because the connection was set with auto-commit=false
	    connection.commit();
	    resultSet.close();
	}	
	catch(Exception Ex) {
	    System.out.println("Machine Error: " +
			       Ex.toString());
	}
	finally{
		try {
			if (statement !=null) statement.close();
		} catch (SQLException e) {
			System.out.println("Cannot close Statement. Machine error: "+e.toString());
		}
	}
   }

	// //// EXAMPLE 5 /////////////////////////////////////////////////////

	public void Example5() {
		try {
			connection.setAutoCommit(false); 
			statement = connection.createStatement();

			query = "update class set max_num_students = 20 where classid = 2";
			int result = statement.executeUpdate(query);
			if (result == 1)
				System.out.println("Update1 is successful " + result);
			else
				System.out.println("No rows were updated");

			Thread.sleep(5000);

			query = "update class set max_num_students = 20 where classid = 1";
			result = statement.executeUpdate(query);

			if (result == 1)
				System.out.println("Update2 is successful " + result);
			else
				System.out.println("No rows were updated");

			connection.commit();
			
		} catch (Exception Ex) {
			System.out.println("Machine Error: " + Ex.toString());
		}
		finally{
			try {
				if (statement!=null) statement.close();
			} catch (SQLException e) {
				System.out.println("Cannot close Statement. Machine error: "+e.toString());
			}
		}

	}
    
  public static void main(String args[]) throws SQLException
  {
    /* Making a connection to a DB causes certain exceptions.  In order to handle
	   these, you either put the DB stuff in a try block or have your function
	   throw the Exceptions and handle them later.  For this demo I will use the
	   try blocks */

    String username, password;
	username = "username"; //This is your username in oracle
	password = "password"; //This is your password in oracle
	
	try{
	    // Register the oracle driver.  
	    DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
	    
	    //This is the location of the database.  This is the database in oracle
	    //provided to the class
	    String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass"; 
	    
	    //create a connection to DB on class3.cs.pitt.edu
	    connection = DriverManager.getConnection(url, username, password); 
	    TranDemo2 demo = new TranDemo2(Integer.parseInt(args[0]));
	    
	}
	catch(Exception Ex)  {
	    System.out.println("Error connecting to database.  Machine Error: " +
			       Ex.toString());
	}
	finally
	{
		/*
		 * NOTE: the connection should be created once and used through out the whole project;
		 * Is very expensive to open a connection therefore you should not close it after every operation on database
		 */
		connection.close();
	}
  }
}

