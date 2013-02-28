package edu.selu.android.classygames.utilities;


import org.json.JSONException;
import org.json.JSONObject;

import edu.selu.android.classygames.games.GenericBoard;


public class GameUtilities
{


	/**
	 * Attempts to create (and then return) a GenericBoard object.
	 * 
	 * @param parameter_board
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
	public static GenericBoard newGame(final String parameter_board, final byte gameType) throws JSONException
	{
		GenericBoard board = null;

		final JSONObject boardJSON = new JSONObject(parameter_board);

		if (Utilities.verifyValidString(parameter_board))
		{
			switch (gameType)
			{
				case Utilities.POST_DATA_GAME_TYPE_CHESS:
					board = new edu.selu.android.classygames.games.chess.Board(boardJSON);
					break;

				case Utilities.POST_DATA_GAME_TYPE_CHECKERS:
					board = new edu.selu.android.classygames.games.checkers.Board(boardJSON);
					break;
			}
		}

		return board;
	}


}
