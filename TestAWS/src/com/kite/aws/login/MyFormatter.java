package com.kite.aws.login;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.kite.aws.util.Util;

public class MyFormatter extends Formatter {

	@Override
	public String format(LogRecord logRecord) {
		return Util.getTime() + " " + logRecord.getMessage() + "\n";
	}
}