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
                sh './gradlew test -Pspring.profiles.active=test'
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
                echo 'Deploying to staging environment'
                // Add your deployment commands here

                        sh '''
            # Define variables
            JAR_FILE=$(find build/libs -name "*.jar" | head -1)
            STAGING_SERVER="ec2-user@3.78.230.46"
            APP_DIR="/opt/exam-ready-app"
            APP_NAME="exam-ready-app"

            # Ensure the application directory exists
            ssh -o StrictHostKeyChecking=no $STAGING_SERVER "sudo mkdir -p $APP_DIR && sudo chown ec2-user:ec2-user $APP_DIR"

            # Copy the JAR file to the server
            scp -o StrictHostKeyChecking=no $JAR_FILE $STAGING_SERVER:$APP_DIR/$APP_NAME.jar

            # Create or update the systemd service file
            ssh -o StrictHostKeyChecking=no $STAGING_SERVER "sudo bash -c 'cat > /etc/systemd/system/$APP_NAME.service << EOL
            [Unit]
            Description=Exam Ready Application
            After=network.target

            [Service]
            User=ec2-user
            WorkingDirectory=$APP_DIR
            ExecStart=/usr/bin/java -jar -Dspring.profiles.active=staging $APP_DIR/$APP_NAME.jar
            SuccessExitStatus=143
            Restart=always
            RestartSec=5

            [Install]
            WantedBy=multi-user.target
            EOL'"

            # Reload systemd, enable and restart the service
            ssh -o StrictHostKeyChecking=no $STAGING_SERVER "sudo systemctl daemon-reload && sudo systemctl enable $APP_NAME && sudo systemctl restart $APP_NAME"

            # Check if the service started successfully
            ssh -o StrictHostKeyChecking=no $STAGING_SERVER "sudo systemctl status $APP_NAME"

            echo "Deployment to staging server at 3.78.230.46 completed successfully"
        '''
            }
        }
    }
    
    post {
        always {
            node {
                junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
            }
        }
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}