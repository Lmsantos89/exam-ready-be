pipeline {
    agent any
    
    tools {
        jdk 'JDK 21'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh './gradlew clean build -Pspring.profiles.active=staging'
            }
        }
        
        stage('Test') {
            steps {
                sh './gradlew test -Pspring.profiles.active=staging'
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                sh './gradlew sonarqube -Pspring.profiles.active=staging'
            }
        }
        
        stage('Deploy to Staging') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew bootJar -Pspring.profiles.active=staging'
                // Deployment steps for staging environment
                echo 'Deploying to staging environment'
                // Add your deployment commands here
            }
        }
    }
    
    post {
        always {
            junit '**/build/test-results/test/*.xml'
            jacoco execPattern: '**/build/jacoco/test.exec'
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}