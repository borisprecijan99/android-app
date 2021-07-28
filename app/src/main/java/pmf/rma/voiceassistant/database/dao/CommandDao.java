package pmf.rma.voiceassistant.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import pmf.rma.voiceassistant.database.entity.CommandEntity;

@Dao
public interface CommandDao {
    @Query(value = "SELECT * FROM command")
    List<CommandEntity> getAll();

    @Query(value = "SELECT * FROM command WHERE name=:name")
    CommandEntity getByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CommandEntity> commandEntityList);
}
