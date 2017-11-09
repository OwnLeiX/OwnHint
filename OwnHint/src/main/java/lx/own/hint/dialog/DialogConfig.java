package lx.own.hint.dialog;

import android.support.annotation.NonNull;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 *         Created on 2017/11/7.
 */

public interface DialogConfig {

    enum Type {
        Cancelable(DialogTypeConfig.getDefaultConfig()), UnCancelable(DialogTypeConfig.getDefaultConfig());
        DialogTypeConfig config;

        Type(DialogTypeConfig config) {
            this.config = config;
        }

        synchronized void custom(@NonNull DialogTypeConfig params) {
            this.config = params;
        }
    }

    interface DismissReason {
        int REASON_REPLACE = 1;
        int REASON_ACTION = 2;
        int REASON_DETACHED = 3;
        int REASON_ACTIVE = 4;
    }

    interface Priority {
        int PROFESSIONAL = 400;
        int HARD = 300;
        int NORMAL = 200;
        int EASY = 100;
        int CHICKEN = 0;
        int LOADING = -1;
    }
}
