package edu.selu.android.classygames.utilities;


public class DatabaseUtilities
{


	public final static String TABLE_GAMES = "games";
	public final static String TABLE_GAMES_COLUMN_ID = "id";
	public final static String TABLE_GAMES_COLUMN_USER_CREATOR = "user_creator";
	public final static String TABLE_GAMES_COLUMN_USER_CHALLENGED = "user_challenged";
	public final static String TABLE_GAMES_COLUMN_BOARD = "board";
	public final static String TABLE_GAMES_COLUMN_TURN = "turn";
	public final static String TABLE_GAMES_COLUMN_GAME_TYPE = "game_type";
	public final static String TABLE_GAMES_COLUMN_LAST_MOVE = "last_move";
	public final static String TABLE_GAMES_COLUMN_FINISHED = "finished";
	public final static byte TABLE_GAMES_TURN_CREATOR = 1;
	public final static byte TABLE_GAMES_TURN_CHALLENGED = 2;
	public final static byte TABLE_GAMES_FINISHED_FALSE = 1;
	public final static byte TABLE_GAMES_FINISHED_TRUE = 2;
	public final static String TABLE_GAMES_FORMAT = "(" + TABLE_GAMES_COLUMN_ID + ", " + TABLE_GAMES_COLUMN_USER_CREATOR + ", " + TABLE_GAMES_COLUMN_USER_CHALLENGED + ", " + TABLE_GAMES_COLUMN_BOARD + ", " + TABLE_GAMES_COLUMN_TURN + ", " + TABLE_GAMES_COLUMN_GAME_TYPE + ", " + TABLE_GAMES_COLUMN_FINISHED + ")";
	public final static String TABLE_GAMES_VALUES = "VALUES (?, ?, ?, ?, ?, ?, ?)";

	public final static String TABLE_USERS = "users";
	public final static String TABLE_USERS_COLUMN_ID = "id";
	public final static String TABLE_USERS_COLUMN_NAME = "name";
	public final static String TABLE_USERS_COLUMN_REG_ID = "reg_id";
	public final static String TABLE_USERS_FORMAT = "(" + TABLE_USERS_COLUMN_ID + ", " + TABLE_USERS_COLUMN_NAME + ", " + TABLE_USERS_COLUMN_REG_ID + ")";
	public final static String TABLE_USERS_VALUES = "VALUES (?, ?, ?)";




	/**
	 * it's best to release SQL resources in reverse order of their creation
	 * https://dev.mysql.com/doc/refman/5.0/en/connector-j-usagenotes-statements.html#connector-j-examples-execute-select
	 */
	public static void closeSQL(Connection sqlConnection, PreparedStatement sqlStatement)
	{
		closeSQLStatement(sqlStatement);
		closeSQLConnection(sqlConnection);
	}


	public static void closeSQLConnection(Connection sqlConnection)
	{
		if (sqlConnection != null)
		{
			try
			{
				sqlConnection.close();
			}
			catch (final SQLException e)
			{

			}
		}
	}


	public static void closeSQLStatement(PreparedStatement sqlStatement)
	{
		if (sqlStatement != null)
		{
			try
			{
				sqlStatement.close();
			}
			catch (final SQLException e)
			{

			}
		}
	}


	/**
	 * Ensures that a given user exists in the database. If the user already exists in the
	 * database then this method doesn't do very much. If the user does not already exist in the
	 * database, then this method will insert them into it.
	 * 
	 * @param sqlConnection
	 * An <strong>already valid</strong> connection to the database. This connection will not be
	 * closed after we are finished performing operations here.
	 * 
	 * @param user_id
	 * The ID of the user as a long. This is the user's Facebook ID.
	 * 
	 * @param user_name
	 * The name of the user as a String.
	 * 
	 * @return
	 * True if we were able to successfully insert this new user into the database OR if the user
	 * already exists in the database. False if the user did not exist in the database and we were
	 * unable to insert him into it.
	 */
	public static boolean ensureUserExistsInDatabase(final Connection sqlConnection, final long user_id, final String user_name)
	{
		boolean errorFree = true;
		PreparedStatement sqlStatement = null;

		try
		{
			// prepare a SQL statement to be run on the database
			String sqlStatementString = "SELECT * FROM " + DATABASE_TABLE_USERS + " WHERE " + DATABASE_TABLE_USERS_COLUMN_ID + " = ?";
			sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

			// prevent SQL injection by inserting data this way
			sqlStatement.setLong(1, user_id);

			// run the SQL statement and acquire any return information
			final ResultSet sqlResult = sqlStatement.executeQuery();

			if (sqlResult.next())
			// user exists in the database. no further actions needs to be taken
			{

			}
			else
			// user does not exist in the database. we need to put them in there
			{
				// prepare a SQL statement to be run on the database
				sqlStatementString = "INSERT INTO " + DATABASE_TABLE_USERS + " (" + DATABASE_TABLE_USERS_COLUMN_ID + ", " + DATABASE_TABLE_USERS_COLUMN_NAME + ") VALUES (?, ?)";
				sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

				// prevent SQL injection by inserting data this way
				sqlStatement.setLong(1, user_id);
				sqlStatement.setString(2, user_name);

				// run the SQL statement
				sqlStatement.executeUpdate();
			}
		}
		catch (final SQLException e)
		{
			errorFree = false;
		}
		finally
		{
			closeSQLStatement(sqlStatement);
		}

		return errorFree;
	}


	/**
	 * Query the database for a user who's ID matches the input's.
	 * 
	 * @param sqlConnection
	 * Your existing database Connection object. Must already be connected, as this method makes no attempt
	 * at doing so.
	 * 
	 * @param user
	 * The ID of the user you're searching for as a long.
	 * 
	 * @return
	 * The name of the user that you queried for as a String.
	 */
	public static String grabUsersName(final Connection sqlConnection, final long user)
	{
		PreparedStatement sqlStatement = null;
		String username = null;

		try
		{
			// prepare a SQL statement to be run on the MySQL database
			final String sqlStatementString = "SELECT " + Utilities.DATABASE_TABLE_USERS_COLUMN_NAME + " FROM " + Utilities.DATABASE_TABLE_USERS + " WHERE " + Utilities.DATABASE_TABLE_USERS_COLUMN_ID + " = ?";
			sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

			// prevent SQL injection by inserting data this way
			sqlStatement.setLong(1, user);

			// run the SQL statement and acquire any return information
			final ResultSet sqlResult = sqlStatement.executeQuery();

			if (sqlResult.next())
			// check to see that we got some SQL return data
			{
				// grab the user's name from the SQL query
				username = sqlResult.getString(Utilities.DATABASE_TABLE_USERS_COLUMN_NAME);
			}
			else
			{
				username = Utilities.APP_NAME;
			}
		}
		catch (final SQLException e)
		{

		}
		finally
		{
			Utilities.closeSQLStatement(sqlStatement);
		}

		return username;
	}


	public static Connection getSQLConnection() throws SQLException
	// I followed this guide to understand how to connect to the MySQL database that is created when
	// making a new Amazon Elastic Beanstalk application
	// http://docs.amazonwebservices.com/elasticbeanstalk/latest/dg/create_deploy_Java.rds.html
	{
		try
		{
			// ensure that the MySQL JDBC Driver has been loaded
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch (final Exception e)
		{

		}

		// acquire database credentials from Amazon Web Services
		final String hostname = System.getProperty("RDS_HOSTNAME");
		final String port = System.getProperty("RDS_PORT");
		final String dbName = System.getProperty("RDS_DB_NAME");
		final String username = System.getProperty("RDS_USERNAME");
		final String password = System.getProperty("RDS_PASSWORD");

		// create the connection string
		final String connectionString = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + username + "&password=" + password;

		// return a new connection to the database
		return DriverManager.getConnection(connectionString);
	}


	public static void removeUserRegId(final Connection sqlConnection, final long userId)
	{
		PreparedStatement sqlStatement = null;

		try
		{
			// prepare a SQL statement to be run on the database
			final String sqlStatementString = "UPDATE " + Utilities.DATABASE_TABLE_USERS + " SET " + Utilities.DATABASE_TABLE_USERS_COLUMN_REG_ID + " = null WHERE " + Utilities.DATABASE_TABLE_USERS_COLUMN_ID + " = ?";
			sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

			// prevent SQL injection by inserting data this way
			sqlStatement.setLong(1, userId);

			// run the SQL statement
			sqlStatement.executeUpdate();
		}
		catch (final SQLException e)
		{

		}
		finally
		{
			closeSQLStatement(sqlStatement);
		}
	}


	public static void updateUserRegId(final Connection sqlConnection, final String userRegId, final long userId)
	{
		PreparedStatement sqlStatement = null;

		try
		{
			// prepare a SQL statement to be run on the database
			final String sqlStatementString = "UPDATE " + Utilities.DATABASE_TABLE_USERS + " SET " + Utilities.DATABASE_TABLE_USERS_COLUMN_REG_ID + " = ? WHERE " + Utilities.DATABASE_TABLE_USERS_COLUMN_ID + " = ?";
			sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

			// prevent SQL injection by inserting user data this way
			sqlStatement.setString(1, userRegId);
			sqlStatement.setLong(2, userId);
		} 
		catch (final SQLException e)
		{

		}
		finally
		{
			closeSQLStatement(sqlStatement);
		}
	}


}
