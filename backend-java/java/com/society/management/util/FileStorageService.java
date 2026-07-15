package com.society.management.util;

import com.society.management.config.AppProperties;
import com.society.management.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final AppProperties props;

    public String store(MultipartFile file, String subDir, Set<String> allowedContentTypes) {
        if (file == null || file.isEmpty()) throw ApiException.badRequest("File is empty");
        if (allowedContentTypes != null && !allowedContentTypes.contains(file.getContentType()))
            throw ApiException.badRequest("Unsupported file type: " + file.getContentType());

        try {
            Path base = Paths.get(props.getUpload().getBaseDir(), subDir).toAbsolutePath();
            Files.createDirectories(base);
            String ext = getExtension(file.getOriginalFilename());
            String name = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            Path target = base.resolve(name);
            file.transferTo(target.toFile());
            return target.toString();
        } catch (IOException e) {
            throw ApiException.badRequest("Unable to store file: " + e.getMessage());
        }
    }

    public byte[] read(String absolutePath) {
        try { return Files.readAllBytes(Paths.get(absolutePath)); }
        catch (IOException e) { throw ApiException.notFound("File not found"); }
    }

    private String getExtension(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return i < 0 ? "" : name.substring(i + 1).toLowerCase();
    }
}
