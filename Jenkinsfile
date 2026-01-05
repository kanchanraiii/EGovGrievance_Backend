pipeline {
    agent any

    tools {
        maven 'maven-3.9'
        jdk 'jdk-17'
    }

    stages {

        stage('Checkout Source Code') {
            steps {
                checkout scm
            }
        }

        stage('Build All Services (mvn package)') {
            steps {
                sh '''
                mvn package -DskipTests
                '''
            }
        }

        stage('Verify JARs') {
            steps {
                sh '''
                echo "==== Built JARs ===="

                find . -type f -path "*/target/*.jar" \
                ! -name "*sources.jar" \
                ! -name "*javadoc.jar"
                '''
            }
        }
    }

    post {
        success {
            echo "All services packaged successfully"
        }
        always {
            cleanWs()
        }
    }
}
