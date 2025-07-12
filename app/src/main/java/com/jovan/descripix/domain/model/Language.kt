package com.jovan.descripix.domain.model


sealed class Language(val name: String, val code: String) {
    data object English : Language("English", "en")
    data object Indonesia : Language("Indonesia", "id")

    companion object {
        fun getAllLanguages() = listOf(English, Indonesia)

        fun fromCode(code: String): Language {
            return when (code) {
                "en" -> English
                "id" -> Indonesia
                else -> English // Default fallback
            }
        }
    }
}