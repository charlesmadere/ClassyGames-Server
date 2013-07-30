package com.charlesmadere.android.classygames;


import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.charlesmadere.android.classygames.utilities.DB;
import com.charlesmadere.android.classygames.utilities.DBConstants;
import com.charlesmadere.android.classygames.utilities.Utilities;


public final class RemoveRegId extends Servlet
{


	private final static long serialVersionUID = 1L;


	private String param_userId;

	private Long userId;




	public RemoveRegId()
	{
		super();
	}




	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		prepare(response);
		param_userId = request.getParameter(Utilities.POST_DATA_ID);

		if (Utilities.verifyValidString(param_userId))
		// check inputs for validity
		{
			userId = Long.valueOf(param_userId);

			if (Utilities.verifyValidLong(userId))
			// check inputs for validity
			{
				try
				{
					removeRegId();
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
	private void removeRegId() throws SQLException, Exception
	{
		DB.open();
		DBConstants.updateUserRegId(sqlConnection, userId.longValue(), null);
		printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_USER_REMOVED_FROM_DATABASE));
	}


}
