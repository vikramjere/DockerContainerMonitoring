package com.monitor.docker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.jayway.restassured.path.json.JsonPath;

public class QueryDockerAPI 
{
	static PrintStream console = System.out;
	public static String getJSonResponse(String dockerurl) 
	{
		String url = dockerurl;
		URL obj;
		StringBuffer response = null;		
		try 
		{
			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) 
			{
				response.append(inputLine);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return (response.toString());
	}
	
	public static String[] getJSonStringValue(String JSonResponse) 
	{		
		String[] metrics = new String[5];
		
		//Derive CPU Usage in Percentage
		Long cputotal = JsonPath.with(JSonResponse).getLong("cpu_stats.cpu_usage.total_usage");
		Long cpusystem = JsonPath.with(JSonResponse).getLong("cpu_stats.system_cpu_usage");
		List<Object> perCPUusage = JsonPath.with(JSonResponse).getList("cpu_stats.cpu_usage.percpu_usage");
		Long cpudelta = cputotal - ContainerMonitoring.prevCPUtotal;
		Long systemdelta = cpusystem - ContainerMonitoring.pevCPUSystem;
		
		double cpuPercent = ((float)cpudelta/(float)systemdelta) * perCPUusage.size() * 100.0;
		ContainerMonitoring.prevCPUtotal = cputotal;
		ContainerMonitoring.pevCPUSystem = cpusystem;
		metrics[0] = String.valueOf(Math.round(cpuPercent * 100.0)/100.0);
		
		//Derive Memory Usage in Percentage
		long memUsage = JsonPath.with(JSonResponse).getLong("memory_stats.usage");
		long memlimit = JsonPath.with(JSonResponse).getLong("memory_stats.limit");
		double memUsagePercentage = (((float)memUsage)/((float)memlimit) * 100.0);
		metrics[1] = String.valueOf(Math.round(memUsagePercentage * 100.0)/100.0);
		
		
		//Derive Disk IO Usage		
		metrics[2] = "0";
		
		
		//Derive Network Usage in Bytes
		long netin = JsonPath.with(JSonResponse).getLong("networks.eth0.rx_bytes");
		long netout = JsonPath.with(JSonResponse).getLong("networks.eth0.tx_bytes");		
		metrics[3] = String.valueOf(netin);
		metrics[4] = String.valueOf(netout);
		
		return metrics;
	}
	
}
