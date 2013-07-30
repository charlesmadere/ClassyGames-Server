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


	private final static int RETRY_ATTEMPTS = 5;
	private static Sender sender;


	private byte messageType;
	private Person personToMention;
	private Person personToReceive;
	private String gameId;




	public boolean isValid()
	{
		return personToReceive != null && personToReceive.hasRegId() &&
			personToMention != null && verifyValidMessageType(messageType);
	}


	public void sendMessage() throws IOException
	{
		if (isValid())
		{
			final Message message = new Message.Builder()
				.addData(Utilities.POST_DATA_GAME_ID, gameId)
				.build();

			final Sender sender = getSender();
			final Result result = sender.send(message, personToReceive.getRegId(), RETRY_ATTEMPTS);
			final String resultMessageId = result.getMessageId();

			if (Utilities.verifyValidString(resultMessageId))
			{
				final String canonicalRegId = result.getCanonicalRegistrationId();

				if (Utilities.verifyValidString(canonicalRegId))
				// Same device has more than one registration ID so let's
				// update database the database. Replacing the existing regId
				// with this new one.
				{
					personToReceive.setRegId(canonicalRegId);
				}
			}
			else
			{
				final String errorCodeName = result.getErrorCodeName();

				if (errorCodeName.equalsIgnoreCase(Constants.ERROR_NOT_REGISTERED))
				{
					personToReceive.setRegId(null);
				}
			}
		}
	}


	public void setGameId(final String gameId)
	{
		this.gameId = gameId;
	}


	public void setMessageType(final byte messageType)
	{
		this.messageType = messageType;
	}


	public void setPersonToMention(final Person personToMention)
	{
		this.personToMention = personToMention;
	}


	public void setPersonToReceive(final Person personToReceive)
	{
		this.personToReceive = personToReceive;
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
			case Utilities.POST_DATA_MESSAGE_TYPE_NEW_GAME:
			case Utilities.POST_DATA_MESSAGE_TYPE_NEW_MOVE:
			case Utilities.POST_DATA_MESSAGE_TYPE_GAME_OVER_LOSE:
			case Utilities.POST_DATA_MESSAGE_TYPE_GAME_OVER_WIN:
				return true;

			default:
				return false;
		}
	}


}
