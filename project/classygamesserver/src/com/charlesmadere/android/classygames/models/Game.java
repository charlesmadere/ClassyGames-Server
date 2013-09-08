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
import com.charlesmadere.android.classygames.utilities.GameUtilities;
import com.charlesmadere.android.classygames.utilities.Utilities;


/**
 * Represents the Game database table. All of that table's properties can be
 * found and accessed here.
 */
public final class Game
{




	/**
	 * This class's database table column information is stored here.
	 */
	public final static class Table
	{


		public final static String TABLE = "games";
		public final static String COLUMN_ID = "id";
		public final static String COLUMN_USER_CREATOR = "user_creator";
		public final static String COLUMN_USER_CHALLENGED = "user_challenged";
		public final static String COLUMN_BOARD = "board";
		public final static String COLUMN_TURN = "turn";
		public final static String COLUMN_GAME_TYPE = "game_type";
		public final static String COLUMN_LAST_MOVE = "last_move";
		public final static String COLUMN_FINISHED = "finished";
		public final static byte TURN_CREATOR = 1;
		public final static byte TURN_CHALLENGED = 2;
		public final static byte FINISHED_FALSE = 1;
		public final static byte FINISHED_TRUE = 2;


	}




	/**
	 * Represents whether this Game object is finished or not. Will equal
	 * Table.FINISHED_FALSE if it's not yet finished, or Table.FINISHED_TRUE if
	 * it is finished.
	 */
	private byte finished;


	/**
	 * Represents what type of Game this is. Currently this could be Checkers
	 * or Chess.
	 */
	private byte gameType;


	/**
	 * Represents which user's turn it currently is for this Game. Will equal
	 * either Table.TURN_CREATOR or Table.TURN_CHALLENGED.
	 */
	private byte turn;


	/**
	 * Represents the current state of the Game board as it exists in the
	 * database. This String is actually JSON data and can be converted
	 * directly to that form for proper reading.
	 */
	private String board;


	/**
	 * This Game's ID. Is a kinda long String (around 64+ characters) that is a
	 * hash of alphanumeric characters.
	 */
	private String id;


	/**
	 * The Unix epoch of when the last move in this Game took place.
	 */
	private Timestamp lastMove;


	/**
	 * The User that this Game's original creator decided to play against.
	 */
	private User userChallenged;


	/**
	 * This Game's original creator.
	 */
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
		finished = Table.FINISHED_FALSE;

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
			id = result.getString(Table.COLUMN_ID);
		}

		finished = result.getByte(Table.COLUMN_FINISHED);
		gameType = result.getByte(Table.COLUMN_GAME_TYPE);
		turn = result.getByte(Table.COLUMN_TURN);
		lastMove = result.getTimestamp(Table.COLUMN_LAST_MOVE);

		if (Utilities.verifyValidString(board))
		{
			newGameBoard = GameUtilities.newGame(board, gameType);

			final String oldBoard = result.getString(Table.COLUMN_BOARD);
			oldGameBoard = GameUtilities.newGame(oldBoard, gameType);
		}
		else
		{
			board = result.getString(Table.COLUMN_BOARD);
			newGameBoard = GameUtilities.newGame(board, gameType);
		}

		if (userChallenged == null)
		{
			final long userChallengedId = result.getLong(Table.COLUMN_USER_CHALLENGED);
			userChallenged = new User(userChallengedId);
		}

		if (userCreator == null)
		{
			final long userCreatorId = result.getLong(Table.COLUMN_USER_CREATOR);
			userCreator = new User(userCreatorId);
		}
	}


	public boolean isChallengedsTurn()
	{
		return turn == Table.TURN_CHALLENGED;
	}


	public boolean isCreatorsTurn()
	{
		return turn == Table.TURN_CREATOR;
	}


	public boolean isFinished()
	{
		return finished == Table.FINISHED_TRUE;
	}


	private boolean isGameValid(final GenericBoard game)
	{
		final byte validity = game.checkValidity();
		return validity == GameUtilities.BOARD_NEW_GAME;
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
					"SELECT " + Table.COLUMN_ID +
					" FROM " + Table.TABLE +
					" WHERE " + Table.COLUMN_ID + " = ?";

				final PreparedStatement statement = DB.connection.prepareStatement(statementString);
				statement.setString(1, id);
				final ResultSet result = statement.executeQuery();

				if (result.next())
				{
					final byte finished = result.getByte(Table.COLUMN_FINISHED);

					if (finished == Table.FINISHED_FALSE)
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
		final JSONObject gameJSON = new JSONObject()
			.put(Utilities.POST_DATA_GAME_ID, id)
			.put(Utilities.POST_DATA_GAME_TYPE, gameType)
			.put(Utilities.POST_DATA_USER_CHALLENGED, userChallenged)
			.put(Utilities.POST_DATA_USER_CREATOR, userCreator)
			.put(Utilities.POST_DATA_BOARD, board)
			.put(Utilities.POST_DATA_LAST_MOVE, getLastMoveInSeconds());

		return gameJSON;
	}


	private void readGameData() throws SQLException, Exception
	{
		final String statementString =
			"SELECT * " +
			" FROM " + Table.TABLE +
			" WHERE " + Table.COLUMN_ID + " = ?";

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
		finished = Table.FINISHED_TRUE;
	}


	public void switchTurns()
	{
		if (isChallengedsTurn())
		{
			turn = Table.TURN_CREATOR;
		}
		else
		{
			turn = Table.TURN_CHALLENGED;
		}
	}


	/**
	 * Saves this Game object's current data state to the database.
	 */
	public void update() throws JSONException, SQLException
	{
		final String statementString =
			"UPDATE " + Table.TABLE +
			" SET " + Table.COLUMN_FINISHED + " = ?, " +
			Table.COLUMN_GAME_TYPE + " = ?, " +
			Table.COLUMN_TURN + " = ?, " +
			Table.COLUMN_USER_CHALLENGED + " = ?, " +
			Table.COLUMN_USER_CREATOR + " = ?, " +
			Table.COLUMN_BOARD + " = ? " +
			"WHERE " + Table.COLUMN_ID + " = ?";

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
