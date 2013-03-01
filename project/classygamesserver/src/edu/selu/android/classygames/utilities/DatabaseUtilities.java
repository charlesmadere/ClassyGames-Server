package edu.selu.android.classygames.utilities;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


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
	 * Establishes a connection to the SQL database and returns that newly made
	 * connection. I followed this guide to understand how to connect to the
	 * MySQL database that is created when making a new Amazon Elastic
	 * Beanstalk application:
	 * http://docs.amazonwebservices.com/elasticbeanstalk/latest/dg/create_deploy_Java.rds.html
	 * A MySQL JDBC Driver is required for this MySQL connection to actually
	 * work. You can download that driver from here:
	 * https://dev.mysql.com/downloads/connector/j/
	 * 
	 * @return
	 * Returns a newly made connection to the SQL database.
	 * 
	 * @throws SQLException
	 * If a connection to the SQL database could not be created then a
	 * SQLException will be thrown.
	 * 
	 * @throws Exception
	 * If, when trying to load the MySQL JDBC driver there is an error, then
	 * an Exception will be thrown.
	 */
	public static Connection acquireSQLConnection() throws SQLException, Exception
	{
		// ensure that the MySQL JDBC Driver has been loaded
		Class.forName("com.mysql.jdbc.Driver").newInstance();

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


	/**
	 * Releases SQL resources. It's best to release SQL resources in reverse
	 * order of their creation. This method <strong>must be used</strong> when
	 * dealing with SQL stuff.
	 * https://dev.mysql.com/doc/refman/5.0/en/connector-j-usagenotes-statements.html#connector-j-examples-execute-select
	 * 
	 * @parameter sqlConnection
	 * A SQL Connection object. It's okay if this object is null or if it was
	 * never given an actual connection to a SQL database.
	 * 
	 * @parameter sqlStatement
	 * A SQL PreparedStatement object. It's okay if this object is null or if
	 * it was never given an actual SQL statement / query to run.
	 */
	public static void closeSQL(final Connection sqlConnection, final PreparedStatement sqlStatement)
	{
		closeSQLStatement(sqlStatement);
		closeSQLConnection(sqlConnection);
	}


	/**
	 * Releases a SQL Connection resource.
	 * 
	 * @parameter sqlConnection
	 * A SQL Connection object. It's okay if this object is null or if it was
	 * never given an actual connection to a SQL database.
	 */
	public static void closeSQLConnection(final Connection sqlConnection)
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


	/**
	 * Releases a SQL PreparedStatement resource.
	 * 
	 * @parameter sqlStatement
	 * A SQL PreparedStatement object. It's okay if this object is null or if
	 * it was never given an actual SQL statement / query to run.
	 */
	public static void closeSQLStatement(final PreparedStatement sqlStatement)
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
	 * Ensures that a given user exists in the database. If the user already
	 * exists in the database then this method doesn't do very much. If the
	 * user does not already exist in the database, then this method will
	 * insert them into it.
	 * 
	 * @param sqlConnection
	 * An <strong>already valid</strong> connection to the database. This
	 * connection will not be closed after we are finished performing
	 * operations here.
	 * 
	 * @param userId
	 * The ID of the user as a long. This is the user's Facebook ID.
	 * 
	 * @param userName
	 * The name of the user as a String.
	 * 
	 * @throws SQLException
	 * If at some point there is some kind of connection error or query problem
	 * with the SQL database then this Exception will be thrown.
	 */
	public static void ensureUserExistsInDatabase(final Connection sqlConnection, final long userId, final String userName) throws SQLException
	{
		// prepare a SQL statement to be run on the database
		String sqlStatementString = "SELECT * FROM " + TABLE_USERS + " WHERE " + TABLE_USERS_COLUMN_ID + " = ?";
		PreparedStatement sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

		// prevent SQL injection by inserting data this way
		sqlStatement.setLong(1, userId);

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
			sqlStatementString = "INSERT INTO " + TABLE_USERS + " (" + TABLE_USERS_COLUMN_ID + ", " + TABLE_USERS_COLUMN_NAME + ") VALUES (?, ?)";
			sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

			// prevent SQL injection by inserting data this way
			sqlStatement.setLong(1, userId);
			sqlStatement.setString(2, userName);

			// run the SQL statement
			sqlStatement.executeUpdate();
		}

		closeSQLStatement(sqlStatement);
	}


	/**
	 * Query the database for a game's info.
	 * 
	 * @param sqlConnection
	 * Your existing database Connection object. Must already be connected, as
	 * this method makes no attempt at doing so.
	 * 
	 * @param gameId
	 * The ID of the game you're searching for.
	 * 
	 * @return
	 * The query's resulting ResultSet object. Could be empty or even null,
	 * check for that with the ResultSet's .next() method.
	 * 
	 * @throws SQLException
	 * If at some point there is some kind of connection error or query problem
	 * with the SQL database then this Exception will be thrown.
	 */
	public static ResultSet grabGamesInfo(final Connection sqlConnection, final String gameId) throws SQLException
	{
		// prepare a SQL statement to be run on the database
		final String sqlStatementString = "SELECT " + TABLE_GAMES_COLUMN_BOARD + " FROM " + TABLE_GAMES + " WHERE " + TABLE_GAMES_COLUMN_ID + " = ?";
		final PreparedStatement sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

		// prevent SQL injection by inserting data this way
		sqlStatement.setString(1, gameId);

		// run the SQL statement and acquire any return information
		final ResultSet sqlResult = sqlStatement.executeQuery();

		closeSQLStatement(sqlStatement);

		return sqlResult;
	}


	/**
	 * Query the database for a user who's ID matches the input's.
	 * 
	 * @param sqlConnection
	 * Your existing database Connection object. Must already be connected, as
	 * this method makes no attempt at doing so.
	 * 
	 * @param userId
	 * The ID of the user you're searching for as a long.
	 * 
	 * @return
	 * The name of the user that you queried for as a String. If the user could
	 * not be found then this method will return null.
	 * 
	 * @throws SQLException
	 * If at some point there is some kind of connection error or query problem
	 * with the SQL database then this Exception will be thrown.
	 */
	public static String grabUsersName(final Connection sqlConnection, final long userId) throws SQLException
	{
		String username = null;

		// prepare a SQL statement to be run on the MySQL database
		final String sqlStatementString = "SELECT " + TABLE_USERS_COLUMN_NAME + " FROM " + TABLE_USERS + " WHERE " + TABLE_USERS_COLUMN_ID + " = ?";
		final PreparedStatement sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

		// prevent SQL injection by inserting data this way
		sqlStatement.setLong(1, userId);

		// run the SQL statement and acquire any return information
		final ResultSet sqlResult = sqlStatement.executeQuery();

		if (sqlResult.next())
		// check to see that we got some SQL return data
		{
			// grab the user's name from the SQL query
			username = sqlResult.getString(TABLE_USERS_COLUMN_NAME);
		}

		closeSQLStatement(sqlStatement);

		return username;
	}


	/**
	 * Finds and then returns a user's reg_id. 
	 * 
	 * @param sqlConnection
	 * An existing connection to the database. This method will make no attempt
	 * to either open or close the connection.
	 * 
	 * @param userId
	 * ID of the user that you want to find a reg_id for.
	 * 
	 * @return
	 * Returns the regId of the user that you want as a String. If the user
	 * could not be found, null is returned.
	 * 
	 * @throws SQLException
	 * If at some point there is some kind of connection error or query problem
	 * with the SQL database then this Exception will be thrown.
	 */
	public static String grabUsersRegId(final Connection sqlConnection, final long userId) throws SQLException
	{
		String regId = null;

		// prepare a SQL statement to be run on the database
		final String sqlStatementString = "SELECT " + TABLE_USERS_COLUMN_REG_ID + " FROM " + TABLE_USERS + " WHERE " + TABLE_USERS_COLUMN_ID + " = ?";
		final PreparedStatement sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

		// prevent SQL injection by inserting data this way
		sqlStatement.setLong(1, userId);

		// run the SQL statement and acquire any return information
		final ResultSet sqlResult = sqlStatement.executeQuery();

		if (sqlResult.next())
		// user with specified id was found in the database
		{
			regId = sqlResult.getString(DatabaseUtilities.TABLE_USERS_COLUMN_REG_ID);
		}

		closeSQLStatement(sqlStatement);

		return regId;
	}


	/**
	 * Removes a given user's regId from the database. This just replaces the
	 * currently existing regId value with null.
	 * 
	 * @param sqlConnection
	 * An existing connection to the database. This method will make no attempt
	 * to either open or close the connection.
	 * 
	 * @param userId
	 * The user ID of the user who's regId needs to be removed.
	 * 
	 * @throws SQLException
	 * If at some point there is some kind of connection error or query problem
	 * with the SQL database then this Exception will be thrown.
	 */
	public static void removeUserRegId(final Connection sqlConnection, final long userId) throws SQLException
	{
		// prepare a SQL statement to be run on the database
		final String sqlStatementString = "UPDATE " + TABLE_USERS + " SET " + TABLE_USERS_COLUMN_REG_ID + " = null WHERE " + TABLE_USERS_COLUMN_ID + " = ?";
		final PreparedStatement sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

		// prevent SQL injection by inserting data this way
		sqlStatement.setLong(1, userId);

		// run the SQL statement
		sqlStatement.executeUpdate();

		closeSQLStatement(sqlStatement);
	}


	/**
	 * Updates a given user's regId in the database. This just replaces the
	 * currently existing regId value with the regId value that you specify
	 * here.
	 * 
	 * @param userRegId
	 * The given user's new regId.
	 * 
	 * @param sqlConnection
	 * An existing connection to the database. This method will make no attempt
	 * to either open or close the connection.
	 * 
	 * @param userId
	 * The user ID of the user's who's regId needs to be updated.
	 * 
	 * @throws SQLException
	 * If at some point there is some kind of connection error or query problem
	 * with the SQL database then this Exception will be thrown.
	 */
	public static void updateUserRegId(final Connection sqlConnection, final long userId, final String userRegId) throws SQLException
	{
		// prepare a SQL statement to be run on the database
		final String sqlStatementString = "UPDATE " + TABLE_USERS + " SET " + TABLE_USERS_COLUMN_REG_ID + " = ? WHERE " + TABLE_USERS_COLUMN_ID + " = ?";
		final PreparedStatement sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

		// prevent SQL injection by inserting user data this way
		sqlStatement.setString(1, userRegId);
		sqlStatement.setLong(2, userId);

		// run the SQL statement
		sqlStatement.executeUpdate();

		closeSQLStatement(sqlStatement);
	}


}
