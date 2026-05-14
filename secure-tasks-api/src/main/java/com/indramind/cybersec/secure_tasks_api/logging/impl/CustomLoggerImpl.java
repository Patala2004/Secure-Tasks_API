package com.indramind.cybersec.secure_tasks_api.logging.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.Arrays;

import com.indramind.cybersec.secure_tasks_api.logging.CustomLogger;

// Class to centralize logger to make future changes easy to implement
public class CustomLoggerImpl implements CustomLogger{

	private Logger log = null;
	
	public CustomLoggerImpl(Class<?> loggerClass){
		this.log = LoggerFactory.getLogger(loggerClass);
	}

    @Override
    public void info(String message, Object... args) {
        args = cleanArgs(args);
        message = cleanString(message);
        log.info(message, args);
    }

    @Override
    public void warn(String message, Object... args) {
        args = cleanArgs(args);
        message = cleanString(message);
        log.warn(message, args);
    }

    @Override
    public void error(String message, Throwable ex, Object... args) {
        args = cleanArgs(args);
        message = cleanString(message);
        log.error(message, args, kv("Exception Class", ex.getClass().getSimpleName()), kv("Exception Message", ex.getMessage()), kv("Exception Stack trace", ex.getStackTrace()));
    }

    @Override
    public void debug(String message, Object... args) {
        args = cleanArgs(args);
        message = cleanString(message);
        log.debug(message, args);
    }

    public String cleanString(String uncleanedText){
        return uncleanedText.replace("\n", "_").replace("\r", "_");
    }

    public Object cleanObject(Object uncleanedText){
        if (uncleanedText == null) return null;

        return cleanString(uncleanedText.toString()); // para evitar log falsification
    }

    public Object[] cleanArgs(Object[] args){
        if (args==null) return null;

        return Arrays.stream(args).map(this::cleanObject).toArray();
    }
}
