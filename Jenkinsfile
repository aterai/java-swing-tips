pipeline {
    agent 'any'
    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/aterai/java-swing-tips.git'
            }
        }
        stage ('Analysis') {
            steps {
                sh "~/.sdkman/candidates/ant/current/bin/ant -file all.xml checkstyle pmd"
            }
        }
    }
    post {
        always {
            recordIssues enabledForFailure: true, tool: checkStyle()
            recordIssues enabledForFailure: true, tool: pmd(pattern: '**/pmd.xml')
        }
    }
}
