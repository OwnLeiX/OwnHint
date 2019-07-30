package lx.own.hint.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.lang.ref.WeakReference;

import lx.own.hint.HintAction;
import lx.own.hint.R;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 * Created on 2017/11/7.
 */

public class DialogHint {

    private static final int FLAG_AUTO_DISMISS = 1;
    private static final int FLAG_IS_DISMISSED = 1 << 1;
    private static final int FLAG_DEPRECATED = 1 << 2;
    private static int mScreenWidth = -1;
    private static int mHorizontalPadding = -1;
    private static int mUniversalWidth = -1;

    /**
     * 构建异形对话框提示的方法
     *
     * @param dialog 实现了{@link BizarreTypeDialog}接口的异形对话框
     * @see AutoPriorityProvider 自动提供对话框优先级的接口，使{@link DialogHint#priority(int)}自动化
     * @see RedefinableDialog 可以重定义的对话框，使异形对话框支持{@link DialogHint#redefineCancelable(boolean)} {@link DialogHint#redefineCancelableOutsideTouch(boolean)}
     * @see Able2ListenCancelDialog 可以监听取消的对话框，使异形对话框支持{@link DialogHint#extraSetOutsideCancelListener(DialogInterface.OnCancelListener)}
     */
    public static DialogHint make(@NonNull BizarreTypeDialog dialog) {
        return new DialogHint(dialog);
    }

    public static DialogHint make(@NonNull DialogConfig.Type type, @NonNull Activity activity, @NonNull String message, @StringRes int sureText, @Nullable HintAction sureAction) {
        return new DialogHint(type, activity, message, getSafeString(activity, sureText), sureAction, null, null);
    }

    public static DialogHint make(@NonNull DialogConfig.Type type, @NonNull Activity activity, @StringRes int message, @StringRes int sureText, @Nullable HintAction sureAction) {
        return new DialogHint(type, activity, getSafeString(activity, message), getSafeString(activity, sureText), sureAction, null, null);
    }

    public static DialogHint make(@NonNull DialogConfig.Type type, @NonNull Activity activity, @NonNull String message, @StringRes int sureText, @Nullable HintAction sureAction, @StringRes int cancelText, @Nullable HintAction cancelAction) {
        return new DialogHint(type, activity, message, getSafeString(activity, sureText), sureAction, getSafeString(activity, cancelText), cancelAction);
    }

    public static DialogHint make(@NonNull DialogConfig.Type type, @NonNull Activity activity, @StringRes int message, @StringRes int sureText, @Nullable HintAction sureAction, @StringRes int cancelText, @Nullable HintAction cancelAction) {
        return new DialogHint(type, activity, getSafeString(activity, message), getSafeString(activity, sureText), sureAction, getSafeString(activity, cancelText), cancelAction);
    }

    /**
     * 关闭优先级小于等于参数的所有对话框
     * - 一般情况下不需要调用
     * 当使用{@link DialogConfig.Priority#SPECIAL_LOADING}时，需要手动调用来关闭对话框
     *
     * @param priority 优先级
     */
    public static void hideBelowPriority(@DialogPriority int priority) {
        DialogHintManager.$().hideBelowPriority(priority);
    }

    /**
     * 关闭属于参数Activity的所有对话框
     * - 一般情况下不需要调用
     * <p>
     * 特别说明：{@link DialogConfig.Type#Cancelable} {@link DialogConfig.Type#UnCancelable}
     * 在点击Action后[默认]会自动关闭对话框，不需要调用此方法来关闭
     * 如果调用了{@link DialogHint#redefineAutoActionDismiss(boolean)}或新增了其他的{@link DialogConfig.Type}，才需要考虑是否需要手动调用此方法来关闭对话框
     *
     * @param activity 对话框依赖的Activity
     */
    public static void hideOwnDialog(Activity activity) {
        DialogHintManager.$().hideOwnDialog(activity);
    }

    private static boolean isActivityRunning(@Nullable Activity activity) {
        return activity != null && (activity.hasWindowFocus() || !activity.isFinishing());
    }

    private static int getScreenWidth(@NonNull Activity activity) {
        if (mScreenWidth == -1) {
            synchronized (DialogHint.class) {
                if (mScreenWidth == -1)
                    mScreenWidth = activity.getWindowManager().getDefaultDisplay().getWidth();
            }
        }
        return mScreenWidth;
    }

    private static int getHorizontalPadding(@NonNull Activity activity) {
        if (mHorizontalPadding == -1) {
            synchronized (DialogHint.class) {
                if (mHorizontalPadding == -1)
                    mHorizontalPadding = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, activity.getResources().getDisplayMetrics()));
            }
        }
        return mHorizontalPadding;
    }

    private static int getUniversalWidth(@NonNull Activity activity) {
        if (mUniversalWidth == -1) {
            synchronized (DialogHint.class) {
                if (mUniversalWidth == -1)
                    mUniversalWidth = getScreenWidth(activity) - (getHorizontalPadding(activity) << 1);
            }
        }
        return mUniversalWidth;
    }

    private static String getSafeString(Activity act, int resId) {
        String returnValue = null;
        if (act != null) {
            try {
                returnValue = act.getResources().getString(resId);
            } catch (Exception ignore) {

            }
        }
        return returnValue;
    }

    private final DialogHintManager.OperateInterface mOperate;
    private final WeakReference<Activity> mActivity;
    private final View.OnClickListener mOnClickListener;
    private final View.OnAttachStateChangeListener mAttachStateChangelistener;
    private final DialogInterface.OnCancelListener mCancelListener;
    private int mFlags;
    private int mPriority;
    private Dialog mUniversalDialog;
    private BizarreTypeDialog mBizarreTypeDialog;
    private HintAction mSureAction, mCancelAction;
    private DialogInterface.OnCancelListener mExtraCancelListener;
    private String mRecorderKey;

    {
        mPriority = DialogConfig.Priority.NORMAL;
        mOperate = new DialogHintManager.OperateInterface() {
            @Override
            public void show(String recorderKey) {
                mRecorderKey = recorderKey;
                Activity activity = mActivity.get();
                if (activity != null && !activity.isFinishing() && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed())) {
                    try {
                        if (mUniversalDialog != null && !mUniversalDialog.isShowing())
                            mUniversalDialog.show();
                        if (mBizarreTypeDialog != null && !mBizarreTypeDialog.isShowing())
                            mBizarreTypeDialog.show();
                    } catch (Exception ignore) {
                        //有些BizarreTypeDialog是DialogFragment，在Activity#onSavedInstanceState后show会抛出异常，这里也没办法判断Activity是否onSaveInstanceState,所以强行try catch
                    }
                } else {
                    dismiss(DialogConfig.DismissReason.REASON_DETACHED);
                }
            }

            @Override
            public void hide(int reason) {
                Activity activity = mActivity.get();
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        if (activity != null && !activity.isDestroyed()) {
                            if (mUniversalDialog != null && mUniversalDialog.isShowing())
                                mUniversalDialog.dismiss();
                            if (mBizarreTypeDialog != null && mBizarreTypeDialog.isShowing())
                                mBizarreTypeDialog.dismiss();
                        }
                    } else {
                        if (activity != null && !activity.isFinishing()) {
                            if (mUniversalDialog != null && mUniversalDialog.isShowing())
                                mUniversalDialog.dismiss();
                            if (mBizarreTypeDialog != null && mBizarreTypeDialog.isShowing())
                                mBizarreTypeDialog.dismiss();
                        }
                    }
                } catch (Exception ignore) {
                    //防止有些BizarreTypeDialog是DialogFragment，dismiss并没有调用DialogFragment#dismissAllowingStateLoss,在Activity#onSavedInstanceState后dismiss会抛出异常,所以强行try catch
                }
            }

            @Override
            public boolean isShowing() {
                return DialogHint.this.isShowing();
            }

            @Override
            public Activity provideActivity() {
                return mActivity.get();
            }
        };
        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mFlags & FLAG_IS_DISMISSED) > 0) return;
                final int id = v.getId();
                if (id == R.id.universalDialog_btv_leftButton) {
                    inspectAutoDismiss(DialogConfig.DismissReason.REASON_ACTION);
                    if (mCancelAction != null)
                        mCancelAction.onAction();
                } else if (id == R.id.universalDialog_btv_rightButton) {
                    inspectAutoDismiss(DialogConfig.DismissReason.REASON_ACTION);
                    if (mSureAction != null)
                        mSureAction.onAction();
                }
            }
        };
        mAttachStateChangelistener = new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                v.removeOnAttachStateChangeListener(this);
                if ((mFlags & FLAG_IS_DISMISSED) == 0) {
                    dismiss(DialogConfig.DismissReason.REASON_DETACHED);
                }
            }
        };
        mCancelListener = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dismiss(DialogConfig.DismissReason.REASON_ACTION);
                if (mExtraCancelListener != null)
                    mExtraCancelListener.onCancel(dialog);
            }
        };
    }

    public DialogHint priority(@DialogPriority int priority) {
        this.mPriority = priority;
        return this;
    }

    public DialogHint title(String title) {
        if (!TextUtils.isEmpty(title) && mUniversalDialog != null) {
            try {
                TextView titleView = mUniversalDialog.findViewById(R.id.universalDialog_btv_title);
                titleView.setText(title);
                titleView.setVisibility(View.VISIBLE);
            } catch (ClassCastException ignore) {

            }
        }
        return this;
    }

    public DialogHint redefineCancelable(boolean cancelable) {
        if (mUniversalDialog != null)
            mUniversalDialog.setCancelable(cancelable);
        if (mBizarreTypeDialog != null && mBizarreTypeDialog instanceof RedefinableDialog)
            ((RedefinableDialog) mBizarreTypeDialog).redefineCancelable(cancelable);
        return this;
    }

    public DialogHint redefineCancelableOutsideTouch(boolean cancelable) {
        if (mUniversalDialog != null)
            mUniversalDialog.setCanceledOnTouchOutside(cancelable);
        if (mBizarreTypeDialog != null && mBizarreTypeDialog instanceof RedefinableDialog)
            ((RedefinableDialog) mBizarreTypeDialog).redefineCancelableOutsideTouch(cancelable);
        return this;
    }

    public DialogHint extraSetOutsideCancelListener(DialogInterface.OnCancelListener listener) {
        mExtraCancelListener = listener;
        return this;
    }

    public DialogHint redefineAutoActionDismiss(boolean dismiss) {
        if (dismiss) {
            mFlags |= FLAG_AUTO_DISMISS;
        } else {
            mFlags &= ~FLAG_AUTO_DISMISS;
        }
        return this;
    }

    public boolean show() {
        boolean returnValue = false;
        if ((mFlags & FLAG_DEPRECATED) == 0 && isActivityRunning(mActivity.get()))
            returnValue = DialogHintManager.$().enqueue(mOperate, mPriority);
        return returnValue;
    }

    public void dismiss() {
        dismiss(DialogConfig.DismissReason.REASON_ACTIVE);
    }

    public Dialog getUniversalDialog() {
        return mUniversalDialog;
    }

    private void dismiss(int reason) {
        if ((mFlags & FLAG_DEPRECATED) == 0) {
            mFlags |= FLAG_IS_DISMISSED;
            DialogHintManager.$().dequeue(mRecorderKey, mOperate, reason);
        }
    }

    public boolean isShowing() {
        final Activity activity = mActivity.get();
        final boolean dialogShowing = (mUniversalDialog != null && mUniversalDialog.isShowing()) || (mBizarreTypeDialog != null && mBizarreTypeDialog.isShowing());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return dialogShowing && activity != null && !activity.isDestroyed();
        } else {
            return dialogShowing && activity != null && !activity.isFinishing();
        }
    }

    private DialogHint(@NonNull BizarreTypeDialog dialog) {
        this.mActivity = new WeakReference<>(dialog.provideActivity());
        //noinspection ConstantConditions
        if (dialog == null) {
            mFlags |= FLAG_DEPRECATED;
        } else {
            this.mBizarreTypeDialog = dialog;
            if (dialog instanceof AutoPriorityProvider)
                this.mPriority = ((AutoPriorityProvider) dialog).providePriority();
            if (mBizarreTypeDialog instanceof Able2ListenCancelDialog)
                ((Able2ListenCancelDialog) mBizarreTypeDialog).setOnCancelListener(mCancelListener);
        }
    }

    private DialogHint(DialogConfig.Type type, Activity activity, String message, String sureText, HintAction sureAction, String cancelText, HintAction cancelAction) {
        this.mActivity = new WeakReference<>(activity);
        if (type == null || activity == null || message == null) {
            mFlags |= FLAG_DEPRECATED;
        } else {
            recordParams(activity, type, sureAction, cancelAction);
            buildViews(activity, message, sureText, cancelText, type);
        }
    }

    private void recordParams(Activity activity, DialogConfig.Type type, HintAction sureAction, HintAction cancelAction) {
        mSureAction = sureAction;
        mCancelAction = cancelAction;
        if (type.config.actionDismiss) {
            mFlags |= FLAG_AUTO_DISMISS;
        } else {
            mFlags &= ~FLAG_AUTO_DISMISS;
        }
    }

    private void buildViews(Activity activity, String message, String sureText, String cancelText, DialogConfig.Type type) {
        mUniversalDialog = new Dialog(activity, type.config.dialogStyle);
        mUniversalDialog.setContentView(R.layout.dialog_layout);
        mUniversalDialog.setCancelable(type.config.cancelable);
        mUniversalDialog.setCanceledOnTouchOutside(type.config.cancelableTouchOutside);
        mUniversalDialog.setOnCancelListener(mCancelListener);
        final View universalDialog_ll_root = mUniversalDialog.findViewById(R.id.universalDialog_ll_root);
        final TextView messageView = mUniversalDialog.findViewById(R.id.universalDialog_btv_content);
        final TextView cancelButton = mUniversalDialog.findViewById(R.id.universalDialog_btv_leftButton);
        final TextView sureButton = mUniversalDialog.findViewById(R.id.universalDialog_btv_rightButton);
        final View dividerView = mUniversalDialog.findViewById(R.id.universalDialog_v_buttonDivider);
        messageView.setText(message);
        sureButton.setText(sureText);
        sureButton.setOnClickListener(mOnClickListener);
        if (!TextUtils.isEmpty(cancelText)) {
            dividerView.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setText(cancelText);
            cancelButton.setOnClickListener(mOnClickListener);
        } else {
            dividerView.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
            cancelButton.setOnClickListener(null);
        }
        universalDialog_ll_root.addOnAttachStateChangeListener(mAttachStateChangelistener);
        Window window = mUniversalDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.width = getUniversalWidth(activity);
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }
    }

    private void inspectAutoDismiss(int reason) {
        if ((mFlags & FLAG_AUTO_DISMISS) > 0)
            dismiss(reason);
    }

    /**
     * 用于将一些奇形怪状的Dialog纳入优先级管理。
     * 这里只关心显示和消失方法，其他由自己处理。
     */
    public interface BizarreTypeDialog {
        void show();

        void dismiss();

        boolean isShowing();

        Activity provideActivity();
    }

    /**
     * 自动提供优先级的OtherDialog接口，用于不想每次show都要写{@link DialogHint#priority(int)}的懒人
     */
    public interface AutoPriorityProvider {
        @DialogPriority
        int providePriority();
    }

    /**
     * 可重定义的Dialog接口，结合BizarreTypeDialog使用，使
     * {@link DialogHint#redefineCancelable(boolean)}
     * {@link DialogHint#redefineCancelableOutsideTouch(boolean)}
     * 能影响到传入的{@link BizarreTypeDialog}
     */
    public interface RedefinableDialog {
        void redefineCancelable(boolean cancelable);

        void redefineCancelableOutsideTouch(boolean cancelable);
    }

    /**
     * 可额外设置CancelListener的Dialog接口，结合BizarreTypeDialog使用，使
     * {@link DialogHint#extraSetOutsideCancelListener(DialogInterface.OnCancelListener)} (boolean)}
     * 能影响到传入的{@link BizarreTypeDialog}
     */
    public interface Able2ListenCancelDialog {
        void setOnCancelListener(DialogInterface.OnCancelListener listener);
    }
}
