package edu.selu.android.classygames.utilities;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;


/**
 * Class filled with a bunch of methods relating to Google Cloud Messaging
 * (GCM).
 */
public class GCMUtilities
{


	private final static int RETRY_ATTEMPTS = 5;




	/**
	 * Sends a Google Cloud Message (GCM) to the user specified by the user_id parameter.
	 * Some of the code here was taken from this guide:
	 * https://developer.android.com/guide/google/gcm/gs.html#server-app
	 * 
	 * @param sqlConnection
	 * An existing open connection to the SQL database.
	 * 
	 * @param gameId
	 * The ID of the game that you are sending the user a notification for.
	 * 
	 * @param userIdToShow
	 * The user ID of the person who is not receiving this notification.
	 * 
	 * @param userNameToShow
	 * The user name of the person who is not receiving this notification.
	 * 
	 * @param userIdOfReceiver
	 * The user ID of the person who is receiving this notification.
	 * 
	 * @param gameType
	 * The type of game that this notification is for. Could be checkers,
	 * chess...
	 * 
	 * @param messageType
	 * The type of message that this is. Could be a new move or a new game
	 * type.
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
	 * If the GCM library couldn't be loaded then this Exception will be
	 * thrown.
	 */
	private static void sendMessage(final Connection sqlConnection, final String gameId, final Long userIdToShow, final String userNameToShow, final Long userIdOfReceiver, final Byte gameType, final Byte messageType) throws IOException, SQLException, Exception
	{
		final String regId = DatabaseUtilities.grabUsersRegId(sqlConnection, userIdOfReceiver.longValue());

		if (Utilities.verifyValidString(regId))
		// ensure that we were able to grab a valid regId for the user
		{
			// load the GCM Sender class
			Class.forName("com.google.android.gcm.server.Sender").newInstance();

			// Create a GCM Sender object with our Google API key
			final Sender sender = new Sender(KeysAndConstants.GOOGLE_API_KEY);

			// build the message that will be sent to the client device
			// https://developer.android.com/guide/google/gcm/server-javadoc/index.html
			final Message message = new Message.Builder()
				.addData(Utilities.POST_DATA_GAME_ID, gameId)
				.addData(Utilities.POST_DATA_GAME_TYPE, gameType.toString())
				.addData(Utilities.POST_DATA_ID, userIdToShow.toString())
				.addData(Utilities.POST_DATA_NAME, userNameToShow)
				.addData(Utilities.POST_DATA_MESSAGE_TYPE, messageType.toString())
				.build();

			final Result result = sender.send(message, regId, RETRY_ATTEMPTS);
			final String messageId = result.getMessageId();

			if (Utilities.verifyValidString(messageId))
			{
				final String canonicalRegId = result.getCanonicalRegistrationId();

				if (Utilities.verifyValidString(canonicalRegId))
				// same device has more than one registration ID: update database. Replace
				// the existing regId with this new one
				{
					DatabaseUtilities.updateUserRegId(sqlConnection, userIdOfReceiver.longValue(), canonicalRegId);
				}
			}
			else
			{
				final String errorCodeName = result.getErrorCodeName();

				if (errorCodeName.equals(Constants.ERROR_NOT_REGISTERED))
				// application has been removed from device - unregister database
				{
					DatabaseUtilities.updateUserRegId(sqlConnection, userIdToShow, null);
				}
			}
		}
	}


	/**
	 * Sends a Google Cloud Message (GCM) to a single user.
	 * 
	 * @param sqlConnection
	 * An existing open connection to the SQL database.
	 * 
	 * @param gameId
	 * The ID of the game that you are sending the user a notification for.
	 * 
	 * @param userIdToShow
	 * The user ID of the person who is not receiving this notification.
	 * 
	 * @param userIdOfReceiver
	 * The user ID of the person who is receiving this notification.
	 * 
	 * @param gameType
	 * The type of game that this notification is for. Could be checkers,
	 * chess...
	 * 
	 * @param messageType
	 * The type of message that this is. Could be a new move or a new game
	 * type.
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
	 * If the GCM library couldn't be loaded then this Exception will be
	 * thrown.
	 */
	public static void sendMessage(final Connection sqlConnection, final String gameId, final Long userIdToShow, final Long userIdOfReceiver, final Byte gameType, final Byte messageType) throws IOException, SQLException, Exception
	{
		final String userNameToShow = DatabaseUtilities.grabUsersName(sqlConnection, userIdToShow.longValue());
		sendMessage(sqlConnection, gameId, userIdToShow, userNameToShow, userIdOfReceiver, gameType, messageType);
	}


	/**
	 * Send a Google Cloud Message (GCM) to two users.
	 * 
	 * @param sqlConnection
	 * An existing open connection to the SQL database.
	 * 
	 * @param gameId
	 * The ID of the game that you are sending the user a notification for.
	 * 
	 * @param userIdToShow
	 * The user ID of the person who is not receiving this notification.
	 * 
	 * @param userIdOfReceiver
	 * The user ID of the person who is receiving this notification.
	 * 
	 * @param gameType
	 * The type of game that this notification is for. Could be checkers,
	 * chess...
	 * 
	 * @param messageType
	 * The type of message that this is. Could be a new move or a new game
	 * type.
	 * 
	 * @param userNameOfReceiver
	 * Name of the user that is getting the win notification.
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
	 * If the GCM library couldn't be loaded then this Exception will be
	 * thrown.
	 */
	public static void sendMessages(final Connection sqlConnection, final String gameId, final Long userIdToShow, final Long userIdOfReceiver, final Byte gameType, final Byte messageType, final String userNameOfReceiver) throws IOException, SQLException, Exception
	{
		final String userNameToShow = DatabaseUtilities.grabUsersName(sqlConnection, userIdToShow.longValue());
		sendMessage(sqlConnection, gameId, userIdToShow, userNameToShow, userIdOfReceiver, gameType, Byte.valueOf(Utilities.BOARD_LOSE));
		sendMessage(sqlConnection, gameId, userIdOfReceiver, userNameOfReceiver, userIdToShow, gameType, Byte.valueOf(Utilities.BOARD_WIN));
	}


}
