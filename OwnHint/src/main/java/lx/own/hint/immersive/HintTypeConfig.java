package lx.own.hint.immersive;

import android.app.Activity;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.view.Gravity;
import android.view.animation.Interpolator;

import lx.own.hint.GravityConfig;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 * Created on 2017/10/11.
 */

public class HintTypeConfig {
    static final HintTypeConfig defaultConfig = new HintTypeConfig();

    //icon
    int iconResId = -1;
    int iconSize = 20;
    int iconRightMargin = 10;
    boolean showIcon = false;
    //message
    int messageTextColor = 0xFFFFFFFF;
    int messageTextSizeSp = 20;
    @SuppressWarnings("all")
    int messageGravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
    //action
    int actionTextColor = 0xFFFFFFFF;
    int actionBackgroundResId = -1;
    int actionTextSizeSp = 20;
    int actionPaddingEndsHorizontal = 10;
    int actionPaddingEndsVertical = 10;
    int actionLeftMargin = 100;
    boolean actionDismiss = false;
    //root
    int backgroundColor = 0xFF00FF00;
    int paddingEndsHorizontal = 10;
    int paddingEndsVertical = 10;
    boolean transmissionTouchEvent = false;
    //other
    long showDuration = 2000L;
    long animDuration = 500L;
    Interpolator showInterpolator, dismissInterpolator;

    OverallModelSupporter overallModelSupporter;

    final public HintTypeConfig setIconResId(@DrawableRes int iconResId) {
        this.iconResId = iconResId;
        return this;
    }

    final public HintTypeConfig setIconSize(int iconSize) {
        this.iconSize = iconSize;
        return this;
    }

    final public HintTypeConfig setMessageTextColor(@ColorInt int messageTextColor) {
        this.messageTextColor = messageTextColor;
        return this;
    }

    final public HintTypeConfig setMessageTextSizeSp(int messageTextSizeSp) {
        this.messageTextSizeSp = messageTextSizeSp;
        return this;
    }

    final public HintTypeConfig setActionTextColor(@ColorInt int actionTextColor) {
        this.actionTextColor = actionTextColor;
        return this;
    }

    final public HintTypeConfig setActionBackgroundResId(@DrawableRes int actionBackgroundResId) {
        this.actionBackgroundResId = actionBackgroundResId;
        return this;
    }

    final public HintTypeConfig setActionTextSizeSp(int actionTextSizeSp) {
        this.actionTextSizeSp = actionTextSizeSp;
        return this;
    }

    final public HintTypeConfig setActionPaddingEndsHorizontal(int actionPaddingEndsHorizontal) {
        this.actionPaddingEndsHorizontal = actionPaddingEndsHorizontal;
        return this;
    }

    final public HintTypeConfig setBackgroundColor(@ColorInt int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    final public HintTypeConfig setPaddingEndsHorizontal(int paddingEndsHorizontal) {
        this.paddingEndsHorizontal = paddingEndsHorizontal;
        return this;
    }

    final public HintTypeConfig setPaddingEndsVertical(int paddingEndsVertical) {
        this.paddingEndsVertical = paddingEndsVertical;
        return this;
    }

    final public HintTypeConfig setShowDuration(long showDuration) {
        this.showDuration = showDuration;
        return this;
    }

    final public HintTypeConfig setAnimDuration(long animDuration) {
        this.animDuration = animDuration;
        return this;
    }

    final public HintTypeConfig setIconRightMargin(int iconRightMargin) {
        this.iconRightMargin = iconRightMargin;
        return this;
    }

    final public HintTypeConfig setShowIcon(boolean showIcon) {
        this.showIcon = showIcon;
        return this;
    }

    final public HintTypeConfig setActionLeftMargin(int actionLeftMargin) {
        this.actionLeftMargin = actionLeftMargin;
        return this;
    }

    final public HintTypeConfig setMessageGravity(@GravityConfig int messageGravity) {
        this.messageGravity = messageGravity;
        return this;
    }

    final public HintTypeConfig setActionPaddingEndsVertical(int actionPaddingEndsVertical) {
        this.actionPaddingEndsVertical = actionPaddingEndsVertical;
        return this;
    }

    final public HintTypeConfig setActionDismiss(boolean actionDismiss) {
        this.actionDismiss = actionDismiss;
        return this;
    }

    final public HintTypeConfig overallModel(OverallModelSupporter supporter) {
        this.overallModelSupporter = supporter;
        return this;
    }

    final public HintTypeConfig transmissionTouchEvent(boolean transmissionTouchEvent) {
        this.transmissionTouchEvent = transmissionTouchEvent;
        return this;
    }

    final public HintTypeConfig setShowInterpolator(Interpolator interpolator) {
        this.showInterpolator = interpolator;
        return this;
    }

    final public HintTypeConfig setDismissInterpolator(Interpolator interpolator) {
        this.dismissInterpolator = interpolator;
        return this;
    }

    public interface OverallModelSupporter {
        Activity provideTopActivity();
    }
}
