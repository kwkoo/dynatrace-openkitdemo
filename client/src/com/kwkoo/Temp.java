package com.kwkoo;

import com.dynatrace.openkit.api.*;
import com.dynatrace.openkit.DynatraceOpenKitBuilder;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Temp {
	public final static int SOCKET_TIMEOUT = 5000;
	public final static String[] EMAIL_ADDRESSES = {"jipsen@aol.com", "dhrakar@outlook.com", "airship@hotmail.com", "daveewart@icloud.com", "danneng@optonline.net", "gilmoure@comcast.net", "adamk@msn.com", "kmself@optonline.net", "kevinm@optonline.net", "flakeg@optonline.net", "graham@yahoo.ca", "ryanshaw@sbcglobal.net", "szymansk@gmail.com", "webdragon@icloud.com", "emmanuel@aol.com", "ajohnson@msn.com", "hauma@icloud.com", "jimmichie@optonline.net", "bolow@yahoo.com", "chrwin@gmail.com", "oevans@yahoo.com", "neonatus@outlook.com", "skoch@verizon.net", "arathi@me.com", "kenja@gmail.com", "dleconte@icloud.com", "mcnihil@yahoo.com", "xnormal@live.com", "wildixon@comcast.net", "chunzi@live.com", "sacraver@yahoo.com", "jfmulder@icloud.com", "sartak@mac.com", "papathan@hotmail.com", "tsuruta@live.com", "heckerman@verizon.net", "jginspace@optonline.net", "bsikdar@outlook.com", "netsfr@msn.com", "dhwon@aol.com", "drezet@yahoo.ca", "evans@outlook.com", "errxn@yahoo.ca", "aglassis@sbcglobal.net", "jesse@live.com", "solomon@yahoo.ca", "ilikered@yahoo.ca", "oneiros@comcast.net", "kaiser@icloud.com", "michiel@live.com"};
	public final static String[] IP_ADDRESSES = {"228.253.103.48", "35.26.91.98", "230.4.31.178", "233.236.198.115", "52.173.102.96", "144.70.125.216", "120.46.144.67", "179.105.224.182", "30.186.97.111", "237.44.37.203", "220.212.196.83", "199.102.93.141", "129.225.171.57", "180.33.244.14", "173.1.115.150", "51.210.122.95", "251.11.3.23", "145.79.118.58", "163.147.89.245", "137.149.236.153", "24.42.159.179", "144.34.30.137", "60.31.52.70", "71.89.21.106", "176.67.220.151", "14.2.222.195", "19.253.112.250", "168.126.108.179", "188.14.199.175", "245.165.59.244", "126.210.86.77", "18.106.6.205", "254.126.80.182", "28.72.129.202", "113.18.228.9", "96.64.50.59", "134.205.191.215", "213.168.131.21", "51.31.38.56", "136.205.41.97", "151.204.187.121", "50.54.113.47", "98.114.133.64", "243.58.246.178", "8.57.189.156", "239.85.140.70", "40.254.247.232", "254.57.171.185", "66.27.157.145", "27.118.41.197"};

	private URL tempServer;
	private OpenKit openKit;
	private Random random;


	public final static void main(String[] args) throws Exception {
		//String applicationID = "5d0b1eb8-6790-418b-86b6-2ca0f7f04e0d";
		//String endpointURL = "https://bf87759gkg.bf.dynatrace.com/mbeacon";
		String applicationID, endpointURL, tempServer;

		if (args.length < 2) {
			System.err.println("Usage: Temp APPLICATION_ID BEACON_URL"
							   + " TEMP_SERVER_URL");
			System.exit(1);
		}

		applicationID = args[0];
		endpointURL = args[1];
		tempServer = (args.length>2)?args[2]:null;

		Temp temp = new Temp(applicationID, endpointURL, tempServer);
		temp.doSomeWork();
		temp.shutdown();
	}

	public Temp(String applicationID,
				String endpointURL,
				String ts)
		throws Exception {
		System.out.println("Application ID: " + applicationID);
		System.out.println("Beacon URL: " + endpointURL);

		if (ts == null) {
			System.out.println("No temperature server URL - will not send"
							   + " readings to server");
			tempServer = null;
		} else {
			tempServer = new URL(ts);
			System.out.println("Temperature Server URL: " + tempServer);
		}

		random = new Random();

		String applicationName = "Kin's Temperature Sensor";
		long deviceID = 42;
		openKit = new DynatraceOpenKitBuilder
							(endpointURL, applicationID, deviceID)
			.withApplicationName(applicationName)
			.withApplicationVersion("1.0.0.0")
			.withOperatingSystem("Blah OS")
			.withManufacturer("Temps Are Us")
			.withModelID("MyFirstModel")
			.build();
	}

	public void doSomeWork() {
		FakeSensor sensor = new FakeSensor();
		String clientIP = IP_ADDRESSES[random.nextInt(IP_ADDRESSES.length)];
		Session session = openKit.createSession(clientIP);
		session.identifyUser
			(EMAIL_ADDRESSES[random.nextInt(EMAIL_ADDRESSES.length)]);

		RootAction action = session.enterAction("Initializing sensor");
		try { Thread.sleep(1000); } catch (InterruptedException e) {}
		action.leaveAction();

		int tempCount = 10;

		for (int i=1; i<=tempCount; i++) {
			System.out.println("Sending reading "
							   + i
							   + " / "
							   + tempCount
							   + "...");
			action = session.enterAction("Temp Reading");
			float temperature = sensor.getTemperature();
			action.reportValue
			  ("temperature", Double.parseDouble(Float.toString(temperature)));
			action.leaveAction();

			reportTemperature(session, temperature);

			try { Thread.sleep(1000); } catch (InterruptedException e) {}
		}

		action = session.enterAction("Shutting down sensor");
		try { Thread.sleep(1000); } catch (InterruptedException e) {}
		action.leaveAction();

		session.end();
	}

	public void shutdown() {
		openKit.shutdown();
		openKit = null;
	}

	private void reportTemperature(Session session, float temp) {
		if (tempServer == null) return;

		URL url = null;
		Action action = null;
		WebRequestTracer tracer = null;
		byte[] buffer = new byte[4096];
		HttpURLConnection conn = null;
		InputStream in = null;

		try {
			url = new URL(tempServer, "?temp=" + Float.toString(temp));
			action = session.enterAction("Report temperature to server");
			conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(SOCKET_TIMEOUT);
			conn.setReadTimeout(SOCKET_TIMEOUT);

			tracer = action.traceWebRequest(conn);
			tracer.start();

			in = conn.getInputStream();
			while (in.read(buffer) != -1) {}
			tracer.setResponseCode(conn.getResponseCode());
		}
		catch (IOException e) {
			System.err.println("Exception reporting to temp server: "
							   + e.getMessage());
			if (action != null)
				action.reportError("ReportTempServer",
								   100,
								   e.getMessage());
		}
		finally {
			if (in != null) try { in.close(); } catch (IOException e) {}
			if (conn != null) conn.disconnect();
			if (tracer != null) tracer.stop();
			if (action != null) action.leaveAction();
		}
	}
}

class FakeSensor {
	public final static int MAX_CHANGE = 3;
	private Random random;
	private float lastSensorReading = 30;

	public FakeSensor() {
		random = new Random();
	}

	public float getTemperature() {
		float delta = random.nextFloat() * MAX_CHANGE;
		if (random.nextBoolean()) delta = -delta;
		lastSensorReading += delta;
		return lastSensorReading;
	}
}
