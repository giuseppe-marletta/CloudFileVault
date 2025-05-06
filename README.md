# ☁️ CloudFileVault

> ⚠️ **Stato del progetto:** In fase di progettazione e sviluppo attivo.  
> Le specifiche architetturali, i componenti e le tecnologie utilizzate sono in continua evoluzione.  
> Il progetto potrebbe subire modifiche sostanziali durante il processo iterativo di progettazione.

CloudFileVault è un sistema distribuito di gestione file cloud-native, costruito con Spring Boot e progettato secondo le best practice AWS. Include autenticazione sicura, controllo degli accessi basato sui ruoli, gestione di file su Amazon S3, notifiche asincrone con RabbitMQ, tracing distribuito e resilienza tramite circuit breaker.

## 🔧 Tecnologie principali
- Spring Boot, Spring Security, AspectJ
- Amazon S3, Amazon DynamoDB, RabbitMQ
- JWT, Scrypt, RBAC, DTO, Remote Facade
- Resilience4j, OpenTelemetry, Jaeger
- GitHub Actions, Terraform, AWS ECS Fargate
- **Amazon SES** per l’invio di email (es. benvenuto, conferma registrazione)
- **AWS Secrets Manager** per la gestione sicura delle credenziali (JWT secret, DynamoDB, ecc.)
- **AWS X-Ray e CloudWatch** per tracing distribuito e osservabilità
- **IAM Roles** per l’accesso sicuro a DynamoDB e S3

## 📦 Microservizi
- `auth-service` – Autenticazione e gestione token
- `user-service` – Profilo utente e ruoli
- `file-service` – Upload/download su S3
- `notification-service` – Notifiche via eventi RabbitMQ

## 📄 Funzionalità principali
- 🔐 Login sicuro e rilascio JWT
- 🗂️ Upload e download di file con visibilità basata sul ruolo
- 📬 Notifiche asincrone su eventi (upload, accesso, promozione utente)
- 📉 Tracciamento distribuito delle chiamate
- 📧 Invio di email automatiche via Amazon SES (es. benvenuto, alert sicurezza)
- 📁 Accesso differenziato a DynamoDB/S3 tramite IAM Role

## 🚀 Deployment
- Infrastruttura definita con Terraform
- CI/CD con GitHub Actions
- Deploy automatico su AWS ECS Fargate
- Gestione segreti centralizzata tramite AWS Secrets Manager

## 🧪 Testing
- JUnit, Mockito, JaCoCo, PITest
- Tracing con Jaeger e AWS X-Ray

## 📚 Pattern distribuiti utilizzati
- Authenticator, Client Session State
- Remote Facade, DTO
- Role-based Access Control (RBAC)
- Circuit Breaker, Message Queue

