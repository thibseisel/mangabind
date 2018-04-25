package com.github.thibseisel.mangabind.ui

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage

/**
 * Defines the entry point for the Graphical User Interface application.
 */
class UiRunner : Application() {

    override fun start(primaryStage: Stage) {
        primaryStage.title = "MangaBind"

        val rootLayout = FXMLLoader.load<Parent>(UiRunner::class.java.getResource("/layout/main.fxml"))
        primaryStage.scene = Scene(rootLayout)
        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(UiRunner::class.java, *args)
}