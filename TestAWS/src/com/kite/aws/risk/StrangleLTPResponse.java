package com.kite.aws.risk;

import java.util.Map;

public class StrangleLTPResponse {
	private String code;
	private String messge;
	private Map<String, String> ltpMap;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessge() {
		return messge;
	}

	public void setMessge(String messge) {
		this.messge = messge;
	}

	public Map<String, String> getLtpMap() {
		return ltpMap;
	}

	public void setLtpMap(Map<String, String> ltpMap) {
		this.ltpMap = ltpMap;
	}

}
