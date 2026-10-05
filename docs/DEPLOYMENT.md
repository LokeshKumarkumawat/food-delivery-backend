# Deployment guide

From a jar on your laptop to an autoscaling deployment on EKS, built and shipped
by Jenkins.

Work through it in order. Each stage runs on its own, so you can stop at any
point and still have something worth showing.

| Stage | What you get | Time | Cost |
|---|---|---|---|
| [1. Container](#stage-1-container) | A working image | 1 h | free |
| [2. Local Kubernetes](#stage-2-local-kubernetes) | Pods, probes, scaling on your laptop | 2–3 h | free |
| [3. Helm](#stage-3-helm) | One command to deploy any environment | 2 h | free |
| [4. ECR](#stage-4-ecr) | Images in a real registry | 30 min | ~$0.10/mo |
| [5. EKS](#stage-5-eks) | Running on AWS | 3–4 h | ~$0.20/h |
| [6. Jenkins](#stage-6-jenkins) | Push to main, it deploys | 3–4 h | free locally |

---

## A note on cost before you start

EKS is **$0.10 per hour for the control plane alone** — about ₹6,500 a month —
plus EC2 nodes on top. Do not leave it running.

Three honest options:

1. **Local only.** `kind` or `minikube` runs the same manifests, the same Helm
   chart, the same HPA. Everything in this guide except stage 5 works for free.
   Most of the learning is here.
2. **One-day EKS.** Spin it up, capture your screenshots, tear it down. Under
   ₹300 total. **This is what I would do.**
3. **k3s on a t3.small.** ~₹1,200 a month, a real cluster with a real public
   URL. Worth it only if you want a permanently live demo.

Nobody reviewing your repo can tell whether the cluster is still running. They
can only see the manifests and the screenshots.

---

## Stage 1: Container

### Build it

```bash
docker build -t food-delivery-backend:local .
docker run --rm -p 8090:8090 --env-file .env food-delivery-backend:local
```

### Why the Dockerfile looks like that

Four things in [`Dockerfile`](../Dockerfile) are deliberate, and each is a
reasonable interview question.

**Dependencies resolve in their own stage.** `COPY pom.xml` then
`mvn dependency:go-offline` *before* `COPY src`. Docker caches layers, so a code
change does not re-download Maven Central. Build time drops from four minutes to
forty seconds.

**The jar is exploded into layers.** `-Djarmode=tools ... extract --layers`
splits it into dependencies, loader, snapshot dependencies and your code. Your
code changes every build; the 55 MB of dependencies does not. So a rebuild
pushes about 2 MB instead of 60 MB.

**The runtime image has a JRE, not a JDK, and no Maven.** Build tools in a
production image are attack surface and about 400 MB of it.

**`MaxRAMPercentage`.** Without it, a JVM in a 512 Mi pod sizes its heap against
the *node's* memory — often 16 GB — and gets OOMKilled almost immediately. This
is the single most common reason a Spring Boot app that works locally dies in
Kubernetes.

### Check it

```bash
docker images food-delivery-backend:local     # expect ~250-300 MB
docker run --rm food-delivery-backend:local whoami   # expect "app", not root
trivy image --severity HIGH,CRITICAL food-delivery-backend:local
```

---

## Stage 2: Local Kubernetes

### Create a cluster

```bash
brew install kind kubectl helm          # or your package manager
kind create cluster --name foodapp

# the HPA cannot work without this
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
kubectl patch -n kube-system deployment metrics-server --type=json \
  -p '[{"op":"add","path":"/spec/template/spec/containers/0/args/-","value":"--kubelet-insecure-tls"}]'
```

That patch is needed because `kind` nodes use self-signed kubelet certificates.
On a real cluster, leave it out.

### Load your image

`kind` cannot see your local Docker images unless you hand them over:

```bash
kind load docker-image food-delivery-backend:local --name foodapp
```

### Dependencies

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install mysql bitnami/mysql -n foodapp --create-namespace \
  --set auth.database=foodapp --set auth.rootPassword=localdev
helm install redis bitnami/redis -n foodapp --set auth.enabled=false
```

### Apply the manifests

```bash
kubectl apply -f k8s/
kubectl -n foodapp get pods --watch
```

### The three probes — the part that actually matters

This is where most people's Kubernetes understanding stops at "I added a health
check", and where a good answer separates you.

| Probe | Question it answers | What happens on failure |
|---|---|---|
| `startupProbe` | Has it finished booting? | Nothing yet — the other probes are suspended |
| `readinessProbe` | Can it serve traffic now? | Removed from the Service. **Not restarted.** |
| `livenessProbe` | Is it wedged? | **Killed and restarted.** |

**Why the startup probe exists.** A Spring Boot app takes 20–40 seconds to boot.
Without a startup probe you have to set `initialDelaySeconds: 60` on liveness —
which means for the first minute of *every* pod's life, a genuinely crashed
process goes undetected. The startup probe lets you say "take up to 150 seconds
to boot, then check every 20 seconds and restart after three failures."

**Why readiness and liveness are different.** Redis goes down for ninety
seconds. Readiness fails, the pod drains, traffic goes to healthy pods — correct.
If you pointed liveness at the same check, Kubernetes would instead kill and
restart every pod in a loop, which does not fix Redis and drops every in-flight
request. Getting this wrong turns a minor dependency blip into a full outage.

Watch it happen:

```bash
kubectl -n foodapp describe pod <pod-name> | grep -A3 "Liveness\|Readiness\|Startup"
kubectl -n foodapp get events --sort-by=.lastTimestamp
```

### Watch the autoscaler

```bash
kubectl -n foodapp get hpa --watch
```

Generate load:

```bash
kubectl -n foodapp run load --rm -it --image=williamyeh/hey -- \
  -z 3m -c 50 http://foodapp:8090/api/menu
```

**If `TARGETS` shows `<unknown>`**, it is one of two things, every time:
metrics-server is not running, or your deployment has no `resources.requests`.
The HPA computes a percentage *of the request* — with no request there is no
denominator and it silently does nothing.

### The `behavior` block

```yaml
scaleUp:
  stabilizationWindowSeconds: 30     # react fast, traffic is already here
scaleDown:
  stabilizationWindowSeconds: 300    # react slowly, a dip may be a lull
```

Without this, the HPA scales up and down every fifteen seconds under spiky
traffic. Pods spend their entire lives starting and terminating, and because a
JVM takes 30 seconds to become useful, you get worse latency than no autoscaling
at all. Asymmetric windows — fast up, slow down — are the fix, and explaining
why is a strong answer.

---

## Stage 3: Helm

### Why not just `kubectl apply`

Raw manifests are readable, which is why `k8s/` is kept in the repo. But they do
not have variables. Three environments means three near-identical copies of
every file, and they drift within a month.

Helm templates them. One chart, one `values.yaml` per environment, overriding
only what differs.

### Use it

```bash
helm lint ./helm/foodapp

# render without applying -- always do this before a real deploy
helm template foodapp ./helm/foodapp --values ./helm/foodapp/values-prod.yaml

helm upgrade --install foodapp ./helm/foodapp \
  -n foodapp --create-namespace \
  --set image.repository=food-delivery-backend \
  --set image.tag=local \
  --atomic --wait
```

### Three flags worth knowing by name

`--atomic` — if the deploy fails, roll back automatically. Without it, a failed
deploy leaves the release stuck half-applied and you fix it by hand.

`--wait` — do not report success until pods are actually Ready. Without it,
`helm upgrade` returns zero the moment the API server accepts the manifest, and
your pipeline reports a successful deploy of a crash-looping pod.

`--set image.tag=...` — CI supplies the tag per build. The chart never hard-codes
a version.

### The config checksum trick

In [`templates/deployment.yaml`](../helm/foodapp/templates/deployment.yaml):

```yaml
annotations:
  checksum/config: {{ include (print $.Template.BasePath "/configmap.yaml") . | sha256sum }}
```

Changing a ConfigMap does **not** restart pods by default. They keep running
with the old values, and you discover this an hour later wondering why your
change did nothing. Hashing the ConfigMap into a pod annotation changes the pod
template, which triggers a rolling restart. Small trick, saves real confusion.

```bash
helm history foodapp -n foodapp
helm rollback foodapp 3 -n foodapp
```

---

## Stage 4: ECR

```bash
aws ecr create-repository \
  --repository-name food-delivery-backend \
  --region ap-south-1 \
  --image-scanning-configuration scanOnPush=true \
  --image-tag-mutability IMMUTABLE
```

**`IMMUTABLE` matters.** It means a tag, once pushed, can never be overwritten.
`v1.2.3` is `v1.2.3` forever. Without it, someone can push a different image
under the same tag and your rollback target silently changes underneath you.

Push:

```bash
ACCOUNT=$(aws sts get-caller-identity --query Account --output text)
REGISTRY=$ACCOUNT.dkr.ecr.ap-south-1.amazonaws.com

aws ecr get-login-password --region ap-south-1 \
  | docker login --username AWS --password-stdin $REGISTRY

docker tag food-delivery-backend:local $REGISTRY/food-delivery-backend:1.0.0
docker push $REGISTRY/food-delivery-backend:1.0.0
```

Add a lifecycle policy so old images do not accumulate at $0.10/GB/month:

```json
{
  "rules": [{
    "rulePriority": 1,
    "description": "Keep 10 most recent",
    "selection": { "tagStatus": "any", "countType": "imageCountMoreThan", "countNumber": 10 },
    "action": { "type": "expire" }
  }]
}
```

### Never deploy `:latest`

Tag with `${BUILD_NUMBER}-${GIT_SHA}`.

With `latest` you cannot answer "what is running right now?", you cannot roll
back (the tag moved), and two pods started a minute apart can be running
different code. Immutable tags make deploys auditable, which is the whole point.

---

## Stage 5: EKS

### Create the cluster

```bash
eksctl create cluster \
  --name foodapp-cluster \
  --region ap-south-1 \
  --nodegroup-name workers \
  --node-type t3.medium \
  --nodes 2 --nodes-min 2 --nodes-max 5 \
  --managed --with-oidc
```

`--with-oidc` is not optional — it is what makes IRSA work, and IRSA is the next
section.

Takes 15–20 minutes. **Set a calendar reminder to delete it.**

```bash
eksctl delete cluster --name foodapp-cluster --region ap-south-1
```

### IRSA — the proper fix for AWS credentials

Your application currently needs `AWS_ACCESS_KEY_ID` and `AWS_SECRET_KEY` to
reach S3. Those are long-lived credentials sitting in config, which is how keys
end up in git.

IRSA removes them entirely. The pod assumes an IAM role through its service
account, using short-lived credentials the SDK fetches automatically. **There is
no key to leak, and nothing to rotate.**

```bash
eksctl create iamserviceaccount \
  --name foodapp \
  --namespace foodapp \
  --cluster foodapp-cluster \
  --attach-policy-arn arn:aws:iam::aws:policy/AmazonS3FullAccess \
  --approve
```

Then remove the AWS keys from your configuration completely. The SDK's default
credential chain finds the role on its own.

If you were asked to name one improvement to this project's security posture,
this is a better answer than most — it does not patch a leak, it removes the
thing that could leak.

### Install the cluster add-ons

```bash
# metrics-server, for the HPA
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

# AWS Load Balancer Controller, for the Ingress
helm repo add eks https://aws.github.io/eks-charts
helm install aws-load-balancer-controller eks/aws-load-balancer-controller \
  -n kube-system --set clusterName=foodapp-cluster
```

### Managed data stores

Do not run MySQL in the cluster for anything you would call production. A
database in a pod loses its data when the pod moves, and the backup story is
yours to build.

```bash
aws rds create-db-instance \
  --db-instance-identifier foodapp-db \
  --db-instance-class db.t3.micro \
  --engine mysql --engine-version 8.0 \
  --allocated-storage 20 \
  --master-username admin \
  --manage-master-user-password \
  --backup-retention-period 7
```

`--manage-master-user-password` puts the password in Secrets Manager rather than
your shell history.

### Secrets

A Kubernetes Secret is **base64, not encryption**. Anyone with read access to
the namespace can decode it. Two real options:

**External Secrets Operator** (recommended) — the real secret stays in AWS
Secrets Manager; the operator syncs it into the cluster and rotates it.

```bash
helm repo add external-secrets https://charts.external-secrets.io
helm install external-secrets external-secrets/external-secrets -n external-secrets --create-namespace
```

**Sealed Secrets** — encrypt with a cluster public key, commit the encrypted
file safely, and only the cluster can decrypt it. Good if you want everything in
git.

### Deploy

```bash
aws eks update-kubeconfig --region ap-south-1 --name foodapp-cluster

helm upgrade --install foodapp ./helm/foodapp \
  -n foodapp --create-namespace \
  --values ./helm/foodapp/values-prod.yaml \
  --set image.repository=$REGISTRY/food-delivery-backend \
  --set image.tag=1.0.0 \
  --atomic --wait --timeout 5m
```

---

## Stage 6: Jenkins

### Run it

```bash
docker run -d --name jenkins -p 8080:8080 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  jenkins/jenkins:lts-jdk21

docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

Mounting the Docker socket lets Jenkins build images. It also effectively grants
root on the host — fine on your laptop, not acceptable in a shared environment,
where you would use Kaniko or a separate build agent instead. Knowing that
distinction is worth saying out loud in an interview.

### Plugins

Docker Pipeline · Amazon ECR · Kubernetes CLI · AWS Credentials ·
Pipeline Utility Steps · JaCoCo · SonarQube Scanner

### Credentials

| ID | Type | What |
|---|---|---|
| `aws-jenkins` | AWS Credentials | IAM user with ECR push + EKS deploy |
| `aws-account-id` | Secret text | Your 12-digit account ID |
| `sonarqube` | Secret text | Sonar token, if you use it |

Give the IAM user only `AmazonEC2ContainerRegistryPowerUser` and a scoped
`eks:DescribeCluster`. A build agent with `AdministratorAccess` is a compromise
away from owning your account.

### Pipeline order, and why

Look at [`Jenkinsfile`](../Jenkinsfile). Four decisions:

**Tests before image build.** Failing fast is cheaper. There is no point
spending ninety seconds building an image for code that does not compile its
tests.

**Scan before push.** Trivy runs on the locally built image. A vulnerable image
never reaches the registry at all — if you scan after pushing, the bad artifact
is already there and someone can pull it.

**Quality gates in parallel.** CVE scanning and static analysis do not depend on
each other, so running them together halves that stage.

**`--atomic` on the deploy.** A failed rollout rolls itself back. Recovery is not
a manual step performed by a tired person.

### The webhook

GitHub → Settings → Webhooks → `http://your-jenkins/github-webhook/`, push
events. Use `ngrok http 8080` if Jenkins is on your laptop.

---

## Suggested order of work

Three weekends, roughly.

**Weekend 1 — containers and local Kubernetes.** Dockerfile, kind, manifests,
probes, HPA. Capture the scaling GIF. Most of the learning is here and it costs
nothing.

**Weekend 2 — Helm and Jenkins.** Convert manifests to a chart, stand up
Jenkins, get a green pipeline. Capture the pipeline screenshot.

**Weekend 3 — AWS.** ECR, EKS for one day, IRSA, deploy, capture everything,
tear it all down.

Then spend an evening on the README and screenshots, which is the part that
actually gets read.

---

## One honest sequencing note

If you have not yet fixed the three critical findings in
[audit/2026-09-security.md](audit/2026-09-security.md), fix those first.

A pipeline that reliably deploys an application where anyone can register as an
administrator is a pipeline that reliably deploys a vulnerable application.
Interviewers who know Kubernetes also know security, and "I built a deployment
pipeline" lands very differently from "I found three criticals in my own code,
fixed them, kept the exploits as regression tests, and then built a pipeline
that will not ship an image with a known CVE."

Same work. Much better story.
