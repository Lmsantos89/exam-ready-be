pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Install Java 21') {
            steps {
                sh '''
                    # Check if Java 21 is already installed
                    if ! command -v java &> /dev/null || ! java -version 2>&1 | grep -q "version \"21"; then
                        echo "Installing Java 21..."
                        # For Amazon Linux 2023
                        sudo dnf install -y java-21-amazon-corretto-devel
                    fi

                    # Set JAVA_HOME
                    export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto.x86_64
                    export PATH=$JAVA_HOME/bin:$PATH

                    # Verify Java installation
                    echo "Java version:"
                    java -version
                    echo "Java compiler version:"
                    javac -version
                    echo "Java home: $JAVA_HOME"
                    ls -la $JAVA_HOME/bin
                '''
            }
        }

        stage('Configure Gradle') {
            steps {
                sh '''
                    # Create a gradle.properties file in the project directory
                    echo "org.gradle.java.installations.paths=/usr/lib/jvm/java-21-amazon-corretto.x86_64" > gradle.properties
                    echo "org.gradle.java.home=/usr/lib/jvm/java-21-amazon-corretto.x86_64" >> gradle.properties

                    # Update build.gradle to explicitly configure Java toolchain
                    if ! grep -q "java {" build.gradle; then
                        cat >> build.gradle << EOL

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
EOL
                    fi

                    # Make gradlew executable
                    chmod +x gradlew
                '''
            }
        }

        stage('Build') {
            steps {
                sh '''
                    # Set JAVA_HOME for this shell session
                    export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto.x86_64
                    export PATH=$JAVA_HOME/bin:$PATH

                    # Run Gradle with explicit Java home and debug output
                    ./gradlew clean build -x test -Pspring.profiles.active=staging \
                    -Dorg.gradle.java.home=/usr/lib/jvm/java-21-amazon-corretto.x86_64 \
                    --debug
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                sh '''
                    export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto.x86_64
                    export PATH=$JAVA_HOME/bin:$PATH

                    ./gradlew sonarqube -Pspring.profiles.active=staging \
                    -Dorg.gradle.java.home=/usr/lib/jvm/java-21-amazon-corretto.x86_64
                '''
            }
        }

        stage('Deploy to Staging') {
            steps {
                sh '''
                    export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto.x86_64
                    export PATH=$JAVA_HOME/bin:$PATH

                    ./gradlew bootJar -Pspring.profiles.active=staging \
                    -Dorg.gradle.java.home=/usr/lib/jvm/java-21-amazon-corretto.x86_64

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
