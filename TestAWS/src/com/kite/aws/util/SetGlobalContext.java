package com.kite.aws.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class SetGlobalContext {

	public static HashMap<String, String> userProfile = new HashMap<String, String>();

	public static boolean isDevelopment() {
		String OS = System.getProperty("os.name").toLowerCase();
		return (OS.contains("win"));
	}

	public static boolean isProduction() {
		String OS = System.getProperty("os.name").toLowerCase();
		return OS.contains("linux");
	}

	public static boolean setAllGlobalProperties() {
		String userProfilePath = "/opt/user.txt";
		boolean loadPropertyCheck = true;
		if (isDevelopment()) {
			userProfilePath = "c:\\user.txt";
		}

		File file = new File(userProfilePath);

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException fne) {
			System.out.println("FileNotFoundException while reading user profile file : " + fne);
			loadPropertyCheck = false;
		}

		String line;
		try {
			while (true) {
				line = br.readLine();
				if (line == null) {
					break;
				}
				if (line.contains(":")) {
					line = line.trim();
					String[] tokens = line.split(":");
					userProfile.put(tokens[0].trim(), tokens[1].trim());
				}
			}
		} catch (IOException ioe) {
			System.out.println("IOException while reading user profile file : " + ioe);
			loadPropertyCheck = false;
		}

		GlobalConstants.LOG_FILE_PATH = userProfile.get("LOG_FILE_PATH");
		GlobalConstants.API_KEY = userProfile.get("API_KEY");
		GlobalConstants.API_SECRET = userProfile.get("API_SECRET");
		GlobalConstants.USER_ID = userProfile.get("USER_ID");
		GlobalConstants.MAX_POSITION_SIZE = Integer.parseInt(userProfile.get("MAX_POSITION_SIZE"));
		GlobalConstants.RISK_TIMER_DELAY = Integer.parseInt(userProfile.get("RISK_TIMER_DELAY"));
		GlobalConstants.INFLATION_TIMER_DELAY = Integer.parseInt(userProfile.get("INFLATION_TIMER_DELAY"));
		return loadPropertyCheck;

	}

	public static void main(String[] args) {
		System.out.println(setAllGlobalProperties());
	}

}
