package io.eventuate.plugins.gradle.publish

import org.gradle.StartParameter
import org.gradle.api.tasks.GradleBuild

class PublishEventuateArtifactsTask extends GradleBuild {


    PublishEventuateArtifactsTask() {

        def branch = GitBranchUtil.gitBranch()

        if (branch == "master") {

            def version = project.version.replace("-SNAPSHOT", ".BUILD-SNAPSHOT")

            startParameter.projectProperties = ["version" : version,
                                "deployUrl" : System.getenv("S3_REPO_DEPLOY_URL")]
            setTasks(["publish"])
        } else if (branch.startsWith("wip-")) {

            def suffix = branch.substring("wip-".length()).replace("-", "_").toUpperCase()

            def version = project.version.replace("-SNAPSHOT", "." + suffix + ".BUILD-SNAPSHOT")

            startParameter.projectProperties = ["version" : version,
                                "deployUrl" : System.getenv("S3_REPO_DEPLOY_URL")]
            setTasks(["publish"])

        } else {

            def bintrayRepoType = GitBranchUtil.determineRepoType(branch)

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

}
