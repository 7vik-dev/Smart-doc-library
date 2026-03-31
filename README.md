# 📚 Smart Document Library — Performance Monitoring Demo

A full-stack teaching project demonstrating backend performance optimization concepts using **Spring Boot + MongoDB + React + TypeScript**.

---

## 🎯 What You'll Learn

| Concept | Implementation |
|---|---|
| **Modern Frontend** | **React 18 + TypeScript + Vite** for a reactive dashboard |
| **Caching** | `@Cacheable` on GET endpoints (in-memory cache) |
| **Async Processing** | `@Async` for summary generation with thread pool |
| **Structured Logging** | SLF4J with INFO/DEBUG/ERROR levels |
| **Actuator Monitoring** | `/actuator/health`, `/actuator/metrics`, `/actuator/loggers` |
| **MongoDB** | Spring Data MongoDB with custom queries |
| **3-Layer Architecture** | Controller → Service → Repository |
| **Cloud Deployment** | Ready for **Render** with Docker multi-stage builds |

---

## 📋 Prerequisites

| Tool | Version | Download |
|---|---|---|
| JDK | 17+ | https://adoptium.net |
| Node.js | 18+ | https://nodejs.org |
| MongoDB | 6.0+ | https://www.mongodb.com/try/download/community |
| IDE | IntelliJ/VS Code | https://www.jetbrains.com/idea |

---

## 🚀 Quick Start

### Step 1 — Start MongoDB
```bash
# Windows — run MongoDB as a service, or:
"C:\Program Files\MongoDB\Server\6.0\bin\mongod.exe"
```

### Step 2 — Run the Backend (Spring Boot)
1. Open the project in your IDE.
2. Run `DocLibraryApplication.java`.
3. The backend starts on `http://localhost:8080`.

### Step 3 — Run the Frontend (React)
```bash
cd smart-doc-library/frontend-react
npm install
npm run dev
```
Open `http://localhost:5173` in your browser.

---

## 🌐 API Endpoints

| Method | URL | Description | Caching |
|---|---|---|---|
| GET | `/api/documents` | Get all documents | ✅ Cached |
| POST | `/api/documents` | Create document | Evicts cache |
| GET | `/api/documents/search?title=` | Search by title | ✅ Cached |
| POST | `/api/documents/{id}/summary` | Start async summary | ❌ |
| GET | `/api/monitor/system` | JVM memory & threads | ❌ |
| GET | `/api/monitor/cache` | Cache info | ❌ |

---

## ☁️ Deployment (Render)

The project is configured for one-click deployment to **Render** using the included `render.yaml` and `Dockerfile`.

1. Push this project to GitHub.
2. Create a new **Blueprint** on Render.
3. Provide your **MongoDB Atlas URI** in the environment variables.

---

## 🗂️ Project Structure

```
smart-doc-library/
├── frontend-react/                  ← React + Vite Frontend
│   ├── src/components/              ← Modular UI components
│   └── src/hooks/                   ← useDashboard custom hook
├── src/main/
│   ├── java/com/example/doclib/
│   │   ├── config/                  ← Async & Cache setup
│   │   ├── controller/              ← REST API endpoints
│   │   ├── service/                 ← Business logic
│   │   └── repository/              ← MongoDB access
│   └── resources/
│       ├── static/                  ← Bundled React app (after build)
│       └── application.properties   ← Config with ENV support
├── Dockerfile                       ← Multi-stage (Node -> Maven -> JDK)
├── render.yaml                      ← Render Blueprint
└── pom.xml                          ← Maven dependencies
```

---

## 🔬 Teaching Scenarios

### Demo 1: Reactive Caching
1. Search for "Spring" in the dashboard.
2. 1st Search: `CACHE MISS` (hits MongoDB).
3. 2nd Search: `CACHE HIT` (instant result from memory).
4. Notice how the React UI updates live stats and log console.

### Demo 2: Non-blocking Async
1. Click **"🤖 Summary"** on a document.
2. Notice the UI stays responsive immediately.
3. The summary status updates from `PROCESSING` to `COMPLETED` automatically via polling.

### Demo 3: Live Metrics
1. Switch to the **📊 Live Metrics** tab.
2. Watch real-time JVM Heap and Thread count charts (powered by `react-chartjs-2`).
3. These metrics are fetched from the custom `/api/monitor/system` endpoint.
