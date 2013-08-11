package com.charlesmadere.android.classygames.utilities;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public final class DB
{


	public static Connection connection;


	/**
	 * Releases this class's SQL database connection.
	 */
	public static void close()
	{
		close(connection);
	}


	/**
	 * Releases a set of SQL resources.
	 * 
	 * @parameter sqls
	 * A set of SQL objects. It's okay if this object is null or if it was
	 * never used.
	 */
	public static void close(final AutoCloseable... sqls)
	{
		if (sqls != null && sqls.length >= 1)
		{
			for (final AutoCloseable sql : sqls)
			{
				if (sql != null)
				{
					try
					{
						sql.close();
					}
					catch (final Exception e)
					{
	
					}
				}
			}
		}
	}


	/**
	 * Establishes a connection to the SQL database. I followed this guide to
	 * understand how to connect to the MySQL database that is created when
	 * making a new Amazon Elastic Beanstalk application:
	 * http://docs.amazonwebservices.com/elasticbeanstalk/latest/dg/create_deploy_Java.rds.html
	 * 
	 * A MySQL JDBC Driver is required for this MySQL connection to actually
	 * work. You can download that driver from here:
	 * https://dev.mysql.com/downloads/connector/j/
	 * 
	 * @throws SQLException
	 * If a connection to the SQL database could not be created then a
	 * SQLException will be thrown.
	 * 
	 * @throws Exception
	 * If there's an error when loading the JDBC driver then a generic
	 * Exception will be thrown.
	 */
	public static void open() throws SQLException, Exception
	{
		// ensure that the MySQL JDBC Driver has been loaded
		Class.forName("com.mysql.jdbc.Driver").newInstance();

		// acquire database credentials from Amazon Web Services
		final String hostname = System.getProperty("RDS_HOSTNAME");
		final String port = System.getProperty("RDS_PORT");
		final String dbName = System.getProperty("RDS_DB_NAME");
		final String username = System.getProperty("RDS_USERNAME");
		final String password = System.getProperty("RDS_PASSWORD");

		// create the connection string
		final String connectionString = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + username + "&password=" + password;

		// return a new connection to the database
		connection = DriverManager.getConnection(connectionString);
	}


}
