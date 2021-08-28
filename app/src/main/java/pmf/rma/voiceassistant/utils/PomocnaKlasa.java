package pmf.rma.voiceassistant.utils;

import android.app.AlarmManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.SmsManager;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pmf.rma.voiceassistant.Global;
import pmf.rma.voiceassistant.database.entity.JokeEntity;
import pmf.rma.voiceassistant.utils.constants.RegularExpressions;

public class PomocnaKlasa {
    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private final SmsManager smsManager;
    private final WifiManager wifiManager;
    private final CameraManager cameraManager;
    private static MediaPlayer mediaPlayer;
    private final List<String> mp3Files;
    private final Random random;
    private Pattern pattern;
    private static boolean isPaused;
    private Global global;
    private final List<JokeEntity> jokes;
    private final AlarmManager alarmManager;

    //smisliti naziv klase
    public PomocnaKlasa(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.smsManager = SmsManager.getDefault();
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.mp3Files = scanDeviceForMp3Files();
        this.random = new Random();
        this.pattern = null;
        this.global = (Global) context.getApplicationContext();
        this.jokes = global.getJokes();
    }

    public String tellAJoke() {
        int size = jokes.size();
        if (size == 0) {
            return null;
        } else {
            int index = random.nextInt(size);
            return jokes.get(index).getText();
        }
    }

    public String whatIsTheDate() {
        LocalDate today = LocalDate.now();
        int day = today.getDayOfMonth();
        String month = today.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
        int year = today.getYear();
        return "Danas je " + day + ". " + month + " " + year + ". godine.";
    }

    public String whatTimeIsIt() {
        LocalTime now = LocalTime.now();
        int hours = now.getHour();
        int minutes = now.getMinute();
        return "Trenutno je " + hours + " časova i " + minutes + " minuta.";
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
        String cameraId;
        try {
            cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            return true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean turnOffFlashlight() {
        String cameraId;
        try {
            cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            return true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean turnOnWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            return true;
        }
        return false;
    }

    public boolean turnOffWifi() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
            return true;
        }
        return false;
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    public void wifiSettings() {
        Intent wifiSettingsIntent = new Intent(Settings.Panel.ACTION_WIFI);
        context.startActivity(wifiSettingsIntent);
    }

    public void call(/*String number*/String speech) {
        /*Pattern pattern = Pattern.compile("vre");
        Matcher matcher = pattern.matcher("");*/
        pattern = Pattern.compile(RegularExpressions.PHONE_CALL_REGEX);
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

    public void setAlarm() {

    }

    public void sendMessage(String number, String text) {
        smsManager.sendTextMessage(number, null, text, null, null);
    }

    //radi
    public boolean openInstagram() {
        try {
            Uri uri = Uri.parse("https://www.instagram.com");
            Intent instagramIntent = new Intent(Intent.ACTION_VIEW, uri);
            instagramIntent.setPackage("com.instagram.android");
            instagramIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(instagramIntent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //radi
    public boolean openFacebook() {
        try {
            //context.getPackageManager().getApplicationInfo("com.facebook.katana", 0);
            Intent facebookIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://root"));
            facebookIntent.setPackage("com.facebook.katana");
            facebookIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(facebookIntent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean openMessenger() {
        try {
            context.getPackageManager().getApplicationInfo("com.facebook.orca", 0);
            Intent messengerIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://messaging"));
            //messengerIntent.setPackage("com.facebook.orca");
            messengerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(messengerIntent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //radi
    public boolean openYouTube() {
        try {
            Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"));
            youtubeIntent.setPackage("com.google.android.youtube");
            youtubeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(youtubeIntent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean openGmail() {
        try {
            Intent gmailIntent = new Intent(Intent.ACTION_VIEW);
            gmailIntent.setPackage("com.google.android.gm");
            gmailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(gmailIntent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //radi
    public boolean openGoogleChrome() {
        try {
            Uri uri = Uri.parse("https://www.google.com");
            Intent chromeIntent = new Intent(Intent.ACTION_VIEW, uri);
            chromeIntent.setPackage("com.android.chrome");
            chromeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chromeIntent);
            return true;
        } catch (Exception e) {
            return false;
        }
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
                    String path = cursor.getString(2);
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
            isPaused = false;
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
        }
    }
}
