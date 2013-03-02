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

import org.json.JSONException;
import org.json.JSONObject;

import edu.selu.android.classygames.games.GenericBoard;
import edu.selu.android.classygames.utilities.DatabaseUtilities;
import edu.selu.android.classygames.utilities.GCMUtilities;
import edu.selu.android.classygames.utilities.GameUtilities;
import edu.selu.android.classygames.utilities.Utilities;


public class SkipMove extends HttpServlet
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

	private Long userChallengedId;
	private Long userCreatorId;

	private GenericBoard board;

	int line = 0;




	public SkipMove()
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

			if (Utilities.verifyValidLong(userCreatorId))
			// check inputs for validity
			{
				try
				{
					line = 90;
					skipMove();
					line = 92;
				}
				catch (final IOException e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_GCM_FAILED_TO_SEND + " ~ " + line + " ~ " + e.getMessage()));
				}
				catch (final SQLException e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_CONNECT + " ~ " + line + " ~ " + e.getMessage()));
				}
				catch (final Exception e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_GENERIC + " ~ " + line + " ~ " + e.getMessage()));
				}
				finally
				{
					DatabaseUtilities.closeSQL(sqlConnection, sqlStatement);
				}
			}
			else
			{
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_EMPTY));
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
		line = 144;
		sqlConnection = DatabaseUtilities.acquireSQLConnection();
		line = 146;
		DatabaseUtilities.ensureUserExistsInDatabase(sqlConnection, userChallengedId.longValue(), param_userChallengedName);
		line = 148;

		sqlResult = DatabaseUtilities.grabGamesInfo(sqlConnection, param_gameId);
		line = 151;

		if (sqlResult != null && sqlResult.next())
		{
			line = 155;
			if (sqlResult.getByte(DatabaseUtilities.TABLE_GAMES_COLUMN_FINISHED) == DatabaseUtilities.TABLE_GAMES_FINISHED_FALSE)
			// make sure that the game has not been finished
			{
				line = 159;
				final long db_userChallengedId = sqlResult.getLong(DatabaseUtilities.TABLE_GAMES_COLUMN_USER_CHALLENGED);
				line = 161;
				final long db_userCreatorId = sqlResult.getLong(DatabaseUtilities.TABLE_GAMES_COLUMN_USER_CREATOR);
				line = 163;
				final Byte db_gameType = Byte.valueOf(sqlResult.getByte(DatabaseUtilities.TABLE_GAMES_COLUMN_GAME_TYPE));
				line = 165;
				final byte db_turn = sqlResult.getByte(DatabaseUtilities.TABLE_GAMES_COLUMN_TURN);
				line = 167;

				if ((userCreatorId.longValue() == db_userChallengedId && db_turn == DatabaseUtilities.TABLE_GAMES_TURN_CHALLENGED)
					|| (userCreatorId.longValue() == db_userCreatorId && db_turn == DatabaseUtilities.TABLE_GAMES_TURN_CREATOR))
				{
					line = 172;
					final String db_oldBoard = sqlResult.getString(DatabaseUtilities.TABLE_GAMES_COLUMN_BOARD);
					line = 174;

					board = GameUtilities.newGame(db_oldBoard, db_gameType.byteValue());
					line = 177;
					board.flipTeams();
					line = 179;

					final JSONObject boardJSON = board.makeJSON();
					line = 182;
					final String boardJSONString = boardJSON.toString();
					line = 184;

					// prepare a SQL statement to be run on the database
					final String sqlStatementString = "UPDATE " + DatabaseUtilities.TABLE_GAMES + " SET " + DatabaseUtilities.TABLE_GAMES_COLUMN_BOARD + " = ?, " + DatabaseUtilities.TABLE_GAMES_COLUMN_TURN + " = ?, "  + DatabaseUtilities.TABLE_GAMES_COLUMN_LAST_MOVE + " = NOW() WHERE " + DatabaseUtilities.TABLE_GAMES_COLUMN_ID + " = ?";
					line = 188;
					sqlStatement = sqlConnection.prepareStatement(sqlStatementString);
					line = 190;

					// prevent SQL injection by inserting data this way
					sqlStatement.setString(1, boardJSONString);
					line = 194;

					if (db_turn == DatabaseUtilities.TABLE_GAMES_TURN_CHALLENGED)
					{
						line = 198;
						sqlStatement.setByte(2, DatabaseUtilities.TABLE_GAMES_TURN_CREATOR);
						line = 200;
					}
					else if (db_turn == DatabaseUtilities.TABLE_GAMES_TURN_CREATOR)
					{
						line = 204;
						sqlStatement.setByte(2, DatabaseUtilities.TABLE_GAMES_TURN_CHALLENGED);
						line = 206;
					}

					line = 209;
					sqlStatement.setString(3, param_gameId);
					line = 211;

					// run the SQL statement
					sqlStatement.executeUpdate();
					line = 215;

					GCMUtilities.sendMessage(sqlConnection, param_gameId, userCreatorId, userChallengedId, db_gameType, Byte.valueOf(Utilities.BOARD_NEW_MOVE));
					line = 218;
					printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_MOVE_ADDED_TO_DATABASE));
					line = 220;
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
