package com.cheffybites.common.infrastructure.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cheffy.foundation")
public record FoundationProperties(
        List<String> allowedOrigins,
        boolean telemetryExportEnabled
) {

    public FoundationProperties {
        allowedOrigins = allowedOrigins == null ? List.of() : List.copyOf(allowedOrigins);
    }
}
