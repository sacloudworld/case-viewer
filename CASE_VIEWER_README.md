# Case Viewer Service

Spring Boot service based on the original Order Service structure, with the business domain changed from **Order / OrderItem** to **Case / Activity**.

## APIs

### Public authentication APIs

`POST /api/auth/register`

```json
{
  "username": "sachin",
  "password": "password123"
}
```

`POST /api/auth/login`

```json
{
  "username": "sachin",
  "password": "password123"
}
```

The login response contains a JWT token.

### Authenticated case view API

`GET /api/cases/{caseNumber}`

Header:

`Authorization: Bearer <JWT_TOKEN>`

Example:

`GET /api/cases/CASE-100001`

Response:

```json
{
  "caseId": "6f7a4c0e-1c5b-4e8e-8b91-2f3c4d5e6a70",
  "caseNumber": "CASE-100001",
  "title": "Customer service case",
  "description": "Customer requested investigation of a failed transaction.",
  "status": "IN_PROGRESS",
  "activities": [
    {
      "activityId": "1a2b3c4d-5e6f-4789-9012-345678901234",
      "activityType": "CASE_CREATED",
      "description": "Case created from customer request",
      "status": "COMPLETED",
      "performedBy": "system",
      "activityAt": "2026-08-31T10:00:00Z"
    },
    {
      "activityId": "2b3c4d5e-6f70-4890-a123-456789012345",
      "activityType": "DOCUMENT_REVIEW",
      "description": "Supporting documents reviewed",
      "status": "IN_PROGRESS",
      "performedBy": "agent01",
      "activityAt": "2026-08-31T11:30:00Z"
    }
  ],
  "createdAt": "2026-08-31T10:00:00Z",
  "updatedAt": "2026-08-31T11:30:00Z"
}
```

## Data model

- `Case` has a one-to-many relationship with `Activity`.
- One case can contain many activities.
- Activities are returned as part of the case view response.
- The case view endpoint is read-only; no case-create API was added because the requested API is a viewer API.
- JWT authentication, registration/login, Redis user caching, PostgreSQL/JPA, Actuator, Prometheus, Swagger/OpenAPI, logging and the existing project configuration structure are retained.

## Database tables

- `users`
- `cases`
- `case_activities`

With development profile, `spring.jpa.hibernate.ddl-auto=update` creates/updates the tables from the entities.

## Swagger

`/swagger-ui/index.html`

## Sample data

After starting the application once, run `database/sample-case-data.sql` against the configured PostgreSQL database to create `CASE-100001` with multiple activities.

## Load test

`load-test/case-view-api.js` tests the authenticated case-view endpoint. Override `BASE_URL`, `CASE_NUMBER`, `USERNAME`, and `PASSWORD` with k6 environment variables when needed.
