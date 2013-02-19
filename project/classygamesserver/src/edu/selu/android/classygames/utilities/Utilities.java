package edu.selu.android.classygames.utilities;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;


public class Utilities
{


	public final static String APP_NAME = "Classy Games";
	private static Random random;

	// list of digest algorithms found here
	// http://docs.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html#MessageDigest
	public final static String MESSAGE_DIGEST_ALGORITHM = "SHA-256";
	public final static int MESSAGE_DIGEST_LENGTH = 64;
	public final static int MESSAGE_DIGEST_RADIX = 16;

	public final static String UTF8 = "UTF-8";
	public final static String CHARSET = "charset=" + UTF8;
	public final static String MIMETYPE_HTML = "text/html";
	public final static String MIMETYPE_JSON = "application/json";
	public final static String CONTENT_TYPE_HTML = MIMETYPE_HTML + "; " + CHARSET;
	public final static String CONTENT_TYPE_JSON = MIMETYPE_JSON + "; " + CHARSET;

	public final static String POST_DATA_BOARD = DATABASE_TABLE_GAMES_COLUMN_BOARD;
	public final static String POST_DATA_FINISHED = DATABASE_TABLE_GAMES_COLUMN_FINISHED;
	public final static String POST_DATA_ID = DATABASE_TABLE_USERS_COLUMN_ID;
	public final static String POST_DATA_GAME_ID = "game_id";
	public final static String POST_DATA_GAME_TYPE = "game_type";
	public final static byte POST_DATA_GAME_TYPE_CHECKERS = 16;
	public final static byte POST_DATA_GAME_TYPE_CHESS = 32;
	public final static String POST_DATA_LAST_MOVE = DATABASE_TABLE_GAMES_COLUMN_LAST_MOVE;
	public final static String POST_DATA_NAME = DATABASE_TABLE_USERS_COLUMN_NAME;
	public final static String POST_DATA_REG_ID = DATABASE_TABLE_USERS_COLUMN_REG_ID;
	public final static String POST_DATA_TURN = DATABASE_TABLE_GAMES_COLUMN_TURN;
	public final static String POST_DATA_TURN_THEIRS = "turn_theirs";
	public final static String POST_DATA_TURN_YOURS = "turn_yours";
	public final static String POST_DATA_TYPE = "type";
	public final static byte POST_DATA_TYPE_NEW_GAME = 1;
	public final static byte POST_DATA_TYPE_NEW_MOVE = 2;
	public final static byte POST_DATA_TYPE_GAME_OVER_LOSE = 7;
	public final static byte POST_DATA_TYPE_GAME_OVER_WIN = 15;
	public final static String POST_DATA_USER_CHALLENGED = DATABASE_TABLE_GAMES_COLUMN_USER_CHALLENGED;
	public final static String POST_DATA_USER_CREATOR = DATABASE_TABLE_GAMES_COLUMN_USER_CREATOR;

	public final static String POST_ERROR_BOARD_INVALID = "Invalid board!";
	public final static String POST_ERROR_COULD_NOT_CREATE_GAME_ID = "Was unable to create a Game ID.";
	public final static String POST_ERROR_DATA_IS_EMPTY = "POST data is empty or incomplete.";
	public final static String POST_ERROR_DATA_IS_MALFORMED = "POST data is malformed.";
	public final static String POST_ERROR_DATA_NOT_DETECTED = "No POST data detected.";
	public final static String POST_ERROR_DATABASE_COULD_NOT_CONNECT = "Database connection was unable to be established.";
	public final static String POST_ERROR_DATABASE_COULD_NOT_CREATE_CONNECTION_STRING = "Database connection String was unable to be created.";
	public final static String POST_ERROR_DATABASE_COULD_NOT_FIND_GAME_WITH_SPECIFIED_ID = "Game could not be found with specified ID.";
	public final static String POST_ERROR_DATABASE_COULD_NOT_GET_BOARD_DATA = "Was unable to acquire board data from the database.";
	public final static String POST_ERROR_DATABASE_COULD_NOT_GET_GAMES = "Was unable to acquire a list of games from the database";
	public final static String POST_ERROR_DATABASE_COULD_NOT_LOAD = "Database DriverManager could not be loaded.";
	public final static String POST_ERROR_JSON_EXCEPTION = "JSONException error!";
	public final static String POST_ERROR_GAME_IS_ALREADY_OVER = "Attempted to add a new move to a game that has already been completed!";
	public final static String POST_ERROR_GENERIC = "POST data received but an error occurred.";
	public final static String POST_ERROR_INVALID_CHALLENGER = "Invalid challenger!";
	public final static String POST_ERROR_ITS_NOT_YOUR_TURN = "Attempted to make a new move when it wasn't the user's turn!";
	public final static String POST_SUCCESS_GAME_ADDED_TO_DATABASE = "Game successfully added to database!";
	public final static String POST_SUCCESS_GENERIC = "POST data received.";
	public final static String POST_SUCCESS_MOVE_ADDED_TO_DATABASE = "Move successfully added to database!";
	public final static String POST_SUCCESS_NO_ACTIVE_GAMES = "Player has no active games!";
	public final static String POST_SUCCESS_USER_ADDED_TO_DATABASE = "You've been successfully registered with " + APP_NAME + ".";
	public final static String POST_SUCCESS_USER_REMOVED_FROM_DATABASE = "You've been successfully unregistered from " + APP_NAME + ".";

	public final static byte BOARD_INVALID = -1;
	public final static byte BOARD_NEW_GAME = POST_DATA_TYPE_NEW_GAME;
	public final static byte BOARD_NEW_MOVE = POST_DATA_TYPE_NEW_MOVE;
	public final static byte BOARD_LOSE = POST_DATA_TYPE_GAME_OVER_LOSE;
	public final static byte BOARD_WIN = POST_DATA_TYPE_GAME_OVER_WIN;




	public static Random getRandom()
	{
		if (random == null)
		{
			// create a Random object. We're seeding it with the epoch in milliseconds because
			// this will 100% certainly always be a different value every single time that it's
			// run, guaranteeing a strong seed.
			random = new Random(System.currentTimeMillis());
		}

		return random;
	}


	/**
	 * Prepares the message that should be written out using the PrintWriter.
	 * 
	 * @param data
	 * Data to include in this output message.
	 * 
	 * @param hasError
	 * Whether or not an error ocurred.
	 * 
	 * @return
	 * Output message that should be written out using the PrintWriter.
	 */
	private static String makePostData(final Object data, final boolean hasError)
	{
		String outputString = null;

		try
		{
			final JSONObject result = new JSONObject();
	
			if (hasError)
			{
				result.put("error", data);
			}
			else
			{
				result.put("success", data);
			}
	
			final JSONObject output = new JSONObject();
			output.put("result", result);
			outputString = output.toString();
		}
		catch (final JSONException e)
		{

		}

		return outputString;
	}


	/**
	 * Makes a message to write out using the PrintWriter.
	 * 
	 * @param data
	 * The data to include in the output message.
	 * 
	 * @return
	 * Output message that should be written out using the PrintWriter.
	 */
	public static String makePostDataError(final Object data)
	{
		return makePostData(data, true);
	}


	/**
	 * Makes a message to write out using the PrintWriter.
	 * 
	 * @param data
	 * The data to include in the output message.
	 * 
	 * @return
	 * Output message that should be written out using the PrintWriter.
	 */
	public static String makePostDataSuccess(final Object data)
	{
		return makePostData(data, false);
	}


	public static boolean validGameTypeValue(final byte gameType)
	{
		switch (gameType)
		{
			case BOARD_INVALID:
			case BOARD_NEW_GAME:
			case BOARD_NEW_MOVE:
			case BOARD_LOSE:
			case BOARD_WIN:
				return true;

			default:
				return false;
		}
	}


	/**
	 * Verifies a Byte object for validity.
	 * 
	 * @param theByte
	 * The Byte to check.
	 * 
	 * @return
	 * Returns true if the given Byte is valid.
	 */
	public static boolean verifyValidByte(final Byte theByte)
	{
		return theByte != null && theByte.byteValue() >= 1;
	}


	/**
	 * Verifies a set of Byte objects for validity.
	 * 
	 * @param bytes
	 * The Bytes to check.
	 * 
	 * @return
	 * Returns true if all of the given Bytes are valid.
	 */
	public static boolean verifyValidBytes(final Byte... bytes)
	{
		for (int i = 0; i < bytes.length; ++i)
		{
			if (!verifyValidByte(bytes[i]))
			{
				return false;
			}
		}

		return true;
	}


	/**
	 * Verifies a Long object for validity.
	 * 
	 * @param theLong
	 * The Long to check.
	 * 
	 * @return
	 * Returns true if the given Long is valid.
	 */
	public static boolean verifyValidLong(final Long theLong)
	{
		return theLong != null && theLong.longValue() >= 1;
	}


	/**
	 * Verifies a set of Long objects for validity.
	 * 
	 * @param longs
	 * The Longs to check.
	 * 
	 * @return
	 * Returns true if all of the given Longs are valid.
	 */
	public static boolean verifyValidLongs(final Long... longs)
	{
		for (int i = 0; i < longs.length; ++i)
		{
			if (!verifyValidLong(longs[i]))
			{
				return false;
			}
		}

		return true;
	}


	/**
	 * Verifies a String object for validity.
	 * 
	 * @param string
	 * The String to check.
	 * 
	 * @return
	 * Returns true if the given String is valid.
	 */
	public static boolean verifyValidString(final String string)
	{
		return string != null && !string.isEmpty();
	}


	/**
	 * Verifies a set of String objects for validity.
	 * 
	 * @param strings
	 * The Strings to check.
	 * 
	 * @return
	 * Returns true if all of the given Strings are valid.
	 */
	public static boolean verifyValidStrings(final String... strings)
	{
		for (int i = 0; i < strings.length; ++i)
		{
			if (!verifyValidString(strings[i]))
			{
				return false;
			}
		}

		return true;
	}


}
