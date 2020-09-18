package io.eventuate.plugins.gradle.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.credentials.AwsCredentials

class EventuatePublishDockerImagesPlugin implements Plugin<Project> {

    void apply(Project rootProject) {
      def publishTask = rootProject.task("publishEventuateDockerImages",
              type: PublishEventuateDockerImagesTask,
              group: 'build setup',
              description: "Publish Eventuate Docker Images")
    }

}
