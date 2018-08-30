package com.kite.aws.order;

import static com.kite.aws.login.GlobalLoggerHandler.ORDER_LOGGER;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kite.aws.risk.RiskStore;
import com.kite.aws.util.Util;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.models.Order;

@WebServlet("/order")
public class OrderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String symbol = request.getParameter("symbol");

		KiteConnect connect = (KiteConnect) getServletContext().getAttribute("kite");
		Object[] reply = Util.getAllOrders(connect, symbol);

		List<Order> orderList = (List<Order>) reply[2];
		PrintWriter out = response.getWriter();
		for (Order order : orderList) {
			String str = " " + order.tradingSymbol + " " + order.quantity + " " + order.transactionType + " "
					+ order.exchangeTimestamp;
			out.println(str);
		}
		out.close();
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action");
		Gson gson = new GsonBuilder().create();
		PrintWriter writer = response.getWriter();

		if (action != null) {

			switch (action) {
			case "order":
				ORDER_LOGGER.info("Inside Order Servlet with action of 'orders'");
				String order = request.getParameter("order");
				NormalOrderResponse orderResponse = placeOrder(order);
				writer.print(gson.toJson(orderResponse));
				break;
			case "skewCheck":
				ORDER_LOGGER.info("Inside Order Servlet with action of 'skewCheck'");
				KiteConnect connect = (KiteConnect) getServletContext().getAttribute("kite");
				String callSymbol = request.getParameter("callSymbol");
				String putSymbol = request.getParameter("putSymbol");
				SkewResponse skewResponse = null;
				String[] reply = OrderExecutor.checkSkew(connect, callSymbol, putSymbol);
				skewResponse = new SkewResponse(reply[0], reply[1]);
				ORDER_LOGGER.info("Inside Order Servlet . 'skewCheck' Response:" + reply[1]);
				writer.print(gson.toJson(skewResponse));
				break;

			}
		}

	}

	private NormalOrderResponse placeOrder(String order) {
		NormalOrderResponse response = null;
		if (order != null) {
			String[] array = order.split("-");
			if (array != null) {
				if (array.length != 5) {
					ORDER_LOGGER.info("Order Servlet: place Order. Insufficient arguments to place order");
					response = new NormalOrderResponse("300", "insufficient arguments to place order");
				} else {
					KiteConnect connect = (KiteConnect) getServletContext().getAttribute("kite");
					if (connect == null) {
						ORDER_LOGGER.info("Order Servlet: place Order. Connect is null from servlet context");
						response = new NormalOrderResponse("300", "connect is null from servlet context");
					} else {
						ORDER_LOGGER.info("Order Servlet: place Order. Control in OrderServlet for placing twin order");
						RiskStore.skipFlag = 2;
						// Verify if order is eligible to be placed (check for skewness)
						String[] checkSkewReply = OrderExecutor.checkSkew(connect, array[1], array[2]);
						// Place Order if condition is success
						if (checkSkewReply[0].equals("200")) {
							ORDER_LOGGER.info("Inside Order Servlet. Skew Check Success. Kite Order to be placed");
							String[] reply = OrderExecutor.placeKiteOrder(connect, array[0], array[1], array[2],
									Boolean.valueOf(array[3]), array[4]);
							ORDER_LOGGER.info("NormalOrderResponse Code: " + reply[0]);
							ORDER_LOGGER.info("NormalOrderResponse Message: " + reply[1]);
							response = new NormalOrderResponse(reply[0], reply[1]);
						} else {
							ORDER_LOGGER.info("Skew Check Failed. Kite Order cannot be placed");
							response = new NormalOrderResponse("300", "Skew Check Failed. Kite Order cannot be placed");
						}
					}

				}
			} else {
				response = new NormalOrderResponse("300", "insufficient arguments to place order");
			}
		} else {
			response = new NormalOrderResponse("300", "insufficient arguments to place order");
		}

		return response;
	}

}
