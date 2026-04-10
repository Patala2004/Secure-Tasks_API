package com.indramind.cybersec.secure_tasks_api.exceptions;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        String error
) {}