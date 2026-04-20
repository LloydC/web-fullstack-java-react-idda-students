# IronBoard — Step 14: JWT Authentication Filter + CORS

IronBoard completes the security layer. The JWT filter validates tokens on every request. HTTP Basic is removed — Bearer tokens are now the only authentication mechanism. CORS is configured for frontend integration.

## What changed from Step 13

| File | Change |
|------|--------|
| `security/JwtAuthenticationFilter.java` | NEW — extracts Bearer token, validates, sets SecurityContext |
| `config/CorsConfig.java` | NEW — allows cross-origin requests from localhost:3000 |
| `config/SecurityConfig.java` | REWRITE — removed HTTP Basic, + stateless sessions, + JWT filter, + CORS, + 401 entry point |

## Breaking change

**HTTP Basic no longer works.** All authenticated requests must include a Bearer token in the Authorization header. In Postman: Authorization tab → Bearer Token → paste the token from login.

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

```
POST http://localhost:8080/api/auth/register
{
  "fullName": "Alice User",
  "email": "alice@example.com",
  "password": "password123"
}
```

**Save the token from the response** — you need it for all authenticated requests below.

Make a second user and promote to admin:

```
POST http://localhost:8080/api/auth/register
{
  "fullName": "Bob Admin",
  "email": "bob@example.com",
  "password": "password123"
}
```

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'bob@example.com';
```

Login as Bob to get an admin token:

```
POST http://localhost:8080/api/auth/login
{
  "email": "bob@example.com",
  "password": "password123"
}
```

**Save Bob's token separately.**

## Test JWT authentication

### GET without token (public — still works)

```
GET http://localhost:8080/api/projects
```

**Check:** 200 OK — GET endpoints are public.

### POST without token (blocked)

```
POST http://localhost:8080/api/projects/development
{
  "name": "Test Project",
  "description": "Testing JWT",
  "category": "WEB",
  "priority": "HIGH",
  "techStack": "Java"
}
```

**Check:** 401 Unauthorized — no token, no access.

### POST with Bearer token (works)

Same request, but in Postman: Authorization tab → Bearer Token → paste Alice's token.

**Check:** 201 Created. The project is created with Alice as the owner.

### POST with HTTP Basic (no longer works)

Try the same request with Authorization: Basic Auth → alice@example.com / password123.

**Check:** 401 Unauthorized — HTTP Basic is removed. Only Bearer tokens work now.

## Test authorization with JWT

### DELETE own project as Alice (allowed)

```
DELETE http://localhost:8080/api/projects/1
Authorization: Bearer <alice-token>
```

**Check:** 204 No Content.

### DELETE someone else's project (blocked)

Create a project as Bob, try to delete it as Alice.

**Check:** 403 Forbidden — Alice is not the owner and not an admin.

### DELETE as Admin (allowed)

```
DELETE http://localhost:8080/api/projects/2
Authorization: Bearer <bob-admin-token>
```

**Check:** 204 No Content — admin can delete anything.

## How the JWT filter works

Every request goes through this flow:

```
Request arrives
  → JwtAuthenticationFilter checks Authorization header
  → If "Bearer <token>" found:
      → Validates token (signature + expiration)
      → Extracts email + role from claims
      → Sets SecurityContext (user is now "authenticated")
  → If no token or invalid token:
      → Does nothing (request continues as anonymous)
  → SecurityConfig checks authorization rules
      → permitAll? → allowed
      → authenticated? → check if SecurityContext has a user
      → hasRole("ADMIN")? → check the role in SecurityContext
  → @PreAuthorize checks ownership if configured
  → Controller method runs (or 401/403 is returned)
```

## CORS

`CorsConfig` allows a frontend at `http://localhost:3000` to call the API. Without CORS configuration, browsers block cross-origin requests. The SecurityConfig integrates CORS via `.cors(Customizer.withDefaults())` so CORS headers are processed before security rules.
