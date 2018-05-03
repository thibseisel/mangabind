package com.github.thibseisel.mangabind.packaging

import java.io.File
import javax.inject.Inject
import javax.inject.Named

/**
 * Groups the downloaded images into a folder whose location is user-defined.
 *
 * @param imagesDir The directory that contains the downloaded images.
 */
class FolderPackager
@Inject constructor(
    @Named("tmpDir") private val imagesDir: File
) : Packager {

    override fun create(dest: String) {
        val destinationDir = File(dest)
        require(!destinationDir.exists() || destinationDir.isDirectory) {
            "The specified output should be a directory"
        }

        check(imagesDir.exists()) {
            "Images should have been downloaded before creating a folder containing them"
        }

        // Create directory structure if necessary.
        destinationDir.mkdirs()

        imagesDir.walkTopDown()
            .filter(File::isFile)
            .forEach {
                // TODO File should be renamed
                val destFile = File(destinationDir, it.name)
                it.copyTo(destFile)
            }
    }
}