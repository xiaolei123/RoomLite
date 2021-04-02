package me.xiaolei.roomlite.room;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface PeopleDao
{
    @Query("select id from people limit 0,1")
    public int firstId();
}
