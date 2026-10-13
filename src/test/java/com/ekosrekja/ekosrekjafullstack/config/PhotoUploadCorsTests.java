package com.ekosrekja.ekosrekjafullstack.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.cors.DefaultCorsProcessor;

import static org.junit.jupiter.api.Assertions.*;

class PhotoUploadCorsTests {
    @Test
    void allowsPhotoUploadPreflightFromSecondVitePort() throws Exception {
        var request = new MockHttpServletRequest("OPTIONS", "/api/admin/photos");
        request.addHeader("Origin", "http://localhost:5174");
        request.addHeader("Access-Control-Request-Method", "POST");
        request.addHeader("Access-Control-Request-Headers", "x-user-id,content-type");
        var response = new MockHttpServletResponse();
        var config = new SecurityConfig().corsConfigurationSource().getCorsConfiguration(request);
        assertTrue(new DefaultCorsProcessor().processRequest(config, request, response));
        assertEquals("http://localhost:5174", response.getHeader("Access-Control-Allow-Origin"));
        assertEquals(200, response.getStatus());
    }
}
