package com.charlesmadere.android.classygames;


import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.charlesmadere.android.classygames.models.User;
import com.charlesmadere.android.classygames.utilities.DB;
import com.charlesmadere.android.classygames.utilities.Utilities;


public final class NewRegId extends Servlet
{


	private final static long serialVersionUID = 1L;


	private String param_userId;
	private String param_userName;
	private String param_userRegId;

	private long userId;




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

		if (Utilities.verifyValidString(param_userId, param_userName))
		// check inputs for validity
		{
			userId = Long.valueOf(param_userId);

			try
			{
				DB.open();
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
				DB.close();
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
		final User user = new User(userId, param_userName, param_userRegId);
		user.update();

		printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_USER_ADDED_TO_DATABASE));
	}


}
