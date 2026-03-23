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

## Roles and Permissions (Simple)
- ADMIN: full access, including user role management
- HR: employee + attendance + reports
- FINANCE: salary + reports
- MANAGER: view employees + attendance + reports
- USER: minimal access (login only by default)

Permissions are now stored in the database and can be managed from the UI
under the "权限管理" tab (admin only).

## Organization Structure
- Departments: tree structure with parent department
- Positions: job titles catalog
- Grades: level catalog
- Direct manager: employee self-reference

## Project Structure
- `src/main/java/com/example/hr`: backend code (controllers, services, repositories, models)
- `src/main/resources/static`: frontend (HTML/CSS/JS)
- `src/main/resources/db/schema.sql`: SQLite schema initialization
