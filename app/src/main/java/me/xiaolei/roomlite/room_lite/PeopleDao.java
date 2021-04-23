package me.xiaolei.roomlite.room_lite;

import androidx.lifecycle.LiveData;

import java.util.List;

import me.xiaolei.room_lite.annotations.Limit;
import me.xiaolei.room_lite.annotations.dao.Dao;
import me.xiaolei.room_lite.annotations.dao.Delete;
import me.xiaolei.room_lite.annotations.dao.Insert;
import me.xiaolei.room_lite.annotations.dao.Query;

@Dao
public interface PeopleDao
{
    @Insert
    public void addPeople();

    @Insert
    public void addPeople(People people);

    @Insert
    public void addPeople(People[] user);

    @Insert
    public int addPeople(User user, People people);

    @Delete
    public void delete();

    @Delete
    public void delete(People single);

    @Delete
    public void delete(People[] array);

    @Delete
    public void delete(List<People> list);

    @Query(what = "count(*)", entity = People.class)
    public LiveData<Integer> syncCount();

    @Query(entity = People.class, limit = @Limit(index = "0", maxLength = "1"))
    public People first();
}
