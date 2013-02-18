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
	 */
	public static GenericBoard newGame(final String parameter_board, final byte gameType)
	{
		GenericBoard board = null;

		try
		{
			final JSONObject boardJSON = new JSONObject(parameter_board);

			if (Utilities.verifyValidString(parameter_board))
			{
				switch (gameType)
				{
					case Utilities.POST_DATA_GAME_TYPE_CHESS:
						board = new edu.selu.android.classygames.games.chess.Board(boardJSON);
						break;

					case Utilities.POST_DATA_GAME_TYPE_CHECKERS:
					default:
						board = new edu.selu.android.classygames.games.checkers.Board(boardJSON);
						break;
				}
			}
		}
		catch (final JSONException e)
		{

		}

		return board;
	}


}
