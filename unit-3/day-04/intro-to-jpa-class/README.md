# IronBoard — Step 04: Introduction to JPA

IronBoard with JPA entity annotations added to Project and Task. Services still use HashMap storage — Hibernate creates the database tables on startup but the app reads/writes from memory. Repositories are introduced on Day 5.

## What changed from Unit 2

| File | Change |
|------|--------|
| `pom.xml` | Added spring-boot-starter-data-jpa + mysql-connector-j |
| `application.properties` | MySQL connection + JPA config |
| `entity/Project.java` | @Entity, @Table, @Id, @GeneratedValue, @Column |
| `entity/Task.java` | Same + @Enumerated(EnumType.STRING) |
| Package `model/` | Renamed to `entity/` |
| Services | UNCHANGED (still HashMap — see comments) |

## Run

```bash
mvn clean compile
mvn spring-boot:run
```

## Version

- Teacher version: heavily commented (PATTERN/WHY/COMMON MISTAKE/TIP)
