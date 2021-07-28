package pmf.rma.voiceassistant.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Locale;

public class TextToSpeech extends Service {
    private android.speech.tts.TextToSpeech textToSpeech;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    /*public TextToSpeech(Context context, String initText) {
        this.textToSpeech = new android.speech.tts.TextToSpeech(context, status -> {
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
                if (initText != null)
                    speak(initText);
            } else {
                throw new RuntimeException();
            }
        });
    }

    public TextToSpeech(Context context) {
        this(context, null);
    }*/

    public TextToSpeech() {

    }

    public void initialize(String initText) {
        this.textToSpeech = new android.speech.tts.TextToSpeech(context, status -> {
            if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
                if (initText != null)
                    speak(initText);
            } else {
                throw new RuntimeException();
            }
        });
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

    public class TextToSpeechBinder extends Binder {
        public TextToSpeech getService() {
            return TextToSpeech.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new TextToSpeechBinder();
    }
}
