package pmf.rma.voiceassistant.unnamed;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.MediaStore;
import android.service.autofill.FieldClassification;
import android.telephony.SmsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pmf.rma.voiceassistant.database.DataGenerator;

public class PomocnaKlasa {
    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private final SmsManager smsManager;
    private final WifiManager wifiManager;
    private final CameraManager cameraManager;
    private MediaPlayer mediaPlayer;
    private final List<String> mp3Files;
    private final Random random;
    private Pattern pattern;

    public PomocnaKlasa(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.smsManager = SmsManager.getDefault();
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        this.mp3Files = scanDeviceForMp3Files();
        this.random = new Random();
        this.pattern = null;
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
        String cameraId = null; // Usually front camera is at 0 position and back camera is 1.
        try {
            cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean turnOffFlashlight() {
        String cameraId = null; // Usually front camera is at 0 position and back camera is 1.
        try {
            cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean turnOnWifi() {
        wifiManager.setWifiEnabled(true);
        return false;
    }

    public boolean turnOffWifi() {
        wifiManager.setWifiEnabled(false);
        return false;
    }

    public void call(/*String number*/String speech) {
        /*Pattern pattern = Pattern.compile("vre");
        Matcher matcher = pattern.matcher("");*/
        pattern = Pattern.compile(DataGenerator.PHONE_CALL_REGEX);
        Matcher matcher = pattern.matcher(speech);
        String number = null;
        if (matcher.find()) {
            number = matcher.group("broj");
        }
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(number)));
        context.startActivity(intent);
    }

    public void takeAScreenshot() {

    }

    //...

    public void setAlarm() {

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

    public void getLocationOnMap() {

    }

    private List<String> scanDeviceForMp3Files(){
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };
        String sortOrder = MediaStore.Audio.AudioColumns.TITLE + " ASC";
        List<String> mp3Files = new ArrayList<>();
        Cursor cursor = null;
        try {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            cursor = context.getContentResolver().query(uri, projection, selection, null, sortOrder);
            if (cursor != null) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    /*String title = cursor.getString(0);
                    String artist = cursor.getString(1);*/
                    String path = cursor.getString(2);
                    /*String displayName  = cursor.getString(3);
                    String songDuration = cursor.getString(4);*/
                    cursor.moveToNext();
                    if (path != null && path.endsWith(".mp3")) {
                        mp3Files.add(path);
                    }
                }
            }
        } catch (Exception ignored) {

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mp3Files;
    }

    private boolean isPaused;

    public void playMusic() {
        int size = mp3Files.size();
        int index = random.nextInt(size);
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, Uri.parse(mp3Files.get(index)));
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPaused = false;
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void pauseMusic() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
        }
    }
}
