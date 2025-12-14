# BookMySeat - Event Booking APIs Spring Boot Project

- This project is a Spring Boot implementation of the backend APIs for an event booking system similar to the popular platform "BookMyShow". It provides a set of RESTful APIs that enable client applications to interact with the booking system and perform various operations.
- This project is a comprehensive event booking system for theater-style seated events. Users can add events (movies, concerts, theater shows, opera, comedy, dance), venues with auditorium seating, shows, and book tickets. The system includes advanced seat locking, real-time availability, Redis caching, and Kafka notifications.

## Features
* **User Management** → Users can register, log in with JWT authentication, and manage their profile
* **Event Management** → Admin users can create, update, and delete events (movies, concerts, theater, comedy, opera, dance)
* **Venue Management** → Admin users can add venues/theaters with seating layouts (Premium/Classic seats)
* **Show Scheduling** → Create shows linking events to venues with specific date/time slots
* **Advanced Booking System** →
  - Seat locking with pessimistic locks and SERIALIZABLE isolation
  - 10-minute timeout with automatic lock release
  - Real-time seat availability
  - Prevents double-booking with ACID transactions
* **Search & Browse** → Filter events by city, date, genre, language, type
* **Redis Caching** → High-performance caching for frequently accessed event data
* **Kafka Notifications** → Asynchronous booking confirmation events
* **Security** → JWT authentication, BCrypt password hashing, role-based access control

## Technologies Used
* **Java 17+**
* **Spring Boot 3.3.0**
* **Spring Data JPA** (with Hibernate)
* **PostgreSQL** (Primary database)
* **Redis** (Caching layer)
* **Apache Kafka** (Message queue for async notifications)
* **Spring Security** (JWT authentication & authorization)
* **Testcontainers** (Integration testing)
* **Docker & Docker Compose** (Containerization)
* **Maven** (Dependency management)

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Docker & Docker Compose (for infrastructure)

### Setup Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd bookmyseat
   ```

2. **Start infrastructure services**
   ```bash
   docker-compose up -d
   ```
   This starts PostgreSQL, Redis, and Kafka.

3. **Configure application** (optional)
   - Database settings are in `src/main/resources/application.properties`
   - Default configuration works with Docker Compose

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

6. **Access the application**
   - API Base URL: `http://localhost:8081`
   - Swagger UI: `http://localhost:8081/swagger-ui.html`

## Database Setup

### Option 1: Docker Compose (Recommended)
```bash
docker-compose up -d
```
This starts PostgreSQL, Redis, and Kafka automatically.

### Option 2: Manual Setup
1. Install PostgreSQL on your local machine
2. Create a new database named `bookmyseat_db`
3. Update database credentials in `application.properties` if needed
4. Install Redis (port 6379)
5. Install Kafka (port 9092)

## API Documentation
- **Swagger UI**: `http://localhost:8081/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8081/v3/api-docs`
- **cURL Examples**: See `API_TEST_COMMANDS.md` for complete test commands

The Swagger documentation provides detailed information about each API endpoint, including:
- Request/response formats
- Authentication requirements
- Parameter validation rules
- Example payloads

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/refresh` - Refresh access token

### Events
- `POST /api/events` - Create event (Admin only)
- `GET /api/events` - Get all events
- `GET /api/events/{id}` - Get event by ID
- `GET /api/events/type/{type}` - Get events by type (MOVIE, CONCERT, etc.)
- `GET /api/events/city/{city}` - Get events by city
- `GET /api/events/genre/{genre}` - Get events by genre
- `GET /api/events/language/{language}` - Get events by language
- `GET /api/events/date/{date}` - Get events by date
- `PUT /api/events/{id}` - Update event (Admin only)
- `DELETE /api/events/{id}` - Delete event (Admin only)

**Example: Create Movie Event**
```json
{
    "name": "Inception",
    "eventType": "MOVIE",
    "duration": 148,
    "rating": 8.8,
    "releaseDate": "2024-12-20",
    "genre": "THRILLER",
    "language": "ENGLISH",
    "director": "Christopher Nolan",
    "cast": "Leonardo DiCaprio, Tom Hardy",
    "description": "A mind-bending thriller",
    "posterUrl": "https://example.com/poster.jpg",
    "city": "Mumbai"
}
```

### Venues (Theaters)
- `POST /theater/addNew` - Create venue (Admin only)
- `POST /theater/addTheaterSeat` - Add seats to venue (Admin only)

**Example: Add Venue**
```json
{
    "name": "PVR Cinemas Phoenix",
    "address": "High Street Phoenix, Lower Parel, Mumbai",
    "city": "Mumbai"
}
```

**Example: Add Seats**
```json
{
    "theaterId": 1,
    "noOfSeatInRow": 10,
    "noOfClassicSeat": 100,
    "noOfPremiumSeat": 50
}
```

### Shows
- `POST /api/shows/addNew` - Create show (Admin only)
- `POST /api/shows/associateSeats` - Set seat prices for show (Admin only)
- `GET /api/shows` - Get all shows
- `GET /api/shows/{id}` - Get show details with seat availability
- `GET /api/shows/event/{eventId}` - Get shows for an event
- `GET /api/shows/theater/{theaterId}` - Get shows at a venue
- `GET /api/shows/date/{date}` - Get shows on a date
- `GET /api/shows/grouped` - Get shows grouped by date and venue

**Example: Create Show**
```json
{
  "showStartTime": "18:00:00",
  "showDate": "2024-12-20",
  "theaterId": 1,
  "eventId": 1
}
```

**Example: Set Seat Prices**
```json
{
  "showId": 1,
  "priceOfPremiumSeat": 400,
  "priceOfClassicSeat": 200
}
```

### Users
- `POST /user/addNew` - Register user
- `GET /user/profile` - Get user profile (authenticated)

**Example: Register User**
```json
{
    "name": "John Doe",
    "emailId": "john@example.com",
    "password": "SecurePassword123!",
    "roles": "ROLE_USER"
}
```

### Booking
- `POST /ticket/book` - Book tickets (authenticated)

**Example: Book Tickets**
```json
{
    "showId": 1,
    "userId": 2,
    "requestSeats": ["1A", "1B", "2A"]
}
```

**Response:**
```json
{
    "time": "18:00:00",
    "date": "2024-12-20",
    "eventName": "Inception",
    "theaterName": "PVR Cinemas Phoenix",
    "address": "High Street Phoenix, Mumbai",
    "bookedSeats": "1A, 1B, 2A",
    "totalPrice": 600
}
```

## Key Features Explained

### Advanced Seat Locking
- Uses **SERIALIZABLE isolation** + **pessimistic locking** to prevent race conditions
- Seats locked for 10 minutes during booking process
- Automatic lock expiry with scheduled cleanup job
- Prevents double-booking across concurrent users

### Redis Caching
- Event listings cached by: type, city, genre, language, date
- Cache invalidation on event updates
- Significantly improves read performance

### Kafka Event Processing
- Asynchronous `BookingConfirmedEvent` publishing
- Email/SMS notification simulation
- Decoupled architecture - booking succeeds even if notifications fail

### Security
- JWT-based authentication with refresh tokens
- BCrypt password hashing
- Role-based access control (USER/ADMIN)
- Admin-only access for event/show/venue management

## Testing

### Run Integration Tests
```bash
mvn test
```

Tests use Testcontainers for PostgreSQL, Redis, and Kafka.

### Manual Testing
See `API_TEST_COMMANDS.md` for complete cURL commands.

## Architecture

```
Client → API Layer (Controllers)
           ↓
       Service Layer (Business Logic)
           ↓
       Repository Layer (JPA)
           ↓
       PostgreSQL Database

       Caching: Redis
       Async Events: Kafka
```

## Event Types Supported

All events feature **theater-style seating** in auditoriums/venues:

- **MOVIE** - Cinema movies (with genre support)
- **CONCERT** - Music concerts in seated venues
- **THEATER** - Theater plays and performances (with genre support)
- **COMEDY_SHOW** - Stand-up comedy shows
- **OPERA** - Opera performances (with genre support)
- **DANCE_SHOW** - Dance performances and recitals

---

