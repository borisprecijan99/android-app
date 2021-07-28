package pmf.rma.voiceassistant.unnamed;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import pmf.rma.voiceassistant.utils.SpeechToText;
import pmf.rma.voiceassistant.utils.TextToSpeech;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Intent speechToTextService = new Intent(getApplicationContext(), SpeechToText.class);
        Intent textToSpeechService = new Intent(getApplicationContext(), TextToSpeech.class);
        getApplicationContext().bindService(speechToTextService, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Toast.makeText(MyApp.this, "SpeechToText servis je uspešno pokrenut.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
        getApplicationContext().bindService(textToSpeechService, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Toast.makeText(MyApp.this, "TextToSpeech servis je uspešno pokrenut.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
    }
}
