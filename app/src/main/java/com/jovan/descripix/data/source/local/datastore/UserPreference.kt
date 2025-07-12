package com.jovan.descripix.data.source.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jovan.descripix.domain.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore by preferencesDataStore( name = "session")
private val TOKEN_KEY = stringPreferencesKey("token")
private val REFRESH_KEY = stringPreferencesKey("refresh")
private val IS_LOGIN_KEY = booleanPreferencesKey("is_login")
private val LANGUAGE_CODE_KEY = stringPreferencesKey("language_code")

class UserPreference @Inject constructor(private val dataStore: DataStore<Preferences>){

    suspend fun saveSession(user: SessionData) {
        dataStore.edit { preferences ->
            preferences[REFRESH_KEY] = user.refreshToken
            preferences[TOKEN_KEY] = user.token
            preferences[IS_LOGIN_KEY] = true
        }
    }

    fun getSession(): Flow<SessionData> {
        return dataStore.data.map { preferences ->
            SessionData(
                refreshToken = preferences[REFRESH_KEY] ?: "",
                token = preferences[TOKEN_KEY] ?: "",
                isLogin = preferences[IS_LOGIN_KEY] ?: false
            )
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun saveLanguage(language: Language) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_CODE_KEY] = language.code
        }
    }

    fun getLanguage(): Flow<Language> {
        return dataStore.data.map { preferences ->
            val languageCode = preferences[LANGUAGE_CODE_KEY] ?: "en"
            Language.fromCode(languageCode)
        }
    }
}