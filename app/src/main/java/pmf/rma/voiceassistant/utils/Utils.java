package pmf.rma.voiceassistant.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import pmf.rma.voiceassistant.Global;
import pmf.rma.voiceassistant.database.entity.JokeEntity;

public class Utils {
    private final Context context;
    private final BluetoothAdapter bluetoothAdapter;
    private final WifiManager wifiManager;
    private final CameraManager cameraManager;
    private static MediaPlayer mediaPlayer;
    private final List<String> mp3Files;
    private final Random random;
    private final List<JokeEntity> jokes;
    private final Geocoder geocoder;
    private double latitude, longitude;

    @SuppressLint("MissingPermission")
    public Utils(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = location -> {
            setLatitude(location.getLatitude());
            setLongitude(location.getLongitude());
        };
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(provider, 2000, 1, locationListener);
        this.mp3Files = scanDeviceForMp3Files();
        this.random = new Random();
        Global global = (Global) context.getApplicationContext();
        this.jokes = global.getJokes();
        this.geocoder = new Geocoder(context, Locale.getDefault());
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
        wifiSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(wifiSettingsIntent);
    }

    public void makeAPhoneCall(String speech) {
        speech = speech.toLowerCase();
        speech = speech.replaceAll("zvezda", "*");
        speech = speech.replaceAll("taraba", "#");
        speech = speech.replaceAll("[a-z\\s]", "");
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(speech)));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void sendMessage(String speech) {
        speech = speech.toLowerCase().replace("pošalji poruku na broj ", "");
        speech = speech.replaceAll("\\s", "");
        Intent sendMessageIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + speech));
        sendMessageIntent.setPackage("com.google.android.apps.messaging");
        sendMessageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(sendMessageIntent);
    }

    public void showContacts() {
        Intent contactsIntent = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
        contactsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(contactsIntent);
    }

    public boolean openInstagram() {
        try {
            Intent instagramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com"));
            instagramIntent.setPackage("com.instagram.android");
            instagramIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(instagramIntent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean openFacebook() {
        try {
            context.getPackageManager().getApplicationInfo("com.facebook.katana", 0);
            Intent facebookIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://root"));
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
            messengerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(messengerIntent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean openYouTube() {
        try {
            context.getPackageManager().getApplicationInfo("com.google.android.youtube", 0);
            Intent youtubeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"));
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
            gmailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ConversationListActivityGmail");
            gmailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(gmailIntent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean openGoogleChrome() {
        try {
            context.getPackageManager().getApplicationInfo("com.android.chrome", 0);
            Intent chromeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"));
            chromeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chromeIntent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void setLatitude(double newLatitude) {
        this.latitude = newLatitude;
    }

    private void setLongitude(double newLongitude) {
        this.longitude = newLongitude;
    }

    @SuppressLint("MissingPermission")
    public String getLocation() {
        if (latitude == 0.0 && longitude == 0.0) {
            return "Ne mogu da utvrdim Vašu lokaciju. Proverite internet konekciju i pokušajte ponovo.";
        } else {
            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                String address = addresses.get(0).getAddressLine(0);
                return "Vaša trenutna lokacija je " + address + ".";
            } catch (Exception e) {
                return "Ne mogu da utvrdim Vašu lokaciju. Proverite internet konekciju i pokušajte ponovo.";
            }
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
        /*if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, Uri.parse(mp3Files.get(index)));
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }*/
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                if (mediaPlayer.getCurrentPosition() == mediaPlayer.getDuration()) {
                    mediaPlayer.stop();
                    mediaPlayer = null;
                } else {
                    mediaPlayer.start();
                }
            }
        } else {
            mediaPlayer = MediaPlayer.create(context, Uri.parse(mp3Files.get(index)));
            mediaPlayer.start();
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }
}
