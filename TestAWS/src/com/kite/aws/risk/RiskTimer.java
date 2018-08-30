package com.kite.aws.risk;

import static com.kite.aws.login.GlobalLoggerHandler.RISK_TIMER_LOGGER;
import static com.kite.aws.risk.RiskStore.m2m;
import static com.kite.aws.risk.RiskStore.profitTarget;
import static com.kite.aws.risk.RiskStore.riskTimerStatus;
import static com.kite.aws.risk.RiskStore.riskTimerStatusMessage;
import static com.kite.aws.risk.RiskStore.stopLoss;

import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import javax.servlet.ServletContext;

import org.json.JSONException;

import com.kite.aws.order.OrderExecutor;
import com.kite.aws.util.GlobalConstants;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Position;

public class RiskTimer extends TimerTask {

	private String riskStatus = "";
	private static String KITE_CONNECT_NULL = "KITE_CONNECT_NULL";
	private static String KITE_EXCEPTION = "KITE_EXCEPTION";
	private static String NUMBER_FORMAT_EXCEPTION = "NUMBER_FORMAT_EXCEPTION";
	private static String NO_OPEN_POSITION = "NO_OPEN_POSITION";
	private static String NO_TRIGGER = "NO_TRIGGER";

	private static final LocalTime START_TIME = LocalTime.parse("09:15");
	private static final LocalTime END_TIME = LocalTime.parse("15:35");
	private static final ZoneId ZONE_ID = ZoneId.of("Asia/Calcutta");
	private static int singlePositionCountCheck;
	private volatile ServletContext conext;

	public RiskTimer(ServletContext conext) {
		this.conext = conext;
	}

	@Override
	public void run() {
		try {
			RISK_TIMER_LOGGER.info("Inside run() of RiskTimer>>" + new Date());
			if (RiskStore.skipFlag == 2) {
				RISK_TIMER_LOGGER.info("Skipping risk timer iteration for the second time");
				RiskStore.skipFlag++;
				return;
			}

			if (RiskStore.skipFlag == 3) {
				RISK_TIMER_LOGGER.info("Setting again risk store back to zero");
				RiskStore.skipFlag = 0;
				return;
			}

			LocalTime now = LocalTime.now(ZONE_ID);
			boolean isBefore = now.isBefore(START_TIME);
			boolean isAfter = now.isAfter(END_TIME);
			if (isBefore || isAfter) {
				return;
			}

			handleRisk();
		} catch (Exception e) {
			RISK_TIMER_LOGGER.info("RiskTimer: Exception >>" + e.getMessage());
		} catch (Throwable t) {
			RISK_TIMER_LOGGER.info("RiskTimer: Throwable >>" + t.getMessage());

		}
	}

	public void handleRisk() {

		KiteConnect connect = (KiteConnect) this.conext.getAttribute("kite");
		if (connect == null) {
			RISK_TIMER_LOGGER.info("connect is null from servlet context in RiskTimer.run()");
			RiskStore.riskTimerStatus = "error";
			riskStatus = KITE_CONNECT_NULL;
			return;
		}

		double profitAndLoss = 0.0;
		Map<String, List<Position>> positionMap = null;
		try {
			positionMap = connect.getPositions();
			RISK_TIMER_LOGGER.info("Size of positionMap is:>>" + positionMap.size());
			/*
			 * size of positionMap ???
			 */
		} catch (JSONException e) {
			riskTimerStatus = "error";
			riskTimerStatusMessage = "JSONException in RiskTimer.run() while getting positions";
			m2m = "" + profitAndLoss;
			RISK_TIMER_LOGGER.info("JSONException in RiskTimer.run() while getting positions");
			riskStatus = KITE_EXCEPTION;
			return;
		} catch (KiteException e) {
			riskTimerStatus = "error";
			riskTimerStatusMessage = "KiteException in RiskTimer.run() while getting positions";
			m2m = "" + profitAndLoss;
			RISK_TIMER_LOGGER.info("KiteException in RiskTimer.run() while getting positions");
			riskStatus = KITE_EXCEPTION;
			return;
		} catch (Exception e) {
			riskTimerStatus = "error";
			riskTimerStatusMessage = "Generic Exception in RiskTimer.run() while getting positions";
			m2m = "" + profitAndLoss;
			RISK_TIMER_LOGGER.info("KiteException in RiskTimer.run() while getting positions");
			riskStatus = KITE_EXCEPTION;
			return;
		}

		double dailyLimit = 0.0;
		int posCount = 0;
		List<Position> netPositions = positionMap.get("net");
		List<Position> trackPositions = new ArrayList<Position>();
		for (Position item : netPositions) {
			dailyLimit = dailyLimit + item.m2m;
			if (item.netQuantity != 0) {
				posCount++;
				trackPositions.add(item);
			}
		}

		RISK_TIMER_LOGGER.info("Open position count : " + posCount);

		int maxLoss = 0;
		if (GlobalConstants.API_KEY.equals("5b4azxoiqmjvsomi")) {
			maxLoss = -15000;
		}

		if (GlobalConstants.API_KEY.equals("3kwyjzh67vq1v3oc")) {
			maxLoss = -9000;
		}

		if (dailyLimit < maxLoss) {
			// game over. exit all positions
			// it needs more work
			RISK_TIMER_LOGGER.info("Exiting all open postions. Reason : daily loss exceeded");
			RISK_TIMER_LOGGER.info("Loss till now : " + dailyLimit);
			OrderExecutor.exitAllPositions(connect, trackPositions);

		}

		if (posCount == 0) {
			riskTimerStatus = "NO_TRIGGER";
			m2m = "" + profitAndLoss;
			stopLoss = "NA";
			profitTarget = "NA";
			riskTimerStatusMessage = "NA";
			// RISK_TIMER_LOGGER.info("openPositionCount=0 in RiskTimer.run()");
			riskStatus = NO_OPEN_POSITION;
			singlePositionCountCheck = 0;
			// RiskStore.trailStopLoss = "NA";
			// RiskStore.trailStopLossTriggerUI = "NA";
			return;
		}

		if ((posCount == 1)) {
			riskTimerStatus = "NO_TRIGGER";
			m2m = "" + profitAndLoss;
			stopLoss = "NA";
			profitTarget = "NA";
			riskTimerStatusMessage = "NA";
			// RISK_TIMER_LOGGER.info("openPositionCount=0 in RiskTimer.run()");
			riskStatus = NO_OPEN_POSITION;
			singlePositionCountCheck = 0;
			// RiskStore.trailStopLoss = "NA";
			// RiskStore.trailStopLossTriggerUI = "NA";
			RISK_TIMER_LOGGER.info("Exiting single open postion. Reason : Single position is not allowed");
			OrderExecutor.exitAllPositions(connect, trackPositions);
			return;
		}

		if (posCount > 2) {
			riskTimerStatus = "NO_TRIGGER";
			m2m = "" + profitAndLoss;
			stopLoss = "NA";
			profitTarget = "NA";
			riskTimerStatusMessage = "NA";
			RISK_TIMER_LOGGER.info("openPositionCount>2 in  RiskTimer.run()");
			riskStatus = NO_OPEN_POSITION;
			// singlePositionCountCheck = 0;
			// RiskStore.trailStopLoss = "NA";
			// RiskStore.trailStopLossTriggerUI = "NA";
			RISK_TIMER_LOGGER.info("Exiting all open postions. Reason : More than 2 positions are not allowed");
			OrderExecutor.exitAllPositions(connect, trackPositions);
			return;
		}

		if ((posCount == 2)) { // RASHI: retrieve open position before each call of exitAllPositions
			// classic strangle positions
			RISK_TIMER_LOGGER.info("Open position count :: 2");
			int totalQty = 0;
			for (Position item : netPositions) {
				int netQuantity = item.netQuantity;
				if (netQuantity != 0) {
					totalQty = totalQty + Math.abs(netQuantity);
				}
			}
			boolean positionSizeCheck = ((totalQty / 2) > GlobalConstants.MAX_POSITION_SIZE);
			RISK_TIMER_LOGGER.info("totalQty/2 : " + totalQty / 2);
			RISK_TIMER_LOGGER.info("GlobalConstants.MAX_POSITION_SIZE : " + GlobalConstants.MAX_POSITION_SIZE);
			if (positionSizeCheck) {
				RISK_TIMER_LOGGER.info("Exiting all as more position size than allowed");
				riskTimerStatus = "NO_TRIGGER";
				stopLoss = "NA";
				profitTarget = "NA";
				riskTimerStatusMessage = "NA";
				riskStatus = NO_OPEN_POSITION;
				OrderExecutor.exitAllPositions(connect, trackPositions);
				return;
			}

			int openPositionQuantity = 0;
			String prefix = "NFO:";
			String[] symbols = new String[2];
			symbols[0] = prefix + trackPositions.get(0).tradingSymbol;
			symbols[1] = prefix + trackPositions.get(1).tradingSymbol;
			RISK_TIMER_LOGGER.info("First symbol : " + symbols[0]);
			RISK_TIMER_LOGGER.info("Second symbol : " + symbols[1]);
			StrangleLTPResponse ltpResponse = RiskUtil.getLTPForStrangle(connect, symbols);
			if (ltpResponse.getCode().equals("error")) {
				riskTimerStatus = "NO_TRIGGER";
				riskTimerStatusMessage = ltpResponse.getMessge();
				m2m = "No Quote";
				RISK_TIMER_LOGGER.info("Getting error code from RiskUtil.getLTPForStrangle()");
				RISK_TIMER_LOGGER.info(riskTimerStatusMessage);
				return;
			}

			NewOrderBook ourOrderBook = new NewOrderBook();
			for (Position item : netPositions) {
				int netQuantity = item.netQuantity;
				if (netQuantity != 0) {
					openPositionQuantity = openPositionQuantity + Math.abs(netQuantity);
					RISK_TIMER_LOGGER.info("Calculating profit and loss for " + item.tradingSymbol);
					RISK_TIMER_LOGGER.info("net quantity : " + item.netQuantity);
					double ltp = Double.parseDouble(ltpResponse.getLtpMap().get("NFO:" + item.tradingSymbol));
					Object[] tokens = ourOrderBook.getPL(connect, item, ltp);
					String code = (String) tokens[0];
					RISK_TIMER_LOGGER.info("code : " + code);
					if (code.equals("success")) {
						profitAndLoss = profitAndLoss + Double.parseDouble((String) tokens[2]);
						RISK_TIMER_LOGGER.info("profit or loss : " + (String) tokens[2]);
					} else {
						riskTimerStatus = "NO_TRIGGER";
						// riskTimerStatusMessage = "Error while iterating Order Book. Skiping this
						// iteration.";
						riskTimerStatusMessage = (String) tokens[1];
						RISK_TIMER_LOGGER.info(riskTimerStatusMessage);
						m2m = "0.0";
						/*
						 * stopLoss = "NA"; profitTarget = "NA";
						 */
						riskStatus = NO_TRIGGER;
						return;
					}

				}
			}

			RISK_TIMER_LOGGER.info("PROFIT/LOSS FOR BOTH POSITIONS : " + profitAndLoss);

			DecimalFormat df = new DecimalFormat("#.##");
			try {
				profitAndLoss = Double.valueOf(df.format(profitAndLoss));
			} catch (NumberFormatException e) {
				RISK_TIMER_LOGGER.info("NumberFormatException in RiskTimer.run()");
				profitAndLoss = 0.0;
				profitAndLoss = Double.valueOf(df.format(profitAndLoss));
				riskStatus = NUMBER_FORMAT_EXCEPTION;
			}

			// for now we'll not do valid strangle check
			// we'll revisit it after
			/*
			 * boolean validStrangle = validStrangleCheck(trackPositions); if (validStrangle
			 * == false) { riskTimerStatus = "NO_TRIGGER"; stopLoss = "NA"; profitTarget =
			 * "NA"; riskTimerStatusMessage = "NA"; riskStatus = NO_OPEN_POSITION;
			 * OrderExecutor.exitAllPositions(connect); return; }
			 */

			if (lossTriggerCheck(profitAndLoss, openPositionQuantity, 2)) {
				RISK_TIMER_LOGGER.info("Got signal for stop loss in RiskTimer.run() from lossTriggerCheck()");
				String[] reply = OrderExecutor.exitAllPositions(connect, trackPositions);
				if (reply[0].equals("success")) {
					riskTimerStatus = "complete";
					riskTimerStatusMessage = "Stop loss hit. Squared off all open positions";
					m2m = "" + profitAndLoss;
					stopLoss = "NA";
					profitTarget = "NA";
					riskStatus = NO_TRIGGER;
					return;
				}
			}

			if (profitTriggerCheck(profitAndLoss, openPositionQuantity, 2)) {
				RISK_TIMER_LOGGER.info("Got signal for profit target in RiskTimer.run() from lossTriggerCheck()");
				String[] reply = OrderExecutor.exitAllPositions(connect, trackPositions);
				if (reply[0].equals("success")) {
					riskTimerStatus = "complete";
					riskTimerStatusMessage = "Profit target hit. Squared off all open positions";
					m2m = "" + profitAndLoss;
					stopLoss = "NA";
					profitTarget = "NA";
					riskStatus = NO_TRIGGER;
					return;
				}
			}

			riskTimerStatus = "NO_TRIGGER";
			m2m = "" + profitAndLoss;
			riskTimerStatusMessage = "NA";
			riskStatus = NO_TRIGGER;
		}
	}

	private boolean validStrangleCheck(List<Position> trackPositions) {
		boolean nameCheck = false;
		boolean qtyCheck = false;

		Position first = trackPositions.get(0);
		Position second = trackPositions.get(1);

		RISK_TIMER_LOGGER.info("Control in validStrangleCheck()");

		// first name check
		// one must be pe and other must be ce
		String firstToken = first.tradingSymbol.toLowerCase();
		String secondToken = second.tradingSymbol.toLowerCase();

		if (firstToken.endsWith("pe")) {
			if (secondToken.endsWith("ce")) {
				nameCheck = true;
			}
		}

		if (firstToken.endsWith("ce")) {
			if (secondToken.endsWith("pe")) {
				nameCheck = true;
			}
		}

		RISK_TIMER_LOGGER.info("nameCheck : " + nameCheck);
		if (nameCheck) {
			// now size check
			// max 150 difference allowed
			int firstNetQty = Math.abs(first.netQuantity);
			int secondNetQty = Math.abs(second.netQuantity);

			int diff = Math.abs(firstNetQty - secondNetQty);
			// qtyCheck = ((diff == 0) || (diff == 75));
			qtyCheck = diff == 0;
			RISK_TIMER_LOGGER.info("qtyCheck : " + qtyCheck);
		}

		return (nameCheck && qtyCheck);
	}

	private boolean lossTriggerCheck(double pnl, int openPositionQuantity, int openPositionCount) {
		// first step : check if user has set stop loss or not
		// if set comparison will be against user stop loss
		RISK_TIMER_LOGGER.info("lossTriggerCheck() START");
		String userLoss = RiskStore.stopLoss;
		RISK_TIMER_LOGGER.info("userLoss = " + userLoss);
		if (userLoss.equals("NA")) {
			RISK_TIMER_LOGGER.info(" stop loss is not entered by user");
			double fixStopLoss = 0.0 - (openPositionQuantity / openPositionCount) * RiskStore.absPointsStopLoss;
			RISK_TIMER_LOGGER.info("fixStopLoss : " + fixStopLoss);
			RISK_TIMER_LOGGER.info("(pnl <= fixStopLoss)" + (pnl <= fixStopLoss));
			if (pnl <= fixStopLoss) {
				RISK_TIMER_LOGGER.info("lossTriggerCheck() END");
				return true;
			} else {
				RISK_TIMER_LOGGER.info("lossTriggerCheck() END");
				// OrderExecutor.waitTime = LocalTime.now(ZONE_ID).plusMinutes(15);
				return false;
			}
		} else {
			RISK_TIMER_LOGGER.info(" stop loss is entered by user");
			RISK_TIMER_LOGGER.info("(pnl <= userLoss)" + (pnl <= Double.parseDouble(userLoss)));
			if (pnl <= Double.parseDouble(userLoss)) {
				RISK_TIMER_LOGGER.info("lossTriggerCheck() END");
				return true;
			} else {
				RISK_TIMER_LOGGER.info("lossTriggerCheck() END");
				return false;
			}
		}
	}

	private boolean profitTriggerCheck(double pnl, int openPositionQuantity, int openPositionCount) {
		// first step : check if user has entered profit target or not
		// if not no comparison can be made
		RISK_TIMER_LOGGER.info("profitTriggerCheck() START");
		String profitTarget = RiskStore.profitTarget;
		RISK_TIMER_LOGGER.info("profitTarget : " + profitTarget);
		if (profitTarget.equals("NA")) {
			RISK_TIMER_LOGGER.info(" Profit target is not entered by user");
			RISK_TIMER_LOGGER.info("profitTriggerCheck() END");
			return false;
		} else {
			RISK_TIMER_LOGGER.info(" Profit target is entered by user");
			RISK_TIMER_LOGGER
					.info("(pnl >= Double.parseDouble(profitTarget))" + (pnl >= Double.parseDouble(profitTarget)));
			if (pnl >= Double.parseDouble(profitTarget)) {
				RISK_TIMER_LOGGER.info("profitTriggerCheck() END");
				return true;
			} else {
				RISK_TIMER_LOGGER.info("profitTriggerCheck() END");
				return false;
			}
		}
	}

}
