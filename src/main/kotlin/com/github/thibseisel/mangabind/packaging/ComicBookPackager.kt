package com.github.thibseisel.mangabind.packaging

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Named

/**
 * Packages the downloaded images as a CBZ archive file.
 */
class ComicBookPackager
@Inject constructor(
    @Named("tmpDir") private val imagesDir: File
) : Packager {

    override fun create(dest: String) {
        check(imagesDir.exists()) {
            "Attempt to create a CBZ file before downloading images"
        }

        val cbzArchive = FileOutputStream(dest)

        try {
            ZipOutputStream(cbzArchive).use { zip ->
                imagesDir.walkTopDown()
                        .filter(File::isFile)
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

        } catch (ze: ZipException) {

        } catch (ioe: IOException) {

        }
    }
}