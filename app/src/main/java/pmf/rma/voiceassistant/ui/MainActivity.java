package pmf.rma.voiceassistant.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jayway.jsonpath.JsonPath;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import okhttp3.ResponseBody;
import pmf.rma.voiceassistant.database.AppDatabase;
import pmf.rma.voiceassistant.database.entity.CommandEntity;
import pmf.rma.voiceassistant.database.entity.JokeEntity;
import pmf.rma.voiceassistant.unnamed.CyrillicLatinConverter;
import pmf.rma.voiceassistant.unnamed.GoogleKnowledgeGraphSearchApi;
import pmf.rma.voiceassistant.unnamed.MyReceiver;
import pmf.rma.voiceassistant.unnamed.PomocnaKlasa;
import pmf.rma.voiceassistant.unnamed.RetrofitClient;
import pmf.rma.voiceassistant.utils.SpeechToText;
import pmf.rma.voiceassistant.utils.SpeechToTextCallback;
import pmf.rma.voiceassistant.utils.StringEditDistance;
import pmf.rma.voiceassistant.utils.TextToSpeech;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static pmf.rma.voiceassistant.database.DataGenerator.*;

public class MainActivity extends AppCompatActivity implements SpeechToTextCallback {
    private static final String INITIAL_TEXT = "Zdravo! Dobrodošli u aplikaciju Glasovni pomoćnik.";

    private TextToSpeech textToSpeech;
    private SpeechToText speechToText;
    private ImageButton startListeningButton;
    private TextView speechTextView;
    private boolean clicked;
    private List<CommandEntity> commands;
    private List<JokeEntity> jokes;
    private PomocnaKlasa utils;
    private GoogleKnowledgeGraphSearchApi googleKnowledgeGraphSearchApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CALL_PHONE, Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.SEND_SMS,
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

        startListeningButton = findViewById(R.id.speakButton);
        speechTextView = findViewById(R.id.textViewSpeech);

        AppDatabase.databaseExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            commands = db.commandDao().getAll();
            jokes = db.jokeDao().getAll();
        });

        //textToSpeech = new TextToSpeech(this, INITIAL_TEXT);
        speechToText = new SpeechToText(this, this);

        this.utils = new PomocnaKlasa(this);

        googleKnowledgeGraphSearchApi = RetrofitClient.getInstance().getGoogleKnowledgeGraphSearchApi();
    }

    public void onButtonClick(View view) {
        if (!clicked) {
            speechToText.startListening();
        } else {
            speechToText.cancel();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent(this, MyReceiver.class);
        intent.setAction("pmf.rma.voiceassistant.NOTIFICATIONS_ON");
        sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, MyReceiver.class);
        intent.setAction("pmf.rma.voiceassistant.NOTIFICATIONS_ON");
        sendBroadcast(intent);
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
        speechTextView.setText(result);
        startListeningButton.setImageResource(R.drawable.microphone);
        test(result);
        clicked = false;
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        String result = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
        speechTextView.setText(result);
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

    private void test(String result) {
        boolean commandFound = false;
        for (CommandEntity command: commands) {
            if (result.matches(command.getRegularExpression())) {
                switch (command.getRegularExpression()) {
                    case TELL_A_JOKE_REGEX:
                        if (jokes.size() != 0) {
                            Random random = new Random();
                            int size = jokes.size();
                            int index = random.nextInt(size);
                            textToSpeech.speak(jokes.get(index).getText());
                        } else {
                            textToSpeech.speak(getString(R.string.jokeNotFound));
                        }
                        break;
                    case TURN_ON_BLUETOOTH_REGEX:
                        boolean isTurnedOn = utils.turnOnBluetooth();
                        if (isTurnedOn)
                            textToSpeech.speak("Uključujem blutut.");
                        else
                            textToSpeech.speak("Blutut je već uključen.");
                        break;
                    case TURN_OFF_BLUETOOTH_REGEX:
                        boolean isTurnedOff = utils.turnOffBluetooth();
                        if (isTurnedOff)
                            textToSpeech.speak("Isključujem blutut.");
                        else
                            textToSpeech.speak("Blutut je već isključen.");
                        break;
                    case TURN_ON_FLASHLIGHT_REGEX:
                        utils.turnOnFlashlight();
                        textToSpeech.speak("Uključujem lampu.");
                        break;
                    case TURN_OFF_FLASHLIGHT_REGEX:
                        utils.turnOffFlashlight();
                        textToSpeech.speak("Isključujem lampu.");
                        break;
                    case TURN_ON_WIFI_REGEX:

                        textToSpeech.speak("Uključujem WiFi...");
                        break;
                    case TURN_OFF_WIFI_REGEX:

                        textToSpeech.speak("Isključujem WiFi...");
                        break;
                    case TAKE_A_SCREENSHOT_REGEX:
                        utils.takeAScreenshot();
                        textToSpeech.speak("Pravim snimak ekrana...");
                        break;
                    case WHAT_TIME_IS_IT_REGEX:
                        LocalTime now = LocalTime.now();
                        textToSpeech.speak("Trenutno je " + now.getHour() + " časova i " + now.getMinute() + " minuta.");
                        break;
                    case WHAT_IS_THE_DATE_REGEX:
                        LocalDate today = LocalDate.now();
                        int day = today.getDayOfMonth();
                        String month = today.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
                        int year = today.getYear();
                        textToSpeech.speak("Danas je " + day + ". " + month + " " + year + ". godine.");
                        break;
                    case PLAY_MUSIC_REGEX:
                        textToSpeech.speak("Tražim neku pesmu na Vašem uređaju.");
                        utils.playMusic();
                        break;
                    case STOP_MUSIC_REGEX:
                        textToSpeech.speak("Zaustavljam pesmu.");
                        utils.stopMusic();
                        break;
                    case PAUSE_MUSIC_REGEX:
                        textToSpeech.speak("Pauziram pesmu.");
                        utils.pauseMusic();
                        break;
                    case PHONE_CALL_REGEX:
                        String number = result.replaceAll("[a-zA-z\\s]", "");
                        textToSpeech.speak("Pozivam broj telefona " + number + ".");
                        utils.call(number);
                        break;
                }
                commandFound = true;
                break;
            }
        }
        if (!commandFound) {
            textToSpeech.speak("Ne znam da izvršim Vašu komandu, pa ću pokušati da pretražim na Google-u rezultate za " + result);
            googleKnowledgeGraphSearchApi.getResult(result).enqueue(
                    new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                                if (response.body() != null) {
                                    JSONParser parser = new JSONParser();
                                    String resp = response.body().string();
                                    JSONObject responseJson = (JSONObject) parser.parse(resp);
                                    JSONArray elements = (JSONArray) responseJson.get("itemListElement");
                                    String name;
                                    String text = null;
                                    int minDistance = Integer.MAX_VALUE;
                                    for (Object element: elements) {
                                        name = JsonPath.read(element, "$.result.name").toString();
                                        name = CyrillicLatinConverter.cyrilicToLatin(name);
                                        int currentDistance = StringEditDistance.getEditDistance(name, result);
                                        if (currentDistance < minDistance){
                                            minDistance = currentDistance;
                                            text = JsonPath.read(element, "$.result.detailedDescription.articleBody");
                                        }
                                    }
                                    textToSpeech.speak(text);
                                } else {
                                    textToSpeech.speak("Nisam pronašla nijedan rezultat.");
                                }
                            } catch (Exception e) {
                                textToSpeech.speak("Nisam pronašla nijedan rezultat.");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            textToSpeech.speak(getString(R.string.commandNotFound));
                        }
                    }
            );
        }
    }
}