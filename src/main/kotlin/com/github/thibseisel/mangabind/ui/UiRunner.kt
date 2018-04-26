package com.github.thibseisel.mangabind.ui

import com.github.thibseisel.mangabind.dagger.DaggerUiComponent
import com.github.thibseisel.mangabind.dagger.FilenameProviderModule
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import javax.inject.Inject

/**
 * Defines the entry point for the Graphical User Interface application.
 */
class UiRunner : Application() {

    @Inject lateinit var fxmlLoader: FXMLLoader

    override fun start(primaryStage: Stage) {

        val component = DaggerUiComponent.builder()
                .bindsPrimaryStage(primaryStage)
                .filenameProvider(FilenameProviderModule("pages"))
                .build()
        component.inject(this)

        primaryStage.title = "MangaBind"

        fxmlLoader.location = UiRunner::class.java.getResource("/layout/main.fxml")
        val rootLayout = fxmlLoader.load<Parent>()
        primaryStage.scene = Scene(rootLayout)
        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(UiRunner::class.java, *args)
}