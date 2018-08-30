package com.kite.aws.inflation;

import static com.kite.aws.inflation.InflationStore.inflationSum;
import static com.kite.aws.inflation.InflationStore.orderStatus;
import static com.kite.aws.inflation.InflationStore.orderStatusMessage;
import static com.kite.aws.login.GlobalLoggerHandler.INFLATION_LOGGER;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.TimerTask;

import javax.servlet.ServletContext;

import com.kite.aws.order.OrderExecutor;
import com.kite.aws.risk.RiskStore;
import com.kite.aws.risk.RiskUtil;
import com.kite.aws.risk.StrangleLTPResponse;
import com.zerodhatech.kiteconnect.KiteConnect;

public class InflationTimer extends TimerTask {
	private volatile ServletContext context;
	
	
	public InflationTimer(ServletContext context) {
		this.context = context;
	}

	@Override
	public void run() {
	//	System.out.println("Entry -> InflationTimer");
		InflationOrder order = InflationStore.inflationOrder;

		if (order == null) {
			setStatus("NA", "No order to execute", "0");
	//		System.out.println("Exit -> InflationTimer");
			return;
		}

		//KiteConnect connect = KiteStore.kiteconnect;
		KiteConnect connect = (KiteConnect)this.context.getAttribute("kite");
		if (connect == null) {
			setStatus("error", "KiteConnect is null in InflationTimer.run() from HttpSession", "0");
			
	//		System.out.println("Exit -> InflationTimer");
			return;
		}
		
		String[] symbols = new String[2]; 
		symbols[0] = "NFO:"+order.getCallSymbol();
		symbols[1] = "NFO:"+order.getPutSymbol();
		
		StrangleLTPResponse response = RiskUtil.getLTPForStrangle(connect, symbols);
		if (response.getCode().equals("success")) {
			Map<String, String> map = response.getLtpMap();
			double sum = Double.parseDouble(map.get(symbols[0])) + Double.parseDouble(map.get(symbols[1]));
			DecimalFormat df = new DecimalFormat("#.##");
			sum = Double.valueOf(df.format(sum));
			
			//String operator = order.getOperator();
			
			// Operator would be both 'lte' and 'gte''
		// Below condition will be checked only if the position selected on UI would be "ANY" 
			// sum <= order.getLteSum()>> position should be SELL   : order.setSell(SELL);
			// sum >= order.getGteSum() >> position should be BUY   : order.setSell(BUY);
			INFLATION_LOGGER.info("Before condition check of 'ANY' in Inflation Timer > order.isSell():" + order.isSell());
			
			if(order.getPosition().equalsIgnoreCase("ANY")){
				if (sum <= order.getLteSum())
					order.setSell(true);
				if (sum >= order.getGteSum())
					order.setSell(false);
			}
				
			INFLATION_LOGGER.info("After condition check of 'ANY' in Inflation Timer > order.isSell():" + order.isSell());
				if (sum <= order.getLteSum()|| sum >= order.getGteSum()) {
					
					placeOrder(connect,order, sum);
					INFLATION_LOGGER.info("Order Placed:");
				} else {
					setStatus("NO_TRIGGER", "NO_TRIGGER", "" + sum);
					
				}
			
			
			/*if (operator.equals("lte")) {
				if (sum <= order.getSum()) {
					placeOrder(connect,order, sum);
				} else {
					setStatus("NO_TRIGGER", "NO_TRIGGER", "" + sum);
				}
			}
			
			if (operator.equals("gte")) {
				if (sum >= order.getSum()) {
					placeOrder(connect,order, sum);
				} else {
					setStatus("NO_TRIGGER", "NO_TRIGGER", "" + sum);
				}
			}*/
			
		} else {
			INFLATION_LOGGER.info("Exception in Inflation timer ::" +response.getMessge());
			setStatus("NO_TRIGGER", "NO_TRIGGER", "0");
		}
		
		// old code

		/*Quote callQuote = null;
		Quote putQuote = null;

		try {
			String callTradingSymbol = "NFO:"+order.getCallSymbol();
			String putTradingSymbol = "NFO:"+order.getPutSymbol();
			Map<String, Quote> callMap = connect.getQuote(new String [] {callTradingSymbol});
			Map<String, Quote> putMap = connect.getQuote(new String [] {putTradingSymbol});
			callQuote = callMap.get(callTradingSymbol);
			putQuote = putMap.get(putTradingSymbol);
		} catch (KiteException e) {
			setStatus("error", "KiteException in InflationTimer while getting quotes", "0");
			return;
		} catch (JSONException e) {
			setStatus("error", "JSONException in InflationTimer while getting quotes", "0");
			return;
		} catch (IOException e) {
			setStatus("error", "IOException in InflationTimer while getting quotes", "0");
			return;
		}*/

	}

	public static void placeOrder(KiteConnect connect,InflationOrder order, double sum) {
		 RiskStore.skipFlag = 2;
		String[] replyArray = OrderExecutor.placeKiteOrder(connect,order.getQuantity(), order.getCallSymbol(),
				order.getPutSymbol(), order.isSell(), order.getProductType());
		
		if (replyArray[0].equals("200")) {
			setStatus("success", "Executed inflation order at sum", "" + sum);
			InflationStore.inflationOrder = null;
		} else {
			setStatus("error", "Error while executing inflation order !!", "" + sum);
			//InflationStore.inflationOrder = null;
		}
		
		/*if (sum < 0) {
			if (replyArray[0].equals("200")) {
				setStatus("success", "Executed inflation order at market rate","0");
				InflationStore.inflationOrder = null;
			} else {
				setStatus("error", "Error while executing inflation order at market rate!!", "" + "0");
				InflationStore.inflationOrder = null;
			}
		}*/
	}

	private static void setStatus(String status, String message, String sum) {
		orderStatus = status;
		orderStatusMessage = message;
		inflationSum = sum;
	}

}
