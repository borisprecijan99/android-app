package pmf.rma.voiceassistant.unnamed;

public class PomocnaKlasa2 {
    public static final String PHONE_CALL_REGEX = "(?i)(po|na)?zovi(\\s+broj)?\\s+(\\d|zvezda|taraba|\\s)+";
    public static final String TURN_ON_BLUETOOTH_REGEX = "(?i)(uključi|upali)\\s+(blutut|bluetooth)";
    public static final String TURN_OFF_BLUETOOTH_REGEX = "(?i)(isključi|ugasi)\\s+(blutut|bluetooth)";
    public static final String TURN_ON_FLASHLIGHT_REGEX = "(?i)(uključi|upali)\\s+lampu";
    public static final String TURN_OFF_FLASHLIGHT_REGEX = "(?i)(isključi|ugasi)\\s+lampu";
    public static final String TURN_ON_WIFI_REGEX = "(?i)(uključi|upali)\\s+(wi-?fi|wireless)";
    public static final String TURN_OFF_WIFI_REGEX = "(?i)(isključi|ugasi)\\s+(wi-?fi|wireless)";
    public static final String TAKE_A_SCREENSHOT_REGEX = "(?i)napravi\\s+snimak\\s+ekrana";
    public static final String SEND_SMS_REGEX = "(?i)pošalji\\s+poruku\\s+na\\s+broj\\s+(?<brojTelefona>[0-9\\s]+)\\s*tekst\\s+(?<tekstPoruke>.+)";
    public static final String TELL_A_JOKE_REGEX = "(?i)ispričaj(\\s+mi)?(\\s+neki)?\\s+vic";
}
