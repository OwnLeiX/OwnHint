package lx.own.hint.dialog;

import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.StyleRes;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 * Created on 2017/11/7.
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

    static final int NO_CONFIG = -1;

    boolean actionDismiss;
    boolean cancelable;
    boolean cancelableTouchOutside;
    int dialogStyle;
    boolean hasButtonDivider = true;
    boolean hasContentDivider = true;
    @DrawableRes
    int dialogBackgroundResId = NO_CONFIG,
            sureButtonBackgroundResId = NO_CONFIG,
            cancelButtonBackgroundResId = NO_CONFIG;
    @ColorInt
    int dividerColor = 0xFF000000;
    @StyleRes
    int titleAppearance = NO_CONFIG,
            contentAppearance = NO_CONFIG,
            sureButtonAppearance = NO_CONFIG,
            cancelButtonAppearance = NO_CONFIG;


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

    public DialogTypeConfig setHasButtonDivider(boolean has) {
        this.hasButtonDivider = has;
        return this;
    }

    public DialogTypeConfig setHasContentDivider(boolean has) {
        this.hasContentDivider = has;
        return this;
    }

    public DialogTypeConfig setDividerColor(@ColorInt int color) {
        this.dividerColor = color;
        return this;
    }

    public DialogTypeConfig setDialogBackgroundResId(@DrawableRes int id) {
        this.dialogBackgroundResId = id;
        return this;
    }

    public DialogTypeConfig setSureButtonBackgroundResId(@DrawableRes int id) {
        this.sureButtonBackgroundResId = id;
        return this;
    }

    public DialogTypeConfig setCancelButtonBackgroundResId(@DrawableRes int id) {
        this.cancelButtonBackgroundResId = id;
        return this;
    }

    public DialogTypeConfig setTitleAppearance(@StyleRes int id) {
        this.titleAppearance = id;
        return this;
    }

    public DialogTypeConfig setContentAppearance(@StyleRes int id) {
        this.contentAppearance = id;
        return this;
    }

    public DialogTypeConfig setSureButtonAppearance(@StyleRes int id) {
        this.sureButtonAppearance = id;
        return this;
    }

    public DialogTypeConfig setCancelButtonAppearance(@StyleRes int id) {
        this.cancelButtonAppearance = id;
        return this;
    }
}
