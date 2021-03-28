package io.eventuate.plugins.gradle.publish

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class WaitForMavenCentralTask extends DefaultTask {

    @TaskAction
    def waitForMavenCentralTask() {

        def group = project.group
        def artifact = project.name
        def version = GitBranchUtil.gitBranch()
        def url = "https://repo1.maven.org/maven2/${group.replace('.', '/')}/${artifact}/maven-metadata.xml"

        while(true) {
            def text = new URL(url).text
            def rootNode = new XmlSlurper().parseText(text)
            def versions = rootNode.versioning.versions.children().collect{ it.text() }

            if (versions.contains(version)) {
                println("Found it")
                break
            } else {
                println("Didn't find ${version} in ${versions}. Sleeping...")
                sleep(30 * 1000)
            }
        }

    }

}