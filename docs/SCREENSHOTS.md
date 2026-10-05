# Screenshot guide

The README references nine images. This is how to produce each one, in the order
that gets the most value for the least work.

Put them all in `docs/images/`. Keep each under 500 KB — a README that takes
four seconds to load gets closed.

---

## Why this matters more than it sounds

A recruiter or hiring manager spends about thirty seconds on your repository.
They scroll. They do not clone, they do not run `./mvnw`, and they almost never
read code.

What they see in those thirty seconds is: the title, the first image, and the
first two headings. That is the whole budget. Everything else in the README is
for the engineer who screens you afterwards and for you, in the interview, as
something to point at.

So: **the first image carries more weight than any other single thing in the
repo.** Make it the architecture diagram, and make it good.

---

## Priority order

If you only do three, do 1, 2 and 3.

| # | Image | Effort | Why |
|---|---|---|---|
| 1 | Architecture diagram | 1–2 h | The first thing anyone sees |
| 2 | Jenkins pipeline | 30 min | Proves CI/CD is real, not aspirational |
| 3 | HPA scaling GIF | 1 h | Movement stops the scroll |
| 4 | Grafana trace | 20 min | Very few candidates have this |
| 5 | Grafana dashboard | 1 h | Shows you think about operations |
| 6 | Swagger UI | 5 min | Free |
| 7 | Test results | 10 min | Free |
| 8 | Postman security folder | 10 min | Sets up your best interview story |
| 9 | Trivy scan | 5 min | Free |
| 10 | k6 load test | 1 h | Only one with real before/after numbers |

---

## 1. Architecture diagram

**Tool:** [Excalidraw](https://excalidraw.com) — free, no account, exports PNG
and SVG, and the hand-drawn style looks deliberate rather than like a failed
attempt at Visio. [draw.io](https://app.diagrams.net) if you prefer clean lines.

**What to draw:**

```
Client → ALB → [Spring Boot pods, 2–10] → MySQL (RDS)
                                        → Redis (ElastiCache)
                                        → Stripe
                                        → S3
                                        → OTLP → Grafana LGTM
```

**Rules that make the difference:**

- **One diagram, not four.** A reader will look at exactly one.
- **Label the edges, not just the boxes.** `JDBC`, `OTLP`, `HTTPS` — the arrows
  are where the information is.
- **Show the pod count** (`2–10`) so autoscaling is visible without a caption.
- **Three colours maximum.** One for your service, one for datastores, one for
  third parties.
- **Export at 2x** so it stays sharp on a retina screen. Target ~1600 px wide,
  displayed at 820.
- **Do not include every class.** This is for someone who has never seen the
  project. Ten boxes, not fifty.

Save as `docs/images/architecture.png`.

---

## 2. Jenkins pipeline

The **Stage View** on the job page — the grid of green boxes with timings.

**Before you capture:**

- Make sure the run is fully green. A screenshot with a red stage reads as
  "does not work", fairly or not.
- Have at least 3–4 runs in the history so it looks used, not staged.
- Collapse the browser sidebar and zoom to ~90% so all stages fit in one shot.
- Crop to just the stage grid. Nobody needs your browser chrome.

If you do not want to run a Jenkins server long-term: start it in Docker, run
the pipeline four or five times, screenshot, then stop it. The screenshot is the
artifact.

```bash
docker run -d -p 8080:8080 -v jenkins_home:/var/jenkins_home \
  jenkins/jenkins:lts-jdk21
```

Save as `docs/images/jenkins-pipeline.png`.

---

## 3. HPA scaling under load (the GIF)

The one that stops the scroll, because it moves.

**Setup:** three terminal panes.

```bash
# pane 1 — the autoscaler
kubectl get hpa foodapp -n foodapp --watch

# pane 2 — pods appearing
kubectl get pods -n foodapp --watch

# pane 3 — the load
k6 run --vus 100 --duration 3m scripts/load/orders.js
```

Record all three. What the viewer sees: CPU climbs past 70%, `REPLICAS` goes
2 → 4 → 7, new pods appear in `ContainerCreating` then `Running`, and the k6
latency numbers come back down.

**Recording:**

```bash
# asciinema → SVG keeps it sharp and tiny. Better than a GIF.
asciinema rec scaling.cast
svg-term --in scaling.cast --out docs/images/hpa-scaling.svg --window

# or terminalizer for an actual GIF
terminalizer record scaling && terminalizer render scaling
```

**Keep it under 30 seconds.** Nobody watches a two-minute GIF. Speed it up in
post if the scaling took longer — `terminalizer` has a `frameDelay` setting.

If this is too much setup, a static before/after is still worth having:

```
$ kubectl get hpa -n foodapp
NAME      TARGETS   MINPODS   MAXPODS   REPLICAS
foodapp   12%/70%   2         10        2

  ...load applied...

NAME      TARGETS   MINPODS   MAXPODS   REPLICAS
foodapp   94%/70%   2         10        7
```

---

## 4. Grafana trace waterfall

Explore → Tempo → pick a `POST /api/orders/checkout` trace.

**Pick a good one.** A trace with 4–6 spans where one is clearly the longest
tells a story. A trace with one span tells nothing. Place an order with a few
cart items first so there is something to see.

Capture the waterfall with span names visible — `OrderService.placeOrderFromCart`,
`CartRepository.findByUser`, the JDBC spans, the SMTP span.

The SMTP span is worth seeking out: if email is slow, it shows up as the longest
bar, and that is a genuinely interesting thing to point at in an interview.

Save as `docs/images/grafana-trace.png`.

---

## 5. Grafana dashboard

Build a small one. Four panels is enough:

| Panel | Query shape |
|---|---|
| Orders placed | `rate(orders_placed_total[5m])` |
| Payment failure rate | `rate(payments_failed_total[5m]) / rate(payments_total[5m])` |
| Latency p50 / p95 / p99 | `histogram_quantile(0.99, http_server_requests_seconds_bucket)` |
| Active pods | `kube_deployment_status_replicas` |

Those metrics need Micrometer counters in the code — this is the one screenshot
that requires writing something first. It is also the one that most clearly
separates "I configured monitoring" from "I know what to monitor".

**Switch Grafana to the light theme** before capturing. Dark dashboards look
great on screen and turn into mud when embedded in a README.

Save as `docs/images/grafana-dashboard.png`.

---

## 6. Swagger UI

Expand two or three interesting endpoints — `POST /api/orders/checkout`,
`POST /api/payments/pay` — so schemas are visible. A page of collapsed grey bars
says nothing.

Save as `docs/images/swagger.png`.

---

## 7. Test results

Either the terminal output of `./mvnw verify` showing the summary line, or the
IDE's test runner tree with all five test classes green.

The terminal version is more honest and easier to crop:

```
[INFO] Tests run: 50, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

Include a few lines above it so the test class names are visible — that is what
shows the five layers.

Save as `docs/images/tests.png`.

---

## 8. Postman security folder

Folder 10 of the collection, expanded so the request names are readable:

```
SEC-01 · Self-assign ADMIN at registration
SEC-02 · Mark an order paid without paying
SEC-03 · Read another customer's order (IDOR)
SEC-04 · Debug endpoint with no auth
```

This is the setup for your strongest interview moment. The screenshot is the
hook; the story is "I audited my own code, found three criticals, and kept the
exploits as regression tests."

Save as `docs/images/postman-security.png`.

---

## 9. Trivy scan

```bash
trivy image --severity HIGH,CRITICAL food-delivery-backend:latest
```

A clean result is good. A result showing a few findings **with the pipeline
failing** is arguably better — it proves the gate works rather than that you
got lucky with a base image.

Save as `docs/images/trivy.png`.

---

## 10. Load test before/after

The only screenshot carrying real numbers, which makes it the most valuable one
to an engineer reading closely.

```bash
k6 run --vus 50 --duration 2m scripts/load/orders.js
```

Run it **before** fixing the eager-fetching problem, save the output, fix it,
run it again. Put both in one image side by side.

```
BEFORE                          AFTER
p50   210 ms                    p50    18 ms
p95   640 ms                    p95    61 ms
p99   840 ms                    p99    95 ms
queries/request  412            queries/request  3
```

That table is worth more than every other screenshot combined, because it is the
only one that proves you measured something rather than configured something.

---

## Embedding tips

**Set a width** so images do not render at full resolution:

```markdown
<img src="docs/images/architecture.png" width="820" alt="System architecture">
```

**Side by side** with a table:

```markdown
<table><tr>
<td width="50%"><img src="docs/images/jenkins-pipeline.png"></td>
<td width="50%"><img src="docs/images/grafana-trace.png"></td>
</tr></table>
```

**Hide the long tail** behind a collapsible block, so the README stays scannable:

```markdown
<details>
<summary><b>More screenshots</b></summary>
...
</details>
```

**Compress before committing:**

```bash
# lossless, usually 40-60% smaller
optipng -o5 docs/images/*.png

# or pngquant for a bigger win at slight quality cost
pngquant --quality=70-90 --ext .png --force docs/images/*.png
```

**Always write alt text.** It renders when an image fails to load, and it is
what a screen reader reads.

---

## Two things not to do

**Do not fake a screenshot.** Not a mocked dashboard, not a diagram of something
you did not build. It is checkable in about four questions, and being caught is
disqualifying in a way that having fewer features is not.

**Do not screenshot code.** GitHub already renders your code, with syntax
highlighting and working links. A picture of code is strictly worse than the
code, and it cannot be searched or copied.
