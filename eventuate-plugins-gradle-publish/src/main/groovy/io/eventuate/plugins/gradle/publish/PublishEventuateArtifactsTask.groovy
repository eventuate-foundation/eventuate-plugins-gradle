package io.eventuate.plugins.gradle.publish

import org.gradle.StartParameter
import org.gradle.api.tasks.GradleBuild

class PublishEventuateArtifactsTask extends GradleBuild {


    PublishEventuateArtifactsTask() {

        def branch = GitBranchUtil.gitBranch(project)

        def tasks = ["testClasses", "publish"]

        if (GitBranchUtil.isPlatform(project))
          tasks = ["publish"]

        if (GitBranchUtil.isTrunk(branch)) {

            def version = project.version.replace("-SNAPSHOT", ".BUILD-SNAPSHOT")

            startParameter.projectProperties = ["version" : version,
                                "deployUrl" : GitBranchUtil.getenv("S3_SNAPSHOT_REPO_DEPLOY_URL")]
            setTasks(tasks)
        } else if (branch.startsWith("wip-")) {

            // Publish <<SUFFIX>>...BUILD-SNAPSHOT

            startParameter.projectProperties = ["version" : GitBranchUtil.getWipPublishingVersion(project),
                                "deployUrl" : GitBranchUtil.getenv("S3_SNAPSHOT_REPO_DEPLOY_URL")]
            setTasks(["publish"])

        } else {

            def bintrayRepoType = GitBranchUtil.determineRepoType(branch)

            if (bintrayRepoType == "release") {

              // TODO deployUrl unused

              startParameter.projectProperties = ["version" : branch,
                                  "deployUrl" : "https://oss.sonatype.org/service/local/staging/deploy/maven2/"]


              tasks.removeLast()
              tasks.add("publishToSonatype")
              tasks.add("closeAndReleaseSonatypeStagingRepository")

              setTasks(tasks)
            } else if (bintrayRepoType != null) {

              // rc or milestone

              startParameter.projectProperties = ["version" : branch,
                                  "deployUrl" : GitBranchUtil.getenv("S3_${repoType.toUpperCase()}_REPO_DEPLOY_URL")]

              setTasks(tasks)

            } else
              throw new RuntimeException("Don't know what to do with branch: <" + branch + ">")

        }
    }

}
