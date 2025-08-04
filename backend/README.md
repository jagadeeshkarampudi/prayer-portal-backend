# Prayer Portal Backend

A comprehensive Spring Boot backend API for the Prayer Portal application, providing authentication, prayer request management, group functionality, and administrative features.

## Features

- **User Authentication**: JWT-based authentication with role-based access control (USER/ADMIN)
- **Prayer Request Management**: Create, update, delete, and manage prayer requests with visibility controls
- **Prayer Tracking**: Users can pray for requests and track prayer counts
- **Comments System**: Users can comment on prayer requests for encouragement
- **Groups/Communities**: Create and manage prayer groups with group-specific requests
- **Notifications**: In-app notification system for prayers and comments
- **User Profiles**: User profile management and customization
- **Answered Prayers**: Mark requests as answered and view testimonies
- **Admin Controls**: User management, content moderation, and analytics
- **Resource Library**: Manage devotionals, prayer guides, and spiritual content
- **Privacy Controls**: Fine-grained visibility settings for prayer requests

## Tech Stack

- **Framework**: Spring Boot 3.2.0
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT
- **Build Tool**: Maven
- **Java Version**: 17+

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+

## Setup Instructions

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE prayer_portal;
CREATE USER prayer_user WITH PASSWORD 'prayer_password';
GRANT ALL PRIVILEGES ON DATABASE prayer_portal TO prayer_user;
```

### 2. Environment Configuration

Create environment variables or update `application.yml`:

```bash
export DB_USERNAME=prayer_user
export DB_PASSWORD=prayer_password
export JWT_SECRET=mySecretKey
export MAIL_HOST=smtp.gmail.com
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

### 3. Build and Run

```bash
# Clone the repository and navigate to backend directory
cd backend

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

### 4. Initial Setup

The application will automatically create the database schema on startup. You can create an admin user by:

1. Register a new user via `/api/auth/signup`
2. Update the user's role to ADMIN in the database:
   ```sql
   UPDATE users SET role = 'ADMIN' WHERE username = 'your-username';
   ```

## API Documentation

### Authentication Endpoints

- `POST /api/auth/signup` - Register a new user
- `POST /api/auth/signin` - Login user

### Prayer Request Endpoints

- `GET /api/prayer-requests` - Get all visible prayer requests
- `POST /api/prayer-requests` - Create a new prayer request
- `GET /api/prayer-requests/{id}` - Get specific prayer request
- `PUT /api/prayer-requests/{id}` - Update prayer request
- `DELETE /api/prayer-requests/{id}` - Delete prayer request
- `POST /api/prayer-requests/{id}/pray` - Pray for a request
- `POST /api/prayer-requests/{id}/answer` - Mark as answered
- `GET /api/prayer-requests/my-requests` - Get user's prayer requests
- `GET /api/prayer-requests/answered` - Get answered prayers

### Comment Endpoints

- `GET /api/comments/prayer-request/{id}` - Get comments for a prayer request
- `POST /api/comments/prayer-request/{id}` - Add comment to prayer request
- `PUT /api/comments/{id}` - Update comment
- `DELETE /api/comments/{id}` - Delete comment

### Group Endpoints

- `GET /api/groups` - Get all groups
- `POST /api/groups` - Create a new group
- `GET /api/groups/{id}` - Get specific group
- `PUT /api/groups/{id}` - Update group
- `DELETE /api/groups/{id}` - Delete group
- `POST /api/groups/{id}/join` - Join a group
- `POST /api/groups/{id}/leave` - Leave a group
- `GET /api/groups/my-groups` - Get user's groups
- `GET /api/groups/{id}/prayers` - Get group's prayer requests

### Notification Endpoints

- `GET /api/notifications` - Get user's notifications
- `GET /api/notifications/unread-count` - Get unread notification count
- `PATCH /api/notifications/{id}/mark-read` - Mark notification as read
- `PATCH /api/notifications/mark-all-read` - Mark all notifications as read
- `DELETE /api/notifications/clear-read` - Clear read notifications

### User Endpoints

- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `PATCH /api/users/change-password` - Change password
- `GET /api/users/{id}` - Get user by ID

### Resource Endpoints

- `GET /api/resources` - Get all resources
- `GET /api/resources/{id}` - Get specific resource
- `GET /api/resources/types` - Get resource types

### Admin Endpoints

- `GET /api/admin/analytics` - Get system analytics
- `GET /api/admin/users` - Get all users
- `PATCH /api/admin/users/{id}/toggle-status` - Enable/disable user
- `PATCH /api/admin/users/{id}/role` - Update user role
- `GET /api/admin/prayer-requests` - Get all prayer requests
- `DELETE /api/admin/prayer-requests/{id}` - Delete prayer request
- `DELETE /api/admin/comments/{id}` - Delete comment
- `GET /api/admin/resources` - Get all resources (admin)
- `POST /api/admin/resources` - Create resource
- `PUT /api/admin/resources/{id}` - Update resource
- `DELETE /api/admin/resources/{id}` - Delete resource

## Configuration

### Database Configuration

The application is configured to use PostgreSQL. Update `application.yml` for different database settings:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/prayer_portal
    username: ${DB_USERNAME:prayer_user}
    password: ${DB_PASSWORD:prayer_password}
```

### Security Configuration

JWT configuration can be customized:

```yaml
app:
  jwt:
    secret: ${JWT_SECRET:mySecretKey}
    expiration: ${JWT_EXPIRATION:86400000} # 24 hours
```

### CORS Configuration

Update allowed origins for frontend connectivity:

```yaml
app:
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:3000,http://localhost:5173}
```

## Development

### Running Tests

```bash
mvn test
```

### Development Profile

For development, you can use H2 database by adding this to `application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
  h2:
    console:
      enabled: true
```

### API Testing

Use tools like Postman or curl to test the API endpoints. Import the API collection for easy testing.

## Production Deployment

### Environment Variables

Set these environment variables for production:

```bash
DB_USERNAME=your_production_db_user
DB_PASSWORD=your_production_db_password
JWT_SECRET=your_strong_jwt_secret
MAIL_HOST=your_smtp_host
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_email_password
CORS_ORIGINS=https://your-frontend-domain.com
```

### Database Migration

The application uses Hibernate DDL auto-update. For production, consider using Flyway or Liquibase for controlled migrations.

### Security Considerations

1. Use strong JWT secret
2. Enable HTTPS in production
3. Configure proper CORS origins
4. Set up proper database user permissions
5. Enable application security headers

## Troubleshooting

### Common Issues

1. **Database Connection Issues**: Verify PostgreSQL is running and credentials are correct
2. **JWT Token Issues**: Check JWT secret configuration
3. **CORS Issues**: Verify allowed origins in configuration
4. **Email Issues**: Check SMTP configuration and credentials

### Logging

Enable debug logging for troubleshooting:

```yaml
logging:
  level:
    com.prayerportal: DEBUG
    org.springframework.security: DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.