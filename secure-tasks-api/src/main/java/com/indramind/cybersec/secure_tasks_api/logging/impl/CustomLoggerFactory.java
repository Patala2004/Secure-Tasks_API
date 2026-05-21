package com.indramind.cybersec.secure_tasks_api.logging.impl;

import com.indramind.cybersec.secure_tasks_api.logging.CustomLogger;

public class CustomLoggerFactory{
	private CustomLoggerFactory(){} // private constructor just to hide implicit public one

	public static CustomLogger getLogger(Class<?> clazz){
		return new CustomLoggerImpl(clazz);
	}
}
