package com.github.thibseisel.mangabind.ui

import com.github.thibseisel.mangabind.source.MangaRepository
import com.github.thibseisel.mangabind.source.MangaSource
import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.ListView
import javafx.scene.control.Slider
import javafx.scene.control.TextField
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import javax.inject.Inject

class MainController
@Inject constructor(
        private val mangaRepository: MangaRepository
) : FXController {

    @FXML
    private lateinit var mangaListView: ListView<MangaSource>

    @FXML
    private lateinit var mangaDetailPane: Parent

    @FXML
    private lateinit var titleInput: TextField

    @FXML
    private lateinit var authorInput: TextField

    @FXML
    private lateinit var startPageSlider: Slider

    @FXML
    private lateinit var templateListView: ListView<String>

    @FXML
    private lateinit var doublePageListView: ListView<String>

    @FXML
    override fun initialize() {
        mangaListView.setCellFactory { _ -> MangaCell() }
        mangaListView.setOnMouseClicked { _ ->
            val selectedSource = mangaListView.selectionModel.selectedItem
            showDetails(selectedSource)
        }

        mangaDetailPane.isVisible = false

        launch(JavaFx) {
            mangaRepository.getAll().consumeEach { sources ->
                mangaListView.items.setAll(sources)
            }
        }
    }

    private fun showDetails(source: MangaSource) {
        mangaDetailPane.isVisible = true
        titleInput.text = source.title
        authorInput.text = source.author
        startPageSlider.value = source.startPage.toDouble()
        templateListView.items.setAll(source.singlePages)
        doublePageListView.items.setAll(source.doublePages ?: emptyList())
    }
}