package com.github.thibseisel.mangabind.ui

import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

class UiRunner : Application() {

    override fun start(primaryStage: Stage) {
        primaryStage.title = "MangaBind"

        val rootLayout: BorderPane = FXMLLoader().apply {
            location = UiRunner::class.java.getResource("/layout/mangalist.fxml")
        }.load()

        primaryStage.scene = Scene(rootLayout)
        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(UiRunner::class.java, *args)
}