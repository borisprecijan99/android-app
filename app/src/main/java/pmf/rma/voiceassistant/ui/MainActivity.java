package pmf.rma.voiceassistant.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import pmf.rma.voiceassistant.utils.SpeechToText;
import pmf.rma.voiceassistant.utils.SpeechToTextCallback;
import pmf.rma.voiceassistant.utils.TextToSpeech;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SpeechToTextCallback {
    private static final String INITIAL_TEXT = "Zdravo! Dobrodošli u aplikaciju Glasovni pomoćnik. Čekam Vašu prvu komandu.";

    private TextToSpeech textToSpeech;
    private SpeechToText speechToText;
    private ImageButton startListeningButton;
    private boolean clicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CALL_PHONE, Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.SEND_SMS}, PackageManager.PERMISSION_GRANTED);

        startListeningButton = findViewById(R.id.speakButton);

        textToSpeech = new TextToSpeech(this, INITIAL_TEXT);
        speechToText = new SpeechToText(this, this);
    }

    public void onButtonClick(View view) {
        if (!clicked) {
            speechToText.startListening();
        } else {
            speechToText.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        textToSpeech.shutdown();
        speechToText.destroy();
        super.onDestroy();
    }

    @Override
    public void onResults(Bundle results) {
        String result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
        textToSpeech.speak(result);
        startListeningButton.setImageResource(R.drawable.microphone);
        clicked = false;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        startListeningButton.setImageResource(R.drawable.microphone_off);
        clicked = true;
    }

    @Override
    public void onError(int error) {
        startListeningButton.setImageResource(R.drawable.microphone);
        clicked = false;
    }
}