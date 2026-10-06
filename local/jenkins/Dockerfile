FROM jenkins/jenkins:lts-jdk21
USER root

# docker CLI (talks to Docker Desktop through docker.sock)
RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl gnupg \
 && install -m 0755 -d /etc/apt/keyrings \
 && curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc \
 && echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo $VERSION_CODENAME) stable" > /etc/apt/sources.list.d/docker.list \
 && apt-get update \
 && apt-get install -y --no-install-recommends docker-ce-cli \
 && rm -rf /var/lib/apt/lists/*

# same versions as on the laptop
RUN curl -fsSL -o /usr/local/bin/kubectl https://dl.k8s.io/release/v1.33.4/bin/linux/amd64/kubectl \
 && chmod +x /usr/local/bin/kubectl \
 && curl -fsSL -o /usr/local/bin/kind https://kind.sigs.k8s.io/dl/v0.33.0/kind-linux-amd64 \
 && chmod +x /usr/local/bin/kind \
 && curl -fsSL https://get.helm.sh/helm-v3.18.6-linux-amd64.tar.gz | tar -xz -C /tmp \
 && mv /tmp/linux-amd64/helm /usr/local/bin/helm \
 && rm -rf /tmp/linux-amd64

RUN git config --system --add safe.directory '*'

RUN jenkins-plugin-cli --plugins workflow-aggregator git pipeline-stage-view

# BuildKit: shares the host Docker's layer cache. The legacy builder cannot.
RUN apt-get update \
 && apt-get install -y --no-install-recommends docker-buildx-plugin \
 && rm -rf /var/lib/apt/lists/*