package me.xiaolei.room_lite.runtime.sqlite.calls;

public interface OnResult<T>
{
    public void callBack(T data);
}