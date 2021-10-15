package pmf.rma.voiceassistant.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;

import androidx.annotation.Nullable;

import java.util.Locale;

public class TextToSpeechService extends Service {
    private TextToSpeech textToSpeech;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public TextToSpeechService() {

    }

    public void initialize(String initText) {
        this.textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
                if (initText != null)
                    speak(initText);
            } else {
                throw new RuntimeException();
            }
        });
    }

    public void speak(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, null);
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

    public class TextToSpeechServiceBinder extends Binder {
        public TextToSpeechService getService() {
            return TextToSpeechService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TextToSpeechServiceBinder();
    }
}
