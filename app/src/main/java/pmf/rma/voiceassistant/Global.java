package pmf.rma.voiceassistant;

import android.app.Application;

import java.util.List;

import pmf.rma.voiceassistant.database.AppDatabase;
import pmf.rma.voiceassistant.database.entity.CommandEntity;
import pmf.rma.voiceassistant.database.entity.JokeEntity;

public class Global extends Application {
    private List<CommandEntity> commands;
    private List<JokeEntity> jokes;

    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase.databaseExecutor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            commands = db.commandDao().getAll();
            jokes = db.jokeDao().getAll();
        });
    }

    public List<CommandEntity> getCommands() {
        return commands;
    }

    public List<JokeEntity> getJokes() {
        return jokes;
    }
}
