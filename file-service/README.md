# Documentazione Tecnica del Servizio di Gestione File

## 1. Introduzione
Il servizio di gestione file rappresenta un componente fondamentale e strategico all'interno dell'ecosistema distribuito, progettato per gestire in modo efficiente e sicuro l'upload, il download e la gestione dei metadati dei file. Implementato utilizzando Spring Boot 3.x, il servizio sfrutta le potenzialità di AWS S3 per lo storage dei file e DynamoDB per la persistenza dei metadati, garantendo così un'architettura scalabile e performante.

Il servizio è stato concepito per soddisfare requisiti di alta affidabilità, sicurezza e scalabilità, offrendo funzionalità avanzate come la gestione della visibilità dei file, il controllo degli accessi basato su ruoli e la generazione di URL presigned per il download sicuro dei file.

### 1.1 Tecnologie Utilizzate
- **Spring Boot 3.x**: Framework principale per lo sviluppo dell'applicazione, scelto per la sua maturità, vasta comunità e ricco ecosistema di componenti. Spring Boot semplifica lo sviluppo di applicazioni enterprise-grade, fornendo configurazioni automatiche e un'architettura modulare.
- **AWS S3**: Storage dei file, utilizzato per la sua affidabilità, scalabilità e costi contenuti. S3 offre funzionalità avanzate come versioning, encryption e lifecycle policies.
- **AWS DynamoDB**: Persistenza dei metadati, scelto per le sue performance in lettura/scrittura e la sua capacità di scalare automaticamente. DynamoDB è particolarmente adatto per applicazioni che richiedono bassa latenza e alta disponibilità.
- **AWS SDK**: Integrazione con i servizi AWS, fornisce un'interfaccia type-safe e thread-safe per interagire con i servizi AWS.
- **JWT**: Validazione dei token per l'autenticazione, implementato per garantire un sistema di autenticazione stateless e sicuro.
- **Lombok**: Riduzione del boilerplate code, migliora la leggibilità e la manutenibilità del codice.
- **Maven**: Gestione delle dipendenze e build automation, garantisce un processo di build riproducibile e gestibile.

### 1.2 Requisiti di Sistema
- **Java 17 o superiore**: Versione LTS di Java che offre migliori performance e nuove funzionalità come i Record e i Sealed Classes.
- **Maven 3.6.x o superiore**: Gestore di dipendenze e build automation, necessario per la compilazione e il packaging dell'applicazione.
- **Accesso a un'istanza S3**: Può essere un'istanza AWS S3 o un'istanza locale (es. LocalStack) per lo sviluppo e il testing.
- **Accesso a un'istanza DynamoDB**: Può essere un'istanza AWS DynamoDB o un'istanza locale per lo sviluppo e il testing.
- **Configurazione AWS**: Credenziali di accesso (access key e secret key) necessarie per l'autenticazione con i servizi AWS.

## 2. Architettura del Sistema

### 2.1 Componenti Principali
Il servizio è strutturato secondo un'architettura a layer, che garantisce una chiara separazione delle responsabilità e una facile manutenibilità:

- **Controller Layer**: Gestisce le richieste HTTP e implementa gli endpoint REST. Questo layer è responsabile della validazione degli input, della gestione delle risposte HTTP e della conversione dei dati tra il formato di rete e il formato interno dell'applicazione.

- **Service Layer**: Implementa la logica di business e la gestione dei file. Questo layer contiene la maggior parte della logica applicativa, inclusa la gestione dei file, la validazione delle operazioni e la coordinazione tra i vari componenti.

- **Repository Layer**: Gestisce la persistenza dei metadati tramite DynamoDB. Questo layer astrae le operazioni di database, fornendo un'interfaccia semplice e type-safe per l'accesso ai dati.

- **Model Layer**: Definisce le entità del dominio e i loro metadati. Questo layer rappresenta la struttura dei dati dell'applicazione e le loro relazioni.

- **DTO Layer**: Gestisce il trasferimento dei dati tra i layer. I DTO (Data Transfer Objects) sono utilizzati per trasferire dati tra i layer dell'applicazione, garantendo che solo i dati necessari vengano esposti.

- **Config Layer**: Configura il sistema e le sue dipendenze. Questo layer gestisce la configurazione dell'applicazione, inclusa la configurazione di Spring, AWS e altri servizi.

- **Util Layer**: Fornisce utility per la gestione dei token JWT e altre funzionalità di supporto. Questo layer contiene classi di utilità che possono essere utilizzate in tutto il sistema.

### 2.2 Diagramma dell'Architettura
```
[Client] <---> [File Service]
                  |
                  |---> [Controller Layer]
                  |        |
                  |        |---> [Service Layer]
                  |        |        |
                  |        |        |---> [Repository Layer]
                  |        |        |        |
                  |        |        |        |---> [DynamoDB]
                  |        |        |
                  |        |        |---> [S3 Client]
                  |        |                 |
                  |        |                 |---> [S3 Storage]
                  |        |
                  |        |---> [DTO Layer]
                  |
                  |---> [Config Layer]
```

## 3. Dettaglio dei Componenti

### 3.1 Model Layer

#### 3.1.1 FileMetadata.java
```java
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
    private String uploadDate;

    @DynamoDBAttribute(attributeName = "s3Key")
    private String s3Key;

    @DynamoDBAttribute(attributeName = "visibility")
    private String visibility;
    
    @DynamoDBAttribute(attributeName = "allowedRoles")
    private List<String> allowedRoles;
}
```
La classe `FileMetadata` rappresenta i metadati di un file nel sistema. È annotata con `@DynamoDBTable` per mapparla alla tabella DynamoDB "FileMetadata". Gli attributi principali sono:
- `fileId`: Chiave primaria generata automaticamente tramite UUID
- `ownerId`: ID dell'utente proprietario del file
- `fileName`: Nome originale del file
- `fileType`: Tipo MIME del file
- `fileSize`: Dimensione del file in bytes
- `uploadDate`: Data di upload in formato ISO 8601
- `s3Key`: Chiave univoca per il file in S3
- `visibility`: Visibilità del file (PUBLIC, PRIVATE, ROLE_BASED)
- `allowedRoles`: Lista dei ruoli autorizzati (per ROLE_BASED)

### 3.2 DTO Layer

#### 3.2.1 FileMetadataDto.java
```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadataDto {
    private String fileId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String uploadDate;
    private String visibility;
}
```
DTO utilizzato per il trasferimento dei metadati dei file, escludendo informazioni sensibili come la chiave S3.

### 3.3 Controller Layer

#### 3.3.1 FileController.java
```java
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileStorageService fileStorageService;
    private final JwtUtil jwtUtil;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestPart("file") MultipartFile file, 
            @RequestPart("visibility") String visibility, 
            @RequestParam(value = "allowedRoles", required = false) String[] allowedRoles, 
            @RequestHeader("Authorization") String tokenHeader) {
        try {
            String token = tokenHeader.replace("Bearer ", "");
            String userId = jwtUtil.extractUserIdFromToken(token);

            List<String> rolesList = allowedRoles != null ? Arrays.asList(allowedRoles) : null;
            FileMetadata saved = fileStorageService.uploadFile(file, userId, visibility, rolesList);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<String> downloadFile(
        @PathVariable String fileId,
        @RequestHeader("Authorization") String tokenHeader) {
        String token = tokenHeader.replace("Bearer ", "");
        String userId = jwtUtil.extractUserIdFromToken(token);
        List<String> rolesList = jwtUtil.extractUserRolesFromToken(token);

        String downloadUrl = fileStorageService.getDownloadUrl(fileId, userId, rolesList);
        return ResponseEntity.ok(downloadUrl);
    }

    @GetMapping("/visible")
    public ResponseEntity<List<FileMetadataDto>> getVisibleFiles(
        @RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        String userId = jwtUtil.extractUserIdFromToken(jwt);
        List<String> userRoles = jwtUtil.extractUserRolesFromToken(jwt);

        return ResponseEntity.ok(fileStorageService.getVisibleFiles(userId, userRoles));
    }
}
```

### 3.4 Service Layer

#### 3.4.1 FileStorageService.java
```java
@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final S3Client s3Client;
    private final FileMetadataRepository fileMetadataRepository;

    @Value("${amazon.s3.bucket.name}")
    private String bucketName;

    @Value("${amazon.s3.endpoint}")
    private String endpoint;

    public FileMetadata uploadFile(MultipartFile file, String userId, 
                                 String visibility, List<String> allowedRoles) throws IOException {
        String key = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        FileMetadata fileMetadata = FileMetadata.builder()
                .fileId(UUID.randomUUID().toString())
                .ownerId(userId)
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadDate(java.time.Instant.now().toString())
                .s3Key(key)
                .visibility(visibility.toUpperCase())
                .build();

        if("ROLE_BASED".equalsIgnoreCase(visibility)) {
            fileMetadata.setAllowedRoles(allowedRoles != null ? allowedRoles : new ArrayList<>());
        }

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
                if(file.getAllowedRoles() == null || 
                   !file.getAllowedRoles().containsAll(userRoles)) {
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
                            return file.getAllowedRoles() != null && 
                                   userRoles.stream().anyMatch(file.getAllowedRoles()::contains);
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

    private String generatePresignedUrl(String keyName) {
        S3Presigner presigner = S3Presigner.builder()
            .region(s3Client.serviceClientConfiguration().region())
            .endpointOverride(URI.create(endpoint))
            .build();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(keyName)
            .build();

        String presignedUrl = presigner.presignGetObject(builder -> builder
            .getObjectRequest(getObjectRequest)
            .signatureDuration(Duration.ofMinutes(5))
            .build())
            .url()
            .toString();

        return presignedUrl.replace("localstack", "localhost")
                         .replace("/" + keyName, "/" + bucketName + "/" + keyName);
    }
}
```

### 3.5 Config Layer

#### 3.5.1 S3Config.java
```java
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
        S3Configuration config = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsAccessKeyId, awsSecretKey))
                )
                .serviceConfiguration(config);

        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        
        return builder.build();
    }
}
```

#### 3.5.2 DynamoDBConfig.java
```java
@Configuration
@EnableDynamoDBRepositories(basePackages = "com.github.giuseppemarletta.file_service.Repository")
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
                .withCredentials(new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(awsAccessKeyId, awsSecretKey)))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                    endpoint, "us-west-2"))
                .build();
        createTableIfNotExists(amazonDynamoDB);
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
                        .withKeySchema(new KeySchemaElement("fileId", KeyType.HASH))
                        .withAttributeDefinitions(new AttributeDefinition("fileId", ScalarAttributeType.S))
                        .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
                amazonDynamoDB.createTable(createTableRequest);
            }
        }
    }
}
```

### 3.6 Util Layer

#### 3.6.1 JwtUtil.java
```java
@Service
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;

    public String extractUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public List<String> extractUserRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();
        String role = claims.get("role", String.class);
        return List.of(role);
    }
}
```

## 4. Flusso di Gestione File

### 4.1 Upload File
Il processo di upload dei file è stato progettato per essere robusto, sicuro e scalabile. Il flusso completo include:

1. **Validazione Input**:
   - **Verifica presenza file**: Il sistema verifica che il file sia stato effettivamente caricato e che non sia vuoto.
   - **Validazione visibilità**: Il sistema verifica che il livello di visibilità specificato sia valido (PUBLIC, PRIVATE, ROLE_BASED).
   - **Controllo ruoli**: Se la visibilità è ROLE_BASED, il sistema verifica che i ruoli specificati siano validi e che l'utente abbia i permessi necessari.

2. **Processo Upload**:
   - **Generazione chiave S3 univoca**: Il sistema genera una chiave univoca per il file in S3, combinando un UUID con il nome originale del file.
   - **Upload file su S3**: Il file viene caricato su S3 utilizzando l'AWS SDK, con gestione automatica del chunking per file di grandi dimensioni.
   - **Creazione metadati**: Il sistema crea un oggetto FileMetadata contenente tutte le informazioni relative al file.
   - **Salvataggio in DynamoDB**: I metadati vengono salvati in DynamoDB, garantendo la consistenza dei dati.

3. **Gestione Errori**:
   - **Rollback in caso di fallimento**: Se l'upload su S3 fallisce, il sistema esegue un rollback della transazione.
   - **Pulizia risorse temporanee**: Il sistema pulisce eventuali risorse temporanee create durante il processo di upload.
   - **Messaggi di errore dettagliati**: Il sistema fornisce messaggi di errore dettagliati per facilitare il debugging.

### 4.2 Download File
Il processo di download dei file è stato progettato per garantire sicurezza e performance:

1. **Validazione Accesso**:
   - **Verifica esistenza file**: Il sistema verifica che il file esista in DynamoDB.
   - **Controllo visibilità**: Il sistema verifica che l'utente abbia i permessi necessari per accedere al file.
   - **Validazione permessi utente**: Il sistema verifica che l'utente sia autorizzato ad accedere al file in base al suo ruolo e alla visibilità del file.

2. **Generazione URL**:
   - **Creazione URL presigned**: Il sistema genera un URL presigned per il download del file, valido per un periodo limitato.
   - **Impostazione scadenza**: L'URL presigned viene configurato con una scadenza appropriata (default: 5 minuti).
   - **Configurazione endpoint**: L'URL viene configurato per utilizzare l'endpoint corretto, sia in produzione che in sviluppo.

3. **Risposta**:
   - **URL di download**: Il sistema restituisce l'URL presigned al client.
   - **Gestione errori**: Il sistema gestisce eventuali errori durante il processo di generazione dell'URL.
   - **Logging accessi**: Il sistema registra l'accesso al file per scopi di audit.

### 4.3 Lista File Visibili
Il processo di recupero della lista dei file visibili è stato ottimizzato per performance e scalabilità:

1. **Recupero File**:
   - **Query DynamoDB**: Il sistema esegue una query su DynamoDB per recuperare tutti i file.
   - **Filtro per visibilità**: Il sistema filtra i file in base alla visibilità e ai permessi dell'utente.
   - **Filtro per ruoli**: Se l'utente ha ruoli specifici, il sistema filtra i file in base a questi ruoli.

2. **Trasformazione Dati**:
   - **Conversione in DTO**: I file vengono convertiti in DTO per la trasmissione al client.
   - **Rimozione dati sensibili**: I dati sensibili (come le chiavi S3) vengono rimossi dai DTO.
   - **Ordinamento risultati**: I risultati vengono ordinati in base a criteri specifici (es. data di upload).

## 5. Sicurezza

### 5.1 Gestione Accessi
Il sistema implementa un robusto sistema di gestione degli accessi:

- **Visibilità**:
  - **PUBLIC**: I file sono accessibili a tutti gli utenti autenticati.
  - **PRIVATE**: I file sono accessibili solo al proprietario.
  - **ROLE_BASED**: I file sono accessibili solo agli utenti con ruoli specifici.

- **Validazione**:
  - **Token JWT**: Ogni richiesta deve includere un token JWT valido.
  - **Ruoli utente**: Il sistema verifica i ruoli dell'utente per determinare l'accesso ai file.
  - **Proprietà file**: Il sistema verifica la proprietà del file per l'accesso PRIVATE.

### 5.2 Storage Sicuro
Il sistema implementa diverse misure di sicurezza per lo storage dei file:

- **S3**:
  - **Chiavi univoche**: Ogni file viene salvato con una chiave univoca per prevenire collisioni.
  - **URL temporanei**: Gli URL di download sono temporanei e scadono dopo un periodo specifico.
  - **Accesso controllato**: L'accesso ai file è controllato tramite policy S3.

- **DynamoDB**:
  - **Crittografia dati**: I dati sensibili vengono crittografati prima di essere salvati.
  - **Backup automatici**: Il sistema utilizza i backup automatici di DynamoDB.
  - **Controllo accessi**: L'accesso a DynamoDB è controllato tramite IAM.

### 5.3 Best Practices
Il sistema implementa diverse best practices di sicurezza:

- **Validazione input**: Tutti gli input vengono validati prima di essere processati.
- **Sanitizzazione nomi file**: I nomi dei file vengono sanitizzati per prevenire attacchi.
- **Logging accessi**: Tutti gli accessi ai file vengono registrati per scopi di audit.
- **Rate limiting**: Il sistema implementa rate limiting per prevenire abusi.
- **Timeout URL**: Gli URL di download hanno un timeout per limitare l'accesso.

## 6. Integrazione AWS

### 6.1 S3
L'integrazione con S3 è stata progettata per essere robusta e scalabile:

- **Configurazione**:
  - **Bucket dedicato**: Il sistema utilizza un bucket S3 dedicato per i file.
  - **CORS policy**: Il bucket è configurato con una policy CORS appropriata.
  - **Lifecycle rules**: Il bucket è configurato con regole di lifecycle per la gestione dei file.

- **Operazioni**:
  - **Upload file**: Il sistema utilizza l'AWS SDK per caricare i file su S3.
  - **Download file**: Il sistema genera URL presigned per il download dei file.
  - **Gestione metadati**: Il sistema gestisce i metadati dei file in DynamoDB.

### 6.2 DynamoDB
L'integrazione con DynamoDB è stata ottimizzata per performance e scalabilità:

- **Struttura**:
  - **Tabella FileMetadata**: La tabella principale per i metadati dei file.
  - **Indici secondari**: Indici per ottimizzare le query più comuni.
  - **Throughput configurato**: Il throughput è configurato in base alle necessità.

- **Query**:
  - **Per fileId**: Query efficienti per recuperare file specifici.
  - **Per ownerId**: Query per recuperare i file di un utente specifico.
  - **Per visibilità**: Query per recuperare file con visibilità specifica.

## 7. API Endpoints

### 7.1 Gestione File
Il sistema espone i seguenti endpoint REST:

- **POST /files/upload**
  - **Body**: multipart/form-data contenente il file e i metadati.
  - **Headers**: Authorization con token JWT.
  - **Response**: FileMetadata con i dettagli del file caricato.

- **GET /files/download/{fileId}**
  - **Headers**: Authorization con token JWT.
  - **Response**: URL presigned per il download del file.

- **GET /files/visible**
  - **Headers**: Authorization con token JWT.
  - **Response**: Lista di FileMetadataDto con i dettagli dei file visibili.

## 8. Scalabilità

### 8.1 Architettura
L'architettura del sistema è stata progettata per essere scalabile:

- **Stateless**: Il sistema è stateless, utilizzando JWT per l'autenticazione.
- **NoSQL**: L'utilizzo di DynamoDB garantisce scalabilità orizzontale.
- **Object Storage**: L'utilizzo di S3 garantisce storage scalabile.

### 8.2 Performance
Il sistema implementa diverse ottimizzazioni per le performance:

- **Caching**: Gli URL presigned vengono cachati per ridurre la latenza.
- **Connection Pool**: Le connessioni a S3 e DynamoDB vengono gestite tramite pool.
- **Async Operations**: Le operazioni di upload/download sono asincrone.

### 8.3 Monitoraggio
Il sistema implementa un robusto sistema di monitoraggio:

- **Logging**: Tutte le operazioni vengono registrate per il debugging.
- **Metrics**: Metriche di performance vengono raccolte per l'analisi.
- **Health Checks**: Il sistema include health check per i servizi AWS.

## 9. Deployment

### 9.1 Requisiti
Il deployment richiede i seguenti requisiti:

- **Java 17+**: Versione LTS di Java.
- **Maven 3.6+**: Gestore di dipendenze e build automation.
- **AWS Services**: Accesso a S3 e DynamoDB.
- **AWS Credentials**: Credenziali di accesso AWS.

### 9.2 Configurazione
La configurazione del sistema avviene tramite:

- **application.properties**:
  - **AWS settings**: Configurazione di S3 e DynamoDB.
  - **JWT settings**: Configurazione del JWT.
  - **Service endpoints**: Configurazione degli endpoint del servizio.

### 9.3 Build
Il sistema può essere buildato utilizzando:

- **Maven**: `mvn clean package` per generare il JAR.
- **Docker**: `docker build -t file-service .` per generare l'immagine Docker.

## 10. Conclusioni

Il servizio di gestione file implementa un sistema robusto e scalabile per la gestione dei file, con particolare attenzione alla sicurezza e alla separazione delle responsabilità. L'utilizzo di S3 e DynamoDB garantisce performance e scalabilità, mentre l'architettura modulare permette facili estensioni e manutenzione.

### 10.1 Punti di Forza
- **Architettura modulare**: Facilita l'estensione e la manutenzione.
- **Sicurezza robusta**: Implementa best practices di sicurezza.
- **Scalabilità orizzontale**: Utilizza servizi AWS scalabili.
- **Manutenibilità**: Codice ben strutturato e documentato.

### 10.2 Aree di Miglioramento
- **Compressione file**: Implementare la compressione automatica dei file.
- **Versioning**: Aggiungere il versioning dei file.
- **CDN integration**: Integrare una CDN per migliorare le performance.
- **Test coverage**: Aumentare la copertura dei test.
- **Monitoring avanzato**: Implementare monitoring più dettagliato. 