package io.eventuate.plugins.gradle.versions


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class UpgradeVersionsTask extends DefaultTask {

    @TaskAction
    def upgradeVersions() {
        new GradlePropertiesFileUpgrader(new Bintray())
                .upgradePropertyFile("./gradle.properties", "./gradle.properties")
    }

}
