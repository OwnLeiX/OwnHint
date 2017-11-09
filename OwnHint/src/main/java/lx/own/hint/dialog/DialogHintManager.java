package lx.own.hint.dialog;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 *         Created on 2017/11/7.
 */

public class DialogHintManager {
    private static DialogHintManager mInstance;

    public static DialogHintManager $() {
        if (mInstance == null) {
            synchronized (DialogHintManager.class) {
                if (mInstance == null)
                    mInstance = new DialogHintManager();
            }
        }
        return mInstance;
    }

    private volatile DialogHintManager.OperateRecorder mCurrentRecorder;

    private DialogHintManager() {
    }

    public DialogHintManager configure(@NonNull DialogConfig.Type type, @NonNull DialogTypeConfig config) {
        type.custom(config);
        return this;
    }

    synchronized boolean enqueue(@NonNull DialogHintManager.OperateInterface operate, int priority) {
        boolean returnValue = false;
        OperateRecorder currentRecorder = this.mCurrentRecorder;
        if (currentRecorder != null) {
            OperateInterface currentOperate = currentRecorder.operate.get();
            if (returnValue = ((currentOperate == null || !currentOperate.isShowing() || (currentRecorder.priority <= priority && currentRecorder.priority != DialogConfig.Priority.PROFESSIONAL)))) {
                cancelOperate(currentRecorder, DialogConfig.DismissReason.REASON_REPLACE);
                orderOperate(new OperateRecorder(operate, priority));
            }
        } else {
            returnValue = true;
            orderOperate(new OperateRecorder(operate, priority));
        }
        return returnValue;
    }

    synchronized boolean dequeue(@NonNull DialogHintManager.OperateInterface operate, int reason) {
        boolean returnValue = false;
        OperateRecorder currentRecorder = this.mCurrentRecorder;
        if (returnValue = isCurrent(operate)) {
            this.mCurrentRecorder = null;
            cancelOperate(currentRecorder, reason);
        }
        return returnValue;
    }

    private void orderOperate(@NonNull OperateRecorder recorder) {
        OperateInterface operate = recorder.operate.get();
        if (operate != null) {
            this.mCurrentRecorder = recorder;
            operate.show();
        }
    }

    private boolean cancelOperate(@NonNull DialogHintManager.OperateRecorder recorder, int reason) {
        boolean returnValue = false;
        final DialogHintManager.OperateInterface operate = recorder.operate.get();
        if (operate != null) {
            operate.hide(reason);
            returnValue = true;
        }
        if (recorder == mCurrentRecorder)
            mCurrentRecorder = null;
        return returnValue;
    }

    private boolean isCurrent(OperateInterface operate) {
        return mCurrentRecorder != null && mCurrentRecorder.is(operate);
    }

    void hideBelowPriority(@DialogPriority int priority) {
        if (mCurrentRecorder != null && mCurrentRecorder.priority <= priority)
            cancelOperate(mCurrentRecorder, DialogConfig.DismissReason.REASON_ACTIVE);
    }

    private class OperateRecorder {
        private final WeakReference<OperateInterface> operate;
        private final int priority;

        private OperateRecorder(@NonNull DialogHintManager.OperateInterface operate, int priority) {
            this.operate = new WeakReference<OperateInterface>(operate);
            this.priority = priority;
        }

        private boolean is(@Nullable DialogHintManager.OperateInterface operate) {
            return this.operate.get() == operate;
        }
    }

    interface OperateInterface {
        void show();

        void hide(int reason);

        boolean isShowing();
    }
}
