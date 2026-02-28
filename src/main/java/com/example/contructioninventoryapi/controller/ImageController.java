package com.example.contructioninventoryapi.controller;

import com.example.contructioninventoryapi.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final FileStorageService fileStorageService;

    public ImageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    // 1. UPLOAD (Create)
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);
        String fileUrl = getFileUrl(fileName);

        Map<String, String> response = new HashMap<>();
        response.put("fileName", fileName);
        response.put("url", fileUrl);

        return ResponseEntity.ok(response);
    }

    // 2. VIEW (Read)
    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        String contentType = determineContentType(request, resource);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // 3. DELETE
    @DeleteMapping("/{fileName:.+}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileName) {
        fileStorageService.deleteFile(fileName);
        return ResponseEntity.ok().body(Map.of("message", "File deleted successfully"));
    }

    // 4. EDIT (Update)
    // Deletes the old file and uploads the new one
    @PutMapping("/{oldFileName:.+}")
    public ResponseEntity<Map<String, String>> updateFile(
            @PathVariable String oldFileName,
            @RequestParam("file") MultipartFile newFile) {

        // 1. Delete the old file
        fileStorageService.deleteFile(oldFileName);

        // 2. Store the new file
        String newFileName = fileStorageService.storeFile(newFile);
        String fileUrl = getFileUrl(newFileName);

        Map<String, String> response = new HashMap<>();
        response.put("message", "File updated successfully");
        response.put("oldFileName", oldFileName);
        response.put("newFileName", newFileName);
        response.put("url", fileUrl);

        return ResponseEntity.ok(response);
    }

    // --- Helper Methods ---
    private String getFileUrl(String fileName) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/")
                .path(fileName)
                .toUriString();
    }

    private String determineContentType(HttpServletRequest request, Resource resource) {
        try {
            String type = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            return (type != null) ? type : "application/octet-stream";
        } catch (IOException ex) {
            return "application/octet-stream";
        }
    }
}