package com.github.thibseisel.mangabind.ui

import com.github.thibseisel.mangabind.dagger.FXController
import com.github.thibseisel.mangabind.source.MangaSource
import com.github.thibseisel.mangabind.source.MangaRepository
import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.ListView
import javafx.scene.control.Slider
import javafx.scene.control.TextField
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
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

        launch(JavaFx, start = CoroutineStart.UNDISPATCHED) {
            val mangas = loadSourcesAsync()
            mangaListView.items.setAll(mangas)

            mangas.firstOrNull()?.let {
                titleInput.text = it.title
                startPageSlider.value = it.startPage.toDouble()
                templateListView.items.setAll(it.singlePages)
                doublePageListView.items.setAll(it.doublePages ?: emptyList())
            }
        }
    }

    private suspend fun loadSourcesAsync(): List<MangaSource> = withContext(CommonPool) {
        mangaRepository.getAll()
    }
}