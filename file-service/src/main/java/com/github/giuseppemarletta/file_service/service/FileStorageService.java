package com.github.giuseppemarletta.file_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.giuseppemarletta.file_service.Repository.FileMetadataRepository;
import com.github.giuseppemarletta.file_service.dto.FileMetadataDto;
import com.github.giuseppemarletta.file_service.model.FileMetadata;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    
    private final S3Client s3Client;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${amazon.s3.bucket.name}")
    private String bucketName;

    public FileMetadata uploadFile(MultipartFile file, String userId, String visibility, List<String> allowedRoles) throws IOException {
        String key = UUID.randomUUID().toString() + "_" + file.getOriginalFilename(); // Generate a unique key for the file

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        // Create FileMetadata object
        FileMetadata fileMetadata = FileMetadata.builder()
                .fileId(UUID.randomUUID().toString())
                .ownerId(userId)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadDate(java.time.Instant.now().toString()) // Use ISO 8601 format
                .s3Key(key)
                .visibility(visibility.toUpperCase()) // Default visibility, can be changed later
                .build();

                if("ROLE_BASED".equalsIgnoreCase(visibility)) {
                    fileMetadata.setAllowedRoles(allowedRoles != null ? allowedRoles : new ArrayList<>());
                }
        // Save metadata to DynamoDB
        return fileMetadataRepository.save(fileMetadata);
    }

    public List<FileMetadataDto> getVisibleFiles(String userId, List<String> userRoles) {
        Iterable<FileMetadata> allFiles = fileMetadataRepository.findAll();

        return StreamSupport.stream(allFiles.spliterator(), false)
                .filter(file -> {
                    switch(file.getVisibility()) {
                        case "PUBLIC":
                            return true;
                        case "PRIVATE":
                            return file.getOwnerId().equals(userId);
                        case "ROLE_BASED":
                            return file.getAllowedRoles() != null && userRoles.stream().anyMatch(file.getAllowedRoles()::contains);
                        default:
                            return false;
                    }
                })
                .map(file -> new FileMetadataDto(
                    file.getFileId(),
                    file.getFileName(),
                    file.getFileType(),
                    file.getFileSize(),
                    file.getUploadDate(),
                    file.getVisibility()
                ))
                .collect(Collectors.toList());
                }
}
