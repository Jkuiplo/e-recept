package com.google.eRecept.utils

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun SharedPreferences.getStringFlow(
    key: String,
    defaultValue: String = "",
): Flow<String> =
    callbackFlow {
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, changedKey ->
                if (changedKey == key) {
                    trySend(sharedPreferences.getString(key, defaultValue) ?: defaultValue)
                }
            }

        registerOnSharedPreferenceChangeListener(listener)

        trySend(getString(key, defaultValue) ?: defaultValue)

        awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
    }
