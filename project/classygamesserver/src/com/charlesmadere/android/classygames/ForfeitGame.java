package com.charlesmadere.android.classygames;


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

import com.charlesmadere.android.classygames.utilities.DatabaseUtilities;
import com.charlesmadere.android.classygames.utilities.GCMUtilities;
import com.charlesmadere.android.classygames.utilities.Utilities;


public class ForfeitGame extends HttpServlet
{


	private final static long serialVersionUID = 1L;


	private Connection sqlConnection;
	private PreparedStatement sqlStatement;
	private PrintWriter printWriter;

	private String param_userChallengedId;
	private String param_userChallengedName;
	private String param_userCreatorId;
	private String param_gameId;

	private Long userChallengedId;
	private Long userCreatorId;


	public ForfeitGame()
	{
		super();
	}


	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType(Utilities.CONTENT_TYPE_JSON);
		printWriter = response.getWriter();
		printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_NOT_DETECTED));
	}


	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType(Utilities.CONTENT_TYPE_JSON);
		printWriter = response.getWriter();

		param_userChallengedId = request.getParameter(Utilities.POST_DATA_USER_CHALLENGED);
		param_userChallengedName = request.getParameter(Utilities.POST_DATA_NAME);
		param_userCreatorId = request.getParameter(Utilities.POST_DATA_USER_CREATOR);
		param_gameId = request.getParameter(Utilities.POST_DATA_GAME_ID);

		if (Utilities.verifyValidStrings(param_userChallengedId, param_userChallengedName, param_userCreatorId, param_gameId))
		// check inputs for validity
		{
			userChallengedId = Long.valueOf(param_userChallengedId);
			userCreatorId = Long.valueOf(param_userCreatorId);

			if (Utilities.verifyValidLongs(userChallengedId, userCreatorId))
			{
				try
				{
					forfeitGame();
				}
				catch (final IOException e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_GCM_FAILED_TO_SEND));
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
			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_EMPTY));
		}
	}


	/**
	 * Runs the meat of this servlet's code.
	 * 
	 * @throws IOException
	 * An IOException could be thrown when the GCM message is attempted to be
	 * sent.
	 * 
	 * @throws SQLException
	 * If at some point there is some kind of connection error or query problem
	 * with the SQL database then this Exception will be thrown.
	 * 
	 * @throws Exception
	 * If the JDBC driver could not be loaded then this Exception will be
	 * thrown.
	 */
	private void forfeitGame() throws IOException, SQLException, Exception
	{
		sqlConnection = DatabaseUtilities.acquireSQLConnection();
		DatabaseUtilities.ensureUserExistsInDatabase(sqlConnection, userChallengedId.longValue(), param_userChallengedName);

		final ResultSet sqlResult = DatabaseUtilities.grabGamesInfo(sqlConnection, param_gameId);

		if (sqlResult != null && sqlResult.next())
		{
			if (sqlResult.getByte(DatabaseUtilities.TABLE_GAMES_COLUMN_FINISHED) == DatabaseUtilities.TABLE_GAMES_FINISHED_FALSE)
			// make sure that the game has not been finished
			{
				final String sqlStatementString = "UPDATE " + DatabaseUtilities.TABLE_GAMES + " SET " + DatabaseUtilities.TABLE_GAMES_COLUMN_FINISHED + " = ? WHERE " + DatabaseUtilities.TABLE_GAMES_COLUMN_ID + " = ?";
				sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

				// prevent SQL injection by inserting data this way
				sqlStatement.setByte(1, DatabaseUtilities.TABLE_GAMES_FINISHED_TRUE);
				sqlStatement.setString(2, param_gameId);

				// run the SQL statement
				sqlStatement.executeUpdate();

				GCMUtilities.sendMessage(sqlConnection, param_gameId, userCreatorId, userChallengedId, Byte.valueOf(Utilities.POST_DATA_GAME_TYPE_CHECKERS), Byte.valueOf(Utilities.BOARD_WIN));
				GCMUtilities.sendMessage(sqlConnection, param_gameId, userChallengedId, userCreatorId, Byte.valueOf(Utilities.POST_DATA_GAME_TYPE_CHECKERS), Byte.valueOf(Utilities.BOARD_LOSE));

				printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_GENERIC));
			}
			else
			{
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_GAME_IS_ALREADY_OVER));
			}
		}
		else
		{
			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_FIND_GAME_WITH_SPECIFIED_ID));
		}
	}


}
