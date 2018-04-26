package com.github.thibseisel.mangabind.source

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.thibseisel.mangabind.toUrl

/**
 * Representation of a manga whose chapters are available through a remote server.
 */
class MangaSource
@JsonCreator constructor(

        /**
         * The unique identifier of a manga resource.
         */
        @JsonProperty("id")
        val id: Long,

        /**
         * The display name of the manga this resource represents.
         */
        @JsonProperty("title")
        val title: String,

        /**
         * The index of the first page of the chapter.
         * This is useful for websites whose page number starts at 1 instead of 0,
         * or to skip a predetermined number of "adds pages" placed before the actual chapter.
         */
        @JsonProperty("start_page")
        val startPage: Int,

        /**
         * A list of template URLs matching image resources in the remote server.
         * Each template URL in this list represents a possible path to a chapter page,
         * sorted by descending probability.
         *
         * URLs may contain the following parameters, replaced by their value at runtime:
         * - `[c]` the chapter number.
         * - `[p]` the page index.
         *
         * Those characters (`c` or `p`) may be prefixed by a positive number `n` which is the desired number of digits.
         * If the number has `m` digits and `m < n`, then that number will be prepended by `n - m` zeros.
         *
         * For example, the template URL `https://example.com/manga/[c]/[2p].png`
         * will be interpolated at runtime as
         * - `https://example.com/manga/6/04.png` for chapter 6 and page 4,
         * - `https://example.com/manga/155/16.png` for chapter 155 and page 16.
         */
        @JsonProperty("single_pages")
        val singlePages: List<String>,

        /**
         * An optional list of templates URLs matching double-page images in the remote server.
         * Some servers may give a special name to double page resource images.
         * This list is therefore used as a fallback for cases where every urls in [singlePages] have failed.
         *
         * This is the same as [singlePages], but this features an additional parameter `[q]`
         * which is the page index of a regarding page (basically `p+1`).
         *
         * For example, the template URL `https://example.com/manga/[c]/[2p]-[2q].png`
         * will be interpolated at runtime as
         * - `https://example.com/manga/6/04-05.png` for chapter 6 and double page 4-5,
         * - `https://example.com/manga/155/16-17.png` for chapter 155 and double page 16-17.
         */
        @JsonProperty("double_pages")
        val doublePages: List<String>?
) {
        /**
         * The name of the website that hosts the downloaded images.
         */
        @JsonIgnore
        val origin: String = singlePages.firstOrNull()?.toUrl()?.authority ?: ""
}