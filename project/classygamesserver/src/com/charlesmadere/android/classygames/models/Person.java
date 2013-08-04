package com.charlesmadere.android.classygames.models;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.charlesmadere.android.classygames.utilities.DB;
import com.charlesmadere.android.classygames.utilities.DBConstants;
import com.charlesmadere.android.classygames.utilities.Utilities;


public final class Person
{


	/**
	 * The person's Facebook ID. This is a unique, always positive, number
	 * across the entire Facebook system.
	 */
	private long id;


	/**
	 * The person's Facebook name. It could be "Charles Madere". Obviously this
	 * is not a unique identifier for this person. This class's long id
	 * variable, however, <strong>is</strong> a unique identifier.
	 */
	private String name;


	/**
	 * The person's Android device's registration ID. This is the unique ID
	 * that is necessary in order for a push notification to be sent to his or
	 * her device. Note that this is just a single String. This means that
	 * Classy Games can only send a push notification to one of the user's
	 * devices.
	 */
	private String regId;


	/**
	 * The number of checkers loses that this Person has.
	 */
	private int checkersLoses;


	/**
	 * The number of checkers wins that this Person has.
	 */
	private int checkersWins;


	/**
	 * The number of chess loses that this Person has.
	 */
	private int chessLoses;


	/**
	 * The number of chess wins that this Person has.
	 */
	private int chessWins;




	/**
	 * Creates a Person object from the given ID and then reads the remaining
	 * data from the database.
	 * 
	 * @param id
	 * The Facebook ID of the user.
	 * 
	 * @throws SQLException
	 * If a database connection or query problem occurs, then this Exception
	 * will be thrown.
	 */
	public Person(final long id) throws SQLException
	{
		this.id = id;
		readPersonData();
	}


	/**
	 * Creates a Person object.
	 * 
	 * @param id
	 * The Facebook ID of the user.
	 * 
	 * @param name
	 * The Facebook name of the user.
	 * 
	 * @param regId
	 * The registration ID of the user's Android device.
	 */
	public Person(final long id, final String name, final String regId) throws SQLException
	{
		this.id = id;
		this.name = name;
		this.regId = regId;
		readPersonData();
	}




	public int getCheckersLoses()
	{
		return checkersLoses;
	}


	public int getCheckersWins()
	{
		return checkersWins;
	}


	public int getChessLoses()
	{
		return chessLoses;
	}


	public int getChessWins()
	{
		return chessWins;
	}


	/**
	 * @return
	 * Returns this Person's Facebook ID.
	 */
	public long getId()
	{
		return id;
	}


	/**
	 * @return
	 * Returns this Person's Facebook name. This is that person's whole name.
	 */
	public String getName()
	{
		return name;
	}


	/**
	 * @return
	 * Returns this Person's Android device's registration ID.
	 */
	public String getRegId()
	{
		return regId;
	}


	/**
	 * Replaces this Person object's current Facebook ID with this newly given
	 * id. An ID should be a number that is always greater than 0.
	 * 
	 * @param id
	 * New Facebook ID of the user.
	 */
	public void setId(final long id)
	{
		this.id = id;
	}


	/**
	 * Replaces this Person object's current Facebook name with this newly
	 * given name. 
	 * 
	 * @param name
	 * New Facebook name of the user.
	 */
	public void setName(final String name)
	{
		this.name = name;
	}


	/**
	 * Replaces this Person object's current Android registration ID with this
	 * new one.
	 * 
	 * @param regId
	 * The new registration ID of the user's Android device.
	 */
	public void setRegId(final String regId)
	{
		this.regId = regId;
	}


	/**
	 * Checks to see that this Person object has a valid Android registration
	 * ID.
	 * 
	 * @return
	 * Returns true if this Person object has a valid Android registration ID.
	 */
	public boolean hasRegId()
	{
		return Utilities.verifyValidString(regId);
	}


	public void incrementCheckersLoses()
	{
		++checkersLoses;
	}


	public void incrementCheckersWins()
	{
		++checkersWins;
	}


	public void incrementChessLoses()
	{
		++chessLoses;
	}


	public void incrementChessWins()
	{
		++chessWins;
	}


	private void readPersonData() throws SQLException
	{
		final String statementString =
			"SELECT * " +
			" FROM " + DBConstants.TABLE_USERS +
			" WHERE " + DBConstants.TABLE_USERS_COLUMN_ID + " = ?";

		final PreparedStatement statement = DB.connection.prepareStatement(statementString);
		statement.setLong(1, id);
		final ResultSet result = statement.executeQuery();

		if (result.next())
		{
			if (!Utilities.verifyValidString(regId))
			{
				regId = result.getString(DBConstants.TABLE_USERS_COLUMN_REG_ID);
			}

			checkersLoses = result.getInt(DBConstants.TABLE_USERS_COLUMN_CHECKERS_LOSES);
			checkersWins = result.getInt(DBConstants.TABLE_USERS_COLUMN_CHECKERS_WINS);
			chessLoses = result.getInt(DBConstants.TABLE_USERS_COLUMN_CHESS_LOSES);
			chessWins = result.getInt(DBConstants.TABLE_USERS_COLUMN_CHESS_WINS);
		}

		DB.close(result);
		DB.close(statement);
	}


	/**
	 * Saves this Person object's current data state to the database.
	 */
	public void update() throws SQLException
	{
		final String statementString =
			"UPDATE " + DBConstants.TABLE_USERS +
			" SET " + DBConstants.TABLE_USERS_COLUMN_NAME + " = ?, " +
			DBConstants.TABLE_USERS_COLUMN_REG_ID + " = ?, " +
			DBConstants.TABLE_USERS_COLUMN_CHECKERS_LOSES + " = ?, " +
			DBConstants.TABLE_USERS_COLUMN_CHECKERS_WINS + " = ?, " +
			DBConstants.TABLE_USERS_COLUMN_CHESS_LOSES + " = ?, " +
			DBConstants.TABLE_USERS_COLUMN_CHESS_WINS + " = ?, " +
			"WHERE " + DBConstants.TABLE_USERS_COLUMN_ID + " = ?";

		final PreparedStatement statement = DB.connection.prepareStatement(statementString);
		statement.setString(1, name);
		statement.setString(2, regId);
		statement.setInt(3, checkersLoses);
		statement.setInt(4, checkersWins);
		statement.setInt(5, chessLoses);
		statement.setInt(6, chessWins);
		statement.setLong(7, id);

		statement.executeUpdate();
		DB.close(statement);
	}


}
