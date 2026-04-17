# IronBoard — Step 13: JWT Token Provider

IronBoard adds JWT token generation. Register and login now return a JWT token. HTTP Basic still works for other endpoints — Day 6 replaces it with JWT-based authentication.

## What changed from Step 12

| File | Change |
|------|--------|
| `pom.xml` | + com.auth0:java-jwt:4.5.1 |
| `application.properties` | + jwt.secret, jwt.expiration |
| `security/JwtTokenProvider.java` | NEW — generateToken, validateToken, getEmailFromToken, getRoleFromToken |
| `dto/request/LoginRequest.java` | NEW — email + password |
| `dto/response/AuthResponse.java` | NEW — token + email + role |
| `controller/AuthController.java` | Register now returns AuthResponse with token. + POST /api/auth/login |
| `config/SecurityConfig.java` | + AuthenticationManager bean |

## Setup

```sql
DROP DATABASE IF EXISTS ironboard;
CREATE DATABASE ironboard;
```

```bash
mvn clean compile
mvn spring-boot:run
```

## Register (now returns a token)

```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "fullName": "Alice User",
  "email": "alice@example.com",
  "password": "password123"
}
```

**Check (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "alice@example.com",
  "role": "USER"
}
```

Copy the token — you'll use it on Day 6 when JWT replaces HTTP Basic.

## Login

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "password123"
}
```

**Check (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "alice@example.com",
  "role": "USER"
}
```

## Login with wrong credentials

```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "wrongpassword"
}
```

**Check:** 401 Unauthorized. The error message is the same whether the email doesn't exist or the password is wrong — this prevents attackers from discovering which emails are registered.

## Decode the token

Paste the token at [jwt.io](https://jwt.io). You'll see:

```json
{
  "sub": "alice@example.com",
  "role": "USER",
  "iat": 1700000000,
  "exp": 1700003600
}
```

The token contains the user's email and role. It's signed but NOT encrypted — anyone can read the payload.

## HTTP Basic still works

At this step, all other endpoints still use HTTP Basic (from Day 4). The JWT token is generated but not yet validated on requests. Day 6 adds the JWT filter that replaces HTTP Basic.
