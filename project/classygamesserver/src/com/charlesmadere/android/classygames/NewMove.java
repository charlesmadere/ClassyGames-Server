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

import org.json.JSONException;
import org.json.JSONObject;

import com.charlesmadere.android.classygames.games.GenericBoard;
import com.charlesmadere.android.classygames.utilities.DatabaseUtilities;
import com.charlesmadere.android.classygames.utilities.GCMUtilities;
import com.charlesmadere.android.classygames.utilities.GameUtilities;
import com.charlesmadere.android.classygames.utilities.Utilities;


public class NewMove extends HttpServlet
{


	private final static long serialVersionUID = 1L;


	private Connection sqlConnection;
	private PreparedStatement sqlStatement;
	private PrintWriter printWriter;
	private ResultSet sqlResult;

	private String param_userChallengedId;
	private String param_userChallengedName;
	private String param_userCreatorId;
	private String param_gameId;
	private String param_board;

	private Long userChallengedId;
	private Long userCreatorId;

	private GenericBoard board;




	public NewMove()
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

		param_userChallengedId = request.getParameter(Utilities.POST_DATA_USER_CHALLENGED);
		param_userChallengedName = request.getParameter(Utilities.POST_DATA_NAME);
		param_userCreatorId = request.getParameter(Utilities.POST_DATA_USER_CREATOR);
		param_gameId = request.getParameter(Utilities.POST_DATA_GAME_ID);
		param_board = request.getParameter(Utilities.POST_DATA_BOARD);

		if (Utilities.verifyValidStrings(param_userChallengedId, param_userChallengedName, param_userCreatorId, param_gameId, param_board))
		// check inputs for validity
		{
			userChallengedId = Long.valueOf(param_userChallengedId);
			userCreatorId = Long.valueOf(param_userCreatorId);

			if (Utilities.verifyValidLongs(userChallengedId, userCreatorId))
			// check inputs for validity
			{
				try
				{
					newMove();
				}
				catch (final IOException e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_GCM_FAILED_TO_SEND));
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
	private void newMove() throws IOException, JSONException, SQLException, Exception
	{
		sqlConnection = DatabaseUtilities.acquireSQLConnection();
		DatabaseUtilities.ensureUserExistsInDatabase(sqlConnection, userChallengedId.longValue(), param_userChallengedName);

		sqlResult = DatabaseUtilities.grabGamesInfo(sqlConnection, param_gameId);

		if (sqlResult != null && sqlResult.next())
		{
			if (sqlResult.getByte(DatabaseUtilities.TABLE_GAMES_COLUMN_FINISHED) == DatabaseUtilities.TABLE_GAMES_FINISHED_FALSE)
			// make sure that the game has not been finished
			{
				final long db_userChallengedId = sqlResult.getLong(DatabaseUtilities.TABLE_GAMES_COLUMN_USER_CHALLENGED);
				final long db_userCreatorId = sqlResult.getLong(DatabaseUtilities.TABLE_GAMES_COLUMN_USER_CREATOR);
				final Byte db_gameType = Byte.valueOf(sqlResult.getByte(DatabaseUtilities.TABLE_GAMES_COLUMN_GAME_TYPE));
				final byte db_turn = sqlResult.getByte(DatabaseUtilities.TABLE_GAMES_COLUMN_TURN);

				if ((userCreatorId.longValue() == db_userChallengedId && db_turn == DatabaseUtilities.TABLE_GAMES_TURN_CHALLENGED)
					|| (userCreatorId.longValue() == db_userCreatorId && db_turn == DatabaseUtilities.TABLE_GAMES_TURN_CREATOR))
				{
					final String db_oldBoard = sqlResult.getString(DatabaseUtilities.TABLE_GAMES_COLUMN_BOARD);

					board = GameUtilities.newGame(db_oldBoard, db_gameType.byteValue());
					final JSONObject param_boardJSON = new JSONObject(param_board);
					final Byte boardValidationResult = Byte.valueOf(board.checkValidity(param_boardJSON));

					if (boardValidationResult.byteValue() == Utilities.BOARD_NEW_MOVE || boardValidationResult.byteValue() == Utilities.BOARD_WIN)
					{
						board = GameUtilities.newGame(param_board, db_gameType.byteValue());
						board.flipTeams();
						final JSONObject newBoardJSON = board.makeJSON();
						final String newBoardJSONString = newBoardJSON.toString();

						// prepare a SQL statement to be run on the database
						final String sqlStatementString = "UPDATE " + DatabaseUtilities.TABLE_GAMES + " SET " + DatabaseUtilities.TABLE_GAMES_COLUMN_BOARD + " = ?, " + DatabaseUtilities.TABLE_GAMES_COLUMN_TURN + " = ?, " + DatabaseUtilities.TABLE_GAMES_COLUMN_FINISHED + " = ?, " + DatabaseUtilities.TABLE_GAMES_COLUMN_LAST_MOVE + " = NOW() WHERE " + DatabaseUtilities.TABLE_GAMES_COLUMN_ID + " = ?";
						sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

						// prevent SQL injection by inserting data this way
						sqlStatement.setString(1, newBoardJSONString);

						if (db_turn == DatabaseUtilities.TABLE_GAMES_TURN_CHALLENGED)
						{
							sqlStatement.setByte(2, DatabaseUtilities.TABLE_GAMES_TURN_CREATOR);
						}
						else if (db_turn == DatabaseUtilities.TABLE_GAMES_TURN_CREATOR)
						{
							sqlStatement.setByte(2, DatabaseUtilities.TABLE_GAMES_TURN_CHALLENGED);
						}

						if (boardValidationResult.byteValue() == Utilities.BOARD_WIN)
						{
							sqlStatement.setByte(3, DatabaseUtilities.TABLE_GAMES_FINISHED_TRUE);
						}
						else if (boardValidationResult.byteValue() == Utilities.BOARD_NEW_MOVE)
						{
							sqlStatement.setByte(3, DatabaseUtilities.TABLE_GAMES_FINISHED_FALSE);
						}

						sqlStatement.setString(4, param_gameId);

						// run the SQL statement
						sqlStatement.executeUpdate();

						if (boardValidationResult.byteValue() == Utilities.BOARD_WIN)
						{
							GCMUtilities.sendMessages(sqlConnection, param_gameId, userCreatorId, userChallengedId, boardValidationResult, db_gameType, param_userChallengedName);
						}
						else if (boardValidationResult.byteValue() == Utilities.BOARD_NEW_MOVE)
						{
							GCMUtilities.sendMessage(sqlConnection, param_gameId, userCreatorId, userChallengedId, db_gameType, boardValidationResult);
						}

						printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_MOVE_ADDED_TO_DATABASE));
					}
					else
					{
						printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_ERROR_BOARD_INVALID));
					}
				}
				else
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_ITS_NOT_YOUR_TURN));
				}
			}
			else
			// we are trying to add a new move to a game that is already finished. this should never happen
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
