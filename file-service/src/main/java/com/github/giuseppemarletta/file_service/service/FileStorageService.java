package com.github.giuseppemarletta.file_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.giuseppemarletta.file_service.Repository.FileMetadataRepository;
import com.github.giuseppemarletta.file_service.model.FileMetadata;


import java.io.IOException;
import java.util.UUID;

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

    @Value("${amazon.s3.bucket}")
    private String bucketName;

    public FileMetadata uploadFile(MultipartFile file, String userId) throws IOException {
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
                .visibility("private") // Default visibility, can be changed later
                .build();
        // Save metadata to DynamoDB
        return fileMetadataRepository.save(fileMetadata);
    }
}
