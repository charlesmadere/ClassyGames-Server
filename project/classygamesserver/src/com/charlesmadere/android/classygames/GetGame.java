package com.charlesmadere.android.classygames;


import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.charlesmadere.android.classygames.utilities.DB;
import com.charlesmadere.android.classygames.utilities.DBConstants;
import com.charlesmadere.android.classygames.utilities.Utilities;


public final class GetGame extends Servlet
{


	private final static long serialVersionUID = 1L;


	private String param_gameId;




	public GetGame()
	{
		super();
	}




	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
		throws IOException, ServletException
	{
		prepare(response);
		param_gameId = request.getParameter(Utilities.POST_DATA_ID);

		if (Utilities.verifyValidString(param_gameId))
		// check inputs for validity
		{
			try
			{
				getGame();
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
			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_EMPTY));
		}
	}


	/**
	 * Runs the meat of this servlet's code.
	 * 
	 * @throws SQLException
	 * If at some point there is some kind of connection error or query problem
	 * with the SQL database then this Exception will be thrown.
	 * 
	 * @throws Exception
	 * If the JDBC driver could not be loaded then this Exception will be
	 * thrown.
	 */
	private void getGame() throws SQLException, Exception
	{
		DB.open();

		// prepare a SQL statement to be run on the database
		final String sqlStatementString = "SELECT " + DBConstants.TABLE_GAMES_COLUMN_BOARD + " FROM " + DBConstants.TABLE_GAMES + " WHERE " + DBConstants.TABLE_GAMES_COLUMN_ID + " = ?";
		sqlStatement = DB.connection.prepareStatement(sqlStatementString);

		// prevent SQL injection by inserting data this way
		sqlStatement.setString(1, param_gameId);

		// run the SQL statement and acquire any return information
		sqlResult = sqlStatement.executeQuery();

		if (sqlResult.next())
		// game with specified id was found in the database, send the board's
		// data to the client
		{
			final String board = sqlResult.getString(DBConstants.TABLE_GAMES_COLUMN_BOARD);

			if (Utilities.verifyValidString(board))
			// return the board's data
			{
				printWriter.write(Utilities.makePostDataSuccess(board));
			}
			else
			{
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_GET_BOARD_DATA));
			}
		}
		else
		// We could not find a game with specified id in the database. This
		// should never happen.
		{
			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_FIND_GAME_WITH_SPECIFIED_ID));
		}
	}


}
