package com.charlesmadere.android.classygames;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.charlesmadere.android.classygames.utilities.Utilities;


public final class GetStats extends Servlet
{


	private final static long serialVersionUID = 1L;


	private String param_userId;

	private Long userId;




	public GetStats()
	{
		super();
	}




	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
		throws IOException, ServletException
	{
		prepare(response);
		param_userId = request.getParameter(Utilities.POST_DATA_ID);

		if (Utilities.verifyValidString(param_userId))
		{
			userId = Long.valueOf(param_userId);
			printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_GENERIC));
		}
		else
		{
			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DATA_IS_EMPTY));
		}
	}


}
