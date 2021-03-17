package io.eventuate.plugins.gradle.publish

import org.gradle.StartParameter
import org.gradle.api.tasks.GradleBuild

class PublishEventuateDockerImagesTask extends GradleBuild {

  PublishEventuateDockerImagesTask() {

      def branch = GitBranchUtil.gitBranch()

      if (branch == "master") {
          startParameter.projectProperties = ["dockerImageTag" : project.version.replace("-SNAPSHOT", ".BUILD-SNAPSHOT")]
          setTasks(["publishComposeBuild", , "publishComposePush"])
      } else if (branch.startsWith("wip-")) {
          startParameter.projectProperties = ["dockerImageTag" : GitBranchUtil.getWipPublishingVersion(project)]
          setTasks(["publishComposeBuild", "publishComposePush"])
      } else {
          def bintrayRepoType = GitBranchUtil.determineRepoType(branch)
          if (bintrayRepoType == null) {
            setTasks(["publishComposeBuild"])
          } else {
            startParameter.projectProperties = ["dockerImageTag" : branch]
            setTasks(["publishComposeBuild", "publishComposePush"])
          }

      }
  }
}
