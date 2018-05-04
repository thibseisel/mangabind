package com.github.thibseisel.mangabind.ui

import com.github.thibseisel.mangabind.dagger.CommonModule
import com.github.thibseisel.mangabind.dagger.FileProviderModule
import dagger.BindsInstance
import dagger.Component
import javafx.stage.Stage
import javax.inject.Singleton

/**
 * The main Dagger component for the Graphical Interface entry point.
 * Its is bound to the whole application scope and should be initialized
 * as soon as the graphical framework is ready.
 */
@Singleton
@Component(modules = [
    CommonModule::class,
    FXModule::class
])
interface UiComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance fun bindsPrimaryStage(primaryStage: Stage): Builder
        fun filenameProvider(module: FileProviderModule): Builder
        fun build(): UiComponent
    }

    fun inject(ui: UiRunner)
}