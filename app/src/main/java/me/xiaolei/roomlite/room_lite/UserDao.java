package me.xiaolei.roomlite.room_lite;


import java.util.List;

import me.xiaolei.myroom.library.anno.dao.Dao;
import me.xiaolei.myroom.library.anno.dao.Insert;
import me.xiaolei.myroom.library.anno.dao.Query;

@Dao
public interface UserDao
{

    @Insert
    int addUser(User user);

    @Insert
    int addUser(User[] user);

    @Insert
    void addUser(List<User> list, User[] array, User single);

    // 查询所有
    @Query(entity = User.class)
    public List<User> queryAll();

    // 查询第一个
    @Query(entity = User.class, limit = "0,1")
    public User query();

    // 查询总数
    @Query(what = "count(id)", entity = User.class)
    public int queryCount();

    @Query(what = "id", entity = User.class, limit = "0,1")
    public int firstId();

    // 查询所有的名字
    @Query(what = "name", entity = User.class)
    public String[] queryNames();

    // 模糊查询
    @Query(entity = User.class, whereClause = "name like ?")
    public User[] querySearch(String name);
}
