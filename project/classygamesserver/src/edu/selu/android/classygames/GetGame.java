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

import edu.selu.android.classygames.utilities.Utilities;


public class GetGame extends HttpServlet
{


	private final static long serialVersionUID = 1L;


	private Connection sqlConnection;
	private PreparedStatement sqlStatement;
	private PrintWriter printWriter;

	private String parameter_gameId;




	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public GetGame()
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
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		response.setContentType(Utilities.CONTENT_TYPE_JSON);
		printWriter = response.getWriter();

		parameter_gameId = request.getParameter(Utilities.POST_DATA_ID);

		if (Utilities.verifyValidString(parameter_gameId))
		// check inputs for validity
		{
			getGame();
		}
		else
		{
			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_MALFORMED));
		}
	}


	private void getGame()
	{
		try
		{
			sqlConnection = Utilities.getSQLConnection();

			// prepare a SQL statement to be run on the database
			final String sqlStatementString = "SELECT " + Utilities.DATABASE_TABLE_GAMES_COLUMN_BOARD + " FROM " + Utilities.DATABASE_TABLE_GAMES + " WHERE " + Utilities.DATABASE_TABLE_GAMES_COLUMN_ID + " = ?";
			sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

			// prevent SQL injection by inserting data this way
			sqlStatement.setString(1, parameter_gameId);

			// run the SQL statement and acquire any return information
			final ResultSet sqlResult = sqlStatement.executeQuery();

			if (sqlResult.next())
			// game with specified id was found in the database, send the board's data to the client
			{
				final String board = sqlResult.getString(Utilities.DATABASE_TABLE_GAMES_COLUMN_BOARD);

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
			// we could not find a game with specified id in the database. this should never happen
			{
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_FIND_GAME_WITH_SPECIFIED_ID));
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


}
