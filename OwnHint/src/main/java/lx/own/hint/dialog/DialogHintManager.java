package lx.own.hint.dialog;

import android.app.Activity;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import java.util.Set;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 * Created on 2017/11/7.
 */

public class DialogHintManager {
    private static volatile DialogHintManager mInstance;

    public static DialogHintManager $() {
        if (mInstance == null) {
            synchronized (DialogHintManager.class) {
                if (mInstance == null)
                    mInstance = new DialogHintManager();
            }
        }
        return mInstance;
    }

    private volatile DialogHintManager.OperateRecorder mCurrentSpecialRecorder;//特殊的Dialog，可以和其他叠加
    private volatile ArrayMap<String, OperateRecorder> mRecorders;
    private volatile boolean hasAnyProfessionalShowing;

    private DialogHintManager() {
        mRecorders = new ArrayMap<>();
    }

    public DialogHintManager configure(@NonNull DialogConfig.Type type, @NonNull DialogTypeConfig config) {
        type.custom(config);
        return this;
    }

    synchronized boolean enqueue(@NonNull DialogHintManager.OperateInterface operate, int priority) {
        if (hasAnyProfessionalShowing) return false;
        boolean returnValue = false;
        if (priority == DialogConfig.Priority.SPECIAL_LOADING) {
            //如果当前有Professional的对话框在显示，则不显示这个特殊的loading圈
            final OperateRecorder specialRecorder = this.mCurrentSpecialRecorder;
            if (specialRecorder != null)
                cancelSpecialOperate(specialRecorder, DialogConfig.DismissReason.REASON_REPLACE);
            orderSpecialOperate(new OperateRecorder(operate, priority));
            returnValue = true;
        } else if (priority == DialogConfig.Priority.PROFESSIONAL) {
            final Activity dependentAct = operate.provideActivity();
            if (dependentAct == null || dependentAct.isFinishing() || (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || dependentAct.isDestroyed()))
                return false;
            cancelBelowPriority(DialogConfig.Priority.HARD, DialogConfig.DismissReason.REASON_REPLACE);
            final String recorderKey = generateRecorderKey(dependentAct);
            orderOperate(recorderKey, new OperateRecorder(operate, priority));
        } else {
            final Activity dependentAct = operate.provideActivity();
            if (dependentAct == null || dependentAct.isFinishing() || (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || dependentAct.isDestroyed()))
                return false;
            final String recorderKey = generateRecorderKey(dependentAct);
            final OperateRecorder currentRecorder = this.mRecorders.get(recorderKey);
            if (currentRecorder != null) {
                OperateInterface currentOperate = currentRecorder.get();
                if (returnValue = (/*currentOperate == null || */!currentOperate.isShowing())) {
                    orderOperate(recorderKey, new OperateRecorder(operate, priority));
                } else if (currentOperate == operate) {
                    returnValue = true;
                } else {
                    if (returnValue = currentRecorder.priority <= priority) {
                        cancelOperate(recorderKey, currentRecorder, DialogConfig.DismissReason.REASON_REPLACE);
                        orderOperate(recorderKey, new OperateRecorder(operate, priority));
                    }
                }
            } else {
                returnValue = true;
                orderOperate(recorderKey, new OperateRecorder(operate, priority));
            }
        }
        return returnValue;
    }

    synchronized boolean dequeue(String recorderKey, @NonNull OperateInterface operate, int reason) {
        boolean returnValue = false;
        final OperateRecorder currentRecorder = mRecorders.get(recorderKey);
        if (returnValue = isCurrent(currentRecorder, operate)) {
            mRecorders.remove(recorderKey);
            cancelOperate(recorderKey, currentRecorder, reason);
        }
        if (!returnValue) {
            OperateRecorder currentSpecialRecorder = this.mCurrentSpecialRecorder;
            if (returnValue = isSpecialCurrent(operate)) {
                this.mCurrentSpecialRecorder = null;
                cancelSpecialOperate(currentSpecialRecorder, reason);
            }
        }
        return returnValue;
    }

    private void orderOperate(String recorderKey, @NonNull OperateRecorder recorder) {
        final OperateInterface operate = recorder.get();
        if (operate != null && !operate.isShowing()) {
            mRecorders.put(recorderKey, recorder);
            operate.show(recorderKey);
            if (recorder.priority == DialogConfig.Priority.PROFESSIONAL)
                hasAnyProfessionalShowing = true;
        }
    }

    private void orderSpecialOperate(OperateRecorder recorder) {
        OperateInterface operate = recorder.get();
        if (operate != null && !operate.isShowing()) {
            this.mCurrentSpecialRecorder = recorder;
            operate.show(null);
        }
    }

    private boolean cancelOperate(@Nullable String recorderKey, @NonNull DialogHintManager.OperateRecorder recorder, int reason) {
        boolean returnValue = false;
        final DialogHintManager.OperateInterface operate = recorder.get();
        if (operate != null && operate.isShowing()) {
            operate.hide(reason);
            returnValue = true;
        }
        final OperateRecorder currRecorder = mRecorders.get(recorderKey);
        if (recorder == currRecorder) {
            mRecorders.remove(recorderKey);
        }
        if (recorder.priority == DialogConfig.Priority.PROFESSIONAL)
            hasAnyProfessionalShowing = false;
        return returnValue;
    }

    private boolean cancelSpecialOperate(@NonNull DialogHintManager.OperateRecorder recorder, int reason) {
        boolean returnValue = false;
        final DialogHintManager.OperateInterface operate = recorder.get();
        if (operate != null && operate.isShowing()) {
            operate.hide(reason);
            returnValue = true;
        }
        if (recorder == mCurrentSpecialRecorder) {
            mCurrentSpecialRecorder = null;
        }
        return returnValue;
    }

    private boolean isCurrent(OperateRecorder currentRecorder, OperateInterface operate) {
        return currentRecorder != null && currentRecorder.is(operate);
    }

    private boolean isSpecialCurrent(OperateInterface operate) {
        return mCurrentSpecialRecorder != null && mCurrentSpecialRecorder.is(operate);
    }

    synchronized void cancelBelowPriority(@DialogPriority int priority, int reason) {
        final Set<String> keys = mRecorders.keySet();
        OperateRecorder recorder;
        for (String key : keys) {
            recorder = mRecorders.get(key);
            if (recorder != null && recorder.priority <= priority)
                cancelOperate(key, recorder, DialogConfig.DismissReason.REASON_ACTIVE);
        }
        if (mCurrentSpecialRecorder != null && mCurrentSpecialRecorder.priority <= priority)
            cancelSpecialOperate(mCurrentSpecialRecorder, DialogConfig.DismissReason.REASON_ACTIVE);
    }


    synchronized void hideBelowPriority(@DialogPriority int priority) {
        final Set<String> keys = mRecorders.keySet();
        OperateRecorder recorder;
        for (String key : keys) {
            recorder = mRecorders.get(key);
            if (recorder != null && recorder.priority <= priority)
                cancelOperate(key, recorder, DialogConfig.DismissReason.REASON_ACTIVE);
        }
        if (mCurrentSpecialRecorder != null && mCurrentSpecialRecorder.priority <= priority)
            cancelSpecialOperate(mCurrentSpecialRecorder, DialogConfig.DismissReason.REASON_ACTIVE);
    }

    synchronized void hideOwnDialog(Activity activity) {
        if (activity == null) return;
        final String recorderKey = generateRecorderKey(activity);
        final OperateRecorder recorder = mRecorders.get(recorderKey);
        if (recorder != null)
            cancelOperate(recorderKey, recorder, DialogConfig.DismissReason.REASON_ACTIVE);
        if (mCurrentSpecialRecorder != null && mCurrentSpecialRecorder.get().provideActivity() == activity)
            cancelSpecialOperate(mCurrentSpecialRecorder, DialogConfig.DismissReason.REASON_ACTIVE);
    }

    private String generateRecorderKey(Activity act) {
        String key = "";
        if (act != null)
            key = act.getClass().getName();
        return key;
    }

    private class OperateRecorder {
        private final DialogHintManager.OperateInterface operate;
        private final int priority;

        private OperateRecorder(@NonNull DialogHintManager.OperateInterface operate, int priority) {
            this.operate = operate;
            this.priority = priority;
        }

        private boolean is(@Nullable DialogHintManager.OperateInterface operate) {
            return this.operate == operate;
        }

        private OperateInterface get() {
            return operate;
        }
    }

    interface OperateInterface {
        void show(String recorderKey);

        void hide(int reason);

        boolean isShowing();

        Activity provideActivity();
    }
}
