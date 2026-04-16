# IronBoard — Step 12: Role-Based Authorization

IronBoard protects endpoints with authentication and authorization rules. Projects now have an owner. PUT/DELETE require ownership or ADMIN role. HTTP Basic authentication is used temporarily (JWT replaces it on Day 6).

## What changed from Step 11

| File | Change |
|------|--------|
| `security/CustomUserDetailsService.java` | NEW — bridges User entity to Spring Security (loads by email) |
| `entity/Project.java` | + @ManyToOne User owner |
| `service/ProjectService.java` | + isOwner() method, create methods now set owner from authenticated user |
| `controller/ProjectController.java` | + Authentication param on POST, + @PreAuthorize on PUT/PATCH/DELETE |
| `controller/TaskController.java` | + @PreAuthorize("hasRole('ADMIN')") on DELETE |
| `config/SecurityConfig.java` | REWRITE — + HTTP Basic, + @EnableMethodSecurity, + URL auth rules |

## Security matrix

| Endpoint | Method | Access |
|----------|--------|--------|
| `/api/auth/**` | * | Public |
| `/api/projects` | GET | Public |
| `/api/projects` | POST | Authenticated |
| `/api/projects/{id}` | PUT/PATCH | Owner or Admin |
| `/api/projects/{id}` | DELETE | Owner or Admin |
| `/api/tasks` | GET | Public |
| `/api/tasks` | POST | Authenticated |
| `/api/tasks/{id}` | DELETE | Admin only |

## Setup

```sql
DROP DATABASE IF EXISTS ironboard;
CREATE DATABASE ironboard;
```

```bash
mvn clean compile
mvn spring-boot:run
```

### Create test data

First register two users (one regular, one admin — change role directly in the database):

```
POST http://localhost:8080/api/auth/register
{
  "fullName": "Alice User",
  "email": "alice@example.com",
  "password": "password123"
}

POST http://localhost:8080/api/auth/register
{
  "fullName": "Bob Admin",
  "email": "bob@example.com",
  "password": "password123"
}
```

Make Bob an admin in the database:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'bob@example.com';
```

## Concept: HTTP Basic Authentication

For now, endpoints are protected with HTTP Basic — credentials sent in every request header. In Postman: Authorization tab → Basic Auth → enter email + password.

This is temporary. Day 6 replaces HTTP Basic with JWT tokens.

## Test authentication

### GET without credentials (public)

```
GET http://localhost:8080/api/projects
```

**Check:** 200 OK — GET endpoints are public.

### POST without credentials (blocked)

```
POST http://localhost:8080/api/projects/development
{
  "name": "Test Project",
  "description": "Testing auth",
  "category": "WEB",
  "priority": "HIGH",
  "techStack": "Java"
}
```

**Check:** 401 Unauthorized — POST requires authentication.

### POST with Basic Auth (works)

Same request with Authorization: Basic Auth → email: `alice@example.com`, password: `password123`

**Check:** 201 Created. Response includes `ownerEmail: "alice@example.com"` — the project owner is set from the authenticated user.

## Test authorization (ownership)

### DELETE own project as Alice (allowed)

```
DELETE http://localhost:8080/api/projects/1
Authorization: Basic alice@example.com / password123
```

**Check:** 204 No Content — Alice owns project 1, so she can delete it.

### DELETE someone else's project as Alice (blocked)

Create a project as Bob, then try to delete it as Alice:

**Check:** 403 Forbidden — Alice is not the owner and not an admin.

### DELETE any project as Bob/Admin (allowed)

```
DELETE http://localhost:8080/api/projects/2
Authorization: Basic bob@example.com / password123
```

**Check:** 204 No Content — Bob is ADMIN, can delete anything.
