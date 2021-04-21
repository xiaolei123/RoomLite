# RoomLite

#### 介绍
Android平台下，一个SQLite数据库ORM的船新版本。

#### 获取

```gradle
allprojects {
	repositories {
		...
		maven { url 'https://www.jitpack.io' }
	}
}
```

```gradle
java:
dependencies {
    annotationProcessor 'com.github.xiaolei123:compiler:+'
    implementation 'com.github.xiaolei123:runtime:+'
}

kotlin:
dependencies {
    kapt 'com.github.xiaolei123:compiler:+'
    implementation 'com.github.xiaolei123:runtime:+'
}


```

[![版本信息](https://www.jitpack.io/v/com.github.xiaolei123/RoomLite.svg)](https://www.jitpack.io/#xiaolei123/RoomLite)


#### 使用说明

 - 创建数据库
```java
public static class DataBase extends RoomLiteDatabase
{
    public DataBase()
    {
        // 数据库名称
        super("school");
    }
    
    // 所有的表Entity
    @Override
    public Class<?>[] getEntities()
    {
        return new Class[]{User.class};
    }
    
    // 是否允许在主线程中执行
    @Override
    public boolean allowRunOnUIThread()
    {
        return true;
    }
    
    // 数据库升级
    @Override
    public void onUpgrade(@Nullable SQLiteDatabase db, int oldVersion, int newVersion)
    {
        
    }
    
    // 数据库版本
    @Override
    public int version()
    {
        return 1;
    }
}
```

 - 在数据库里创建表

```java
@Entity(name = "User")
public class User
{
    @Column
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name = "当前时间:" + System.currentTimeMillis();

    @Ignore
    public Bitmap bitmap;
}
```

 -字段 NOT NULL
```java
@Column(notNull = true)
```

 -字段唯一 UNIQUE

```java
@Column(unique = true)
```

 -默认值 DEFAULT
```java
@Column( defaultValue = "0")
```

 -忽略某个字段
```java
@Ignore
public Bitmap bitmap;
```

 - 创建索引 方式一

```java
@Entity(name = "User", indices = {
        @Index(columnNames = {"id", "name"}),
        @Index(name = "index2", columnNames = {"id", "name"}),
        @Index(name = "index3", columnNames = {"id", "name"}, unique = true),
})
```

 - 创建索引 方式二

```java
@Column(index = true)
```



 - 创建Dao

```java
@Dao
public interface UserDao
{
    @Insert
    public int addUser(User user);

    @Delete
    public int deleteUser(User user);

    @Update
    public void update(User user);

    @Query(entity = User.class, limit = "0,1")
    public User query();
}
```

 - 获取DataBase实例,获得Dao

```java
DataBase dataBase = RoomLite.build(DataBase.class);
UserDao dao = dataBase.getDao(UserDao.class);
```

 - 增

```java
@Insert
public int addUser(User user);
@Insert
public void addUser(User[] users);
@Insert
public void addUserList(List<User> users);
```

 - 删除
```java
@Delete
public int deleteUser(User user);
@Delete
public void deleteUser(User[] users);
@Delete
public void deleteUserList(List<User> users);
```

 - 改
```java
@Update
public int updateUser(User user);
@Update
public void updateUser(User[] users);
@Update
public void updateUserList(List<User> users);
```

 - 查
```java
// 查询所有
@Query(entity = User.class)
public List<User> queryAll();

// 查询第一个
@Query(entity = User.class, limit = "0,1")
public User query();

// 查询总数
@Query(what = "count(id)", entity = User.class)
public int queryCount();

// 查询所有的名字
@Query(what = "name", entity = User.class)
public String[] queryNames();

// 模糊查询
@Query(entity = User.class, whereClause = "name like ?")
public User[] querySearch(String name);
```

 - 查询分页
```java
@Query(entity = User.class, whereClause = "name like ?",limit="0,30")
```