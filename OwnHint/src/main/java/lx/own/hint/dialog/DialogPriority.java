package lx.own.hint.dialog;

import android.support.annotation.IntDef;

/**
 * <p> </p><br/>
 *
 * @author Lx
 *         Create on 15/10/2017.
 */
@IntDef({DialogConfig.Priority.PROFESSIONAL,
        DialogConfig.Priority.HARD,
        DialogConfig.Priority.NORMAL,
        DialogConfig.Priority.EASY,
        DialogConfig.Priority.CHICKEN,
        DialogConfig.Priority.LOADING})
public @interface DialogPriority {
}
