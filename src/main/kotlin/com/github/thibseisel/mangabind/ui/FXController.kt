package com.github.thibseisel.mangabind.ui

import javafx.fxml.Initializable
import javafx.fxml.FXML

/**
 * An interface shared by all JavaFX controllers.
 *
 * It defines a single parameterless [initialize] method, replacing the one defined in [Initializable]
 * (as it has been superseded with automatic injection of `location` and `resources` into the controller)
 * while keeping a contract common to all controllers.
 */
interface FXController {

    /**
     * Called once when the contents of the document associated with this controller has been completely loaded.
     * This allows performing any necessary post-processing of the content.
     *
     * Implementations are required to annotate this method with [FXML]
     * in order to be automatically called by the JavaFX framework.
     */
    fun initialize()
}