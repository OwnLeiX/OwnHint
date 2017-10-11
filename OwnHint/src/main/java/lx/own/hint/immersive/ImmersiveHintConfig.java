package lx.own.hint.immersive;

import android.support.annotation.NonNull;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 *         Created on 2017/10/11.
 */

public interface ImmersiveHintConfig {
    enum Type {
        Warning(CustomConfig.defaultConfig), Hint(CustomConfig.defaultConfig);
        CustomConfig config;

        Type(CustomConfig config) {
            this.config = config;
        }

        public synchronized void custom(@NonNull CustomConfig params) {
            this.config = params;
        }
    }

    interface DismissReason {
        int REASON_TIMEOUT = 1;
        int REASON_REPLACE = 2;
        int REASON_ACTION = 3;
        int REASON_CODES = 4;
    }
}
