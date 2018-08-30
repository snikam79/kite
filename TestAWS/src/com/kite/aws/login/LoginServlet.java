package com.kite.aws.login;

import static com.kite.aws.login.GlobalLoggerHandler.LOGIN_LOGGER;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kite.aws.util.GlobalConstants;
import com.zerodhatech.kiteconnect.KiteConnect;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action");
		String status = request.getParameter("status");
		String token = request.getParameter("request_token");
		String user = null;
		if (GlobalConstants.API_KEY.equals("5b4azxoiqmjvsomi")) {
			user = "Ashish";
		} else {
			user = "Sachin";
		}

		request.setAttribute("user", user);
		if ((action != null) && (status != null) && (token != null)) {
			if ((action.equals("login")) && (status.equals("success"))) {
				String reply = loginUser(token, request);
				if (reply.contains("success")) {
					request.setAttribute("code", "success");
				} else {
					request.setAttribute("code", "error");
					request.setAttribute("message", "Login failure");
				}
			} else {
				request.setAttribute("code", "error");
				request.setAttribute("message", "Missing login parameters");
			}
		} else {
			request.setAttribute("code", "error");
			request.setAttribute("message", "Missing login parameters");
		}

		RequestDispatcher rd = request.getRequestDispatcher("login.jsp");
		rd.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action");

		if (GlobalConstants.API_KEY.equals("5b4azxoiqmjvsomi")) {
			GlobalConstants.MAX_POSITION_SIZE = 1500;
		} else {
			GlobalConstants.MAX_POSITION_SIZE = 600;
		}

		if ((action != null) && (action.equals("login"))) {
			String token = request.getParameter("token");
			response.getWriter().print(loginUser(token, request));
		}

		if ((action != null) && (action.equals("close"))) {

		}
	}

	private String loginUser(String token, HttpServletRequest request) {

		if (token != null) {
			Object[] reply = Login.login(token);
			LoginResponse response = new LoginResponse((String) reply[0], (String) reply[1]);
			Gson gson = new GsonBuilder().create();
			LOGIN_LOGGER.info("Generated token...");
			KiteConnect connect = (KiteConnect) reply[2];
			if (connect != null) {
				LOGIN_LOGGER.info("Got connect from Login.java");
				LOGIN_LOGGER.info("Now setting it up in servlet context");
				getServletContext().setAttribute("kite", connect);
				try {
					LOGIN_LOGGER.info("Starting Timers...");
					KiteStore.startTimers(getServletContext());
				} catch (Exception e) {
					LOGIN_LOGGER.info("Exception in timer >>" + e.getMessage());
					e.printStackTrace();
				}
			} else {
				response = new LoginResponse("error", "connect is null from Login.java");
				LOGIN_LOGGER.info("connect is null from Login.java");
			}

			return gson.toJson(response);
		} else {
			LoginResponse response = new LoginResponse("error", "Insufficient arguments");
			Gson gson = new GsonBuilder().create();
			return gson.toJson(response);
		}

	}

}
