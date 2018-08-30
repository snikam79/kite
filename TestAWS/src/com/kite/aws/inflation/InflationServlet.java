package com.kite.aws.inflation;

import static com.kite.aws.login.GlobalLoggerHandler.INFLATION_LOGGER;
import static com.kite.aws.login.GlobalLoggerHandler.ORDER_LOGGER;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kite.aws.order.NormalOrderResponse;
import com.kite.aws.order.OrderExecutor;
import com.kite.aws.order.SkewResponse;
import com.kite.aws.util.Util;
import com.zerodhatech.kiteconnect.KiteConnect;
/**
 * Servlet implementation class InflationServlet
 */
@WebServlet("/inflation")
public class InflationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//doGet(request, response);
		PrintWriter out = response.getWriter();
		Gson gson = new GsonBuilder().create();
		String action = request.getParameter("action");
		INFLATION_LOGGER.info("Control in InflationServlet for action = " +action);
		
		KiteConnect connect = (KiteConnect)getServletContext().getAttribute("kite");
		if (connect == null) {
			INFLATION_LOGGER.info("connect is null in InflationServlet from servlet context");
			InflationResponse reply = new InflationResponse("error", "connect is null in InflationServlet from servlet context");
			out.println(gson.toJson(reply));
			return;
		}
		
		if (action.equals("skewCheck")) {
			INFLATION_LOGGER.info("Inside Inflation Servlet with action of 'skewCheck'");
			String callSymbol = request.getParameter("callSymbol");
			String putSymbol = request.getParameter("putSymbol");
			
			SkewResponse skewResponse = null;
		    String[] reply = OrderExecutor.checkSkew(connect, callSymbol, putSymbol);
		    skewResponse = new SkewResponse(reply[0],reply[1]);
		    INFLATION_LOGGER.info("Inside Inflation Servlet . 'skewCheck' Response:"+reply[1]);
		    out.println(gson.toJson(skewResponse));
		}
		
		if (action.equals("place")) {
			String data = request.getParameter("data");
			INFLATION_LOGGER.info("Got data for InflationServlet :");
			INFLATION_LOGGER.info(data);
			InflationResponse reply = null;
			// Do the skew check. if non skewed then only create the order. else error message saying order cannot be placed
			String[] array = data.split("-");
			if(array!=null) {
			 String[] checkSkewReply = OrderExecutor.checkSkew(connect,array[1],array[2]);
			 if(checkSkewReply[0].equals("200")){
				    INFLATION_LOGGER.info("Inside Inflation Servlet. Skew Check Success. Kite Order to be placed");	 
				    InflationOrder inflationOrder = Util.createInflationOrder(data);
				    if (inflationOrder != null) {
						InflationStore.inflationOrder = inflationOrder;
						reply = new InflationResponse("success", "Placed order");
					} else {
						reply = new InflationResponse("error", "Insufficient arguments ");
					}
					 }else{
						 ORDER_LOGGER.info("Inside Inflation Servlet. Skew Check Failed. Kite Order cannot be placed");
						 reply = new InflationResponse("error","Skew Check Failed. Kite Order cannot be placed for Inflation");
					 }
			}
			out.println(gson.toJson(reply));
		}
		
		if (action.equals("enter")) {
			InflationOrder order = InflationStore.inflationOrder;
			InflationResponse reply = null;
			if (order == null) {
				reply = new InflationResponse("error", "There is no inflation order");
			} else {
				InflationTimer.placeOrder(connect,order, -1.0);
				if (InflationStore.orderStatus.equals("success")) {
					reply = new InflationResponse("success", "Executed inflation order at market rate");
				} else {
					reply = new InflationResponse("error", "Error while executing inflation order at market rate");	
				}
			}
			out.println(gson.toJson(reply));
		}
		
		if (action.equals("cancel")) {
			InflationStore.inflationOrder = null;
			InflationResponse reply = new InflationResponse("success", "Cancelled order successfully");
			out.println(gson.toJson(reply));
		}
		
		if (action.equals("status")) {
			
			String status = InflationStore.orderStatus;
			if (status.equals("NA")) {
				InflationResponse reply = new InflationResponse("NA", "No order to execute");
				out.println(gson.toJson(reply));
			}
			
			if (status.equals("error")) {
				InflationResponse reply = new InflationResponse("error", InflationStore.orderStatusMessage);
				out.println(gson.toJson(reply));
			}
			
			if (status.equals("NO_TRIGGER")) {
				InflationStatusReply reply = new InflationStatusReply();
				reply.setOrderStatus(status);
				reply.setQuantity(InflationStore.inflationOrder.getQuantity());
				reply.setCallSymbol(InflationStore.inflationOrder.getCallSymbol());
				reply.setPutSymbol(InflationStore.inflationOrder.getPutSymbol());
				if (InflationStore.inflationOrder.isSell()) {
					reply.setSell("Sell");
				} else {
					reply.setSell("Buy");
				}
				
				reply.setProductType(InflationStore.inflationOrder.getProductType());
			//	String sum = InflationStore.inflationSum + "" + " , " +InflationStore.inflationOrder.getSum() + "";
				/*reply.setTriggerSum(InflationStore.inflationOrder.getSum() + "");*/
				reply.setLteSum(InflationStore.inflationOrder.getLteSum() + "");
				reply.setGteSum(InflationStore.inflationOrder.getGteSum()+ "");
				reply.setRealTimeSum(InflationStore.inflationSum + "");
				reply.setTime(Util.getTime());
				reply.setOrderStatusMessage(InflationStore.orderStatusMessage);
				
				out.println(gson.toJson(reply));
			}
			
			if (status.equals("success")) {
				InflationResponse reply = new InflationResponse("success", InflationStore.orderStatusMessage);
				out.println(gson.toJson(reply));
			}
			
			
			/*String message = InflationStore.orderStatusMessage;
			
			
			if (InflationStore.inflationOrder != null) {
				InflationStatusReply reply = new InflationStatusReply();
				reply.setOrderStatus(status);
				reply.setQuantity(InflationStore.inflationOrder.getQuantity());
				reply.setCallSymbol(InflationStore.inflationOrder.getCallSymbol());
				reply.setPutSymbol(InflationStore.inflationOrder.getPutSymbol());
				if (InflationStore.inflationOrder.isSell()) {
					reply.setSell("Sell");
				} else {
					reply.setSell("Buy");
				}
				
				reply.setProductType(InflationStore.inflationOrder.getProductType());
			//	String sum = InflationStore.inflationSum + "" + " , " +InflationStore.inflationOrder.getSum() + "";
				reply.setTriggerSum(InflationStore.inflationOrder.getSum() + "");
				reply.setRealTimeSum(InflationStore.inflationSum + "");
				reply.setTime(Util.getTime());
				reply.setOrderStatusMessage(InflationStore.orderStatusMessage);
				
				out.println(gson.toJson(reply));
			} else {
				InflationStatusReply reply = new InflationStatusReply();
				reply.setOrderStatus("NA");
				reply.setTime(Util.getTime());
				out.println(gson.toJson(reply));
			}*/
		}
	}

}
