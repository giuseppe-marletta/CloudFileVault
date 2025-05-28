package com.github.giuseppemarletta.file_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.github.giuseppemarletta.file_service.service.FileStorageService;
import com.github.giuseppemarletta.file_service.util.JwtUtil;
import com.github.giuseppemarletta.file_service.model.FileMetadata;


@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService fileStorageService;
    private final JwtUtil jwtUtil;

    // Define endpoints for file upload, download, and metadata retrieval here
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestPart("file") MultipartFile file, @RequestHeader("Authorization") String header) {
        try {
            String token = header.replace("Bearer ", "");
            String userId = jwtUtil.extractUserIdFromToken(token);

            FileMetadata saved  = fileStorageService.uploadFile(file, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
            }
    }

}
