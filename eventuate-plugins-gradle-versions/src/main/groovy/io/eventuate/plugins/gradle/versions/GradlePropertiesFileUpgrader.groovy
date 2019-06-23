package io.eventuate.plugins.gradle.versions

class GradlePropertiesFileUpgrader {

    private Bintray bintray

    GradlePropertiesFileUpgrader(Bintray bintray) {
        this.bintray = bintray
    }

    def upgradePropertyFile(String inputFileName, String outputFileName) {
        def inputFile = new File(inputFileName)
        List<String> lines = inputFile.readLines()
        def transformed = upgradeLines(lines)
        if (transformed != lines) {
            println "writing new inputFile"
            def outputFile = new File(outputFileName)
            outputFile.withWriter('utf-8') { writer ->
                transformed.each { it ->
                    writer.writeLine it
                }
            }

        }
    }

    List<GString> upgradeLines(List<String> lines) {
        lines.collect { it -> upgrade(it) }
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
                String latestVersion = bintray.fetchLatestVersion(artifacts[name])
                if (latestVersion != currentVersion) {
                    println "Upgrading ${name} from ${currentVersion} to ${latestVersion}"
                    return "eventuate${name}Version=${latestVersion}"
                }
            }
        }
        return line
    }

}
