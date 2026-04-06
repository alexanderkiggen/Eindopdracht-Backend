# TicketTracker - Installatiehandleiding


## Inhoudsopgave

1. [Inleiding](#inleiding)
2. [Beschrijving en functionaliteit](#beschrijving-en-functionaliteit)
3. [Projectstructuur en gebruikte technieken](#projectstructuur-en-gebruikte-technieken)
4. [Benodigdheden](#4-benodigdheden)
5. [Installatie instructies](#5-installatie-instructies)
6. [Uitvoeren van tests](#6-uitvoeren-van-tests)
7. [Testgebruikers en user-rollen](#7-testgebruikers-en-user-rollen)
8. [Overige commando's](#8-overige-commandos)


## 1. Inleiding

Dit document beschrijft hoe de TicketTracker web-API geïnstalleerd en gebruikt kan worden. TicketTracker is een backend web-API gebouwd met [Spring Boot](https://spring.io/projects/spring-boot) waarmee ontwikkelteams tickets (zoals bugs, taken en features) kunnen beheren en structureren. De applicatie maakt gebruik van PostgreSQL voor dataopslag en [Keycloak](https://www.keycloak.org/) voor authenticatie en autorisatie via JWT-tokens.


## 2. Beschrijving en functionaliteit

TicketTracker is een beveiligde REST web-API ontworpen voor kleine ontwikkelteams die op een efficiënte manier bugs en taken willen beheren. De applicatie biedt een gericht alternatief voor grote platforms zoals [Jira van Atlassian](https://www.atlassian.com/nl/software/jira).

**Kernfunctionaliteiten:**

- **Authenticatie & autorisatie**: Beveiligde toegang via Keycloak JWT-tokens met twee gebruikersrollen: `DEVELOPER` en `PROJECTMANAGER`.
- **Ticketbeheer**: Aanmaken, opvragen en bijwerken van tickets inclusief verplichte bestandsbijlagen.
- **Projectorganisatie**: Tickets koppelen aan projecten en toewijzen aan developers.
- **Communicatie**: Opmerkingen plaatsen en inzien per ticket.


## 3. Projectstructuur en gebruikte technieken

### Projectstructuur

```
src/
├── main/
│   ├── java/nl/novi/tickettracker/
│   │   ├── controllers/        # REST-endpoints (TicketController, ProjectController, UserController)
│   │   ├── dtos/               # Data Transfer Objects (input en output)
│   │   ├── exceptions/         # GlobalExceptionHandler en RecordNotFoundException
│   │   ├── models/             # JPA-entiteiten en enumeraties
│   │   ├── repositories/       # Spring Data JPA repositories
│   │   ├── security/           # SecurityConfig en JwtUserSync
│   │   └── services/           # Bedrijfslogica (TicketService, ProjectService, UserService)
│   └── resources/
│       ├── application.yaml    # Configuratie voor productie
│       └── data.sql            # Initiële data
└── test/
    ├── java/nl/novi/tickettracker/
    │   ├── controllers/        # Integratietests
    │   └── services/           # Unit-tests
    └── resources/
        └── application-test.yaml  # Configuratie voor tests (H2 in-memory database)
```

### Gebruikte technieken en frameworks

| Technologie         | Versie  | Doel                                  |
|---------------------|---------|---------------------------------------|
| Java                | 21      | Programmeertaal (LTS-versie)          |
| Spring Boot         | 3.x     | Framework voor de REST web-API        |
| Maven               | 3.x     | Dependency manager en build-tool      |
| Spring Security     | 6.x     | Authenticatie en autorisatie          |
| Spring Data JPA     | 3.x     | Databasecommunicatie via repositories |
| Hibernate           | 6.x     | ORM (Object Relational Mapping)       |
| PostgreSQL          | 15+     | Relationele productiedatabase         |
| H2                  | 2.x     | In-memory database voor tests         |
| Keycloak            | 24+     | Identity provider voor JWT-tokens     |
| Lombok              | 1.18+   | Reduceert boilerplate code            |
| JUnit 5             | 5.x     | Unit- en integratietests              |
| Mockito             | 5.x     | Mocking framework                     |

---

## 4. Benodigdheden

Om de web-API te starten moet de volgende software op je systeem geïnstalleerd zijn:


- **Java 21**: [https://www.java.com/nl/](https://www.java.com/nl/)
- **Maven 3.8+**: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
- **PostgreSQL 15+**: [https://www.postgresql.org/download](https://www.postgresql.org/download)
- **Keycloak 24+**: [https://www.keycloak.org/downloads](https://www.keycloak.org/downloads)
- **Postman** (optioneel, voor het testen van endpoints): [https://www.postman.com/downloads](https://www.postman.com/downloads)  
  De Postman-collectie is meegeleverd als `tickettracker_postman_collection.json` in de root van het project.
  Importeer dit in Postman via File → Import en raadpleeg de API-documentatie voor verdere configuratie.
  Controleer de installaties met:

```bash
java -version
mvn -version
psql --version
```


## 5. Installatie instructies

### Stap 1. PostgreSQL-database aanmaken

Start PostgreSQL en maak een nieuwe database aan:

```sql
CREATE DATABASE tickettracker;
```

### Stap 2. Keycloak configureren via realm-export

Bij het project is een kant-en-klare realm-export meegeleverd als `tickettracker-realm-export.json`. Dit bestand bevat de volledige Keycloak-configuratie inclusief de client, rollen en gebruikers.

#### Stap 2a. Keycloak starten

Installeer [Keycloak](https://www.keycloak.org/downloads). Klik daarna met de rechtermuisknop op de map van je Keycloak-installatie en selecteer **"Open in terminal"**. Start Keycloak vervolgens in development-modus met het volgende commando:

**Op Windows:**
```bash
bin\kc.bat start-dev --http-port 9090
```

**Op macOS/Linux:**
```bash
./kc.sh start-dev --http-port=9090
```

#### Stap 2b. Realm importeren via de Admin Console

1. Open de Keycloak Admin Console via `http://localhost:9090`.
2. Log in met jouw persoonlijke beheerdersinloggegevens.
3. Klik linksboven op de realm-selector en kies **Create realm**.
4. Klik op **Browse** en selecteer het bestand `tickettracker-realm-export.json`.
5. Klik op **Create**.

De realm `tickettracker` wordt aangemaakt inclusief de client en rollen.

#### Stap 2c. Gebruikerswachtwoorden instellen

De gebruikers worden geïmporteerd, maar wachtwoorden worden om veiligheidsredenen **niet** meegenomen in een export. Stel voor elke gebruiker handmatig een wachtwoord in:

1. Ga in de Admin Console naar **Users**.
2. Klik op een gebruiker, ga naar het tabblad **Credentials**.
3. Klik op **Set password**, vul een wachtwoord in en zet **Temporary** op **Off**.
4. Herhaal dit voor alle vier de gebruikers:

   | Gebruikersnaam | Wachtwoord  | Rol              |
   |----------------|-------------|------------------|
   | johndoe        | 0000        | DEVELOPER        |
   | janedoe        | 0000        | DEVELOPER        |
   | johnsmith      | 0000        | PROJECTMANAGER   |
   | no_roles_user  | 0000        | (geen)           |

#### Stap 2d. Client secret ophalen

De `client_secret` is nodig voor het ophalen van JWT-tokens. Ga naar:

1. Open de Keycloak Admin Console via http://localhost:9090.
2. Selecteer linksboven de realm tickettracker.
3. Ga in het menu naar Clients en klik op tickettracker.
4. Ga naar het tabblad Credentials en kopieer de client_secret.

De client_secret is nodig voor het ophalen van JWT-tokens, bijvoorbeeld bij het gebruik van de Postman-collectie.
Controleer daarnaast onder het tabblad Settings of de optie direct access grants is ingeschakeld.
### Stap 3. Applicatieconfiguratie aanpassen

Open `src/main/resources/application.yaml` en pas de volgende waarden aan indien nodig:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tickettracker
    username: postgres          # Jouw persoonlijke PostgreSQL-gebruikersnaam
    password: jouw_wachtwoord   # Jouw persoonlijke PostgreSQL-wachtwoord
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9090/realms/tickettracker
```

### Stap 4. Project bouwen

Navigeer naar de projectmap en bouw het project:

```bash
cd tickettracker
mvn clean install -DskipTests
```

### Stap 5. Applicatie starten

```bash
mvn spring-boot:run
```

De applicatie start op `http://localhost:8080`. Bij de eerste start worden automatisch de tabellen aangemaakt en de testdata uit `data.sql` geladen.


## 6. Uitvoeren van tests

De applicatie bevat integratietests en unit-tests. De tests draaien op een H2 in-memory database en vereisen geen actieve PostgreSQL- of Keycloak-instantie.

### Alle tests uitvoeren

```bash
mvn test
```

### Overzicht van testklassen

| Klasse                               | Type           | Beschrijving                                |
|--------------------------------------|----------------|---------------------------------------------|
| `TicketControllerIntegrationTest`    | Integratie     | Test alle ticket-endpoints                  |
| `ProjectControllerIntegrationTest`   | Integratie     | Test alle project-endpoints                 |
| `UserControllerIntegrationTest`      | Integratie     | Test alle user-endpoints                    |
| `TicketServiceTest`                  | Unit           | Test de volledige logica van TicketService  |
| `ProjectServiceTest`                 | Unit           | Test de volledige logica van ProjectService |
| `UserServiceTest`                    | Unit           | Test de volledige logica van UserService    |

### Gebruikte tools

- **Spring Boot Test**: Context laden en integratietests
- **MockMvc**: Simuleren van HTTP-verzoeken zonder echte server
- **JUnit 5**: Testframework (annotaties: `@Test`, `@BeforeEach`, `@AfterEach`)
- **Mockito**: Mocking van dependencies in unit-tests
- **H2**: In-memory database tijdens tests


## 7. Testgebruikers en user-rollen

De volgende gebruikers worden automatisch aangemaakt via `data.sql`. Zorg ervoor dat deze gebruikers ook in Keycloak aanwezig zijn met dezelfde gebruikersnaam.

| Gebruikersnaam | Rol              | Rechten                                                                                                                                                                                        |
|----------------|------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `johndoe`      | DEVELOPER        | Tickets opvragen, status bijwerken (alleen eigen tickets), bestanden uploaden, opmerkingen plaatsen                                                                                            |
| `janedoe`      | DEVELOPER        | Zelfde als johndoe                                                                                                                                                                             |
| `johnsmith`    | PROJECTMANAGER   | Alle rechten van DEVELOPER + projecten aanmaken/bewerken, developers toewijzen aan tickets, tickets naar andere projecten verplaatsen en het verwijderen van tickets, opmerkingen en bestanden |
| `no_roles_user`| (geen)           | Geen toegang tot endpoints (alleen voor het testen van 403-scenario's)                                                                                                                         |


## 8. Overige commando's

### Project bouwen zonder tests

```bash
mvn clean install -DskipTests
```

### Applicatie stoppen

```
CTRL + C
```

### Database handmatig resetten

```bash
psql -U postgres -c "DROP DATABASE tickettracker;"
psql -U postgres -c "CREATE DATABASE tickettracker;"
```

Start daarna de applicatie opnieuw. De tabellen en testdata worden automatisch opnieuw aangemaakt.

### Logs bekijken

De applicatie toont SQL-queries in de console omdat `show-sql: true` is ingesteld in `application.yaml`. Dit helpt bij het debuggen van database-interacties.
