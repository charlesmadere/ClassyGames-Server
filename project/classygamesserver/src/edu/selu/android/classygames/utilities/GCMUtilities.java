package edu.selu.android.classygames.utilities;


import java.io.IOException;
import java.sql.Connection;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;


public class GCMUtilities
{


	private final static int RETRY_ATTEMPTS = 5;




	/**
	 * Sends a Google Cloud Message (GCM) to the user specified by the user_id parameter.
	 * Some of the code here was taken from this guide:
	 * https://developer.android.com/guide/google/gcm/gs.html#server-app
	 */
	public static void sendMessage(final Connection sqlConnection, final String game_id, final Long userIdToShow, final String userNameToShow, final Long userIdOfReceiver, final Byte gameType, final Byte messageType)
	{
		final String regId = DatabaseUtilities.grabUsersRegId(sqlConnection, userIdOfReceiver.longValue());

		if (Utilities.verifyValidString(regId))
		// ensure that we were able to grab a valid regId for the user
		{
			final Sender sender = new Sender("");

			// build the message that will be sent to the client device
			// https://developer.android.com/guide/google/gcm/server-javadoc/index.html
			final Message message = new Message.Builder()
				.addData(Utilities.POST_DATA_GAME_ID, game_id)
				.addData(Utilities.POST_DATA_GAME_TYPE, gameType.toString())
				.addData(Utilities.POST_DATA_ID, userIdToShow.toString())
				.addData(Utilities.POST_DATA_NAME, userNameToShow)
				.addData(Utilities.POST_DATA_MESSAGE_TYPE, messageType.toString())
				.build();

			try
			{
				final Result result = sender.send(message, regId, RETRY_ATTEMPTS);
				final String messageId = result.getMessageId();

				if (messageId != null && !messageId.isEmpty())
				{
					final String canonicalRegId = result.getCanonicalRegistrationId();

					if (canonicalRegId != null && !canonicalRegId.isEmpty())
					// same device has more than one registration ID: update database. Replace
					// the existing regId with this new one
					{
						DatabaseUtilities.updateUserRegId(sqlConnection, regId, userIdOfReceiver.longValue());
					}
				}
				else
				{
					final String errorCodeName = result.getErrorCodeName();

					if (errorCodeName.equals(Constants.ERROR_NOT_REGISTERED))
					// application has been removed from device - unregister database
					{
						DatabaseUtilities.removeUserRegId(sqlConnection, userIdToShow);
					}
				}
			}
			catch (final IOException e)
			{

			}
		}
	}


	/**
	 * Sends a Google Cloud Message (GCM) to the user specified by the user_id parameter.
	 * Some of the code here was taken from this guide:
	 * https://developer.android.com/guide/google/gcm/gs.html#server-app
	 */
	public static void sendMessage(final Connection sqlConnection, final String game_id, final Long userIdToShow, final Long userIdOfReceiver, final Byte gameType, final Byte messageType)
	{
		final String userNameToShow = DatabaseUtilities.grabUsersName(sqlConnection, userIdToShow.longValue());
		sendMessage(sqlConnection, game_id, userIdToShow, userNameToShow, userIdOfReceiver, gameType, messageType);
	}


	/**
	 * Sends Google Cloud Messages (GCMs) to the users specified by the user_id parameter
	 * and the opponent_id parameter.
	 * Some of the code here was taken from this guide:
	 * https://developer.android.com/guide/google/gcm/gs.html#server-app
	 */
	public static void sendMessages(final Connection sqlConnection, final String game_id, final Long userIdToShow, final Long userIdOfReceiver, final Byte gameType, final Byte messageType, final String userNameOfReceiver)
	{
		final String userNameToShow = DatabaseUtilities.grabUsersName(sqlConnection, userIdToShow.longValue());
		sendMessage(sqlConnection, game_id, userIdToShow, userNameToShow, userIdOfReceiver, gameType, Byte.valueOf(Utilities.BOARD_LOSE));
		sendMessage(sqlConnection, game_id, userIdOfReceiver, userNameOfReceiver, userIdToShow, gameType, Byte.valueOf(Utilities.BOARD_WIN));
	}


}
