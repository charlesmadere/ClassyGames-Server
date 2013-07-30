package com.charlesmadere.android.classygames;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
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


public final class NewGame extends Servlet
{


	private final static long serialVersionUID = 1L;


	private String param_userChallengedId;
	private String param_userChallengedName;
	private String param_userCreatorId;
	private String param_gameType;
	private String param_board;

	private Long userChallengedId;
	private Long userCreatorId;
	private Byte gameType;

	private GenericBoard board;




	public NewGame()
	{
		super();
	}




	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
		throws IOException, ServletException
	{
		prepare(response);
		param_userChallengedId = request.getParameter(Utilities.POST_DATA_USER_CHALLENGED);
		param_userChallengedName = request.getParameter(Utilities.POST_DATA_NAME);
		param_userCreatorId = request.getParameter(Utilities.POST_DATA_USER_CREATOR);
		param_gameType = request.getParameter(Utilities.POST_DATA_GAME_TYPE);
		param_board = request.getParameter(Utilities.POST_DATA_BOARD);

		if (Utilities.verifyValidStrings(param_userChallengedId, param_userChallengedName, param_userCreatorId, param_board))
		{
			userChallengedId = Long.valueOf(param_userChallengedId);
			userCreatorId = Long.valueOf(param_userCreatorId);

			if (Utilities.verifyValidLongs(userChallengedId, userCreatorId))
			// check inputs for validity
			{
				if (Utilities.verifyValidString(param_gameType))
				// check to see if we were given a gameType parameter
				{
					gameType = Byte.valueOf(param_gameType);
				}
				else
				{
					gameType = Byte.valueOf(Utilities.POST_DATA_GAME_TYPE_CHECKERS);
				}

				try
				{
					board = GameUtilities.newGame(param_board, gameType.byteValue());

					if (board.checkValidity() == Utilities.BOARD_NEW_GAME)
					{
						newGame();
					}
					else
					{
						printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_BOARD_INVALID));
					}
				}
				catch (final UnsupportedEncodingException e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DIGEST_HAD_IMPROPER_ENCODING));
				}
				catch (final IOException e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_GCM_FAILED_TO_SEND));
				}
				catch (final JSONException e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_JSON_EXCEPTION));
				}
				catch (final NoSuchAlgorithmException e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DIGEST_HAD_UNSUPPORTED_ALGORITHM));
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
	 * @throws UnsupportedEncodingException
	 * If the character encoding that we try to convert a randomly generated
	 * int into (which at the time of this writing is UTF-8) turns out to be
	 * unsupported, then this Exception will be thrown.
	 * 
	 * @throws IOException
	 * An IOException could be thrown when the GCM message is attempted to be
	 * sent.
	 * 
	 * @throws JSONException
	 * If at some point the JSON data that this method tries to create has an
	 * issue then this Exception will be thrown.
	 * 
	 * @throws NoSuchAlgorithmException
	 * If the algorithm that we try to use in creating the digest (which at the
	 * time of this writing is SHA-256) doesn't exist, then this Exception will
	 * be thrown.
	 * 
	 * @throws SQLException
	 * If at some point there is some kind of connection error or query problem
	 * with the SQL database then this Exception will be thrown.
	 * 
	 * @throws Exception
	 * If the JDBC driver could not be loaded then this Exception will be
	 * thrown.
	 */
	private void newGame() throws UnsupportedEncodingException, IOException, JSONException, NoSuchAlgorithmException, SQLException, Exception
	{
		DB.open();
		DBConstants.ensureUserExistsInDatabase(sqlConnection, userChallengedId.longValue(), param_userChallengedName);

		board.flipTeams();
		final JSONObject boardJSON = board.makeJSON();
		final String boardJSONString = boardJSON.toString();

		boolean continueToRun = true;
		String digest = null;

		do
		// This loop does a ton of stuff. First a digest is created to be
		// used as this new game's game ID. Then we check to see if this ID
		// is already in the database. If the ID is already in the
		// database, we check to see if the game it belongs to is a
		// finished game. If it is a finished game, then we can safely
		// replace the data from that game with the data from our new game.
		// If it is not a finished game, this whole loop will have to
		// restart as we're going to have to create a new ID (we somehow
		// managed to create an SHA-256 digest that clashed with another
		// one. The odds of this happening are extremely unlikely but we
		// still have to check for it.)
		// But back to the ifs and such: if we created an ID that does not
		// already exist in the database, then we can simply insert our new
		// game data safely into it.
		{
			// create a digest to use as the Game ID
			digest = createDigest
			(
				userChallengedId.toString().getBytes(Utilities.UTF8),
				userCreatorId.toString().getBytes(Utilities.UTF8),
				boardJSONString.getBytes(Utilities.UTF8)
			);

			// check to see if the digest we created is already taken by a game
			// in the database
			final ResultSet sqlResult = DBConstants.grabGamesInfo(sqlConnection, digest);

			if (sqlResult != null && sqlResult.next())
			// the digest we created to use as an ID already exists in the
			// games table
			{
				if (sqlResult.getByte(DBConstants.TABLE_GAMES_COLUMN_FINISHED) == DBConstants.TABLE_GAMES_FINISHED_TRUE)
				// Game with the digest we created already exists, AND
				// has been finished. Because of this, we can safely
				// replace that game's data with our new game's data
				{
					DB.close(sqlStatement);

					// prepare a SQL statement to be run on the database
					final String sqlStatementString = "UPDATE " + DBConstants.TABLE_GAMES + " SET " + DBConstants.TABLE_GAMES_COLUMN_USER_CREATOR + " = ?, " + DBConstants.TABLE_GAMES_COLUMN_USER_CHALLENGED + " = ?, " + DBConstants.TABLE_GAMES_COLUMN_BOARD + " = ?, " + DBConstants.TABLE_GAMES_COLUMN_TURN + " = ?, " + DBConstants.TABLE_GAMES_COLUMN_GAME_TYPE + " = ?, " + DBConstants.TABLE_GAMES_COLUMN_FINISHED + " = ? WHERE " + DBConstants.TABLE_GAMES_COLUMN_ID + " = ?";
					sqlStatement = DB.connection.prepareStatement(sqlStatementString);

					// prevent SQL injection by inserting data this way
					sqlStatement.setLong(1, userCreatorId.longValue());
					sqlStatement.setLong(2, userChallengedId.longValue());
					sqlStatement.setString(3, boardJSONString);
					sqlStatement.setByte(4, DBConstants.TABLE_GAMES_TURN_CHALLENGED);
					sqlStatement.setByte(5, gameType.byteValue());
					sqlStatement.setByte(6, DBConstants.TABLE_GAMES_FINISHED_FALSE);
					sqlStatement.setString(7, digest);

					// run the SQL statement
					sqlStatement.executeUpdate();

					continueToRun = false;
				}
			}
			else
			// the digest that we created to use as an ID DOES NOT already
			// exist in the games table. We we can now just simply insert
			// this new game's data into the table.
			{
				DB.close(sqlStatement);

				// prepare a SQL statement to be run on the database
				final String sqlStatementString = "INSERT INTO " + DBConstants.TABLE_GAMES + " " + DBConstants.TABLE_GAMES_FORMAT + " " + DBConstants.TABLE_GAMES_VALUES;
				sqlStatement = DB.connection.prepareStatement(sqlStatementString);

				// prevent SQL injection by inserting data this way
				sqlStatement.setString(1, digest);
				sqlStatement.setLong(2, userCreatorId.longValue());
				sqlStatement.setLong(3, userChallengedId.longValue());
				sqlStatement.setString(4, boardJSONString);
				sqlStatement.setByte(5, DBConstants.TABLE_GAMES_TURN_CHALLENGED);
				sqlStatement.setByte(6, gameType.byteValue());
				sqlStatement.setByte(7, DBConstants.TABLE_GAMES_FINISHED_FALSE);

				// run the SQL statement
				sqlStatement.executeUpdate();

				continueToRun = false;
			}
		}
		while (continueToRun);

		GCMUtilities.sendMessage(sqlConnection, digest, userCreatorId, userChallengedId, gameType, Byte.valueOf(Utilities.BOARD_NEW_GAME));
		printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_GAME_ADDED_TO_DATABASE));
	}


	/**
	 * Creates and then returns a digest (also known as a hash) to be used as
	 * this game's ID. The digest is going to be generated using the algorithm
	 * specified in Utilities.MESSAGE_DIGEST_ALGORITHM (at the time of this
	 * writing it's SHA-256), and if the length of the hash is less than what
	 * is specified in Utilities.MESSAGE_DIGEST_LENGTH (at the time of this
	 * writing it's 64), we will continue to add random characters to it until
	 * it hits that length.
	 * 
	 * @param userChallengedBytes
	 * The user ID of the challenged user converted to a byte array.
	 * 
	 * @param userCreatorBytes
	 * The user ID of the creator user converted to a byte array.
	 * 
	 * @param boardBytes
	 * The game board converted to a byte array.
	 * 
	 * @return
	 * Returns a nifty digest as a String object. This digest should be used as
	 * this game's ID.
	 * 
	 * @throws UnsupportedEncodingException
	 * If the character encoding that we try to convert a randomly generated
	 * int into (which at the time of this writing is UTF-8) turns out to be
	 * unsupported, then this Exception will be thrown.
	 * 
	 * @throws NoSuchAlgorithmException
	 * If the algorithm that we try to use in creating the digest (which at the
	 * time of this writing is SHA-256) doesn't exist, then this Exception will
	 * be thrown.
	 */
	private String createDigest(final byte[] userChallengedBytes, final byte[] userCreatorBytes,
		final byte[] boardBytes) throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		// create a digest to use as the Game ID. We are going to be using the
		// Utilities.MESSAGE_DIGEST_ALGORITHM as our hash generation algorithm.
		// At the time of this writing it's SHA-256, but plenty more algorithms
		// are available.
		final MessageDigest digest = MessageDigest.getInstance(Utilities.MESSAGE_DIGEST_ALGORITHM);

		// Build the digest. As can be seen, we're using a bunch of different
		// variables here. The more data we use here the better our digest
		// will be.
		digest.update(userChallengedBytes);
		digest.update(userCreatorBytes);
		digest.update(boardBytes);

		// This line looks funky but it's really not that bad. First we're
		// getting a random int. Then we're storing it as an Integer object.
		// Next we're calling its toString() method to convert the Integer
		// object into a String. And finally, we're calling that String's
		// getBytes() method. All of this junk is needed because the
		// digest.update() method requires a byte array as its input parameter.
		digest.update(Integer.valueOf(Utilities.getRandom().nextInt()).toString().getBytes(Utilities.UTF8));

		final StringBuilder digestBuilder = new StringBuilder(new BigInteger(digest.digest()).abs().toString(Utilities.MESSAGE_DIGEST_RADIX));

		for (int nibble = 0; digestBuilder.length() < Utilities.MESSAGE_DIGEST_LENGTH; )
		// we want a digest that's Utilities.MESSAGE_DIGEST_LENGTH characters
		// in length. At the time of this writing, we are aiming for 64
		// characters long. Sometimes the digest algorithm will give us a bit
		// less than that. So here we're making up for that shortcoming by
		// continuously adding random characters to the digest until we get a
		// digest that is 64 characters long.
		{
			do
			// We don't want a negative number. Keep generating random ints
			// until we get one that's positive.
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

				case 15:
					digestBuilder.append('f');
					break;
			}
		}

		return digestBuilder.toString();
	}


}
