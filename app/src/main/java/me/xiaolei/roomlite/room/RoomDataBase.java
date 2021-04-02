package me.xiaolei.roomlite.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {People.class}, version = 1)
public abstract class RoomDataBase extends RoomDatabase
{
    public abstract PeopleDao peopleDao();
}
