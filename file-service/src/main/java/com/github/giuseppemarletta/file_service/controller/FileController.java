package com.github.giuseppemarletta.file_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;


import java.util.List;
import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.giuseppemarletta.file_service.service.FileStorageService;
import com.github.giuseppemarletta.file_service.util.JwtUtil;
import com.github.giuseppemarletta.file_service.dto.FileMetadataDto;
import com.github.giuseppemarletta.file_service.model.FileMetadata;


@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService fileStorageService;
    private final JwtUtil jwtUtil;

    // Define endpoints for file upload, download, and metadata retrieval here
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestPart("file") MultipartFile file, 
            @RequestPart("visibility") String visibility, 
            @RequestParam(value = "allowedRoles", required = false) String[] allowedRoles, 
            @RequestHeader("Authorization") String tokenHeader) {
        try {
            String token = tokenHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserIdFromToken(token);

            List<String> rolesList = allowedRoles != null ? Arrays.asList(allowedRoles) : null;
            FileMetadata saved = fileStorageService.uploadFile(file, userId, visibility, rolesList);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/visible")
    public ResponseEntity<List<FileMetadataDto>> getVisibleFiles(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        String userId = jwtUtil.extractUserIdFromToken(jwt);

        List<String> userRoles = jwtUtil.extractUserRolesFromToken(jwt);

        return ResponseEntity.ok(fileStorageService.getVisibleFiles(userId, userRoles));
    }


}
