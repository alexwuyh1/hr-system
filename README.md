# HR System (Spring Boot + SQLite)

## Run
1. Ensure JDK 17+ and Maven are installed.
2. Start the app:
   ```
   mvn spring-boot:run
   ```
3. Open `http://localhost:8080` in your browser.

## Default Admin
- Username: `admin`
- Password: `admin123`

## Project Structure
- `src/main/java/com/example/hr`: backend code (controllers, services, repositories, models)
- `src/main/resources/static`: frontend (HTML/CSS/JS)
- `src/main/resources/db/schema.sql`: SQLite schema initialization
