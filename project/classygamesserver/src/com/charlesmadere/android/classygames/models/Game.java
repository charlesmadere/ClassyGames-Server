package com.charlesmadere.android.classygames.models;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONException;

import com.charlesmadere.android.classygames.models.games.GenericBoard;
import com.charlesmadere.android.classygames.utilities.DB;
import com.charlesmadere.android.classygames.utilities.DBConstants;
import com.charlesmadere.android.classygames.utilities.GameUtilities;


public final class Game
{


	private byte finished;
	private byte gameType;
	private byte turn;
	private long lastMove;
	private long userChallenged;
	private long userCreator;
	private String board;
	private String id;

	private GenericBoard oldGameBoard;
	private GenericBoard newGameBoard;




	public Game(final String id) throws SQLException
	{
		this.id = id;
		readGameData();
	}




	public boolean isFinished()
	{
		return finished == DBConstants.TABLE_GAMES_FINISHED_TRUE;
	}


	public boolean isChallengedsTurn()
	{
		return turn == DBConstants.TABLE_GAMES_TURN_CHALLENGED;
	}


	public boolean isCreatorsTurn()
	{
		return turn == DBConstants.TABLE_GAMES_TURN_CREATOR;
	}


	public byte getGameType()
	{
		return gameType;
	}


	public long getLastMove()
	{
		return lastMove;
	}


	public long getUserChallenged()
	{
		return userChallenged;
	}


	public long getUserCreator()
	{
		return userCreator;
	}


	public String getBoard()
	{
		return board;
	}


	public String getId()
	{
		return id;
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


	public boolean isTurn(final long id)
	{
		if (isChallengedsTurn() && id == userChallenged)
		{
			return true;
		}
		else if (isCreatorsTurn() && id == userCreator)
		{
			return true;
		}
		else
		{
			return false;
		}
	}


	private void readGameData() throws SQLException
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
			finished = result.getByte(DBConstants.TABLE_GAMES_COLUMN_FINISHED);
			gameType = result.getByte(DBConstants.TABLE_GAMES_COLUMN_GAME_TYPE);
			turn = result.getByte(DBConstants.TABLE_GAMES_COLUMN_TURN);
			lastMove = result.getLong(DBConstants.TABLE_GAMES_COLUMN_LAST_MOVE);
			userChallenged = result.getLong(DBConstants.TABLE_GAMES_COLUMN_USER_CHALLENGED);
			userCreator = result.getLong(DBConstants.TABLE_GAMES_COLUMN_USER_CREATOR);
			board = result.getString(DBConstants.TABLE_GAMES_COLUMN_BOARD);
		}

		DB.close(result, statement);
	}


	public void setFinished()
	{
		finished = DBConstants.TABLE_GAMES_FINISHED_TRUE;
	}


	public void setNewGameBoard(final String board)
	{
		newGameBoard = GameUtilities.newGame(board, gameType);
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
		statement.setLong(4, userChallenged);
		statement.setLong(5, userCreator);

		if (newGameBoard != null)
		{
			board = newGameBoard.makeJSON().toString();
		}
		else if (oldGameBoard != null)
		{
			board = oldGameBoard.makeJSON().toString();
		}

		statement.setString(6, board);
		statement.setString(7, id);
		statement.executeUpdate();

		DB.close(statement);
	}


}
