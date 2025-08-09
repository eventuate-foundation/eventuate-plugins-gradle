package io.eventuate.plugins.gradle.publish

import org.gradle.api.Plugin
import org.gradle.api.Project

class PublishMultiArchContainerImagesPlugin implements Plugin<Project> {

    void apply(Project rootProject) {
      def publishTask = rootProject.task("publishMultiArchContainerImages",
              type: PublishMultiArchContainerImagesTask,
              group: 'build setup',
              description: "Publish Multi-Architecture Container Images")
    }

}