package com.cheffybites.common.infrastructure.http;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void propagatesATrustedIdentifierAndClearsLogContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER, "request-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (servletRequest, servletResponse) -> assertThat(MDC.get("correlationId"))
                .isEqualTo("request-123");

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("request-123");
        assertThat(request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE)).isEqualTo("request-123");
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void replacesMalformedOrMissingIdentifiers() {
        assertThat(CorrelationIdFilter.resolve("unsafe value\nInjected: true"))
                .matches("[0-9a-f-]{36}");
        assertThat(CorrelationIdFilter.resolve(null)).matches("[0-9a-f-]{36}");
    }
}
