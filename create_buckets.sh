#!/bin/bash

# Wait for LocalStack to be ready
echo "Waiting for LocalStack to be ready..."
while ! curl -s http://localhost:4566/_localstack/health | grep -q '"s3": "\(running\|available\)"'; do
    sleep 1
done

# Create the S3 bucket
echo "Creating S3 bucket..."
aws --endpoint-url=http://localhost:4566 s3 mb s3://cloudfileaivault

echo "Bootstrap completed!" 