package edu.selu.android.classygames;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.selu.android.classygames.utilities.DatabaseUtilities;
import edu.selu.android.classygames.utilities.Utilities;


/**
 * Servlet implementation class RemoveRegId
 */
public class RemoveRegId extends HttpServlet
{


	private final static long serialVersionUID = 1L;


	private Connection sqlConnection;
	private PrintWriter printWriter;

	private String parameter_userId;

	private Long userId;




	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RemoveRegId()
	{
		super();
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		response.setContentType(Utilities.CONTENT_TYPE_JSON);
		printWriter = response.getWriter();
		printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_NOT_DETECTED));
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		response.setContentType(Utilities.CONTENT_TYPE_JSON);
		printWriter = response.getWriter();

		parameter_userId = request.getParameter(Utilities.POST_DATA_ID);

		if (Utilities.verifyValidString(parameter_userId))
		// check inputs for validity
		{
			userId = Long.valueOf(parameter_userId);

			if (Utilities.verifyValidLong(userId))
			// check inputs for validity
			{
				removeRegId();
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


	private void removeRegId()
	{
		try
		{
			sqlConnection = DatabaseUtilities.getSQLConnection();
			DatabaseUtilities.removeUserRegId(sqlConnection, userId.longValue());
			printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_USER_REMOVED_FROM_DATABASE));
		}
		catch (final SQLException e)
		{
			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATABASE_COULD_NOT_CONNECT));
		}
		finally
		{
			DatabaseUtilities.closeSQLConnection(sqlConnection);
		}
	}


}
