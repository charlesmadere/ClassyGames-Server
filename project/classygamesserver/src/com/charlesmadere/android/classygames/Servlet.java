package com.charlesmadere.android.classygames;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.charlesmadere.android.classygames.utilities.Utilities;


public abstract class Servlet extends HttpServlet
{


	private static final long serialVersionUID = 1L;


	protected PrintWriter printWriter;




	protected Servlet()
	{
		super();
	}


	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
		throws IOException, ServletException
	{
		prepare(response);
		printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_NOT_DETECTED));
	}


	protected void prepare(final HttpServletResponse response) throws IOException
	{
		response.setContentType(Utilities.CONTENT_TYPE_JSON);
		printWriter = response.getWriter();
	}


}
