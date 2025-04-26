package com.github.giuseppemarletta.auth_service.config;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Bean;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
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
@EnableDynamoDBRepositories(basePackages = "com.github.giuseppemarletta.auth_service.Repository") // Enable DynamoDB repositories
public class DynamoDBConfig {
    
    @Value("${aws.dynamodb.accessKeyId}")
    private String awsAccessKeyId;

    @Value("${aws.dynamodb.secretKey}")
    private String awsSecretKey;

    @Value("${aws.dynamodb.region}")
    private String Region;

    @Value("${aws.dynamodb.endpoint}")
    private String endpoint;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
                //.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("dummy-access-key", "dummy-secret-key")))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, Region)) // Set the endpoint and region (local)
                //.withRegion(Region)  // Uncomment this line if you want to use the region in aws  
                .build();

        createTableIfNotExists(amazonDynamoDB); // Create the table if it doesn't exist
        return amazonDynamoDB;
    }

    @Bean
    @Primary 
    public DynamoDBMapper dynamoDBMapper(AmazonDynamoDB amazonDynamoDB) { // Create a DynamoDBMapper bean to interact with DynamoDB
        return new DynamoDBMapper(amazonDynamoDB);
    }


    private void createTableIfNotExists(AmazonDynamoDB amazonDynamoDB) {
        DynamoDB dynamoDB = new DynamoDB(amazonDynamoDB);
        Table table = dynamoDB.getTable("users");

        try {
            table.describe();
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                CreateTableRequest createTableRequest = new CreateTableRequest()
                        .withTableName("users")
                        .withKeySchema(new KeySchemaElement("id", KeyType.HASH)) // Partition key
                        .withAttributeDefinitions(new AttributeDefinition("id", ScalarAttributeType.S)) // Define the attribute type
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
