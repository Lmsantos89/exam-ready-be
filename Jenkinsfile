pipeline {
    agent any
    
    environment {
        JAVA_HOME = '/usr/lib/jvm/java-21-amazon-corretto.x86_64'
        PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Setup Java Toolchain') {
            steps {
                // Create a gradle.properties file to configure the Java toolchain
                sh '''
                    echo "org.gradle.java.installations.paths=${JAVA_HOME}" > gradle.properties
                    echo "org.gradle.java.home=${JAVA_HOME}" >> gradle.properties

                    # Verify Java installation
                    echo "Java home: ${JAVA_HOME}"
                    ls -la ${JAVA_HOME}/bin
                    ${JAVA_HOME}/bin/java -version || echo "Java not found at specified path"
                '''
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh '''
                    # Pass Java home explicitly to Gradle
                    ./gradlew clean build -x test -Pspring.profiles.active=staging \
                    -Dorg.gradle.java.home=${JAVA_HOME} \
                    --info
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                sh '''
                    ./gradlew sonarqube -Pspring.profiles.active=staging \
                    -Dorg.gradle.java.home=${JAVA_HOME}
                '''
            }
        }

        stage('Deploy to Staging') {
            steps {
                sh '''
                    ./gradlew bootJar -Pspring.profiles.active=staging \
                    -Dorg.gradle.java.home=${JAVA_HOME}

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
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}
