package com.kite.aws.risk;

import static com.kite.aws.login.GlobalLoggerHandler.NEW_ORDERBOOK_LOGGER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import com.kite.aws.util.Util;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.kiteconnect.kitehttp.exceptions.KiteException;
import com.zerodhatech.models.Order;
import com.zerodhatech.models.Position;
import com.zerodhatech.models.Quote;;

public class NewOrderBook {
	
	//private List<Order> orderList;
	
	/*public NewOrderBook(List<Order> orderList) {
		this.orderList = orderList;
	}
	

	public List<Order> getOrderList() {
		return orderList;
	}*/
	
	public  Object[] getPL(KiteConnect connect,Position entry,double ltp) {
		
		if (entry.netQuantity > 0) {
			return getPLForLong(connect,entry,ltp);
		} else {
			return getPLForShort(connect,entry,ltp);
		}
	}
	

	
	private  Object[] getPLForShort(KiteConnect connect,Position entry,double ltp) {
		int netQuantity = Math.abs(entry.netQuantity);
		double pl = 0.0;
		
		String code = "";
		String message = "";
		String profitLoss = "";
		
		NEW_ORDERBOOK_LOGGER.info("Control in NewOrderBook.getPLForShort()");
		
		///	double ltp = getLtp(connect,entry.tradingSymbol);
		//for now we'll not use quote api
	//	double ltp = 0.0;
	//	ltp = getLtp(connect,entry.instrumentToken);
	//	ltp = getLtp(connect,entry.tradingSymbol);
	/*	if (ltp == 0.0) {
			ltp = entry.lastPrice;
		}*/
		/*if (ltp == 0.0) {
			code = "error";
			message = "Unable to calculate P&L as getLtp() is returning 0.0";
			profitLoss = "0.0";
			NEW_ORDERBOOK_LOGGER.info("Unable to calculate P&L as getLtp() is returning 0.0 in NewOrderBook.getPLForShort()");
			return new Object[] { code,message,profitLoss};
		}*/
		
		Object[] response = getAllBookOrders(connect,entry);
		String status = (String) response[0];
		if (status.equals("success")) {
			ArrayList<Order> matchList = (ArrayList<Order>) response[2];
		//	matchList = sortOrderList(matchList);
			int totalQty = Math.abs(netQuantity);
			double average = 0.0;
			double positionValue = 0.0;
			double buyValue = 0.0;
			double sellValue = 0.0;
			for (Order item : matchList) {
				String transactionType = item.transactionType;
				String orderType = item.orderType;
				int qty = Integer.parseInt(item.quantity);
				
				double price = getPrice(item);
				if (transactionType.equals("BUY")) {
					buyValue = buyValue + (qty * price);
					totalQty = totalQty + qty;
					//totalQty = totalQty - qty;
				}
				
				if (transactionType.equals("SELL")) {
					sellValue = sellValue + (qty * price);
					totalQty = totalQty - qty;
				}
				if (totalQty <= 0) {
					break;
				}
			}
			NEW_ORDERBOOK_LOGGER.info("Outside of for loop");
			NEW_ORDERBOOK_LOGGER.info("sellValue : " + sellValue);
			NEW_ORDERBOOK_LOGGER.info("buyValue : " + buyValue);
			
			
			positionValue = sellValue - buyValue;
			NEW_ORDERBOOK_LOGGER.info("positionValue = (sellValue - buyValue) : " +positionValue);
			average = positionValue/netQuantity;
			NEW_ORDERBOOK_LOGGER.info(" netQuantity : " +netQuantity);
			NEW_ORDERBOOK_LOGGER.info("average ="+average);
			NEW_ORDERBOOK_LOGGER.info("ltp : " +ltp);
			pl = (average - ltp) *netQuantity;
			NEW_ORDERBOOK_LOGGER.info("pl=" +pl);
			code = "success";
			message = "success";
			profitLoss ="" + pl;
			NEW_ORDERBOOK_LOGGER.info(entry.tradingSymbol + " " + entry.netQuantity);
			NEW_ORDERBOOK_LOGGER.info("profitLoss = " + profitLoss);
	} else {
		code = "error";
		message = (String) response[1];
		profitLoss = "0.0";
		NEW_ORDERBOOK_LOGGER.info("Unable to calculate P&L as getAllBookOrders() is returning error code in NewOrderBook.getPLForShort()");
	}
		return new Object[] { code,message,profitLoss};
	}
	
	private  Object[] getPLForLong(KiteConnect connect,Position entry,double ltp) {
		int netQuantity = Math.abs(entry.netQuantity);
		double pl = 0.0;
		
		NEW_ORDERBOOK_LOGGER.info("Control in NewOrderBook.getPLForLong()");
		
		String code = "";
		String message = "";
		String profitLoss = "";
		
	//	double ltp = getLtp(connect,entry.tradingSymbol);
		//for now we'll not use quote api
		//double ltp = 0.0;
		//ltp = getLtp(connect,entry.instrumentToken);
		//ltp = getLtp(connect,entry.tradingSymbol);
		/*if (ltp == 0.0) {
			ltp = entry.lastPrice;
		}*/
		
		/*if (ltp == 0.0) {
			code = "error";
			message = "Unable to calculate P&L as getLtp() is returning 0.0";
			profitLoss = "0.0";
			NEW_ORDERBOOK_LOGGER.info("Unable to calculate P&L as getLtp() is returning 0.0 in NewOrderBook.getPLForLong()");
			return new Object[] { code,message,profitLoss};
		}*/
		
		Object[] response = getAllBookOrders(connect,entry);
		String status = (String) response[0];
		if (status.equals("success")) {
			ArrayList<Order> matchList = (ArrayList<Order>) response[2];
			//matchList = sortOrderList(matchList);
			int totalQty = netQuantity;
			double average = 0.0;
			double positionValue = 0.0;
			double buyValue = 0.0;
			double sellValue = 0.0;
			for (Order item : matchList) {
				String transactionType = item.transactionType;
				int qty = Integer.parseInt(item.quantity);
				
				double price = getPrice(item);
				if (transactionType.equals("SELL")) {
					sellValue = sellValue + (qty * price);
					totalQty = totalQty + qty;
				}
				
				if (transactionType.equals("BUY")) {
					buyValue = buyValue + (qty * price);
					totalQty = totalQty - qty;
				}
				if (totalQty <= 0) {
					break;
				}
			}
			NEW_ORDERBOOK_LOGGER.info("Outside of for loop");
			NEW_ORDERBOOK_LOGGER.info(" buyValue : " +buyValue);
			NEW_ORDERBOOK_LOGGER.info(" sellValue : " +sellValue);
			positionValue = buyValue - sellValue;
			NEW_ORDERBOOK_LOGGER.info("positionValue : " +positionValue);
			average = positionValue/netQuantity;
			NEW_ORDERBOOK_LOGGER.info("netQuantity : " +netQuantity);
			NEW_ORDERBOOK_LOGGER.info("ltp : " +ltp);
			pl = (ltp - average) * netQuantity;
			code = "success";
			message = "success";
			profitLoss ="" + pl;
			NEW_ORDERBOOK_LOGGER.info(entry.tradingSymbol + " " + entry.netQuantity);
			NEW_ORDERBOOK_LOGGER.info("profitLoss = " + profitLoss);
		} else {
			code = "error";
			message = (String) response[1];
			profitLoss = "0.0";
			NEW_ORDERBOOK_LOGGER.info("Unable to calculate P&L as getAllBookOrders() is returning error code in NewOrderBook.getPLForLong()");
		}
		return new Object[] { code,message,profitLoss};
	}
	
	
	private  double getLtp(KiteConnect connect,String symbol) {
		Quote quote = null;
	//	boolean exceptionCheck = false;
		if (connect == null) {
			NEW_ORDERBOOK_LOGGER.info("kiteConnect is null in NewOrderBook.getLtp()");
			return 0.0;
		}
		try {
			String tradingSymbol = "NFO:" + symbol;
			 Map<String,Quote> quoteMap = null;
			 quoteMap = connect.getQuote(new String[] {tradingSymbol});
			 quote = quoteMap.get(tradingSymbol);
		} catch (JSONException  e) {
			NEW_ORDERBOOK_LOGGER.info("JSONException in NewOrderBook.getLtp()");
			String msg = e.getMessage();
			NEW_ORDERBOOK_LOGGER.info("Exception msg : " + msg);
		//	exceptionCheck = true;
			return 0.0;
		} catch (KiteException e) {
			NEW_ORDERBOOK_LOGGER.info("KiteException in NewOrderBook.getLtp()");
			String msg = e.getMessage();
			NEW_ORDERBOOK_LOGGER.info("Exception msg : " + msg);
			//	exceptionCheck = true;
			return 0.0;
		} catch (IOException e) {
			NEW_ORDERBOOK_LOGGER.info("IOException in NewOrderBook.getLtp()");
			String msg = e.getMessage();
			NEW_ORDERBOOK_LOGGER.info("Exception msg : " + msg);
			//	exceptionCheck = true;
			return 0.0;
		}
		/*if (exceptionCheck) { // means some exception was thrown while getting ltp
			NEW_ORDERBOOK_LOGGER.info("exceptionCheck is true in NewOrderBook.getLtp()");
			if (quote != null) {
				return quote.lastPrice;
			} else {
				NEW_ORDERBOOK_LOGGER.info("quote is null in NewOrderBook.getLtp()");
				return 0.0;
			}
		}*/
		return quote.lastPrice;
	}
	
	
	
	private Object[] getAllBookOrders(KiteConnect connect,Position entry) {
		Object[] response = new Object[3];
		ArrayList<Order> matchingOrderList = new ArrayList<Order>();
		if (connect == null) {
			response[0] = "error";
			response[1] = "KiteConnect is null in NewOrderBook.getAllBookOrders()";
			response[2] = null;
			NEW_ORDERBOOK_LOGGER.info(response[1]+"");
			return response;	
		}
		List<Order> ordersList = null;
		try {
			ordersList = connect.getOrders();
		} catch (KiteException e) {
			response[0] = "error";
			response[1] = "KiteException in NewOrderBook.getAllBookOrders()";
			response[2] = null;
			NEW_ORDERBOOK_LOGGER.info(response[1]+"");
			return response;
		} catch (JSONException e) {
			response[0] = "error";
			response[1] = "JSONException in NewOrderBook.getAllBookOrders()";
			response[2] = null;
			NEW_ORDERBOOK_LOGGER.info(response[1]+"");
			return response;
		} catch (IOException e) {
			response[0] = "error";
			response[1] = "IOException in NewOrderBook.getAllBookOrders()";
			response[2] = null;
			NEW_ORDERBOOK_LOGGER.info(response[1]+"");
			return response;
		}
		if (ordersList == null) {
			response[0] = "error";
			response[1] = "orderBook is null in NewOrderBook.getAllBookOrders()";
			response[2] = null;
			NEW_ORDERBOOK_LOGGER.info(response[1]+"");
			return response;
		}
		List<Order> localOrderList = null;
	//	localOrderList = orderBook.orders;
		localOrderList = ordersList;
		if (localOrderList == null) {
			response[0] = "error";
			response[1] = "orderBook is not null but localOrderList is null in NewOrderBook.getAllBookOrders()";
			response[2] = null;
			NEW_ORDERBOOK_LOGGER.info(response[1]+"");
			return response;
		}
	//	List<Order> tempOrderList = Util.sortList(localOrderList);
		
		int size = localOrderList.size();
		for (int cnt = size-1; cnt >= 0 ; cnt--) {
			Order item = localOrderList.get(cnt);
					String status = item.status;
					String tradingSymbol = item.tradingSymbol;
					// String transactionType = item.transactionType;
					String product = item.product;

					if ((status.equals("COMPLETE")) && (tradingSymbol.equals(entry.tradingSymbol))
							&& (product.equals(entry.product))) {
						matchingOrderList.add(item);
						response[0] = "success";
						response[1] = "Found at least one matching order";
					}
		}

		/*for (Order item : tempOrderList) {
		//	Order item = localOrderList.get(i);
			String status = item.status;
			String tradingSymbol = item.tradingSymbol;
			// String transactionType = item.transactionType;
			String product = item.product;

			if ((status.equals("COMPLETE")) && (tradingSymbol.equals(entry.tradingSymbol))
					&& (product.equals(entry.product))) {
				matchingOrderList.add(item);
				response[0] = "success";
				response[1] = "Found at least one matching order";
			}
		}*/
		if (matchingOrderList.size() < 1) {
			response[0] = "error";
			response[1] = "No matching order(s) found while scanning order book for : " + entry.tradingSymbol;
			response[2] = null;
			NEW_ORDERBOOK_LOGGER.info(response[1]+"");
			return response;
		}
		response[2] = matchingOrderList;
		return response;

	}
	
	
	
	private  double getPrice(Order order) {
		double ans = 0.0;
		if (Double.parseDouble(order.averagePrice) == 0.0) {
			ans = Double.parseDouble(order.price);
		} else {
			ans = Double.parseDouble(order.averagePrice);
		}
		return ans;
	}
	
	/*public static Object[] getPL(Position entry) {
	int netQuantity = entry.netQuantity;
	double pl = 0.0;
	
	String code = "";
	String message = "";
	String profitLoss = "";
	
	if (netQuantity == 0) {
		code = "success";
		message = "no open positions";
		profitLoss = "0.0";
	}
	
	//buy
	if (netQuantity > 0) {
		//double ltp = entry.lastPrice;
		double ltp = getLtp(entry.tradingSymbol);
		if (ltp == 0.0) {
			code = "error";
			message = "Unable to calculate P&L as getLtp() is returning 0.0";
			profitLoss = "0.0";
			return new Object[] { code,message,profitLoss};
		}
		
		int sellQty = entry.sellQuantity;
		
		// I'm Commenting this portion
		// It will eliminate garbage value issue from kite server
		if (sellQty == 0) { // clean position
			pl = (ltp - entry.buyPrice) * netQuantity;
			code = "success";
			message = "success";
			profitLoss ="" + pl;
			return new Object[] { code,message,profitLoss};
		}
		
		//get all matching orders
		//Object[] response = getAllBookOrders(entry.tradingSymbol,"BUY");
		Object[] response = getAllBookOrders(entry);
		String status = (String) response[0];
		if (status.equals("success")) {
			ArrayList<Order> matchList = (ArrayList<Order>) response[2];
			matchList = sortOrderList(matchList);
			int totalQty = netQuantity;
			double average = 0.0;
			double positionValue = 0.0;
			double buyValue = 0.0;
			double sellValue = 0.0;
			for (Order item : matchList) {
				String transactionType = item.transactionType;
				String orderType = item.orderType;
				int qty = Integer.parseInt(item.quantity);
				
				if (transactionType.equals("SELL")) {
					if (orderType.equals("MARKET")) {
						//double price = Double.parseDouble(item.averagePrice);
						double price = getPrice(item);
						//positionValue = positionValue + (qty * price);
						sellValue = sellValue + (qty * price);
					} else {
						if (orderType.equals("LIMIT")) {
						//	double price = Double.parseDouble(item.price);
							double price = getPrice(item);
							//positionValue = positionValue + (qty * price);
							sellValue = sellValue + (qty * price);
						}
					}
					totalQty = totalQty + qty;
				}
				
				if (transactionType.equals("BUY")) {
					if (orderType.equals("MARKET")) {
						//double price = Double.parseDouble(item.averagePrice);
						double price = getPrice(item);
						//positionValue = positionValue - (qty * price);
						//positionValue = positionValue + (qty * price);
						buyValue = buyValue + (qty * price);
					} else {
						if (orderType.equals("LIMIT")) {
						//	double price = Double.parseDouble(item.price);
							double price = getPrice(item);
							//positionValue = positionValue - (qty * price);
							//positionValue = positionValue + (qty * price);
							buyValue = buyValue + (qty * price);
						}
					}
					totalQty = totalQty - qty;
				}
				if (totalQty <= 0) {
					break;
				}
			}
			positionValue = buyValue - sellValue;
			average = positionValue/Math.abs(netQuantity);
			//average = Math.abs(average);
			pl = (ltp - average) * netQuantity;
			code = "success";
			message = "success";
			profitLoss ="" + pl;
			NEW_ORDERBOOK_LOGGER.info(entry.tradingSymbol + " " + entry.netQuantity);
			NEW_ORDERBOOK_LOGGER.info("profitLoss = " + profitLoss);
		} else {
			code = "error";
			message = (String) response[1];
			profitLoss = "0.0";
		}
		
	} else { // Short sell position
		//double ltp = entry.lastPrice;
		double ltp = getLtp(entry.tradingSymbol);
		if (ltp == 0.0) {
			code = "error";
			message = "Unable to calculate P&L as getLtp() is returning 0.0";
			profitLoss = "0.0";
			return new Object[] { code,message,profitLoss};
		}
		int buyQty = entry.buyQuantity;
		int netQty = Math.abs(entry.netQuantity);
		// I'm Commenting this portion
		// It will eliminate garbage value issue from kite server
		if (buyQty == 0) { // clean position
			pl = (entry.sellPrice - ltp) * netQty;
			code = "success";
			message = "success";
			profitLoss ="" + pl;
			return new Object[] {code,message,profitLoss};
		}
		
		//get all matching orders
		//Object[] response = getAllBookOrders(entry.tradingSymbol,"SELL");
		Object[] response = getAllBookOrders(entry);
		String status = (String) response[0];
		
		if (status.equals("success")) {
				ArrayList<Order> matchList = (ArrayList<Order>) response[2];
				matchList = sortOrderList(matchList);
				int totalQty = Math.abs(netQuantity);
				double average = 0.0;
				double positionValue = 0.0;
				double buyValue = 0.0;
				double sellValue = 0.0;
				for (Order item : matchList) {
					String transactionType = item.transactionType;
					String orderType = item.orderType;
					int qty = Integer.parseInt(item.quantity);
					
					if (transactionType.equals("BUY")) {
						if (orderType.equals("MARKET")) {
						//	double price = Double.parseDouble(item.averagePrice);
						double price = getPrice(item);
						//	positionValue = positionValue - (qty * price);
						buyValue = buyValue + (qty * price);
						} else {
							if (orderType.equals("LIMIT")) {
						//		double price = Double.parseDouble(item.price);
								double price = getPrice(item);
								//positionValue = positionValue - (qty * price);
								buyValue = buyValue + (qty * price);
							}
						}
						totalQty = totalQty + qty;
					}
					
					if (transactionType.equals("SELL")) {
						if (orderType.equals("MARKET")) {
						//	double price = Double.parseDouble(item.averagePrice);
							double price = getPrice(item);
							//positionValue = positionValue + (qty * price);
							sellValue = sellValue + (qty * price);
						} else {
							if (orderType.equals("LIMIT")) {
							//	double price = Double.parseDouble(item.price);
								double price = getPrice(item);
								//positionValue = positionValue + (qty * price);
								sellValue = sellValue + (qty * price);
							}
						}
						totalQty = totalQty - qty;
					}
					//NEW_ORDERBOOK_LOGGER.info("Inside for loop");
					//int qty = Integer.parseInt(item.quantity);
					//NEW_ORDERBOOK_LOGGER.info("qty = " +qty);
					//totalQty = totalQty - qty;
					//NEW_ORDERBOOK_LOGGER.info("totalQty = " + totalQty);
					double price = Double.parseDouble(item.averagePrice);
					NEW_ORDERBOOK_LOGGER.info("price = " +price);
					//positionValue = positionValue + (qty * price);
					NEW_ORDERBOOK_LOGGER.info("positionValue = " +positionValue);
					NEW_ORDERBOOK_LOGGER.info("condition (totalQty <= 0) = " +(totalQty <= 0));
					if (totalQty <= 0) {
						break;
					}
					
					if (totalQty >= qty) {
						double price = Double.parseDouble(item.averagePrice);
						positionValue = positionValue + (qty * price);
						totalQty = totalQty - qty;
						break;
					} else {
						totalQty = totalQty - qty;
						double price = Double.parseDouble(item.averagePrice);
						positionValue = positionValue + (qty * price);
					}
					
					
				}
				NEW_ORDERBOOK_LOGGER.info("Outside of for loop");
			//	average = positionValue/Math.abs(netQuantity);
				positionValue = sellValue - buyValue;
				average = positionValue/Math.abs(netQuantity);
				NEW_ORDERBOOK_LOGGER.info("average ="+average);
				pl = (average - ltp) * Math.abs(netQuantity);
				NEW_ORDERBOOK_LOGGER.info("pl=" +pl);
				code = "success";
				message = "success";
				profitLoss ="" + pl;
				NEW_ORDERBOOK_LOGGER.info(entry.tradingSymbol + " " + entry.netQuantity);
				NEW_ORDERBOOK_LOGGER.info("profitLoss = " + profitLoss);
		} else {
			code = "error";
			message = (String) response[1];
			profitLoss = "0.0";
		}
	}
	return new Object[] { code,message,profitLoss};
	
}*/
	
	
/*private static Object[] getAllBookOrders(String symbol,String opeation) {
		
		String code = "";
		String message = "";
		
		Object[] response = new Object[3];
		
		KiteConnect kiteConnect = KiteStore.kiteconnect;
		ArrayList<Order> matchingOrderList = new ArrayList<Order>();
		
		if (kiteConnect == null) {
			code = "error";
			message ="kiteConnect is null";
			matchingOrderList = null;
			
			response[0] = code;
			response[1] = message;
			response[2] = matchingOrderList;
			return response;
		}
		
		try {
			Order orderBook = kiteConnect.getOrders();
			List<Order> orderList = orderBook.orders;
			int size = orderList.size();
			
			if (size > 0) {
				for (int i = size-1; i>0 ;i--) {
					Order item = orderList.get(i);
					String status = item.status;
					String tradingSymbol = item.tradingSymbol;
					String transactionType = item.transactionType;
					String product = item.product;
					
					if  ((status.equals("COMPLETE")) && (tradingSymbol.equals(symbol)) && (transactionType.equals(opeation)) && (product.equals("MIS"))) {
						matchingOrderList.add(item);
					}
				}
			} else {
				code = "error";
				message ="order book is empty";
				matchingOrderList = null;
				
				response[0] = code;
				response[1] = message;
				response[2] = matchingOrderList;
				return response;
				
			}
		} catch (JSONException e) {
			code = "error";
			message ="JSONException in NewOrderBook.getAllBookOrders()";
			matchingOrderList = null;
			
			response[0] = code;
			response[1] = message;
			response[2] = matchingOrderList;
			return response;
		}
		catch (KiteException e) {
			code = "error";
			message ="KiteException in NewOrderBook.getAllBookOrders()";
			matchingOrderList = null;
			
			response[0] = code;
			response[1] = message;
			response[2] = matchingOrderList;
			return response;
		}
		
		if (matchingOrderList != null) {
			code = "success";
			message ="success";
		}
		
		response[0] = code;
		response[1] = message;
		response[2] = matchingOrderList;
		return response;
		
	}*/

}
