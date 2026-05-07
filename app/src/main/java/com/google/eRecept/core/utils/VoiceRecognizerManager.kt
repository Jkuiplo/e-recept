package com.google.eRecept.core.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    data class Recognizing(val partialText: String) : VoiceState()
    data class Success(val text: String) : VoiceState()
    data class Error(val message: String) : VoiceState()
}

class VoiceRecognizerManager(private val context: Context) : RecognitionListener {
    private var speechRecognizer: SpeechRecognizer? = null
    private val _state = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _state.value = VoiceState.Error("Распознавание речи не поддерживается на этом устройстве")
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(this@VoiceRecognizerManager)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ru-RU")
                putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayOf("ru-RU", "ru_RU"))
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }
            startListening(intent)
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun cancelListening() {
        speechRecognizer?.cancel()
        _state.value = VoiceState.Idle
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    override fun onReadyForSpeech(params: Bundle?) { _state.value = VoiceState.Listening }
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
        val msg = when (error) {
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Таймаут сети"
            SpeechRecognizer.ERROR_NETWORK -> "Ошибка сети"
            SpeechRecognizer.ERROR_AUDIO -> "Ошибка записи звука"
            SpeechRecognizer.ERROR_CLIENT -> "Ошибка клиента"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Ничего не было сказано"
            SpeechRecognizer.ERROR_NO_MATCH -> "Речь не распознана"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Служба занята"
            else -> "Неизвестная ошибка ($error)"
        }
        _state.value = VoiceState.Error(msg)
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _state.value = VoiceState.Success(matches[0])
        } else {
            _state.value = VoiceState.Idle
        }
    }

    override fun onPartialResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty() && _state.value !is VoiceState.Error) {
            _state.value = VoiceState.Recognizing(matches[0])
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}
}