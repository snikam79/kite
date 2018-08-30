package com.kite.aws.util;

import static com.kite.aws.login.GlobalLoggerHandler.NEW_ORDERBOOK_LOGGER;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONException;

import com.kite.aws.inflation.InflationOrder;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.kiteconnect.utils.Constants;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.OrderParams;

public class Util {

	public static InflationOrder createInflationOrder(String data) {
		InflationOrder order = null;

		if (data != null) {
			String[] tokens = data.split("-");
			if ((tokens != null) && (tokens.length == 8)) {
				DecimalFormat df = new DecimalFormat("#.##");
				double lteSum = Double.valueOf(df.format(Double.parseDouble(tokens[5])));
				double gteSum = Double.valueOf(df.format(Double.parseDouble(tokens[6])));
				order = new InflationOrder(tokens[0], tokens[1], tokens[2], Boolean.valueOf(tokens[3]), tokens[4],
						lteSum, gteSum, tokens[7]);
			}
		}
		return order;
	}

	public static String getTime() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
		GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
		cal.add(Calendar.DATE, 0);
		return dateFormat.format(cal.getTime());
	}

	public static GregorianCalendar getTime(boolean robo) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
		GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();
		cal.add(Calendar.DATE, 0);
		return cal;
	}

	public static Object[] getAllOrders(KiteConnect kiteConnect) {
		List<Order> orderList = new ArrayList<Order>();

		Object[] response = new Object[3];

		if (kiteConnect == null) {

			response[0] = "error";
			response[1] = "kiteConnect is null in Util.getAllOrders()";
			response[2] = null;
			return response;
		}

		List<Order> orderBook = null;
		try {
			orderBook = kiteConnect.getOrders();
			if (orderList.size() < 1) {
				response[0] = "error";
				response[1] = "orderList is empty in Util.getAllOrders()";
				response[2] = null;

				return response;
			} else {
				response[0] = "success";
				response[1] = "success";
				response[2] = orderList;
				return response;
			}
		} catch (JSONException e) {
			response[0] = "error";
			response[1] = "JSONException in Util.getAllOrders()";
			response[2] = null;
			return response;
		} catch (KiteException e) {
			response[0] = "error";
			response[1] = "KiteException in Util.getAllOrders()";
			response[2] = null;
			return response;
		} catch (IOException e) {
			response[0] = "error";
			response[1] = "IOException in Util.getAllOrders()";
			response[2] = null;
			return response;
		}

	}

	public static Object[] getAllOrders(KiteConnect kiteConnect, String args) {
		if (args.equals("all")) {
			return getAllOrders(kiteConnect);
		}

		List<Order> orderList = new ArrayList<Order>();

		Object[] response = new Object[3];

		if (kiteConnect == null) {

			response[0] = "error";
			response[1] = "kiteConnect is null in Util.getAllOrders()";
			response[2] = null;
			return response;
		}

		List<Order> orderBook = null;
		try {
			orderBook = kiteConnect.getOrders();
			orderList = orderBook;
			if (orderList.size() < 1) {
				response[0] = "error";
				response[1] = "orderList is empty in Util.getAllOrders()";
				response[2] = null;

				return response;
			} else {
				response[0] = "success";
				response[1] = "success";
				List<Order> finalList = new ArrayList<Order>();
				for (Order item : orderList) {
					if (item.tradingSymbol.equals(args)) {
						finalList.add(item);
					}
				}
				response[2] = sortList(finalList);
				return response;
			}
		} catch (JSONException e) {
			response[0] = "error";
			response[1] = "JSONException in Util.getAllOrders()";
			response[2] = null;
			return response;
		} catch (KiteException e) {
			response[0] = "error";
			response[1] = "KiteException in Util.getAllOrders()";
			response[2] = null;
			return response;
		} catch (IOException e) {
			response[0] = "error";
			response[1] = "IOException in Util.getAllOrders()";
			response[2] = null;
			return response;
		}

	}

	public static List<Order> sortList(List<Order> orderList) {
		List<Order> list = new ArrayList<Order>();

		for (Order order : orderList) {
			if (order.status.equals("COMPLETE")) {
				list.add(order);
			}
		}
		int size = list.size();
		for (int i = 0; i < size - 1; i++) {
			for (int j = 0; j < (size - i - 1); j++) {
				String ts1 = list.get(j).exchangeTimestamp.toString();
				String ts2 = list.get(j + 1).exchangeTimestamp.toString();
				if (swapCheck(ts1, ts2)) {
					Order temp = list.get(j);
					list.set(j, list.get(j + 1));
					list.set(j + 1, temp);
				}
			}
		}

		return list;
	}

	// e.g. ts1 = 2015-12-20 15:01:43
	private static boolean swapCheck(String ts1, String ts2) {
		boolean check = false;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-M-dd HH:mm:ss");
		Date d1 = null;
		Date d2 = null;
		try {
			d1 = format.parse(ts1);
			d2 = format.parse(ts2);
			check = d2.after(d1);
		} catch (ParseException e) {
			NEW_ORDERBOOK_LOGGER.info("ParseException in Util.swapCheck() while comparing time stamps");
		}
		return check;
	}

	public static boolean checkForSkewness(double callLtp, double putLtp) {
		boolean check = false;

		NEW_ORDERBOOK_LOGGER.info(" callLtp : " + callLtp);
		NEW_ORDERBOOK_LOGGER.info(" putLtp : " + putLtp);
		double sum = callLtp + putLtp;
		NEW_ORDERBOOK_LOGGER.info(" sum : " + sum);
		double maxDiff = Math.round((sum / 10.0)) + 2.0; // Round off division.
		NEW_ORDERBOOK_LOGGER.info("maxDiff : " + maxDiff);
		if (Math.abs(callLtp - putLtp) <= maxDiff) {
			check = true;
		}

		return check;
	}

	public static String skewValue(double sum, double diff) {
		DecimalFormat df2 = new DecimalFormat(".##");
		double skewValue;
		double maxDiff = Math.round((sum / 10.0)) + 2.0;
		skewValue = Math.abs(maxDiff) - Math.abs(diff);
		return df2.format(Math.abs(skewValue));
	}

	public static OrderParams getOrderParams(String tradingSymbol, String quantity, String position, String product) {
		OrderParams orderParams = new OrderParams();
		orderParams.tradingsymbol = tradingSymbol;
		orderParams.quantity = Integer.valueOf(quantity);

		if (position.equalsIgnoreCase("buy")) {
			orderParams.transactionType = Constants.TRANSACTION_TYPE_BUY;
		} else {
			orderParams.transactionType = Constants.TRANSACTION_TYPE_SELL;
		}
		if (product.equalsIgnoreCase("mis")) {
			orderParams.product = Constants.PRODUCT_MIS;
		}

		if (product.equalsIgnoreCase("nrml")) {
			orderParams.product = Constants.PRODUCT_NRML;
		}
		orderParams.exchange = Constants.EXCHANGE_NFO;
		orderParams.orderType = Constants.ORDER_TYPE_MARKET;
		return orderParams;
	}

}
