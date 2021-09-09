package pmf.rma.voiceassistant.ui;

import static pmf.rma.voiceassistant.utils.constants.RegularExpressions.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.SpeechRecognizer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jayway.jsonpath.JsonPath;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.List;

import okhttp3.ResponseBody;
import pmf.rma.voiceassistant.Global;
import pmf.rma.voiceassistant.R;
import pmf.rma.voiceassistant.database.entity.CommandEntity;
import pmf.rma.voiceassistant.utils.CyrillicLatinConverter;
import pmf.rma.voiceassistant.services.http.GoogleKnowledgeGraphSearchApi;
import pmf.rma.voiceassistant.receivers.NotificationBroadcastReceiver;
import pmf.rma.voiceassistant.services.http.RetrofitClient;
import pmf.rma.voiceassistant.services.SpeechToTextService;
import pmf.rma.voiceassistant.services.SpeechToTextServiceCallback;
import pmf.rma.voiceassistant.utils.PomocnaKlasa;
import pmf.rma.voiceassistant.utils.StringEditDistance;
import pmf.rma.voiceassistant.services.TextToSpeechService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SpeechToTextServiceCallback, ServiceConnection {
    private static final String INITIAL_TEXT = "Zdravo! Dobrodošli u aplikaciju Glasovni pomoćnik.";

    private TextToSpeechService textToSpeechService;
    private SpeechToTextService speechToTextService;
    private ImageButton startListeningButton;
    private TextView speechTextView;
    private boolean clicked;
    private List<CommandEntity> commands;
    private PomocnaKlasa utils;
    private GoogleKnowledgeGraphSearchApi googleKnowledgeGraphSearchApi;
    private Global global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CALL_PHONE, Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.SEND_SMS,
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

        startListeningButton = findViewById(R.id.speakButton);
        speechTextView = findViewById(R.id.textViewSpeech);

        global = (Global) getApplicationContext();
        commands = global.getCommands();

        Intent textToSpeechServiceIntent = new Intent(this, TextToSpeechService.class);
        Intent speechToTextServiceIntent = new Intent(this, SpeechToTextService.class);
        bindService(textToSpeechServiceIntent, this, BIND_AUTO_CREATE);
        bindService(speechToTextServiceIntent, this, BIND_AUTO_CREATE);

        this.utils = new PomocnaKlasa(this);

        googleKnowledgeGraphSearchApi = RetrofitClient.getInstance().getGoogleKnowledgeGraphSearchApi();
    }

    @SuppressLint("MissingPermission")
    public void onButtonClick(View view) {
        if (!clicked) {
            speechToTextService.startListening();
        } else {
            speechToTextService.stopListening();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        intent.setAction("pmf.rma.voiceassistant.NOTIFICATIONS_ON");
        sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        intent.setAction("pmf.rma.voiceassistant.NOTIFICATIONS_OFF");
        sendBroadcast(intent);
        if (speechToTextService != null)
            speechToTextService.setSpeechToTextCallback(this);
    }

    @Override
    protected void onDestroy() {
        textToSpeechService.shutdown();
        speechToTextService.destroy();
        /*Intent intent = new Intent(this, NotificationBroadcastReceiver.class);
        intent.setAction("pmf.rma.voiceassistant.NOTIFICATIONS_OFF");
        sendBroadcast(intent);*/
        super.onDestroy();
    }

    @Override
    public void onResults(Bundle results) {
        String result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
        speechTextView.setText(result);
        startListeningButton.setImageResource(R.drawable.microphone);
        processTheResults(result);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        speechTextView.setText(R.string.textview_text);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent activityIntent = new Intent(this, UserManualActivity.class);
        startActivity(activityIntent);
        return true;
    }

    private void processTheResults(String result) {
        boolean commandFound = false;
        for (CommandEntity command: commands) {
            if (result.matches(command.getRegularExpression())) {
                switch (command.getRegularExpression()) {
                    case TELL_A_JOKE_REGEX:
                        String joke = utils.tellAJoke();
                        if (joke == null)
                            textToSpeechService.speak(getString(R.string.jokeNotFound));
                        else
                            textToSpeechService.speak(joke);
                        break;
                    case TURN_ON_BLUETOOTH_REGEX:
                        boolean isBluetoothTurnedOn = utils.turnOnBluetooth();
                        if (isBluetoothTurnedOn)
                            textToSpeechService.speak("Uključujem blutut.");
                        else
                            textToSpeechService.speak("Blutut je već uključen.");
                        break;
                    case TURN_OFF_BLUETOOTH_REGEX:
                        boolean isBluetoothTurnedOff = utils.turnOffBluetooth();
                        if (isBluetoothTurnedOff)
                            textToSpeechService.speak("Isključujem blutut.");
                        else
                            textToSpeechService.speak("Blutut je već isključen.");
                        break;
                    case TURN_ON_FLASHLIGHT_REGEX:
                        boolean isFlashlightTurnedOn = utils.turnOnFlashlight();
                        if (isFlashlightTurnedOn)
                            textToSpeechService.speak("Uključujem lampu.");
                        else
                            textToSpeechService.speak("Lampa je već uključena.");
                        break;
                    case TURN_OFF_FLASHLIGHT_REGEX:
                        boolean isFlashlightTurnedOff = utils.turnOffFlashlight();
                        if (isFlashlightTurnedOff)
                            textToSpeechService.speak("Isključujem lampu.");
                        else
                            textToSpeechService.speak("Lampa je već isključena.");
                        break;
                    case TURN_ON_WIFI_REGEX:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            utils.wifiSettings();
                            textToSpeechService.speak("Otvaram podešavanja za WiFi.");
                        } else {
                            boolean isWifiTurnedOn = utils.turnOnWifi();
                            if (isWifiTurnedOn)
                                textToSpeechService.speak("Uključujem WiFi.");
                            else
                                textToSpeechService.speak("WiFi je već uključen.");
                        }
                        break;
                    case TURN_OFF_WIFI_REGEX:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            utils.wifiSettings();
                            textToSpeechService.speak("Otvaram podešavanja za WiFi.");
                        } else {
                            boolean isWifiTurnedOff = utils.turnOffWifi();
                            if (isWifiTurnedOff)
                                textToSpeechService.speak("Isključujem WiFi.");
                            else
                                textToSpeechService.speak("WiFi je već isključen.");
                        }
                        break;
                    case WHAT_TIME_IS_IT_REGEX:
                        String time = utils.whatTimeIsIt();
                        textToSpeechService.speak(time);
                        break;
                    case WHAT_IS_THE_DATE_REGEX:
                        String date = utils.whatIsTheDate();
                        textToSpeechService.speak(date);
                        break;
                    case PLAY_MUSIC_REGEX:
                        textToSpeechService.speak("Tražim neku pesmu na Vašem uređaju.");
                        utils.playMusic();
                        break;
                    case PAUSE_MUSIC_REGEX:
                        textToSpeechService.speak("Pauziram pesmu.");
                        utils.pauseMusic();
                        break;
                    case PHONE_CALL_REGEX:
                        utils.makeAPhoneCall(result);
                        break;
                    case OPEN_FACEBOOK_REGEX:
                        boolean isFacebookOpened = utils.openFacebook();
                        if (isFacebookOpened) {
                            textToSpeechService.speak("Otvaram aplikaciju Facebook.");
                        } else {
                            textToSpeechService.speak("Aplikacija Facebook nije instalirana.");
                        }
                        break;
                    case OPEN_MESSENGER_REGEX:
                        boolean isMessengerOpened = utils.openMessenger();
                        if (isMessengerOpened) {
                            textToSpeechService.speak("Otvaram aplikaciju Messenger.");
                        } else {
                            textToSpeechService.speak("Aplikacija Messenger nije instalirana.");
                        }
                        break;
                    case OPEN_INSTAGRAM_REGEX:
                        boolean isInstagramOpened = utils.openInstagram();
                        if (isInstagramOpened) {
                            textToSpeechService.speak("Otvaram aplikaciju Instagram.");
                        } else {
                            textToSpeechService.speak("Aplikacija Instagram nije instalirana.");
                        }
                        break;
                    case OPEN_YOUTUBE_REGEX:
                        boolean isYouTubeOpened = utils.openYouTube();
                        if (isYouTubeOpened) {
                            textToSpeechService.speak("Otvaram aplikaciju YouTube.");
                        } else {
                            textToSpeechService.speak("Aplikacija YouTube nije instalirana.");
                        }
                        break;
                    case OPEN_GMAIL_REGEX:
                        boolean isGmailOpened = utils.openGmail();
                        if (isGmailOpened) {
                            textToSpeechService.speak("Otvaram aplikaciju Gmail.");
                        } else {
                            textToSpeechService.speak("Aplikacija Gmail nije instalirana.");
                        }
                        break;
                    case OPEN_GOOGLE_CHROME_REGEX:
                        boolean isGoogleChromeOpened = utils.openGoogleChrome();
                        if (isGoogleChromeOpened) {
                            textToSpeechService.speak("Otvaram aplikaciju Google Chrome.");
                        } else {
                            textToSpeechService.speak("Aplikacija Google Chrome nije instalirana.");
                        }
                        break;
                    case GET_LOCATION_REGEX:
                        String location = utils.getLocation();
                        textToSpeechService.speak(location);
                        break;
                }
                commandFound = true;
                break;
            }
        }
        if (!commandFound) {
            textToSpeechService.speak("Ne znam da izvršim Vašu komandu, pa ću pokušati da pretražim na Guglu rezultate za " + result);
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
                                    textToSpeechService.speak(text);
                                } else {
                                    textToSpeechService.speak("Nisam pronašla nijedan rezultat.");
                                }
                            } catch (Exception e) {
                                textToSpeechService.speak("Nisam pronašla nijedan rezultat.");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            textToSpeechService.speak(getString(R.string.commandNotFound));
                        }
                    }
            );
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        if (service instanceof SpeechToTextService.SpeechToTextServiceBinder) {
            SpeechToTextService.SpeechToTextServiceBinder speechToTextServiceBinder = (SpeechToTextService.SpeechToTextServiceBinder) service;
            speechToTextService = speechToTextServiceBinder.getService();
            speechToTextService.initialize(this);
        } else if (service instanceof TextToSpeechService.TextToSpeechServiceBinder) {
            TextToSpeechService.TextToSpeechServiceBinder textToSpeechServiceBinder = (TextToSpeechService.TextToSpeechServiceBinder) service;
            textToSpeechService = textToSpeechServiceBinder.getService();
            textToSpeechService.initialize(INITIAL_TEXT);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}