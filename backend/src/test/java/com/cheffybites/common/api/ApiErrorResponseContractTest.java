package com.cheffybites.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ApiErrorResponseContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesOnlyTheCanonicalSafeErrorShape() throws Exception {
        ApiErrorResponse response = new ApiErrorResponse(
                "VALIDATION_FAILED",
                "The request contains invalid values.",
                "correlation-123",
                Map.of("field", "invalid")
        );

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains(
                "\"code\":\"VALIDATION_FAILED\"",
                "\"message\":\"The request contains invalid values.\"",
                "\"traceId\":\"correlation-123\"",
                "\"details\":{\"field\":\"invalid\"}"
        );
        assertThat(json).doesNotContain("stackTrace", "SQLException", "password");
    }
}
