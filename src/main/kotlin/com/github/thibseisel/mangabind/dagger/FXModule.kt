package com.github.thibseisel.mangabind.dagger

import dagger.Module
import dagger.Provides
import javafx.fxml.FXMLLoader

@Module(includes = [
    FxControllerFactoryModule::class
])
object FXModule {

    /**
     * Provides a [FXMLLoader] configured with the [FXControllerFactory], allowing to create controller instances
     * with Dagger-injected dependencies.
     */
    @JvmStatic @Provides
    fun providesFXmlLoader(factory: FXControllerFactory) = FXMLLoader().apply {
        controllerFactory = factory
    }
}