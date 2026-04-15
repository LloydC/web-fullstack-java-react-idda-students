# IronBoard — Step 10: Spring Security Setup

IronBoard adds the Spring Security framework. All endpoints remain open (permitAll) — this step sets up the infrastructure for authorization rules on Day 4.

## What changed from Step 09

| File | Change |
|------|--------|
| `pom.xml` | + `spring-boot-starter-security` |
| `config/SecurityConfig.java` | NEW — @EnableWebSecurity, CSRF disabled, all endpoints permitAll |

## What Spring Security does by default

When you add `spring-boot-starter-security` to your project WITHOUT a SecurityConfig, Spring Security:

1. Returns **401 Unauthorized** on ALL endpoints
2. Generates a **random password** at startup (visible in the console)
3. Enables **form login** at `/login` (not useful for REST APIs)

Our `SecurityConfig` overrides all of this: CSRF is disabled (REST APIs don't use browser sessions), and all requests are permitted. The framework is in place — we tighten the rules on Day 4.

## Setup

```sql
DROP DATABASE IF EXISTS ironboard;
CREATE DATABASE ironboard;
```

```bash
mvn clean compile
mvn spring-boot:run
```

## Verify

All existing endpoints work exactly as before:

```
GET http://localhost:8080/api/projects
GET http://localhost:8080/api/tasks
POST http://localhost:8080/api/projects/development
{
  "name": "IronBoard",
  "description": "Project management app",
  "category": "WEB",
  "priority": "HIGH",
  "techStack": "Java, Spring Boot"
}
```

**Check:** 200/201 responses — no 401s, no login prompts. Spring Security is active but not blocking anything.
