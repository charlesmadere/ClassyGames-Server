package edu.selu.android.classygames;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.selu.android.classygames.utilities.Utilities;


public class GetGames extends HttpServlet
{


	private final static long serialVersionUID = 1L;


	private Connection sqlConnection;
	private PreparedStatement sqlStatement;
	private PrintWriter printWriter;

	private String parameter_userId;

	private Long userId;




	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetGames()
	{
		super();
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		response.setContentType(Utilities.CONTENT_TYPE_JSON);
		printWriter = response.getWriter();
		printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_NOT_DETECTED));
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
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
				getGames();
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


	private void getGames()
	{
		try
		{
			sqlConnection = Utilities.getSQLConnection();

			// prepare a SQL statement to be run on the MySQL database
			final String sqlStatementString = "SELECT * FROM " + Utilities.DATABASE_TABLE_GAMES + " WHERE " + Utilities.DATABASE_TABLE_GAMES_COLUMN_FINISHED + " = ? AND (" + Utilities.DATABASE_TABLE_GAMES_COLUMN_USER_CREATOR + " = ? OR " + Utilities.DATABASE_TABLE_GAMES_COLUMN_USER_CHALLENGED + " = ?)";
			sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

			// prevent SQL injection by inserting data this way
			sqlStatement.setByte(1, Utilities.DATABASE_TABLE_GAMES_FINISHED_FALSE);
			sqlStatement.setLong(2, userId.longValue());
			sqlStatement.setLong(3, userId.longValue());

			// run the SQL statement and acquire any return information
			final ResultSet sqlResult = sqlStatement.executeQuery();

			if (sqlResult.next())
			// check to see that we got some SQL return data
			{
				createReturnGameData(sqlResult);
			}
			else
			// we did not get any SQL return data
			{
				printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_NO_ACTIVE_GAMES));
			}
		}
		catch (final SQLException e)
		{
			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_CONNECT));
		}
		finally
		{
			Utilities.closeSQL(sqlConnection, sqlStatement);
		}
	}


	private void createReturnGameData(final ResultSet sqlResult) throws SQLException
	{
		// TODO
		// convert this json making code into the org.json way of doing JSON

		final Map<String, Object> jsonData = new LinkedHashMap<String, Object>();
		final List<Map<String, Object>> turnYours = new LinkedList<Map<String, Object>>();
		final List<Map<String, Object>> turnTheirs = new LinkedList<Map<String, Object>>();

		do
		// loop through all of the SQL return data
		{
			final String database_gameId = sqlResult.getString(Utilities.DATABASE_TABLE_GAMES_COLUMN_ID);
			final Long database_userCreatorId = sqlResult.getLong(Utilities.DATABASE_TABLE_GAMES_COLUMN_USER_CREATOR);
			final Long database_userChallengedId = sqlResult.getLong(Utilities.DATABASE_TABLE_GAMES_COLUMN_USER_CHALLENGED);
			final Timestamp database_lastMove = sqlResult.getTimestamp(Utilities.DATABASE_TABLE_GAMES_COLUMN_LAST_MOVE);

			// initialize a JSONObject. All of the current game's data will be stored here. At the end of this
			// loop iteration this JSONObject will be added to one of the above JSONArrays
			final Map<String, Object> game = new LinkedHashMap<String, Object>();

			if (database_userCreatorId.longValue() == userId.longValue())
			{
				game.put(Utilities.POST_DATA_ID, database_userChallengedId.longValue());
				game.put(Utilities.POST_DATA_NAME, Utilities.grabUsersName(sqlConnection, database_userChallengedId));
			}
			else
			{
				game.put(Utilities.POST_DATA_ID, database_userCreatorId.longValue());
				game.put(Utilities.POST_DATA_NAME, Utilities.grabUsersName(sqlConnection, database_userCreatorId));
			}

			game.put(Utilities.POST_DATA_GAME_ID, database_gameId);
			game.put(Utilities.POST_DATA_LAST_MOVE, database_lastMove.getTime() / 1000);

			switch (sqlResult.getByte(Utilities.DATABASE_TABLE_GAMES_COLUMN_TURN))
			{
				case Utilities.DATABASE_TABLE_GAMES_TURN_CREATOR:
				// it's the creator's turn
					if (database_userCreatorId.longValue() == userId.longValue())
					{
						turnYours.add(game);
					}
					else
					{
						turnTheirs.add(game);
					}
					break;

				case Utilities.DATABASE_TABLE_GAMES_TURN_CHALLENGED:
				// it's the challenger's turn
					if (database_userChallengedId.longValue() == userId.longValue())
					{
						turnYours.add(game);
					}
					else
					{
						turnTheirs.add(game);
					}
					break;
			}
		}
		while (sqlResult.next());

		jsonData.put(Utilities.POST_DATA_TURN_YOURS, turnYours);
		jsonData.put(Utilities.POST_DATA_TURN_THEIRS, turnTheirs);

		printWriter.write(Utilities.makePostDataSuccess(jsonData));
	}


}
