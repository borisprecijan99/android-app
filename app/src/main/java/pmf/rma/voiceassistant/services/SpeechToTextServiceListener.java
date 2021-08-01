package pmf.rma.voiceassistant.services;

import android.os.Bundle;
import android.speech.RecognitionListener;

public class SpeechToTextServiceListener implements RecognitionListener {
    private final SpeechToTextServiceCallback speechToTextServiceCallback;

    public SpeechToTextServiceListener(SpeechToTextServiceCallback speechToTextServiceCallback) {
        this.speechToTextServiceCallback = speechToTextServiceCallback;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        speechToTextServiceCallback.onReadyForSpeech(params);
    }

    @Override
    public void onBeginningOfSpeech() {
        speechToTextServiceCallback.onBeginningOfSpeech();
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        speechToTextServiceCallback.onRmsChanged(rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        speechToTextServiceCallback.onBufferReceived(buffer);
    }

    @Override
    public void onEndOfSpeech() {
        speechToTextServiceCallback.onEndOfSpeech();
    }

    @Override
    public void onError(int error) {
        speechToTextServiceCallback.onError(error);
    }

    @Override
    public void onResults(Bundle results) {
        speechToTextServiceCallback.onResults(results);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        speechToTextServiceCallback.onPartialResults(partialResults);
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        speechToTextServiceCallback.onEvent(eventType, params);
    }
}
