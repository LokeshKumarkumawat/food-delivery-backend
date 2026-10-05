pipeline {
    agent any

    parameters {
        booleanParam(name: 'RUN_QUALITY_GATES', defaultValue: false,
                     description: 'OWASP dependency-check + SonarQube (needs NVD API key and a Sonar server)')
    }

    environment {
        AWS_REGION      = 'ap-south-1'
        AWS_ACCOUNT_ID  = credentials('aws-account-id')
        ECR_REPO        = 'food-delivery-backend'
        ECR_REGISTRY    = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        IMAGE           = "${ECR_REGISTRY}/${ECR_REPO}"

        EKS_CLUSTER     = 'foodapp-cluster'
        HELM_RELEASE    = 'foodapp'
        K8S_NAMESPACE   = 'foodapp'
        APP_SECRET      = 'foodapp-secrets'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
        timestamps()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // Immutable tag per build. Never deploy :latest -- you cannot
                    // tell which commit is running, and you cannot roll back to a
                    // tag that keeps moving.
                    def sha = sh(returnStdout: true, script: 'git rev-parse --short=7 HEAD').trim()
                    env.IMAGE_TAG = "${env.BUILD_NUMBER}-${sha}"
                    currentBuild.displayName = "#${env.BUILD_NUMBER} ${env.IMAGE_TAG}"
                }
            }
        }

        stage('Build & unit test') {
            steps {
                sh './mvnw -B clean verify -Dspring.profiles.active=test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml',
                          allowEmptyResults: false
                    // jacoco: add the jacoco-maven-plugin to pom.xml first,
                    // then enable:  jacoco execPattern: 'target/jacoco.exec'
                }
            }
        }

        stage('Quality gates') {
            when { expression { params.RUN_QUALITY_GATES } }
            parallel {

                stage('Dependency CVEs') {
                    steps {
                        // Fails the build on a CVSS 7+ dependency.
                        sh './mvnw -B org.owasp:dependency-check-maven:check ' +
                           '-DfailBuildOnCVSS=7'
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: 'target/dependency-check-report.html',
                                             allowEmptyArchive: true
                        }
                    }
                }

                stage('Static analysis') {
                    steps {
                        withSonarQubeEnv('sonarqube') {
                            sh './mvnw -B sonar:sonar -Dsonar.projectKey=food-delivery-backend'
                        }
                    }
                }
            }
        }

        stage('Helm lint') {
            steps {
                sh '''
                    helm lint helm/foodapp -f helm/foodapp/values-prod.yaml
                    helm template foodapp helm/foodapp -f helm/foodapp/values-prod.yaml > /dev/null
                '''
            }
        }

        stage('Build image') {
            steps {
                sh """
                    docker build -t ${IMAGE}:${env.IMAGE_TAG} .
                """
            }
        }

        stage('Scan image') {
            steps {
                // Scanning after build and before push means a vulnerable
                // image never reaches the registry at all.
                sh """
                    trivy image \
                      --severity HIGH,CRITICAL \
                      --exit-code 1 \
                      --ignore-unfixed \
                      --format table \
                      ${IMAGE}:${env.IMAGE_TAG}
                """
            }
        }

        stage('Push to ECR') {
            when { anyOf { branch 'main'; branch 'develop' } }
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding',
                                  credentialsId: 'aws-jenkins']]) {
                    sh """
                        aws ecr get-login-password --region ${AWS_REGION} \
                          | docker login --username AWS --password-stdin ${ECR_REGISTRY}

                        docker push ${IMAGE}:${env.IMAGE_TAG}
                    """
                }
            }
        }

        stage('Deploy to EKS') {
            when { branch 'main' }
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding',
                                  credentialsId: 'aws-jenkins']]) {
                    sh """
                        aws eks update-kubeconfig \
                          --region ${AWS_REGION} --name ${EKS_CLUSTER}

                        kubectl create namespace ${K8S_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

                        kubectl -n ${K8S_NAMESPACE} get secret ${APP_SECRET} > /dev/null \
                          || { echo "ERROR: secret ${APP_SECRET} is missing in ${K8S_NAMESPACE}"; exit 1; }

                        helm upgrade --install ${HELM_RELEASE} ./helm/foodapp \
                          --namespace ${K8S_NAMESPACE} \
                          --values ./helm/foodapp/values-prod.yaml \
                          --set image.repository=${IMAGE} \
                          --set image.tag=${env.IMAGE_TAG} \
                          --atomic --timeout 5m
                    """
                }
            }
        }

        stage('Smoke test') {
            when { branch 'main' }
            steps {
                // exec runs inside the pod, so the NetworkPolicy is not in the way.
                sh """
                    kubectl -n ${K8S_NAMESPACE} rollout status \
                      deployment/${HELM_RELEASE} --timeout=300s

                    kubectl -n ${K8S_NAMESPACE} exec deploy/${HELM_RELEASE} -- \
                      curl -fsS http://localhost:9090/actuator/health/readiness
                """
            }
        }
    }

    post {
        always {
            sh 'docker image prune -f --filter "until=24h" || true'
        }
        success {
            echo "Deployed ${IMAGE}:${env.IMAGE_TAG}"
        }
        failure {
            // --atomic on the helm upgrade already rolled back. This is the
            // notification, not the recovery.
            echo "Build ${env.BUILD_NUMBER} failed. Helm rolled back automatically."
        }
    }
}