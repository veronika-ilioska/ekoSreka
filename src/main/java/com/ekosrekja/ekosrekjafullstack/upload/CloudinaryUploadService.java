package com.ekosrekja.ekosrekjafullstack.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class CloudinaryUploadService {
    private final Cloudinary cloudinary;
    private final StoredUploadRepository storedUploads;

    public CloudinaryUploadService(
            @Value("${cloudinary.url:}") String cloudinaryUrl,
            @Value("${cloudinary.cloud-name:}") String cloudName,
            @Value("${cloudinary.api-key:}") String apiKey,
            @Value("${cloudinary.api-secret:}") String apiSecret,
            StoredUploadRepository storedUploads) {
        this.storedUploads = storedUploads;

        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            this.cloudinary = new Cloudinary(cloudinaryUrl);
            return;
        }

        if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)) {
            this.cloudinary = null;
            return;
        }

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    public String upload(MultipartFile file, String folder, String expectedContentTypePrefix) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload file is required");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith(expectedContentTypePrefix)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type");
        }

        if (cloudinary == null) {
            return storePersistentUpload(file, folder);
        }

        String resourceType = expectedContentTypePrefix.startsWith("video/") ? "video" : "image";

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "ekosrekja/" + folder,
                    "resource_type", resourceType,
                    "overwrite", false,
                    "unique_filename", true));
            Object secureUrl = result.get("secure_url");
            if (secureUrl == null) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Cloudinary did not return a secure URL");
            }
            return secureUrl.toString();
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not upload file to Cloudinary",
                    ex);
        }
    }

    private String storePersistentUpload(MultipartFile file, String folder) {
        try {
            String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalName.length() - 1) {
                extension = originalName.substring(dotIndex).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9.]", "");
            }

            String filename = UUID.randomUUID() + extension;
            storedUploads.save(folder + "/" + filename, file.getContentType(), file.getBytes());
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/media/")
                    .path(folder)
                    .path("/")
                    .path(filename)
                    .toUriString();
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not store uploaded file",
                    ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
