package com.charlesmadere.android.classygames.utilities;


public final class DBConstants
{


	public final static String TABLE_GAMES = "games";
	public final static String TABLE_GAMES_COLUMN_ID = "id";
	public final static String TABLE_GAMES_COLUMN_USER_CREATOR = "user_creator";
	public final static String TABLE_GAMES_COLUMN_USER_CHALLENGED = "user_challenged";
	public final static String TABLE_GAMES_COLUMN_BOARD = "board";
	public final static String TABLE_GAMES_COLUMN_TURN = "turn";
	public final static String TABLE_GAMES_COLUMN_GAME_TYPE = "game_type";
	public final static String TABLE_GAMES_COLUMN_LAST_MOVE = "last_move";
	public final static String TABLE_GAMES_COLUMN_FINISHED = "finished";
	public final static byte TABLE_GAMES_TURN_CREATOR = 1;
	public final static byte TABLE_GAMES_TURN_CHALLENGED = 2;
	public final static byte TABLE_GAMES_FINISHED_FALSE = 1;
	public final static byte TABLE_GAMES_FINISHED_TRUE = 2;
	public final static String TABLE_GAMES_FORMAT = "(" + TABLE_GAMES_COLUMN_ID + ", " + TABLE_GAMES_COLUMN_USER_CREATOR + ", " + TABLE_GAMES_COLUMN_USER_CHALLENGED + ", " + TABLE_GAMES_COLUMN_BOARD + ", " + TABLE_GAMES_COLUMN_TURN + ", " + TABLE_GAMES_COLUMN_GAME_TYPE + ", " + TABLE_GAMES_COLUMN_FINISHED + ")";
	public final static String TABLE_GAMES_VALUES = "VALUES (?, ?, ?, ?, ?, ?, ?)";

	public final static String TABLE_USERS = "users";
	public final static String TABLE_USERS_COLUMN_ID = "id";
	public final static String TABLE_USERS_COLUMN_NAME = "name";
	public final static String TABLE_USERS_COLUMN_REG_ID = "reg_id";
	public final static String TABLE_USERS_COLUMN_CHECKERS_LOSES = "checkers_loses";
	public final static String TABLE_USERS_COLUMN_CHECKERS_WINS = "checkers_wins";
	public final static String TABLE_USERS_COLUMN_CHESS_LOSES = "chess_loses";
	public final static String TABLE_USERS_COLUMN_CHESS_WINS = "chess_wins";
	public final static String TABLE_USERS_FORMAT = "(" + TABLE_USERS_COLUMN_ID + ", " + TABLE_USERS_COLUMN_NAME + ", " + TABLE_USERS_COLUMN_REG_ID + ")";
	public final static String TABLE_USERS_VALUES = "VALUES (?, ?, ?)";


}
