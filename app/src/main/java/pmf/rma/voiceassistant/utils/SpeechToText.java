package pmf.rma.voiceassistant.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.annotation.Nullable;

import java.util.Locale;

public class SpeechToText extends Service {
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public SpeechToText(Context context, SpeechToTextCallback speechToTextCallback) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizer.setRecognitionListener(new SpeechToTextListener(speechToTextCallback));
    }

    public SpeechToText() {

    }

    public void initialize(SpeechToTextCallback speechToTextCallback) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
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

    public class SpeechToTextBinder extends Binder {
        public SpeechToText getService() {
            return SpeechToText.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new SpeechToTextBinder();
    }
}
