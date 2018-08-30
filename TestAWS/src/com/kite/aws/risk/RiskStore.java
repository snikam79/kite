package com.kite.aws.risk;

public class RiskStore {

	public static String profitTarget = "NA";

	public static String stopLoss = "";

	public static String riskTimerStatus;

	public static String riskTimerStatusMessage;

	public static String m2m;

	public static int absPointsStopLoss = 5;

	public volatile static int skipFlag = 0;

}
