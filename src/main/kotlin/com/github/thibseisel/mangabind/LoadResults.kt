package com.github.thibseisel.mangabind

class PageResult(
    val isSuccessful: Boolean,
    val chapter: Int,
    val page: Int,
    val isDoublePage: Boolean = false
)

data class ChapterResult(
    val chapter: Int,
    val pages: List<PageResult>,
    val error: Throwable?
)