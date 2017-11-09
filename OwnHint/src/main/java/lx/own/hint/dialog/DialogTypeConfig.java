package lx.own.hint.dialog;

import android.support.annotation.StyleRes;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 *         Created on 2017/11/7.
 */

public class DialogTypeConfig {
    private static DialogTypeConfig defaultConfig;

    public static DialogTypeConfig getDefaultConfig() {
        if (defaultConfig == null) {
            synchronized (DialogTypeConfig.class) {
                if (defaultConfig == null)
                    defaultConfig = new DialogTypeConfig();
            }
        }
        return defaultConfig;
    }


    boolean actionDismiss;
    boolean cancelable;
    boolean cancelableTouchOutside;
    int dialogStyle;


    public DialogTypeConfig setActionDismiss(boolean actionDismiss) {
        this.actionDismiss = actionDismiss;
        return this;
    }

    public DialogTypeConfig setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        return this;
    }

    public DialogTypeConfig setCancelableTouchOutside(boolean cancelableTouchOutside) {
        this.cancelableTouchOutside = cancelableTouchOutside;
        return this;
    }

    public DialogTypeConfig setDialogStyle(@StyleRes int dialogStyle) {
        this.dialogStyle = dialogStyle;
        return this;
    }
}
