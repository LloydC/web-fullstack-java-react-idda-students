# IronBoard — Step 11: User Management

IronBoard adds user registration with BCrypt password hashing. Users have an email (unique login identifier), a hashed password, and a role (USER or ADMIN).

## What changed from Step 10

| File | Change |
|------|--------|
| `entity/User.java` | NEW — email, password (BCrypt hash), fullName, UserRole |
| `entity/UserRole.java` | NEW — enum: USER, ADMIN |
| `repository/UserRepository.java` | NEW — findByEmail, existsByEmail |
| `service/UserService.java` | NEW — register (hash password, check duplicate email) |
| `dto/request/RegisterRequest.java` | NEW — @Email, @NotBlank, @Size(min=6) on password |
| `dto/response/UserResponse.java` | NEW — id, fullName, email, role (no password) |
| `mapper/UserMapper.java` | NEW — toResponse (never includes password) |
| `controller/AuthController.java` | NEW — POST /api/auth/register (201 Created) |
| `config/SecurityConfig.java` | + PasswordEncoder bean (BCryptPasswordEncoder) |

## Setup

```sql
DROP DATABASE IF EXISTS ironboard;
CREATE DATABASE ironboard;
```

```bash
mvn clean compile
mvn spring-boot:run
```

## Concept: BCrypt password hashing

Passwords are never stored as plain text. `BCryptPasswordEncoder.encode()` generates a unique hash every time — even the same password produces different hashes because BCrypt adds a random salt. To verify a password, use `passwordEncoder.matches(rawPassword, hash)` — never compare strings directly.

## Register a user

```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "fullName": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Check (201 Created):**
```json
{
  "id": 1,
  "fullName": "John Doe",
  "email": "john@example.com",
  "role": "USER"
}
```

- Password is NOT in the response — the mapper deliberately excludes it
- Role defaults to USER
- Check the database: `SELECT email, password, role FROM users` — password is a BCrypt hash starting with `$2a$`

## Register with duplicate email

```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "fullName": "Jane Doe",
  "email": "john@example.com",
  "password": "different123"
}
```

**Check:** 400 Bad Request — "Email already registered: john@example.com"

## Register with invalid data

```
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "not-an-email",
  "password": "123"
}
```

**Check:** 400 Bad Request with field-level errors from @Valid.
