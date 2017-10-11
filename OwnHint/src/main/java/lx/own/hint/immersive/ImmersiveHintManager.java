package lx.own.hint.immersive;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 *         Created on 2017/10/11.
 */

public class ImmersiveHintManager {

    private static ImmersiveHintManager mInstance;

    public static ImmersiveHintManager $() {
        if (mInstance == null) {
            synchronized (ImmersiveHintManager.class) {
                if (mInstance == null)
                    mInstance = new ImmersiveHintManager();
            }
        }
        return mInstance;
    }

    private final Handler mHandler;
    private volatile OperateRecorder mCurrentRecorder;
    private final LinkedBlockingQueue<OperateRecorder> mRecorders;

    private ImmersiveHintManager() {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    if (msg.what == ImmersiveHintConfig.DismissReason.REASON_TIMEOUT)
                        processOperateTimeout((OperateRecorder) msg.obj);
                } catch (ClassCastException e) {

                }
            }
        };
        mRecorders = new LinkedBlockingQueue<>();
    }

    public void init(@NonNull CustomConfig config) {
        ImmersiveHintConfig.Type.Hint.custom(config);
    }

    void enqueue(@NonNull OperateInterface operate, long duration, int priority) {
        if (isCurrent(operate)) {
            mHandler.removeCallbacksAndMessages(mCurrentRecorder);
            mCurrentRecorder.duration = duration;
            mCurrentRecorder.priority = priority;
            scheduleOperateTimeout(mCurrentRecorder);
        } else {
            final OperateRecorder next = new OperateRecorder(operate, duration, priority);
            if (mCurrentRecorder != null) {
                if (mCurrentRecorder.priority >= priority || cancelOperate(mCurrentRecorder, ImmersiveHintConfig.DismissReason.REASON_REPLACE)) {
                    mRecorders.offer(next);
                } else {
                    orderOperate(next);
                }
            } else {
                orderOperate(next);
            }
        }
    }

    void cancel(@NonNull OperateInterface operate, int reason) {
        if (isCurrent(operate)) {
            cancelOperate(mCurrentRecorder, reason);
            mCurrentRecorder = null;
        } else {
            Iterator<OperateRecorder> iterator = mRecorders.iterator();
            while (iterator.hasNext()) {
                OperateRecorder next = iterator.next();
                if (next == null) continue;
                if (next.is(operate)) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    void processOperateShown(@NonNull OperateInterface operate) {
        if (isCurrent(operate)) {
            scheduleOperateTimeout(mCurrentRecorder);
        }
    }

    void processOperateHidden(@NonNull OperateInterface operate) {
        if (isCurrent(operate)) {
            mCurrentRecorder = null;
            final OperateRecorder next = mRecorders.poll();
            if (next != null)
                orderOperate(next);
        }
    }

    void runOnUIThread(@NonNull Runnable r) {
        mHandler.post(r);
    }

    private boolean orderOperate(@NonNull OperateRecorder recorder) {
        boolean returnValue = false;
        mCurrentRecorder = recorder;
        OperateInterface operate = recorder.operate;
        if (operate != null) {
            operate.show();
            returnValue = true;
        } else {
            mCurrentRecorder = null;
            final OperateRecorder next = mRecorders.poll();
            if (next != null)
                orderOperate(next);
        }
        return returnValue;
    }

    private boolean cancelOperate(@NonNull OperateRecorder recorder, int reason) {
        boolean returnValue = false;
        OperateInterface operate = recorder.operate;
        if (operate != null) {
            mHandler.removeCallbacksAndMessages(recorder);
            operate.dismiss(reason);
            returnValue = true;
        }
        return returnValue;
    }

    private void scheduleOperateTimeout(@NonNull OperateRecorder recorder) {
        long delay = recorder.duration;
        if (delay <= 0)
            delay = 100;
        mHandler.removeCallbacksAndMessages(recorder);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, ImmersiveHintConfig.DismissReason.REASON_TIMEOUT, recorder), delay);
    }

    private void processOperateTimeout(@NonNull OperateRecorder recorder) {
        mHandler.removeCallbacksAndMessages(recorder);
        cancelOperate(recorder, ImmersiveHintConfig.DismissReason.REASON_TIMEOUT);
    }

    private boolean isCurrent(@Nullable OperateInterface operate) {
        return this.mCurrentRecorder != null && this.mCurrentRecorder.is(operate);
    }

    private class OperateRecorder {
        private final OperateInterface operate;
        private int priority;
        private long duration;

        public OperateRecorder(@NonNull OperateInterface operate, long duration, int priority) {
            this.operate = operate;
            this.duration = duration;
            this.priority = priority;
        }

        public boolean is(@Nullable OperateInterface operate) {
            return this.operate == operate;
        }
    }

    interface OperateInterface {
        void show();

        void dismiss(int reason);
    }
}
