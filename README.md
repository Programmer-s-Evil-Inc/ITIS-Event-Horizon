# 📚ITIS-Event-Horizon

Creation of a centralized platform for students and organizers of ITIS KFU to publish, view, and manage information about extracurricular events.  

## ⚙️ Tech Stack
**Backend**:
- Java 23
- Spring Boot 3
- Spring Security
- PostgreSQL
- MinIO (объектное хранилище)
- Docker

**Frontend**:
- JavaScript
- HTML/CSS
- Bootstrap

## 🛠️ Local Development

### Prerequisites
- Docker
- Java 23

### First Launch
1) Start `docker-compose.yaml`:
2) Configure `application.properties`:
```properties
spring.jpa.hibernate.ddl-auto=update - automatically updates the database schema
spring.sql.init.mode=always - initializes the database with scripts
minio.init.enabled=true - loads local images into MinIO
```
3) Launch the backend application.

### Subsequent Launches
Update application.properties:
```properties
spring.jpa.hibernate.ddl-auto=update (or `create-drop` completely recreates the schema (deletes old data))
spring.sql.init.mode=never
minio.init.enabled=false
```

## 📖 Documentation
- [Swagger UI](http://localhost:8080/swagger-ui/index.html) (after app launch)

## 👥Development Team  
- Okoneshnikov Vladimir, frontend developer  
- Bikteev Timur, frontend developer  
- Gafiyatullin Aizat, backend developer  
- Andreev Sergey, backend developer  
