package pmf.rma.voiceassistant.database;

import static pmf.rma.voiceassistant.utils.constants.RegularExpressions.*;

import java.util.ArrayList;
import java.util.List;

import pmf.rma.voiceassistant.database.entity.CommandEntity;
import pmf.rma.voiceassistant.database.entity.JokeEntity;

public class DataGenerator {
    private static final String[] names;
    private static final String[] regularExpressions;
    private static final String[] titles;
    private static final String[] texts;

    static {
        names = new String[] {
                "TURN_ON_BLUETOOTH", "TURN_OFF_BLUETOOTH",
                "TURN_ON_FLASHLIGHT", "TURN_OFF_FLASHLIGHT",
                "TELL_A_JOKE", "TURN_ON_WIFI", "TURN_OFF_WIFI",
                "WHAT_TIME_IS_IT", "WHAT_IS_THE_DATE",
                "PLAY_MUSIC", "PAUSE_MUSIC", "PHONE_CALL",
                "OPEN_FACEBOOK", "OPEN_MESSENGER", "OPEN_INSTAGRAM",
                "OPEN_YOUTUBE", "OPEN_GMAIL", "OPEN_GOOGLE_CHROME",
                "GET_LOCATION", "SEARCH", "SHOW_CONTACTS", "SEND_SMS"
        };
        regularExpressions = new String[] {
                TURN_ON_BLUETOOTH_REGEX, TURN_OFF_BLUETOOTH_REGEX,
                TURN_ON_FLASHLIGHT_REGEX, TURN_OFF_FLASHLIGHT_REGEX,
                TELL_A_JOKE_REGEX, TURN_ON_WIFI_REGEX, TURN_OFF_WIFI_REGEX,
                WHAT_TIME_IS_IT_REGEX, WHAT_IS_THE_DATE_REGEX,
                PLAY_MUSIC_REGEX, PAUSE_MUSIC_REGEX, PHONE_CALL_REGEX,
                OPEN_FACEBOOK_REGEX, OPEN_MESSENGER_REGEX, OPEN_INSTAGRAM_REGEX,
                OPEN_YOUTUBE_REGEX, OPEN_GMAIL_REGEX, OPEN_GOOGLE_CHROME_REGEX,
                GET_LOCATION_REGEX, SEARCH_REGEX, SHOW_CONTACTS_REGEX, SEND_SMS_REGEX
        };
        titles = new String[] {
                "Jedno pitanje"
        };
        texts = new String[] {
                "Perice, postaviću ti samo jedno pitanje, ali odgovor mora biti brz! " +
                        "Dobro, spreman sam. Koliko je 17 puta 4? Brz!"
        };
    }

    public static List<JokeEntity> generateJokes() {
        int n = titles.length;
        List<JokeEntity> jokeEntityList = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int id = i + 1;
            String title = titles[i];
            String text = texts[i];
            JokeEntity jokeEntity = new JokeEntity();
            jokeEntity.setId(id);
            jokeEntity.setTitle(title);
            jokeEntity.setText(text);
            jokeEntityList.add(jokeEntity);
        }
        return jokeEntityList;
    }

    public static List<CommandEntity> generateCommands() {
        int n = names.length;
        List<CommandEntity> commandEntityList = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int id = i + 1;
            String name = names[i];
            String regularExpression = regularExpressions[i];
            CommandEntity commandEntity = new CommandEntity();
            commandEntity.setId(id);
            commandEntity.setName(name);
            commandEntity.setRegularExpression(regularExpression);
            commandEntityList.add(commandEntity);
        }
        return commandEntityList;
    }
}
