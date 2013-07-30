package com.charlesmadere.android.classygames.models;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.charlesmadere.android.classygames.utilities.DB;
import com.charlesmadere.android.classygames.utilities.DBConstants;


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

		DB.close(result);
		DB.close(statement);
	}


	public void setBoard(final String board)
	{
		this.board = board;
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
	public void update() throws SQLException
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
		statement.setString(6, board);
		statement.setString(7, id);

		statement.executeUpdate();
		DB.close(statement);
	}


}
