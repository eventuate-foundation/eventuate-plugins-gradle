package io.eventuate.plugins.gradle.publish

import org.gradle.StartParameter
import org.gradle.api.tasks.GradleBuild

class PublishEventuateDockerImagesTask extends GradleBuild {

  PublishEventuateDockerImagesTask() {

      def branch = GitBranchUtil.gitBranch(project)

      if (GitBranchUtil.isTrunk(branch)) {
          def version = project.version.replace("-SNAPSHOT", ".BUILD-SNAPSHOT")
          startParameter.projectProperties = ["dockerImageTag" : version, "version" : version]
          setTasks(["publishComposeBuild", , "publishComposePush"])
      } else if (branch.startsWith("wip-")) {
          def version = GitBranchUtil.getWipPublishingVersion(project)
          startParameter.projectProperties = ["dockerImageTag" : version, "version" : version]
          setTasks(["publishComposeBuild", "publishComposePush"])
      } else {
          def bintrayRepoType = GitBranchUtil.determineRepoType(branch)
          if (bintrayRepoType == null) {
            setTasks(["publishComposeBuild"])
          } else {
            startParameter.projectProperties = ["dockerImageTag" : branch, "version" : branch]
            setTasks(["publishComposeBuild", "publishComposePush"])
          }

      }
  }
}
