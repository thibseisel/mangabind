package com.github.thibseisel.mangabind.dagger

import com.github.thibseisel.mangabind.ui.UiRunner
import dagger.BindsInstance
import dagger.Component
import javafx.stage.Stage
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    FileModule::class,
    FXModule::class
])
interface UiComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance fun bindsPrimaryStage(primaryState: Stage): Builder
        fun filenameProvider(module: FilenameProviderModule): Builder
        fun build(): UiComponent
    }

    fun inject(ui: UiRunner)
}