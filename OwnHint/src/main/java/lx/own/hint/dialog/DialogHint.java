package lx.own.hint.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

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

    public static void configure(@NonNull DialogConfig.Type type, @NonNull DialogTypeConfig config) {
        DialogHintManager.$().configure(type, config);
    }

    public static DialogHint make(@NonNull BizarreTypeDialog dialog) {
        return new DialogHint(dialog);
    }

    public static DialogHint make(@NonNull DialogConfig.Type type, @NonNull Activity activity, @NonNull String message, @StringRes int sureText, @Nullable HintAction sureAction) {
        return make(type, activity, message, activity.getString(sureText), sureAction, null, null);
    }

    public static DialogHint make(@NonNull DialogConfig.Type type, @NonNull Activity activity, @StringRes int message, @StringRes int sureText, @Nullable HintAction sureAction) {
        return make(type, activity, activity.getString(message), activity.getString(sureText), sureAction, null, null);
    }

    public static DialogHint make(@NonNull DialogConfig.Type type, @NonNull Activity activity, @NonNull String message, @NonNull String sureText, @Nullable HintAction sureAction) {
        return make(type, activity, message, sureText, sureAction, null, null);
    }

    public static DialogHint make(@NonNull DialogConfig.Type type, @NonNull Activity activity, @NonNull String message, @StringRes int sureText, @Nullable HintAction sureAction, @StringRes int cancelText, @Nullable HintAction cancelAction) {
        return new DialogHint(type, activity, message, activity.getString(sureText), sureAction, activity.getString(cancelText), cancelAction);
    }

    public static DialogHint make(@NonNull DialogConfig.Type type, @NonNull Activity activity, @StringRes int message, @StringRes int sureText, @Nullable HintAction sureAction, @StringRes int cancelText, @Nullable HintAction cancelAction) {
        return new DialogHint(type, activity, activity.getString(message), activity.getString(sureText), sureAction, activity.getString(cancelText), cancelAction);
    }

    public static DialogHint make(@NonNull DialogConfig.Type type, @NonNull Activity activity, @NonNull String message, @Nullable String sureText, @Nullable HintAction sureAction, @NonNull String cancelText, @Nullable HintAction cancelAction) {
        return new DialogHint(type, activity, message, sureText, sureAction, cancelText, cancelAction);
    }

    public static void hideBelowPriority(@DialogPriority int priority) {
        DialogHintManager.$().hideBelowPriority(priority);
    }

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

    {
        mPriority = DialogConfig.Priority.NORMAL;
        mOperate = new DialogHintManager.OperateInterface() {
            @Override
            public void show() {
                Activity activity = mActivity.get();
                if (activity != null && !activity.isFinishing() && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed())) {
                    if (mUniversalDialog != null && !mUniversalDialog.isShowing())
                        mUniversalDialog.show();
                    if (mBizarreTypeDialog != null && !mBizarreTypeDialog.isShowing())
                        mBizarreTypeDialog.show();
                } else {
                    dismiss(DialogConfig.DismissReason.REASON_DETACHED);
                }
            }

            @Override
            public void hide(int reason) {
                Activity activity = mActivity.get();
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
                TextView titleView = (TextView) mUniversalDialog.findViewById(R.id.universalDialog_btv_title);
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

    public DialogHint extraSetCancelListener(DialogInterface.OnCancelListener listener) {
        mExtraCancelListener = listener;
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
            DialogHintManager.$().dequeue(mOperate, reason);
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
        this.mActivity = new WeakReference<Activity>(dialog.provideActivity());
        if (dialog == null) {
            mFlags |= FLAG_DEPRECATED;
        } else {
            this.mBizarreTypeDialog = dialog;
            if (dialog instanceof AutoPriorityProvider)
                this.mPriority = ((AutoPriorityProvider) dialog).providePriority();
        }
    }

    private DialogHint(DialogConfig.Type type, Activity activity, String message, String sureText, HintAction sureAction, String cancelText, HintAction cancelAction) {
        this.mActivity = new WeakReference<Activity>(activity);
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
        if (type.config.dialogBackgroundResId != DialogTypeConfig.NO_CONFIG)
            universalDialog_ll_root.setBackgroundResource(type.config.dialogBackgroundResId);

        final TextView messageView = (TextView) mUniversalDialog.findViewById(R.id.universalDialog_btv_content);
        if (type.config.contentAppearance != DialogTypeConfig.NO_CONFIG)
            messageView.setTextAppearance(activity, type.config.contentAppearance);

        final TextView cancelButton = (TextView) mUniversalDialog.findViewById(R.id.universalDialog_btv_leftButton);
        if (type.config.cancelButtonBackgroundResId != DialogTypeConfig.NO_CONFIG)
            cancelButton.setBackgroundResource(type.config.cancelButtonBackgroundResId);
        if (type.config.cancelButtonAppearance != DialogTypeConfig.NO_CONFIG)
            cancelButton.setTextAppearance(activity, type.config.cancelButtonAppearance);

        final TextView sureButton = (TextView) mUniversalDialog.findViewById(R.id.universalDialog_btv_rightButton);
        if (type.config.sureButtonBackgroundResId != DialogTypeConfig.NO_CONFIG)
            sureButton.setBackgroundResource(type.config.sureButtonBackgroundResId);
        if (type.config.sureButtonAppearance != DialogTypeConfig.NO_CONFIG)
            sureButton.setTextAppearance(activity, type.config.sureButtonAppearance);

        if (type.config.titleAppearance != DialogTypeConfig.NO_CONFIG) {
            final TextView title = (TextView) mUniversalDialog.findViewById(R.id.universalDialog_btv_title);
            title.setTextAppearance(activity, type.config.titleAppearance);
        }

        final View buttonDivider = mUniversalDialog.findViewById(R.id.universalDialog_v_buttonDivider);
        final View contentDivider = mUniversalDialog.findViewById(R.id.universalDialog_v_contentDivider);
        if (type.config.hasContentDivider) {
            contentDivider.setVisibility(View.VISIBLE);
            contentDivider.setBackgroundColor(type.config.dividerColor);
        } else {
            contentDivider.setVisibility(View.GONE);
        }
        messageView.setText(message);
        sureButton.setText(sureText);
        sureButton.setOnClickListener(mOnClickListener);
        if (!TextUtils.isEmpty(cancelText)) {
            if (type.config.hasButtonDivider) {
                buttonDivider.setVisibility(View.VISIBLE);
                buttonDivider.setBackgroundColor(type.config.dividerColor);
            } else {
                buttonDivider.setVisibility(View.GONE);
            }
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setText(cancelText);
            cancelButton.setOnClickListener(mOnClickListener);
        } else {
            buttonDivider.setVisibility(View.GONE);
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

    public interface RedefinableDialog {
        void redefineCancelable(boolean cancelable);

        void redefineCancelableOutsideTouch(boolean cancelable);
    }
}
