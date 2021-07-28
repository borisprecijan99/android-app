package pmf.rma.voiceassistant.unnamed;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import org.w3c.dom.Text;

import java.util.concurrent.Executors;

import pmf.rma.voiceassistant.ui.R;
import pmf.rma.voiceassistant.utils.SpeechToText;
import pmf.rma.voiceassistant.utils.SpeechToTextCallback;
import pmf.rma.voiceassistant.utils.TextToSpeech;

public class MyReceiver extends BroadcastReceiver implements SpeechToTextCallback {
    private static final String CHANNEL_ID = "notificationChannel";
    private static final int NOTIFICATION_ID = 1;
    private NotificationChannelCompat notificationChannel;
    private NotificationManagerCompat notificationManager;
    private SpeechToText speechToText;
    private TextToSpeech textToSpeech;
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
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
        if (intent.getAction().equals("pmf.rma.voiceassistant.NOTIFICATIONS_ON")) {
            RemoteViews collapsed = new RemoteViews(context.getPackageName(), R.layout.notification_collapsed);
            RemoteViews expanded = new RemoteViews(context.getPackageName(), R.layout.notification_expanded);
            Intent switchIntent = new Intent(context, MyReceiver.class);
            switchIntent.setAction("pmf.rma.voiceassistant.CLICK");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, switchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            expanded.setOnClickPendingIntent(R.id.imageButton2, pendingIntent);

            Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.microphone)
                    .setCustomContentView(collapsed)
                    .setCustomBigContentView(expanded)
                    .setOngoing(true)
                    .build();
            notificationManager.notify(NOTIFICATION_ID, notification);
        } else if (intent.getAction().equals("pmf.rma.voiceassistant.NOTIFICATIONS_OFF")) {
            notificationManager.cancel(NOTIFICATION_ID);
        } else if (intent.getAction().equals("pmf.rma.voiceassistant.CLICK")) {
            if (speechToText == null) {
                SpeechToText.SpeechToTextBinder speechToTextBinder = (SpeechToText.SpeechToTextBinder) peekService(context, new Intent(context, SpeechToText.class));
                speechToText = speechToTextBinder.getService();
                speechToText.initialize(this);
            }
            /*if (textToSpeech == null) {
                TextToSpeech.TextToSpeechBinder textToSpeechBinder = (TextToSpeech.TextToSpeechBinder) peekService(context, new Intent(context, TextToSpeech.class));
                TextToSpeech textToSpeech = textToSpeechBinder.getService();
                textToSpeech.initialize("test 1 2 3");
            }*/
            speechToText.startListening();
        }
    }

    @Override
    public void onResults(Bundle results) {
        PomocnaKlasa pk = new PomocnaKlasa(context);
        pk.playMusic();
    }
}