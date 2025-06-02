# Documentazione Tecnica dell'Ambiente di Sviluppo e Deployment

## 1. Introduzione
Questa documentazione descrive l'ambiente di sviluppo e deployment del sistema, basato su container Docker e servizi AWS emulati localmente tramite LocalStack. L'architettura è stata progettata per garantire un ambiente di sviluppo coerente e riproducibile, facilitando il testing e il deployment dei servizi.

### 1.1 Tecnologie Utilizzate
- **Docker**: Piattaforma di containerizzazione che permette di isolare e distribuire le applicazioni
- **Docker Compose**: Strumento per definire e gestire applicazioni multi-container
- **LocalStack**: Emulatore locale dei servizi AWS, utilizzato per lo sviluppo e il testing
- **AWS DynamoDB**: Database NoSQL per la persistenza dei dati
- **AWS S3**: Servizio di storage per i file
- **AWS IAM**: Gestione delle credenziali e dei permessi

### 1.2 Requisiti di Sistema
- Docker 20.10.x o superiore
- Docker Compose 2.x o superiore
- 4GB di RAM disponibile
- 10GB di spazio su disco
- Porte disponibili: 8080, 4566 (LocalStack)

## 2. Architettura del Sistema

### 2.1 Componenti Principali
Il sistema è strutturato in diversi container e servizi:

- **Auth Service**: Servizio di autenticazione in container Docker
- **File Service**: Servizio di gestione file in container Docker
- **LocalStack**: Emulatore dei servizi AWS
- **DynamoDB**: Database NoSQL per la persistenza dei dati
- **S3**: Storage per i file
- **IAM**: Gestione delle credenziali

### 2.2 Diagramma dell'Architettura
```
[Client] <---> [Auth Service] <---> [DynamoDB]
     |              |                  ^
     |              |                  |
     |              v                  |
     |          [LocalStack] <---------+
     |              |
     |              v
     +--------> [File Service] <---> [S3]
```

## 3. Configurazione dei Container

### 3.1 Dockerfile Auth Service
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### 3.2 Dockerfile File Service
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### 3.3 Docker Compose
```yaml
version: '3.8'

services:
  localstack:
    image: localstack/localstack:latest
    ports:
      - "4566:4566"
    environment:
      - SERVICES=dynamodb,s3
      - DEBUG=1
      - DATA_DIR=/tmp/localstack/data
      - DOCKER_HOST=unix:///var/run/docker.sock
    volumes:
      - "${LOCALSTACK_VOLUME_DIR:-./volume}:/var/lib/localstack"
      - "/var/run/docker.sock:/var/run/docker.sock"

  auth-service:
    build: 
      context: ../auth-service
      dockerfile: Dockerfile
    ports:
      - "8081:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - AMAZON_AWS_ACCESSKEY=test
      - AMAZON_AWS_SECRETKEY=test
      - AMAZON_DYNAMODB_ENDPOINT=http://localstack:4566
    depends_on:
      - localstack

  file-service:
    build:
      context: ../file-service
      dockerfile: Dockerfile
    ports:
      - "8082:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - AMAZON_AWS_ACCESSKEY=test
      - AMAZON_AWS_SECRETKEY=test
      - AMAZON_S3_ENDPOINT=http://localstack:4566
      - AMAZON_DYNAMODB_ENDPOINT=http://localstack:4566
    depends_on:
      - localstack
```

## 4. Configurazione LocalStack

### 4.1 Inizializzazione Servizi
```bash
#!/bin/bash

# Crea il bucket S3
awslocal s3 mb s3://file-service-bucket

# Crea la tabella DynamoDB per gli utenti
awslocal dynamodb create-table \
    --table-name Users \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5

# Crea la tabella DynamoDB per i metadati dei file
awslocal dynamodb create-table \
    --table-name FileMetadata \
    --attribute-definitions AttributeName=fileId,AttributeType=S \
    --key-schema AttributeName=fileId,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5
```

### 4.2 Configurazione IAM
```bash
#!/bin/bash

# Crea un utente IAM per i servizi
awslocal iam create-user --user-name service-user

# Crea una policy per l'accesso a S3 e DynamoDB
awslocal iam create-policy \
    --policy-name service-policy \
    --policy-document '{
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Action": [
                    "s3:*",
                    "dynamodb:*"
                ],
                "Resource": "*"
            }
        ]
    }'

# Attacca la policy all'utente
awslocal iam attach-user-policy \
    --user-name service-user \
    --policy-arn arn:aws:iam::000000000000:policy/service-policy
```

## 5. Gestione dei Container

### 5.1 Avvio dell'Ambiente
```bash
# Build delle immagini
docker-compose build

# Avvio dei servizi
docker-compose up -d

# Verifica dello stato
docker-compose ps
```

### 5.2 Logging e Monitoraggio
```bash
# Visualizzazione log di tutti i servizi
docker-compose logs -f

# Visualizzazione log di un servizio specifico
docker-compose logs -f auth-service
docker-compose logs -f file-service
docker-compose logs -f localstack
```

### 5.3 Gestione dei Volumi
```bash
# Lista dei volumi
docker volume ls

# Pulizia dei volumi
docker-compose down -v
```

## 6. Integrazione con AWS

### 6.1 Configurazione Credenziali
```properties
# application-dev.properties
amazon.aws.accesskey=test
amazon.aws.secretkey=test
amazon.s3.endpoint=http://localstack:4566
amazon.dynamodb.endpoint=http://localstack:4566
```

### 6.2 Configurazione S3
```java
@Configuration
public class S3Config {
    @Value("${amazon.aws.accesskey}")
    private String awsAccessKey;

    @Value("${amazon.aws.secretkey}")
    private String awsSecretKey;

    @Value("${amazon.s3.endpoint}")
    private String endpoint;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
            .region(Region.US_WEST_2)
            .build();
    }
}
```

### 6.3 Configurazione DynamoDB
```java
@Configuration
@EnableDynamoDBRepositories
public class DynamoDBConfig {
    @Value("${amazon.aws.accesskey}")
    private String awsAccessKey;

    @Value("${amazon.aws.secretkey}")
    private String awsSecretKey;

    @Value("${amazon.dynamodb.endpoint}")
    private String endpoint;

    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                endpoint, "us-west-2"))
            .withCredentials(new AWSStaticCredentialsProvider(
                new BasicAWSCredentials(awsAccessKey, awsSecretKey)))
            .build();
    }
}
```

## 7. Best Practices

### 7.1 Sicurezza
- Utilizzo di credenziali di test in ambiente di sviluppo
- Isolamento dei container tramite network Docker
- Limitazione dei permessi IAM al minimo necessario
- Utilizzo di volumi per la persistenza dei dati

### 7.2 Performance
- Configurazione appropriata delle risorse dei container
- Ottimizzazione delle query DynamoDB
- Gestione efficiente dello storage S3
- Monitoraggio delle performance

### 7.3 Manutenibilità
- Documentazione aggiornata
- Script di inizializzazione automatizzati
- Versionamento delle configurazioni
- Backup regolari dei dati

## 8. Troubleshooting

### 8.1 Problemi Comuni
- **Container non si avvia**: Verificare i log e le dipendenze
- **Connessione a LocalStack fallisce**: Verificare la configurazione delle credenziali
- **Errori di permessi**: Verificare le policy IAM
- **Problemi di storage**: Verificare la configurazione dei volumi

### 8.2 Comandi Utili
```bash
# Riavvio di un servizio
docker-compose restart auth-service

# Ricostruzione di un'immagine
docker-compose build auth-service

# Pulizia completa
docker-compose down -v --remove-orphans

# Verifica della rete
docker network inspect docker-compose_default
```

## 9. Deployment

### 9.1 Ambiente di Sviluppo
- Utilizzo di LocalStack per emulare i servizi AWS
- Configurazione di profili Spring per lo sviluppo
- Hot-reload per lo sviluppo locale

### 9.2 Ambiente di Produzione
- Utilizzo di servizi AWS reali
- Configurazione di credenziali sicure
- Monitoraggio e logging avanzati

### 9.3 CI/CD
- Integrazione con pipeline di build
- Test automatizzati
- Deployment automatizzato

## 10. Conclusioni

L'ambiente di sviluppo e deployment basato su Docker e LocalStack fornisce un'infrastruttura robusta e flessibile per lo sviluppo e il testing dei servizi. L'utilizzo di container garantisce la riproducibilità dell'ambiente, mentre LocalStack permette di testare l'integrazione con i servizi AWS in modo locale.

### 10.1 Punti di Forza
- Ambiente di sviluppo riproducibile
- Integrazione semplificata con AWS
- Facilità di deployment
- Isolamento dei servizi

### 10.2 Aree di Miglioramento
- Automazione degli script di inizializzazione
- Miglioramento del monitoring
- Ottimizzazione delle performance
- Implementazione di test end-to-end 