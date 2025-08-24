package io.eventuate.plugins.gradle.versions

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when;

public class GradlePropertiesFileUpgraderTest {

  @org.junit.jupiter.api.Test
  public void shouldUpgradeFile() throws IOException {
    def inputFile = File.createTempFile("input", ".properties");
    def outputFile = File.createTempFile("output", ".properties");

    def bintray = mock(Bintray)

    def upgrader = new GradlePropertiesFileUpgrader(bintray)

    inputFile.withWriter('utf-8') { writer ->
        writer.writeLine "eventuateCommonVersion=1.0.RELEASE"
    }

    when(bintray.fetchLatestVersion("eventuate-common")).thenReturn("2.0.RELEASE")

    upgrader.upgradePropertyFile(inputFile.getAbsolutePath(), outputFile.getAbsolutePath())

    assertEquals(["eventuateCommonVersion=2.0.RELEASE"], outputFile.readLines())
  }

}