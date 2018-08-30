package com.kite.aws.order;

import static com.kite.aws.login.GlobalLoggerHandler.ORDER_LOGGER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.kite.aws.risk.RiskStore;
import com.kite.aws.risk.RiskUtil;
import com.kite.aws.risk.StrangleLTPResponse;
import com.kite.aws.util.Util;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderParams;
import com.zerodhatech.models.Position;

public class OrderExecutor {

	public static String[] placeKiteOrder(KiteConnect connect, String quantity, String callSymbol, String putSymbol,
			boolean sell, String productType) {

		/*
		 * if(true){ String[] checkSkew = checkSkew(connect,callSymbol,putSymbol);
		 * return checkSkew; }
		 */
		String errorCode = "300";
		String returnMessage = "";

		ORDER_LOGGER.info("Control in OrderExecutor.placeKiteOrder()");
		ORDER_LOGGER.info("quantity = " + quantity);
		ORDER_LOGGER.info("callSymbol = " + callSymbol);
		ORDER_LOGGER.info("putSymbol = " + putSymbol);
		ORDER_LOGGER.info("sell = " + sell);
		ORDER_LOGGER.info("productType = " + productType);

		if (connect != null) {
			try {

				String position = "BUY";
				if (sell) {
					position = "SELL";
				}
				OrderParams callParams = Util.getOrderParams(callSymbol, quantity, position, productType);
				OrderParams putParams = Util.getOrderParams(putSymbol, quantity, position, productType);
				connect.placeOrder(callParams, Constants.VARIETY_REGULAR);
				connect.placeOrder(putParams, Constants.VARIETY_REGULAR);
				errorCode = "200";
				returnMessage = "Executed both orders successfully !!";
				ORDER_LOGGER.info("First order");
				String action = ((sell == true) ? "SELL" : "BUY");
				ORDER_LOGGER.info(callSymbol + " " + action + " " + quantity + " " + productType);
				ORDER_LOGGER.info("Second order");
				ORDER_LOGGER.info(putSymbol + " " + action + " " + quantity + " " + productType);
			} catch (KiteException e) {
				errorCode = "300";
				returnMessage = "Error while executing order(s). Please handle it manually immediately !!";
				String msg = e.getMessage();
				ORDER_LOGGER.info(msg);
			} catch (JSONException e) {
				errorCode = "300";
				returnMessage = "Error while executing order(s). Please handle it manually immediately !!";
				String msg = e.getMessage();
				ORDER_LOGGER.info(msg);
			} catch (IOException e) {
				errorCode = "300";
				returnMessage = "Error while executing order(s). Please handle it manually immediately !!";
				String msg = e.getMessage();
				ORDER_LOGGER.info(msg);
			}
		} else {
			errorCode = "300";
			returnMessage = "kiteConnect is null in RestOrderExecutor.placeKiteOrder()";
			ORDER_LOGGER.info(returnMessage);
		}
		return new String[] { errorCode, returnMessage };
	}

	public static String[] placeSingleOrder(KiteConnect connect, String instrument, String product, String quantity,
			boolean isSell) {
		String code = "";
		String returnMessage = "";

		String logMsg = instrument + " , " + product + " , " + quantity + " , " + isSell;
		ORDER_LOGGER.info("Got order for execution :" + logMsg);

		try {
			String position = "BUY";
			if (isSell) {
				position = "SELL";
			}
			OrderParams param = Util.getOrderParams(instrument, quantity, position, product);
			connect.placeOrder(param, Constants.VARIETY_REGULAR);
		} catch (JSONException | IOException | KiteException e) {
			code = "error";
			returnMessage = "Error while executing order...Please take manual control";
			return new String[] { code, returnMessage };
		}
		code = "success";
		returnMessage = "success";

		return new String[] { code, returnMessage };
	}

	public static String[] cancelPendingOrders(KiteConnect connect) {
		String[] reply = new String[2];
		String errorCode = null;
		String returnMessage = null;
		ORDER_LOGGER.info("Control in cancelPendingOrders()");

		if (connect == null) {
			errorCode = "error";
			returnMessage = "Please genrate access token first !!";
			reply[0] = errorCode;
			reply[1] = returnMessage;
			ORDER_LOGGER.info(returnMessage);
			return reply;
		}
		List<Order> ordersList = null;
		try {
			ordersList = connect.getOrders();
		} catch (JSONException e) {
			errorCode = "error";
			returnMessage = "JSONException in cancelPendingOrders()";
			reply[0] = errorCode;
			reply[1] = returnMessage;
			ORDER_LOGGER.info(e.getMessage());
			return reply;
		} catch (KiteException e) {
			errorCode = "error";
			returnMessage = "KiteException in cancelPendingOrders()";
			reply[0] = errorCode;
			reply[1] = returnMessage;
			ORDER_LOGGER.info(e.getMessage());
			return reply;
		} catch (IOException e) {
			errorCode = "error";
			returnMessage = "KiteException in cancelPendingOrders()";
			reply[0] = errorCode;
			reply[1] = returnMessage;
			ORDER_LOGGER.info(e.getMessage());
			return reply;
		}
		if ((ordersList == null) || (ordersList.size() == 0)) {
			errorCode = "error";
			returnMessage = "orderList is null or empty in cancelPendingOrders()";
			reply[0] = errorCode;
			reply[1] = returnMessage;
			ORDER_LOGGER.info(returnMessage);
			return reply;
		}

		for (Order item : ordersList) {
			if (item.status.equals("OPEN")) {
				try {
					connect.cancelOrder(item.orderId, "regular");
				} catch (JSONException e) {
					errorCode = "error";
					returnMessage = "JSONException while cancelling pending order in cancelPendingOrders()";
					reply[0] = errorCode;
					reply[1] = returnMessage;
					ORDER_LOGGER.info(e.getMessage());
					return reply;
				} catch (KiteException e) {
					errorCode = "error";
					returnMessage = "KiteException while cancelling pending order in cancelPendingOrders()";
					reply[0] = errorCode;
					reply[1] = returnMessage;
					ORDER_LOGGER.info(e.getMessage());
					return reply;
				} catch (IOException e) {
					errorCode = "error";
					returnMessage = "IOException while cancelling pending order in cancelPendingOrders()";
					reply[0] = errorCode;
					reply[1] = returnMessage;
					ORDER_LOGGER.info(e.getMessage());
					return reply;
				}
			}
		}

		errorCode = "success";
		returnMessage = "success";
		reply[0] = errorCode;
		reply[1] = returnMessage;

		return reply;

	}

	// this method will be used by risk timer mainly.
	// also risk servlet can use it as well but not that much like timer
	public static String[] exitAllPositions(KiteConnect connect, List<Position> openPositions) {
		ORDER_LOGGER.info("Control in new exitAllPositions()");
		RiskStore.skipFlag = 2;

		// Check if there are any open positions. If any open positions then proceed
		// else exit
		Map<String, List<Position>> positionMap = null;
		try {
			positionMap = connect.getPositions();
		} catch (IOException | JSONException | KiteException e) {
			ORDER_LOGGER.info("IOException or JSONException KiteException in new exitAllPositions()");
			return new String[] { "error", "IOException or JSONException KiteException in new exitAllPositions()" };
		}

		if (positionMap == null) {
			ORDER_LOGGER.info("OrderExecutor: exitAllPositions(): There are no Open Positions");
			return new String[] { "error", "There are no Open positions" };
		}

		List<Position> list = positionMap.get("net");
		openPositions = new ArrayList<Position>();
		for (Position p : list) {
			if (p.netQuantity != 0) {
				openPositions.add(p);
			}
		}

		if (openPositions.isEmpty()) {
			ORDER_LOGGER.info("OrderExecutor: exitAllPositions(): There are no Open Positions");
			return new String[] { "error", "There are no Open positions" };
		}

		String[] cancelReply = cancelPendingOrders(connect);
		if (cancelReply[0].equals("error")) {
			return cancelReply;
		}

		String[] reply = new String[2];

		int size = openPositions.size();
		ORDER_LOGGER.info("Open position size in new exitAllPositions() :: " + size);

		for (Position item : openPositions) {
			String instrument = item.tradingSymbol;
			int netQuantity = item.netQuantity;
			String product = item.product;
			boolean sellCheck = (netQuantity > 0 ? true : false);
			String netQuantityStr = Math.abs(netQuantity) + "";

			ORDER_LOGGER.info(" instrument :: " + instrument);
			ORDER_LOGGER.info(" netQuantityStr :: " + netQuantityStr);
			ORDER_LOGGER.info(" product :: " + product);
			ORDER_LOGGER.info(" sellCheck :: " + sellCheck);

			String[] response = placeSingleOrder(connect, instrument, product, netQuantityStr, sellCheck);
			if (response[0].equals("error")) {
				reply[0] = response[0];
				reply[1] = response[1];
				ORDER_LOGGER.info("in new exitAllPositions()  reply[0] :: " + reply[0]);
				ORDER_LOGGER.info("in new exitAllPositions()  reply[1] :: " + reply[1]);
				return reply;
			}
		}
		reply[0] = "success";
		reply[1] = "Exited all open positions";
		ORDER_LOGGER.info("in new exitAllPositions()  reply[0] :: " + reply[0]);
		ORDER_LOGGER.info("in new exitAllPositions()  reply[1] :: " + reply[1]);
		return reply;

	}
	// This was current version of exit all positions.
	// We'll not use it for time being.
	/*
	 * public static String[] exitAllPositions(KiteConnect connect) {
	 * ORDER_LOGGER.info("Control in exitAllPositions()"); RiskStore.skipFlag = 2;
	 * String[] cancelReply = cancelPendingOrders(connect); if
	 * (cancelReply[0].equals("error")) { return cancelReply; } String errorCode =
	 * null; String returnMessage = null;
	 * 
	 * String[] reply = new String[2];
	 * 
	 * 
	 * if (connect == null) { errorCode = "error"; returnMessage =
	 * "Please genrate access token first !!"; reply[0] = errorCode; reply[1] =
	 * returnMessage; return reply; }
	 * 
	 * Map<String, List<Position>> positionMap = null; try { positionMap =
	 * connect.getPositions(); } catch (JSONException e) { errorCode = "error";
	 * returnMessage = "JSONException in exitAllPositions()"; reply[0] = errorCode;
	 * reply[1] = returnMessage; return reply; } catch (KiteException e) { errorCode
	 * = "error"; returnMessage = "KiteException in exitAllPositions()"; reply[0] =
	 * errorCode; reply[1] = returnMessage; return reply; } catch (IOException e) {
	 * errorCode = "error"; returnMessage = "IOException in exitAllPositions()";
	 * reply[0] = errorCode; reply[1] = returnMessage; return reply; } int
	 * openPositionCount = 0; for (Position item : positionMap.get("net")) { int
	 * netQuantity = item.netQuantity; if (netQuantity != 0) { openPositionCount =
	 * openPositionCount + 1; } }
	 * 
	 * if (openPositionCount == 0) { errorCode = "error"; returnMessage =
	 * "No open positions to square off"; reply[0] = errorCode; reply[1] =
	 * returnMessage; return reply; } else { List<Position> netPositions =
	 * positionMap.get("net");
	 * 
	 * List<SquareOffOrder> squareOffList = new ArrayList<SquareOffOrder>(); for
	 * (Position item :netPositions) { if (item.netQuantity != 0) { String
	 * instrument = item.tradingSymbol; int netQuantity = item.netQuantity; String
	 * product = item.product; boolean sellCheck = ( netQuantity > 0 ? true :
	 * false); String netQuantityStr = Math.abs(netQuantity) + ""; SquareOffOrder
	 * order = new SquareOffOrder(instrument,product,netQuantityStr,sellCheck);
	 * squareOffList.add(order); } }
	 * 
	 * int successCount = 0; for (SquareOffOrder order : squareOffList) { String[]
	 * response = placeSingleOrder(connect,order.getInstrument(),
	 * order.getProduct(), order.getQuantity(), order.isSell()); if
	 * (response[0].equals("error")) { errorCode = "error"; returnMessage =
	 * "Error while squaring off....Please handle it manually"; reply[0] =
	 * errorCode; reply[1] = returnMessage; return reply; } else { successCount++; }
	 * }
	 * 
	 * if (squareOffList.size() == successCount) { errorCode = "success";
	 * returnMessage = "Squared off all open positions !!"; reply[0] = errorCode;
	 * reply[1] = returnMessage; return reply; } } return reply; }
	 */

	public static String[] exitHalfPositions(KiteConnect connect) {
		String errorCode = null;
		String returnMessage = null;
		ORDER_LOGGER.info("Control in exitHalfPositions() ");

		String[] reply = new String[2];

		if (connect == null) {
			errorCode = "error";
			returnMessage = "Please genrate access token first !!";
			reply[0] = errorCode;
			reply[1] = returnMessage;
			return reply;
		}
		Map<String, List<Position>> positionMap = null;
		try {
			positionMap = connect.getPositions();
		} catch (JSONException e) {
			errorCode = "error";
			returnMessage = "JSONException in exitHalfPositions()";
			reply[0] = errorCode;
			reply[1] = returnMessage;
			return reply;
		} catch (KiteException e) {
			errorCode = "error";
			returnMessage = "KiteException in exitHalfPositions()";
			reply[0] = errorCode;
			reply[1] = returnMessage;
			return reply;
		} catch (IOException e) {
			errorCode = "error";
			returnMessage = "IOException in exitHalfPositions()";
			reply[0] = errorCode;
			reply[1] = returnMessage;
			return reply;
		}
		int openPositionCount = 0;
		for (Position item : positionMap.get("net")) {
			int netQuantity = item.netQuantity;
			if (netQuantity != 0) {
				openPositionCount = openPositionCount + 1;
			}
		}

		if (openPositionCount == 0) {
			errorCode = "error";
			returnMessage = "No open positions to square off";
			reply[0] = errorCode;
			reply[1] = returnMessage;
			return reply;
		} else {
			List<SquareOffOrder> squareOffList = new ArrayList<SquareOffOrder>();
			for (Position item : positionMap.get("net")) {
				if (item.netQuantity != 0) {
					String instrument = item.tradingSymbol;
					int netQuantity = getHalfQuantity(item.netQuantity);
					String product = item.product;
					boolean sellCheck = (netQuantity > 0 ? true : false);
					String netQuantityStr = Math.abs(netQuantity) + "";
					SquareOffOrder order = new SquareOffOrder(instrument, product, netQuantityStr, sellCheck);
					squareOffList.add(order);
				}
			}

			int successCount = 0;
			for (SquareOffOrder order : squareOffList) {
				String[] response = placeSingleOrder(connect, order.getInstrument(), order.getProduct(),
						order.getQuantity(), order.isSell());
				if (response[0].equals("error")) {
					errorCode = "error";
					returnMessage = "Error while squaring off half quantity....Please handle it manually";
					reply[0] = errorCode;
					reply[1] = returnMessage;
					return reply;
				} else {
					successCount++;
				}
			}
			if (squareOffList.size() == successCount) {
				errorCode = "success";
				returnMessage = "Squared off half positions !!";
				reply[0] = errorCode;
				reply[1] = returnMessage;
				return reply;
			}
		}
		return reply;
	}

	public static int getHalfQuantity(int qty) {
		int ans = 0;
		if (qty % 2 == 0) {
			ans = qty / 2;
		} else {
			if (qty > 0) {
				ans = qty / 2 - 37;
			} else {
				ans = qty / 2 + 37;
			}
		}
		return ans;
	}

	public static String[] checkSkew(KiteConnect connect, String callSymbol, String putSymbol) {
		String errorCode = "300";
		String returnMessage = "";
		String strategyDiff = "";
		String strategySum = "";

		ORDER_LOGGER.info("Control in OrderExecutor.checkSkew()");
		ORDER_LOGGER.info("callSymbol = " + callSymbol);
		ORDER_LOGGER.info("putSymbol = " + putSymbol);

		if (connect != null) {
			String[] symbols = new String[2];
			symbols[0] = "NFO:" + callSymbol;
			symbols[1] = "NFO:" + putSymbol;

			StrangleLTPResponse reply = RiskUtil.getLTPForStrangle(connect, symbols);

			if (reply.getCode().equals("success")) {
				Map<String, String> map = reply.getLtpMap();
				strategyDiff = Double.toString(
						Math.abs(Double.parseDouble(map.get(symbols[0])) - Double.parseDouble(map.get(symbols[1]))));
				strategySum = Double.toString(
						Math.abs(Double.parseDouble(map.get(symbols[0])) + Double.parseDouble(map.get(symbols[1]))));
				boolean skewCheck = Util.checkForSkewness(Double.parseDouble(map.get(symbols[0])),
						Double.parseDouble(map.get(symbols[1])));
				if (skewCheck == false) {
					errorCode = "300";
					returnMessage = "Strategy skewed by "
							+ Util.skewValue(Double.parseDouble(strategySum), Double.parseDouble(strategyDiff))
							+ " . Not allowed to enter position for callSymbol: " + callSymbol + " and putSymbol: "
							+ putSymbol + " . Strategy Sum is:" + strategySum;
					ORDER_LOGGER.info(returnMessage);
					ORDER_LOGGER.info("call LTP : " + map.get(symbols[0]));
					ORDER_LOGGER.info("put LTP : " + map.get(symbols[1]));
					return new String[] { errorCode, returnMessage };
				} else {

					errorCode = "200";
					returnMessage = "Strategy non-skewed by"
							+ Util.skewValue(Double.parseDouble(strategySum), Double.parseDouble(strategyDiff))
							+ " . Allowed to enter position for callSymbol: " + callSymbol + " and putSymbol: "
							+ putSymbol + " . Strategy Sum is:" + strategySum;
					ORDER_LOGGER.info(returnMessage);
					ORDER_LOGGER.info("call LTP : " + map.get(symbols[0]));
					ORDER_LOGGER.info("put LTP : " + map.get(symbols[1]));
					return new String[] { errorCode, returnMessage };

				}

			} else {
				ORDER_LOGGER.info("Exception in RiskUtil.getLTPForStrangle()");
				ORDER_LOGGER.info(reply.getMessge());
				return new String[] { errorCode, reply.getMessge() };
			}

		} else {
			errorCode = "300";
			returnMessage = "kiteConnect is null in OrderExecutor.checkSkew()";
			ORDER_LOGGER.info(returnMessage);
		}
		return new String[] { errorCode, returnMessage };
	}

}
