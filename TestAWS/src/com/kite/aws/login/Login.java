package com.kite.aws.login;

import java.io.IOException;

import org.json.JSONException;

import com.kite.aws.util.GlobalConstants;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.User;
import static com.kite.aws.login.GlobalLoggerHandler.LOGIN_LOGGER;

public class Login {

	public static String accessToken;

	public static KiteConnect kiteconnect;

	public static User user;

	public static double profitTrigger;

	public static double lossTrigger;

	public static Object[] login(String requestToken) {

		KiteConnect kiteconnect = new KiteConnect(GlobalConstants.API_KEY);
		kiteconnect.setUserId(GlobalConstants.USER_ID);
		User user = null;
		String code = "success";
		String message = "";

		try {
			user = kiteconnect.generateSession(requestToken, GlobalConstants.API_SECRET);
		} catch (JSONException e) {
			code = "error";
			message = "JSONException while genrating access token";
			LOGIN_LOGGER.info(e.getMessage());
		} catch (KiteException e) {
			code = "error";
			message = "KiteException while genrating access token";
			LOGIN_LOGGER.info(e.getMessage());
		} catch (IOException e) {
			code = "error";
			message = "IOException while genrating access token";
			LOGIN_LOGGER.info(e.getMessage());
		}
		if (user != null) {

			message = "Generated access token successfuly";
			kiteconnect.setAccessToken(user.accessToken);
			kiteconnect.setPublicToken(user.publicToken);
		} else {
			code = "error";
			message = "userModel is null in Login.java";
		}

		return new Object[] { code, message, kiteconnect };
	}

}
