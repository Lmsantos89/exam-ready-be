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
                sh '''
                # Set JAVA_HOME to the correct path
                export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto.x86_64
                export PATH=$JAVA_HOME/bin:$PATH

                # Run Gradle with explicit Java home
                gradle -Dorg.gradle.java.home=/usr/lib/jvm/java-21-amazon-corretto.x86_64 clean build -x test
                '''
            }
        }
        
        stage('Test') {
            steps {
                sh '''
                export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto.x86_64
                export PATH=$JAVA_HOME/bin:$PATH
                gradle -Dorg.gradle.java.home=/usr/lib/jvm/java-21-amazon-corretto.x86_64 test
                '''
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