package com.charlesmadere.android.classygames;


import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.charlesmadere.android.classygames.utilities.DB;
import com.charlesmadere.android.classygames.utilities.DBConstants;
import com.charlesmadere.android.classygames.utilities.Utilities;


public final class GetGames extends Servlet
{


	private final static long serialVersionUID = 1L;


	private String param_userId;

	private Long userId;




	public GetGames()
	{
		super();
	}


	@Override
	protected void doPost(final HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
	{
		prepare(response);
		param_userId = request.getParameter(Utilities.POST_DATA_ID);

		if (Utilities.verifyValidString(param_userId))
		// check inputs for validity
		{
			userId = Long.valueOf(param_userId);

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
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_GENERIC));
				}
				finally
				{
					DB.close(sqlStatement);
					DB.close();
				}
			}
			else
			{
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_MALFORMED));
			}
		}
		else
		{
			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_EMPTY));
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
		DB.open();

		// prepare a SQL statement to be run on the MySQL database
		final String sqlStatementString = "SELECT * FROM " + DBConstants.TABLE_GAMES + " WHERE " + DBConstants.TABLE_GAMES_COLUMN_FINISHED + " = ? AND (" + DBConstants.TABLE_GAMES_COLUMN_USER_CREATOR + " = ? OR " + DBConstants.TABLE_GAMES_COLUMN_USER_CHALLENGED + " = ?)";
		sqlStatement = DB.connection.prepareStatement(sqlStatementString);

		// prevent SQL injection by inserting data this way
		sqlStatement.setByte(1, DBConstants.TABLE_GAMES_FINISHED_FALSE);
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
			final String db_gameId = sqlResult.getString(DBConstants.TABLE_GAMES_COLUMN_ID);
			final long db_userCreatorId = sqlResult.getLong(DBConstants.TABLE_GAMES_COLUMN_USER_CREATOR);
			final long db_userChallengedId = sqlResult.getLong(DBConstants.TABLE_GAMES_COLUMN_USER_CHALLENGED);
			final byte db_gameType = sqlResult.getByte(DBConstants.TABLE_GAMES_COLUMN_GAME_TYPE);
			final Timestamp db_lastMove = sqlResult.getTimestamp(DBConstants.TABLE_GAMES_COLUMN_LAST_MOVE);

			// Initialize a JSONObject. All of the current game's data will be
			// stored here. At the end of this loop iteration this JSONObject
			// will be added to one of the above JSONArrays.
			final JSONObject game = new JSONObject();

			if (db_userCreatorId == userId.longValue())
			{
				game.put(Utilities.POST_DATA_ID, db_userChallengedId);
				game.put(Utilities.POST_DATA_NAME, DBConstants.grabUsersName(sqlConnection, db_userChallengedId));
			}
			else
			{
				game.put(Utilities.POST_DATA_ID, db_userCreatorId);
				game.put(Utilities.POST_DATA_NAME, DBConstants.grabUsersName(sqlConnection, db_userCreatorId));
			}

			game.put(Utilities.POST_DATA_GAME_ID, db_gameId);
			game.put(Utilities.POST_DATA_GAME_TYPE, db_gameType);
			game.put(Utilities.POST_DATA_LAST_MOVE, db_lastMove.getTime() / 1000);

			switch (sqlResult.getByte(DBConstants.TABLE_GAMES_COLUMN_TURN))
			{
				case DBConstants.TABLE_GAMES_TURN_CREATOR:
				// it's the creator's turn
					if (db_userCreatorId == userId.longValue())
					{
						turnYours.put(game);
					}
					else
					{
						turnTheirs.put(game);
					}
					break;

				case DBConstants.TABLE_GAMES_TURN_CHALLENGED:
				// it's the challenger's turn
					if (db_userChallengedId == userId.longValue())
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
