package com.github.thibseisel.mangabind.packaging

import java.io.File
import javax.inject.Inject
import javax.inject.Named

/**
 * Groups the downloaded images into a folder whose location is user-defined.
 * Each image will be named after the chapter it comes from and the number of the page.
 *
 * @param imagesDir The directory that contains the downloaded images to be copied to the destination folder.
 */
class FolderPackager
@Inject constructor(
    @Named("tmpDir") private val imagesDir: File
) : Packager {

    override fun create(dest: String): File {
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
                val destFile = File(destinationDir, it.name)
                it.copyTo(destFile)
            }

        return destinationDir
    }
}