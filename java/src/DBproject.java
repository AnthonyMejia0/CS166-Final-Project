/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");

			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}

	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 *
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 *
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;

		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 *
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 * obtains the metadata object for the returned result set.  The metadata
		 * contains row and column info.
		*/
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;

		//iterates through the result set and saves the data returned by the query.
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>();
		while (rs.next()){
			List<String> record = new ArrayList<String>();
			for (int i=1; i<=numCol; ++i)
				record.add(rs.getString (i));
			result.add(record);
		}//end while
		stmt.close ();
		return result;
	}//end executeQueryAndReturnResult

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 *
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}

	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current
	 * value of sequence used for autogenerated keys
	 *
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */

	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();

		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 *
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if

		DBproject esql = null;

		try{
			System.out.println("(1)");

			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}

			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];

			esql = new DBproject (dbname, dbport, user, "");

			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Plane");
				System.out.println("2. Add Pilot");
				System.out.println("3. Add Flight");
				System.out.println("4. Add Technician");
				System.out.println("5. Book Flight");
				System.out.println("6. List number of available seats for a given flight.");
				System.out.println("7. List total number of repairs per plane in descending order");
				System.out.println("8. List total number of repairs per year in ascending order");
				System.out.println("9. Find total number of passengers with a given status");
				System.out.println("10. < EXIT");

				switch (readChoice()){
					case 1: AddPlane(esql); break;
					case 2: AddPilot(esql); break;
					case 3: AddFlight(esql); break;
					case 4: AddTechnician(esql); break;
					case 5: BookFlight(esql); break;
					case 6: ListNumberOfAvailableSeats(esql); break;
					case 7: ListsTotalNumberOfRepairsPerPlane(esql); break;
					case 8: ListTotalNumberOfRepairsPerYear(esql); break;
					case 9: FindPassengersCountWithStatus(esql); break;
					case 10: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddPlane(DBproject esql) {//1
		// need to acquire data to input into esql
		int Plane_ID;
		String Plane_Make;
		String Plane_Model;
		int Plane_Age;
		int num_seats;

		// the following do while loops are to ensure user inputs proper id, make, model, age, seats
		do
		{
			System.out.print("Input Plane_ID: ");
			try{
				Plane_ID = Integer.parseInt(in.readLine());
				break;
			}
			catch (Exception e)
			{
				System.out.println("Invalid Plane ID!");
				continue;
			}
		}while(true);

		do
		{
			System.out.print("Input Plane Make: ");
			try
			{
				Plane_Make = in.readLine();
				if(Plane_Make.length() <= 0 || Plane_Make.length() > 32)
				{
					throw new RuntimeException("Plane Make must be > 0 AND <=32 characters");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println(e);
				continue;
			}
		}while(true);

		do
		{
			System.out.print("Input Plane Model: ");
			try
			{
				Plane_Model = in.readLine();
				if(Plane_Model.length() <= 0 || Plane_Model.length() > 64)
				{
					throw new RuntimeException("Plane Model must be > 0 AND <= 64");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println(e);
				continue;
			}
		}while(true);

		do
		{
			System.out.print("Input Plane Age: ");
			try
			{
				Plane_Age = Integer.parseInt(in.readLine());
				if(Plane_Age < 0)
				{
					throw new RuntimeException("Plane Age can't be negative. UNSAFE TO FLY IF TOO OLD");
				}
				break;
			}
			catch (NumberFormatException e)
			{
				System.out.println("Invalid Input!");
				continue;
			}
			catch (Exception e)
			{
				System.out.println(e);
				continue;
			}
		}while(true);

		do
		{
			System.out.print("Input Number of Plane Seats: ");
			try
			{
				num_seats = Integer.parseInt(in.readLine());
				if( num_seats <= 0)
				{
					throw new RuntimeException("Number of Plane Seats should be > 0");
				}
				break;
			}
			catch (NumberFormatException e)
			{
				System.out.println("Invalid Input!");
				continue;
			}
			catch (Exception e)
			{
				System.out.println(e);
				continue;
			}
		}while(true);
		// attempt query
		try
		{
			String sql_query = "INSERT INTO Plane (id, make, model, age, seats) VALUES (" + Plane_ID + ", \'" + Plane_Make + "\', \'" + Plane_Model + "\', " + Plane_Age + ", " + num_seats + ");";
			esql.executeUpdate(sql_query);
			System.out.println("Successfully added Plane.\n");
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage());
		}
	}

	public static void AddPilot(DBproject esql) {//2
		//get pilot informoation
		int Pilot_ID;
		String Pilot_Name;
		String Pilot_Nationality;
		//the following do while loops are to ensure proper id, fullname. nationality
		do
		{
			System.out.print("Input Pilot ID: ");
			try
			{
				Pilot_ID = Integer.parseInt(in.readLine());
				break;
			}
			catch (Exception e)
			{
				System.out.println("Invalid Input!");
				continue;
			}
		}while(true);

		do
		{
			System.out.print("Input Pilot Name: ");
			try
			{
				Pilot_Name = in.readLine();
				if(Pilot_Name.length() <= 0 || Pilot_Name.length() > 128)
				{
					throw new RuntimeException("Pilot Name should be > 0 AND <= 128");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println(e);
				continue;
			}
		}while(true);

		do
		{
			System.out.print("Input Pilot Nationality: ");
			try
			{
				Pilot_Nationality = in.readLine();
				if(Pilot_Nationality.length() <= 0 || Pilot_Nationality.length() > 24)
				{
					throw new RuntimeException("Pilot Nationality must be  > 0 AND <= 24");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println(e);
				continue;
			}
		}while(true);
		//attempt query
		try
		{
			String sql_query = "INSERT INTO Pilot (id, fullname, nationality) VALUES (" + Pilot_ID + ", \'" + Pilot_Name + "\', \'" + Pilot_Nationality + "\');";
			esql.executeUpdate(sql_query);
			System.out.println("Successfully added Pilot.\n");
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage());
		}
	}

	public static void AddFlight(DBproject esql) {//3

		int flight_num;
		int flight_cost;
		int seats_sold;
		int flight_stops;
		String Flight_Arrival;
		String Flight_Destination;
		String Flight_Departure;
		//the following do while loops are used to correct input
		// check for proper fnum
		do
		{
			System.out.print("Input Flight Number: ");
			try
			{
				flight_num = Integer.parseInt(in.readLine());
				break;
			}
			catch (Exception e)
			{
				System.out.println("Invalid Input!");
				continue;
			}
		}while (true);
		//check for proper cost
		do
		{
			System.out.print("Input Flight Cost: $");
			try
			{
				flight_cost = Integer.parseInt(in.readLine());
				if (flight_cost <= 0)
				{
					throw new RuntimeException("Flight Cost shouldn't be free");
				}
				break;
			}
			catch (NumberFormatException e)
			{
				System.out.println("Your input is invalid!");
				continue;
			}
			catch (Exception e)
			{
				System.out.println(e);
				continue;
			}
		}while (true);
		//check for proper num_sold
		do {
			System.out.print("Input Number of Seats Sold: ");
			try {
				seats_sold = Integer.parseInt(in.readLine());
				if(seats_sold < 0)
				{
					throw new RuntimeException("Seats Sold cannot be negative / greater than capacity");
				}
				break;
			}
			catch (NumberFormatException e)
			{
				System.out.println("Invalid Input!");
				continue;
			}
			catch (Exception e)
			{
				System.out.println(e);
				continue;
			}
		}while (true);
		//check for proper num_stops
		do
		{
			System.out.print("Input Number of Stops: ");
			try
			{
				flight_stops = Integer.parseInt(in.readLine());
				if(flight_stops < 0)
				{
					throw new RuntimeException("flight stops cannot be negative");
				}
				break;
			}
			catch (NumberFormatException e)
			{
				System.out.println("Invalid Input!");
				continue;
			}
			catch (Exception e)
			{
				System.out.println(e);
				continue;
			}
		}while (true);
		//let's book a flight!
		// The dept_date, depart, date_time format is used to clarify and properly create a date/time object
		LocalDateTime dept_date;
		String depart;
		DateTimeFormatter date_time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

		//acquire the customer's Depature date and time
		do
		{
			System.out.print("Input Departure Time (YYYY-MM-DD hh:mm): ");
			try
			{
				depart = in.readLine();
				dept_date = LocalDateTime.parse(depart, date_time);
				break;
			}
			catch (Exception e)
			{
				System.out.println("Your input is invalid!");
				continue;
			}
		}while (true);

		//acquire the customer's arrival time assuming they are aware arrival time comes after departure time based off their local time zone
		do {
			System.out.print("Input Arrival Time (YYYY-MM-DD hh:mm): ");
			try
			{
				Flight_Arrival = in.readLine();
				LocalDateTime arrivalDate = LocalDateTime.parse(Flight_Arrival, date_time);
				if(!arrivalDate.isAfter(dept_date))
				{
					throw new RuntimeException("ERROR: Arrival Date cannot be before Departure Date.");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println(e);
				continue;
			}
		}while (true);
		// acquire  DEPT/ARRIVAL destinaitons of airport, inpute must be at most 5 char
		do {
			System.out.print("Input Airport Destination: ");
			try
			{
				Flight_Destination = in.readLine();
				if(Flight_Destination.length() <= 0)
				{
					throw new RuntimeException("Desination is at most 5 characters, but cannot be empty");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println(e);
				continue;
			}
		}while (true);

		do {
			System.out.print("Input Departure Airport: ");
			try {
				Flight_Departure = in.readLine();
				if(Flight_Departure.length() <= 0 || Flight_Departure.length() > 5)
				{
					throw new RuntimeException("Departure is 0-5 characters");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println(e);
				continue;
			}
		}while (true);
		// attempt queury
		try
		{
			String sql_query = "INSERT INTO Flight (fnum, cost, num_sold, num_stops, actual_departure_date, actual_arrival_date, arrival_airport, departure_airport) VALUES (" + flight_num + ", " + flight_cost + ", " + seats_sold + ", " + flight_stops + ", \'" + depart + "\', \'" + Flight_Arrival + "\', \'" + Flight_Destination + "\', \'" + Flight_Departure + "\');";
			esql.executeUpdate(sql_query);
			System.out.println("Successfully added Flight.\n");
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage());
		}
	}

	public static void AddTechnician(DBproject esql) {//4
		int tech_ID;
		String Tech_Name;
		//obtain a technicians ID and name to check who maintained what plane
		do {
			System.out.print("Input Technician ID Number: ");
			try
			{
				tech_ID = Integer.parseInt(in.readLine());
				break;
			}
			catch (Exception e)
			{
				System.out.println("Invalid Input!");
				continue;
			}
		}while(true);

		do {
			System.out.print("Input Technician Name: ");
			try
			{
				Tech_Name = in.readLine();
				if(Tech_Name.length() <= 0 || Tech_Name.length() > 128)
				{
					throw new RuntimeException("Technician Name must be > 0 AND <+ 128");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println(e);
				continue;
			}
		}while (true);

		//attempt queury
		try
		{
			String query = "INSERT INTO Technician (id, full_name) VALUES (" + tech_ID + ", \'" + Tech_Name + "\');";
			esql.executeUpdate(query);
			System.out.println("Successfully added Technician.\n");
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage());
		}
	}

	public static void BookFlight(DBproject esql) {//5
		// Given a customer and a flight that he/she wants to book, add a reservation to the DB
		int cust_ID;
		int flight_num;
		//acquire customers ID and identify the flight they want to take
		do
		{
			System.out.print("Input Customer ID: ");
			try
			{
				cust_ID = Integer.parseInt(in.readLine());
				break;
			}
			catch (Exception e)
			{
				System.out.println("Invalid Input");
				continue;
			}
		}while(true);

		do
		{
			System.out.print("Input Flight Number: ");
			try
			{
				flight_num = Integer.parseInt(in.readLine());
				break;
			}
			catch (Exception e)
			{
				System.out.println("Invalid Input!");
				continue;
			}
		}while(true);
		//the following query is used to check whether a customer made a reservation for a flight, else we will book one
		try {
			String sql_query = "SELECT status\nFROM Reservation\nWHERE cid = " + cust_ID + " AND fid = " + flight_num + ";";
			String reserve_input;
		// if the query returns no matching results, we will ask to book a flight
			if(esql.executeQueryAndPrintResult(sql_query) == 0) {
				do {
					System.out.println("Book a reservation? (Yes/No): ");
					try {
						reserve_input = in.readLine();
						if(reserve_input.equals("Yes")) {
							int reserve_num;
							String reserve_status;
							do
							{
								System.out.print("Input Reservation Number: ");
								try
								{
									reserve_num = Integer.parseInt(in.readLine());
									break;
								}
								catch (Exception e)
								{
									System.out.println("Invalid Input");
									continue;
								}
							}
							while (true);

							do
							{
								System.out.print("Input Reservation Status (W / R / C): ");
								try
								{
									reserve_status = in.readLine();
									if(!reserve_status.equals("W") && !reserve_status.equals("R") && !reserve_status.equals("C"))
									{
										throw new RuntimeException("Valid inputs are ( W / R / C )");
									}
									break;
								}
								catch (Exception e)
								{
									System.out.println(e);
									continue;
								}
							}while(true);

							try
							{
								//update the table with new flight reservation
								sql_query = "INSERT INTO Reservation (rnum, cid, fid, status) VALUES (" + reserve_num + ", " + cust_ID + ", " + flight_num + ", \'" + reserve_status + "\');";
								esql.executeUpdate(sql_query);
								System.out.println("Successfully booked reservation.\n");
								break;
							}
							catch (Exception e)
							{
								System.err.println (e.getMessage());
							}
						}
						//if input was no, don't book
						else if(reserve_input.equals("No"))
						{
							System.out.println("Not Booking reservation.\n");
							break;
						}
						else {
							System.out.println("Not a valid option!\n");
						}
					}
					catch (Exception e)
					{
						System.out.println(e);
						continue;
					}
				}while (true);
			}
			else
			{
				//if the reservation exists we can update the reservation content
				do
				{
					try
					{
						System.out.println("Update Reservation? (Yes/No): ");
						reserve_input = in.readLine();
						if(reserve_input.equals("Yes"))
						{
							String Status_Update;
							do
							{
								System.out.print("Input Updated Status (W / R / C): ");
								try
								{
									Status_Update = in.readLine();
									if(!Status_Update.equals("W") && !Status_Update.equals("R") && !Status_Update.equals("C"))
									{
										throw new RuntimeException("Valid inputs are ( W / R / C )");
									}
									break;
								}
								catch (Exception e)
								{
									System.out.println(e);
									continue;
								}
							}while (true);

							try
							{
								sql_query = "UPDATE Reservation SET status = \'" + Status_Update + "\' WHERE cid = " + cust_ID + " AND fid = " + flight_num + ";";
								esql.executeUpdate(sql_query);
								System.out.println("Successfully updated reservation.");
								break;
							}
							catch (Exception e)
							{
								System.err.println (e.getMessage());
							}
						}
						else if(reserve_input.equals("No"))
						{
							System.out.println("Will not update table.\n");
							break;
						}
					}
					catch (Exception e)
					{
						System.out.println(e);
						continue;
					}
				}while (true);
			}
		}
		catch (Exception e)
		{
			System.err.println (e.getMessage());
		}
	}

	public static void ListNumberOfAvailableSeats(DBproject esql) {//6
		// For flight number and date, find the number of availalbe seats (i.e. total plane capacity minus booked seats )
		int number;
		String departure_date;
		
		do {
			try {
				System.out.print("Enter Flight Number: ");
				number = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("ERROR: Input is not a valid flight #.\n");
				continue;
			}
		}while (true);
				
		do {
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
				System.out.print("Enter Departure Date and Time (YYYY-MM-DD hh:mm): ");
				departure_date = in.readLine();
				LocalDate formatted = LocalDate.parse(departure_date, formatter);
				break;
			}catch (Exception e) {
				System.out.println("ERROR: Input format is invalid.\n");
				continue;
			}
		}while (true);

		try {
			String query = "SELECT capacity - booked as \"Available Seats\" FROM (SELECT P.seats as capacity FROM Plane P, FlightInfo I WHERE I.flight_id = " + number + " AND I.plane_id = P.id) AS CAP, (SELECT F.num_sold as booked FROM Flight F WHERE F.fnum = " + number + " AND F.actual_departure_date = \'" + departure_date + "\') AS BOOK;";
			
			if(esql.executeQueryAndPrintResult(query) == 0) {
				System.out.println("Flight info does not exist.\n");
			}
		}catch (Exception e) {
			System.err.println (e.getMessage());
		}

		System.out.println("\n");
	}

	public static void ListsTotalNumberOfRepairsPerPlane(DBproject esql) {//7
		// Count number of repairs per planes and list them in descending order
		try {
			String query = "SELECT P.id, count(R.rid) FROM Plane P, Repairs R WHERE P.id = R.plane_id GROUP BY P.id ORDER BY count DESC;";
			esql.executeQueryAndPrintResult(query);
		}catch (Exception e) {
			System.err.println (e.getMessage());
		}

		System.out.println("\n");
	}

	public static void ListTotalNumberOfRepairsPerYear(DBproject esql) {//8
		// Count repairs per year and list them in ascending order
		try {
			String query = "SELECT EXTRACT (year FROM R.repair_date) as \"Year\", count(R.rid) FROM repairs R GROUP BY \"Year\" ORDER BY count ASC;";
			esql.executeQueryAndPrintResult(query);
		}catch (Exception e) {
			System.err.println (e.getMessage());
		}

		System.out.println("\n");
	}
	
	public static void FindPassengersCountWithStatus(DBproject esql) {//9
		// Find how many passengers there are with a status (i.e. W,C,R) and list that number.
		int number;
		String status;
		
		do {
			try {
				System.out.print("Enter Flight Number: ");
				number = Integer.parseInt(in.readLine());
				String check_flight = "SELECT fnum FROM Flight WHERE fnum = " + number + ";";

				if (esql.executeQuery(check_flight) == 0){
					System.out.println("Flight number does not exist!\n");
					continue;
				}
				break;
			}catch (Exception e) {
				System.out.println("ERROR: Input is not a valid flight #.\n");
				continue;
			}
		}while (true);
				
		do {
			System.out.print("Enter Passenger Status (W, R, C): ");
			try {
				status = in.readLine();
				if(!status.equals("W") && !status.equals("R") && !status.equals("C")) {
					throw new RuntimeException("ERROR: Acceptable inputs are W, R, C.\n");
				}
				break;
			}catch (Exception e) {
				System.out.println(e);
				continue;
			}
		}while (true);

		try {
			String query = "SELECT COUNT(*) FROM Reservation WHERE fid = " + number + " AND status = \'" + status + "\';";
			
			esql.executeQueryAndPrintResult(query);
		}catch (Exception e) {
			System.err.println (e.getMessage());
		}

		System.out.println("\n");
	}
}
