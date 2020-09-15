package io.eventuate.plugins.gradle.publish

import org.gradle.StartParameter
import org.gradle.api.tasks.GradleBuild

class PublishEventuateArtifactsTask extends GradleBuild {

    def executeCommand(command) {
        def lastLine = ""
        def proc = command.execute()
        proc.in.eachLine { line -> lastLine = line }
        proc.err.eachLine { line -> println line }
        proc.waitFor()
        lastLine
    }

    def gitBranch() {
        executeCommand("git rev-parse --abbrev-ref HEAD")
    }

    PublishEventuateArtifactsTask() {

        def branch = gitBranch()

        if (branch == "master") {

            def version = project.version.replace("-SNAPSHOT", ".BUILD-SNAPSHOT")

            startParameter.projectProperties = ["version" : version,
                                "deployUrl" : System.getenv("S3_REPO_DEPLOY_URL")]
            setTasks(["publish"])

        } else {

            def bintrayRepoType = determineRepoType(branch)

            if (bintrayRepoType == null) {
              setTasks(["publish"])
            } else {

              startParameter.projectProperties = ["version" : branch,
                                  "bintrayRepoType": bintrayRepoType,
                                  "deployUrl" : "https://dl.bintray.com/eventuateio-oss/eventuate-maven-${bintrayRepoType}"]

              setTasks(["testClasses", "bintrayUpload"])
            }

        }
    }

    static String determineRepoType(String branch) {
        if (branch ==~ /.*RELEASE$/)
            return "release"
        if (branch ==~ /.*M[0-9]+$/)
            return "milestone"
        if (branch ==~ /.*RC[0-9]+$/)
            return "rc"
        return null;
    }
}
