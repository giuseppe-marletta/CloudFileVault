package com.github.giuseppemarletta.file_service.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
public class S3config {
    
    @Value("${amazon.aws.accesskey}")
    private String awsAccessKeyId;

    @Value("${amazon.aws.secretkey}")
    private String awsSecretKey;

    @Value("${amazon.s3.endpoint}")
    private String endpoint;

    @Value("${amazon.s3.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region)) 
                .credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(awsAccessKeyId, awsSecretKey))
                );
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        
        return builder.build();
    }

}
