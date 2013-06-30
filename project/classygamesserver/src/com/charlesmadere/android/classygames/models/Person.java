package com.charlesmadere.android.classygames.models;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.charlesmadere.android.classygames.utilities.DatabaseUtilities;
import com.charlesmadere.android.classygames.utilities.Utilities;


/**
 * Class representing a real person.
 */
public class Person
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
	 * Creates a Person object.
	 * 
	 * @param id
	 * The Facebook ID of the user.
	 * 
	 * @param name
	 * The Facebook name of the user.
	 */
	public Person(final long id, final String name)
	{
		this.id = id;
		this.name = name;
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
	public Person(final long id, final String name, final String regId)
	{
		this.id = id;
		this.name = name;
		this.regId = regId;
	}


	/**
	 * @return
	 * Returns this Person's Facebook ID (a long).
	 */
	public long getId()
	{
		return id;
	}


	/**
	 * Converts this Person's Facebook ID (a long) into a String and then
	 * returns that String.
	 * 
	 * @return
	 * Returns this Person's Facebook ID as a String.
	 */
	public String getIdAsString()
	{
		return String.valueOf(id);
	}


	/**
	 * Returns this Person's Facebook name (a String). This is that person's
	 * <strong>whole name</strong>.
	 * 
	 * @return
	 * Returns this Person's Facebook name (a String).
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
	 * Replaces this Person object's current Facebook ID with this newly given
	 * id. An ID should be a number that is always greater than 0. This method
	 * will convert the given String into a long. As this method doesn't check
	 * for the possibility that a null String was given to it, this method will
	 * cause a crash if that happens.
	 * 
	 * @param id
	 * New Facebook ID of the user.
	 */
	public void setId(final String id)
	{
		this.id = Long.parseLong(id);
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
	 * If this Person object does not already have an Android registration ID
	 * associated with it, then this method will attempt to find it in the
	 * database. Note that it is possible for the registration ID to not be
	 * able to be found. This is nothing really to worry about, but just know
	 * then that this Person will not be able to receive push notifications.
	 * 
	 * @param sqlConnection
	 * An existing open connection to the database.
	 * 
	 * @throws SQLException
	 * If a database connection or query problem occurs, then this Exception
	 * will be thrown.
	 */
	public void findRegId(final Connection sqlConnection) throws SQLException
	{
		if (!hasRegId())
		{
			final String sqlStatementString =
				"SELECT * " +
				" FROM " + DatabaseUtilities.TABLE_USERS +
				" WHERE " + DatabaseUtilities.TABLE_USERS_COLUMN_ID + " = ?";
	
			final PreparedStatement sqlStatement = sqlConnection.prepareStatement(sqlStatementString);
			sqlStatement.setLong(1, id);
	
			final ResultSet sqlResult = sqlStatement.executeQuery();
	
			if (sqlResult.next())
			{
				regId = sqlResult.getString(DatabaseUtilities.TABLE_USERS_COLUMN_REG_ID);
			}
		}
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


	/**
	 * Checks to see that this Person object is valid. Valid means three
	 * things:
	 * <ol>
	 * <li>This Person's Facebook ID is greater than or equal to 1.</li>
	 * <li>This Person's name is not null.</li>
	 * <li>This Person's name is not empty.</li>
	 * </ol>
	 * 
	 * @return
	 * Returns true if all of the above conditions are true. Returns false if
	 * any single one of the above conditions are false.
	 */
	public boolean isValid()
	{
		return isIdValid(id) && isNameValid(name);
	}




	@Override
	public String toString()
	{
		return name;
	}




	/**
	 * When Facebook IDs are acquired throughout the app's runtime they should
	 * be checked for validity. Use this method to check for that validity.
	 * Valid means one thing:
	 * <ol>
	 * <li>This ID is greater than or equal to 1.</li>
	 * </ol>
	 * 
	 * @param id
	 * The Facebook ID to check for validity.
	 * 
	 * @return
	 * Returns true if the above condition is true. Returns false if the above
	 * condition is false.
	 */
	public static boolean isIdValid(final long id)
	{
		return id >= 1;
	}


	/**
	 * When Facebook IDs are acquired throughout the app's runtime they should
	 * be checked for validity. Use this method to check for that validity.
	 * Valid means three things:
	 * <ol>
	 * <li>This String is not null.</li>
	 * <li>This String has a length of greater than or equal to 1.</li>
	 * <li>This String, when converted into a long, is greater than or equal to
	 * 1.</li>
	 * </ol>
	 * 
	 * @param id
	 * The Facebook ID to check for validity. This parameter is converted into
	 * a long. This String is 
	 * 
	 * @return
	 * Returns true if the above condition is true. Returns false if the above
	 * condition is false.
	 */
	public static boolean isIdValid(final String id)
	{
		if (Utilities.verifyValidString(id))
		// First, ensure that we were given a valid String. If this proves true
		// then we will check to see that the long value of this String is a
		// valid ID value.
		{
			return isIdValid(Long.parseLong(id));
		}
		else
		{
			return false;
		}
	}


	/**
	 * When Facebook IDs are acquired throughout the app's runtime they should
	 * be checked for validity. Use this method to check for that validity.
	 * This method allows you to check a whole bunch of IDs at once. If any
	 * single ID that is passed in is invalid, then this method will cease to
	 * check the rest and will immediately return false.
	 * 
	 * @param ids
	 * The Facebook IDs to check for validity.
	 * 
	 * @return
	 * Returns true if <strong>all</strong> of the passed in Facebook IDs are
	 * valid. Returns false if <strong>any single</strong> ID is invalid.
	 */
	public static boolean areIdsValid(final long... ids)
	{
		for (final long id : ids)
		{
			if (!isIdValid(id))
			{
				return false;
			}
		}

		return true;
	}


	/**
	 * When Facebook IDs are acquired throughout the app's runtime they should
	 * be checked for validity. Use this method to check for that validity.
	 * This method allows you to check a whole bunch of IDs at once. If any
	 * single ID that is passed in is invalid, then this method will cease to
	 * check the rest and will immediately return false.
	 * 
	 * @param ids
	 * The Facebook IDs to check for validity.
	 * 
	 * @return
	 * Returns true if <strong>all</strong> of the passed in Facebook IDs are
	 * valid. Returns false if <strong>any single</strong> ID is invalid.
	 */
	public static boolean areIdsValid(final String... ids)
	{
		for (final String id : ids)
		{
			if (!isIdValid(id))
			{
				return false;
			}
		}

		return true;
	}


	/**
	 * When Facebook names are acquired throughout the app's runtime they
	 * should be checked to make sure they're not messed up in any way. Use
	 * this method to check to make sure that they're not messed up. Valid
	 * means three things:
	 * <ol>
	 * <li>This String is not null.</li>
	 * <li>This String has a length of greater than or equal to 1.</li>
	 * </ol>
	 * 
	 * @return
	 * Returns true if the passed in Facebook name is valid.
	 */
	public static boolean isNameValid(final String name)
	{
		return Utilities.verifyValidString(name);
	}


	/**
	 * When Facebook names are acquired throughout the app's runtime they
	 * should be checked to make sure they're not messed up in any way. This
	 * method allows you to check a whole bunch of names at once. If any single
	 * name that is passed in is invalid, then this method will cease to check
	 * the rest and will immediately return false.
	 * 
	 * @param names
	 * The Facebook names to check for validity.
	 * 
	 * @return
	 * Returns true if <strong>all</strong> of the passed in Facebook names are
	 * valid. Returns false if <strong>any single</strong> name is invalid.
	 */
	public static boolean areNamesValid(final String... names)
	{
		for (final String name : names)
		{
			if (!isNameValid(name))
			{
				return false;
			}
		}

		return true;
	}


	/**
	 * Checks a given ID and name for validity. Some of the other validity
	 * verifying methods in this class define exactly what <i>validity</i> is.
	 * See those for more information!
	 *
	 * @param id
	 * The Facebook ID to check for validity.
	 *
	 * @param name
	 * The name to check for validity.
	 *
	 * @return
	 * Returns true if the values of both given parameters are detected as
	 * being valid.
	 */
	public static boolean isIdAndNameValid(final long id, final String name)
	{
		return isIdValid(id) && isNameValid(name);
	}


}