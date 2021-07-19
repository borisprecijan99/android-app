package pmf.rma.voiceassistant.utils;

import android.app.Activity;

import java.util.Locale;

public class TextToSpeech {
    private android.speech.tts.TextToSpeech textToSpeech;

    public TextToSpeech(Activity activity, String initText) {
        this.textToSpeech = new android.speech.tts.TextToSpeech(activity.getApplicationContext(), status -> {
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
                if (initText != null)
                    speak(initText);
            } else {
                throw new RuntimeException();
            }
        });
    }

    public TextToSpeech(Activity activity) {
        this(activity, null);
    }

    public void speak(String text) {
        textToSpeech.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD, null, null);
    }

    public void shutdown() {
        textToSpeech.shutdown();
    }

    public void stop() {
        textToSpeech.stop();
    }

    public boolean isSpeaking() {
        return textToSpeech.isSpeaking();
    }
}
