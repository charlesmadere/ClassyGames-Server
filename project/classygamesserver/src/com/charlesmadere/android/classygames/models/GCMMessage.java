package com.charlesmadere.android.classygames.models;


import java.io.IOException;

import com.charlesmadere.android.classygames.utilities.KeysAndConstants;
import com.charlesmadere.android.classygames.utilities.Utilities;
import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;


public class GCMMessage
{


	public final static byte MESSAGE_TYPE_NEW_GAME = 1;
	public final static byte MESSAGE_TYPE_NEW_MOVE = 2;
	public final static byte MESSAGE_TYPE_GAME_OVER_LOSE = 7;
	public final static byte MESSAGE_TYPE_GAME_OVER_WIN = 15;

	private final static int RETRY_ATTEMPTS = 5;
	private static Sender sender;


	private byte messageType;
	private User userToMention;
	private User userToReceive;
	private String gameId;




	public void sendMessage() throws IOException
	{
		if (userToReceive != null && userToReceive.hasRegId() && userToMention != null &&
			userToMention.hasId() && userToMention.hasName() && verifyValidMessageType(messageType))
		{
			final Message message = new Message.Builder()
				.addData(Utilities.POST_DATA_GAME_ID, gameId)
				.build();

			final Sender sender = getSender();
			final Result result = sender.send(message, userToReceive.getRegId(), RETRY_ATTEMPTS);
			final String resultMessageId = result.getMessageId();

			if (Utilities.verifyValidString(resultMessageId))
			{
				final String canonicalRegId = result.getCanonicalRegistrationId();

				if (Utilities.verifyValidString(canonicalRegId))
				// Same device has more than one registration ID so let's
				// update database the database. Replacing the existing regId
				// with this new one.
				{
					userToReceive.setRegId(canonicalRegId);
				}
			}
			else
			{
				final String errorCodeName = result.getErrorCodeName();

				if (errorCodeName.equalsIgnoreCase(Constants.ERROR_NOT_REGISTERED))
				{
					userToReceive.setRegId(null);
				}
			}
		}
	}


	public void setGameId(final String gameId)
	{
		this.gameId = gameId;
	}


	public void setMessageTypeNewGame()
	{
		messageType = MESSAGE_TYPE_NEW_GAME;
	}


	public void setMessageTypeNewMove()
	{
		messageType = MESSAGE_TYPE_NEW_MOVE;
	}


	public void setMessageTypeGameOverLose()
	{
		messageType = MESSAGE_TYPE_GAME_OVER_LOSE;
	}


	public void setMessageTypeGameOverWin()
	{
		messageType = MESSAGE_TYPE_GAME_OVER_WIN;
	}


	public void setUserToMention(final User userToMention)
	{
		this.userToMention = userToMention;
	}


	public void setUserToReceive(final User userToReceive)
	{
		this.userToReceive = userToReceive;
	}


	private static Sender getSender()
	{
		if (sender == null)
		{
			sender = new Sender(KeysAndConstants.GOOGLE_API_KEY);
		}

		return sender;
	}


	public static boolean verifyValidMessageType(final byte messageType)
	{
		switch (messageType)
		{
			case MESSAGE_TYPE_NEW_GAME:
			case MESSAGE_TYPE_NEW_MOVE:
			case MESSAGE_TYPE_GAME_OVER_LOSE:
			case MESSAGE_TYPE_GAME_OVER_WIN:
				return true;

			default:
				return false;
		}
	}


}
