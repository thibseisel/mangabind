package com.github.thibseisel.mangabind.packaging

import java.io.File

interface Packager {
    fun create(dest: String): File

    enum class Output(val translationKey: String) {
        FOLDER("packagingFolder"),
        CBZ("packagingCbz");
    }
}