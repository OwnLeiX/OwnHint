package lx.own.example;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import lx.own.hint.immersive.HintTypeConfig;
import lx.own.hint.immersive.ImmersiveConfig;
import lx.own.hint.immersive.ImmersiveHintManager;

/**
 * <p> </p><br/>
 *
 * @author Lx
 *         Create on 14/10/2017.
 */

public class BaseApplication extends Application {

    private Activity mCurr;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                mCurr = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
        HintTypeConfig.OverallModelSupporter SUPPORTER = new HintTypeConfig.OverallModelSupporter() {
            @Override
            public Activity provideTopActivity() {
                return mCurr;
            }
        };
        HintTypeConfig customConfig = new HintTypeConfig().overallModel(SUPPORTER);
        ImmersiveHintManager.$()
                .configure(ImmersiveConfig.Type.Hint, customConfig)
                .configure(ImmersiveConfig.Type.Warning, customConfig);
    }
}
