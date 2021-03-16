package io.eventuate.plugins.gradle.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.artifacts.repositories.PasswordCredentials

class EventuatePublishPlugin implements Plugin<Project> {


    void apply(Project rootProject) {

        def release = GitBranchUtil.isRelease()

        if (release) {
          rootProject.apply plugin: 'io.codearte.nexus-staging'

          rootProject.nexusStaging {
            packageGroup = "io.eventuate"
            username = System.getenv('OSSRH_USERNAME')
            password = System.getenv('OSSRH_PASSWORD')
          }

        }

        rootProject.allprojects { project ->
            apply plugin: 'java'

            if (release) {
              apply plugin: 'signing'
            }

            apply plugin: 'maven-publish'

            def repoPrefix = project.hasProperty("bintrayRepoPrefix") ? project.bintrayRepoPrefix : "eventuate-maven"

            project.java {
                withJavadocJar()
                withSourcesJar()
            }

            project.publishing {
                  repositories {

                      // TODO - given each repository a unique name

                      maven {

                          //
                          url = project.deployUrl

                          if (release) {
                            credentials(PasswordCredentials) {
                              username = System.getenv('OSSRH_USERNAME')
                              password = System.getenv('OSSRH_PASSWORD')
                            }

//                            snapshotRepository(url: "https://oss.sonatype.org/content/repositories/snapshots/") {
//                              authentication(userName: ossrhUsername, password: ossrhPassword)
//                            }
                          } else
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
                              name = project.name
                              description = "An eventuate project"
                              url = "https://eventuate.io"
                              licenses {
                                  license {
                                      name = 'The Apache Software License, Version 2.0'
                                      url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                                  }
                              }

                              developers {
                                developer {
                                  name = "Chris Richardson"
                                  email = "Chris@eventuate.io"
                                  organization = "Eventuate, Inc"
                                  organizationUrl = "https://eventuate.io"
                                }
                              }

                              scm {
                                def remote = GitBranchUtil.getRemote().replace("git@github.com:", "")  // git@github.com:eventuate-foundation/eventuate-plugins-gradle.git
                                def remoteSansSuffix = remote.replace(".git", "")
                                connection =          "scm:git:git://github.com/${remote}"
                                developerConnection = "scm:git:ssh://github.com:${remote}"
                                url =                 "http://github.com/${remoteSansSuffix}/tree/master"
                              }
                          }
                      }

                      if (project.name.endsWith("-bom")) {
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

              if (release) {
                project.signing {
                  def signingKey = findProperty("signingKey")
                  def signingPassword = findProperty("signingPassword")

                  useInMemoryPgpKeys(signingKey, signingPassword)
                  sign publishing.publications.maven
                }
              }

        }

        def publishTask = rootProject.task("publishEventuateArtifacts",
                type: PublishEventuateArtifactsTask,
                group: 'build setup',
                description: "Publish Eventuate Artifacts")
        publishTask.finalizedBy("closeAndReleaseRepository")

    }
}
