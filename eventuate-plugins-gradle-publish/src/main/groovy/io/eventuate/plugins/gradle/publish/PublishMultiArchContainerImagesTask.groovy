package io.eventuate.plugins.gradle.publish

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

class PublishMultiArchContainerImagesTask extends DefaultTask {

  @Input
  @Optional
  String containerNames
  
  @Input
  @Optional
  String multiArchTag
  
  @Input
  @Optional
  String circleBuildNum

  @TaskAction
  def publish() {
    def branch = GitBranchUtil.gitBranch(project)
    def targetTag = calculateTargetTag(branch)
    
    def sourceTag = project.hasProperty('multiArchTag') ? project.multiArchTag : multiArchTag
    if (!sourceTag) {
      throw new IllegalArgumentException("multiArchTag property must be specified")
    }
    
    def containers = project.hasProperty('containerNames') ? project.containerNames : containerNames
    if (!containers) {
      throw new IllegalArgumentException("containerNames property must be specified")
    }
    
    containers.split(',').each { containerName ->
      retagImage(containerName.trim(), sourceTag, targetTag)
    }
  }
  
  private String calculateTargetTag(String branch) {
    if (GitBranchUtil.isTrunk(branch)) {
      return project.version.replace("-SNAPSHOT", ".BUILD-SNAPSHOT")
    } else if (branch.startsWith("wip-")) {
      return GitBranchUtil.getWipPublishingVersion(project)
    } else {
      def bintrayRepoType = GitBranchUtil.determineRepoType(branch)
      if (bintrayRepoType == null) {
        def buildNum = project.hasProperty('circleBuildNum') ? project.circleBuildNum : circleBuildNum
        if (!buildNum) {
          throw new IllegalArgumentException("circleBuildNum property must be specified for branch: ${branch}")
        }
        return "${branch}-${buildNum}"
      } else {
        return branch
      }
    }
  }
  
  def retagImage(String containerName, String sourceTag, String targetTag) {
    def sourceImage = "${containerName}:${sourceTag}"
    def targetImage = "${containerName}:${targetTag}"
    
    logger.lifecycle("Retagging ${sourceImage} to ${targetImage}")
    
    def outputStream = new ByteArrayOutputStream()
    project.exec {
      commandLine 'docker', 'manifest', 'inspect', sourceImage
      standardOutput = outputStream
      ignoreExitValue = false
    }
    
    def manifestJson = outputStream.toString()
    def slurper = new groovy.json.JsonSlurper()
    def manifest = slurper.parseText(manifestJson)
    
    def sources = manifest.manifests.collect { m ->
      "${containerName}@${m.digest}"
    }
    
    logger.lifecycle("${targetImage} sources ${sources}")

    def commandArgs = ['docker', 'buildx', 'imagetools', 'create', '-t', targetImage] + sources
    
    project.exec {
      commandLine commandArgs
      ignoreExitValue = false
    }
    
    logger.lifecycle("Successfully retagged ${sourceImage} to ${targetImage}")
  }
}