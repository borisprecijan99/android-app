package pmf.rma.voiceassistant.receivers;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
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

import pmf.rma.voiceassistant.ui.R;
import pmf.rma.voiceassistant.services.SpeechToTextService;
import pmf.rma.voiceassistant.services.SpeechToTextServiceCallback;
import pmf.rma.voiceassistant.services.TextToSpeechService;

public class NotificationBroadcastReceiver extends BroadcastReceiver implements SpeechToTextServiceCallback {
    private static final String CHANNEL_ID = "notificationChannel";
    private static final int NOTIFICATION_ID = 1;
    private NotificationChannelCompat notificationChannel;
    private NotificationManagerCompat notificationManager;
    private SpeechToTextService speechToTextService;
    private TextToSpeechService textToSpeechService;
    private Context context;
    private static boolean clicked;

    private Notification notification;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (this.context == null)
            this.context = context;
        if (notificationChannel == null) {
            notificationChannel = new NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                    .setDescription("Notifikacije koje dolaze od aplikacije Glasovni pomoćnik")
                    .setName("Glasovni pomoćnik")
                    .build();
        }
        if (notificationManager == null) {
            notificationManager = NotificationManagerCompat.from(context);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        if (notification == null) {
            notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.microphone)
                    .setCustomContentView(getContentView())
                    .setCustomBigContentView(getBigContentView())
                    .setOngoing(true)
                    .build();
        }
        if (intent.getAction().equals("pmf.rma.voiceassistant.NOTIFICATIONS_ON")) {
            /*Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.microphone)
                    .setCustomContentView(getContentView())
                    .setCustomBigContentView(getBigContentView())
                    .setOngoing(true)
                    .build();*/
            //notificationManager.notify(NOTIFICATION_ID, notification);
            if (speechToTextService == null) {
                SpeechToTextService.SpeechToTextServiceBinder speechToTextServiceBinder = (SpeechToTextService.SpeechToTextServiceBinder) peekService(context, new Intent(context, SpeechToTextService.class));
                speechToTextService = speechToTextServiceBinder.getService();
                speechToTextService.setSpeechToTextCallback(this);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    speechToTextService.startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
                }
            }
        } else if (intent.getAction().equals("pmf.rma.voiceassistant.NOTIFICATIONS_OFF")) {
            //notificationManager.cancel(NOTIFICATION_ID);
            if (speechToTextService != null) {
                speechToTextService.stopForeground(false);
            }
        } else if (intent.getAction().equals("pmf.rma.voiceassistant.CLICK")) {
            if (speechToTextService == null) {
                SpeechToTextService.SpeechToTextServiceBinder speechToTextServiceBinder = (SpeechToTextService.SpeechToTextServiceBinder) peekService(context, new Intent(context, SpeechToTextService.class));
                speechToTextService = speechToTextServiceBinder.getService();
                speechToTextService.setSpeechToTextCallback(this);
            }
            /*if (textToSpeech == null) {
                TextToSpeech.TextToSpeechBinder textToSpeechBinder = (TextToSpeech.TextToSpeechBinder) peekService(context, new Intent(context, TextToSpeech.class));
                TextToSpeech textToSpeech = textToSpeechBinder.getService();
                textToSpeech.initialize("test 1 2 3");
            }*/
            if (!clicked) {
                speechToTextService.startListening();
            } else {
                speechToTextService.cancel();
            }
        }
    }

    private RemoteViews getContentView() {
        RemoteViews collapsed = new RemoteViews(context.getPackageName(), R.layout.notification_collapsed);

        return collapsed;
    }

    private RemoteViews getBigContentView() {
        RemoteViews expanded = new RemoteViews(context.getPackageName(), R.layout.notification_expanded);
        Intent switchIntent = new Intent(context, NotificationBroadcastReceiver.class);
        switchIntent.setAction("pmf.rma.voiceassistant.CLICK");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, switchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        expanded.setOnClickPendingIntent(R.id.imageButton2, pendingIntent);
        expanded.setTextViewText(R.id.textView2, !clicked ? "staro" : "novo");
        return expanded;
    }

    @Override
    public void onResults(Bundle results) {
        String result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);

        clicked = false;
        System.out.println(result);
        //notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        String result = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);

    }

    @Override
    public void onReadyForSpeech(Bundle params) {

        clicked = true;
        //notificationManager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public void onError(int error) {

        clicked = false;
        //notificationManager.notify(NOTIFICATION_ID, notification);
    }
}