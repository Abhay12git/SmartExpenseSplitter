pipeline {
    agent any

    environment {
        // Replace with your GitHub repository URL
        REPO_URL = 'https://github.com/Abhay12git/SmartExpenseSplitter.git'
        REPO_BRANCH = 'main'
        EMAIL_RECIPIENTS = 'abhaykowshik@gmail.com'
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
                    printf "help\nlist-users\nlist-groups\nlist-expenses g1\nshow-balances g1\nshow-balance-between a1b2 c3d4\nsettle g1\nanalytics-paid\nanalytics-owed\nlargest-debtor\nlargest-creditor\nexit\n" | java -jar target/expense-splitter.jar | tee target/app-output.log
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
            mail to: "${EMAIL_RECIPIENTS}",
                 subject: "[Jenkins] ${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: """Build Result: ${currentBuild.currentResult}
Job: ${env.JOB_NAME}
Build Number: #${env.BUILD_NUMBER}
Build URL: ${env.BUILD_URL}
Git Branch: ${REPO_BRANCH}
Repository: ${REPO_URL}
"""
        }
        failure {
            echo 'Build failed. Check Maven logs and surefire reports.'
        }
    }
}