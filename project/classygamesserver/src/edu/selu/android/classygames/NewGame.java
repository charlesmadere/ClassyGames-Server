package edu.selu.android.classygames;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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


public class NewGame extends HttpServlet
{


	private final static long serialVersionUID = 1L;


	private final static byte RUN_STATUS_NO_ERROR = 0;
	private final static byte RUN_STATUS_UNSUPPORTED_ENCODING = 1;
	private final static byte RUN_STATUS_NO_SUCH_ALGORITHM = 2;


	private Connection sqlConnection;
	private PreparedStatement sqlStatement;
	private PrintWriter printWriter;

	private String parameter_userChallengedId;
	private String parameter_userChallengedName;
	private String parameter_userCreatorId;
	private String parameter_gameType;
	private String parameter_board;

	private Long userChallengedId;
	private Long userCreatorId;

	private Byte gameType;

	private GenericBoard board;




	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public NewGame()
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
		parameter_gameType = request.getParameter(Utilities.POST_DATA_GAME_TYPE);
		parameter_board = request.getParameter(Utilities.POST_DATA_BOARD);

		if (Utilities.verifyValidStrings(parameter_userChallengedId, parameter_userChallengedName, parameter_userCreatorId, parameter_board))
		{
			userChallengedId = Long.valueOf(parameter_userChallengedId);
			userCreatorId = Long.valueOf(parameter_userCreatorId);

			if (Utilities.verifyValidLongs(userChallengedId, userCreatorId))
			// check inputs for validity
			{
				if (Utilities.verifyValidString(parameter_gameType))
				// check to see if we were given a gameType parameter
				{
					gameType = Byte.valueOf(parameter_gameType);
				}
				else
				{
					gameType = Byte.valueOf(Utilities.POST_DATA_GAME_TYPE_CHECKERS);
				}

				board = GameUtilities.newGame(parameter_board, gameType.byteValue());

				if (board.checkValidity() == Utilities.BOARD_NEW_GAME)
				{
					try
					{
						newGame();
					}
					catch (final JSONException e)
					{
						printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_JSON_EXCEPTION));
					}
				}
				else
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_BOARD_INVALID));
				}
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


	private void newGame() throws JSONException
	{
		try
		{
			sqlConnection = DatabaseUtilities.getSQLConnection();

			if (DatabaseUtilities.ensureUserExistsInDatabase(sqlConnection, userChallengedId.longValue(), parameter_userChallengedName))
			{
				board.flipTeams();
				final JSONObject boardJSON = board.makeJSON();
				final String boardJSONString = boardJSON.toString();

				byte runStatus = RUN_STATUS_NO_ERROR;
				String digest = null;

				for (boolean continueToRun = true; continueToRun; )
				// This loop does a ton of stuff. First a digest is created to be used as this new game's game ID. Then we
				// check to see if this ID is already in the database. If the ID is already in the database, we check to
				// see if the game it belongs to is a finished game. If it is a finished game, then we can safely replace
				// the data from that game with the data from our new game. If it is not a finished game, this whole loop
				// will have to restart as we're going to have to create a new ID (we somehow managed to create an SHA-256
				// digest that clashed with another one. The odds of this happening are extremely unlikely but we still
				// have to check for it.)
				// But back to the ifs and such: if we created an ID that does not already exist in the database, then we
				// can simply insert our new game data safely into it.
				{
					// prepare a String to hold a digest in
					digest = null;

					try
					{
						// create a digest to use as the Game ID
						digest = createDigest
						(
							userChallengedId.toString().getBytes(Utilities.UTF8),
							userCreatorId.toString().getBytes(Utilities.UTF8),
							boardJSONString.getBytes(Utilities.UTF8)
						);
					}
					catch (final NoSuchAlgorithmException e)
					// the algorithm we tried to use to create a digest was invalid
					{
						runStatus = RUN_STATUS_NO_SUCH_ALGORITHM;
					}
					catch (final UnsupportedEncodingException e)
					// the character set we tried to use in digest creation was invalid
					{
						runStatus = RUN_STATUS_UNSUPPORTED_ENCODING;
					}

					if (runStatus != RUN_STATUS_NO_ERROR || digest == null || digest.isEmpty())
					// check to see if we encountered any of the exceptions above or if our digest is broken
					{
						continueToRun = false;
					}
					else
					// no exceptions were encountered. let's continue. Once past this point we no longer have to check on or
					// modify the runStatus variable
					{
						// prepare a SQL statement to be run on the database
						String sqlStatementString = "SELECT " + DatabaseUtilities.TABLE_GAMES_COLUMN_FINISHED + " FROM " + DatabaseUtilities.TABLE_GAMES + " WHERE " + DatabaseUtilities.TABLE_GAMES_COLUMN_ID + " = ?";
						sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

						// prevent SQL injection by inserting data this way
						sqlStatement.setString(1, digest);

						// run the SQL statement and acquire any return information
						final ResultSet sqlResult = sqlStatement.executeQuery();

						if (sqlResult.next())
						// the digest we created to use as an ID already exists in the games table
						{
							if (sqlResult.getByte(DatabaseUtilities.TABLE_GAMES_COLUMN_FINISHED) == DatabaseUtilities.TABLE_GAMES_FINISHED_TRUE)
							// Game with the digest we created already exists, AND has been finished. Because of this, we can
							// safely replace that game's data with our new game's data
							{
								DatabaseUtilities.closeSQLStatement(sqlStatement);

								// prepare a SQL statement to be run on the database
								sqlStatementString = "UPDATE " + DatabaseUtilities.TABLE_GAMES + " SET " + DatabaseUtilities.TABLE_GAMES_COLUMN_USER_CREATOR + " = ?, " + DatabaseUtilities.TABLE_GAMES_COLUMN_USER_CHALLENGED + " = ?, " + DatabaseUtilities.TABLE_GAMES_COLUMN_BOARD + " = ?, " + DatabaseUtilities.TABLE_GAMES_COLUMN_TURN + " = ?, " + DatabaseUtilities.TABLE_GAMES_COLUMN_GAME_TYPE + " = ?, " + DatabaseUtilities.TABLE_GAMES_COLUMN_FINISHED + " = ? WHERE " + DatabaseUtilities.TABLE_GAMES_COLUMN_ID + " = ?";
								sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

								// prevent SQL injection by inserting data this way
								sqlStatement.setLong(1, userCreatorId.longValue());
								sqlStatement.setLong(2, userChallengedId.longValue());
								sqlStatement.setString(3, boardJSONString);
								sqlStatement.setByte(4, DatabaseUtilities.TABLE_GAMES_TURN_CHALLENGED);
								sqlStatement.setByte(5, gameType.byteValue());
								sqlStatement.setByte(6, DatabaseUtilities.TABLE_GAMES_FINISHED_FALSE);
								sqlStatement.setString(7, digest);

								// run the SQL statement
								sqlStatement.executeUpdate();

								continueToRun = false;
							}
						}
						else
						// the digest that we created to use as an ID DOES NOT already exist in the games table. We we can now
						// just simply insert this new game's data into the table
						{
							DatabaseUtilities.closeSQLStatement(sqlStatement);

							// prepare a SQL statement to be run on the database
							sqlStatementString = "INSERT INTO " + DatabaseUtilities.TABLE_GAMES + " " + DatabaseUtilities.TABLE_GAMES_FORMAT + " " + DatabaseUtilities.TABLE_GAMES_VALUES;
							sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

							// prevent SQL injection by inserting data this way
							sqlStatement.setString(1, digest);
							sqlStatement.setLong(2, userCreatorId.longValue());
							sqlStatement.setLong(3, userChallengedId.longValue());
							sqlStatement.setString(4, boardJSONString);
							sqlStatement.setByte(5, DatabaseUtilities.TABLE_GAMES_TURN_CHALLENGED);
							sqlStatement.setByte(6, gameType.byteValue());
							sqlStatement.setByte(7, DatabaseUtilities.TABLE_GAMES_FINISHED_FALSE);

							// run the SQL statement
							sqlStatement.executeUpdate();

							continueToRun = false;
						}
					}
				}

				switch (runStatus)
				// we may have hit an error in the above loop
				{
					case RUN_STATUS_NO_SUCH_ALGORITHM:
						printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_COULD_NOT_CREATE_GAME_ID));
						break;

					case RUN_STATUS_UNSUPPORTED_ENCODING:
						printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_COULD_NOT_CREATE_GAME_ID));
						break;

					default:
						GCMUtilities.sendMessage(sqlConnection, digest, userCreatorId, userChallengedId, gameType, Byte.valueOf(Utilities.BOARD_NEW_GAME));
						printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_GAME_ADDED_TO_DATABASE));
						break;
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
		finally
		{
			DatabaseUtilities.closeSQL(sqlConnection, sqlStatement);
		}
	}


	private String createDigest(final byte[] user_challenged_bytes, final byte[] user_creator_bytes, final byte[] board_bytes) throws NoSuchAlgorithmException, UnsupportedEncodingException
	// huge hash creation algorithm magic below
	{
		// create a digest to use as the Game ID. We are going to be using the Utilities.MESSAGE_DIGEST_ALGORITHM as our
		// hash generation algorithm. At the time of this writing it's SHA-256, but plenty more algorithms are available.
		final MessageDigest digest = MessageDigest.getInstance(Utilities.MESSAGE_DIGEST_ALGORITHM);

		// Build the digest. As can be seen, we're using a bunch of different variables here. The more data we use here
		// the better our digest will be.
		digest.update(user_challenged_bytes);
		digest.update(user_creator_bytes);
		digest.update(board_bytes);
		digest.update(new Integer(Utilities.getRandom().nextInt()).toString().getBytes(Utilities.UTF8));

		final StringBuilder digestBuilder = new StringBuilder(new BigInteger(digest.digest()).abs().toString(Utilities.MESSAGE_DIGEST_RADIX));

		for (int nibble = 0; digestBuilder.length() < Utilities.MESSAGE_DIGEST_LENGTH; )
		// we want a digest that's Utilities.MESSAGE_DIGEST_LENGTH characters in length. At the time of this writing, we
		// are aiming for 64 characters long. Sometimes the digest algorithm will give us a bit less than that. So here
		// we're making up for that shortcoming by continuously adding random characters to the digest until we get 64
		{
			do
			// we don't want a negative number. keep generating random ints until we get one that's positive
			{
				// don't allow the random number we've generated to be above 15
				nibble = Utilities.getRandom().nextInt() % 16;
			}
			while (nibble < 0);

			switch (nibble)
			// add a hexadecimal character onto the end of the StringBuilder
			{
				case 0:
					digestBuilder.append('0');
					break;

				case 1:
					digestBuilder.append('1');
					break;

				case 2:
					digestBuilder.append('2');
					break;

				case 3:
					digestBuilder.append('3');
					break;

				case 4:
					digestBuilder.append('4');
					break;

				case 5:
					digestBuilder.append('5');
					break;

				case 6:
					digestBuilder.append('6');
					break;

				case 7:
					digestBuilder.append('7');
					break;

				case 8:
					digestBuilder.append('8');
					break;

				case 9:
					digestBuilder.append('9');
					break;

				case 10:
					digestBuilder.append('a');
					break;

				case 11:
					digestBuilder.append('b');
					break;

				case 12:
					digestBuilder.append('c');
					break;

				case 13:
					digestBuilder.append('d');
					break;

				case 14:
					digestBuilder.append('e');
					break;

				default:
					digestBuilder.append('f');
					break;
			}
		}

		return digestBuilder.toString();
	}


}
