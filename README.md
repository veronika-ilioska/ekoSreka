# ЕкоСреќа Fullstack

ЕкоСреќа е full-stack еко апликација со Spring Boot backend, PostgreSQL база и Vue 3 frontend. Апликацијата има вести, квизови, профил, поставки, забавна секција, продавница и admin UI за додавање/бришење содржина.

## Stack

**Backend**
- Java 17
- Spring Boot 3.5.6
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway migrations
- Maven

**Frontend**
- Vue 3
- Vite
- Pinia
- Axios
- Bootstrap 5

## Project Structure

```text
.
├── src/                         # Spring Boot backend
├── src/main/resources/db/migration
│   ├── V2__create_admins_and_seed_admin.sql
│   └── V3__delete_existing_news.sql
├── ekosreka-frontend/           # Main Vue frontend
├── client/                      # Older/duplicate frontend copy
├── pom.xml
├── mvnw.cmd
└── .env                         # Local-only values, ignored by git
```

Use `ekosreka-frontend` for frontend work.

## Prerequisites

- Java 17+
- Node.js 18+ with npm
- PostgreSQL 12+
- Maven wrapper from this repo

## Local Database

Create the local database:

```powershell
psql -h localhost -p 5432 -U postgres -c "CREATE DATABASE ""EkoSrekja"";"
```

Default connection values:

```text
Host: localhost
Port: 5432
Database: EkoSrekja
Username: postgres
Password: postgre
```

The backend currently reads these values from:

```text
src/main/resources/application.properties
```

The root `.env` is ignored by git and keeps the same local values in one place.

## Run Backend

From the repository root:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend URL:

```text
http://localhost:8082
```

API base URL:

```text
http://localhost:8082/api
```

## Run Frontend

In a second terminal:

```powershell
cd ekosreka-frontend
npm install
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

## Build

Backend:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Frontend:

```powershell
cd ekosreka-frontend
npm run build
```

## Admin Test User

The admin table and test admin are created through Flyway migration:

```text
src/main/resources/db/migration/V2__create_admins_and_seed_admin.sql
```

Test admin:

```text
Username: admin_test
Password: Admin123!
Email: admin@test.local
```

After logging in, open:

```text
http://localhost:5173/admin
```

Admins can add/delete:
- news
- quizzes
- photos
- videos
- games
- horoscope entries

## Database Migrations

Flyway runs on backend startup.

Current migrations:
- `V2__create_admins_and_seed_admin.sql` creates the `admins` table and inserts the test admin.
- `V3__delete_existing_news.sql` removes existing news records so the news page starts empty.

## News Categories

News categories are in Macedonian:

```text
Рециклирање
Енергија
Природа
Клима
```

The public news page filters by these values, and the admin news form uses the same category list.

## Useful URLs

```text
Frontend: http://localhost:5173
Backend:  http://localhost:8082
API:      http://localhost:8082/api
Admin:    http://localhost:5173/admin
```

## Notes

- `.env` and `.env.*` are ignored by git.
- `target/`, `node_modules/`, and frontend `dist/` should not be committed.
- The site UI is currently in Macedonian.
