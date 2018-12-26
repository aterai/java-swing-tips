pipeline {
    agent 'any'
    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/aterai/java-swing-tips.git'
            }
        }
        stage('PMD') {
            steps {
                sh "~/.sdkman/candidates/ant/current/bin/ant -file all.xml pmd"
                step([$class: 'PmdPublisher', pattern: '**/pmd.xml'])
            }
        }
        stage('CheckStyle') {
            steps {
                sh "~/.sdkman/candidates/ant/current/bin/ant -file all.xml checkstyle"
                step([$class: 'CheckStylePublisher', pattern: '**/checkstyle-result.xml'])
            }
        }
    }
}
