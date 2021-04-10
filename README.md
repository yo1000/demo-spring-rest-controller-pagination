# API Pagination Demo

```
./mvnw clean spring-boot:run
```

```
# DataSource from InMemory Items
curl localhost:8080/member?page=10

# DataSource from Database
curl localhost:8080/member?page=10&src=db

# DataSource from Database using SyntaxSugar
curl localhost:8080/member?page=10&src=db_simple
```
