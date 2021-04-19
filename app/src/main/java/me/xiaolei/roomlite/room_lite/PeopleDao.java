package me.xiaolei.roomlite.room_lite;

import java.util.List;

import me.xiaolei.room_lite.annotations.dao.Dao;
import me.xiaolei.room_lite.annotations.dao.Delete;
import me.xiaolei.room_lite.annotations.dao.Insert;

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
}
