package lx.own.hint.immersive;

import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 *         Created on 2017/10/11.
 */

interface ImmersiveHintConfig {
    enum Type {
        Warning, Hint
    }

    class Params {
        //icon
        @DrawableRes
        static int iconResId = -1;
        static int iconSize = 20;
        //message
        @ColorInt
        static int messageTextColor = 0xFFFFFFFF;
        static int messageTextSize = 20;
        //action
        @ColorInt
        static int actionTextColor = 0xFFFFFFFF;
        @DrawableRes
        static int actionBackgroundResId = -1;
        static int actionTextSize = 20;
        static int actionPaddingEndsHorizontal = 10;
        //root
        @ColorInt
        static int hintBackgroundColor = 0xFF00FF00;
        @ColorInt
        static int warningBackgroundColor = 0xFFFF0000;
        static int paddingEndsHorizontal = 10;
        static int paddingEndsVertical = 10;
        //other
        static long duration = 5000L;
        static long animDuration = 500L;

        static void update(@NonNull CustomConfig customConfig) {
            Params.iconResId = customConfig.iconResId;
            Params.iconSize = customConfig.iconSize;
            Params.messageTextColor = customConfig.messageTextColor;
            Params.messageTextSize = customConfig.messageTextSize;
            Params.actionTextColor = customConfig.actionTextColor;
            Params.actionBackgroundResId = customConfig.actionBackgroundResId;
            Params.actionTextSize = customConfig.actionTextSize;
            Params.actionPaddingEndsHorizontal = customConfig.actionPaddingEndsHorizontal;
            Params.hintBackgroundColor = customConfig.hintBackgroundColor;
            Params.warningBackgroundColor = customConfig.warningBackgroundColor;
            Params.paddingEndsHorizontal = customConfig.paddingEndsHorizontal;
            Params.paddingEndsVertical = customConfig.paddingEndsVertical;
            Params.duration = customConfig.duration;
            Params.animDuration = customConfig.animDuration;
        }
    }
}
