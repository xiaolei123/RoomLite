package me.xiaolei.myroom.library.sqlite.calls;

public interface OnResult<T>
{
    public void callBack(T data);
}