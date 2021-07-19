package pmf.rma.voiceassistant.database.dao;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import pmf.rma.voiceassistant.database.entity.JokeEntity;

@Dao
public interface JokeDao {
    @Query(value = "SELECT * FROM joke")
    List<JokeEntity> getAll();
}
