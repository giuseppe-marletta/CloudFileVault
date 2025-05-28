package com.github.giuseppemarletta.file_service.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@DynamoDBTable(tableName = "FileMetadata")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
    
    @DynamoDBHashKey(attributeName = "fileId")
    private String fileId;

    @DynamoDBAttribute(attributeName = "ownerId")
    private String ownerId;

    @DynamoDBAttribute(attributeName = "fileName")
    private String fileName;

    @DynamoDBAttribute(attributeName = "fileType")
    private String fileType;

    @DynamoDBAttribute(attributeName = "fileSize")
    private Long fileSize;

    @DynamoDBAttribute(attributeName = "uploadDate")
    private String uploadDate; // ISO 8601 format (e.g., "2023-10-01T12:00:00Z")

    @DynamoDBAttribute(attributeName = "s3Key")
    private String s3Key; // The key used to store the file in S3

    @DynamoDBAttribute(attributeName = "visibility")
    private String visibility; // e.g., "public", "private", "restricted ROLE_BASED"

}