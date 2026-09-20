package com.ekosrekja.ekosrekjafullstack.upload;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StoredUploadTests {
    @Test
    void unconfiguredCloudinaryPersistsUploadAndReturnsReadableUrl() throws Exception {
        var repository = mock(StoredUploadRepository.class);
        var service = new CloudinaryUploadService("", "", "", "", repository);
        byte[] bytes = {1, 2, 3, 4};
        var file = new MockMultipartFile("file", "cover.jpg", "image/jpeg", bytes);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        String url;
        try {
            url = service.upload(file, "news", "image/");
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
        assertTrue(url.startsWith("http://localhost/api/media/news/"));
        String path = url.substring("http://localhost/api/media/".length());
        verify(repository).save(eq(path), eq("image/jpeg"), aryEq(bytes));
        when(repository.find(path)).thenReturn(Optional.of(new StoredUploadRepository.Upload("image/jpeg", bytes)));
        MockMvcBuilders.standaloneSetup(new StoredUploadController(repository)).build()
                .perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"))
                .andExpect(content().bytes(bytes));
    }

    @Test
    void missingUploadReturns404() throws Exception {
        var repository = mock(StoredUploadRepository.class);
        when(repository.find(anyString())).thenReturn(Optional.empty());
        MockMvcBuilders.standaloneSetup(new StoredUploadController(repository)).build()
                .perform(get("/api/media/news/missing.jpg"))
                .andExpect(status().isNotFound());
    }
}
