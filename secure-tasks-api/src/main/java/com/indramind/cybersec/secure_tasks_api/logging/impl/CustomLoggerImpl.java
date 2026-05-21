package com.indramind.cybersec.secure_tasks_api.logging.impl;

import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.Arrays;

import com.indramind.cybersec.secure_tasks_api.logging.CustomLogger;

// Class to centralize logger to make future changes easy to implement
@SuppressFBWarnings(
    value = "CRLF_INJECTION_LOGS",
    justification = "All log inputs are sanitized in CustomLoggerImpl" // because false positives were appearing even though CRLF chars are escaped
)
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
        log.error(message, args, kv("Exception Class", ex.getClass().getSimpleName()));
    }

    @Override
    public void debug(String message, Object... args) {
        args = cleanArgs(args);
        message = cleanString(message);
        log.debug(message, args);
    }

    public String cleanString(String uncleanedText){
        if (uncleanedText == null) return null;
        return uncleanedText.replaceAll("[\r\n]", "_");
    }

    public Object cleanObject(Object obj){
        if (obj instanceof String s) {
            return cleanString(s);
        }
        if (obj == null) return null;
        return cleanString(obj.toString()); // para evitar log falsification
    }

    public Object[] cleanArgs(Object[] args){
        if (args==null) return new Object[0];

        return Arrays.stream(args).map(this::cleanObject).toArray();
    }
}
