# 📚 Smart Document Library — Performance Monitoring Demo

A full-stack teaching project demonstrating backend performance optimization concepts using **Spring Boot + MongoDB + React-style dashboard**.

---

## 🎯 What You'll Learn

| Concept | Implementation |
|---|---|
| **Caching** | `@Cacheable` on GET endpoints (in-memory cache) |
| **Async Processing** | `@Async` for summary generation with thread pool |
| **Structured Logging** | SLF4J with INFO/DEBUG/ERROR levels |
| **Actuator Monitoring** | `/actuator/health`, `/actuator/metrics`, `/actuator/loggers` |
| **MongoDB** | Spring Data MongoDB with custom queries |
| **3-Layer Architecture** | Controller → Service → Repository |

---

## 📋 Prerequisites

| Tool | Version | Download |
|---|---|---|
| JDK | 17+ | https://adoptium.net |
| Maven | 3.8+ | Included in IntelliJ |
| MongoDB | 6.0+ | https://www.mongodb.com/try/download/community |
| IntelliJ IDEA | Any | https://www.jetbrains.com/idea |

---

## 🚀 Quick Start

### Step 1 — Start MongoDB
```bash
# macOS (Homebrew)
brew services start mongodb-community

# Windows — run MongoDB as a service, or:
"C:\Program Files\MongoDB\Server\6.0\bin\mongod.exe"

# Linux
sudo systemctl start mongod
```
Verify MongoDB is running: open a browser and go to `http://localhost:27017` — you should see a MongoDB message.

### Step 2 — Open in IntelliJ IDEA
1. Open IntelliJ IDEA
2. **File → Open** → select the `smart-doc-library` folder
3. IntelliJ will detect the `pom.xml` and import Maven dependencies automatically
4. Wait for indexing to complete (progress bar at bottom)

### Step 3 — Run the Application
1. Open `src/main/java/com/example/doclib/DocLibraryApplication.java`
2. Click the **▶ green Run button** next to `main()`
3. Watch the console — you should see the startup banner

### Step 4 — Open the Dashboard
Simply open `frontend/index.html` in your browser (double-click the file).

✅ That's it! No npm, no Node.js, no build step needed.

---

## 🌐 API Endpoints

| Method | URL | Description | Caching |
|---|---|---|---|
| GET | `/api/documents` | Get all documents | ✅ Cached |
| POST | `/api/documents` | Create document (JSON body) | Evicts cache |
| GET | `/api/documents/{id}` | Get document by ID | ❌ |
| GET | `/api/documents/search?title=` | Search by title | ✅ Cached |
| POST | `/api/documents/{id}/summary` | Start async summary | ❌ |
| GET | `/api/documents/stats` | Cache hit/miss stats | ❌ |
| GET | `/api/monitor/system` | JVM memory & threads | ❌ |
| GET | `/api/monitor/cache` | Cache info | ❌ |
| DELETE | `/api/monitor/cache` | Clear all caches | ❌ |

### Actuator Endpoints

| URL | Description |
|---|---|
| `/actuator/health` | App + MongoDB health status |
| `/actuator/metrics` | JVM memory, HTTP counts, cache stats |
| `/actuator/loggers` | View and change log levels at runtime |
| `/actuator/caches` | List registered Spring caches |
| `/actuator/env` | Configuration and environment variables |

---

## 🔬 Teaching Scenarios

### Demo 1: Observe Caching in Action
1. Open the dashboard → Documents tab
2. Click **"Refresh List"** → watch log: `CACHE MISS — MongoDB queried`
3. Click **"Refresh List"** again → watch log: `CACHE HIT! Response in 0ms`
4. Notice the response time difference!
5. Click **"Clear Cache"** → click refresh again → back to CACHE MISS

**What to explain:**
- First request: `@Cacheable` finds no cached data → runs method body → queries MongoDB → stores result
- Second request: `@Cacheable` finds cached data → **skips method body entirely** → returns from RAM

---

### Demo 2: Observe Async Processing
1. Click **"🤖 Summary"** button on any document
2. Notice the HTTP response arrives in **~1ms**
3. Watch the logs — background processing appears after 5 seconds
4. The document's status changes: `NONE → PROCESSING → COMPLETED`

**What to explain:**
- Without `@Async`: Client waits 5+ seconds staring at a spinner
- With `@Async`: Client gets immediate response, polls for result
- Spring hands the task to a thread pool (`DocLib-Async-1`)
- The HTTP request thread is freed immediately

---

### Demo 3: Watch Structured Logs
Run the app in IntelliJ and perform actions. Point students to the IntelliJ console:

```
12:34:56.789 [http-nio-8080-exec-1] INFO  DocumentService - 📦 [CACHE MISS] getAllDocuments - querying MongoDB
12:34:56.801 [http-nio-8080-exec-1] INFO  DocumentService -    ✅ MongoDB returned 4 documents in 12ms
12:34:57.002 [http-nio-8080-exec-2] INFO  DocumentService - ⚡ [CACHE HIT] Response served from memory - hit #1
12:34:58.100 [http-nio-8080-exec-3] INFO  DocumentController - 📥 [REQUEST] POST /api/documents/{id}/summary
12:34:58.102 [DocLib-Async-1]       INFO  SummaryService -    🔄 [ASYNC START] Summary generation started
12:35:03.103 [DocLib-Async-1]       INFO  SummaryService -    ✅ [ASYNC COMPLETE] Summary ready!
```

**Key teaching points:**
- Thread name changes: `http-nio-*` (HTTP thread) vs `DocLib-Async-*` (async thread)
- Log level meanings: INFO for business events, DEBUG for detail, ERROR for failures
- Structured messages answer: who, what, when, result

---

### Demo 4: Explore Actuator
Open these URLs and explain each one:

**`http://localhost:8080/actuator/health`**
```json
{
  "status": "UP",
  "components": {
    "mongo": { "status": "UP", "details": { "version": "6.0.x" } },
    "diskSpace": { "status": "UP" }
  }
}
```

**`http://localhost:8080/actuator/metrics`**  
→ Shows all available metric names. Then drill down:  
`http://localhost:8080/actuator/metrics/jvm.memory.used`

**`http://localhost:8080/actuator/loggers`**  
→ Change log level at runtime without restarting:
```bash
curl -X POST http://localhost:8080/actuator/loggers/com.example.doclib \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "TRACE"}'
```

---

## 🗂️ Project Structure

```
smart-doc-library/
├── pom.xml                          ← Maven dependencies
├── frontend/
│   └── index.html                   ← Dashboard (open in browser)
└── src/main/
    ├── resources/
    │   └── application.properties   ← Configuration
    └── java/com/example/doclib/
        ├── DocLibraryApplication.java    ← Entry point (@SpringBootApplication)
        ├── config/
        │   ├── AsyncConfig.java          ← Thread pool setup (@EnableAsync)
        │   └── CacheConfig.java          ← Cache setup (@EnableCaching)
        ├── controller/
        │   ├── DocumentController.java   ← REST endpoints (HTTP layer)
        │   └── MonitoringController.java ← Metrics endpoints
        ├── service/
        │   ├── DocumentService.java      ← Business logic + caching
        │   └── SummaryService.java       ← @Async processing
        ├── repository/
        │   └── DocumentRepository.java  ← MongoDB queries
        └── model/
            └── Document.java             ← MongoDB document model
```

---

## ⚙️ Configuration Reference

```properties
# application.properties

# MongoDB — change if your MongoDB is on a different host/port
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=smart_doc_library

# Caching — "simple" uses in-memory ConcurrentHashMap
spring.cache.type=simple

# Actuator — which endpoints to expose
management.endpoints.web.exposure.include=health,metrics,loggers,info,caches,env

# Async thread pool sizes
async.core-pool-size=2
async.max-pool-size=5
async.queue-capacity=100

# Logging levels
logging.level.com.example.doclib=DEBUG
logging.level.org.springframework.cache=TRACE
```

---

## 🧪 Testing with curl

```bash
# Create a document
curl -X POST http://localhost:8080/api/documents \
  -H "Content-Type: application/json" \
  -d '{"title":"My First Doc","content":"Hello from the API!"}'

# Get all documents (1st call = cache miss, 2nd call = cache hit)
curl http://localhost:8080/api/documents

# Search (cached by keyword)
curl "http://localhost:8080/api/documents/search?title=First"

# Generate async summary (replace <ID> with actual document id)
curl -X POST http://localhost:8080/api/documents/<ID>/summary

# Clear all caches
curl -X DELETE http://localhost:8080/api/monitor/cache

# Check health
curl http://localhost:8080/actuator/health
```

---

## 🐛 Troubleshooting

| Problem | Fix |
|---|---|
| `Connection refused` on startup | Start MongoDB first |
| `Port 8080 already in use` | Change `server.port=8081` in `application.properties` |
| Dashboard shows "Backend Offline" | Verify Spring Boot is running and check browser console for CORS errors |
| Lombok errors in IntelliJ | Enable annotation processing: Settings → Build → Compiler → Annotation Processors |
| No data in dashboard | Click "Refresh List" — the app auto-seeds 4 sample documents |

---

## 📐 Architecture Diagram

```
┌─────────────────────────────────────────────┐
│           Client (Browser Dashboard)         │
│  Axios HTTP calls │ Chart.js metrics display  │
└──────────────────────┬──────────────────────┘
                       │ HTTP (port 8080)
┌──────────────────────▼──────────────────────┐
│            Spring Boot REST API              │
│  ┌────────────────────────────────────────┐ │
│  │  DocumentController  MonitoringCtrl    │ │ ← Controller Layer
│  └──────────────┬─────────────────────────┘ │
│  ┌──────────────▼─────────────────────────┐ │
│  │  DocumentService      SummaryService   │ │ ← Service Layer
│  │  @Cacheable / @CacheEvict   @Async     │ │
│  └──────────────┬─────────────────────────┘ │
│  ┌──────────────▼─────────────────────────┐ │
│  │         DocumentRepository             │ │ ← Repository Layer
│  └──────────────┬─────────────────────────┘ │
│                 │                            │
│  ┌──────────────▼─────────────────────────┐ │
│  │              MongoDB                   │ │ ← Database Layer
│  └────────────────────────────────────────┘ │
│                                             │
│  ┌──────────────────────────────────────┐  │
│  │  Performance Layers (Cross-cutting)   │  │
│  │  🗄️ Cache     🔄 Async Thread Pool    │  │
│  │  📝 SLF4J Logging  🔩 Actuator        │  │
│  └──────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
```
