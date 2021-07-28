package pmf.rma.voiceassistant.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import pmf.rma.voiceassistant.database.entity.JokeEntity;

@Dao
public interface JokeDao {
    @Query(value = "SELECT * FROM joke")
    List<JokeEntity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<JokeEntity> jokeEntityList);
}
