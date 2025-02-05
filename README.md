# PairCode

Real-time collaborative code editor. Users work on **Projects** made of source files, edit together over WebSockets, **Save** to MongoDB, and **Run** code in Docker.

[![Demo Video](https://img.youtube.com/vi/i8Jdqr6eWaI/2.jpg)](https://www.youtube.com/watch?v=i8Jdqr6eWaI)

## Tech stack

| | |
|---|---|
| **Frontend** | React, TypeScript, Vite, MUI, Monaco Editor, React Query, STOMP, Axios |
| **Backend** | Java, Spring Boot, Spring Security (JWT), Spring WebSocket/STOMP, Spring Data MongoDB |
| **Data & runtime** | MongoDB, Docker |
| **Testing** | JUnit, Mockito, Vitest |

## Architecture

```mermaid
flowchart LR
    subgraph client [Frontend]
        React[React + Monaco]
        STOMP[STOMP Client]
    end

    subgraph server [Backend]
        API[REST + WebSocket]
        Services[Project · Collaboration · Run · Auth]
        Cache[Editor Session Cache]
    end

    Mongo[(MongoDB)]
    Docker[(Docker)]

    React --> API
    STOMP --> API
    API --> Services
    Services --> Mongo
    Services --> Cache
    Services --> Docker
```

Live edits happen in an in-memory **editor session**; **Save** writes the snapshot to MongoDB.

```mermaid
sequenceDiagram
    participant User
    participant Editor
    participant Server
    participant MongoDB
    participant Docker

    User->>Editor: Open project
    Editor->>Server: Join session
    Server->>MongoDB: Load snapshot
    User->>Editor: Edit code
    Editor->>Server: Broadcast changes
    User->>Editor: Save
    Server->>MongoDB: Persist snapshot
    User->>Editor: Run
    Server->>Docker: Execute code
    Docker-->>Editor: Output
```

## Project structure

```
pairCode/
├── backend/
└── frontend/
```
