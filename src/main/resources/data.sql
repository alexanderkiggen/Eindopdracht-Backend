--  TicketTracker – SEED DATA

-- Stap 1. User profiles

INSERT INTO user_profiles (id, first_name, last_name, bio, email, phone, birthdate)
VALUES
    (1, 'John',  'Doe',   'Backend developer met focus op Java en Spring Boot.',  'johndoe@novi-education.nl',   '0612345678', '1995-06-15 00:00:00'),
    (2, 'Jane',  'Doe',   'Senior developer, gespecialiseerd in REST API design.', 'janedoe@novi-education.nl',   '0687654321', '1993-03-22 00:00:00'),
    (3, 'John',  'Smith', 'Projectmanager verantwoordelijk voor het TicketTracker team.', 'johnsmith@novi-education.nl', '0611223344', '1988-11-05 00:00:00'),
    (4, 'NoRolesUser',  'NoRolesUser', 'Gebruiker zonder rollen.', 'norolesuser@novi-education.nl', '0612345679', '1988-11-05 00:00:00')

ON CONFLICT (id) DO NOTHING;

-- Stap 2. Users

INSERT INTO users (id, username, user_profile_id)
VALUES
    (1, 'johndoe',   1), -- (DEVELOPER)
    (2, 'janedoe',   2), -- (DEVELOPER)
    (3, 'johnsmith', 3), -- (PROJECTMANAGER)
    (4, 'no_roles_user', 4)  -- (Gebruiker heeft geen rollen en kan dus niets)

ON CONFLICT (id) DO NOTHING;

-- Stap 3. Projects

INSERT INTO projects (id, name, description, start_date)
VALUES
    (1, 'TicketTracker Backend',  'Ontwikkeling van de Spring Boot REST API voor het TicketTracker systeem.',  '2026-01-10 09:00:00'),
    (2, 'Klantportaal v2',        'Herontwerp van het klantportaal inclusief dashboard en rapportages.',        '2026-02-15 09:00:00'),
    (3, 'DevOps Pipeline',        'Opzetten van CI/CD pipelines en automatische deployments via GitHub Actions.', '2026-03-01 09:00:00')
ON CONFLICT (id) DO NOTHING;

-- Stap 4. Tickets

INSERT INTO tickets (id, title, description, status, type, project_id, assigned_user_id)
VALUES
    (1,  'Login endpoint reageert te traag',
     'Het authenticatie-endpoint heeft een gemiddelde responstijd van 800ms bij 10 gelijktijdige verzoeken. Verwachte tijd is onder 200ms.',
     'OPEN',        'BUG',     1, 1),

    (2,  'JWT refresh token implementeren',
     'Voeg refresh-token functionaliteit toe zodat gebruikers niet iedere 15 minuten opnieuw hoeven in te loggen.',
     'IN_PROGRESS', 'FEATURE', 1, 2),

    (3,  'Unit tests schrijven voor TicketService',
     'Minimaal 10 nuttige unit tests volgens de 3 A-structuur met 100% line coverage op de service laag.',
     'IN_REVIEW',   'TASK',    1, 1),

    (4,  'Dashboard overzichtspagina bouwen',
     'Maak een overzichtspagina waarop openstaande, in-progress en gesloten tickets per project worden weergegeven.',
     'OPEN',        'FEATURE', 2, 2),

    (5,  'Bestandsupload crasht bij grote bestanden',
     'Bij het uploaden van bestanden groter dan 5MB retourneert de API een HTTP 500. Maximale bestandsgrootte moet worden afgedwongen en foutmelding verbeterd.',
     'CLOSED',      'BUG',     2, 1),

    (6,  'RBAC controleren op alle endpoints',
     'Verificatie dat alle endpoints correct beveiligd zijn per rol: DEVELOPER mag geen projecten aanmaken of developers toewijzen.',
     'OPEN',        'TASK',    1, 2),

    (7,  'Docker Compose configuratie aanmaken',
     'Maak een docker-compose.yml aan voor de lokale ontwikkelomgeving inclusief PostgreSQL en Keycloak containers.',
     'IN_PROGRESS', 'TASK',    3, 1),

    (8,  'Paginering toevoegen aan ticket overzicht',
     'Het GET /tickets endpoint moet paginering ondersteunen via page, size, sort en dir parameters.',
     'CLOSED',      'FEATURE', 1, 2),

    (9,  'Null pointer in UserService bij nieuw account',
     'Bij een nieuw Keycloak account zonder profiel gooit getUserByUsername een NullPointerException. Afvangen met RecordNotFoundException.',
     'OPEN',        'BUG',     1, 1),

    (10, 'README installatiehandleiding schrijven',
     'Volledige installatiehandleiding in README.md inclusief Keycloak configuratie, database setup en teststappen.',
     'IN_REVIEW',   'TASK',    3, 2)
ON CONFLICT (id) DO NOTHING;

-- Stap 5. Comments

INSERT INTO comments (id, text, timestamp, ticket_id)
VALUES
    (1,  'Gereproduceerd op lokale omgeving. Lijkt op een database connection pool issue. Ga dit verder onderzoeken.',
     '2026-03-10 10:30:00', 1),

    (2,  'Ik pak dit op na het afronden van de refresh token implementatie.',
     '2026-03-10 14:00:00', 1),

    (3,  'Eerste implementatie klaar. Wacht op code review van John Smith voordat ik verder ga.',
     '2026-03-11 14:15:00', 2),

    (4,  'Code review ingepland voor morgenochtend.',
     '2026-03-12 09:00:00', 2),

    (5,  'Tests zijn bijna klaar. Wacht nog op goedkeuring van de service-laag implementatie.',
     '2026-03-12 09:45:00', 3),

    (6,  'Prioriteit verhoogd door de product owner. Graag voor sprint-einde afronden.',
     '2026-03-13 16:00:00', 4),

    (7,  'Bug bevestigd. De MaxUploadSizeExceededException wordt nu correct afgehandeld door de GlobalExceptionHandler.',
     '2026-03-14 11:20:00', 5),

    (8,  'Opgelost in commit a3f9c12. Maximale bestandsgrootte ingesteld op 10MB in application.yaml.',
     '2026-03-14 15:45:00', 5),

    (9,  'Alle DEVELOPER-endpoints getest. Twee endpoints missen nog de juiste rolcontrole.',
     '2026-03-15 10:00:00', 6),

    (10, 'Docker Compose basis werkt. Keycloak realm import nog toevoegen.',
     '2026-03-16 13:30:00', 7)
ON CONFLICT (id) DO NOTHING;

-- Stap 6. Sequences resetten zodat nieuwe inserts via JPA geen conflicten veroorzaken met handmatig ingevoegde IDs.

SELECT setval('user_profiles_id_seq', (SELECT MAX(id) FROM user_profiles));
SELECT setval('users_id_seq',         (SELECT MAX(id) FROM users));
SELECT setval('projects_id_seq',      (SELECT MAX(id) FROM projects));
SELECT setval('tickets_id_seq',       (SELECT MAX(id) FROM tickets));
SELECT setval('comments_id_seq',      (SELECT MAX(id) FROM comments));