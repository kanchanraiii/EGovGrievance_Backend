// Configured according to Linux VM on Azure
pipeline {
    agent any

    tools {
        maven 'maven-3.9.0'
        jdk 'jdk-17'
    }

    stages {

        stage('Checkout Code') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/kanchanraiii/EGOVGRIEVANCE_BACKEND.git'
            }
        }

        stage('Build All Services (Maven)') {
            steps {
                sh '''
                  mvn -f Eureka-Server/pom.xml package -DskipTests
                  mvn -f Config-Server/pom.xml package -DskipTests
                  mvn -f Api-Gateway/pom.xml package -DskipTests
                  mvn -f Auth-Service/pom.xml package -DskipTests
                  mvn -f Grievance-Service/pom.xml package -DskipTests
                  mvn -f Feedback-Service/pom.xml package -DskipTests
                  mvn -f Notification-Service/pom.xml package -DskipTests
                  mvn -f Storage-Service/pom.xml package -DskipTests
                '''
            }
        }

        stage('Build Docker Images') {
            steps {
                sh 'docker compose build'
            }
        }

        stage('Restart Containers') {
            steps {
                sh '''
                  docker compose down || true
                  docker compose up -d
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
        failure {
            sh 'docker compose down || true'
        }
        success {
            echo 'EGOV Grievance Backend is UP and running'
        }
    }
}
