# Common Jenkins Pipeline Errors and Solutions

Since no specific error was mentioned in the problem statement, here are common Jenkins pipeline issues that might occur with the provided Jenkinsfile and how to resolve them:

## 1. JDK Tool Configuration Error

### Error:
```
ERROR: Could not find a JDK installation named 'JDK21'
```

### Solution:
Configure JDK21 in Jenkins:
1. Go to Jenkins > Manage Jenkins > Global Tool Configuration
2. Find the JDK section and click "Add JDK"
3. Set "Name" as "JDK21"
4. Either:
   - Check "Install automatically" and select JDK 21 from the Oracle website
   - Uncheck "Install automatically" and provide the path to an existing JDK 21 installation

## 2. SonarQube Integration Error

### Error:
```
ERROR: No such DSL method 'withSonarQubeEnv' found among steps
```

### Solution:
Install the SonarQube Scanner plugin:
1. Go to Jenkins > Manage Jenkins > Plugins > Available
2. Search for "SonarQube Scanner"
3. Install the plugin and restart Jenkins
4. Configure SonarQube in Jenkins > Manage Jenkins > Configure System
5. Add your SonarQube server with the name 'SonarQube'

## 3. JaCoCo Plugin Missing

### Error:
```
ERROR: No such DSL method 'jacoco' found among steps
```

### Solution:
Install the JaCoCo plugin:
1. Go to Jenkins > Manage Jenkins > Plugins > Available
2. Search for "JaCoCo"
3. Install the plugin and restart Jenkins

## 4. Gradle Wrapper Permission Issue

### Error:
```
ERROR: ./gradlew: Permission denied
```

### Solution:
This is already handled in the current Jenkinsfile with the `chmod +x ./gradlew` command for Unix systems.

## 5. JUnit Test Results Not Found

### Error:
```
WARNING: No test report files were found. Configuration error?
```

### Solution:
Ensure tests are generating XML reports at the expected locations. The current Jenkinsfile looks for reports at `**/build/test-results/**/*.xml` which is the standard location for Gradle tests.

## 6. SonarQube URL and Token Issues

If the SonarQube URL or token in build.gradle is incorrect:

### Solution:
Update the SonarQube properties in build.gradle:
```gradle
sonarqube {
    properties {
        property 'sonar.host.url', 'http://your-sonarqube-host:9000'
        property 'sonar.login', 'your-actual-token'
    }
}
```

## 7. Network Issues with External Services

### Solution:
Ensure Jenkins has network access to any external services like SonarQube, Maven repositories, etc.

---

The updated Jenkinsfile provided should address most common Jenkins pipeline errors by:
1. Using the correct JDK tool configuration
2. Adding proper conditional logic for Windows/Unix systems
3. Including test reporting
4. Configuring SonarQube analysis
5. Adding JaCoCo code coverage reporting