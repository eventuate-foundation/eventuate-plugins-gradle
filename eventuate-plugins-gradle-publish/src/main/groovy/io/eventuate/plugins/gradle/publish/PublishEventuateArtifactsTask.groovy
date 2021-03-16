package io.eventuate.plugins.gradle.publish

import org.gradle.StartParameter
import org.gradle.api.tasks.GradleBuild

class PublishEventuateArtifactsTask extends GradleBuild {


    PublishEventuateArtifactsTask() {

        def branch = GitBranchUtil.gitBranch()

        if (branch == "master") {

            def version = project.version.replace("-SNAPSHOT", ".BUILD-SNAPSHOT")

            startParameter.projectProperties = ["version" : version,
                                "deployUrl" : GitBranchUtil.getenv("S3_SNAPSHOT_REPO_DEPLOY_URL")]
            setTasks(["publish"])
        } else if (branch.startsWith("wip-")) {

            // Publish <<SUFFIX>>...BUILD-SNAPSHOT

            startParameter.projectProperties = ["version" : GitBranchUtil.getWipPublishingVersion(project),
                                "deployUrl" : GitBranchUtil.getenv("S3_SNAPSHOT_REPO_DEPLOY_URL")]
            setTasks(["publish"])

        } else {

            def bintrayRepoType = GitBranchUtil.determineRepoType(branch)

            if (bintrayRepoType == "release") {

              startParameter.projectProperties = ["version" : branch,
                                  "deployUrl" : "https://oss.sonatype.org/service/local/staging/deploy/maven2/"]

              setTasks(["testClasses", "publish"])
            } else if (bintrayRepoType != null) {

              // rc or milestone

              startParameter.projectProperties = ["version" : branch,
                                  "deployUrl" : GitBranchUtil.getenv("S3_${repoType.toUpperCase()}_REPO_DEPLOY_URL")]

              setTasks(["testClasses", "publish"])

            } else
              throw new RuntimeException("Don't know what to do with: " + branch)

        }
    }

}
