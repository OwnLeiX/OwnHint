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
 * Created on 2017/10/11.
 */

final class ImmersiveHintManager {
    private static ImmersiveHintManager mInstance;

    static ImmersiveHintManager $() {
        if (mInstance == null) {
            synchronized (ImmersiveHintManager.class) {
                if (mInstance == null)
                    mInstance = new ImmersiveHintManager();
            }
        }
        return mInstance;
    }

    private final int MSG_SHOW = -1;
    private final int MSG_DISMISS = -2;
    private final int MSG_TIME_OUT = -3;
    private final int MSG_DISMISS_MISMATCHED = -4;
    private final Handler mHandler;
    private volatile OperateRecorder mCurrentRecorder;
    private final LinkedBlockingQueue<OperateRecorder> mRankSSPriorRecorders, mRankSPriorRecorders,
            mRankAPriorRecorders, mRankBPriorRecorders, mRankCPriorRecorders;

    {
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_SHOW) {
                    ((OperateRecorder) msg.obj).operate.show();
                } else if (msg.what == MSG_DISMISS) {
                    ((OperateRecorder) msg.obj).operate.hide(msg.arg1);
                } else if (msg.what == MSG_TIME_OUT) {
                    processOperateTimeout((OperateRecorder) msg.obj);
                } else if (msg.what == MSG_DISMISS_MISMATCHED) {
                    ((OperateInterface) msg.obj).hide(msg.arg1);
                }
            }
        };
        mRankSSPriorRecorders = new LinkedBlockingQueue<>();
        mRankSPriorRecorders = new LinkedBlockingQueue<>();
        mRankAPriorRecorders = new LinkedBlockingQueue<>();
        mRankBPriorRecorders = new LinkedBlockingQueue<>();
        mRankCPriorRecorders = new LinkedBlockingQueue<>();
    }

    private ImmersiveHintManager() {
    }

    ImmersiveHintManager configure(@NonNull ImmersiveConfig.Type type, @NonNull HintTypeConfig config) {
        type.custom(config);
        return this;
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
                if (mCurrentRecorder.priority > priority || cancelOperate(mCurrentRecorder, ImmersiveConfig.DismissReason.REASON_REPLACE)) {
                    offerRecorder(next);
                } else {
                    orderOperate(next);
                }
            } else {
                orderOperate(next);
            }
        }
    }

    boolean dequeue(@NonNull OperateInterface operate, int reason) {
        boolean returnValue;
        if (isCurrent(operate)) {
            cancelOperate(mCurrentRecorder, reason);
            returnValue = true;
        } else {
            returnValue = removeInQueue(operate, mRankSPriorRecorders)
                    || removeInQueue(operate, mRankAPriorRecorders)
                    || removeInQueue(operate, mRankBPriorRecorders);
            if (!returnValue)
                mHandler.sendMessage(Message.obtain(mHandler, MSG_DISMISS_MISMATCHED, reason, 0, operate));
        }
        return returnValue;
    }

    void processOperateShown(@NonNull OperateInterface operate) {
        if (isCurrent(operate)) {
            scheduleOperateTimeout(mCurrentRecorder);
        }
    }

    void processOperateHidden(@NonNull OperateInterface operate) {
        if (isCurrent(operate)) {
            mCurrentRecorder = null;
            final OperateRecorder next = pollRecorder();
            if (next != null)
                orderOperate(next);
        }
    }

    private boolean orderOperate(@NonNull OperateRecorder recorder) {
        boolean returnValue = false;
        mCurrentRecorder = recorder;
        final OperateInterface operate = recorder.operate;
        if (operate != null) {
            mHandler.sendMessage(Message.obtain(mHandler, MSG_SHOW, recorder));
            returnValue = true;
        } else {
            final OperateRecorder next = pollRecorder();
            if (next != null)
                orderOperate(next);
        }
        return returnValue;
    }

    private boolean cancelOperate(@NonNull OperateRecorder recorder, int reason) {
        boolean returnValue = false;
        final OperateInterface operate = recorder.operate;
        if (operate != null) {
            mHandler.removeCallbacksAndMessages(recorder);
            mHandler.sendMessage(Message.obtain(mHandler, MSG_DISMISS, reason, 0, recorder));
            returnValue = true;
        }
        return returnValue;
    }

    private void scheduleOperateTimeout(@NonNull OperateRecorder recorder) {
        long delay = recorder.duration;
        if (delay <= 0)
            delay = 100;
        mHandler.removeCallbacksAndMessages(recorder);
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_TIME_OUT, recorder), delay);
    }

    private void processOperateTimeout(@NonNull OperateRecorder recorder) {
        mHandler.removeCallbacksAndMessages(recorder);
        cancelOperate(recorder, ImmersiveConfig.DismissReason.REASON_TIMEOUT);
    }

    private boolean isCurrent(@Nullable OperateInterface operate) {
        return this.mCurrentRecorder != null && this.mCurrentRecorder.is(operate);
    }

    private void offerRecorder(@NonNull OperateRecorder recorder) {
        if (recorder.priority == ImmersiveConfig.Priority.PROFESSIONAL) {
            mRankSSPriorRecorders.offer(recorder);
        } else if (recorder.priority == ImmersiveConfig.Priority.HARD) {
            mRankSPriorRecorders.offer(recorder);
        } else if (recorder.priority == ImmersiveConfig.Priority.NORMAL) {
            mRankAPriorRecorders.offer(recorder);
        } else if (recorder.priority == ImmersiveConfig.Priority.EASY) {
            mRankBPriorRecorders.offer(recorder);
        } else {
            mRankCPriorRecorders.offer(recorder);
        }
    }

    private OperateRecorder pollRecorder() {
        OperateRecorder returnValue = mRankSSPriorRecorders.poll();
        if (returnValue == null)
            returnValue = mRankSPriorRecorders.poll();
        if (returnValue == null)
            returnValue = mRankAPriorRecorders.poll();
        if (returnValue == null)
            returnValue = mRankBPriorRecorders.poll();
        if (returnValue == null)
            returnValue = mRankCPriorRecorders.poll();
        return returnValue;
    }

    private boolean removeInQueue(@NonNull OperateInterface target, @NonNull LinkedBlockingQueue<OperateRecorder> queue) {
        Iterator<OperateRecorder> iterator = queue.iterator();
        while (iterator.hasNext()) {
            OperateRecorder next = iterator.next();
            if (next == null) continue;
            if (next.is(target)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    private class OperateRecorder {
        private OperateInterface operate;
        private int priority;
        private long duration;

        private OperateRecorder(@NonNull OperateInterface operate, long duration, int priority) {
            this.operate = operate;
            this.duration = duration;
            this.priority = priority;
        }

        private boolean is(@Nullable OperateInterface operate) {
            return this.operate == operate;
        }
    }

    interface OperateInterface {
        void show();

        void hide(int reason);
    }
}
