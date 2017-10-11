package lx.own.hint.immersive;

import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;

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
    }
}
