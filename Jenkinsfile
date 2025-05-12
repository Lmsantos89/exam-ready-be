pipeline {
    agent any
    
    tools {
        // Remove the OpenJDK installation since it's causing issues
        // Just use the JDK that's already installed on the Jenkins agent
        jdk 'JDK21'
        gradle 'Gradle'
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

    }
    
    post {
        failure {
            echo 'Build failed!'
        }
        success {
            echo 'Build succeeded!'
        }
    }
}