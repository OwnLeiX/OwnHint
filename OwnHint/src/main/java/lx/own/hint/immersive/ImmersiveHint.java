package lx.own.hint.immersive;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

import lx.own.hint.HintAction;
import lx.own.hint.R;


/**
 * <p>沉浸式提示</p><br/>
 *
 * @author Lx
 * Create on 10/10/2017.
 */

final public class ImmersiveHint {

    private static final int FLAG_AUTO_DISMISS = 1;
    private static final int FLAG_ACTION_EXECUTED = 1 << 1;
    private static final int FLAG_IN_ANIMATING = 1 << 2;
    private static final int FLAG_OUT_ANIMATING = 1 << 3;
    private static final int FLAG_REPLACE_WAITING = 1 << 4;
    private static final int FLAG_IS_FINISHED = 1 << 5;
    private static final int FLAG_SYSTEM_CANCELED = 1 << 6;
    private static final int FLAG_DEPRECATED = 1 << 7;

    private static volatile int mStatusHeight = -1;
    private static volatile WeakReference<ViewGroup> mFanciedParent;

    public static void configure(@NonNull ImmersiveConfig.Type type, @NonNull HintTypeConfig config) {
        ImmersiveHintManager.$().configure(type, config);
    }

    public static ImmersiveHint make(@NonNull ImmersiveConfig.Type type, @NonNull Activity activity, @StringRes int messageRes) {
        return make(type, activity, messageRes, -1, null);
    }

    public static ImmersiveHint make(@NonNull ImmersiveConfig.Type type, @NonNull Activity activity, @NonNull String message) {
        return make(type, activity, message, "", null);
    }

    public static ImmersiveHint make(@NonNull ImmersiveConfig.Type type, @NonNull Activity activity, @StringRes int messageRes, @StringRes int actionRes, HintAction action) {
        Resources resources = activity.getResources();
        return new ImmersiveHint(type, activity, resources.getString(messageRes), actionRes == -1 ? "" : resources.getString(actionRes), action);
    }

    public static ImmersiveHint make(@NonNull ImmersiveConfig.Type type, @NonNull Activity activity, @NonNull String message, @Nullable String actionText, HintAction action) {
        return new ImmersiveHint(type, activity, message, actionText, action);
    }

    private static void viewHeightCompat(View view) {
        int statusBarHeight = getStatusBarHeight(view.getContext());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        view.setPadding(view.getPaddingLeft()
                , view.getPaddingTop() + statusBarHeight, view.getPaddingRight(), view.getPaddingBottom());
        view.setMinimumHeight(statusBarHeight);
//        } else {
//            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
//            if (layoutParams != null && layoutParams instanceof ViewGroup.MarginLayoutParams) {
//                ((ViewGroup.MarginLayoutParams) layoutParams).topMargin += statusBarHeight;
//            }
//        }
    }

    private static int getStatusBarHeight(Context context) {
        if (mStatusHeight == -1) {
            synchronized (ImmersiveHint.class) {
                if (mStatusHeight == -1) {
                    try {
                        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
                        if (resourceId > 0) {
                            mStatusHeight = context.getResources().getDimensionPixelSize(resourceId);
                        }
                    } catch (Exception e) {
                        return 0;
                    }
                }
            }
        }
        return mStatusHeight;
    }

    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content) {
                    return (ViewGroup) view;
                } else {
                    fallback = (ViewGroup) view;
                }
            }
            if (view != null) {
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);
        return fallback;
    }

    private static WeakReference<ViewGroup> buildFanciedParent() {
        if (mFanciedParent == null) {
            synchronized (ImmersiveHint.class) {
                if (mFanciedParent == null)
                    mFanciedParent = new WeakReference<>(null);
            }
        }
        return mFanciedParent;
    }

    private static boolean isActivityRunning(@Nullable Activity activity) {
        return activity != null && (activity.hasWindowFocus() || !activity.isFinishing());
    }

    private int mFlags;
    private int mPriority;
    private ImmersiveConfig.Type mType;
    private WeakReference<ViewGroup> mParent;
    private WeakReference<Activity> mActivity;
    private ImmersiveLayout mView;
    private Animator mLayOutAnim, mLayInAnim;
    private HintAction mAction;
    private final ImmersiveHintManager.OperateInterface mOperate;
    private final View.OnAttachStateChangeListener mParentDetachListener;
    private final ImmersiveLayout.OnDetachedListener mViewDetachListener;
    private final ImmersiveLayout.OnUpglideListener mViewUpglideListener;
    private final View.OnClickListener mClickListener;

    {
        mFlags = 0;
        mPriority = ImmersiveConfig.Priority.NORMAL;
        mOperate = new ImmersiveHintManager.OperateInterface() {
            @Override
            public void show() {
                beginTransition();
            }

            @Override
            public void hide(int reason) {
                endTransition(reason);
            }
        };
        mParentDetachListener = new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                v.removeOnAttachStateChangeListener(this);
                if (mView.getParent() == v) {
                    mView.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelLayAnim();
                            dismiss(ImmersiveConfig.DismissReason.REASON_DETACHED);
                        }
                    });
                }
                if (mParent.get() == v)
                    mParent.clear();
            }
        };
        mViewDetachListener = new ImmersiveLayout.OnDetachedListener() {
            @Override
            public void onDetachedFromWindow(View view) {
                if (mView.getParent() != null) {
                    mView.setDetachedListener(null);
                    mView.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelLayAnim();
                            dismiss(ImmersiveConfig.DismissReason.REASON_DETACHED);
                        }
                    });
                }
            }
        };
        mClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAction != null && (mFlags & FLAG_ACTION_EXECUTED) == 0) {
                    mFlags |= FLAG_ACTION_EXECUTED;
                    mAction.onAction();
                }
                if ((mFlags & FLAG_AUTO_DISMISS) == FLAG_AUTO_DISMISS)
                    dismiss(ImmersiveConfig.DismissReason.REASON_ACTION);
            }
        };
        mViewUpglideListener = new ImmersiveLayout.OnUpglideListener() {
            @Override
            public void onUpglide() {
                dismiss(ImmersiveConfig.DismissReason.REASON_ACTIVE);
            }
        };
    }

    private ImmersiveHint(@NonNull ImmersiveConfig.Type type, @NonNull Activity activity, @NonNull String message, @Nullable String actionText, HintAction action) {
        if (type == null || activity == null || message == null) {
            mFlags |= FLAG_DEPRECATED;
            return;
        }
        recordParams(activity, type, action);
        buildViews(activity, message, actionText, type);
    }

    public void show() {
        if ((mFlags & FLAG_DEPRECATED) == 0)
            show(mType.config.showDuration);
    }

    public void show(final long duration) {
        if ((mFlags & FLAG_DEPRECATED) == 0)
            ImmersiveHintManager.$().enqueue(mOperate, duration, mPriority);
    }

    public void dismiss() {
        if ((mFlags & FLAG_DEPRECATED) == 0)
            dismiss(ImmersiveConfig.DismissReason.REASON_ACTIVE);
    }

    private void dismiss(final int reason) {
        if ((mFlags & FLAG_DEPRECATED) == 0)
            ImmersiveHintManager.$().dequeue(mOperate, reason);
    }

    public ImmersiveHint withIcon(boolean show) {
        if ((mFlags & FLAG_DEPRECATED) == 0)
            mView.mIconView.setVisibility(show ? View.VISIBLE : View.GONE);
        return this;
    }

    public ImmersiveHint priority(@ImmersivePriority int priority) {
        this.mPriority = priority;
        return this;
    }

    public ImmersiveHint redefineIconDrawable(@DrawableRes int resId) {
        if ((mFlags & FLAG_DEPRECATED) == 0)
            mView.mIconView.setImageResource(resId);
        return this;
    }

    public ImmersiveHint redefineIconSize(int radius) {
        if ((mFlags & FLAG_DEPRECATED) == 0) {
            ViewGroup.LayoutParams params = mView.mIconView.getLayoutParams();
            params.width = radius;
            params.height = radius;
            mView.mIconView.setLayoutParams(params);
        }
        return this;
    }

    public ImmersiveHint redefineBackgroundColor(@ColorInt int color) {
        if ((mFlags & FLAG_DEPRECATED) == 0)
            mView.setBackgroundColor(color);
        return this;
    }

    public ImmersiveHint redefineBackgroundDrawable(@DrawableRes int resId) {
        if ((mFlags & FLAG_DEPRECATED) == 0)
            mView.setBackgroundResource(resId);
        return this;
    }

    public ImmersiveHint redefineMessageTextSize(int size) {
        if ((mFlags & FLAG_DEPRECATED) == 0)
            mView.mMessageView.setTextSize(size);
        return this;
    }

    public ImmersiveHint redefineMessageTextColor(@ColorInt int color) {
        if ((mFlags & FLAG_DEPRECATED) == 0)
            mView.mMessageView.setTextColor(color);
        return this;
    }

    public ImmersiveHint redefineActionTextSize(int size) {
        if ((mFlags & FLAG_DEPRECATED) == 0)
            mView.mActionView.setTextSize(size);
        return this;
    }

    public ImmersiveHint redefineActionTextColor(@ColorInt int color) {
        if ((mFlags & FLAG_DEPRECATED) == 0)
            mView.mActionView.setTextColor(color);
        return this;
    }

    public ImmersiveHint redefineActionBackgroundDrawable(@DrawableRes int resId) {
        if ((mFlags & FLAG_DEPRECATED) == 0)
            mView.mActionView.setBackgroundResource(resId);
        return this;
    }

    public ImmersiveHint redefineTransmissionTouchEvent(boolean transmission) {
        if ((mFlags & FLAG_DEPRECATED) == 0)
            mView.setClickable(!transmission);
        return this;
    }

    public ImmersiveHint redefineActionDismiss(boolean dismissByAction) {
        if (dismissByAction) {
            mFlags |= FLAG_AUTO_DISMISS;
        } else {
            mFlags &= ~FLAG_AUTO_DISMISS;
        }
        return this;
    }

    private void recordParams(Activity activity, ImmersiveConfig.Type type, HintAction action) {
        this.mType = type;
        this.mActivity = new WeakReference<>(activity);
        this.mAction = action;
        final boolean dismissByAction = mType.config.actionDismiss;
        if (dismissByAction) {
            mFlags |= FLAG_AUTO_DISMISS;
        } else {
            mFlags &= ~FLAG_AUTO_DISMISS;
        }
    }

    private void buildViews(Activity activity, String message, String actionText, ImmersiveConfig.Type type) {
        final ViewGroup parent = findSuitableParent(activity.getWindow().getDecorView());
        if (parent != null && ViewCompat.isAttachedToWindow(parent)) {
            parent.addOnAttachStateChangeListener(mParentDetachListener);
            mParent = new WeakReference<>(parent);
        } else {
            mParent = buildFanciedParent();//This just makes mParent != null;
        }
        mView = (ImmersiveLayout) activity.getLayoutInflater().inflate(R.layout.immersive_layout, parent, false);
        mView.adaptContent(type, message, actionText, mClickListener);
        mView.setDetachedListener(mViewDetachListener);
        mView.setOnUpglideListener(mViewUpglideListener);
        viewHeightCompat(mView);
    }

    private void beginTransition() {
        ViewGroup parent = mParent.get();
        if (parent == null || !isActivityRunning(this.mActivity.get())) {
            inspectOverallModel();
        } else {
            if (mView.getParent() == null)
                parent.addView(mView);
            if (ViewCompat.isLaidOut(mView)) {
                animateIn();
            } else {
                mView.setOnLayoutChangedListener(new ImmersiveLayout.OnLayoutChangedListener() {
                    @Override
                    public void onLayoutChanged(View view, int left, int top, int right, int bottom) {
                        animateIn();
                        mView.setOnLayoutChangedListener(null);
                    }
                });
            }
        }
    }

    private void endTransition(int reason) {
        if (reason == ImmersiveConfig.DismissReason.REASON_DETACHED
                || mView.getVisibility() != View.VISIBLE
                || !isActivityRunning(this.mActivity.get())) {
            dispatchHidden();
        } else {
            if ((mFlags & FLAG_IN_ANIMATING) == FLAG_IN_ANIMATING) {
                mFlags |= FLAG_REPLACE_WAITING;
            } else {
                animateOut();
            }
        }
    }

    private void inspectOverallModel() {
        final HintTypeConfig.OverallModelSupporter supporter = mType.config.overallModelSupporter;
        if (supporter != null) {
            final Activity act = supporter.provideTopActivity();
            if (act != null && !act.isFinishing()) {
                final ViewGroup parent = findSuitableParent(act.getWindow().getDecorView());
                if (parent != null && ViewCompat.isAttachedToWindow(parent)) {
                    parent.addOnAttachStateChangeListener(mParentDetachListener);
                    mActivity = new WeakReference<>(act);
                    mParent = new WeakReference<>(parent);
                    if (isActivityRunning(act)) {
                        if (mView.getParent() == null)
                            parent.addView(mView);
                        if (ViewCompat.isLaidOut(mView)) {
                            animateIn();
                        } else {
                            mView.setOnLayoutChangedListener(new ImmersiveLayout.OnLayoutChangedListener() {
                                @Override
                                public void onLayoutChanged(View view, int left, int top, int right, int bottom) {
                                    animateIn();
                                    mView.setOnLayoutChangedListener(null);
                                }
                            });
                        }
                        return;
                    }
                }
            }
        }
        dispatchHidden();
    }

    private void animateIn() {
        if ((mFlags & FLAG_IN_ANIMATING) == FLAG_IN_ANIMATING)
            return;
        mView.setTranslationY(-mView.getMeasuredHeight());
        mLayInAnim = ObjectAnimator.ofFloat(mView, "translationY", mView.getTranslationY(), 0f);
        mLayInAnim.setDuration(mType.config.animDuration);
        mLayInAnim.setInterpolator(mType.config.showInterpolator);
        mLayInAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mFlags |= FLAG_IN_ANIMATING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeListener(this);
                ImmersiveHint.this.mLayInAnim = null;
                mFlags &= ~FLAG_IN_ANIMATING;
                if ((mFlags & FLAG_REPLACE_WAITING) == FLAG_REPLACE_WAITING) {
                    mFlags &= ~FLAG_REPLACE_WAITING;
                    if ((mFlags & FLAG_IS_FINISHED) == 0)
                        animateOut();
                } else {
                    dispatchShown();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if ((mFlags & FLAG_SYSTEM_CANCELED) == 0)
                    dismiss(ImmersiveConfig.DismissReason.REASON_DETACHED);
                animation.removeListener(this);
                ImmersiveHint.this.mLayOutAnim = null;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mLayInAnim.start();
    }

    private void animateOut() {
        if ((mFlags & FLAG_OUT_ANIMATING) == FLAG_OUT_ANIMATING)
            return;
        mLayOutAnim = ObjectAnimator.ofFloat(mView, "translationY", 0f, -mView.getHeight());
        mLayOutAnim.setDuration(mType.config.animDuration);
        mLayOutAnim.setInterpolator(mType.config.dismissInterpolator);
        mLayOutAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mFlags |= FLAG_OUT_ANIMATING;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeListener(this);
                ImmersiveHint.this.mLayOutAnim = null;
                mFlags &= ~FLAG_OUT_ANIMATING;
                dispatchHidden();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                if ((mFlags & FLAG_SYSTEM_CANCELED) == 0)
                    dismiss(ImmersiveConfig.DismissReason.REASON_DETACHED);
                animation.removeListener(this);
                ImmersiveHint.this.mLayOutAnim = null;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mLayOutAnim.start();
    }

    private void dispatchShown() {
        ImmersiveHintManager.$().processOperateShown(mOperate);
    }

    private void dispatchHidden() {
        ImmersiveHintManager.$().processOperateHidden(mOperate);
        final ViewParent parent = mView.getParent();
        if (parent instanceof ViewGroup) {
            mView.setDetachedListener(null);
            ((ViewGroup) parent).removeOnAttachStateChangeListener(mParentDetachListener);
            ((ViewGroup) parent).removeView(mView);
        }
    }

    private void cancelLayAnim() {
        final Animator layInAnim = this.mLayInAnim;
        this.mLayInAnim = null;
        if (layInAnim != null)
            layInAnim.cancel();
        final Animator layOutAnim = this.mLayOutAnim;
        this.mLayOutAnim = null;
        if (layOutAnim != null)
            layOutAnim.cancel();
        mFlags &= ~(FLAG_IN_ANIMATING | FLAG_OUT_ANIMATING);
        mFlags |= FLAG_IS_FINISHED;
    }
}
