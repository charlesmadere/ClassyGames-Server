package com.charlesmadere.android.classygames;


import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.charlesmadere.android.classygames.models.User;
import com.charlesmadere.android.classygames.utilities.DB;
import com.charlesmadere.android.classygames.utilities.Utilities;


public final class GetGames extends Servlet
{


	private final static long serialVersionUID = 1L;


	private String param_userId;

	private Long userId;




	public GetGames()
	{
		super();
	}


	@Override
	protected void doPost(final HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
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
					DB.open();
					getGames();
				}
				catch (final JSONException e)
				{
					printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_JSON_EXCEPTION));
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
	 * @throws JSONException
	 * If at some point the JSON data that this method tries to create has an
	 * issue then this Exception will be thrown.
	 * 
	 * @throws SQLException
	 * If at some point there is some kind of connection error or query problem
	 * with the SQL database then this Exception will be thrown.
	 * 
	 * @throws Exception
	 * If the JDBC driver could not be loaded then this Exception will be
	 * thrown.
	 */
	private void getGames() throws JSONException, SQLException, Exception
	{
		final User user = new User(userId);
		user.readGames();

		final JSONArray myTurnGames = user.makeMyTurnGamesJSON();
		final JSONArray theirTurnGames = user.makeTheirTurnGamesJSON();

		final JSONObject games = new JSONObject();
		games.put(Utilities.POST_DATA_TURN_YOURS, myTurnGames);
		games.put(Utilities.POST_DATA_TURN_THEIRS, theirTurnGames);

		printWriter.write(Utilities.makePostDataSuccess(games));
	}


}
