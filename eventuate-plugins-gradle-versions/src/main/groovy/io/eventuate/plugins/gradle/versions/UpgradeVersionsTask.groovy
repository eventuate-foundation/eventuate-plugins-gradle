package io.eventuate.plugins.gradle.versions

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class UpgradeVersionsTask extends DefaultTask {

    @TaskAction
    def greet() {
        def file = new File("./gradle.properties")
        List<String> lines = file.readLines()
        def transformed = lines.collect { it -> upgrade(it) }
        if (transformed != lines) {
            println "writing new file"

            file.withWriter('utf-8') { writer ->
                transformed.each {it ->
                    writer.writeLine it
                }
            }

        }
    }

    def artifacts = ["Cdc"           : "eventuate-cdc",
                     "Common"        : "eventuate-common",
                     "MessagingKafka": "eventuate-messaging-kafka",
                     "MessagingActiveMQ": "eventuate-messaging-activemq",
                     "MessagingRedis": "eventuate-messaging-redis",
                     "MessagingRabbitMQ": "eventuate-messaging-rabbitmq"
    ]

    def upgrade(line) {
        def matcher = line =~ /eventuate(.*)Version=(.*)$/
        if (matcher.find()) {
            def name = matcher.group(1)
            def currentVersion = matcher.group(2)
            if (artifacts[name]) {
                def json = "https://api.bintray.com/packages/eventuateio-oss/eventuate-maven-release/${artifacts[name]}".toURL().text

                def jsonSlurper = new JsonSlurper()
                def latestVersion = jsonSlurper.parseText(json)["latest_version"]
                if (latestVersion != currentVersion) {
                    println "Upgrading ${name} from ${currentVersion} to ${latestVersion}"
                    return "eventuate${name}Version=${latestVersion}"
                }
            }
        }
        return line
    }
}
