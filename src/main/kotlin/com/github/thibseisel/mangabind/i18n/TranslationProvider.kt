package com.github.thibseisel.mangabind.i18n

import java.util.*
import javax.inject.Inject

/**
 * Provides text to be displayed to users in their native language.
 *
 * This implementation retrieves translations from a set of properties files located in the application resources.
 * Each file defines a different languages text is translated into,
 * following the naming convention of the [ResourceBundle] class.
 */
class TranslationProvider
@Inject constructor() {

    private val bundle = ResourceBundle.getBundle("strings", Locale.getDefault())

    /**
     * Returns a translated String identified by its unique [key], substituting the specified format arguments if any.
     *
     * @param key The identifier of the string message to translate.
     * @param formatArgs Optional format parameters to replace in the translated String, as per [String.format].
     * @return A text String in the user's mother language.
     *
     * @throws IllegalArgumentException If the specified [key] matches no translatable String.
     * @throws IllegalFormatException If parameters cannot be replaced in the translated String.
     */
    fun getText(key: String, vararg formatArgs: Any): String {
        return try {
            val text = bundle.getString(key)
            if (formatArgs.isEmpty()) text else text.format(formatArgs)
        } catch (mre: MissingResourceException) {
            throw IllegalArgumentException("No translation with key \"$key\" has been defined.")
        }
    }
}