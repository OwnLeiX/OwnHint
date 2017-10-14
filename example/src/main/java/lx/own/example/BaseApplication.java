package lx.own.example;

import android.app.Application;

import lx.own.hint.immersive.ImmersiveHintManager;

/**
 * <p> </p><br/>
 *
 * @author Lx
 *         Create on 14/10/2017.
 */

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ImmersiveHintManager.$().init(this);
    }
}
