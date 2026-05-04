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

        stage('Run App Demo') {
            steps {
                sh '''
                    mkdir -p target
                    printf "help\nlist-users\nlist-groups\nexit\n" | java -jar target/expense-splitter.jar | tee target/app-output.log
                '''
            }
        }
    }

    post {
        success {
            echo 'Build completed successfully.'
        }
        always {
            junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
            archiveArtifacts artifacts: 'target/*.jar,target/app-output.log', fingerprint: true, onlyIfSuccessful: true
        }
        failure {
            echo 'Build failed. Check Maven logs and surefire reports.'
        }
    }
}