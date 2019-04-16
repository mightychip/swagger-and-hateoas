#!/usr/bin/env groovy

pipeline {
    agent any
    tools {
        maven 'Maven 3.6.0'
    }
    stages {

        stage ('Clone') {
            steps {
                git branch: 'master',
                        url: 'https://github.com/mightychip/swagger-and-hateoas.git'
            }
        }

        stage ('Execute Maven') {
            steps {
                sh 'mvn clean install'
            }
        }
    }

    post {
        always {
            slackAnnouncer(currentBuild.currentResult)
        }
    }
}