package com.charlesmadere.android.classygames;


import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.charlesmadere.android.classygames.models.games.GenericBoard;
import com.charlesmadere.android.classygames.utilities.DB;
import com.charlesmadere.android.classygames.utilities.DBConstants;
import com.charlesmadere.android.classygames.utilities.GCMUtilities;
import com.charlesmadere.android.classygames.utilities.GameUtilities;
import com.charlesmadere.android.classygames.utilities.Utilities;


public final class SkipMove extends Servlet
{


	private final static long serialVersionUID = 1L;


	private String param_userChallengedId;
	private String param_userChallengedName;
	private String param_userCreatorId;
	private String param_gameId;

	private Long userChallengedId;
	private Long userCreatorId;

	private GenericBoard board;




	public SkipMove()
	{
		super();
	}


	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
		throws ServletException, IOException
	{
		prepare(response);
		param_userChallengedId = request.getParameter(Utilities.POST_DATA_USER_CHALLENGED);
		param_userChallengedName = request.getParameter(Utilities.POST_DATA_NAME);
		param_userCreatorId = request.getParameter(Utilities.POST_DATA_USER_CREATOR);
		param_gameId = request.getParameter(Utilities.POST_DATA_GAME_ID);

		if (Utilities.verifyValidStrings(param_userChallengedId, param_userChallengedName, param_userCreatorId, param_gameId))
		{
			userChallengedId = Long.valueOf(param_userChallengedId);
			userCreatorId = Long.valueOf(param_userCreatorId);

			if (Utilities.verifyValidLong(userCreatorId))
			{
				try
				{
					skipMove();
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
	private void skipMove() throws IOException, SQLException, Exception
	{
		DB.open();
		DBConstants.ensureUserExistsInDatabase(sqlConnection, userChallengedId.longValue(), param_userChallengedName);

		sqlResult = DBConstants.grabGamesInfo(sqlConnection, param_gameId);

		if (sqlResult != null && sqlResult.next())
		{
			if (sqlResult.getByte(DBConstants.TABLE_GAMES_COLUMN_FINISHED) == DBConstants.TABLE_GAMES_FINISHED_FALSE)
			// make sure that the game has not been finished
			{
				final long db_userChallengedId = sqlResult.getLong(DBConstants.TABLE_GAMES_COLUMN_USER_CHALLENGED);
				final long db_userCreatorId = sqlResult.getLong(DBConstants.TABLE_GAMES_COLUMN_USER_CREATOR);
				final Byte db_gameType = Byte.valueOf(sqlResult.getByte(DBConstants.TABLE_GAMES_COLUMN_GAME_TYPE));
				final byte db_turn = sqlResult.getByte(DBConstants.TABLE_GAMES_COLUMN_TURN);

				if ((userCreatorId.longValue() == db_userChallengedId && db_turn == DBConstants.TABLE_GAMES_TURN_CHALLENGED)
					|| (userCreatorId.longValue() == db_userCreatorId && db_turn == DBConstants.TABLE_GAMES_TURN_CREATOR))
				{
					final String db_oldBoard = sqlResult.getString(DBConstants.TABLE_GAMES_COLUMN_BOARD);

					board = GameUtilities.newGame(db_oldBoard, db_gameType.byteValue());
					board.flipTeams();
					final JSONObject boardJSON = board.makeJSON();
					final String boardJSONString = boardJSON.toString();

					// prepare a SQL statement to be run on the database
					final String sqlStatementString = "UPDATE " + DBConstants.TABLE_GAMES + " SET " + DBConstants.TABLE_GAMES_COLUMN_BOARD + " = ?, " + DBConstants.TABLE_GAMES_COLUMN_TURN + " = ?, "  + DBConstants.TABLE_GAMES_COLUMN_LAST_MOVE + " = NOW() WHERE " + DBConstants.TABLE_GAMES_COLUMN_ID + " = ?";
					sqlStatement = DB.connection.prepareStatement(sqlStatementString);

					// prevent SQL injection by inserting data this way
					sqlStatement.setString(1, boardJSONString);

					if (db_turn == DBConstants.TABLE_GAMES_TURN_CHALLENGED)
					{
						sqlStatement.setByte(2, DBConstants.TABLE_GAMES_TURN_CREATOR);
					}
					else if (db_turn == DBConstants.TABLE_GAMES_TURN_CREATOR)
					{
						sqlStatement.setByte(2, DBConstants.TABLE_GAMES_TURN_CHALLENGED);
					}

					sqlStatement.setString(3, param_gameId);

					// run the SQL statement
					sqlStatement.executeUpdate();

					GCMUtilities.sendMessage(sqlConnection, param_gameId, userCreatorId, userChallengedId, db_gameType, Byte.valueOf(Utilities.BOARD_NEW_MOVE));
					printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_MOVE_ADDED_TO_DATABASE));
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
