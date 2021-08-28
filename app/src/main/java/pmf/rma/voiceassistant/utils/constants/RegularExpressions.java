package pmf.rma.voiceassistant.utils.constants;

public class RegularExpressions {
    public static final String PHONE_CALL_REGEX = "(?i)(po|na)?zovi(\\s+broj)?\\s+(?<broj>(\\d|zvezda|taraba|\\s)+)";
    public static final String TURN_ON_BLUETOOTH_REGEX = "(?i)(uključi|upali)\\s+(blutut|bluetooth)";
    public static final String TURN_OFF_BLUETOOTH_REGEX = "(?i)(isključi|ugasi)\\s+(blutut|bluetooth)";
    public static final String TURN_ON_FLASHLIGHT_REGEX = "(?i)(uključi|upali)\\s+lampu";
    public static final String TURN_OFF_FLASHLIGHT_REGEX = "(?i)(isključi|ugasi)\\s+lampu";
    public static final String TURN_ON_WIFI_REGEX = "(?i)(uključi|upali)\\s+(wi-?fi|wireless|vaj-faj)";
    public static final String TURN_OFF_WIFI_REGEX = "(?i)(isključi|ugasi)\\s+(wi-?fi|wireless|vaj-faj)";
    public static final String TAKE_A_SCREENSHOT_REGEX = "(?i)napravi\\s+snimak\\s+ekrana";
    public static final String SEND_SMS_REGEX = "(?i)pošalji\\s+poruku\\s+na\\s+broj\\s+(?<brojTelefona>[0-9\\s]+)\\s*tekst\\s+(?<tekstPoruke>.+)";
    public static final String TELL_A_JOKE_REGEX = "(?i)ispričaj(\\s+mi)?(\\s+neki)?\\s+vic";
    public static final String WHAT_TIME_IS_IT_REGEX = "(?i)koliko\\s+(je|ima)\\s+sati";
    public static final String WHAT_IS_THE_DATE_REGEX = "(?i)koji\\s+je\\s+(danas|današnji)\\s+datum";
    public static final String PLAY_MUSIC_REGEX = "(?i)pusti\\s+(neku\\s+)?pesmu";
    public static final String STOP_MUSIC_REGEX = "(?i)zaustavi\\s+pesmu";
    public static final String PAUSE_MUSIC_REGEX = "(?i)pauziraj\\s+pesmu";
    public static final String OPEN_FACEBOOK_REGEX = "(?i)otvori\\s+(aplikaciju\\s+)?facebook";
    public static final String OPEN_INSTAGRAM_REGEX = "(?i)otvori\\s+(aplikaciju\\s+)?instagram";
    public static final String OPEN_YOUTUBE_REGEX = "(?i)otvori\\s+(aplikaciju\\s+)?youtube";
    public static final String OPEN_MESSENGER_REGEX = "(?i)otvori\\s+(aplikaciju\\s+)?messenger";
    public static final String OPEN_GMAIL_REGEX = "(?i)otvori\\s+(aplikaciju\\s+)?gmail";
    public static final String OPEN_GOOGLE_CHROME_REGEX = "(?i)otvori\\s+(aplikaciju\\s+)?google\\s+chrome";
}
