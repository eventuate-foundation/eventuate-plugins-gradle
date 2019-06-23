package io.eventuate.plugins.gradle.versions

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class UpgradeVersions implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        project.task("upgradeVersions", type: UpgradeVersionsTask)
    }
}
