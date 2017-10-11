package lx.own.hint.immersive;

import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 *         Created on 2017/10/11.
 */

public class CustomConfig {
    public static final CustomConfig defaultConfig = new CustomConfig();

    //icon
    @DrawableRes
    int iconResId = -1;
    int iconSize = 20;
    int iconRightMargin = 10;
    boolean showIcon = false;
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
    int actionLeftMargin = 100;
    //root
    @ColorInt
    int backgroundColor = 0xFF00FF00;
    int paddingEndsHorizontal = 10;
    int paddingEndsVertical = 10;
    //other
    long showDuration = 5000L;
    long animDuration = 500L;

    public CustomConfig setIconResId(@DrawableRes int iconResId) {
        this.iconResId = iconResId;
        return this;
    }

    public CustomConfig setIconSize(int iconSize) {
        this.iconSize = iconSize;
        return this;
    }

    public CustomConfig setMessageTextColor(@ColorInt int messageTextColor) {
        this.messageTextColor = messageTextColor;
        return this;
    }

    public CustomConfig setMessageTextSize(int messageTextSize) {
        this.messageTextSize = messageTextSize;
        return this;
    }

    public CustomConfig setActionTextColor(@ColorInt int actionTextColor) {
        this.actionTextColor = actionTextColor;
        return this;
    }

    public CustomConfig setActionBackgroundResId(@DrawableRes int actionBackgroundResId) {
        this.actionBackgroundResId = actionBackgroundResId;
        return this;
    }

    public CustomConfig setActionTextSize(int actionTextSize) {
        this.actionTextSize = actionTextSize;
        return this;
    }

    public CustomConfig setActionPaddingEndsHorizontal(int actionPaddingEndsHorizontal) {
        this.actionPaddingEndsHorizontal = actionPaddingEndsHorizontal;
        return this;
    }

    public CustomConfig setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public CustomConfig setPaddingEndsHorizontal(int paddingEndsHorizontal) {
        this.paddingEndsHorizontal = paddingEndsHorizontal;
        return this;
    }

    public CustomConfig setPaddingEndsVertical(int paddingEndsVertical) {
        this.paddingEndsVertical = paddingEndsVertical;
        return this;
    }

    public CustomConfig setShowDuration(long showDuration) {
        this.showDuration = showDuration;
        return this;
    }

    public CustomConfig setAnimDuration(long animDuration) {
        this.animDuration = animDuration;
        return this;
    }

    public CustomConfig setIconRightMargin(int iconRightMargin) {
        this.iconRightMargin = iconRightMargin;
        return this;
    }

    public CustomConfig setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
        return this;
    }

    public CustomConfig setActionLeftMargin(int actionLeftMargin) {
        this.actionLeftMargin = actionLeftMargin;
        return this;
    }
}
