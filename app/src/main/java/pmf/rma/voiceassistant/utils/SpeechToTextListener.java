package pmf.rma.voiceassistant.utils;

import android.os.Bundle;
import android.speech.RecognitionListener;

public class SpeechToTextListener implements RecognitionListener {
    private final SpeechToTextCallback speechToTextCallback;

    public SpeechToTextListener(SpeechToTextCallback speechToTextCallback) {
        this.speechToTextCallback = speechToTextCallback;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        speechToTextCallback.onReadyForSpeech(params);
    }

    @Override
    public void onBeginningOfSpeech() {
        speechToTextCallback.onBeginningOfSpeech();
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        speechToTextCallback.onRmsChanged(rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        speechToTextCallback.onBufferReceived(buffer);
    }

    @Override
    public void onEndOfSpeech() {
        speechToTextCallback.onEndOfSpeech();
    }

    @Override
    public void onError(int error) {
        speechToTextCallback.onError(error);
    }

    @Override
    public void onResults(Bundle results) {
        speechToTextCallback.onResults(results);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        speechToTextCallback.onPartialResults(partialResults);
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        speechToTextCallback.onEvent(eventType, params);
    }
}
