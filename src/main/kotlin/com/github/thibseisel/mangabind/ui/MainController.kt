package com.github.thibseisel.mangabind.ui

import com.github.thibseisel.mangabind.dagger.FXController
import com.github.thibseisel.mangabind.source.MangaSource
import com.github.thibseisel.mangabind.source.SourceLoader
import javafx.fxml.FXML
import javafx.scene.control.ListView
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineStart
import kotlinx.coroutines.experimental.javafx.JavaFx
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject

class MainController
@Inject constructor(
        private val sourceLoader: SourceLoader
) : FXController {

    @FXML
    private lateinit var mangaListView: ListView<MangaSource>

    @FXML
    override fun initialize() {
        mangaListView.setCellFactory { MangaCell() }

        launch(JavaFx, start = CoroutineStart.UNDISPATCHED) {
            val mangas = loadSourcesAsync()
            mangaListView.items.setAll(mangas)
        }
    }

    private suspend fun loadSourcesAsync(): List<MangaSource> = withContext(CommonPool) {
        sourceLoader.loadAll()
    }
}