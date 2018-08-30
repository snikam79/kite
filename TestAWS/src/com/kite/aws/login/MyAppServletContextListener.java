package com.kite.aws.login;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.kite.aws.util.SetGlobalContext;

import static com.kite.aws.login.GlobalLoggerHandler.LOGIN_LOGGER;

@WebListener
public class MyAppServletContextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		LOGIN_LOGGER.info("Application is about to stop");
		LOGIN_LOGGER.info("Removing kite from servlet context");
		event.getServletContext().removeAttribute("kite");
		LOGIN_LOGGER.info("Bye from MyAppServletContextListener.contextDestroyed()");
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		boolean check = SetGlobalContext.setAllGlobalProperties();

		GlobalLoggerHandler.init();

		if (check) {
			LOGIN_LOGGER.info("Loaded all user specific properties from user.txt");
		} else {
			LOGIN_LOGGER.info("Something went wrong while reading properties from user.txt");
			LOGIN_LOGGER.info("Application will not work !!!");
		}
		LOGIN_LOGGER.info("Control in MyAppServletContextListener.contextInitialized()");
		LOGIN_LOGGER.info("Application started....");

	}

}
