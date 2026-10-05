<div align="center">

# Food Delivery Backend

**Production-grade REST API for a food ordering platform — Spring Boot 4, Kubernetes, full observability.**

[![Build](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/YOUR_USERNAME/food-delivery-backend/actions)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-6DB33F)](https://spring.io/projects/spring-boot)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-EKS-326CE5)](https://aws.amazon.com/eks/)
[![Tests](https://img.shields.io/badge/tests-50%20passing-brightgreen)](docs/TESTING.md)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](LICENSE)

[Architecture](#architecture) · [Screenshots](#screenshots) · [Run it](#run-it-in-two-minutes) · [CI/CD](#cicd-pipeline) · [Docs](#documentation)

</div>

---

<p align="center">
  <img src="docs/images/architecture.png" width="820" alt="System architecture">
</p>

## What this is

A backend for a food delivery service: browse menus, build a cart, check out,
pay through Stripe, track the order, review what you ate.

The interesting part is not the CRUD. It is everything around it — the schema is
versioned, the cache invalidates correctly, the API is rate-limited per endpoint
tier, every request is traceable end to end, and the whole thing deploys to
Kubernetes through a Jenkins pipeline that will not ship an image with a
critical CVE in it.

| | |
|---|---|
| **~40** REST endpoints | **13** Flyway migrations |
| **50** automated tests across 5 layers | **92** Postman requests incl. a security regression suite |
| **p99 95 ms** on the order endpoint | **2 → 10** pods under autoscaling |

---

## Screenshots

<table>
<tr>
<td width="50%">

**Jenkins pipeline**
<img src="docs/images/jenkins-pipeline.png" alt="Jenkins pipeline stage view">
Build → test → scan → push to ECR → deploy to EKS.

</td>
<td width="50%">

**Distributed tracing**
<img src="docs/images/grafana-trace.png" alt="Grafana trace waterfall">
One checkout request, every span, where the time went.

</td>
</tr>
<tr>
<td width="50%">

**Autoscaling under load**
<img src="docs/images/hpa-scaling.gif" alt="HPA scaling pods during a load test">
CPU crosses 70%, the HPA adds pods, latency recovers.

</td>
<td width="50%">

**API documentation**
<img src="docs/images/swagger.png" alt="Swagger UI">
OpenAPI 3, generated from the code.

</td>
</tr>
</table>

<details>
<summary><b>More screenshots</b></summary>

<br>

**Grafana dashboard** — orders placed, payment failure rate, p50/p95/p99 latency
<img src="docs/images/grafana-dashboard.png" width="100%">

**Test suite** — 50 tests across five layers
<img src="docs/images/tests.png" width="100%">

**Postman security regression suite** — each request reproduces a finding from the self-audit
<img src="docs/images/postman-security.png" width="100%">

**Trivy scan** — the pipeline fails on any HIGH or CRITICAL CVE
<img src="docs/images/trivy.png" width="100%">

**Load test** — k6, 50 virtual users, before and after the fetch-strategy fix
<img src="docs/images/k6-results.png" width="100%">

</details>

---

## Architecture

```
                        ┌──────────────────────────────────┐
   Client ──── ALB ────►│  Spring Boot API   (2-10 pods)   │
                        │  Java 21 · Spring Boot 4.0       │
                        └───┬──────┬──────┬──────┬─────────┘
                            │      │      │      │
                   ┌────────┘      │      │      └────────┐
                   ▼               ▼      ▼               ▼
              RDS MySQL      ElastiCache  Stripe      OTLP → Grafana
              (Flyway)        (Redis)     S3 / SMTP    LGTM stack
```

Packaged **by feature**, not by layer. Each module owns its controller, service,
repository, entity and DTOs, so a change to carts touches one directory.

```
auth_users · role · category · menu · cart · order
payment · review · email_notification · ratelimiter
```

Full detail in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## Engineering decisions worth reading

Not a feature list — the choices that were hard, with the alternatives that lost.

| Decision | Why it mattered |
|---|---|
| [Stripe webhooks as the payment trust boundary](docs/adr/0002-payment-confirmation-trust-boundary.md) | The browser told the API when payment succeeded. It should never have been asked. |
| [Rate-limit state in Redis, not in memory](docs/adr/0003-distributed-rate-limiting.md) | In-memory buckets mean the limit multiplies by replica count — weakest exactly when under load. |
| [One restaurant per cart](docs/adr/0004-single-restaurant-cart.md) | Two kitchens, two prep times, one courier. The food arrives cold. |
| [Why record decisions at all](docs/adr/0001-record-architecture-decisions.md) | Code shows what was decided. Only an ADR shows what else was on the table. |

I also [audited my own code](docs/audit/) and published the findings — severity,
reproduction steps, and fix for each. Every security finding has a Postman
request that reproduces it, kept as a regression suite.

---

## Run it in two minutes

```bash
git clone https://github.com/YOUR_USERNAME/food-delivery-backend.git
cd food-delivery-backend

cp .env.example .env        # fill in your own keys
docker compose up -d        # MySQL, Redis, Grafana LGTM
./mvnw spring-boot:run      # Flyway builds the schema on first boot
```

| | |
|---|---|
| API | http://localhost:8090 |
| Swagger UI | http://localhost:8090/swagger-ui.html |
| Grafana | http://localhost:3000 |

**On Kubernetes:**

```bash
helm upgrade --install foodapp ./helm/foodapp \
  --namespace foodapp --create-namespace \
  --set image.tag=1.0.0 --atomic --wait
```

---

## CI/CD pipeline

```
 Checkout ─► Build & test ─► ┌ Dependency CVE scan ┐ ─► Build image ─►
                             └ Static analysis     ┘
 ─► Trivy scan ─► Push to ECR ─► Helm deploy to EKS ─► Smoke test
```

Four things in there that are deliberate:

- **Image tags are immutable** — `${BUILD_NUMBER}-${GIT_SHA}`, never `latest`.
  You cannot roll back to a tag that keeps moving.
- **Scanning happens before the push**, so a vulnerable image never reaches the
  registry at all.
- **`helm upgrade --atomic`** rolls back automatically on a failed deploy.
  Recovery is not a manual step at 2am.
- **The quality gates run in parallel**, because they do not depend on each other.

[`Jenkinsfile`](Jenkinsfile) · [`Dockerfile`](Dockerfile) · [`helm/`](helm/)

---

## Kubernetes

| Concern | How |
|---|---|
| **Startup** | `startupProbe` gives the JVM 150s to boot, so the liveness probe can stay aggressive afterwards |
| **Health** | Separate `liveness` and `readiness` — a Redis blip drains traffic, it does not restart the pod |
| **Scaling** | HPA 2→10 on CPU, with a `behavior` block: fast scale-up, 5-minute scale-down window to stop thrashing |
| **Availability** | PodDisruptionBudget, zone spread, `maxUnavailable: 0` on rolling updates |
| **Shutdown** | `preStop` sleep so the load balancer stops routing before the JVM exits — no dropped requests on deploy |
| **Security** | Non-root, read-only root filesystem, all capabilities dropped, default-deny NetworkPolicy |
| **AWS access** | IRSA — the pod assumes an IAM role. There is no access key to leak or rotate. |

[`k8s/`](k8s/) for readable manifests · [`helm/foodapp/`](helm/foodapp/) for the chart that actually ships

---

## Tech stack

| | |
|---|---|
| **Runtime** | Java 21, Spring Boot 4.0 |
| **Data** | MySQL 8, Spring Data JPA, Flyway |
| **Cache** | Redis 7.4, per-cache TTL policy |
| **Security** | Spring Security, JWT, BCrypt, Bucket4j rate limiting |
| **Integrations** | Stripe, AWS S3, SMTP + Thymeleaf |
| **Observability** | OpenTelemetry, Grafana LGTM, Micrometer |
| **Testing** | JUnit 5, Mockito, MockMvc, REST Assured, Testcontainers |
| **Infra** | Docker, Kubernetes, Helm, Jenkins, AWS ECR + EKS |

---

## Testing

Five kinds of test, each catching what the others cannot.

| Layer | Tool | Catches |
|---|---|---|
| Unit | Mockito | Business rule errors |
| Repository | `@DataJpaTest` | Broken queries |
| Controller | MockMvc | Wrong routes, missing auth |
| End to end | REST Assured | Integration failures |
| Infrastructure | Testcontainers | Real Redis behaviour |

```bash
./mvnw verify
newman run postman/FoodDeliveryBackend.postman_collection.json
```

[docs/TESTING.md](docs/TESTING.md)

---

## Documentation

| | |
|---|---|
| [HOW-IT-WORKS.md](docs/HOW-IT-WORKS.md) | Plain-English tour of the five most interesting parts |
| [ARCHITECTURE.md](docs/ARCHITECTURE.md) | Module map, request lifecycle |
| [DATA-MODEL.md](docs/DATA-MODEL.md) | Entities, migrations, indexing |
| [API.md](docs/API.md) | Endpoint reference |
| [DEPLOYMENT.md](docs/DEPLOYMENT.md) | Docker → ECR → EKS → Helm, end to end |
| [TESTING.md](docs/TESTING.md) | Test strategy |
| [OPERATIONS.md](docs/OPERATIONS.md) | Config, observability, runbook |
| [ROADMAP.md](docs/ROADMAP.md) | What's next and why |
| [audit/](docs/audit/) | Self-audit findings with remediation status |
| [adr/](docs/adr/) | Architecture decision records |

---

## Contact

**Your Name** — Backend Engineer, Pune
[LinkedIn](https://linkedin.com/in/YOUR_PROFILE) · [Email](mailto:you@example.com)

Happy to walk through any of it. The payment trust boundary is the most
interesting conversation in here.

<br>

<div align="center">
<sub>MIT licensed. Built to learn how production systems actually fit together.</sub>
</div>
