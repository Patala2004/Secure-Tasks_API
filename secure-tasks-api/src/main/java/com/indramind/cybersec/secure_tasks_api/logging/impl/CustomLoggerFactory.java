package com.indramind.cybersec.secure_tasks_api.logging.impl;

import com.indramind.cybersec.secure_tasks_api.logging.CustomLogger;

public class CustomLoggerFactory{
	public static CustomLogger getLogger(Class<?> clazz){
		return new CustomLoggerImpl(clazz);
	};
}
