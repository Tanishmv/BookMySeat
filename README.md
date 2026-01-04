# BookMySeat - Event & Ticket Booking Platform

![Java](https://img.shields.io/badge/Java-17+-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red?style=for-the-badge&logo=redis)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.5.0-black?style=for-the-badge&logo=apache-kafka)

**Author:** Tanish M V

## Table of Contents
- [Project Overview](#project-overview)
- [Core Technologies](#core-technologies)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Conflict-Free Seat Allocation](#conflict-free-seat-allocation)
- [Database Schema](#database-schema)
- [Redis Caching](#redis-caching)
- [Project Structure](#project-structure)
- [Kafka Integration](#kafka-integration)
- [Testing](#testing)
- [Security Features](#security-features)
- [Scheduled Jobs](#scheduled-jobs)

## Project Overview

BookMYSeat is a **production-grade event booking platform** built with Spring Boot, demonstrating enterprise architecture patterns including comprehensive seat management, JWT-based authentication, and role-based access controls. The system supports multiple event types (movies, concerts, theater, comedy shows, opera, dance performances) with real-time seat allocation and conflict-free booking mechanisms.

## Core Technologies

- **Java 17+** with Spring Boot framework
- **PostgreSQL 16** for relational data persistence
- **Redis 7** for distributed caching (Jedis client)
- **Apache Kafka 7.5.0** for event-driven messaging
- **Zookeeper** for Kafka cluster coordination
- **Docker & Docker Compose** for containerization
- **Swagger/OpenAPI** for API documentation
- **Testcontainers 2.0.2** for integration testing with real infrastructure


## Key Features

1. **Multi-Event Type Support**
   - Movies, Concerts, Theater, Comedy Shows, Opera, Dance Performances
   - Flexible event metadata (genre, language, duration, rating, performers)
   - Dynamic show scheduling with venue and theater associations

2. **Advanced Seat Management**
   - Real-time availability tracking with pessimistic database locking
   - Temporary seat reservations (10-minute lock duration)
   - Automatic lock expiry via scheduled cleanup jobs
   - Dual pricing: Premium and Classic seat categories

3. **Conflict-Free Booking**
   - ACID transactions with SERIALIZABLE isolation
   - Pessimistic write locks (`SELECT ... FOR UPDATE`)
   - Optimistic locking with version control
   - Race condition prevention for concurrent bookings

4. **Authentication & Authorization**
   - Stateless JWT authentication (access + refresh tokens)
   - BCrypt password hashing
   - Role-based access control (USER/ADMIN)
   - Token revocation support

5. **Event-Driven Architecture**
   - Kafka integration for booking events
   - Asynchronous email notifications
   - Separate topics for success/failure events
   - Manual acknowledgment for reliable processing

6. **Performance Optimization**
   - Redis distributed caching with TTL-based expiry
   - Cache invalidation on entity updates

7. **Comprehensive API**
   - RESTful endpoints with Swagger documentation
   - Advanced search and filtering capabilities
   - Grouped show listings by date and venue
   - Real-time seat availability queries

---

## Technology Stack

| Layer | Technologies |
|-------|-------------|
| **Backend** | Spring Boot 3.3.3, Java 17 |
| **Security** | Spring Security, JWT (jjwt 0.11.5), BCrypt |
| **Database** | PostgreSQL 16, Spring Data JPA, Hibernate |
| **Caching** | Redis 7, Jedis Client, Spring Cache |
| **Messaging** | Apache Kafka 7.5.0, Zookeeper, Spring Kafka |
| **Documentation** | Swagger/OpenAPI (springdoc 2.5.0) |
| **Validation** | Jakarta Validation |
| **Email** | Spring Boot Mail (SMTP) |
| **Testing** | JUnit 5, Mockito, Testcontainers 2.0.2, Spring Security Test |
| **Utilities** | Lombok |
| **DevOps** | Docker, Docker Compose |
| **Build** | Maven |

---

## Architecture

![System Architecture](Architecture.png)

---

## Getting Started

### Prerequisites

- **Java 17+** (JDK required for local development)
- **Maven 3.6+** for dependency management
- **Docker 20+** and **Docker Compose V3.8+** for containerized deployment

### Quick Start (Docker)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd bookmyseat
   ```

2. **Configure environment variables**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration (JWT secret, email credentials, etc.)
   ```

3. **Start all services**
   ```bash
   docker-compose up -d
   ```

4. **Verify services are running**
   ```bash
   docker-compose ps
   ```

5. **Access the application**
   - **API Base URL**: http://localhost:8081
   - **Swagger UI**: http://localhost:8081/swagger-ui.html
   - **pgAdmin**: http://localhost:5051 (Credentials: `admin`)
   - **RedisInsight**: http://localhost:5540
   - **Kafka UI**: http://localhost:8090

### Local Development (without Docker)

1. **Start infrastructure services only**
   ```bash
   docker-compose up -d postgres redis kafka zookeeper
   ```

2. **Configure application properties**
   ```bash
   # Update src/main/resources/application.properties or use environment variables
   ```

3. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Run tests**
   ```bash
   mvn test
   ```

---

## API Endpoints

### Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login with email/password, returns JWT access & refresh tokens |
| POST | `/api/auth/refresh` | Refresh access token using refresh token |
| POST | `/api/auth/logout` | Invalidate refresh token and logout |

### Users

| Method | Endpoint | Description | Authorization |
|--------|----------|-------------|---------------|
| POST | `/user/addNew` | Register new user with email, password, name, phone, gender | Public |
| GET | `/user/me` | Get current user profile | USER/ADMIN |
| PUT | `/user/me` | Update user profile (name, email, phone) | USER/ADMIN |

### Events (Admin endpoints require ADMIN role)

| Method | Endpoint | Description | Authorization |
|--------|----------|-------------|---------------|
| POST | `/api/events` | Create new event | ADMIN |
| GET | `/api/events` | Search events with optional filters | Public |
| GET | `/api/events/{id}` | Get event details by ID | Public |
| PUT | `/api/events/{id}` | Update event | ADMIN |
| DELETE | `/api/events/{id}` | Delete event | ADMIN |

**Query Parameters for GET /api/events:**
- `name` - Event name (partial match)
- `city` - Filter by city
- `type` - Filter by event type (MOVIE, CONCERT, THEATER, COMEDY_SHOW, OPERA, DANCE_SHOW)
- `genre` - Filter by genre
- `language` - Filter by language
- `showDate` - Filter by show date (yyyy-MM-dd)
- `releaseDate` - Filter by release date (yyyy-MM-dd)

**Event Types:** `MOVIE`, `CONCERT`, `THEATER`, `DANCE_SHOW`, `COMEDY_SHOW`, `OPERA`

**Event Fields:**
- name, eventType, duration (minutes), rating, releaseDate
- genre, language, description, posterUrl
- artist (for concerts, comedy, dance)
- director (for movies, theater)
- performers (for movies, theater, opera)

### Shows (Admin endpoints require ADMIN role)

| Method | Endpoint | Description | Authorization |
|--------|----------|-------------|---------------|
| POST | `/api/shows/addNew` | Create show with seat prices | ADMIN |
| GET | `/api/shows` | Search shows with filters | Public |
| GET | `/api/shows/{id}` | Get show details | Public |
| GET | `/api/shows/grouped` | Get shows grouped by date & venue | Public |
| GET | `/api/shows/{id}/seats` | Get real-time seat availability | Public |
| PUT | `/api/shows/{id}` | Update show | ADMIN |
| DELETE | `/api/shows/{id}` | Delete show | ADMIN |

**Query Parameters for GET /api/shows:**
- `eventId` - Filter by event ID
- `theaterId` - Filter by theater ID
- `date` - Filter by date (yyyy-MM-dd)

### Tickets (All require authentication)

| Method | Endpoint | Description | Authorization |
|--------|----------|-------------|---------------|
| POST | `/ticket/lock-seats` | Lock seats for 10 minutes | USER/ADMIN |
| POST | `/ticket/book` | Confirm booking (must have locked seats) | USER/ADMIN |
| POST | `/ticket/release-seats` | Manually release locked seats | USER/ADMIN |
| GET | `/ticket/me` | Get my booking history | USER/ADMIN |
| GET | `/ticket/{ticketId}` | Get ticket details by ID | USER/ADMIN |
| DELETE | `/ticket/{ticketId}` | Cancel ticket and release seats | USER/ADMIN |

### Venues (Admin endpoints require ADMIN role)

| Method | Endpoint | Description | Authorization |
|--------|----------|-------------|---------------|
| POST | `/venue/addNew` | Create venue with city, address | ADMIN |
| GET | `/venue` | Get all venues (optional ?city=X filter) | Public |
| GET | `/venue/{id}` | Get venue details by ID | Public |
| PUT | `/venue/{id}` | Update venue | ADMIN |
| DELETE | `/venue/{id}` | Delete venue | ADMIN |

### Theaters (Admin endpoints require ADMIN role)

| Method | Endpoint | Description | Authorization |
|--------|----------|-------------|---------------|
| POST | `/theater/addNew` | Create theater with seat layout (rows, columns, seat types) | ADMIN |
| GET | `/theater` | Get all theaters | Public |
| GET | `/theater/{id}` | Get theater details with seat layout | Public |
| PUT | `/theater/{id}` | Update theater | ADMIN |
| DELETE | `/theater/{id}` | Delete theater | ADMIN |

**Theater Creation:**
- Associated with a venue
- Define seat layout: number of rows, seats per row
- Assign seat types (PREMIUM, CLASSIC) to specific seats
- Seats are identified by row-number format (e.g., "1A", "2B")

---

## Conflict-Free Seat Allocation

### How It Works

1. **Pessimistic Database Locking**
   ```java
   @Lock(LockModeType.PESSIMISTIC_WRITE)
   @Query("SELECT ss FROM ShowSeat ss WHERE ss.id IN :seatIds")
   List<ShowSeat> findAndLockByIds(@Param("seatIds") List<Integer> seatIds);
   ```
   Executes `SELECT ... FOR UPDATE` to lock rows at database level, preventing concurrent modifications.

2. **Transaction Isolation**
   ```java
   @Transactional(isolation = Isolation.SERIALIZABLE)
   public SeatLockResponse lockSeats(SeatLockRequest request) {
       // Lock seats for specific user
   }
   ```
   Uses highest isolation level to prevent phantom reads and concurrent modifications.

3. **Optimistic Locking**
   ```java
   @Version
   private Long version;  // Automatically managed by JPA
   ```
   Detects concurrent modifications and prevents lost updates.

4. **Seat States & Lifecycle**
   - `AVAILABLE` - Can be locked by any user
   - `LOCKED` - Held for specific user with timestamp (10-minute duration)
   - `BOOKED` - Permanently reserved after payment confirmation

5. **Automatic Cleanup**
   - Scheduled job runs every 2 minutes (`@Scheduled`)
   - Releases locks older than 10 minutes
   - Sets seats back to `AVAILABLE` status
   - Clears `lockedAt` timestamp and `lockedByUserId`

### Booking Flow Sequence

![Booking Sequence Diagram](Sequence_diagram.png)

1. **User browses events** → Selects event → Views available shows
2. **User selects show** → Views real-time seat availability
3. **User selects seats** → Calls `POST /ticket/lock-seats` with seat IDs
4. **System locks seats** → Marks seats as LOCKED with user ID and timestamp
5. **User confirms booking** → Calls `POST /ticket/book` within 10 minutes
6. **System creates ticket** → Marks seats as BOOKED, publishes Kafka event, sends email
7. **Optional: User cancels** → Calls `POST /ticket/release-seats` or waits for auto-expiry

### Error Handling

- **Seats already locked/booked**: Returns error with unavailable seat details
- **Lock expired**: User must re-lock seats before booking
- **Concurrent booking attempts**: Database locks prevent double-booking
- **Invalid seat selection**: Validates seat existence and show association

---

## Database Schema

![Entity Relationship Diagram](ERD.png)

**Core Entities:**
- **EVENTS**: Event details (name, type, genre, language, duration, rating, performers)
- **SHOWS**: Event instances at specific date/time/theater
- **VENUES**: Physical locations with multiple theaters
- **THEATERS**: Auditoriums with seat layouts
- **SHOW_SEATS**: Per-show seat inventory (price, status, locks)
- **TICKETS**: Booking records with seat associations
- **USERS**: User accounts with authentication
- **REFRESH_TOKENS**: JWT refresh token management

---

## Redis Caching

### Cached Data

| Data | TTL | Eviction Trigger |
|------|-----|------------------|
| All events | 2 hours | Event create/update/delete |
| Event by ID | 2 hours | Event update/delete |
| Events by type/city/genre/language | 1 hour | Event changes |
| All shows | 30 min | Show create/update/delete |
| Show by ID | 15 min | Show update, seat lock/book |
| Shows by event/theater/date | 30 min | Show changes |

### Cache Eviction Strategy

- **Write-through caching**: Updates both cache and database
- **Automatic eviction**: Cache is invalidated on entity modifications
- **TTL-based expiry**: Stale data automatically removed
- **Manual eviction**: Admin operations trigger immediate cache clear

---

## Project Structure

```
bookmyseat/
├── src/main/java/com/sb/movie/
│   ├── config/              # Configuration classes
│   ├── controllers/         # REST API controllers
│   ├── entities/            # JPA entities
│   ├── repositories/        # Spring Data JPA repositories
│   ├── services/            # Business logic services
│   ├── converter/           # DTO converters
│   ├── enums/               # Enumerations
│   ├── events/              # Kafka event models
│   ├── request/             # Request DTOs
│   ├── response/            # Response DTOs
│   └── security/            # JWT, authentication filters
├── src/test/                # Integration and unit tests
├── docker-compose.yml       # Docker orchestration
├── Dockerfile               # Application container image
├── pom.xml                  # Maven dependencies
└── README.md                # Project documentation
```

---

## Kafka Integration

**Topics:**
- `booking-confirmed` - Successful booking events
- `booking-failed` - Failed booking attempts

**Events Published:**
- **BookingConfirmedEvent**: Booking ID, user details, show info, seats, price, timestamp
- **BookingFailedEvent**: User details, requested seats, failure reason

**Consumer Processing:**
- Sends email notifications for booking confirmations and failures
- Manual acknowledgment for reliable message processing
- Async processing - booking succeeds even if Kafka fails

---

## Testing

### Test Execution

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TicketServiceTest

# Run with coverage report
mvn clean test jacoco:report
```

### Test Results

**All 13 integration tests passing** with 100% success rate.

### Testing Strategy

The project employs **Testcontainers** for integration testing with real database infrastructure.

### Test Coverage

**✓ Authentication & Authorization:**
- User registration with validation
- Login with JWT token generation
- Token refresh and expiry handling
- Role-based access control (USER vs ADMIN)
- Invalid credentials rejection

**✓ Event & Show Management:**
- Event CRUD operations with admin authorization
- Show creation with seat pricing
- Search and filtering by multiple criteria
- Grouped show listings by date and venue

**✓ Seat Booking & Locking:**
- Seat lock acquisition with timestamp
- Booking confirmation with seat state transition
- Concurrent booking conflict prevention
- Pessimistic locking behavior validation

**✓ Error Scenarios:**
- Seat already booked error handling
- Invalid seat selection validation
- Permission denied for unauthorized operations

### Manual Testing

- **Swagger UI**: http://localhost:8081/swagger-ui.html

---

## Security Features

### Authentication & Authorization

- **BCrypt password hashing** for secure password storage
- **JWT access tokens** (30-minute expiry) for stateless authentication
- **JWT refresh tokens** (7-day expiry) with revocation support
- **Role-based access control** (USER/ADMIN roles)
- **Token blacklisting** via REFRESH_TOKENS table

### Data Protection

- **Input validation** using Jakarta Validation annotations
- **Transaction isolation** preventing race conditions
- **Optimistic locking** for concurrent update protection

---

## Scheduled Jobs

### Expired Lock Cleanup

```java
@Scheduled(fixedRate = 120000) // Every 2 minutes
public void releaseExpiredLocks() {
    // Find seats locked more than 10 minutes ago
    // Set status back to AVAILABLE
    // Clear lockedAt and lockedByUserId
}
```

**Configuration:**
- **Frequency**: Every 2 minutes
- **Lock duration**: 10 minutes
- **Action**: Release locks, update seat status, invalidate cache




