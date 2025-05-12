pipeline {
    agent any

    environment {
        JAVA_HOME = '/usr/lib/jvm/java-21-amazon-corretto.x86_64'
    }
    
    tools {
        // Remove the OpenJDK installation since it's causing issues
        // Just use the JDK that's already installed on the Jenkins agent
        jdk 'JDK21'
        gradle 'Gradle'
    }
    
    stages {
        stage('Debug') {
            steps {
                sh 'echo JAVA_HOME=$JAVA_HOME'
                sh 'java -version'
                sh 'gradle --version'
                sh 'ls -ld $JAVA_HOME'
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'gradle clean build -Pspring.profiles.active=staging --stacktrace'
            }
        }

        stage('Deploy to Staging') {
            steps {
                sh 'gradle bootJar -Pspring.profiles.active=staging'
                echo 'Deploying to staging environment'

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