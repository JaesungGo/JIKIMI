pipeline {
    agent any
    environment {
        DOCKERHUB_CREDENTIALS = credentials('docker-hub')
        DOCKER_USERNAME = credentials('DOCKER_USERNAME')
    }
    stages {
        stage('Test') {
            steps {
                dir('backend') {
                    sh './mvnw test'
                }
                dir('frontend') {
                    sh 'npm test'
                }
            }
        }
        stage('Build and Push') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub') {
                        def frontendImage = docker.build("${DOCKER_USERNAME}/frontend:latest", './frontend')
                        def backendImage = docker.build("${DOCKER_USERNAME}/backend:latest", './backend')

                        frontendImage.push()
                        backendImage.push()
                    }
                }
            }
        }
        stage('Deploy') {
            steps {
                sh 'docker-compose pull'
                sh 'docker-compose up -d'
            }
        }
    }
    post {
        always {
            sh 'docker logout'
            cleanWs()
        }
        failure {
            emailext (
                subject: "파이프라인 실패: ${currentBuild.fullDisplayName}",
                body: "빌드 상태: ${currentBuild.result}",
                to: 'your-email@domain.com'
            )
        }
    }
}