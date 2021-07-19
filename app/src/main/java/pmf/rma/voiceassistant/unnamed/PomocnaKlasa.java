package pmf.rma.voiceassistant.unnamed;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.telephony.SmsManager;

public class PomocnaKlasa {
    private Activity activity;
    private final BluetoothAdapter bluetoothAdapter;
    private final SmsManager smsManager;

    public PomocnaKlasa(Activity activity) {
        this.activity = activity;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.smsManager = SmsManager.getDefault();
    }

    public boolean turnOnBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            return true;
        }
        return false;
    }

    public boolean turnOffBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            return true;
        }
        return false;
    }

    public boolean turnOnFlashlight() {
        return false;
    }

    public boolean turnOffFlashlight() {
        return false;
    }

    public boolean turnOnWifi() {
        return false;
    }

    public boolean turnOffWifi() {
        return false;
    }

    public void sendSms(String number, String text) {
        smsManager.sendTextMessage(number, null, text, null, null);
    }

    public void showAllMessages() {

    }

    public void openInstagram() {

    }

    public void openFacebook() {

    }

    public void openYouTube() {

    }
}
