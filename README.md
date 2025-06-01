# ☁️ CloudFileVault

> ⚠️ **Stato del progetto:** In sviluppo attivo  
> Il progetto evolve in modo iterativo, con estensioni architetturali continue basate su scenari reali cloud-native.

CloudFileVault è una piattaforma distribuita per la gestione intelligente di file nel cloud. Ogni utente può autenticarsi, caricare file con controlli granulari di visibilità e ruolo, ricevere notifiche asincrone sugli eventi rilevanti, ed eseguire **azioni intelligenti sui file** in base al loro tipo o contenuto grazie all'integrazione con **Amazon Bedrock**.

Il sistema segue un approccio modulare a microservizi, resiliente, osservabile e interamente gestito tramite AWS.

---

## 🔧 Tecnologie principali
- **Spring Boot**, Spring Security, AspectJ
- **Amazon S3**, **DynamoDB (via Spring Data DynamoDB)**, **RabbitMQ**
- JWT, Scrypt, RBAC, DTO, Remote Facade
- **Amazon Bedrock** (azioni intelligenti sui file)
- **Amazon SES** per notifiche email (es. registrazione, alert)
- **AWS X-Ray**, **OpenTelemetry**, **CloudWatch**
- **AWS IAM Roles** per accesso controllato a S3 e DynamoDB
- **Secrets Manager** per la gestione centralizzata dei segreti
- GitHub Actions, Terraform, ECR, ECS Fargate

---

## 📦 Microservizi
- `auth-service`: Autenticazione, registrazione, rilascio token JWT
- `user-service`: Gestione ruoli e profilo utente
- `file-service`: Upload/download file, visibilità e permessi
- `notification-service`: Notifiche asincrone via RabbitMQ + Amazon SES
- `action-service`: Azioni automatiche sui file (es. esecuzione .py, analisi .csv, compilazione .java) orchestrate via Bedrock

---

## 📄 Funzionalità principali
- 🔐 Autenticazione sicura e gestione JWT
- 🗂️ Upload file con metadati (visibilità, ruoli richiesti)
- 🔎 Azioni dinamiche sui file in base al tipo (Bedrock + modelli generativi)
- 📬 Notifiche asincrone su eventi (upload, modifica ruolo, alert sicurezza)
- ✉️ Email automatiche (benvenuto, alert) via Amazon SES
- ⚙️ Accesso sicuro a DynamoDB e S3 tramite IAM Role

---

## 🎯 Obiettivi progettuali
- Applicazione reale cloud-ready per portfolio (in stile AWS Solutions Architect)
- Architettura scalabile, resiliente e osservabile
- Integrazione AI cloud-native (Bedrock) per estendere le capacità del sistema
- Esperienza end-to-end: sviluppo, CI/CD, infrastruttura-as-code, monitoraggio

---

## 💻 Interfaccia
Il sistema include anche un’interfaccia web minimale (in fase di sviluppo) per l'interazione visuale con l'utente (upload file, login, anteprima).

---

## 🚀 Deployment
- Infrastruttura gestita con Terraform
- CI/CD via GitHub Actions
- Deploy su ECS Fargate con immagini Docker
- Segreti centralizzati su AWS Secrets Manager

---

## 🧪 Testing
- Test unitari e di integrazione con JUnit, Mockito
- Copertura con JaCoCo e PITest
- Tracing distribuito verificabile con Jaeger o X-Ray

---

## 📚 Pattern distribuiti adottati
- Remote Facade
- Data Transfer Object (DTO)
- Role-based Access Control (RBAC)
- Client Session Token + Authenticator
- Circuit Breaker (Resilience4j)
- Event-driven Messaging (RabbitMQ)
- Serverless AI Orchestration (Bedrock)

---

## 🛠️ Esempi di azioni intelligenti sui file (`action-service`)
- `.java` → compilazione automatica
- `.py` → esecuzione sandboxata
- `.csv` → analisi descrittiva con LLM
- `.txt` → riassunto automatico
- `.json` → validazione schema
- Estensibile via orchestrazione Amazon Bedrock

---

## 🧠 Estensioni future
- Integrazione con API esterne per versioning file
- Firma digitale dei documenti
- Logica di approvazione per file visibili solo ad admin
- SAML/OIDC per Single Sign-On aziendale

---

CloudFileVault è pensato come un progetto reale, pronto per essere discusso in un contesto lavorativo, universitario o di certificazione (es. AWS Developer / Solutions Architect).



