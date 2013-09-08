package com.charlesmadere.android.classygames;


import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.charlesmadere.android.classygames.models.GCMMessage;
import com.charlesmadere.android.classygames.models.Game;
import com.charlesmadere.android.classygames.models.User;
import com.charlesmadere.android.classygames.utilities.DB;
import com.charlesmadere.android.classygames.utilities.Utilities;


public final class ForfeitGame extends Servlet
{


	private final static long serialVersionUID = 1L;


	private String param_userChallengedId;
	private String param_userCreatorId;
	private String param_gameId;

	private long userChallengedId;
	private long userCreatorId;




	public ForfeitGame()
	{
		super();
	}




	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
		throws IOException, ServletException
	{
		prepare(response);
		param_userChallengedId = request.getParameter(Utilities.POST_DATA_USER_CHALLENGED);
		param_userCreatorId = request.getParameter(Utilities.POST_DATA_USER_CREATOR);
		param_gameId = request.getParameter(Utilities.POST_DATA_GAME_ID);

		if (Utilities.verifyValidString(param_userChallengedId, param_userCreatorId, param_gameId))
		// check inputs for validity
		{
			userChallengedId = Long.parseLong(param_userChallengedId);
			userCreatorId = Long.parseLong(param_userCreatorId);

			try
			{
				DB.open();
				forfeitGame();
			}
			catch (final IOException e)
			{
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_GCM_FAILED_TO_SEND));
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
	 * @throws IOException
	 * An IOException could be thrown when the GCM message is attempted to be
	 * sent.
	 * 
	 * @throws SQLException
	 * If at some point there is some kind of connection error or query problem
	 * with the SQL database then this Exception will be thrown.
	 * 
	 * @throws Exception
	 * If the JDBC driver could not be loaded then this Exception will be
	 * thrown.
	 */
	private void forfeitGame() throws IOException, SQLException, Exception
	{
		final Game game = new Game(param_gameId);

		if (game.isFinished())
		{
			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_GAME_IS_ALREADY_OVER));
		}
		else
		{
			game.setFinished();
			game.update();

			final User challenged = new User(userChallengedId);
			final User creator = new User(userCreatorId);

			if (game.isTypeCheckers())
			{
				challenged.incrementCheckersWins();
				creator.incrementCheckersLoses();
			}
			else if (game.isTypeChess())
			{
				challenged.incrementChessWins();
				creator.incrementChessLoses();
			}

			challenged.update();
			creator.update();

			new GCMMessage()
				.setGameId(param_gameId)
				.setMessageTypeGameOverWin()
				.setUserToMention(creator)
				.setUserToReceive(challenged)
				.sendMessage();

			new GCMMessage()
				.setGameId(param_gameId)
				.setMessageTypeGameOverLose()
				.setUserToMention(challenged)
				.setUserToReceive(creator)
				.sendMessage();

			printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_GENERIC));
		}
	}


}
