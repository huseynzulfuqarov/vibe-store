# vibe-store

A Spring Boot REST API for managing retail store operations, employee lifecycle, 
and an automated bonus/payroll calculation engine.

## Overview

vibe-store is a backend system built for companies that operate multiple retail 
stores under a warehouse hierarchy. It handles everything from hiring employees 
and tracking job transfers to calculating monthly payroll with a flexible, 
rule-based bonus system.

## Features

- **Store & Warehouse Management** — Create and manage stores linked to a 
  warehouse and company hierarchy
- **Employee Management** — Hire employees, assign positions, track salary 
  changes and store transfers via work history
- **Sales Tracking** — Record sales per employee per store with validation 
  against active work history
- **Grade & Bonus System** — Define reusable bonus grades with three strategies:
  - `FIXED_GRADE` — flat bonus amount per employee
  - `PERCENT_GRADE` — percentage of individual or store-wide sales
  - `GRADE_THRESHOLD` — tiered percentage bonuses within sales ranges
- **Payroll Calculation** — Monthly payroll per store or per employee, 
  with prorated salary based on days worked and automatic bonus aggregation. 
  Handles mid-month store transfers correctly.

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- MySQL
- ModelMapper
- Lombok
- Jakarta Validation

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/stores` | Create a store |
| GET | `/api/stores/{id}` | Get store by ID |
| POST | `/api/employees` | Hire an employee |
| POST | `/api/employees/changeJobDetails` | Transfer or update job details |
| POST | `/api/grades` | Create a grade |
| POST | `/api/grades/assign` | Assign a grade to a store or employee |
| POST | `/api/sales` | Record a sale |
| POST | `/api/payroll/store/{storeId}/calculate` | Calculate payroll for a store |
| POST | `/api/payroll/employee/{employeeId}/calculate` | Calculate payroll for an employee |
