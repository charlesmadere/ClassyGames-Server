package com.charlesmadere.android.classygames.utilities;


import org.json.JSONException;
import org.json.JSONObject;

import com.charlesmadere.android.classygames.models.GCMMessage;
import com.charlesmadere.android.classygames.models.games.GenericBoard;


public class GameUtilities
{


	public final static byte BOARD_INVALID = -1;
	public final static byte BOARD_NEW_GAME = GCMMessage.MESSAGE_TYPE_NEW_GAME;
	public final static byte BOARD_NEW_MOVE = GCMMessage.MESSAGE_TYPE_NEW_MOVE;
	public final static byte BOARD_LOSE = GCMMessage.MESSAGE_TYPE_GAME_OVER_LOSE;
	public final static byte BOARD_WIN = GCMMessage.MESSAGE_TYPE_GAME_OVER_WIN;




	/**
	 * Attempts to create (and then return) a GenericBoard object.
	 * 
	 * @param param_board
	 * JSON String containing this game board's information.
	 * 
	 * @param gameType
	 * Byte specifying what type of game this is.
	 * 
	 * @return
	 * Returns a GenericBoard object if one could be instantiated. Returns null
	 * otherwise.
	 * 
	 * @throws JSONException
	 * If at some point the JSON data that this method tries to create has an
	 * issue then this Exception will be thrown.
	 */
	public static GenericBoard newGame(final String param_board, final byte gameType) throws JSONException
	{
		GenericBoard board = null;
		final JSONObject boardJSON = new JSONObject(param_board);

		if (Utilities.verifyValidString(param_board))
		{
			switch (gameType)
			{
				case Utilities.POST_DATA_GAME_TYPE_CHECKERS:
					board = new com.charlesmadere.android.classygames.models.games.checkers.Board(boardJSON);
					break;

				case Utilities.POST_DATA_GAME_TYPE_CHESS:
					board = new com.charlesmadere.android.classygames.models.games.chess.Board(boardJSON);
					break;
			}
		}

		return board;
	}


}
