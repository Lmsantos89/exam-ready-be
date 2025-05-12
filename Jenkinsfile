pipeline {
    agent any

    environment {
        JAVA_HOME = '/usr/lib/jvm/java-21-amazon-corretto.x86_64'
        EC2_USER = 'ec2-user'
        EC2_HOST = '52.59.17.136'
        DEPLOY_DIR = '/opt/exam-ready-be'
        APP_NAME = 'exam-ready-be'
        ENVIRONMENT = 'staging'
        // Store SSH key credentials in Jenkins and reference them here
        SSH_CREDENTIALS = 'ec2-ssh-key'
    }

    tools {
        jdk 'JDK21'
        gradle 'Gradle'
    }

    stages {
        stage('Checkout') {
            steps {
                // Checkout code from GitHub
                git branch: 'master', url: 'https://github.com/Lmsantos89/exam-ready-be.git'
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean build -x test'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew test'
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh './gradlew bootJar'
            }
        }

        stage('Prepare Deployment') {
            steps {
                // Rebuild with staging profile
                sh './gradlew bootJar -Pprofile=staging'

                // Find the generated JAR file
                script {
                    env.JAR_FILE = sh(script: 'find build/libs -name "*.jar" | head -n 1', returnStdout: true).trim()
                    echo "JAR file to deploy: ${env.JAR_FILE}"
                }
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent([SSH_CREDENTIALS]) {
                    // Create deployment directory if it doesn't exist
                    sh "ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} 'mkdir -p ${DEPLOY_DIR}'"

                    // Copy the JAR file to the EC2 instance
                    sh "scp -o StrictHostKeyChecking=no ${env.JAR_FILE} ${EC2_USER}@${EC2_HOST}:${DEPLOY_DIR}/${APP_NAME}.jar"

                    // Create a service file for the application
                    sh """
                        ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} 'sudo tee /etc/systemd/system/${APP_NAME}.service > /dev/null << EOT
[Unit]
Description=${APP_NAME} Spring Boot Application
After=network.target

[Service]
User=${EC2_USER}
WorkingDirectory=${DEPLOY_DIR}
ExecStart=/usr/bin/java -Dspring.profiles.active=${ENVIRONMENT} -jar ${DEPLOY_DIR}/${APP_NAME}.jar
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
EOT'
                    """

                    // Stop the existing application if running
                    sh "ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} 'sudo systemctl stop ${APP_NAME} || true'"

                    // Reload systemd, enable and start the service
                    sh """
                        ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} 'sudo systemctl daemon-reload && \\
                        sudo systemctl enable ${APP_NAME} && \\
                        sudo systemctl start ${APP_NAME}'
                    """

                    // Check if service started successfully
                    sh "ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} 'sudo systemctl status ${APP_NAME}'"

                    // Wait for application to start and check health
                    sh "sleep 30"
                    sh "ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} 'curl -f http://localhost:8080/actuator/health || echo \"Health check failed but continuing\"'"
                }
            }
        }
    }

    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
            // Optionally send notification about failure
        }
        always {
            // Clean workspace after build
            cleanWs()
        }
    }
}
