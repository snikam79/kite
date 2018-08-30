package com.kite.aws.inflation;

public class InflationOrder {
	
	private String quantity;
	private String callSymbol;
	private String putSymbol;
	private boolean sell;
	private String productType;
	//private KiteConnect connect;
	//private String expiry;
	private double sum;
	private double lteSum;
	private double gteSum;
	private String operator;
	
	private String position;
	
	
	
	public InflationOrder(String quantity, String callSymbol, String putSymbol,
			boolean sell, String productType,  double lteSum,double gteSum,String position) {
		super();
		this.quantity = quantity;
		this.callSymbol = callSymbol;
		this.putSymbol = putSymbol;
		this.sell = sell;
		this.productType = productType;
		this.lteSum = lteSum;
		this.gteSum = gteSum;
		this.position = position;
		
	}
	
	/*public InflationOrder(String quantity, String callSymbol, String putSymbol,
			boolean sell, String productType,  double lteSum,double gteSum) {
		super();
		this.quantity = quantity;
		this.callSymbol = callSymbol;
		this.putSymbol = putSymbol;
		this.sell = sell;
		this.productType = productType;
		this.lteSum = lteSum;
		this.gteSum = gteSum;
		
	}*/
	
	/*public InflationOrder(String quantity, String callSymbol, String putSymbol,
			boolean sell, String productType,  double sum,String operator) {
		super();
		this.quantity = quantity;
		this.callSymbol = callSymbol;
		this.putSymbol = putSymbol;
		this.sell = sell;
		this.productType = productType;
		//this.connect = connect;
	//	this.expiry = expiry;
		this.sum = sum;
		this.operator = operator.toLowerCase();
	}*/

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getCallSymbol() {
		return callSymbol;
	}

	public void setCallSymbol(String callSymbol) {
		this.callSymbol = callSymbol;
	}

	public String getPutSymbol() {
		return putSymbol;
	}

	public void setPutSymbol(String putSymbol) {
		this.putSymbol = putSymbol;
	}

	public boolean isSell() {
		return sell;
	}

	public void setSell(boolean sell) {
		this.sell = sell;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	/*public KiteConnect getConnect() {
		return connect;
	}

	public void setConnect(KiteConnect connect) {
		this.connect = connect;
	}*/

	/*public String getExpiry() {
		return expiry;
	}

	public void setExpiry(String expiry) {
		this.expiry = expiry;
	}*/

	public double getSum() {
		return sum;
	}

	public void setSum(double sum) {
		this.sum = sum;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public double getLteSum() {
		return lteSum;
	}

	public void setLteSum(double lteSum) {
		this.lteSum = lteSum;
	}

	public double getGteSum() {
		return gteSum;
	}

	public void setGteSum(double gteSum) {
		this.gteSum = gteSum;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}
	
	
	
	
}
