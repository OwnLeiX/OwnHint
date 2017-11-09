package lx.own.hint.immersive;

import android.support.annotation.IntDef;

/**
 * <p> </p><br/>
 *
 * @author Lx
 *         Create on 15/10/2017.
 */
@IntDef({ImmersiveConfig.Priority.PROFESSIONAL,
        ImmersiveConfig.Priority.HARD,
        ImmersiveConfig.Priority.NORMAL,
        ImmersiveConfig.Priority.EASY,
        ImmersiveConfig.Priority.CHICKEN})
public @interface ImmersivePriority {
}
