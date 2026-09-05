package com.cheffybites.common.api;

import java.util.Map;

public record ApiErrorResponse(
        String code,
        String message,
        String traceId,
        Map<String, Object> details
) {

    public ApiErrorResponse {
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}
