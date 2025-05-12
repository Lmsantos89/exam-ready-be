pipeline {
    agent any
    
    tools {
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
                // Fix permissions for Gradle wrapper on Unix systems
                sh 'chmod +x ./gradlew'
                
                // Use platform-independent way to run Gradle
                script {
                    if (isUnix()) {
                        sh './gradlew clean build'
                    } else {
                        bat 'gradlew.bat clean build'
                    }
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh './gradlew test'
                    } else {
                        bat 'gradlew.bat test'
                    }
                }
            }
            post {
                always {
                    // Publish JUnit test results
                    junit '**/build/test-results/**/*.xml'
                    
                    // Generate JaCoCo code coverage report
                    jacoco(
                        execPattern: '**/build/jacoco/*.exec',
                        classPattern: '**/build/classes/java/main',
                        sourcePattern: '**/src/main/java',
                        exclusionPattern: '**/src/test/**'
                    )
                }
            }
        }
        
        stage('Code Quality') {
            steps {
                // SonarQube analysis
                withSonarQubeEnv('SonarQube') {
                    script {
                        if (isUnix()) {
                            sh './gradlew sonarqube'
                        } else {
                            bat 'gradlew.bat sonarqube'
                        }
                    }
                }
                
                // Wait for SonarQube quality gate
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Package') {
            steps {
                script {
                    if (isUnix()) {
                        sh './gradlew jar'
                    } else {
                        bat 'gradlew.bat jar'
                    }
                }
                archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
            }
        }
    }
    
    post {
        always {
            // Clean workspace after build
            cleanWs()
        }
        success {
            echo 'Build succeeded!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}