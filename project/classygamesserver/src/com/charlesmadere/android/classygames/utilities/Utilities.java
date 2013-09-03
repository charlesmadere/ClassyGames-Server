package com.charlesmadere.android.classygames.utilities;


import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.charlesmadere.android.classygames.models.GCMMessage;


/**
 * Class filled with a bunch of miscellaneous utility methods and constants.
 */
public class Utilities
{


	public final static String APP_NAME = "Classy Games";
	private static Random random;

	// list of digest algorithms found here
	// http://docs.oracle.com/javase/6/docs/technotes/guides/security/StandardNames.html#MessageDigest
	public final static String MESSAGE_DIGEST_ALGORITHM = "SHA-256";
	public final static int MESSAGE_DIGEST_LENGTH = 80;
	public final static int MESSAGE_DIGEST_RADIX = 16;

	public final static String UTF8 = "UTF-8";
	public final static String CHARSET = "charset=" + UTF8;
	public final static String MIMETYPE_HTML = "text/html";
	public final static String MIMETYPE_JSON = "application/json";
	public final static String CONTENT_TYPE_HTML = MIMETYPE_HTML + "; " + CHARSET;
	public final static String CONTENT_TYPE_JSON = MIMETYPE_JSON + "; " + CHARSET;

	public final static String POST_DATA_BOARD = DBConstants.TABLE_GAMES_COLUMN_BOARD;
	public final static String POST_DATA_CHECKERS = "checkers";
	public final static String POST_DATA_CHESS = "chess";
	public final static String POST_DATA_FINISHED = DBConstants.TABLE_GAMES_COLUMN_FINISHED;
	public final static String POST_DATA_ID = DBConstants.TABLE_USERS_COLUMN_ID;
	public final static String POST_DATA_GAME_ID = "game_id";
	public final static String POST_DATA_GAME_TYPE = DBConstants.TABLE_GAMES_COLUMN_GAME_TYPE;
	public final static byte POST_DATA_GAME_TYPE_CHECKERS = 1;
	public final static byte POST_DATA_GAME_TYPE_CHESS = 2;
	public final static String POST_DATA_LAST_MOVE = DBConstants.TABLE_GAMES_COLUMN_LAST_MOVE;
	public final static String POST_DATA_LOSES = "loses";
	public final static String POST_DATA_MESSAGE_TYPE = "message_type";
	public final static String POST_DATA_NAME = DBConstants.TABLE_USERS_COLUMN_NAME;
	public final static String POST_DATA_REG_ID = DBConstants.TABLE_USERS_COLUMN_REG_ID;
	public final static String POST_DATA_TURN = DBConstants.TABLE_GAMES_COLUMN_TURN;
	public final static String POST_DATA_TURN_THEIRS = "turn_theirs";
	public final static String POST_DATA_TURN_YOURS = "turn_yours";
	public final static String POST_DATA_USER_CHALLENGED = DBConstants.TABLE_GAMES_COLUMN_USER_CHALLENGED;
	public final static String POST_DATA_USER_CREATOR = DBConstants.TABLE_GAMES_COLUMN_USER_CREATOR;
	public final static String POST_DATA_WINS = "wins";

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
	public final static String POST_ERROR_DIGEST_HAD_IMPROPER_ENCODING = "Game's ID could not be created as the digest encoding (" + UTF8 + ") is unsupported.";
	public final static String POST_ERROR_DIGEST_HAD_UNSUPPORTED_ALGORITHM = "Game's ID could not be created as the digest algorithm (" + MESSAGE_DIGEST_ALGORITHM + ") is unsupported.";
	public final static String POST_ERROR_GAME_IS_ALREADY_OVER = "Attempted to add a new move to a game that has already been completed!";
	public final static String POST_ERROR_GCM_FAILED_TO_SEND = "Google Cloud Message failed to send.";
	public final static String POST_ERROR_GENERIC = "POST data received but an error occurred.";
	public final static String POST_ERROR_JDBC_DRIVER_COULD_NOT_LOAD = "JDBC Driver could not be loaded.";
	public final static String POST_ERROR_JSON_EXCEPTION = "JSONException error!";
	public final static String POST_ERROR_INVALID_CHALLENGER = "Invalid challenger!";
	public final static String POST_ERROR_ITS_NOT_YOUR_TURN = "Attempted to make a new move when it wasn't the user's turn!";
	public final static String POST_SUCCESS_GAME_ADDED_TO_DATABASE = "Game successfully added to database!";
	public final static String POST_SUCCESS_GENERIC = "POST data received.";
	public final static String POST_SUCCESS_MOVE_ADDED_TO_DATABASE = "Move successfully added to database!";
	public final static String POST_SUCCESS_NO_ACTIVE_GAMES = "Player has no active games!";
	public final static String POST_SUCCESS_USER_ADDED_TO_DATABASE = "You've been successfully registered with " + APP_NAME + ".";
	public final static String POST_SUCCESS_USER_REMOVED_FROM_DATABASE = "You've been successfully unregistered from " + APP_NAME + ".";

	public final static byte BOARD_INVALID = -1;
	public final static byte BOARD_NEW_GAME = GCMMessage.MESSAGE_TYPE_NEW_GAME;
	public final static byte BOARD_NEW_MOVE = GCMMessage.MESSAGE_TYPE_NEW_MOVE;
	public final static byte BOARD_LOSE = GCMMessage.MESSAGE_TYPE_GAME_OVER_LOSE;
	public final static byte BOARD_WIN = GCMMessage.MESSAGE_TYPE_GAME_OVER_WIN;




	/**
	 * If a Random object does not already exist, this method will create one
	 * and then return that newly created object. The Random object will be
	 * seeded with the System time (System.currentTimeMillis()).
	 * 
	 * @return
	 * Returns a ready-to-use Random object.
	 */
	public static Random getRandom()
	{
		if (random == null)
		{
			// Create a Random object. We're seeding it with the nano time
			// because this will always be a difficult-to-predict value,
			// meaning that it's a relatively strong seed.
			random = new Random(System.nanoTime());
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
	private static String makePostData(final Object data, final boolean hasError) throws JSONException
	{
		String outputString = null;
		final JSONObject result = new JSONObject();

		if (hasError)
		{
			try
			{
				final JSONObject dataJSON = (JSONObject) data;
				result.put("error", dataJSON);
			}
			catch (final ClassCastException e)
			{
				result.put("error", data);
			}
		}
		else
		{
			try
			{
				final JSONObject dataJSON = (JSONObject) data;
				result.put("success", dataJSON);
			}
			catch (final ClassCastException e)
			{
				result.put("success", data);
			}
		}

		final JSONObject output = new JSONObject();
		output.put("result", result);
		outputString = output.toString();

		return outputString;
	}


	/**
	 * Makes a message to write out using the PrintWriter.
	 * 
	 * @param message
	 * The message to send out.
	 * 
	 * @return
	 * Output message that should be written out using the PrintWriter.
	 */
	public static String makePostDataError(final String message)
	{
		return makePostData(message, true);
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
