# Project Management Backend

Spring Boot 3.3.4 (Java 21) REST API for workspaces, projects, tickets, sprints,
timesheets, and notifications. Persistence is PostgreSQL with Flyway. Auth is
JWT access tokens plus rotating refresh tokens.

## Requirements

- JDK 21
- Maven 3.9+
- PostgreSQL 14+

## Configuration

Values below are from `src/main/resources/application.properties`. Spring Boot
relaxed binding still lets you override them with environment variables
(for example `SPRING_DATASOURCE_URL`, `JWT_SECRET`, `APP_CORS_ALLOWED_ORIGINS`).

The names `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` are **not** wired in the main
app properties. They are only used in the **test** profile
(`src/test/resources/application-test.properties`).

| Property                                                   | Local default                                           | Env example                                | Purpose                                           |
| ---------------------------------------------------------- | ------------------------------------------------------- | ------------------------------------------ | ------------------------------------------------- |
| `server.port`                                              | `8080`                                                  | `SERVER_PORT`                              | HTTP port                                         |
| `spring.datasource.url`                                    | `jdbc:postgresql://10.180.102.238:5432/pmt`             | `SPRING_DATASOURCE_URL`                    | Postgres JDBC URL                                 |
| `spring.datasource.username`                               | `postgres`                                              | `SPRING_DATASOURCE_USERNAME`               | DB user                                           |
| `spring.datasource.password`                               | `<required secret>`                                     | `DB_PASSWORD`                              | Production DB password                            |
| `jwt.secret`                                               | committed HMAC key                                      | `JWT_SECRET`                               | Access-token signing key (override outside local) |
| `jwt.expiration`                                           | `86400000` (24h, ms)                                    | `JWT_EXPIRATION`                           | Access-token lifetime                             |
| `jwt.issuer`                                               | `project-management-api`                                | `JWT_ISSUER`                               | JWT issuer claim                                  |
| `auth.refresh-expiration-days`                             | `7`                                                     | `AUTH_REFRESH_EXPIRATION_DAYS`             | Refresh-token lifetime                            |
| `auth.password-reset-expiration-minutes`                   | `10`                                                    | `AUTH_PASSWORD_RESET_EXPIRATION_MINUTES`   | Reset token lifetime                              |
| `auth.email-verification-expiration-hours`                 | `24`                                                    | `AUTH_EMAIL_VERIFICATION_EXPIRATION_HOURS` | Email-verify token lifetime                       |
| `auth.login.max-attempts`                                  | `5`                                                     | `AUTH_LOGIN_MAX_ATTEMPTS`                  | Failed-login cap per window                       |
| `auth.login.window-minutes`                                | `15`                                                    | `AUTH_LOGIN_WINDOW_MINUTES`                | Login rate-limit window                           |
| `app.cors.allowed-origins`                                 | `http://10.180.102.238:5173,http://10.180.102.238:3000` | `APP_CORS_ALLOWED_ORIGINS`                 | Browser origins                                   |
| `app.frontend-url`                                         | `http://10.180.102.238:5173`                            | `APP_FRONTEND_URL`                         | Links in mail templates                           |
| `app.mail.enabled`                                         | `false`                                                 | `APP_MAIL_ENABLED`                         | SMTP for reset/verify/ticket mail                 |
| `app.mail.from`                                            | `no-reply@projectmanagement.local`                      | `APP_MAIL_FROM`                            | From address                                      |
| `app.notification.email.enabled`                           | `false`                                                 | `APP_NOTIFICATION_EMAIL_ENABLED`           | Ticket notification emails                        |
| `app.notification.web-push.enabled`                        | `false`                                                 | `APP_NOTIFICATION_WEB_PUSH_ENABLED`        | Browser push                                      |
| `app.security.trust-forwarded-headers`                     | `false`                                                 | `APP_SECURITY_TRUST_FORWARDED_HEADERS`     | Trust `X-Forwarded-For` only behind a proxy       |
| `VAPID_PUBLIC_KEY` / `VAPID_PRIVATE_KEY` / `VAPID_SUBJECT` | empty / mailto default                                  | same                                       | Required when web push is enabled                 |

Profiles:

- Default / `dev` (`application-dev.properties`): verbose SQL and error bodies
- `prod`: quiet logs, no stack traces in HTTP errors
- `test`: `pmt_test` via `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`

Do not ship the committed `jwt.secret` or local DB password.

## Running locally

```bash
createdb pmt
mvn spring-boot:run
```

Flyway runs `src/main/resources/db/migration` on startup. The default admin is
seeded in `V20__create_default_admin_user.sql`:

- email: `admin@gmail.com`
- password: `password` (bcrypt hash stored in that migration)

Change that account if this database is shared.

Uploads: multipart max 50 MB per file / 100 MB per request.

## Workspaces and issues

Projects belong to a workspace. After Flyway, existing rows sit on a `default`
workspace. Create one with `POST /api/workspaces`, then send `workspaceId` on
project create/update.

Workspace owners/admins manage members at `/api/workspaces/{id}/members`.
Transfer ownership with `POST /api/workspaces/{id}/ownership/{newOwnerId}`.

Project member roles: `ADMIN`, `MEMBER`, `VIEWER`.

Sub-tasks: set `parentId` on a ticket. Root issues:
`GET /api/tickets/project/{projectId}/root`. Children:
`GET /api/tickets/{id}/children`.

Status categories: `BACKLOG`, `TODO`, `IN_PROGRESS`, `DONE`, `CANCELLED`.
Move a ticket with `PUT /api/tickets/{id}/transition` and `{ "statusId": ... }`.
Kanban columns: `GET /api/tickets/project/{projectId}/board`.

Bulk plan/rank: `PUT /api/tickets/project/{projectId}/plan` with `ticketIds`,
optional `statusId`, `sprintId`, or `moveToBacklog: true`.

Filter: `GET /api/tickets/project/{projectId}/filter` (`q`, `statusId`,
`priorityId`, `responsibleId`, `sprintId`, `epicId`, `labelId`, `rootOnly`,
`page`, `size`, `sort`, `direction`). Saved views:
`/api/projects/{projectId}/ticket-views`.

Comment `userId` is ignored; the author is the authenticated user. Assignees,
subscribers, and `@email` mentions get in-app notifications.

Analytics: `GET /api/projects/{projectId}/analytics`.

## Sprints

`POST /api/sprints/{id}/complete-with-carry-over` completes an active sprint and
sends unfinished issues to the backlog unless `carryOverSprintId` is set.
`GET /api/sprints/{id}/burndown` returns daily remaining/completed points.
Completion writes an issue snapshot for historical charts.

## Audit and realtime

- `GET /api/projects/{projectId}/audit-events`
- `GET /api/tickets/{ticketId}/audit-events`
- SSE `GET /api/realtime/projects/{projectId}` (ticket/comment events)
- SSE `GET /api/realtime/notifications` (current user only)

Streams time out after 30 minutes; reconnect. EventSource clients should pass
the access token as `access_token` query param (or `Authorization: Bearer` for
non-EventSource clients).

## Tests

```bash
mvn test
```

Unit tests cover JWT validation and project access. Integration-style tests
need Postgres matching the `test` profile (`pmt_test` by default).

## Building

```bash
mvn clean package
java -jar target/project-management-backend-0.0.1-SNAPSHOT.jar
```

## API index

Public (no JWT): login, signup, refresh, password-reset request/confirm,
email-verification confirm, and `GET /api/web-push/vapid-public-key`.

Everything else needs `Authorization: Bearer <accessToken>`.

Full route list, error body, mail, and push setup: [API.md](API.md).

## LAN / SAME-NETWORK ACCESS

This project is configured so the PC running both Spring Boot and Vite can be accessed from another PC on the same LAN.

1. Put the PC's LAN IPv4 address in `backend/.env`:
   - `APP_FRONTEND_URL=http://<LAN_IP>:5173`
   - `APP_CORS_ALLOWED_ORIGINS=http://<LAN_IP>:5173,http://localhost:5173,http://127.0.0.1:5173`
2. Put the same LAN IP in `frontend/.env`:
   - `VITE_API_BASE_URL=http://<LAN_IP>:8080/api`
3. Start the backend normally. `SERVER_ADDRESS=0.0.0.0` makes it listen on the LAN interface.
4. Start the frontend with `npm run dev`. Vite reads `VITE_DEV_HOST=0.0.0.0`.
5. From another PC on the same network open `http://<LAN_IP>:5173`.
6. If Windows Firewall blocks access, allow inbound TCP ports `5173` and `8080`.

All runtime configuration is centralized in `application.properties` + backend `.env`, and frontend `.env`. Do not commit real `.env` files.

## Production security requirements

- Never commit `.env`; inject DB, JWT, SMTP and VAPID secrets through the deployment secret manager/environment.
- Use HTTPS only and set `APP_CORS_ALLOWED_ORIGINS` to the exact production frontend origin(s).
- Set `AUTH_SIGNUP_ENABLED=false` unless public self-registration is explicitly required.
- Reset/rotate every credential that was ever present in the development `.env` before production.
- Run a dependency vulnerability scanner in CI/CD before release; the project must not rely on an offline/local `npm audit` result.

## Production security

Production deployments must provide `DB_PASSWORD`, `JWT_SECRET`, and `APP_CORS_ALLOWED_ORIGINS` as external secrets/configuration. Never commit `.env`, production secrets, database dumps, or Firebase credentials. Access tokens expire after 15 minutes by default; keep refresh tokens protected and rotate all credentials that were ever present in development repositories.
