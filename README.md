# BookNest Backend

Spring Boot REST API for the BookNest online book library.

## Live Endpoints

- **API base URL:** https://booknestbckend.onrender.com
- **Books endpoint:** https://booknestbckend.onrender.com/api/books
- **Frontend:** https://booknest-fronted.netlify.app

## Technology Stack

- Java 21
- Spring Boot
- Maven
- Spring Security
- JWT authentication
- Spring Data JPA
- Database configuration through environment variables

## Run Locally

1. Install Java 21 and Maven.
2. Clone this repository.
3. Build the application:
   ```bash
   mvn clean package
   ```
4. Run the generated JAR (adjust the filename if your build produces a different version):
   ```bash
   java -jar target/booknest-api-1.0.0.jar
   ```
5. The API uses port 8080 by default unless `PORT` is set.

## Environment Configuration

Configure these in the deployment environment; do not commit real secrets:

| Variable | Purpose |
| --- | --- |
| `JWT_SECRET` | Secret used to sign/validate JWTs |
| `JWT_EXPIRATION_MS` | Token lifetime in milliseconds |
| `FRONTEND_ORIGINS` | Comma-separated allowed frontend origins |
| `DB_URL` | Optional JDBC connection URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |

The application configuration has an H2 file-database fallback when `DB_URL` is not supplied. For production, verify the intended database provider and persistence/storage configuration.

## API Smoke Test

Open this URL in a browser:

`https://booknestbckend.onrender.com/api/books`

A successful response is JSON containing book records. The endpoint was verified during deployment and returned five catalogue records.

## Deployment

The backend is hosted on Render. Keep `JWT_SECRET` and database credentials in Render environment variables, and ensure `FRONTEND_ORIGINS` includes the deployed frontend origin.

## Security Checklist

- [ ] Never commit JWT secrets or database passwords.
- [ ] Use a strong, private JWT secret in production.
- [ ] Restrict CORS to trusted frontend origins.
- [ ] Verify database persistence and backups before production use.
- [ ] Test authentication and protected endpoints before submission.

## Related Repository

Frontend: https://github.com/24eg107f23-gif/BookNestFronted
