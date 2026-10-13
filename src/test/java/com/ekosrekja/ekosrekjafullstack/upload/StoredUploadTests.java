package com.ekosrekja.ekosrekjafullstack.upload;

import java.util.Optional;
import com.ekosrekja.ekosrekjafullstack.fun.controllers.ListingController;
import com.ekosrekja.ekosrekjafullstack.fun.entity.Photo;
import com.ekosrekja.ekosrekjafullstack.fun.repos.PhotoRepository;
import com.ekosrekja.ekosrekjafullstack.fun.repos.VideoRepository;
import com.ekosrekja.ekosrekjafullstack.fun.repos.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
    void photoFilesAndPhotoDetailsResolveToTheirOwnControllers() throws Exception {
        var uploads = mock(StoredUploadRepository.class);
        var photos = mock(PhotoRepository.class);
        String filename = "e6607773-7510-40c7-a37c-fa70a6234c3c.png";
        byte[] bytes = {1, 2, 3, 4};
        when(uploads.find("photos/" + filename))
                .thenReturn(Optional.of(new StoredUploadRepository.Upload("image/png", bytes)));
        var photo = new Photo();
        photo.setId(42L);
        photo.setTitle("Uploaded photo");
        when(photos.findById(42L)).thenReturn(Optional.of(photo));
        var mvc = MockMvcBuilders.standaloneSetup(
                new StoredUploadController(uploads),
                new ListingController(photos, mock(VideoRepository.class), mock(GameRepository.class))).build();
        mvc.perform(get("/api/media/photos/" + filename))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(bytes));
        mvc.perform(get("/api/media/photos/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(42));
        mvc.perform(get("/api/media/photos/missing.png"))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"news", "photos"})
    void unconfiguredCloudinaryPersistsUploadAndReturnsReadableUrl(String folder) throws Exception {
        var repository = mock(StoredUploadRepository.class);
        var service = new CloudinaryUploadService("", "", "", "", repository);
        byte[] bytes = {1, 2, 3, 4};
        var file = new MockMultipartFile("file", "cover.jpg", "image/jpeg", bytes);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest()));
        String url;
        try {
            url = service.upload(file, folder, "image/");
        } finally {
            RequestContextHolder.resetRequestAttributes();
        }
        assertTrue(url.startsWith("http://localhost/api/media/" + folder + "/"));
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
