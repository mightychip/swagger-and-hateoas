#!/usr/bin/env groovy

def call(buildResult) {
    //If we have a "null" build result, we can assume that's successful from what I've read... maybe I'm wrong, though...
    buildResult = buildResult ?: "SUCCESS"

    switch(buildResult) {
        case "SUCCESS":
            slackSend color: "good",
                    message: "${env.JOB_NAME} build ${env.BUILD_NUMBER} was successful!  Hurrah!!"
            break

        case "FAILURE":
            slackSend color: "danger",
                    message: "${env.JOB_NAME} build ${env.BUILD_NUMBER} failed.  Who broke the build?!"
            break

        case "UNSTABLE":
            slackSend color: "warning",
                    message: "${env.JOB_NAME} build ${env.BUILD_NUMBER} appears to be unstable.  Someone should investigate."
            break

        default:
            slackSend color: "danger",
                    message: "${env.JOB_NAME} build ${env.BUILD_NUMBER} returned an unexpected result.  See the logs to find out more."
    }
}