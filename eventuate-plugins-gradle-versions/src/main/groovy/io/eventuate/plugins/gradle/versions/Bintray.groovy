package io.eventuate.plugins.gradle.versions

import groovy.json.JsonSlurper

class Bintray {

    String fetchLatestVersion(String artifact) {
        def json = "https://api.bintray.com/packages/eventuateio-oss/eventuate-maven-release/${artifact}".toURL().text
        def jsonSlurper = new JsonSlurper()
        return jsonSlurper.parseText(json)["latest_version"]
    }

}
