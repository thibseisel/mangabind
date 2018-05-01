package com.github.thibseisel.mangabind.packaging

import com.github.thibseisel.mangabind.isImageFile
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class FolderPackager
@Inject constructor(
    @Named("outputDir") outDirPath: String
) {

    private val outDir =  File(outDirPath)

    fun create(dirName: String) {
        val inDir = File("pages")
        inDir.walkTopDown()
            .filter(File::isImageFile)
            .forEach {
            it.copyTo(outDir, overwrite = true)
        }
    }
}