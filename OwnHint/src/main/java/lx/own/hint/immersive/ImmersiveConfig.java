package lx.own.hint.immersive;

import android.support.annotation.NonNull;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 *         Created on 2017/10/11.
 */

public interface ImmersiveConfig {
    enum Type {
        Warning(HintTypeConfig.defaultConfig), Hint(HintTypeConfig.defaultConfig),Succeed(HintTypeConfig.defaultConfig),Failed(HintTypeConfig.defaultConfig);
        HintTypeConfig config;

        Type(HintTypeConfig config) {
            this.config = config;
        }

        synchronized void custom(@NonNull HintTypeConfig params) {
            this.config = params;
        }
    }

    interface DismissReason {
        int REASON_TIMEOUT = 1;
        int REASON_REPLACE = 2;
        int REASON_ACTION = 3;
        int REASON_DETACHED = 4;
        int REASON_ACTIVE = 5;
    }

    interface Priority {
        int PROFESSIONAL = 400;
        int HARD = 300;
        int NORMAL = 200;
        int EASY = 100;
        int CHICKEN = 0;
    }
}
