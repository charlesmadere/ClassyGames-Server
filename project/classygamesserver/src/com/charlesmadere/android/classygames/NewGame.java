package com.charlesmadere.android.classygames;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.charlesmadere.android.classygames.models.GCMMessage;
import com.charlesmadere.android.classygames.models.Game;
import com.charlesmadere.android.classygames.models.User;
import com.charlesmadere.android.classygames.utilities.DB;
import com.charlesmadere.android.classygames.utilities.Utilities;


public final class NewGame extends Servlet
{


	private final static long serialVersionUID = 1L;


	private String param_userChallengedId;
	private String param_userChallengedName;
	private String param_userCreatorId;
	private String param_gameType;
	private String param_board;

	private Long userChallengedId;
	private Long userCreatorId;
	private Byte gameType;




	public NewGame()
	{
		super();
	}




	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
		throws IOException, ServletException
	{
		prepare(response);
		param_userChallengedId = request.getParameter(Utilities.POST_DATA_USER_CHALLENGED);
		param_userChallengedName = request.getParameter(Utilities.POST_DATA_NAME);
		param_userCreatorId = request.getParameter(Utilities.POST_DATA_USER_CREATOR);
		param_gameType = request.getParameter(Utilities.POST_DATA_GAME_TYPE);
		param_board = request.getParameter(Utilities.POST_DATA_BOARD);

		if (Utilities.verifyValidStrings(param_userChallengedId, param_userChallengedName, param_userCreatorId, param_board))
		{
			userChallengedId = Long.valueOf(param_userChallengedId);
			userCreatorId = Long.valueOf(param_userCreatorId);

			if (Utilities.verifyValidString(param_gameType))
			// check to see if we were given a gameType parameter
			{
				gameType = Byte.valueOf(param_gameType);
			}
			else
			{
				gameType = Byte.valueOf(Utilities.POST_DATA_GAME_TYPE_CHECKERS);
			}

			try
			{
				DB.open();
				newGame();
			}
			catch (final UnsupportedEncodingException e)
			{
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DIGEST_HAD_IMPROPER_ENCODING));
			}
			catch (final IOException e)
			{
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_GCM_FAILED_TO_SEND));
			}
			catch (final JSONException e)
			{
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_JSON_EXCEPTION));
			}
			catch (final NoSuchAlgorithmException e)
			{
				printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_DIGEST_HAD_UNSUPPORTED_ALGORITHM));
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
	 * @throws UnsupportedEncodingException
	 * If the character encoding that we try to convert a randomly generated
	 * int into (which at the time of this writing is UTF-8) turns out to be
	 * unsupported, then this Exception will be thrown.
	 * 
	 * @throws IOException
	 * An IOException could be thrown when the GCM message is attempted to be
	 * sent.
	 * 
	 * @throws JSONException
	 * If at some point the JSON data that this method tries to create has an
	 * issue then this Exception will be thrown.
	 * 
	 * @throws NoSuchAlgorithmException
	 * If the algorithm that we try to use in creating the digest (which at the
	 * time of this writing is SHA-256) doesn't exist, then this Exception will
	 * be thrown.
	 * 
	 * @throws SQLException
	 * If at some point there is some kind of connection error or query problem
	 * with the SQL database then this Exception will be thrown.
	 * 
	 * @throws Exception
	 * If the JDBC driver could not be loaded then this Exception will be
	 * thrown.
	 */
	private void newGame() throws UnsupportedEncodingException, IOException, JSONException, NoSuchAlgorithmException, SQLException, Exception
	{
		final User userChallenged = new User(userChallengedId, param_userChallengedName);
		userChallenged.update();

		final User userCreator = new User(userCreatorId);
		final Game game = new Game(userChallenged, userCreator, param_board, gameType);

		if (game.isNewGameValid())
		{
			game.flipNewGameBoard();
			game.makeId();
			game.update();

			new GCMMessage()
				.setGameId(game.getId())
				.setMessageTypeNewGame()
				.setUserToMention(userCreator)
				.setUserToReceive(userChallenged)
				.sendMessage();

			printWriter.write(Utilities.makePostDataSuccess(Utilities.POST_SUCCESS_GAME_ADDED_TO_DATABASE));
		}
		else
		{
			printWriter.write(Utilities.makePostDataError(Utilities.POST_ERROR_BOARD_INVALID));
		}
	}


}
