package com.charlesmadere.android.classygames.models;


import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.json.JSONException;
import org.json.JSONObject;

import com.charlesmadere.android.classygames.models.games.GenericBoard;
import com.charlesmadere.android.classygames.utilities.DB;
import com.charlesmadere.android.classygames.utilities.DBConstants;
import com.charlesmadere.android.classygames.utilities.GameUtilities;
import com.charlesmadere.android.classygames.utilities.Utilities;


public final class Game
{


	private byte finished;
	private byte gameType;
	private byte turn;
	private String board;
	private String id;
	private Timestamp lastMove;
	private User userChallenged;
	private User userCreator;

	private GenericBoard newGameBoard;
	private GenericBoard oldGameBoard;




	public Game(final String id) throws SQLException, Exception
	{
		this.id = id;
		readGameData();
	}


	public Game(final String id, final String board) throws SQLException, Exception
	{
		this.id = id;
		this.board = board;
		readGameData();
	}


	public Game(final ResultSet result) throws SQLException
	{
		initFromSQLResult(result);
	}


	public Game(final User userChallenged, final User userCreator, final String board, final byte gameType)
		throws SQLException
	{
		this.userChallenged = userChallenged;
		this.userCreator = userCreator;
		this.board = board;
		this.gameType = gameType;
		finished = DBConstants.TABLE_GAMES_FINISHED_FALSE;

		newGameBoard = GameUtilities.newGame(board, gameType);
	}




	public String getBoard()
	{
		return board;
	}


	public String getId()
	{
		return id;
	}


	public Timestamp getLastMove()
	{
		return lastMove;
	}


	public long getLastMoveInSeconds()
	{
		return lastMove.getTime() / 1000L;
	}


	public GenericBoard getNewGameBoard()
	{
		return newGameBoard;
	}


	public GenericBoard getOldGameBoard()
	{
		return oldGameBoard;
	}


	public User getUserChallenged()
	{
		return userChallenged;
	}


	public User getUserCreator()
	{
		return userCreator;
	}


	public void flipNewGameBoard() throws JSONException
	{
		newGameBoard.flipTeams();
	}


	public void flipOldGameBoard() throws JSONException
	{
		if (oldGameBoard == null)
		{
			oldGameBoard = GameUtilities.newGame(board, gameType);
		}

		oldGameBoard.flipTeams();
	}


	private void initFromSQLResult(final ResultSet result) throws SQLException
	{
		if (!Utilities.verifyValidString(id))
		{
			id = result.getString(DBConstants.TABLE_GAMES_COLUMN_ID);
		}

		finished = result.getByte(DBConstants.TABLE_GAMES_COLUMN_FINISHED);
		gameType = result.getByte(DBConstants.TABLE_GAMES_COLUMN_GAME_TYPE);
		turn = result.getByte(DBConstants.TABLE_GAMES_COLUMN_TURN);
		lastMove = result.getTimestamp(DBConstants.TABLE_GAMES_COLUMN_LAST_MOVE);

		if (Utilities.verifyValidString(board))
		{
			newGameBoard = GameUtilities.newGame(board, gameType);

			final String oldBoard = result.getString(DBConstants.TABLE_GAMES_COLUMN_BOARD);
			oldGameBoard = GameUtilities.newGame(oldBoard, gameType);
		}
		else
		{
			board = result.getString(DBConstants.TABLE_GAMES_COLUMN_BOARD);
			newGameBoard = GameUtilities.newGame(board, gameType);
		}

		if (userChallenged == null)
		{
			final long userChallengedId = result.getLong(DBConstants.TABLE_GAMES_COLUMN_USER_CHALLENGED);
			userChallenged = new User(userChallengedId);
		}

		if (userCreator == null)
		{
			final long userCreatorId = result.getLong(DBConstants.TABLE_GAMES_COLUMN_USER_CREATOR);
			userCreator = new User(userCreatorId);
		}
	}


	public boolean isChallengedsTurn()
	{
		return turn == DBConstants.TABLE_GAMES_TURN_CHALLENGED;
	}


	public boolean isCreatorsTurn()
	{
		return turn == DBConstants.TABLE_GAMES_TURN_CREATOR;
	}


	public boolean isFinished()
	{
		return finished == DBConstants.TABLE_GAMES_FINISHED_TRUE;
	}


	private boolean isGameValid(final GenericBoard game)
	{
		final byte validity = game.checkValidity();
		return validity == Utilities.BOARD_NEW_GAME;
	}


	public boolean isNewGameValid()
	{
		return isGameValid(newGameBoard);
	}


	public byte isNewMoveValid()
	{
		return oldGameBoard.checkValidity(newGameBoard);
	}


	public boolean isOldGameValid()
	{
		return isGameValid(oldGameBoard);
	}


	public boolean isTurn(final long id)
	{
		if (isChallengedsTurn() && id == userChallenged.getId())
		{
			return true;
		}
		else if (isCreatorsTurn() && id == userCreator.getId())
		{
			return true;
		}
		else
		{
			return false;
		}
	}


	public boolean isTypeCheckers()
	{
		return gameType == Utilities.POST_DATA_GAME_TYPE_CHECKERS;
	}


	public boolean isTypeChess()
	{
		return gameType == Utilities.POST_DATA_GAME_TYPE_CHESS;
	}


	public void makeId() throws UnsupportedEncodingException, NoSuchAlgorithmException, SQLException
	{
		if (!Utilities.verifyValidString(id))
		{
			final String userChallenged = String.valueOf(this.userChallenged);
			final String userCreator = String.valueOf(this.userCreator);
			final String random = String.valueOf(Utilities.getRandom().nextInt());

			boolean idCollisionOccurred = false;

			do
			{
				final MessageDigest digest = MessageDigest.getInstance(Utilities.MESSAGE_DIGEST_ALGORITHM);
				digest.update(gameType);
				digest.update(userChallenged.getBytes(Utilities.UTF8));
				digest.update(userCreator.getBytes(Utilities.UTF8));
				digest.update(board.getBytes(Utilities.UTF8));
				digest.update(random.getBytes(Utilities.UTF8));

				final BigInteger digestValue = new BigInteger(digest.digest());
				final StringBuilder digestBuilder = new StringBuilder(digestValue.toString(Utilities.MESSAGE_DIGEST_RADIX));

				for (int nibble = 0; digestBuilder.length() < Utilities.MESSAGE_DIGEST_LENGTH; )
				// We want a digest that's Utilities.MESSAGE_DIGEST_LENGTH
				// characters in length. At the time of this writing, we are aiming
				// for 80 characters long. The digest algorithm alone will give us
				// a few less than that. So here we're making up for that deficit
				// by continuously adding random characters to the digest until we
				// get a digest that is 80 characters long.
				{
					do
					// We don't want a negative number. Keep generating random ints
					// until we get one that's positive.
					{
						// don't allow the random number we've generated to be
						// greater than 15
						nibble = Utilities.getRandom().nextInt() % 16;
					}
					while (nibble < 0);

					switch (nibble)
					{
						case 0:
							digestBuilder.append('0');
							break;

						case 1:
							digestBuilder.append('1');
							break;

						case 2:
							digestBuilder.append('2');
							break;

						case 3:
							digestBuilder.append('3');
							break;

						case 4:
							digestBuilder.append('4');
							break;

						case 5:
							digestBuilder.append('5');
							break;

						case 6:
							digestBuilder.append('6');
							break;

						case 7:
							digestBuilder.append('7');
							break;

						case 8:
							digestBuilder.append('8');
							break;

						case 9:
							digestBuilder.append('9');
							break;

						case 10:
							digestBuilder.append('a');
							break;

						case 11:
							digestBuilder.append('b');
							break;

						case 12:
							digestBuilder.append('c');
							break;

						case 13:
							digestBuilder.append('d');
							break;

						case 14:
							digestBuilder.append('e');
							break;

						case 15:
							digestBuilder.append('f');
							break;
					}
				}

				id = digestBuilder.toString();

				final String statementString =
					"SELECT * " +
					" FROM " + DBConstants.TABLE_GAMES +
					" WHERE " + DBConstants.TABLE_GAMES_COLUMN_ID + " = ?";

				final PreparedStatement statement = DB.connection.prepareStatement(statementString);
				statement.setString(1, id);
				final ResultSet result = statement.executeQuery();

				if (result.next())
				{
					final byte finished = result.getByte(DBConstants.TABLE_GAMES_COLUMN_FINISHED);

					if (finished == DBConstants.TABLE_GAMES_FINISHED_FALSE)
					{
						idCollisionOccurred = true;
					}
					else
					{
						idCollisionOccurred = false;
					}
				}
				else
				{
					idCollisionOccurred = false;
				}

				DB.close(result, statement);
			}
			while (idCollisionOccurred);
		}
	}


	public JSONObject makeJSON() throws JSONException
	{
		final JSONObject gameJSON = new JSONObject();
		gameJSON.put(Utilities.POST_DATA_GAME_ID, id);
		gameJSON.put(Utilities.POST_DATA_GAME_TYPE, gameType);
		gameJSON.put(Utilities.POST_DATA_USER_CHALLENGED, userChallenged);
		gameJSON.put(Utilities.POST_DATA_USER_CREATOR, userCreator);
		gameJSON.put(Utilities.POST_DATA_BOARD, board);
		gameJSON.put(Utilities.POST_DATA_LAST_MOVE, getLastMoveInSeconds());

		return gameJSON;
	}


	private void readGameData() throws SQLException, Exception
	{
		final String statementString =
			"SELECT * " +
			" FROM " + DBConstants.TABLE_GAMES +
			" WHERE " + DBConstants.TABLE_GAMES_COLUMN_ID + " = ?";

		final PreparedStatement statement = DB.connection.prepareStatement(statementString);
		statement.setString(1, id);
		final ResultSet result = statement.executeQuery();

		if (result.next())
		{
			initFromSQLResult(result);
		}
		else
		{
			throw new Exception("Could not find game with ID of \"" + id + "\".");
		}

		DB.close(result, statement);
	}


	public void setFinished()
	{
		finished = DBConstants.TABLE_GAMES_FINISHED_TRUE;
	}


	public void switchTurns()
	{
		if (isChallengedsTurn())
		{
			turn = DBConstants.TABLE_GAMES_TURN_CREATOR;
		}
		else
		{
			turn = DBConstants.TABLE_GAMES_TURN_CHALLENGED;
		}
	}


	/**
	 * Saves this Game object's current data state to the database.
	 */
	public void update() throws JSONException, SQLException
	{
		final String statementString =
			"UPDATE " + DBConstants.TABLE_GAMES +
			" SET " + DBConstants.TABLE_GAMES_COLUMN_FINISHED + " = ?, " +
			DBConstants.TABLE_GAMES_COLUMN_GAME_TYPE + " = ?, " +
			DBConstants.TABLE_GAMES_COLUMN_TURN + " = ?, " +
			DBConstants.TABLE_GAMES_COLUMN_USER_CHALLENGED + " = ?, " +
			DBConstants.TABLE_GAMES_COLUMN_USER_CREATOR + " = ?, " +
			DBConstants.TABLE_GAMES_COLUMN_BOARD + " = ? " +
			"WHERE " + DBConstants.TABLE_GAMES_COLUMN_ID + " = ?";

		final PreparedStatement statement = DB.connection.prepareStatement(statementString);
		statement.setByte(1, finished);
		statement.setByte(2, gameType);
		statement.setByte(3, turn);
		statement.setLong(4, userChallenged.getId());
		statement.setLong(5, userCreator.getId());

		if (newGameBoard != null)
		{
			board = newGameBoard.makeJSON().toString();
		}
		else
		{
			board = oldGameBoard.makeJSON().toString();
		}

		statement.setString(6, board);
		statement.setString(7, id);
		statement.executeUpdate();

		DB.close(statement);
	}


}
