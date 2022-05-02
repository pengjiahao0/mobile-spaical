package me.gfuil.bmap.lite.storage;

import android.content.Context;

import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;
import me.gfuil.bmap.lite.BuildConfig;

public class ObjectBox {
    private static BoxStore boxStore;

    public static void init(Context context) {
        if(boxStore==null){
            boxStore = MyObjectBox.builder()
                    .androidContext(context.getApplicationContext())
                    .build();
        }
        if(BuildConfig.DEBUG){
            boolean status = new  AndroidObjectBrowser(boxStore).start(context);

        }
    }

    public static BoxStore get() {

        return boxStore;
    }
}
