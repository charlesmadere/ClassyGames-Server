package com.charlesmadere.android.classygames;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.charlesmadere.android.classygames.utilities.DatabaseUtilities;
import com.charlesmadere.android.classygames.utilities.Utilities;


public class NewRegId extends HttpServlet
{


	private final static long serialVersionUID = 1L;


	private Connection sqlConnection;
	private PreparedStatement sqlStatement;
	private PrintWriter printWriter;

	private String param_userId;
	private String param_userName;
	private String param_userRegId;

	private Long userId;




	public NewRegId()
	{
		super();
	}


	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		response.setContentType(Utilities.CONTENT_TYPE_JSON);
		printWriter = response.getWriter();
		printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_NOT_DETECTED));
	}


	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		response.setContentType(Utilities.CONTENT_TYPE_JSON);
		printWriter = response.getWriter();

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
					DatabaseUtilities.closeSQL(sqlConnection, sqlStatement);
				}
			}
			else
			{
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_MALFORMED));
			}
		}
		else
		{
			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_MALFORMED));
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
		sqlConnection = DatabaseUtilities.acquireSQLConnection();

		// prepare a SQL statement to be run on the database
		String sqlStatementString = "SELECT * FROM " + DatabaseUtilities.TABLE_USERS + " WHERE " + DatabaseUtilities.TABLE_USERS_COLUMN_ID + " = ?";
		sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

		// prevent SQL injection by inserting data this way
		sqlStatement.setLong(1, userId.longValue());

		// run the SQL statement and acquire any return information
		final ResultSet sqlResult = sqlStatement.executeQuery();

		if (sqlResult.next())
		// the id already exists in the table therefore it's data needs to be updated
		{
			DatabaseUtilities.closeSQLStatement(sqlStatement);

			// prepare a SQL statement to be run on the database
			sqlStatementString = "UPDATE " + DatabaseUtilities.TABLE_USERS + " SET " + DatabaseUtilities.TABLE_USERS_COLUMN_NAME + " = ?, " + DatabaseUtilities.TABLE_USERS_COLUMN_REG_ID + " = ? WHERE " + DatabaseUtilities.TABLE_USERS_COLUMN_ID + " = ?";
			sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

			// prevent SQL injection by inserting data this way
			sqlStatement.setString(1, param_userName);
			sqlStatement.setString(2, param_userRegId);
			sqlStatement.setLong(3, userId.longValue());
		}
		else
		// id does not already exist in the table. let's insert it
		{
			DatabaseUtilities.closeSQLStatement(sqlStatement);

			// prepare a SQL statement to be run on the database
			sqlStatementString = "INSERT INTO " + DatabaseUtilities.TABLE_USERS + " " + DatabaseUtilities.TABLE_USERS_FORMAT + " " + DatabaseUtilities.TABLE_USERS_VALUES;
			sqlStatement = sqlConnection.prepareStatement(sqlStatementString);

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
