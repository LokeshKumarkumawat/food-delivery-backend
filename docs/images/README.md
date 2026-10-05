# Images

Screenshots referenced by the root README. See [../SCREENSHOTS.md](../SCREENSHOTS.md)
for what to capture and how.

Expected files:

| File | Content |
|---|---|
| `architecture.png` | System architecture diagram (do this one first) |
| `jenkins-pipeline.png` | Jenkins stage view, all green |
| `hpa-scaling.gif` | HPA adding pods during a load test |
| `grafana-trace.png` | Trace waterfall for a checkout request |
| `grafana-dashboard.png` | Orders, payment failures, latency percentiles |
| `swagger.png` | Swagger UI with two endpoints expanded |
| `tests.png` | `mvn verify` output, 50 tests passing |
| `postman-security.png` | Folder 10, security regression suite |
| `trivy.png` | Image scan result |
| `k6-results.png` | Load test, before and after |

Keep each under 500 KB. Compress with `optipng -o5 *.png` before committing.
