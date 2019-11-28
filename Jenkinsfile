node {
  stage ('Checkout') {
    git 'https://github.com/aterai/java-swing-tips.git'
  }

  stage ('Analysis') {
    withEnv(["ANT_HOME=${env.ANT_HOME}"]) {
      if (isUnix()) {
        sh '"$ANT_HOME/bin/ant" -file all.xml checkstyle pmd'
      } else {
        bat(/"%ANT_HOME%\bin\ant" -file all.xml checkstyle pmd/)
      }
    }

    def checkstyle = scanForIssues tool: checkStyle(pattern: '**/checkstyle-result.xml')
    publishIssues issues: [checkstyle]

    def pmd = scanForIssues tool: pmdParser(pattern: '**/pmd.xml')
    publishIssues issues: [pmd]

    publishIssues id: 'analysis', name: 'All Issues',
      issues: [checkstyle, pmd],
      filters: [includePackage('io.jenkins.plugins.analysis.*')]
  }
}
