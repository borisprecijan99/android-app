package pmf.rma.voiceassistant.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pmf.rma.voiceassistant.database.dao.CommandDao;
import pmf.rma.voiceassistant.database.dao.JokeDao;
import pmf.rma.voiceassistant.database.entity.CommandEntity;
import pmf.rma.voiceassistant.database.entity.JokeEntity;

@Database(entities = {JokeEntity.class, CommandEntity.class}, exportSchema = false, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    @VisibleForTesting
    public static final String DATABASE_NAME = "voice-assistant-db";

    public abstract CommandDao commandDao();
    public abstract JokeDao jokeDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = buildDatabase(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private static AppDatabase buildDatabase(Context appContext) {
        return Room.databaseBuilder(appContext, AppDatabase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        databaseExecutor.execute(() -> {
                            AppDatabase database = AppDatabase.getInstance(appContext);
                            List<CommandEntity> commands = DataGenerator.generateCommands();
                            List<JokeEntity> jokes = DataGenerator.generateJokes();
                            insertData(database, commands, jokes);
                        });
                    }
                })
                .build();
    }

    private static void insertData(AppDatabase database, List<CommandEntity> commands, List<JokeEntity> jokes) {
        database.runInTransaction(() -> {
            database.commandDao().insertAll(commands);
            database.jokeDao().insertAll(jokes);
        });
    }
}
