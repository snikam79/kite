package com.kite.aws.risk;

import static com.kite.aws.login.GlobalLoggerHandler.RISK_TIMER_LOGGER;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.LTPQuote;
import com.zerodhatech.models.Position;

public class RiskUtil {

	public static String[] stopLossLimitCheck(KiteConnect kiteconnect, String lossStr) {
		List<Position> netPositions;
		String[] reply = new String[3];

		if (kiteconnect == null) {
			reply[0] = "fatal";
			reply[1] = "kiteconnect is null in RiskUtil.stopLossLimitCheck()";
			reply[2] = null;
			return reply;
		}

		try {
			netPositions = kiteconnect.getPositions().get("net");
		} catch (JSONException e) {
			reply[0] = "fatal";
			reply[1] = "JSONException in RiskUtil.stopLossLimitCheck()";
			reply[2] = null;
			return reply;
		} catch (KiteException e) {
			reply[0] = "fatal";
			reply[1] = "KiteException in RiskUtil.stopLossLimitCheck()";
			reply[2] = null;
			return reply;
		} catch (IOException e) {
			reply[0] = "fatal";
			reply[1] = "IOException in RiskUtil.stopLossLimitCheck()";
			reply[2] = null;
			return reply;
		}

		int openPosCount = 0;
		int openQuantity = 0;
		for (Position item : netPositions) {
			int quantity = item.netQuantity;
			if ((quantity != 0)) {
				openQuantity = openQuantity + Math.abs(item.netQuantity);
				openPosCount++;

			}
		}

		if (openPosCount == 0) {
			reply[0] = "error";
			reply[1] = "There are no open positions";
			reply[2] = null;
			return reply;
		}

		double lossTrigger = (openQuantity / openPosCount) * RiskStore.absPointsStopLoss;
		lossTrigger = 0.0 - lossTrigger;
		double loss = Double.parseDouble(lossStr);

		if (loss < lossTrigger) {
			reply[0] = "error";
			reply[1] = "Please set loss less than : " + lossTrigger;
			reply[2] = lossTrigger + "";
		} else {
			reply[0] = "success";
			reply[1] = "success";
			reply[2] = lossTrigger + "";
		}
		return reply;
	}

	public static StrangleLTPResponse getLTPForStrangle(KiteConnect connect, String[] symbols) {
		Map<String, LTPQuote> quoteMap = null;
		Map<String, String> ltpMap = new HashMap<String, String>();
		StrangleLTPResponse response = new StrangleLTPResponse();
		String msg = null;
		try {
			quoteMap = connect.getLTP(symbols);
		} catch (JSONException e) {
			msg = e.getMessage();

			if (msg == null) {
				msg = "JSONException in RiskUtil.getLTPForStrangle()";
			}
			response.setCode("error");
			response.setMessge(msg);
			RISK_TIMER_LOGGER.info(msg);
			return response;
		} catch (IOException e) {
			msg = e.getMessage();

			if (msg == null) {
				msg = "IOException in RiskUtil.getLTPForStrangle()";
			}
			response.setCode("error");
			response.setMessge(msg);
			RISK_TIMER_LOGGER.info(msg);
			return response;
		} catch (KiteException e) {
			msg = e.getMessage();

			if (msg == null) {
				msg = "KiteException in RiskUtil.getLTPForStrangle()";
			}
			response.setCode("error");
			response.setMessge(msg);
			RISK_TIMER_LOGGER.info(msg);
			return response;
		}

		if (quoteMap == null) {
			msg = "quoteMap is null in RiskUtil.getLTPForStrangle()";
			response.setCode("error");
			response.setMessge(msg);
			RISK_TIMER_LOGGER.info(msg);
			return response;
		}

		double ltp1 = quoteMap.get(symbols[0]).lastPrice;
		double ltp2 = quoteMap.get(symbols[1]).lastPrice;

		if ((ltp1 == 0.0) || (ltp2 == 0.0)) {
			msg = "either of the quotes is 0.0";
			response.setCode("error");
			response.setMessge(msg);
			RISK_TIMER_LOGGER.info(msg);
			return response;
		}

		ltpMap.put(symbols[0], ltp1 + "");
		ltpMap.put(symbols[1], ltp2 + "");

		response.setCode("success");
		response.setMessge("success");
		response.setLtpMap(ltpMap);

		return response;
	}

}
