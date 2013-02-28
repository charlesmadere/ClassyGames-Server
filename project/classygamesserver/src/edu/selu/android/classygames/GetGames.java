package edu.selu.android.classygames;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.selu.android.classygames.utilities.DatabaseUtilities;
import edu.selu.android.classygames.utilities.Utilities;


public class GetGames extends HttpServlet
{


	private final static long serialVersionUID = 1L;


	private Connection sqlConnection;
	private PreparedStatement sqlStatement;
	private PrintWriter printWriter;
	private ResultSet sqlResult;

	private String parameter_userId;

	private Long userId;




	public GetGames()
	{
		super();
	}


	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		response.setContentType(Utilities.CONTENT_TYPE_JSON);
		printWriter = response.getWriter();
		printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_NOT_DETECTED));
	}


	@Override
	protected void doPost(final HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		response.setContentType(Utilities.CONTENT_TYPE_JSON);
		printWriter = response.getWriter();

		parameter_userId = request.getParameter(Utilities.POST_DATA_ID);

		if (Utilities.verifyValidString(parameter_userId))
		// check inputs for validity
		{
			userId = Long.valueOf(parameter_userId);

			if (Utilities.verifyValidLong(userId))
			// check inputs for validity
			{
				try
				{
					getGames();
				}
				catch (final JSONException e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_JSON_EXCEPTION));
				}
				catch (final SQLException e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_CONNECT));
				}
				catch (final Exception e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_JDBC_DRIVER_COULD_NOT_LOAD));
				}
				finally
				{
					DatabaseUtilities.closeSQL(sqlConnection, sqlStatement);
				}
			}
			else
			{
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_MALFORMED));
			}
		}
		else
		{
			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_MALFORMED));
		}
	}


	/**
	 * Runs the meat of this servlet's code.
	 * 
	 * @throws JSONException
	 * If at some point the JSON data that this method tries to create has an
	 * issue then this Exception will be thrown.
	 * 
	 * @throws SQLException
	 * If at some point there is some kind of connection error or query problem
	 * with the SQL database then this Exception will be thrown.
	 * 
	 * @throws Exception
	 * If the JDBC driver could not be loaded then this Exception will be
	 * thrown.
	 */
	private void getGames() throws JSONException, SQLException, Exception
	{
		sqlConnection = DatabaseUtilities.acquireSQLConnection();

		// prepare a SQL statement to be run on the MySQL database
		final String sqlStatementString = "SELECT * FROM " + DatabaseUtilities.TABLE_GAMES + " WHERE " + DatabaseUtilities.TABLE_GAMES_COLUMN_FINISHED + " = ? AND (" + DatabaseUtilities.TABLE_GAMES_COLUMN_USER_CREATOR + " = ? OR " + DatabaseUtilities.TABLE_GAMES_COLUMN_USER_CHALLENGED + " = ?)";
		sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

		// prevent SQL injection by inserting data this way
		sqlStatement.setByte(1, DatabaseUtilities.TABLE_GAMES_FINISHED_FALSE);
		sqlStatement.setLong(2, userId.longValue());
		sqlStatement.setLong(3, userId.longValue());

		// run the SQL statement and acquire any return information
		sqlResult = sqlStatement.executeQuery();

		if (sqlResult.next())
		// check to see that we got some SQL return data
		{
			createGamesListData();
		}
		else
		// we did not get any SQL return data
		{
			printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_NO_ACTIVE_GAMES));
		}
	}


	/**
	 * Creates a big JSONObject that represents the user's games list.
	 * 
	 * @throws JSONException
	 * If something weird happened when creating JSON data then this exception
	 * will be thrown.
	 * 
	 * @throws SQLException
	 * If something weird happens with the given SQL database query result then
	 * this exception will be thrown.
	 */
	private void createGamesListData() throws JSONException, SQLException
	{
		final JSONObject gamesList = new JSONObject();
		final JSONArray turnYours = new JSONArray();
		final JSONArray turnTheirs = new JSONArray();

		do
		// loop through all of the SQL return data
		{
			final String database_gameId = sqlResult.getString(DatabaseUtilities.TABLE_GAMES_COLUMN_ID);
			final long database_userCreatorId = sqlResult.getLong(DatabaseUtilities.TABLE_GAMES_COLUMN_USER_CREATOR);
			final long database_userChallengedId = sqlResult.getLong(DatabaseUtilities.TABLE_GAMES_COLUMN_USER_CHALLENGED);
			final byte database_gameType = sqlResult.getByte(DatabaseUtilities.TABLE_GAMES_COLUMN_GAME_TYPE);
			final Timestamp database_lastMove = sqlResult.getTimestamp(DatabaseUtilities.TABLE_GAMES_COLUMN_LAST_MOVE);

			// Initialize a JSONObject. All of the current game's data will be
			// stored here. At the end of this loop iteration this JSONObject
			// will be added to one of the above JSONArrays.
			final JSONObject game = new JSONObject();

			if (database_userCreatorId == userId.longValue())
			{
				game.put(Utilities.POST_DATA_ID, database_userChallengedId);
				game.put(Utilities.POST_DATA_NAME, DatabaseUtilities.grabUsersName(sqlConnection, database_userChallengedId));
			}
			else
			{
				game.put(Utilities.POST_DATA_ID, database_userCreatorId);
				game.put(Utilities.POST_DATA_NAME, DatabaseUtilities.grabUsersName(sqlConnection, database_userCreatorId));
			}

			game.put(Utilities.POST_DATA_GAME_ID, database_gameId);
			game.put(Utilities.POST_DATA_GAME_TYPE, database_gameType);
			game.put(Utilities.POST_DATA_LAST_MOVE, database_lastMove.getTime() / 1000);

			switch (sqlResult.getByte(DatabaseUtilities.TABLE_GAMES_COLUMN_TURN))
			{
				case DatabaseUtilities.TABLE_GAMES_TURN_CREATOR:
				// it's the creator's turn
					if (database_userCreatorId == userId.longValue())
					{
						turnYours.put(game);
					}
					else
					{
						turnTheirs.put(game);
					}
					break;

				case DatabaseUtilities.TABLE_GAMES_TURN_CHALLENGED:
				// it's the challenger's turn
					if (database_userChallengedId == userId.longValue())
					{
						turnYours.put(game);
					}
					else
					{
						turnTheirs.put(game);
					}
					break;
			}
		}
		while (sqlResult.next());

		gamesList.put(Utilities.POST_DATA_TURN_YOURS, turnYours);
		gamesList.put(Utilities.POST_DATA_TURN_THEIRS, turnTheirs);

		printWriter.write(Utilities.makePostDataSuccess(gamesList));
	}


}
