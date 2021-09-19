package pmf.rma.voiceassistant.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import pmf.rma.voiceassistant.R;
import pmf.rma.voiceassistant.receivers.NotificationBroadcastReceiver;

public class UserManualActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manual);

        TextView userManual = findViewById(R.id.userManualTextView);
        userManual.setText(Html.fromHtml(getString(R.string.userManualText), Html.FROM_HTML_MODE_COMPACT));
        userManual.setMovementMethod(new ScrollingMovementMethod());
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
    }
}