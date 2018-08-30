package com.kite.aws.login;

import java.util.Timer;

import javax.servlet.ServletContext;

import com.kite.aws.inflation.InflationTimer;
import com.kite.aws.risk.RiskTimer;
import com.kite.aws.util.GlobalConstants;

public class KiteStore {

	public static String accessToken;

	public static String publicToken;

	public static RiskTimer riskTimer;

	public static InflationTimer inflationTimer;

	public static void startTimers(ServletContext context) {

		riskTimer = new RiskTimer(context);
		inflationTimer = new InflationTimer(context);

		Timer timer1 = new Timer();
		try {
			timer1.scheduleAtFixedRate(riskTimer, 0, GlobalConstants.RISK_TIMER_DELAY * 1000);
		} catch (Exception e) {

			e.printStackTrace();
		}

		Timer timer2 = new Timer();
		timer2.schedule(inflationTimer, 1000, GlobalConstants.INFLATION_TIMER_DELAY * 1000);
	}

}
