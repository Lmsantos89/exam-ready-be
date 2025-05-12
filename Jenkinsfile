pipeline {
    agent any
    
    tools {
        // Remove the OpenJDK installation since it's causing issues
        // Just use the JDK that's already installed on the Jenkins agent
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
                sh './gradlew clean build'
            }
        }
        
        stage('Test') {
            steps {
                sh './gradlew test'
            }
        }
    }
    
    post {
        always {
            // Put cleanWs inside a node block to provide the required FilePath context
            node {
                cleanWs()
            }
        }
        failure {
            echo 'Build failed!'
        }
        success {
            echo 'Build succeeded!'
        }
    }
}