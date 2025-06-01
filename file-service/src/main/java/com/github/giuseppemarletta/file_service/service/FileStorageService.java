package com.github.giuseppemarletta.file_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.github.giuseppemarletta.file_service.Repository.FileMetadataRepository;
import com.github.giuseppemarletta.file_service.dto.FileMetadataDto;
import com.github.giuseppemarletta.file_service.model.FileMetadata;


import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import java.time.Duration;
import java.net.URI;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    
    private final S3Client s3Client;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${amazon.s3.bucket.name}")
    private String bucketName;

    @Value("${amazon.s3.endpoint}")
    private String endpoint;

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

    public String getDownloadUrl(String fileId, String userId, List<String> userRoles) {
        FileMetadata file = fileMetadataRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found"));

        switch(file.getVisibility().toUpperCase()) {
            case "PUBLIC":
                break;
            case "PRIVATE":
                if(!file.getOwnerId().equals(userId)) {
                    throw new RuntimeException("You are not the owner of this file");
                }
                break;
            case "ROLE_BASED":
                if(file.getAllowedRoles() == null || !file.getAllowedRoles().containsAll(userRoles)) {
                    throw new RuntimeException("You do not have permission to download this file");
                }
                break;
            default:
                throw new RuntimeException("Invalid file visibility");
        }

        return generatePresignedUrl(file.getS3Key());
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

    /**
     * Genera un URL presigned per il download di un file da S3.
     * Un URL presigned è un URL temporaneo che permette di scaricare un file senza necessità di credenziali AWS.
     * 
     * @param keyName Il nome/chiave del file in S3
     * @return L'URL presigned come stringa
     */
    private String generatePresignedUrl(String keyName) {
        // Crea un S3Presigner usando la stessa regione e endpoint del client S3 esistente
        S3Presigner presigner = S3Presigner.builder()
            .region(s3Client.serviceClientConfiguration().region())
            .endpointOverride(URI.create(endpoint))
            .build();

        // Crea una richiesta per ottenere l'oggetto da S3
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)    // Il nome del bucket dove è salvato il file
            .key(keyName)         // Il nome/chiave del file in S3
            .build();

        // Genera l'URL presigned con una validità di 5 minuti
        String presignedUrl = presigner.presignGetObject(builder -> builder
            .getObjectRequest(getObjectRequest)    // Usa la richiesta creata sopra
            .signatureDuration(Duration.ofMinutes(5))  // L'URL sarà valido per 5 minuti
            .build())
            .url()
            .toString();

        // Sostituisci l'host con localhost e aggiungi il bucket nel path
        return presignedUrl.replace("localstack", "localhost")
                         .replace("/" + keyName, "/" + bucketName + "/" + keyName);
    }
}
