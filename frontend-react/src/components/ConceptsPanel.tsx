import React from 'react';

const ConceptsPanel: React.FC = () => {
  return (
    <div id="tab-concepts" className="tab-panel active">
      <h2 style={{ fontSize: '16px', fontWeight: 700, marginBottom: '20px' }}>🎓 How It Works — Teaching Guide</h2>

      <div className="concept-card">
        <div className="concept-title">⚡ Caching with @Cacheable</div>
        <div className="concept-body">
          Caching stores query results in memory (RAM). The first request queries MongoDB and caches the result. Every subsequent request returns the cached result instantly — <strong>no database round trip</strong>.
          <br /><br />
          <strong>Without cache:</strong> 100 requests → 100 MongoDB queries → slow 🐌<br />
          <strong>With cache:</strong> 100 requests → 1 MongoDB query + 99 memory reads → fast ⚡
        </div>
        <div className="concept-code">
          <span className="cmt">// First call hits MongoDB and stores result in "documents" cache</span><br />
          <span className="cmt">// Second call skips the method body entirely — returns from memory!</span><br />
          <span className="ann">@Cacheable</span>(<span className="str">"documents"</span>)<br />
          <span className="kw">public</span> List&lt;Document&gt; getAllDocuments() &#123;<br />
          &nbsp;&nbsp;&nbsp;&nbsp;<span className="kw">return</span> documentRepository.findAll(); <span className="cmt">// Only runs on cache MISS</span><br />
          &#125;
        </div>
      </div>

      <div className="concept-card">
        <div className="concept-title">🔄 Async Processing with @Async</div>
        <div className="concept-body">
          <code>@Async</code> runs a method on a separate background thread. The calling thread doesn't wait — it returns immediately. This is perfect for slow operations like generating summaries, sending emails, or processing files.
          <br /><br />
          <strong>Without @Async:</strong> Client waits 5 seconds for summary → poor UX 😣<br />
          <strong>With @Async:</strong> Client gets instant "PROCESSING" response → polls for result ✅
        </div>
        <div className="concept-code">
          <span className="cmt">// This method runs on DocLib-Async-1 thread (NOT the HTTP thread)</span><br />
          <span className="cmt">// The HTTP response is sent immediately — client doesn't wait!</span><br />
          <span className="ann">@Async</span>(<span className="str">"taskExecutor"</span>)<br />
          <span className="kw">public void</span> generateSummaryAsync(String documentId) &#123;<br />
          &nbsp;&nbsp;&nbsp;&nbsp;Thread.sleep(<span className="str">5000</span>); <span className="cmt">// simulate heavy AI processing</span><br />
          &nbsp;&nbsp;&nbsp;&nbsp;<span className="cmt">// update MongoDB when done...</span><br />
          &#125;
        </div>
      </div>

      <div className="concept-card">
        <div className="concept-title">📝 Structured Logging with SLF4J</div>
        <div className="concept-body">
          Good logs answer: <em>who did what with what data and what was the result?</em>
          Use log LEVELS to control verbosity:
          <br /><br />
          <strong>ERROR</strong> → something broke, needs attention<br />
          <strong>WARN</strong>  → unexpected but handled<br />
          <strong>INFO</strong>  → normal business events (uploads, cache hits)<br />
          <strong>DEBUG</strong> → detailed trace for development
        </div>
        <div className="concept-code">
          <span className="kw">private static final</span> Logger log = LoggerFactory.getLogger(DocumentService.<span className="kw">class</span>);<br /><br />
          <span className="cmt">// ✅ Good structured log — includes context</span><br />
          log.info(<span className="str">"Upload complete - id='{}', file='{}', duration={}ms"</span>, id, fileName, ms);<br /><br />
          <span className="cmt">// ❌ Bad log — no useful context</span><br />
          log.info(<span className="str">"upload done"</span>);
        </div>
      </div>

      <div className="concept-card">
        <div className="concept-title">🔩 Spring Boot Actuator</div>
        <div className="concept-body">
          Actuator is like a health dashboard built into your Spring Boot app. Add the dependency and production-ready endpoints appear automatically. No extra code needed!
          <br /><br />
          Key endpoints exposed in this project: <code>/actuator/health</code>, <code>/actuator/metrics</code>, <code>/actuator/loggers</code>, <code>/actuator/caches</code>, <code>/actuator/env</code>
        </div>
        <div className="concept-code">
          <span className="cmt"># application.properties</span><br />
          management.endpoints.web.exposure.include=health,metrics,loggers,caches,env<br />
          management.endpoint.health.show-details=always<br /><br />
          <span className="cmt"># Result: Spring auto-creates these endpoints:</span><br />
          <span className="cmt"># GET /actuator/health  → &#123;"status":"UP","components":&#123;"mongo":...&#125;&#125;</span><br />
          <span className="cmt"># GET /actuator/metrics → lists all available metrics</span>
        </div>
      </div>

      <div className="concept-card">
        <div className="concept-title">🍃 3-Layer Architecture</div>
        <div className="concept-body">
          The application is divided into 3 layers, each with a single responsibility:
          <br /><br />
          <strong>Controller</strong> → handles HTTP (receives request, sends response)<br />
          <strong>Service</strong>    → business logic, caching, logging, orchestration<br />
          <strong>Repository</strong> → data access (talks to MongoDB only)
        </div>
        <div className="concept-code">
          Client (Browser/App)<br />
          &nbsp;&nbsp;&nbsp;&nbsp;↓  HTTP Request<br />
          <span className="ann">@RestController</span>&nbsp;&nbsp;&nbsp;DocumentController&nbsp;&nbsp;&nbsp;<span className="cmt">← HTTP Layer</span><br />
          &nbsp;&nbsp;&nbsp;&nbsp;↓  calls method<br />
          <span className="ann">@Service</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;DocumentService&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span className="cmt">← Business Logic + Cache</span><br />
          &nbsp;&nbsp;&nbsp;&nbsp;↓  calls method<br />
          <span className="ann">@Repository</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;DocumentRepository&nbsp;&nbsp;&nbsp;<span className="cmt">← Database Access</span><br />
          &nbsp;&nbsp;&nbsp;&nbsp;↓  MongoDB query<br />
          &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;MongoDB&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span className="cmt">← Data Storage</span>
        </div>
      </div>
    </div>
  );
};

export default ConceptsPanel;
