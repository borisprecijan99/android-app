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
import java.util.stream.Collectors;

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
import pmf.rma.voiceassistant.utils.StringEditDistance;
import pmf.rma.voiceassistant.services.TextToSpeechService;
import pmf.rma.voiceassistant.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SpeechToTextServiceCallback, ServiceConnection {
    private TextToSpeechService textToSpeechService;
    private SpeechToTextService speechToTextService;
    private ImageButton startListeningButton;
    private TextView speechTextView;
    private boolean clicked;
    private List<CommandEntity> commands;
    private Utils utils;
    private GoogleKnowledgeGraphSearchApi googleKnowledgeGraphSearchApi;

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

        Global global = (Global) getApplicationContext();
        commands = global.getCommands();

        Intent textToSpeechServiceIntent = new Intent(this, TextToSpeechService.class);
        Intent speechToTextServiceIntent = new Intent(this, SpeechToTextService.class);
        bindService(textToSpeechServiceIntent, this, BIND_AUTO_CREATE);
        bindService(speechToTextServiceIntent, this, BIND_AUTO_CREATE);

        this.utils = new Utils(this);

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
        String regex = commands.stream().map(CommandEntity::getRegularExpression).filter(result::matches).collect(Collectors.joining());
        switch (regex) {
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
                    textToSpeechService.speak(getString(R.string.turnOnBluetooth));
                else
                    textToSpeechService.speak(getString(R.string.bluetoothAlreadyTurnedOn));
                break;
            case TURN_OFF_BLUETOOTH_REGEX:
                boolean isBluetoothTurnedOff = utils.turnOffBluetooth();
                if (isBluetoothTurnedOff)
                    textToSpeechService.speak(getString(R.string.turnOffBluetooth));
                else
                    textToSpeechService.speak(getString(R.string.bluetoothAlreadyTurnedOff));
                break;
            case TURN_ON_FLASHLIGHT_REGEX:
                boolean isFlashlightTurnedOn = utils.turnOnFlashlight();
                if (isFlashlightTurnedOn)
                    textToSpeechService.speak(getString(R.string.turnOnFlashlight));
                else
                    textToSpeechService.speak(getString(R.string.flashlightAlreadyTurnedOn));
                break;
            case TURN_OFF_FLASHLIGHT_REGEX:
                boolean isFlashlightTurnedOff = utils.turnOffFlashlight();
                if (isFlashlightTurnedOff)
                    textToSpeechService.speak(getString(R.string.turnOffFlashlight));
                else
                    textToSpeechService.speak(getString(R.string.flashlightAlreadyTurnedOff));
                break;
            case TURN_ON_WIFI_REGEX:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    utils.wifiSettings();
                    textToSpeechService.speak(getString(R.string.openWiFiSettings));
                } else {
                    boolean isWifiTurnedOn = utils.turnOnWifi();
                    if (isWifiTurnedOn)
                        textToSpeechService.speak(getString(R.string.turnOnWiFi));
                    else
                        textToSpeechService.speak(getString(R.string.wiFiAlreadyTurnedOn));
                }
                break;
            case TURN_OFF_WIFI_REGEX:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    utils.wifiSettings();
                    textToSpeechService.speak(getString(R.string.openWiFiSettings));
                } else {
                    boolean isWifiTurnedOff = utils.turnOffWifi();
                    if (isWifiTurnedOff)
                        textToSpeechService.speak(getString(R.string.turnOffWiFi));
                    else
                        textToSpeechService.speak(getString(R.string.wiFiAlreadyTurnedOff));
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
                textToSpeechService.speak(getString(R.string.playMusic));
                utils.playMusic();
                break;
            case PAUSE_MUSIC_REGEX:
                textToSpeechService.speak(getString(R.string.pauseMusic));
                utils.pauseMusic();
                break;
            case PHONE_CALL_REGEX:
                utils.makeAPhoneCall(result);
                break;
            case OPEN_FACEBOOK_REGEX:
                boolean isFacebookOpened = utils.openFacebook();
                if (isFacebookOpened) {
                    textToSpeechService.speak(getString(R.string.openFacebook));
                } else {
                    textToSpeechService.speak(getString(R.string.facebookNotFound));
                }
                break;
            case OPEN_MESSENGER_REGEX:
                boolean isMessengerOpened = utils.openMessenger();
                if (isMessengerOpened) {
                    textToSpeechService.speak(getString(R.string.openMessenger));
                } else {
                    textToSpeechService.speak(getString(R.string.messengerNotFound));
                }
                break;
            case OPEN_INSTAGRAM_REGEX:
                boolean isInstagramOpened = utils.openInstagram();
                if (isInstagramOpened) {
                    textToSpeechService.speak(getString(R.string.openInstagram));
                } else {
                    textToSpeechService.speak(getString(R.string.instagramNotFound));
                }
                break;
            case OPEN_YOUTUBE_REGEX:
                boolean isYouTubeOpened = utils.openYouTube();
                if (isYouTubeOpened) {
                    textToSpeechService.speak(getString(R.string.openYouTube));
                } else {
                    textToSpeechService.speak(getString(R.string.youTubeNotFound));
                }
                break;
            case OPEN_GMAIL_REGEX:
                boolean isGmailOpened = utils.openGmail();
                if (isGmailOpened) {
                    textToSpeechService.speak(getString(R.string.openGmail));
                } else {
                    textToSpeechService.speak(getString(R.string.gmailNotFound));
                }
                break;
            case OPEN_GOOGLE_CHROME_REGEX:
                boolean isGoogleChromeOpened = utils.openGoogleChrome();
                if (isGoogleChromeOpened) {
                    textToSpeechService.speak(getString(R.string.openGoogleChrome));
                } else {
                    textToSpeechService.speak(getString(R.string.googleChromeNotFound));
                }
                break;
            case GET_LOCATION_REGEX:
                String location = utils.getLocation();
                textToSpeechService.speak(location);
                break;
            case SEARCH_REGEX:
                search(result);
                break;
            case SHOW_CONTACTS_REGEX:
                textToSpeechService.speak(getString(R.string.showContacts));
                utils.showContacts();
                break;
            case SEND_SMS_REGEX:
                textToSpeechService.speak(getString(R.string.openMessagesApp));
                utils.sendMessage(result);
                break;
            default:
                textToSpeechService.speak(getString(R.string.commandNotFound));
                break;
        }
    }

    private void search(String result) {
        String finalResult = result.toLowerCase().replace("pretraži ", "");
        googleKnowledgeGraphSearchApi.getResult(finalResult).enqueue(
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
                                    int currentDistance = StringEditDistance.getEditDistance(name, finalResult);
                                    if (currentDistance < minDistance){
                                        minDistance = currentDistance;
                                        text = JsonPath.read(element, "$.result.detailedDescription.articleBody");
                                    }
                                }
                                if (text != null)
                                    textToSpeechService.speak(text);
                                else
                                    textToSpeechService.speak(getString(R.string.resultNotFound));
                            } else {
                                textToSpeechService.speak(getString(R.string.resultNotFound));
                            }
                        } catch (Exception e) {
                            textToSpeechService.speak(getString(R.string.resultNotFound));
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        textToSpeechService.speak(getString(R.string.resultNotFound));
                    }
                }
        );
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
            textToSpeechService.initialize(null);
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}