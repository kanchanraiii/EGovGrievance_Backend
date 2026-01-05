pipeline {
    agent any

    stages {

        stage('Checkout Code') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/kanchanraiii/EGOVGRIEVANCE_BACKEND.git'
            }
        }

        stage('Build Eureka Server') {
            steps {
                dir('Eureka-Server') {
                    bat 'mvn package'
                }
            }
        }

        stage('Build Config Server') {
            steps {
                dir('Config-Server') {
                    bat 'mvn package'
                }
            }
        }

        stage('Build API Gateway') {
            steps {
                dir('Api-Gateway') {
                    bat 'mvn package'
                }
            }
        }

        stage('Build Auth Service') {
            steps {
                dir('Auth-Service') {
                    bat 'mvn package'
                }
            }
        }

        stage('Build Grievance Service') {
            steps {
                dir('Grievance-Service') {
                    bat 'mvn package'
                }
            }
        }

        stage('Build Feedback Service') {
            steps {
                dir('Feedback-Service') {
                    bat 'mvn package'
                }
            }
        }

        stage('Build Notification Service') {
            steps {
                dir('Notification-Service') {
                    bat 'mvn package'
                }
            }
        }

        stage('Build Storage Service') {
            steps {
                dir('Storage-Service') {
                    bat 'mvn package -DskipTests'
                }
            }
        }

        stage('Docker Compose Build') {
            steps {
                echo 'Building Docker images using docker-compose'
                bat 'docker compose build'
            }
        }

        stage('Cleanup Existing Containers') {
            steps {
                echo 'Stopping any running containers'
                bat 'docker compose down || exit 0'
            }
        }

        stage('Docker Compose Up') {
            steps {
                echo 'Starting EGOV containers'
                bat 'docker compose up -d'
            }
        }
    }

    post {
        always {
            echo 'Archiving all built JARs'
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
        failure {
            echo 'Pipeline failed – stopping containers'
            bat 'docker compose down || exit 0'
        }
        success {
            echo 'EGOV Grievance Backend is UP and running'
        }
    }
}
