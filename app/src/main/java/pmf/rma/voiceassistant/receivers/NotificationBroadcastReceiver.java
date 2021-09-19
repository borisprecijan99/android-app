package pmf.rma.voiceassistant.receivers;

import static pmf.rma.voiceassistant.utils.constants.RegularExpressions.*;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.SpeechRecognizer;
import android.widget.RemoteViews;

import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.jayway.jsonpath.JsonPath;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.List;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import pmf.rma.voiceassistant.Global;
import pmf.rma.voiceassistant.database.entity.CommandEntity;
import pmf.rma.voiceassistant.services.http.GoogleKnowledgeGraphSearchApi;
import pmf.rma.voiceassistant.services.http.RetrofitClient;
import pmf.rma.voiceassistant.R;
import pmf.rma.voiceassistant.services.SpeechToTextService;
import pmf.rma.voiceassistant.services.SpeechToTextServiceCallback;
import pmf.rma.voiceassistant.services.TextToSpeechService;
import pmf.rma.voiceassistant.utils.CyrillicLatinConverter;
import pmf.rma.voiceassistant.utils.StringEditDistance;
import pmf.rma.voiceassistant.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationBroadcastReceiver extends BroadcastReceiver implements SpeechToTextServiceCallback {
    private static final String CHANNEL_ID = "notificationChannel";
    private NotificationChannelCompat notificationChannel;
    private NotificationManagerCompat notificationManager;
    private SpeechToTextService speechToTextService;
    private TextToSpeechService textToSpeechService;
    private Context context;
    private Utils utils;
    private static boolean clicked;
    private Notification notification;
    private List<CommandEntity> commands;
    private GoogleKnowledgeGraphSearchApi googleKnowledgeGraphSearchApi;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        this.utils = new Utils(context);
        Global global = (Global) context.getApplicationContext();
        commands = global.getCommands();

        googleKnowledgeGraphSearchApi = RetrofitClient.getInstance().getGoogleKnowledgeGraphSearchApi();

        notificationChannel = new NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setDescription("Notifikacije koje dolaze od aplikacije Glasovni pomoćnik")
                .setName("Glasovni pomoćnik")
                .build();

        notificationManager = NotificationManagerCompat.from(context);
        notificationManager.createNotificationChannel(notificationChannel);

        notification = createNotification();

        SpeechToTextService.SpeechToTextServiceBinder speechToTextServiceBinder = (SpeechToTextService.SpeechToTextServiceBinder) peekService(context, new Intent(context, SpeechToTextService.class));
        speechToTextService = speechToTextServiceBinder.getService();

        TextToSpeechService.TextToSpeechServiceBinder textToSpeechServiceBinder = (TextToSpeechService.TextToSpeechServiceBinder) peekService(context, new Intent(context, TextToSpeechService.class));
        textToSpeechService = textToSpeechServiceBinder.getService();

        if (intent.getAction().equals("pmf.rma.voiceassistant.NOTIFICATIONS_ON")) {
            speechToTextService.setSpeechToTextCallback(this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    speechToTextService.startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
                }
            }
        } else if (intent.getAction().equals("pmf.rma.voiceassistant.NOTIFICATIONS_OFF")) {
            speechToTextService.stopForeground(true);
        } else if (intent.getAction().equals("pmf.rma.voiceassistant.CLICK")) {
            speechToTextService.setSpeechToTextCallback(this);
            if (!clicked) {
                speechToTextService.startListening();
            } else {
                speechToTextService.stopListening();
            }
        }
    }

    private RemoteViews getContentView() {
        return new RemoteViews(context.getPackageName(), R.layout.notification_collapsed);
    }

    private RemoteViews getBigContentView() {
        RemoteViews expanded = new RemoteViews(context.getPackageName(), R.layout.notification_expanded);
        Intent intent = new Intent(context, NotificationBroadcastReceiver.class);
        intent.setAction("pmf.rma.voiceassistant.CLICK");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        expanded.setOnClickPendingIntent(R.id.imageButton2, pendingIntent);
        expanded.setImageViewResource(R.id.imageButton2, !clicked ? R.drawable.microphone : R.drawable.microphone_off);
        return expanded;
    }

    @Override
    public void onResults(Bundle results) {
        String result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
        clicked = false;
        notification = createNotification();
        notificationManager.notify(1, notification);
        processTheResults(result);
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
        clicked = true;
        notification = createNotification();
        notificationManager.notify(1, notification);
    }

    @Override
    public void onError(int error) {
        clicked = false;
        notification = createNotification();
        notificationManager.notify(1, notification);
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.microphone)
                .setCustomContentView(getContentView())
                .setCustomBigContentView(getBigContentView())
                .setOngoing(true)
                .build();
    }

    private void processTheResults(String result) {
        String regex = commands.stream().map(CommandEntity::getRegularExpression).filter(result::matches).collect(Collectors.joining());
        switch (regex) {
            case TELL_A_JOKE_REGEX:
                String joke = utils.tellAJoke();
                if (joke == null)
                    textToSpeechService.speak(context.getString(R.string.jokeNotFound));
                else
                    textToSpeechService.speak(joke);
                break;
            case TURN_ON_BLUETOOTH_REGEX:
                boolean isBluetoothTurnedOn = utils.turnOnBluetooth();
                if (isBluetoothTurnedOn)
                    textToSpeechService.speak(context.getString(R.string.turnOnBluetooth));
                else
                    textToSpeechService.speak(context.getString(R.string.bluetoothAlreadyTurnedOn));
                break;
            case TURN_OFF_BLUETOOTH_REGEX:
                boolean isBluetoothTurnedOff = utils.turnOffBluetooth();
                if (isBluetoothTurnedOff)
                    textToSpeechService.speak(context.getString(R.string.turnOffBluetooth));
                else
                    textToSpeechService.speak(context.getString(R.string.bluetoothAlreadyTurnedOff));
                break;
            case TURN_ON_FLASHLIGHT_REGEX:
                boolean isFlashlightTurnedOn = utils.turnOnFlashlight();
                if (isFlashlightTurnedOn)
                    textToSpeechService.speak(context.getString(R.string.turnOnFlashlight));
                else
                    textToSpeechService.speak(context.getString(R.string.flashlightAlreadyTurnedOn));
                break;
            case TURN_OFF_FLASHLIGHT_REGEX:
                boolean isFlashlightTurnedOff = utils.turnOffFlashlight();
                if (isFlashlightTurnedOff)
                    textToSpeechService.speak(context.getString(R.string.turnOffFlashlight));
                else
                    textToSpeechService.speak(context.getString(R.string.flashlightAlreadyTurnedOff));
                break;
            case TURN_ON_WIFI_REGEX:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    utils.wifiSettings();
                    textToSpeechService.speak(context.getString(R.string.openWiFiSettings));
                } else {
                    boolean isWifiTurnedOn = utils.turnOnWifi();
                    if (isWifiTurnedOn)
                        textToSpeechService.speak(context.getString(R.string.turnOnWiFi));
                    else
                        textToSpeechService.speak(context.getString(R.string.wiFiAlreadyTurnedOn));
                }
                break;
            case TURN_OFF_WIFI_REGEX:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    utils.wifiSettings();
                    textToSpeechService.speak(context.getString(R.string.openWiFiSettings));
                } else {
                    boolean isWifiTurnedOff = utils.turnOffWifi();
                    if (isWifiTurnedOff)
                        textToSpeechService.speak(context.getString(R.string.turnOffWiFi));
                    else
                        textToSpeechService.speak(context.getString(R.string.wiFiAlreadyTurnedOff));
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
                String playMusic = utils.playMusic();
                textToSpeechService.speak(playMusic);
                break;
            case PAUSE_MUSIC_REGEX:
                String pauseMusic = utils.pauseMusic();
                textToSpeechService.speak(pauseMusic);
                break;
            case PHONE_CALL_REGEX:
                utils.makeAPhoneCall(result);
                break;
            case OPEN_FACEBOOK_REGEX:
                boolean isFacebookOpened = utils.openFacebook();
                if (isFacebookOpened) {
                    textToSpeechService.speak(context.getString(R.string.openFacebook));
                } else {
                    textToSpeechService.speak(context.getString(R.string.facebookNotFound));
                }
                break;
            case OPEN_MESSENGER_REGEX:
                boolean isMessengerOpened = utils.openMessenger();
                if (isMessengerOpened) {
                    textToSpeechService.speak(context.getString(R.string.openMessenger));
                } else {
                    textToSpeechService.speak(context.getString(R.string.messengerNotFound));
                }
                break;
            case OPEN_INSTAGRAM_REGEX:
                boolean isInstagramOpened = utils.openInstagram();
                if (isInstagramOpened) {
                    textToSpeechService.speak(context.getString(R.string.openInstagram));
                } else {
                    textToSpeechService.speak(context.getString(R.string.instagramNotFound));
                }
                break;
            case OPEN_YOUTUBE_REGEX:
                boolean isYouTubeOpened = utils.openYouTube();
                if (isYouTubeOpened) {
                    textToSpeechService.speak(context.getString(R.string.openYouTube));
                } else {
                    textToSpeechService.speak(context.getString(R.string.youTubeNotFound));
                }
                break;
            case OPEN_GMAIL_REGEX:
                boolean isGmailOpened = utils.openGmail();
                if (isGmailOpened) {
                    textToSpeechService.speak(context.getString(R.string.openGmail));
                } else {
                    textToSpeechService.speak(context.getString(R.string.gmailNotFound));
                }
                break;
            case OPEN_GOOGLE_CHROME_REGEX:
                boolean isGoogleChromeOpened = utils.openGoogleChrome();
                if (isGoogleChromeOpened) {
                    textToSpeechService.speak(context.getString(R.string.openGoogleChrome));
                } else {
                    textToSpeechService.speak(context.getString(R.string.googleChromeNotFound));
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
                textToSpeechService.speak(context.getString(R.string.showContacts));
                utils.showContacts();
                break;
            case SEND_SMS_REGEX:
                textToSpeechService.speak(context.getString(R.string.openMessagesApp));
                utils.sendMessage(result);
                break;
            default:
                textToSpeechService.speak(context.getString(R.string.commandNotFound));
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
                                    textToSpeechService.speak(context.getString(R.string.resultNotFound));
                            } else {
                                textToSpeechService.speak(context.getString(R.string.resultNotFound));
                            }
                        } catch (Exception e) {
                            textToSpeechService.speak(context.getString(R.string.resultNotFound));
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        textToSpeechService.speak(context.getString(R.string.resultNotFound));
                    }
                }
        );
    }
}