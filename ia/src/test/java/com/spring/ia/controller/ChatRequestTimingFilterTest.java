package com.spring.ia.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;

class ChatRequestTimingFilterTest {

    @Test
    void chatRequestAddsRequestIdAndClearsMdcAfterCompletion() throws Exception {
        ChatRequestTimingFilter filter = new ChatRequestTimingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/chat");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            assertNotNull(MDC.get("requestId"));
            ((MockHttpServletResponse) res).setStatus(201);
        };

        filter.doFilter(request, response, chain);

        assertNotNull(response.getHeader("X-Request-Id"));
        assertEquals(201, response.getStatus());
        assertNull(MDC.get("requestId"));
    }

    @Test
    void nonChatRequestDoesNotReceiveChatRequestId() throws Exception {
        ChatRequestTimingFilter filter = new ChatRequestTimingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> { });

        assertNull(response.getHeader("X-Request-Id"));
    }
}
