package com.monitor.docker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ContainerMonitoring 
{
	static Long prevCPUtotal = (long) 0;
	static Long pevCPUSystem = (long) 0;
	
	public static void main(String[] args) 
	{				
		System.out.println("\n---------------------------------------------------\n");
		System.out.println("Entering Container Monitor program to collect container stats......\n");
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		System.out.println("Start Time: "+dateFormat.format(cal.getTime()));
		String[] containernames = args;
		try 
		{
			if (containernames[0] != null) 
			{
				getContainermetrics(containernames);
			}			
		} catch (SQLException e) 
		{
			e.printStackTrace();
		}
		
		Calendar cal1 = Calendar.getInstance();		
		System.out.println("End Time: " +dateFormat.format(cal1.getTime()));
		System.out.println("\n----------------------------------------------------\n");
	}

	private static void getContainermetrics(String[] containernames) throws SQLException 
	{		
		for (int i = 0; i < containernames.length; i++) 
		{
			String url = "http://<dockerhostname>:<port>/containers/"	+ containernames[i] + "/stats?stream=false";
			System.out.println(url);
			String response = QueryDockerAPI.getJSonResponse(url);
			String metrics[] = null;
			metrics = QueryDockerAPI.getJSonStringValue(response);
			for (int m = 0; m < metrics.length; m++) 
			{
				metrics[m] = '"' + metrics[m] + '"';
			}
			//Load the metrics to DB
			LoadMetricstoDB("container", containernames[i], metrics);
		}
	}

	private static void LoadMetricstoDB(String type, String Containername, String[] metrics) throws SQLException 
	{

		Connection conn = openDB();
		String query = null;
		if(type.equalsIgnoreCase("container"))
		{
			double networkusage = (Double.valueOf(metrics[3].substring(1,metrics[3].length() - 1))) + (Double.valueOf(metrics[4].substring(1,metrics[4].length() - 1)));
			query = "insert into <mysql_schema_name>.containermetrics(cpuutil,memutil,iorate,networkusage,comments,containername) values (?,?,?,?,?,?)";
			PreparedStatement preparedStmt = conn.prepareStatement(query);			
			preparedStmt.setString(1, metrics[0].substring(1,metrics[0].length() - 1));
			preparedStmt.setString(2, metrics[1].substring(1,metrics[1].length() - 1));
			preparedStmt.setString(3, metrics[2].substring(1,metrics[2].length() - 1));
			preparedStmt.setString(4, Double.toString(networkusage));
			preparedStmt.setString(5, "");	
			preparedStmt.setString(6, Containername);
			preparedStmt.execute();
		} 	
	}
	
	public static Connection openDB() 
	{
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			String connectionUrl = "jdbc:mysql://<mysql_host>:3306/<mysql_Schema>";
			String connectionUser = "user1";
			String connectionPassword = "pwd1";
			conn = DriverManager.getConnection(connectionUrl, connectionUser,
					connectionPassword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}	
}
