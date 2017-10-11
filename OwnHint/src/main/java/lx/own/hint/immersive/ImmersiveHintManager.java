package lx.own.hint.immersive;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
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

    static int REASON_TIMEOUT = 1;
    static int REASON_REPLACE = 2;
    static int REASON_ACTION = 3;
    static int REASON_CODES = 4;

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
                    if (msg.what == REASON_TIMEOUT)
                        processTimeoutOperate((OperateRecorder) msg.obj);
                } catch (ClassCastException e) {

                }
            }
        };
        mRecorders = new LinkedBlockingQueue<>();
    }

    public void init(@NonNull CustomConfig config) {
        ImmersiveHintConfig.Params.update(config);
    }

    void enqueue(@NonNull OperateInterface operate, long duration) {
        if (isCurrent(operate)) {
            mHandler.removeCallbacksAndMessages(mCurrentRecorder);
            mCurrentRecorder.duration = duration;
            scheduleTimeoutOperate(mCurrentRecorder);
        } else {
            final OperateRecorder next = new OperateRecorder(operate, duration);
            if (mCurrentRecorder != null && cancelOperate(mCurrentRecorder, REASON_REPLACE)) {
                mRecorders.offer(next);
                return;
            } else {
                orderOperate(next);
            }
        }
    }

    void dismiss(@NonNull OperateInterface operate, int reason) {
        if (isCurrent(operate)) {
            cancelOperate(mCurrentRecorder, reason);
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

    void onShown(@NonNull OperateInterface operate) {
        if (isCurrent(operate)) {
            scheduleTimeoutOperate(mCurrentRecorder);
        }
    }

    void onDismissed(@NonNull OperateInterface operate) {
        if (isCurrent(operate)) {
            mCurrentRecorder = null;
            final OperateRecorder next = mRecorders.poll();
            if (next != null)
                orderOperate(next);
        }
    }

    private boolean orderOperate(@NonNull OperateRecorder recorder) {
        boolean returnValue = false;
        mCurrentRecorder = recorder;
        OperateInterface operate = recorder.operate.get();
        if (operate != null) {
            operate.show();
            returnValue = true;
        } else {
            mCurrentRecorder = null;
        }
        return returnValue;
    }

    private boolean cancelOperate(@NonNull OperateRecorder recorder, int reason) {
        boolean returnValue = false;
        OperateInterface operate = recorder.operate.get();
        if (operate != null) {
            mHandler.removeCallbacksAndMessages(recorder);
            operate.dismiss(reason);
            returnValue = true;
        }
        return returnValue;
    }

    private void scheduleTimeoutOperate(@NonNull OperateRecorder recorder) {
        long delay = recorder.duration <= 0 ? ImmersiveHintConfig.Params.duration : recorder.duration;
        if (delay <= 0)
            delay = 100;
        mHandler.removeCallbacksAndMessages(recorder);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, REASON_TIMEOUT, recorder), delay);
    }

    private void processTimeoutOperate(@NonNull OperateRecorder recorder) {
        mHandler.removeCallbacksAndMessages(recorder);
        cancelOperate(recorder, REASON_TIMEOUT);
    }

    private boolean isCurrent(@Nullable OperateInterface operate) {
        return this.mCurrentRecorder != null && this.mCurrentRecorder.is(operate);
    }

    private class OperateRecorder {
        private final WeakReference<OperateInterface> operate;
        private long duration;

        public OperateRecorder(@NonNull OperateInterface operate, long duration) {
            this.operate = new WeakReference<>(operate);
            this.duration = duration;
        }

        public boolean is(@Nullable OperateInterface operate) {
            OperateInterface self = this.operate.get();
            return self != null && self == operate;
        }
    }

    interface OperateInterface {
        void show();

        void dismiss(int reason);
    }
}
