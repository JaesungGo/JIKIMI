pipeline {
    agent any

    environment {
        DOCKERHUB_CREDENTIALS = credentials('docker-hub-credentials')
        GITHUB_CREDENTIALS = credentials('github-credentials')
        DOCKER_IMAGE_FRONTEND = 'jikimi/frontend'
        DOCKER_IMAGE_BACKEND = 'jikimi/backend'
        DOCKER_TAG = 'latest'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Login to DockerHub') {
            steps {
                sh 'echo $DOCKERHUB_CREDENTIALS_PSW | docker login -u $DOCKERHUB_CREDENTIALS_USR --password-stdin'
            }
        }

        stage('Build and Push Frontend') {
            steps {
                dir('frontend') {
                    sh "docker build -t ${DOCKER_IMAGE_FRONTEND}:${DOCKER_TAG} ."
                    sh "docker push ${DOCKER_IMAGE_FRONTEND}:${DOCKER_TAG}"
                }
            }
        }

        stage('Build and Push Backend') {
            steps {
                dir('backend') {
                    sh "docker build -t ${DOCKER_IMAGE_BACKEND}:${DOCKER_TAG} ."
                    sh "docker push ${DOCKER_IMAGE_BACKEND}:${DOCKER_TAG}"
                }
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker-compose down || true'
                sh 'docker-compose up -d'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}