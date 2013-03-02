package edu.selu.android.classygames;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.selu.android.classygames.utilities.DatabaseUtilities;
import edu.selu.android.classygames.utilities.Utilities;


public class GetGame extends HttpServlet
{


	private final static long serialVersionUID = 1L;


	private Connection sqlConnection;
	private PreparedStatement sqlStatement;
	private PrintWriter printWriter;
	private ResultSet sqlResult;

	private String param_gameId;




	public GetGame()
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
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		response.setContentType(Utilities.CONTENT_TYPE_JSON);
		printWriter = response.getWriter();

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
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_CONNECT + e.getMessage()));
			}
			catch (final Exception e)
			{
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_GENERIC + e.getMessage()));
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
		sqlConnection = DatabaseUtilities.acquireSQLConnection();

		// prepare a SQL statement to be run on the database
		final String sqlStatementString = "SELECT " + DatabaseUtilities.TABLE_GAMES_COLUMN_BOARD + " FROM " + DatabaseUtilities.TABLE_GAMES + " WHERE " + DatabaseUtilities.TABLE_GAMES_COLUMN_ID + " = ?";
		sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

		// prevent SQL injection by inserting data this way
		sqlStatement.setString(1, param_gameId);

		// run the SQL statement and acquire any return information
		sqlResult = sqlStatement.executeQuery();

		if (sqlResult.next())
		// game with specified id was found in the database, send the board's
		// data to the client
		{
			final String board = sqlResult.getString(DatabaseUtilities.TABLE_GAMES_COLUMN_BOARD);

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
