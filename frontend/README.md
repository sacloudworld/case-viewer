# Case Viewer UI — Secure Inbox + Agent Console

React + TypeScript (Vite), two pages:

- **Customer secure inbox** — `/case-viewer/` (`index.html`, `src/customer/`). Customers register,
  send messages (new message = new case, INBOUND activity) and reply on existing cases.
- **Agent console** — `/case-viewer/agent/` (`agent/index.html`, `src/agent/`). Agents (role `AGENT`)
  see every conversation, reply (OUTBOUND activity) and change case status.
  In the dev profile an agent account is created on startup: `agent1` / `agentpass123`
  (`app.agent.*` in `application-dev.properties`).

## Production-style (single URL)

`npm run build` writes the UI into `../src/main/resources/static`, so Spring Boot
serves it together with the API:

```bash
cd frontend && npm install && npm run build
cd .. && ./mvnw -DskipTests package
SPRING_PROFILES_ACTIVE=dev java -jar target/case-viewer-0.0.1-SNAPSHOT.jar
```

Customer: http://localhost:8081/case-viewer/ · Agent: http://localhost:8081/case-viewer/agent/

## Development (hot reload)

Run the Spring Boot app on :8081, then:

```bash
cd frontend && npm run dev
```

Customer: http://localhost:5173/case-viewer/ · Agent: http://localhost:5173/case-viewer/agent/
(`/case-viewer/api` is proxied to :8081).

## API used

| Method | Path | Who | Purpose |
| --- | --- | --- | --- |
| POST | `/api/auth/register`, `/api/auth/login` | public | Existing auth (register always creates a customer) |
| GET | `/api/me` | signed in | Username and role |
| GET | `/api/inbox/conversations?q=&status=` | customer | My conversations |
| POST | `/api/inbox/conversations` (multipart `subject`, `body`, `files`) | customer | New message → new case |
| GET | `/api/inbox/conversations/{caseId}` | customer | Thread with attachments |
| POST | `/api/inbox/conversations/{caseId}/messages` (multipart `body`, `files`) | customer | Reply on the same case |
| GET | `/api/inbox/attachments/{attachmentId}` | customer | Download (own cases only) |
| GET | `/api/agent/conversations?q=&status=` | agent | All conversations |
| GET | `/api/agent/conversations/{caseId}` | agent | Thread |
| POST | `/api/agent/conversations/{caseId}/messages` (multipart) | agent | Reply (OUTBOUND) |
| PATCH | `/api/agent/conversations/{caseId}/status` | agent | Change case status |
| GET | `/api/agent/attachments/{attachmentId}` | agent | Download |

Customers only ever see their own cases (another customer's case returns 404).

Tables: `cases` (`case_id`), `case_activities` (`activity_id`, type `INBOUND`/`OUTBOUND`),
`case_emails` (`email_id`, `activity_id`, `email_data` JSON), `email_attachments`
(`attachment_id`, `email_id`, `activity_id`, `attachment_data`, plus file name/type/size).
All ids come from Oracle sequences starting at 1000.
