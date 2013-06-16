package com.charlesmadere.android.classygames.gcm;


import com.charlesmadere.android.classygames.models.Person;
import com.charlesmadere.android.classygames.utilities.Utilities;


/**
 * 
 */
public class GCMMessage
{


	/**
	 * 
	 */
	private byte messageType;


	/**
	 * 
	 */
	private Person personReceiving;


	/**
	 * 
	 */
	private Person personToBeMentioned;




	/**
	 * 
	 * 
	 * @return
	 * 
	 */
	public boolean sendMessage()
	{
		boolean didMessageSendSuccessfully = false;

		if (personReceiving != null && personReceiving.hasRegId() &&
			personToBeMentioned != null && personToBeMentioned.hasRegId()
			&& verifyValidMessageType(messageType))
		{
			// TODO
			// send GCM message
		}

		return didMessageSendSuccessfully;
	}


	/**
	 * 
	 * 
	 * @param messageType
	 * 
	 */
	public void setMessageType(final byte messageType)
	{
		this.messageType = messageType;
	}


	/**
	 * 
	 * 
	 * @param personReceiving
	 * 
	 */
	public void setPersonReceivingMessage(final Person personReceiving)
	{
		this.personReceiving = personReceiving;
	}


	/**
	 * 
	 * 
	 * @param personToBeMentioned
	 * 
	 */
	public void setPersonToBeMentioned(final Person personToBeMentioned)
	{
		this.personToBeMentioned = personToBeMentioned;
	}


	/**
	 * 
	 * 
	 * @param messageType
	 * 
	 * 
	 * @return
	 * 
	 */
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
