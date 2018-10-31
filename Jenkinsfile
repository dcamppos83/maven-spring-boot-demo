def scmVars
def tag

pipeline {
    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '5', artifactNumToKeepStr: '5', daysToKeepStr: '5', numToKeepStr: '5')
        durabilityHint 'PERFORMANCE_OPTIMIZED'
        timeout(15)
    }
    libraries {
        lib('core@master')
        lib('maven@master')
    }
    agent {
        kubernetes {
            label 'mypod'
            defaultContainer 'jnlp'
            yaml """
apiVersion: v1
kind: Pod
metadata:
  labels:
    some-label: some-label-value
spec:
  containers:
  - name: maven
    image: maven:3-jdk-11-slim
    command:
    - cat
    tty: true
"""
        }
    }
    stages {
        stage('Test versions') {
            steps {
                container('maven') {
                    sh 'uname -a'
                    sh 'mvn -version'
                }
            }
        }
        stage('Checkout') {
            steps {
                script {
                    scmVars = checkout scm
                }
                echo "scmVars=${scmVars}"
                gitRemoteConfigByUrl(scmVars.GIT_URL, 'githubtoken')
                sh '''
                git config --global user.email "jenkins@jenkins.io"
                git config --global user.name "Jenkins"
                '''
                //sh 'env'
            }
        }
        stage('Build') {
            steps {
                container('maven') {
                    sh 'mvn clean verify -B -e'
                }
            }
        }
        stage('Version & Analysis') {
            parallel {
                stage('Version Bump') {
                    when { branch 'master' }
                    environment {
                        NEW_VERSION = gitNextSemverTagMaven('pom.xml')
                    }
                    steps {
                        script {
                            tag = "${NEW_VERSION}"
                        }
                        container('maven') {
                            sh 'mvn versions:set -DnewVersion=${NEW_VERSION}'
                        }
                        gitTag("v${NEW_VERSION}")
                    }
                }
                stage('Sonar Analysis') {
                    when {branch 'master'}
                    environment {
                        SONAR_HOST='https://sonarcloud.io'
                        KEY='spring-maven-demo'
                        ORG='demomon'
                        SONAR_TOKEN=credentials('sonarcloud')
                    }
                    steps {
                        container('maven') {
                            sh '''mvn sonar:sonar \
                              -Dsonar.projectKey=${KEY} \
                              -Dsonar.organization=${ORG} \
                              -Dsonar.host.url=${SONAR_HOST} \
                              -Dsonar.login=${SONAR_TOKEN}
                            '''
                        }
                    }
                }
            }
        }
        stage('Publish Artifact') {
            when { branch 'master' }
            environment {
                DHUB=credentials('dockerhub')
            }
            steps {
                container('maven') {
                    // we should never come here if the tests have not run, as we run verify before
                    sh 'mvn clean compile -B -e jib:build -Djib.to.auth.username=${DHUB_USR} -Djib.to.auth.password=${DHUB_PSW} -DskipTests'
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
