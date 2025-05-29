#!/bin/bash

# Nome del bucket richiesto dal codice
BUCKET_NAME="cloudfileaivault"

# Endpoint LocalStack
ENDPOINT_URL="http://localhost:4566"

# Crea il bucket solo se non esiste già
if ! aws --endpoint-url=$ENDPOINT_URL s3 ls | grep -q $BUCKET_NAME; then
  echo "Creo il bucket $BUCKET_NAME su LocalStack..."
  aws --endpoint-url=$ENDPOINT_URL s3 mb s3://$BUCKET_NAME
else
  echo "Il bucket $BUCKET_NAME esiste già."
fi 