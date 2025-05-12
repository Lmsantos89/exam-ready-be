pipeline {
    agent any

    tools {
        gradle 'Gradle'
    }

    environment {
        STAGING_SERVER = '3.78.230.46' //
        STAGING_USER = 'ec2-user'
        APP_NAME = 'Exam Ready'
        DEPLOY_DIR = '/opt/app'
        APP_PORT = '8080'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'gradle clean build -x test'
            }
        }

        stage('Test') {
            steps {
                sh 'gradle test'
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Deploy to Staging') {
            steps {
                sshagent(['staging-ssh-key']) {
                    // Create deployment directory if it doesn't exist
                    sh "ssh -o StrictHostKeyChecking=no ${STAGING_USER}@${STAGING_SERVER} 'mkdir -p ${DEPLOY_DIR}/config'"

                    // Copy JAR file to staging server
                    sh "scp build/libs/*.jar ${STAGING_USER}@${STAGING_SERVER}:${DEPLOY_DIR}/${APP_NAME}.jar"

                    // Create application.properties file
                    sh """
                    ssh ${STAGING_USER}@${STAGING_SERVER} 'cat > ${DEPLOY_DIR}/config/application.properties << EOL
# Server configuration
server.port=${APP_PORT}

# Database configuration
spring.datasource.url=jdbc:mysql://exam-ready-staging-db.cbsyqgugwa4s.eu-central-1.rds.amazonaws.com:3306/examready
spring.datasource.username=staging_db_user
spring.datasource.password=your-staging_db_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Flyway configuration
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
EOL'
                    """

                    // Create or update service file
                    sh """
                    ssh ${STAGING_USER}@${STAGING_SERVER} 'sudo tee /etc/systemd/system/${APP_NAME}.service > /dev/null << EOL
[Unit]
Description=${APP_NAME} Service
After=network.target

[Service]
User=${STAGING_USER}
WorkingDirectory=${DEPLOY_DIR}
ExecStart=/usr/bin/java -jar ${DEPLOY_DIR}/${APP_NAME}.jar --spring.config.location=file:${DEPLOY_DIR}/config/application.properties
SuccessExitStatus=143
TimeoutStopSec=10
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
EOL'
                    """

                    // Reload systemd, enable and restart the service
                    sh """
                    ssh ${STAGING_USER}@${STAGING_SERVER} 'sudo systemctl daemon-reload && \\
                    sudo systemctl enable ${APP_NAME} && \\
                    sudo systemctl restart ${APP_NAME}'
                    """

                    // Check service status
                    sh "ssh ${STAGING_USER}@${STAGING_SERVER} 'sudo systemctl status ${APP_NAME}'"
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
        }
    }
}
