pipeline {
    agent any

    environment {
        // Replace with your GitHub repository URL
        REPO_URL = 'https://github.com/Abhay12git/SmartExpenseSplitter.git'
        REPO_BRANCH = 'main'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: "${REPO_BRANCH}", url: "${REPO_URL}"
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn -B clean verify'
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B -DskipTests package'
            }
        }
    }

    post {
        success {
            echo 'Build completed successfully.'
        }
        always {
            junit 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, onlyIfSuccessful: true
        }
        failure {
            echo 'Build failed. Check Maven logs and surefire reports.'
        }
    }
}