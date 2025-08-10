package io.eventuate.plugins.gradle.publish

import org.gradle.api.Project

class PublishMultiArchContainerImagesExtension {
    private Project project
    private Set<String> images = new LinkedHashSet<>()
    
    PublishMultiArchContainerImagesExtension(Project project) {
        this.project = project
    }
    
    void image(String imageName) {
        images.add(imageName)
    }
    
    Set<String> getImages() {
        return images
    }
}