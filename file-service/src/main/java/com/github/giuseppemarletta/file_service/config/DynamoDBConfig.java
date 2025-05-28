package com.github.giuseppemarletta.file_service.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;



import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableDynamoDBRepositories(basePackages = "com.github.giuseppemarletta.file_service.Repository") // Enable DynamoDB repositories
public class DynamoDBConfig {
    
    
    @Value("${amazon.aws.accesskey}")
    private String awsAccessKeyId;

    @Value("${amazon.aws.secretkey}")
    private String awsSecretKey;

    @Value("${amazon.dynamodb.endpoint}")
    private String endpoint;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, "us-west-2"))
                .build();
        createTableIfNotExists(amazonDynamoDB); // Create the table if it doesn't exist
        return amazonDynamoDB;
    }


    private void createTableIfNotExists(AmazonDynamoDB amazonDynamoDB) {
        DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
        Table table = dynamoDB.getTable("FileMetadata");

        try {
            table.describe();
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                CreateTableRequest createTableRequest = new CreateTableRequest()
                        .withTableName("FileMetadata")
                        .withKeySchema(new KeySchemaElement("fileId", KeyType.HASH)) // Partition key
                        .withAttributeDefinitions(new AttributeDefinition("fileId", ScalarAttributeType.S)) // Define the attribute type
                        .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L)); // Set read and write capacity units
                amazonDynamoDB.createTable(createTableRequest);
                //table.waitForActive();
                System.out.println("Table created successfully.");
            } else {
                System.out.println("Failed to create table: " + e.getMessage());
            }
        }
    }

}

