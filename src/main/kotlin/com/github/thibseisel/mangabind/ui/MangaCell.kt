package com.github.thibseisel.mangabind.ui

import com.github.thibseisel.mangabind.source.MangaSource
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell

/**
 * A custom ListCell dedicated to the display of a manga source.
 * It features two text labels, respectively for the title of manga and its hostname.
 */
class MangaCell : ListCell<MangaSource>() {

    private val itemView: Node
    private val controller: MangaCellController

    init {
        val loader = FXMLLoader(MangaCell::class.java.getResource("/layout/cell_manga.fxml"))
        itemView = loader.load()
        controller = loader.getController()
    }

    override fun updateItem(item: MangaSource?, empty: Boolean) {
        super.updateItem(item, empty)
        text = null

        if (empty || item == null) {
            graphic = null
        } else {
            graphic = itemView
            controller.updateItem(item)
        }
    }
}

class MangaCellController {

    @FXML
    private lateinit var title: Label

    @FXML
    private lateinit var origin: Label

    fun updateItem(item: MangaSource) {
        title.text = item.title
        origin.text = item.origin
    }
}