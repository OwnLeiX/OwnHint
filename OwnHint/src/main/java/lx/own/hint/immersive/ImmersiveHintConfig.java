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

public interface ImmersiveHintConfig {
    enum Type {
        Warning, Hint
    }

    interface DismissReason{
        int REASON_TIMEOUT = 1;
        int REASON_REPLACE = 2;
        int REASON_ACTION = 3;
        int REASON_CODES = 4;
    }

    class DefaultParams {
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
        static long showDuration = 5000L;
        static long animDuration = 500L;

        static void update(@NonNull DefaultConfig defaultConfig) {
            DefaultParams.iconResId = defaultConfig.iconResId;
            DefaultParams.iconSize = defaultConfig.iconSize;
            DefaultParams.messageTextColor = defaultConfig.messageTextColor;
            DefaultParams.messageTextSize = defaultConfig.messageTextSize;
            DefaultParams.actionTextColor = defaultConfig.actionTextColor;
            DefaultParams.actionBackgroundResId = defaultConfig.actionBackgroundResId;
            DefaultParams.actionTextSize = defaultConfig.actionTextSize;
            DefaultParams.actionPaddingEndsHorizontal = defaultConfig.actionPaddingEndsHorizontal;
            DefaultParams.hintBackgroundColor = defaultConfig.hintBackgroundColor;
            DefaultParams.warningBackgroundColor = defaultConfig.warningBackgroundColor;
            DefaultParams.paddingEndsHorizontal = defaultConfig.paddingEndsHorizontal;
            DefaultParams.paddingEndsVertical = defaultConfig.paddingEndsVertical;
            DefaultParams.showDuration = defaultConfig.showDuration;
            DefaultParams.animDuration = defaultConfig.animDuration;
        }
    }
}
