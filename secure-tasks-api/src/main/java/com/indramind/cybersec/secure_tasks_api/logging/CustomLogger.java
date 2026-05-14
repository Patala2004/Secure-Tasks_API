package com.indramind.cybersec.secure_tasks_api.logging;

public interface CustomLogger {
	void info(String message, Object... args);

    void warn(String message, Object... args);

    void error(String message, Throwable ex, Object... args);

    void debug(String message, Object... args);
}
