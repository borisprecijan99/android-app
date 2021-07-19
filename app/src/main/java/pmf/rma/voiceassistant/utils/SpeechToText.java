package pmf.rma.voiceassistant.utils;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.Locale;

public class SpeechToText {
    private final SpeechRecognizer speechRecognizer;
    private final Intent speechRecognizerIntent;

    public SpeechToText(Activity activity, SpeechToTextCallback speechToTextCallback) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.setRecognitionListener(new SpeechToTextListener(speechToTextCallback));
    }

    public void startListening() {
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    public void stopListening() {
        speechRecognizer.stopListening();
    }

    public void destroy() {
        speechRecognizer.destroy();
    }

    public void cancel() {
        speechRecognizer.cancel();
    }
}
