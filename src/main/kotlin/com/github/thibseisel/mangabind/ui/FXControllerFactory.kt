package com.github.thibseisel.mangabind.ui

import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import javafx.util.Callback
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.annotation.AnnotationTarget
import kotlin.reflect.KClass

/**
 * Configures JavaFX controllers whose instances should be created by [FXControllerFactory].
 * This is required for dependencies to be injected as constructor parameters.
 */
@Module
@Suppress("unused")
abstract class FxControllerFactoryModule {

    @Binds @IntoMap
    @ControllerKey(MainController::class)
    abstract fun bindsMainController(controller: MainController): FXController
}

/**
 * Creates instances of JavaFX Controllers by injecting Dagger dependencies into their constructor.
 *
 * Controllers that have a non-empty constructor should be registered in the [FxControllerFactoryModule]
 * in order to be constructed by this factory.
 * Otherwise, this will attempt to create an instance of the controller by reflection,
 * using the parameterless constructor, failing with an exception if no such constructor exists.
 */
@Singleton
class FXControllerFactory
@Inject constructor(
        private val controllerProviders: Map<Class<out FXController>, @JvmSuppressWildcards Provider<FXController>>
): Callback<Class<*>, Any> {

    @Suppress("unchecked_cast")
    override fun call(controllerClass: Class<*>): Any {
        val injector = controllerProviders[controllerClass as Class<out FXController>]
        return if (injector != null)
            injector.get()
        else try {
            controllerClass.newInstance()
        } catch (ie: InstantiationException) {
            throw IllegalStateException("${controllerClass.simpleName} is not registered in FXControllerFactoryModule.", ie)
        }
    }
}

/**
 * Defines keys for mapping [FXController] classes to their instances in a Dagger 2 Map-Multibinding.
 */
@MapKey
@Target(AnnotationTarget.FUNCTION)
annotation class ControllerKey(val value: KClass<out FXController>)