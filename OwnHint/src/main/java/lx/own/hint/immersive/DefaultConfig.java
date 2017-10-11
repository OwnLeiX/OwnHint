package lx.own.hint.immersive;

import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 *         Created on 2017/10/11.
 */

public class DefaultConfig {
    //icon
    @DrawableRes
    int iconResId = -1;
    int iconSize = 20;
    //message
    @ColorInt
    int messageTextColor = 0xFFFFFFFF;
    int messageTextSize = 20;
    //action
    @ColorInt
    int actionTextColor = 0xFFFFFFFF;
    @DrawableRes
    int actionBackgroundResId = -1;
    int actionTextSize = 20;
    int actionPaddingEndsHorizontal = 10;
    //root
    @ColorInt
    int hintBackgroundColor = 0xFF00FF00;
    @ColorInt
    int warningBackgroundColor = 0xFFFF0000;
    int paddingEndsHorizontal = 10;
    int paddingEndsVertical = 10;
    //other
    long showDuration = 5000L;
    long animDuration = 500L;

    public DefaultConfig setIconResId(@DrawableRes int iconResId) {
        this.iconResId = iconResId;
        return this;
    }

    public DefaultConfig setIconSize(int iconSize) {
        this.iconSize = iconSize;
        return this;
    }

    public DefaultConfig setMessageTextColor(@ColorInt int messageTextColor) {
        this.messageTextColor = messageTextColor;
        return this;
    }

    public DefaultConfig setMessageTextSize(int messageTextSize) {
        this.messageTextSize = messageTextSize;
        return this;
    }

    public DefaultConfig setActionTextColor(@ColorInt int actionTextColor) {
        this.actionTextColor = actionTextColor;
        return this;
    }

    public DefaultConfig setActionBackgroundResId(@DrawableRes int actionBackgroundResId) {
        this.actionBackgroundResId = actionBackgroundResId;
        return this;
    }

    public DefaultConfig setActionTextSize(int actionTextSize) {
        this.actionTextSize = actionTextSize;
        return this;
    }

    public DefaultConfig setActionPaddingEndsHorizontal(int actionPaddingEndsHorizontal) {
        this.actionPaddingEndsHorizontal = actionPaddingEndsHorizontal;
        return this;
    }

    public DefaultConfig setHintBackgroundColor(@ColorInt int hintBackgroundColor) {
        this.hintBackgroundColor = hintBackgroundColor;
        return this;
    }

    public DefaultConfig setWarningBackgroundColor(@ColorInt int warningBackgroundColor) {
        this.warningBackgroundColor = warningBackgroundColor;
        return this;
    }

    public DefaultConfig setPaddingEndsHorizontal(int paddingEndsHorizontal) {
        this.paddingEndsHorizontal = paddingEndsHorizontal;
        return this;
    }

    public DefaultConfig setPaddingEndsVertical(int paddingEndsVertical) {
        this.paddingEndsVertical = paddingEndsVertical;
        return this;
    }

    public DefaultConfig setShowDuration(long showDuration) {
        this.showDuration = showDuration;
        return this;
    }

    public DefaultConfig setAnimDuration(long animDuration) {
        this.animDuration = animDuration;
        return this;
    }
}
