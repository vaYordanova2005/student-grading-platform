# Markly

Училищна система: Spring Boot REST backend + PostgreSQL + React frontend. Роли Admin /
Teacher / Student — admin създава потребители, учител вписва оценки, ученик вижда своите.

Оригиналната TCP socket версия на проекта е запазена в [legacy/](legacy/) само като
референция и вече не се поддържа.

## Технологии

* Backend: Java 21, Spring Boot, Spring Security (JWT), Spring Data JPA, Flyway, Maven
* Frontend: React, TypeScript, Vite, React Router
* База данни: PostgreSQL

## Стартиране локално

### 1. PostgreSQL

Създай база данни (по подразбиране очаква `markly`):

```
createdb markly
```

### 2. Backend

```
cd backend
DB_USERNAME=postgres DB_PASSWORD=<твоята парола> ./mvnw spring-boot:run
```

При първо стартиране Flyway създава схемата и се seed-ва администраторски акаунт
(`admin` / `admin12345` по подразбиране — виж `app.seed-admin.*` в
`src/main/resources/application.yml`, конфигурируемо през env vars `SEED_ADMIN_USERNAME` /
`SEED_ADMIN_PASSWORD`). Смени паролата на админа след първи вход.

Конфигурируеми env vars: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`,
`DB_SSLMODE`, `SERVER_PORT`, `JWT_SECRET`, `JWT_EXPIRATION_MINUTES`, `FRONTEND_ORIGIN`.

За отдалечена база (напр. [Neon](https://neon.tech)) сложи `DB_HOST` на хоста от
connection string-а и `DB_SSLMODE=require`:

```
DB_HOST=<ep-xxx.neon.tech> DB_NAME=<database> DB_USERNAME=<user> DB_PASSWORD=<password> DB_SSLMODE=require ./mvnw spring-boot:run
```

### 3. Frontend

```
cd frontend
npm install
npm run dev
```

По подразбиране очаква backend на `http://localhost:8080` (виж `frontend/.env`).

## Роли

* **Admin** → създава teacher/student акаунти (username за teacher: `@uni-sofia.bg` имейл;
  за student: 9-цифрен факултетен номер, парола = 10-цифрено ЕГН)
* **Teacher** → вписва оценки на ученици по факултетен номер
* **Student** → вижда собствените си оценки
