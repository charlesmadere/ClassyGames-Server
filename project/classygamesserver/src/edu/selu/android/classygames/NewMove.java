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
import edu.selu.android.classygames.utilities.GCMUtilities;
import edu.selu.android.classygames.utilities.GameUtilities;
import edu.selu.android.classygames.utilities.Utilities;


/**
 * Servlet implementation class NewMove
 */
public class NewMove extends HttpServlet
{


	private final static long serialVersionUID = 1L;

	private Connection sqlConnection;
	private PreparedStatement sqlStatement;
	private PrintWriter printWriter;
	private ResultSet sqlResult;

	private String parameter_userChallengedId;
	private String parameter_userChallengedName;
	private String parameter_userCreatorId;
	private String parameter_gameId;
	private String parameter_gameType;
	private String parameter_board;

	private Long userChallengedId;
	private Long userCreatorId;
	private Byte gameType;

	private GenericBoard board;




	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public NewMove()
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

		parameter_userChallengedId = request.getParameter(Utilities.POST_DATA_USER_CHALLENGED);
		parameter_userChallengedName = request.getParameter(Utilities.POST_DATA_NAME);
		parameter_userCreatorId = request.getParameter(Utilities.POST_DATA_USER_CREATOR);
		parameter_gameId = request.getParameter(Utilities.POST_DATA_GAME_ID);
		parameter_board = request.getParameter(Utilities.POST_DATA_BOARD);

		if (Utilities.verifyValidStrings(parameter_userChallengedId, parameter_userChallengedName, parameter_userCreatorId, parameter_gameId, parameter_board))
		// check for invalid inputs
		{
			userChallengedId = Long.valueOf(parameter_userChallengedId);
			userCreatorId = Long.valueOf(parameter_userChallengedName);

			if (Utilities.verifyValidLongs(userChallengedId, userCreatorId))
			// check for invalid inputs
			{
				try
				{
					sqlConnection = Utilities.getSQLConnection();

					if (Utilities.ensureUserExistsInDatabase(sqlConnection, userChallengedId.longValue(), parameter_userChallengedName))
					{
						// prepare a SQL statement to be run on the database
						String sqlStatementString = "SELECT " +  + "";

						// prepare a SQL statement to be run on the database
						sqlStatementString = "SELECT * FROM " + DatabaseUtilities.TABLE_GAMES + " WHERE " + DatabaseUtilities.TABLE_GAMES_COLUMN_ID + " = ?";
						sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

						// prevent SQL injection by inserting data this way
						sqlStatement.setString(1, parameter_gameId);

						// run the SQL statement and acquire any return information
						sqlResult = sqlStatement.executeQuery();

						if (sqlResult.next())
						{
							if (sqlResult.getByte(DatabaseUtilities.TABLE_GAMES_COLUMN_FINISHED) == DatabaseUtilities.TABLE_GAMES_FINISHED_FALSE)
							// make sure that the game has not been finished
							{
								final long database_userChallengedId = sqlResult.getLong(DatabaseUtilities.TABLE_GAMES_COLUMN_USER_CHALLENGED);
								final long database_userCreatorId = sqlResult.getLong(DatabaseUtilities.TABLE_GAMES_COLUMN_USER_CREATOR);
								final byte database_turn = sqlResult.getByte(DatabaseUtilities.TABLE_GAMES_COLUMN_TURN);

								if ((userCreatorId.longValue() == database_userChallengedId && database_turn == DatabaseUtilities.TABLE_GAMES_TURN_CHALLENGED) || (userCreatorId.longValue() == database_userCreatorId && database_turn == DatabaseUtilities.TABLE_GAMES_TURN_CREATOR))
								{
									final String database_oldBoard = sqlResult.getString(DatabaseUtilities.TABLE_GAMES_COLUMN_BOARD);
									board = GameUtilities.newGame(database_oldBoard, gameType.byteValue());
									final JSONObject oldBoardJSON = new JSONObject(database_oldBoard);
									final Byte boardValidationResult = Byte.valueOf(board.checkValidity(oldBoardJSON));

									if (boardValidationResult.byteValue() == Utilities.BOARD_NEW_MOVE || boardValidationResult.byteValue() == Utilities.BOARD_WIN)
									{
										board.flipTeams();
										final JSONObject newBoardJSON = board.makeJSON();
										final String newBoardJSONString = newBoardJSON.toString();

										// prepare a SQL statement to be run on the database
										sqlStatementString = "UPDATE " + DatabaseUtilities.TABLE_GAMES + " SET " + DatabaseUtilities.TABLE_GAMES_COLUMN_BOARD + " = ?, " + Utilities.DATABASE_TABLE_GAMES_COLUMN_TURN + " = ?, " + DatabaseUtilities.TABLE_GAMES_COLUMN_FINISHED + " = ?, " + DatabaseUtilities.TABLE_GAMES_COLUMN_LAST_MOVE + " = NOW() WHERE " + DatabaseUtilities.TABLE_GAMES_COLUMN_ID + " = ?";
										sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

										// prevent SQL injection by inserting data this way
										sqlStatement.setString(1, newBoardJSONString);

										if (database_turn == DatabaseUtilities.TABLE_GAMES_TURN_CHALLENGED)
										{
											sqlStatement.setByte(2, DatabaseUtilities.TABLE_GAMES_TURN_CREATOR);
										}
										else if (database_turn == DatabaseUtilities.TABLE_GAMES_TURN_CREATOR)
										{
											sqlStatement.setByte(2, DatabaseUtilities.TABLE_GAMES_TURN_CHALLENGED);
										}

										if (boardValidationResult.byteValue() == Utilities.BOARD_WIN)
										{
											sqlStatement.setByte(3, DatabaseUtilities.TABLE_GAMES_FINISHED_TRUE);
											GCMUtilities.sendMessages(sqlConnection, parameter_gameId, userCreatorId, userChallengedId, boardValidationResult, parameter_userChallengedName);
										}
										else if (boardValidationResult.byteValue() == Utilities.BOARD_NEW_MOVE)
										{
											sqlStatement.setByte(3, DatabaseUtilities.TABLE_GAMES_FINISHED_FALSE);
											GCMUtilities.sendMessage(sqlConnection, parameter_gameId, userCreatorId, userChallengedId, boardValidationResult);
										}

										sqlStatement.setString(4, parameter_gameId);

										// run the SQL statement
										sqlStatement.executeUpdate();

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
					else
					{
						printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_INVALID_CHALLENGER));
					}
				}
				catch (final SQLException e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_CONNECT));
				}
				catch (final JSONException e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_JSON_EXCEPTION));
				}
				finally
				{
					Utilities.closeSQL(sqlConnection, sqlStatement);
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


}
