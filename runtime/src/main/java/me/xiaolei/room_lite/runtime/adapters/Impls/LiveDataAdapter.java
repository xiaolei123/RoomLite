package me.xiaolei.room_lite.runtime.adapters.Impls;

import androidx.lifecycle.LiveData;

import java.lang.reflect.Type;

import me.xiaolei.room_lite.runtime.adapters.Adapter;
import me.xiaolei.room_lite.runtime.adapters.OnLiveProcessListener;
import me.xiaolei.room_lite.runtime.adapters.Processor;

/**
 * 支持LiveData的适配器
 */
public class LiveDataAdapter extends Adapter<LiveData>
{
    public LiveDataAdapter()
    {
        super(LiveData.class);
    }
    
    @Override
    public LiveData<?> process(Processor processor, Type generic)
    {
        return (LiveData<?>) new LiveData<Object>()
        {
            @Override
            protected void onActive()
            {
                processor.registerLiveProcess((OnLiveProcessListener) this::postValue);
            }

            @Override
            protected void onInactive()
            {
                processor.unRegisterLiveProcess();
            }
        };
    }
}
