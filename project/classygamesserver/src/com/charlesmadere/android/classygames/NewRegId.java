package com.charlesmadere.android.classygames;


import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.charlesmadere.android.classygames.utilities.DB;
import com.charlesmadere.android.classygames.utilities.DBConstants;
import com.charlesmadere.android.classygames.utilities.Utilities;


public final class NewRegId extends Servlet
{


	private final static long serialVersionUID = 1L;


	private String param_userId;
	private String param_userName;
	private String param_userRegId;

	private Long userId;




	public NewRegId()
	{
		super();
	}




	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		prepare(response);
		param_userId = request.getParameter(Utilities.POST_DATA_ID);
		param_userName = request.getParameter(Utilities.POST_DATA_NAME);
		param_userRegId = request.getParameter(Utilities.POST_DATA_REG_ID);

		if (Utilities.verifyValidStrings(param_userId, param_userName, param_userRegId))
		// check inputs for validity
		{
			userId = Long.valueOf(param_userId);

			if (Utilities.verifyValidLong(userId))
			// check inputs for validity
			{
				try
				{
					newRegId();
				}
				catch (final SQLException e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_CONNECT));
				}
				catch (final Exception e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_GENERIC));
				}
				finally
				{
					DB.close(sqlStatement);
					DB.close();
				}
			}
			else
			{
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_MALFORMED));
			}
		}
		else
		{
			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_EMPTY));
		}
	}


	/**
	 * Runs the meat of this servlet's code.
	 * 
	 * @throws SQLException
	 * If at some point there is some kind of connection error or query problem
	 * with the SQL database then this Exception will be thrown.
	 * 
	 * @throws Exception
	 * If the JDBC driver could not be loaded then this Exception will be
	 * thrown.
	 */
	private void newRegId() throws SQLException, Exception
	{
		DB.open();

		// prepare a SQL statement to be run on the database
		String sqlStatementString = "SELECT * FROM " + DBConstants.TABLE_USERS + " WHERE " + DBConstants.TABLE_USERS_COLUMN_ID + " = ?";
		sqlStatement = DB.connection.prepareStatement(sqlStatementString);

		// prevent SQL injection by inserting data this way
		sqlStatement.setLong(1, userId.longValue());

		// run the SQL statement and acquire any return information
		final ResultSet sqlResult = sqlStatement.executeQuery();

		if (sqlResult.next())
		// the id already exists in the table therefore it's data needs to be updated
		{
			DB.close(sqlStatement);

			// prepare a SQL statement to be run on the database
			sqlStatementString = "UPDATE " + DBConstants.TABLE_USERS + " SET " + DBConstants.TABLE_USERS_COLUMN_NAME + " = ?, " + DBConstants.TABLE_USERS_COLUMN_REG_ID + " = ? WHERE " + DBConstants.TABLE_USERS_COLUMN_ID + " = ?";
			sqlStatement = DB.connection.prepareStatement(sqlStatementString);

			// prevent SQL injection by inserting data this way
			sqlStatement.setString(1, param_userName);
			sqlStatement.setString(2, param_userRegId);
			sqlStatement.setLong(3, userId.longValue());
		}
		else
		// id does not already exist in the table. let's insert it
		{
			DB.close(sqlStatement);

			// prepare a SQL statement to be run on the database
			sqlStatementString = "INSERT INTO " + DBConstants.TABLE_USERS + " " + DBConstants.TABLE_USERS_FORMAT + " " + DBConstants.TABLE_USERS_VALUES;
			sqlStatement = DB.connection.prepareStatement(sqlStatementString);

			// prevent SQL injection by inserting data this way
			sqlStatement.setLong(1, userId.longValue());
			sqlStatement.setString(2, param_userName);
			sqlStatement.setString(3, param_userRegId);
		}

		// run the SQL statement
		sqlStatement.executeUpdate();

		printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_USER_ADDED_TO_DATABASE));
	}


}
