package com.github.thibseisel.mangabind.ui

import dagger.Module
import dagger.Provides
import javafx.fxml.FXMLLoader
import java.util.*

@Module(includes = [FxControllerFactoryModule::class])
class FXModule {

    /**
     * Provides a [FXMLLoader] configured with the [FXControllerFactory] to allow creating controller instances
     * with Dagger-injected dependencies, and the [ResourceBundle] to translate strings refered in FXML.
     */
    @Provides
    fun providesFXmlLoader(
        factory: FXControllerFactory,
        translations: ResourceBundle
    ) = FXMLLoader().apply {
        controllerFactory = factory
        resources = translations
    }
}