package com.ironhack.ironboard.repository;

// =============================================
// PROJECT REPOSITORY -- Step 05: Spring Data Repositories
// =============================================
// NEW: Replaces the HashMap in ProjectService.
//
// KEY CONCEPT: Spring Data JPA Repository
// By extending JpaRepository<Project, Long>, you get ALL CRUD
// operations for free -- no implementation code needed:
//
//   findAll()       -> SELECT * FROM projects
//   findById(id)    -> SELECT * FROM projects WHERE id = ?
//   save(project)   -> INSERT or UPDATE
//   deleteById(id)  -> DELETE FROM projects WHERE id = ?
//   count()         -> SELECT COUNT(*) FROM projects
//   existsById(id)  -> SELECT EXISTS(...)
//
// PATTERN: Repository = data access layer.
// Service = business logic layer.
// The service calls the repository; the controller calls the service.
//
// =============================================

import com.ironhack.ironboard.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // --- DERIVED QUERY METHODS ---
    // You declare the method signature. Spring writes the SQL.
    // The method name IS the query — Spring reads it left to right.
    //
    // ANATOMY:  <Subject> + By + <Property> [+ <Condition>] [+ And/Or + ...]
    //
    //   Subject:    find, read, get, query, count, exists, delete
    //   By:         starts the WHERE clause
    //   Property:   entity field name in camelCase (e.g., Name, Status, ProjectId)
    //   Condition:  keyword appended to the property (see below)
    //   Combinator: And / Or to chain multiple conditions
    //
    // CONDITION KEYWORDS (most common):
    //   (none)              -> =           findByStatus(s)
    //   Not                 -> <>          findByStatusNot(s)
    //   Containing          -> LIKE %x%    findByNameContaining(s)
    //   StartingWith        -> LIKE x%     findByNameStartingWith(s)
    //   EndingWith          -> LIKE %x     findByNameEndingWith(s)
    //   IgnoreCase          -> UPPER()     findByNameIgnoreCase(s)
    //   LessThan / GreaterThan             findByPriceLessThan(n)
    //   Between             -> BETWEEN     findByPriceBetween(a, b)
    //   In                  -> IN (...)    findByStatusIn(list)
    //   IsNull / IsNotNull  -> IS NULL     findByDeletedAtIsNull()
    //   True / False        -> = true      findByActiveTrue()
    //   Before / After      -> < / >       findByCreatedAtAfter(date)
    //   OrderBy...Asc/Desc  -> ORDER BY    findByStatusOrderByNameAsc(s)
    //
    // RETURN TYPES:
    //   List<T>     = zero or more rows
    //   Optional<T> = zero or one row
    //   long        = for count queries
    //   boolean     = for exists queries
    //
    // HOW TO BUILD ONE — think of it as 4 steps:
    //   Step 1: What do you want?        -> find (returns data)
    //   Step 2: Which field to filter?    -> By + Name (entity property)
    //   Step 3: How to match?             -> Containing (LIKE %...%)
    //   Step 4: Any modifier?             -> IgnoreCase (case-insensitive)
    //
    // Result: findByNameContainingIgnoreCase
    // Spring reads this and generates:
    //   WHERE UPPER(name) LIKE UPPER('%keyword%')
    List<Project> findByNameContainingIgnoreCase(String keyword);
}
