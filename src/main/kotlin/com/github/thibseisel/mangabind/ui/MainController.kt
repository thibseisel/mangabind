package com.github.thibseisel.mangabind.ui

import com.github.thibseisel.mangabind.source.MangaSource
import javafx.fxml.FXML
import javafx.scene.control.ListView

class MainController {

    @FXML
    private lateinit var mangaListView: ListView<MangaSource>

    /**
     * Called once when the contents of the document associated with this controller has been completely loaded.
     * This allows performing any necessary post-processing of the content.
     */
    @FXML fun initialize() {
        mangaListView.setCellFactory { MangaCell() }
        mangaListView.items.setAll(listOf(
            MangaSource(1L, "Tokyo Ghoul Re", 1, listOf("http://lirescan.com/tokyoghoulre"), null),
            MangaSource(2L, "My Hero Academia", 1, listOf("http://japscan.cc/myhero"), null)
        ))
    }
}