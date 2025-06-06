# Documentazione Tecnica del Servizio di Autenticazione

## 1. Introduzione
Il servizio di autenticazione è un componente fondamentale del sistema, progettato per gestire l'autenticazione e l'autorizzazione degli utenti. Il servizio è implementato utilizzando Spring Boot 3.x e utilizza JWT (JSON Web Tokens) per la gestione delle sessioni, con DynamoDB come database per la persistenza dei dati degli utenti.

### 1.1 Tecnologie Utilizzate
- **Spring Boot 3.x**: Framework principale per lo sviluppo dell'applicazione
- **Spring Security**: Gestione della sicurezza e autenticazione
- **JWT (JSON Web Tokens)**: Gestione delle sessioni e autenticazione stateless
- **DynamoDB**: Database NoSQL per la persistenza dei dati
- **AWS SDK**: Integrazione con i servizi AWS
- **Lombok**: Riduzione del boilerplate code
- **Maven**: Gestione delle dipendenze e build automation

### 1.2 Requisiti di Sistema
- Java 17 o superiore
- Maven 3.6.x o superiore
- Accesso a un'istanza DynamoDB (locale o AWS)
- Configurazione AWS (access key e secret key)

## 2. Architettura del Sistema

### 2.1 Componenti Principali
Il servizio è strutturato in diversi layer:
- **Controller Layer**: Gestisce le richieste HTTP e implementa gli endpoint REST
- **Service Layer**: Implementa la logica di business e la gestione dei token JWT
- **Repository Layer**: Gestisce la persistenza dei dati tramite DynamoDB
- **Security Layer**: Gestisce l'autenticazione e l'autorizzazione tramite Spring Security
- **Model Layer**: Definisce le entità del dominio e le loro relazioni
- **DTO Layer**: Gestisce il trasferimento dei dati tra i layer
- **Config Layer**: Configura il sistema e le sue dipendenze

### 2.2 Diagramma dell'Architettura
```
[Client] <---> [Auth Service]
                  |
                  |---> [Controller Layer]
                  |        |
                  |        |---> [Service Layer]
                  |        |        |
                  |        |        |---> [Repository Layer]
                  |        |        |        |
                  |        |        |        |---> [DynamoDB]
                  |        |        |
                  |        |        |---> [Security Layer]
                  |        |                 |
                  |        |                 |---> [JWT Processing]
                  |        |                 |---> [Role Management]
                  |        |
                  |        |---> [DTO Layer]
                  |
                  |---> [Config Layer]
```

## 3. Dettaglio dei Componenti

### 3.1 Model Layer

#### 3.1.1 User.java
```java
@DynamoDBTable(tableName = "users")
public class User {
    @DynamoDBHashKey(attributeName = "id")
    @DynamoDBAutoGeneratedKey
    private String id;

    @DynamoDBAttribute(attributeName = "email")
    private String email;

    @DynamoDBAttribute(attributeName = "password")
    private String password;

    @DynamoDBAttribute(attributeName = "role")
    private String role;
}
```
La classe `User` rappresenta l'entità utente nel sistema. È annotata con `@DynamoDBTable` per mapparla alla tabella DynamoDB "users". Gli attributi principali sono:
- `id`: Chiave primaria generata automaticamente tramite UUID
- `email`: Email dell'utente (usata per il login), deve essere unica
- `password`: Password criptata dell'utente usando BCrypt
- `role`: Ruolo dell'utente nel sistema (USER, MODERATOR, ADMIN)

#### 3.1.2 Validazioni e Vincoli
- Email deve essere un indirizzo valido
- Password deve rispettare i criteri di sicurezza (minimo 8 caratteri, maiuscole, numeri)
- Role deve essere uno dei valori predefiniti

### 3.2 DTO Layer

#### 3.2.1 LoginRequest.java
```java
@Data
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String Email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String Password;
}
```
DTO utilizzato per la richiesta di login, contenente le credenziali dell'utente. Include validazioni per:
- Email: formato valido e non vuoto
- Password: lunghezza minima e non vuota

#### 3.2.2 RegisterRequest.java
```java
@Data
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String Email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$", 
             message = "Password must contain at least one digit, one lowercase and one uppercase letter")
    private String Password;

    @Pattern(regexp = "^(USER|MODERATOR|ADMIN)$", message = "Invalid role")
    private String Role;
}
```
DTO utilizzato per la registrazione di nuovi utenti, includendo validazioni per:
- Email: formato valido e non vuoto
- Password: requisiti di sicurezza
- Role: valori predefiniti

#### 3.2.3 JwtResponse.java
```java
@Data
@AllArgsConstructor
public class JwtResponse {
    private String Token;
    private String Expiration;
    private String Role;
}
```
DTO utilizzato per la risposta di autenticazione, contenente:
- Token JWT
- Data di scadenza
- Ruolo dell'utente

### 3.3 Controller Layer

#### 3.3.1 AuthController.java
```java
@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        if(userOptional.isPresent() && 
           passwordEncoder.matches(loginRequest.getPassword(), userOptional.get().getPassword())) {
            String token = jwtService.generateToken(userOptional.get());
            return ResponseEntity.ok(new JwtResponse(token, 
                                                   jwtService.getExpirationDate(token),
                                                   userOptional.get().getRole())); 
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                           .body(new ErrorResponse("Invalid credentials"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if(userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                               .body(new ErrorResponse("Email already exists"));
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(registerRequest.getRole() != null ? 
                    registerRequest.getRole().toUpperCase() : "USER");
        
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
```

#### 3.3.2 UserController.java
```java
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @RequireRole({"ADMIN", "MODERATOR"})
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = (List<User>) userRepository.findAll();
        List<UserDTO> userDTOs = users.stream()
            .map(user -> new UserDTO(user.getEmail(), user.getRole()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }

    @RequireRole({"ADMIN"})
    @DeleteMapping("/{email}")
    public ResponseEntity<Void> deleteUser(@PathVariable String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
            
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/public")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("This is a public endpoint");
    }
}
```

### 3.4 Service Layer

#### 3.4.1 JwtService.java
```java
@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    public String generateToken(User user) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String validateTokenAndGetUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder() 
                    .setSigningKey(jwtSecret.getBytes()) 
                    .build() 
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException e) {
            return null;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getExpirationDate(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getExpiration().toString();
    }
}
```

### 3.5 Security Layer

#### 3.5.1 JwtAuthenticationFilter.java
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        String token = jwtService.extractToken(request.getHeader("Authorization"));

        if (token != null && !token.isEmpty()) {
            String username = jwtService.validateTokenAndGetUsername(token);
            if (username != null) {
                User user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());
                
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user, 
                    null, 
                    Collections.singletonList(authority)
                );
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

#### 3.5.2 RoleAspect.java
```java
@Aspect
@Component
@RequiredArgsConstructor
public class RoleAspect {
    private final HttpServletRequest request;
    private final JwtService jwtService;

    @Around("@annotation(RequireRole) || @within(RequireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequireRole requireRole = method.getAnnotation(RequireRole.class);

        if (requireRole == null) {
            requireRole = joinPoint.getTarget().getClass().getAnnotation(RequireRole.class);
        }

        if(requireRole == null) {
            return joinPoint.proceed();
        }

        String authHeader = request.getHeader("Authorization"); 
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"); 
        }

        String token = authHeader.substring(7);
        Claims claims = jwtService.extractAllClaims(token);

        String userRole = "ROLE_" + claims.get("role", String.class);

        if(Arrays.stream(requireRole.value())
                .map(role -> "ROLE_" + role)
                .noneMatch(r -> r.equals(userRole))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "You are not authorized to access this resource");
        }

        return joinPoint.proceed();
    }
}
```

### 3.6 Config Layer

#### 3.6.1 SecurityConfig.java
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/auth/**", "/hello", "/users/public").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(httpBasic -> httpBasic.disable());
        return http.build();
    }
}
```

#### 3.6.2 DynamoDBConfig.java
```java
@Configuration
@EnableDynamoDBRepositories(basePackages = "com.github.giuseppemarletta.auth_service.Repository")
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
        Table table = dynamoDB.getTable("users");

        try {
            table.describe();
        } catch (Exception e) {
            if (e instanceof ResourceNotFoundException) {
                CreateTableRequest createTableRequest = new CreateTableRequest()
                        .withTableName("users")
                        .withKeySchema(new KeySchemaElement("id", KeyType.HASH))
                        .withAttributeDefinitions(new AttributeDefinition("id", ScalarAttributeType.S))
                        .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
                amazonDynamoDB.createTable(createTableRequest);
            }
        }
    }
}
```

## 4. Flusso di Autenticazione

### 4.1 Registrazione
1. **Validazione Input**:
   - Verifica formato email
   - Verifica requisiti password
   - Validazione ruolo (se specificato)

2. **Controllo Duplicati**:
   - Verifica esistenza email nel database
   - Gestione conflitti

3. **Creazione Utente**:
   - Generazione ID univoco
   - Criptazione password
   - Impostazione ruolo default (USER)

4. **Persistenza**:
   - Salvataggio in DynamoDB
   - Gestione errori di persistenza

### 4.2 Login
1. **Validazione Credenziali**:
   - Verifica esistenza utente
   - Confronto password criptata

2. **Generazione Token**:
   - Creazione claims JWT
   - Impostazione scadenza
   - Firma token

3. **Risposta**:
   - Token JWT
   - Informazioni utente
   - Data scadenza

### 4.3 Autenticazione Richieste
1. **Estrazione Token**:
   - Lettura header Authorization
   - Validazione formato Bearer

2. **Validazione Token**:
   - Verifica firma
   - Controllo scadenza
   - Estrazione claims

3. **Contesto Sicurezza**:
   - Creazione Authentication
   - Impostazione authorities
   - Aggiornamento SecurityContext

### 4.4 Autorizzazione
1. **Controllo Ruoli**:
   - Lettura annotazione @RequireRole
   - Verifica ruoli utente
   - Gestione accesso negato

2. **Esecuzione Endpoint**:
   - Procedura normale se autorizzato
   - Risposta 403 se non autorizzato

## 5. Sicurezza

### 5.1 Gestione Password
- **Criptazione**: BCrypt con salt automatico
- **Validazione**: Requisiti minimi di sicurezza
- **Storage**: Solo hash, mai in chiaro

### 5.2 Token JWT
- **Struttura**:
  - Header: algoritmo HS256
  - Payload: sub, role, iat, exp
  - Signature: HMAC-SHA256

- **Sicurezza**:
  - Scadenza: 1 ora
  - Firma: chiave segreta
  - Claims: minimi necessari

### 5.3 Ruoli e Permessi
- **Gerarchia**:
  - USER: accesso base
  - MODERATOR: gestione utenti
  - ADMIN: accesso completo

- **Implementazione**:
  - Annotazioni @RequireRole
  - AOP per controllo
  - Spring Security integration

## 6. Integrazione DynamoDB

### 6.1 Struttura Tabella
- **Nome**: users
- **Chiave**: id (String)
- **Indici**:
  - Email (secondario)
  - Role (secondario)

### 6.2 Operazioni
- **CRUD**:
  - Create: nuovo utente
  - Read: per email/id
  - Update: modifica dati
  - Delete: rimozione utente

- **Query**:
  - Per email
  - Per ruolo
  - Lista completa

## 7. API Endpoints

### 7.1 Autenticazione
- **POST /auth/register**
  - Body: {email, password, role?}
  - Response: 201 Created
  - Error: 409 Conflict

- **POST /auth/login**
  - Body: {email, password}
  - Response: 200 OK + JWT
  - Error: 401 Unauthorized

### 7.2 Gestione Utenti
- **GET /users**
  - Auth: ADMIN/MODERATOR
  - Response: List<UserDTO>
  - Error: 403 Forbidden

- **DELETE /users/{email}**
  - Auth: ADMIN
  - Response: 204 No Content
  - Error: 404 Not Found

### 7.3 Health Check
- **GET /ping**
  - Response: "pong"
  - No Auth Required

- **GET /hello**
  - Response: Welcome Message
  - No Auth Required

## 8. Scalabilità

### 8.1 Architettura
- **Stateless**: JWT-based
- **NoSQL**: DynamoDB
- **Microservizi**: Isolamento

### 8.2 Performance
- **Caching**: Token validation
- **Connection Pool**: DynamoDB
- **Async Operations**: Where possible

### 8.3 Monitoraggio
- **Logging**: Request/Response
- **Metrics**: Performance
- **Health Checks**: Service status

## 9. Deployment

### 9.1 Requisiti
- Java 17+
- Maven 3.6+
- DynamoDB
- AWS Credentials

### 9.2 Configurazione
- **application.properties**:
  - JWT settings
  - AWS credentials
  - DynamoDB endpoint

### 9.3 Build
- **Maven**: `mvn clean package`
- **Docker**: `docker build -t auth-service .`

## 10. Conclusioni

Il servizio di autenticazione implementa un sistema robusto e scalabile per la gestione degli utenti, con particolare attenzione alla sicurezza e alla separazione delle responsabilità. L'utilizzo di JWT e DynamoDB garantisce performance e scalabilità, mentre l'architettura modulare permette facili estensioni e manutenzione.

### 10.1 Punti di Forza
- Architettura modulare
- Sicurezza robusta
- Scalabilità orizzontale
- Manutenibilità

### 10.2 Aree di Miglioramento
- Refresh token
- Rate limiting
- Logging avanzato
- Monitoring
- Test coverage 