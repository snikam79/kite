package com.kite.aws.risk;

import static com.kite.aws.login.GlobalLoggerHandler.LOGIN_LOGGER;

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
import com.kite.aws.order.OrderExecutor;
import com.kite.aws.util.Util;
import com.zerodhatech.kiteconnect.KiteConnect;
import com.zerodhatech.models.Order;

@WebServlet("/risk")
public class RiskServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action");
		LOGIN_LOGGER.info("Control in RiskServlet for action = " + action);
		KiteConnect connect = (KiteConnect) getServletContext().getAttribute("kite");
		Gson gson = new GsonBuilder().create();
		PrintWriter writer = response.getWriter();

		if (connect == null) {
			LOGIN_LOGGER.info("connect is null from servlet context");
			RiskResponse riskResponse = new RiskResponse("na", "error", "connect is null from servlet context");
			writer.print(gson.toJson(riskResponse));
			return;
		}

		if (action != null) {
			RiskResponse riskResponse = null;

			switch (action) {
			case "orders":
				Object[] orderReply = Util.getAllOrders(connect);
				displayOrders(writer, gson, orderReply);
				break;
			case "exitall":
				// RiskStore.trailStopLoss = "NA";
				String[] reply = OrderExecutor.exitAllPositions(connect, null);
				riskResponse = new RiskResponse("exitall", reply[0], reply[1]);
				writer.print(gson.toJson(riskResponse));
				break;
			case "exithalf":
				String[] reply1 = OrderExecutor.exitHalfPositions(connect);
				riskResponse = new RiskResponse("exithalf", reply1[0], reply1[1]);
				writer.print(gson.toJson(riskResponse));
				break;
			case "getm2m":
				String m2mStr = RiskStore.m2m;
				String ts = Util.getTime();
				RiskStatus riskStatus = new RiskStatus(RiskStore.riskTimerStatus, RiskStore.riskTimerStatusMessage,
						m2mStr, RiskStore.profitTarget, RiskStore.stopLoss, ts);
				LOGIN_LOGGER.info("getm2m = " + gson.toJson(riskStatus));
				writer.print(gson.toJson(riskStatus));
				break;
			case "update":
				String profit = request.getParameter("profit");
				String loss = request.getParameter("loss");
				RiskStore.profitTarget = profit;

				String[] msg = RiskUtil.stopLossLimitCheck(connect, loss);
				if ((msg[0].equals("success")) && (msg[1].equals("success"))) {
					RiskStore.stopLoss = loss;
					String displayMessage = "Updated profit target to " + RiskStore.profitTarget + " and stop loss to "
							+ RiskStore.stopLoss;
					UpdateRiskPOJO obj = new UpdateRiskPOJO("success", displayMessage);
					writer.print(gson.toJson(obj));
				}

				if ((msg[0].equals("error")) && (msg[2] != null)) {
					RiskStore.stopLoss = msg[2];
					String displayMessage = "Updated profit target to " + RiskStore.profitTarget + " and stop loss to "
							+ RiskStore.stopLoss;
					UpdateRiskPOJO obj = new UpdateRiskPOJO("error", displayMessage);
					writer.print(gson.toJson(obj));
				}

				if ((msg[0].equals("fatal"))) {
					RiskStore.stopLoss = "NA";
					RiskStore.profitTarget = "NA";
					String displayMessage = "Unable to update stop loss due to error :" + msg[2];
					UpdateRiskPOJO obj = new UpdateRiskPOJO("error", displayMessage);
					writer.print(gson.toJson(obj));
				}
				break;
			}
		}
	}

	private void displayOrders(PrintWriter out, Gson gson, Object[] orderReply) {
		String code = (String) orderReply[0];

		if (code.equals("error")) {
			String message = (String) orderReply[1];
			out.println(message);
			return;
		}
		String tab = "\t";
		List<Order> orderList = (List<Order>) orderReply[2];
		out.println("orderId" + tab + "time" + tab + "type" + tab + "instrument" + tab + "quantity" + tab + "status");
		for (Order order : orderList) {
			String orderId = order.orderId;
			String time = order.exchangeTimestamp.toString();
			String type = order.transactionType;
			String instrument = order.tradingSymbol;
			String qty = order.quantity;
			String status = order.status;
			out.println(orderId + tab + time + tab + type + tab + instrument + tab + qty + tab + status);
		}

	}

}
