package com.github.thibseisel.mangabind

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL

class MangaSource
@JsonCreator constructor(

        @JsonProperty("id")
        val id: Long,

        @JsonProperty("title")
        val title: String,

        @JsonProperty("start_page")
        val startPage: Int,

        @JsonProperty("single_pages")
        val singlePages: List<String>,

        @JsonProperty("double_pages")
        val doublePages: List<String>?
) {
        @JsonIgnore
        val baseUrl: String = singlePages.firstOrNull()?.let {
                URL(it).authority
        } ?: ""
}