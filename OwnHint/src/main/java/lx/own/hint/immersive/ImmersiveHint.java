package lx.own.hint.immersive;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

import lx.own.hint.R;

/**
 * <p>沉浸式提示</p><br/>
 *
 * @author Lx
 *         Create on 10/10/2017.
 */

final public class ImmersiveHint {
    private static volatile int mStatusHeight = -1;
    private static volatile WeakReference<ViewGroup> mFanciedParent;
    private Animation mLayOutAnim;
    private Animation mLayInAnim;

    @IntDef({ImmersiveHintConfig.Priority.HIGH, ImmersiveHintConfig.Priority.NORMAL, ImmersiveHintConfig.Priority.LOW})
    public @interface HintPriority {
    }

    int mPriority = ImmersiveHintConfig.Priority.NORMAL;
    private final ImmersiveHintConfig.Type mType;
    private WeakReference<ViewGroup> mParent;
    private WeakReference<Activity> mActivity;
    private ImmersiveLayout mView;
    private final ImmersiveHintManager.OperateInterface mOperate = new ImmersiveHintManager.OperateInterface() {
        @Override
        public void show() {
            beginTransition();
        }

        @Override
        public void dismiss(int reason) {
            endTransition(reason);
        }
    };
    private final View.OnAttachStateChangeListener mParentDetachListener = new View.OnAttachStateChangeListener() {
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
                        dismiss(ImmersiveHintConfig.DismissReason.REASON_DETACHED);
                    }
                });
            }
            if (mParent.get() == v)
                mParent.clear();
        }
    };
    private final ImmersiveLayout.OnDetachedListener mViewDetachListener = new ImmersiveLayout.OnDetachedListener() {
        @Override
        public void onDetachedFromWindow(View view) {
            if (mView.getParent() != null) {
                mView.setDetachedListener(null);
                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelLayAnim();
                        dismiss(ImmersiveHintConfig.DismissReason.REASON_DETACHED);
                    }
                });
            }
        }
    };

    public static ImmersiveHint make(@NonNull ImmersiveHintConfig.Type type, @NonNull Activity activity, @StringRes int messageRes) {
        return make(type, activity, messageRes, -1, null);
    }

    public static ImmersiveHint make(@NonNull ImmersiveHintConfig.Type type, @NonNull Activity activity, @NonNull String message) {
        return make(type, activity, message, "", null);
    }

    public static ImmersiveHint make(@NonNull ImmersiveHintConfig.Type type, @NonNull Activity activity, @StringRes int messageRes, @StringRes int actionRes, HintAction action) {
        Resources resources = activity.getResources();
        return new ImmersiveHint(type, activity, resources.getString(messageRes), actionRes == -1 ? "" : resources.getString(actionRes), action);
    }

    public static ImmersiveHint make(@NonNull ImmersiveHintConfig.Type type, @NonNull Activity activity, @NonNull String message, @Nullable String actionText, HintAction action) {
        return new ImmersiveHint(type, activity, message, actionText, action);
    }

    private static void viewHeightCompat(View view) {
        int statusBarHeight = getStatusBarHeight(view.getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            view.setPadding(view.getPaddingLeft()
                    , view.getPaddingTop() + statusBarHeight, view.getPaddingRight(), view.getPaddingBottom());
            view.setMinimumHeight(statusBarHeight);
        } else {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams != null && layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams) layoutParams).topMargin += statusBarHeight;
            }
        }
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
                    mFanciedParent = new WeakReference<ViewGroup>(null);
            }
        }
        return mFanciedParent;
    }

    private ImmersiveHint(@NonNull ImmersiveHintConfig.Type type, @NonNull Activity activity, @NonNull String message, @Nullable String actionText, HintAction action) {
        this.mType = type;
        this.mActivity = new WeakReference<Activity>(activity);
        buildViews(activity, message, actionText, type, action);
    }

    public void show() {
        show(mType.config.showDuration);
    }

    private void show(final long duration) {
        ImmersiveHintManager.$().enqueue(mOperate, duration, mPriority);
    }

    public void dismiss() {
        dismiss(ImmersiveHintConfig.DismissReason.REASON_CODES);
    }

    private void dismiss(final int reason) {
        ImmersiveHintManager.$().cancel(mOperate, reason);
    }

    public ImmersiveHint withIcon(boolean show) {
        mView.mIconView.setVisibility(show ? View.VISIBLE : View.GONE);
        return this;
    }

    public ImmersiveHint priority(@HintPriority int priority) {
        this.mPriority = priority;
        return this;
    }

    public ImmersiveHint redefineIconDrawable(@DrawableRes int resId) {
        mView.mIconView.setImageResource(resId);
        return this;
    }

    public ImmersiveHint redefineIconSize(int radius) {
        ViewGroup.LayoutParams params = mView.mIconView.getLayoutParams();
        params.width = radius;
        params.height = radius;
        mView.mIconView.setLayoutParams(params);
        return this;
    }

    public ImmersiveHint redefineBackgroundColor(@ColorInt int color) {
        mView.setBackgroundColor(color);
        return this;
    }

    public ImmersiveHint redefineBackgroundDrawable(@DrawableRes int resId) {
        mView.setBackgroundResource(resId);
        return this;
    }

    public ImmersiveHint redefineMessageTextSize(int size) {
        mView.mMessageView.setTextSize(size);
        return this;
    }

    public ImmersiveHint redefineMessageTextColor(@ColorInt int color) {
        mView.mMessageView.setTextColor(color);
        return this;
    }

    public ImmersiveHint redefineActionTextSize(int size) {
        mView.mActionView.setTextSize(size);
        return this;
    }

    public ImmersiveHint redefineActionTextColor(@ColorInt int color) {
        mView.mActionView.setTextColor(color);
        return this;
    }

    public ImmersiveHint redefineActionBackgroundDrawable(@DrawableRes int resId) {
        mView.mActionView.setBackgroundResource(resId);
        return this;
    }

    private void buildViews(Activity activity, String message, String actionText, ImmersiveHintConfig.Type type, HintAction action) {
        final ViewGroup parent = findSuitableParent(activity.getWindow().getDecorView());
        if (parent != null && ViewCompat.isAttachedToWindow(parent)) {
            parent.addOnAttachStateChangeListener(mParentDetachListener);
            mParent = new WeakReference<ViewGroup>(parent);
        } else {
            mParent = buildFanciedParent();//This just makes mParent != null;
        }
        mView = (ImmersiveLayout) activity.getLayoutInflater().inflate(R.layout.immersive_layout, parent, false);
        mView.adaptContent(type, message, actionText, action);
        mView.setDetachedListener(mViewDetachListener);
        viewHeightCompat(mView);
    }

    private void beginTransition() {
        ViewGroup parent = mParent.get();
        if (parent == null || !ImmersiveHintManager.$().isActivityRunning(this.mActivity.get())) {
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
        if (reason == ImmersiveHintConfig.DismissReason.REASON_DETACHED
                || mView.getVisibility() != View.VISIBLE
                || !ImmersiveHintManager.$().isActivityRunning(this.mActivity.get())) {
            dispatchHidden();
        } else {
            animateOut(reason);
        }
    }

    private void inspectOverallModel() {
        final CustomConfig.OverallModelSupporter supporter = mType.config.overallModelSupporter;
        if (supporter != null) {
            final Activity act = supporter.supportNewActivity();
            if (act != null && !act.isFinishing()) {
                final ViewGroup parent = findSuitableParent(act.getWindow().getDecorView());
                if (parent != null && ViewCompat.isAttachedToWindow(parent)) {
                    parent.addOnAttachStateChangeListener(mParentDetachListener);
                    mActivity = new WeakReference<Activity>(act);
                    mParent = new WeakReference<ViewGroup>(parent);
                    beginTransition();
                    return;
                }
            }
        }
        dispatchHidden();
    }

    private void animateIn() {
        mLayInAnim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0f);
        mLayInAnim.setDuration(mType.config.animDuration);
        mLayInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mLayInAnim = null;
                dispatchShown();
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mView.startAnimation(mLayInAnim);
    }

    private void animateOut(int reason) {

        mLayOutAnim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1.0f);
        mLayOutAnim.setDuration(mType.config.animDuration);
        mLayOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mLayOutAnim = null;
                dispatchHidden();
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mView.startAnimation(mLayOutAnim);
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
        final Animation layInAnim = this.mLayInAnim;
        this.mLayInAnim = null;
        if (layInAnim != null)
            layInAnim.cancel();
        final Animation layOutAnim = this.mLayOutAnim;
        this.mLayOutAnim = null;
        if (layOutAnim != null)
            layOutAnim.cancel();
    }
}
