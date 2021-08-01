package pmf.rma.voiceassistant.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.annotation.Nullable;

import java.util.Locale;

public class SpeechToTextService extends Service {
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public SpeechToTextService() {

    }

    public void initialize(SpeechToTextServiceCallback speechToTextServiceCallback) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        speechRecognizer.setRecognitionListener(new SpeechToTextServiceListener(speechToTextServiceCallback));
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

    public void setSpeechToTextCallback(SpeechToTextServiceCallback speechToTextServiceCallback) {
        speechRecognizer.setRecognitionListener(new SpeechToTextServiceListener(speechToTextServiceCallback));
    }

    public class SpeechToTextServiceBinder extends Binder {
        public SpeechToTextService getService() {
            return SpeechToTextService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new SpeechToTextServiceBinder();
    }
}
