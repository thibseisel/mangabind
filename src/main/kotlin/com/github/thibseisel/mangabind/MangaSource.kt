package com.github.thibseisel.mangabind

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class MangaSource
@JsonCreator constructor(

        @JsonProperty("id")
        val id: Long,

        @JsonProperty("title")
        val title: String,

        @JsonProperty("base_url")
        val baseUrl: String,

        @JsonProperty("single_pages")
        val singlePages: List<String>?,

        @JsonProperty("double_pages")
        val doublePages: List<String>?,

        @JsonProperty("start_page") val startPage: Int
)