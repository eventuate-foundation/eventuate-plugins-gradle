package io.eventuate.plugins.gradle.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.credentials.AwsCredentials

class EventuatePublishPlugin implements Plugin<Project> {


    void apply(Project rootProject) {


        rootProject.allprojects { project ->
            apply plugin: 'java'
            apply plugin: 'maven-publish'
            apply plugin: 'com.jfrog.bintray'

            def repoPrefix = project.hasProperty("bintrayRepoPrefix") ? project.bintrayRepoPrefix : "eventuate-maven"

            if (!project.name.endsWith("-bom")) {

                project.publishing {
                      repositories {

                          maven {
                              url = project.deployUrl
                              if (project.deployUrl.startsWith("s3"))
                                  credentials(AwsCredentials) {
                                      accessKey System.getenv('S3_REPO_AWS_ACCESS_KEY')
                                      secretKey System.getenv('S3_REPO_AWS_SECRET_ACCESS_KEY')
                                  }
                          }
                      }
                      publications {
                          maven(MavenPublication) {
                              from components.java
                              pom {
                                  licenses {
                                      license {
                                          name = 'The Apache Software License, Version 2.0'
                                          url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                                      }
                                  }
                              }
                          }
                      }
                  }
            } else {
              project.publishing {
                  repositories {

                      maven {
                          url = project.deployUrl
                          if (project.deployUrl.startsWith("s3"))
                              credentials(AwsCredentials) {
                                  accessKey System.getenv('S3_REPO_AWS_ACCESS_KEY')
                                  secretKey System.getenv('S3_REPO_AWS_SECRET_ACCESS_KEY')
                              }
                      }
                  }
                  publications {
                      maven(MavenPublication) {
                          pom {
                              licenses {
                                  license {
                                      name = 'The Apache Software License, Version 2.0'
                                      url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                                  }
                              }
                          }
                          pom.withXml {
                              def n = asNode().appendNode('dependencyManagement').appendNode('dependencies')

                              project.parent.subprojects.sort { "$it.name" }.findAll { !it.name.endsWith("-bom") }.each { dep ->
                                  def dependency = n.appendNode('dependency')
                                  dependency.appendNode('groupId', project.group)
                                  dependency.appendNode('artifactId', dep.name)
                                  dependency.appendNode('version', project.version)
                              }

                              n
                          }
                      }
                  }
              }
            }

            project.bintray {
                publish = true
                user = System.getenv('BINTRAY_USER')
                key = System.getenv('BINTRAY_KEY')
                publications = ['maven']
                pkg {
                    repo = "${repoPrefix}-${project.bintrayRepoType}"
                    name = project.bintrayPkgName
                    licenses = ['Apache-2.0']
                    vcsUrl = project.bintrayPkgVcsUrl
                }
            }


        }

        def publishTask = rootProject.task("publishEventuateArtifacts",
                type: PublishEventuateArtifactsTask,
                group: 'build setup',
                description: "Publish Eventuate Artifacts")


    }
}
