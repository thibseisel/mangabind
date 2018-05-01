package com.github.thibseisel.mangabind.packaging

import com.github.thibseisel.mangabind.isImageFile
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Named

class ComicBookPackager
@Inject constructor(
    @Named("outputDir") tmpPath: String
) {

    private val imageTempDir = File(tmpPath)

    fun create(cbzFilepath: String) {
        check(imageTempDir.exists()) {
            "Attempt to create a CBZ file before downloading images"
        }

        val cbzArchive = FileOutputStream(cbzFilepath)

        ZipOutputStream(cbzArchive).use { zip ->
            imageTempDir.walkTopDown()
                .filter(File::isImageFile)
                .forEachIndexed { index, file ->
                    val newName = "%02d.%s".format(index, file.name.substringAfterLast('.'))
                    val entry = ZipEntry(newName)

                    zip.putNextEntry(entry)
                    file.inputStream().use {
                        it.copyTo(zip)
                    }
                    zip.closeEntry()
                }
        }
    }
}