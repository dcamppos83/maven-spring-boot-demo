def scmVars

pipeline {
    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '5', artifactNumToKeepStr: '5', daysToKeepStr: '5', numToKeepStr: '5')
        durabilityHint 'PERFORMANCE_OPTIMIZED'
        timeout(5)
    }
    libraries {
        lib('jenkins-pipeline-library@master')
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
    resources:
      limits:
        cpu: 1
        memory: 1Gi
      requests:
        cpu: 0.5
        memory: 500Mi
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
                    sh 'mvn clean verify'
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

                    // TODO: do we need to compile before jib?
                    // TODO: if not, and we can reusue what we have, that saves time
                    // however, currently we get exit '143', this means something crashes
                    // perhaps doing a clean and then compile solves it
                    // if not, try without compiling and reusing what came out of verify
                    sh 'mvn clean compile jib:build -Djib.to.auth.username=${DHUB_USR} -Djib.to.auth.password=${DHUB_PSW} -DskipTests'
                }
            }
        }
        stage('Something') {
            steps {
                container('jpb') {
                    // just trying to see if this saves us from error 143
                    echo 'Hello'
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
