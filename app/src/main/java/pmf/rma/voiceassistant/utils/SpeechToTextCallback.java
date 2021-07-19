package pmf.rma.voiceassistant.utils;

import android.os.Bundle;

public interface SpeechToTextCallback {
    default void onReadyForSpeech(Bundle params) { }

    default void onBeginningOfSpeech() { }

    default void onRmsChanged(float rmsdB) { }

    default void onBufferReceived(byte[] buffer) { }

    default void onEndOfSpeech() { }

    default void onError(int error) { }

    void onResults(Bundle results);

    default void onPartialResults(Bundle partialResults) { }

    default void onEvent(int eventType, Bundle params) { }
}
