pipeline {
    agent any

    parameters {
        choice(
            name: 'DEMO_MODE',
            choices: ['BASIC', 'ANALYTICS', 'FULL', 'CUSTOM'],
            description: 'Choose which demo input set to execute.'
        )
        text(
            name: 'APP_COMMANDS',
            defaultValue: '''help
list-users
list-groups
list-expenses g1
show-balances g1
show-balance-between a1b2 c3d4
settle g1
analytics-paid
analytics-owed
largest-debtor
largest-creditor
exit''',
            description: 'Used only when DEMO_MODE = CUSTOM (one command per line).'
        )
    }

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
                script {
                    sh 'mkdir -p target'
                    echo "Running demo mode: ${params.DEMO_MODE}"
                    String commands
                    switch (params.DEMO_MODE) {
                        case 'BASIC':
                            commands = '''help
list-users
list-groups
list-expenses g1
show-balances g1
exit'''
                            break
                        case 'ANALYTICS':
                            commands = '''help
list-expenses g1
show-balances g1
show-balance-between a1b2 c3d4
settle g1
analytics-paid
analytics-owed
largest-debtor
largest-creditor
exit'''
                            break
                        case 'FULL':
                            commands = '''help
list-users
list-groups
list-expenses g1
show-balances g1
show-balance-between a1b2 c3d4
settle g1
analytics-paid
analytics-owed
largest-debtor
largest-creditor
exit'''
                            break
                        default:
                            commands = params.APP_COMMANDS
                            break
                    }

                    writeFile file: 'target/commands.txt', text: commands + '\n'
                    sh '''
                        java -jar target/expense-splitter.jar < target/commands.txt | tee target/app-output.log
                    '''
                }
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
            script {
                try {
                    mail to: "${EMAIL_RECIPIENTS}",
                         subject: "[Jenkins] ${currentBuild.currentResult}: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                         body: """Build Result: ${currentBuild.currentResult}
Job: ${env.JOB_NAME}
Build Number: #${env.BUILD_NUMBER}
Build URL: ${env.BUILD_URL}
Git Branch: ${REPO_BRANCH}
Repository: ${REPO_URL}
"""
                } catch (Exception ignored) {
                    echo 'Mail notification skipped because SMTP was unavailable.'
                }
            }
        }
        failure {
            echo 'Build failed. Check Maven logs and surefire reports.'
        }
    }
}