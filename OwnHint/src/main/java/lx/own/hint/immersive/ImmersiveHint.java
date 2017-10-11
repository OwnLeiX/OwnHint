package lx.own.hint.immersive;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
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
import android.widget.TextView;

import java.lang.ref.WeakReference;

import lx.own.hint.R;

/**
 * <p>沉浸式提示</p><br/>
 *
 * @author Lx
 *         Create on 10/10/2017.
 */

public class ImmersiveHint {
    private static volatile int mStatusHeight = -1;

    private WeakReference<ViewGroup> mParent;
    private ImmersiveLayout mView;
    private TextView mMessageView, mActionView;
    private HintAction mAction;
    private final ImmersiveHintManager.OperateInterface mOperate = new ImmersiveHintManager.OperateInterface() {
        @Override
        public void show() {
            beginTransition();
        }

        @Override
        public void dismiss(int reason) {
            endTransition();
        }
    };

    public static ImmersiveHint make(@NonNull Activity activity, @NonNull String message, ImmersiveHintConfig.Type type) {
        return make(activity, message, "", type, null);
    }

    public static ImmersiveHint make(@NonNull Activity activity, @StringRes int messageRes, ImmersiveHintConfig.Type type) {
        return make(activity, messageRes, -1, type, null);
    }

    public static ImmersiveHint make(@NonNull Activity activity, @StringRes int messageRes, @StringRes int actionRes, ImmersiveHintConfig.Type type, HintAction action) {
        Resources resources = activity.getResources();
        return new ImmersiveHint(activity, resources.getString(messageRes), actionRes == -1 ? "" : resources.getString(actionRes), type, action);
    }

    public static ImmersiveHint make(@NonNull Activity activity, @NonNull String message, @Nullable String actionText, ImmersiveHintConfig.Type type, HintAction action) {
        return new ImmersiveHint(activity, message, actionText, type, action);
    }

    private static void supportHeight(View view) {
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

    public void show() {
        show(ImmersiveHintConfig.Params.duration);
    }

    private void show(long duration) {
        ImmersiveHintManager.$().enqueue(mOperate, duration);
    }

    public void dismiss() {
        dismiss(ImmersiveHintManager.REASON_CODES);
    }

    private void dismiss(int reason) {
        ImmersiveHintManager.$().dismiss(mOperate, reason);
    }

    private ImmersiveHint(@NonNull Activity activity, @NonNull String message, @Nullable String actionText, ImmersiveHintConfig.Type type, HintAction action) {
        buildViews(activity, message, actionText, type, action);
    }

    private void buildViews(Activity activity, String message, String actionText, ImmersiveHintConfig.Type type, HintAction action) {
        ViewGroup parent = findSuitableParent(activity.getWindow().getDecorView());
        mParent = new WeakReference<ViewGroup>(parent);
        mView = (ImmersiveLayout) activity.getLayoutInflater().inflate(R.layout.immersive_layout, parent, false);
        supportHeight(mView);
        mView.adaptContent(type, message, actionText, action);
    }

    private void beginTransition() {
        ViewGroup parent = mParent.get();
        if (parent == null)
            return;
        if (mView.getParent() == null)
            parent.addView(mView);
        if (ViewCompat.isLaidOut(mView)) {
            animateViewIn();
        } else {
            mView.setOnLayoutChangedListener(new ImmersiveLayout.OnLayoutChangedListener() {
                @Override
                public void onLayoutChanged(View view, int left, int top, int right, int bottom) {
                    animateViewIn();
                    mView.setOnLayoutChangedListener(null);
                }
            });
        }
    }

    private void endTransition() {
        animateViewOut();
    }

    private void animateViewIn() {
        Animation anim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0f);
        anim.setDuration(ImmersiveHintConfig.Params.animDuration);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                onShown();
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mView.startAnimation(anim);
    }

    private void animateViewOut() {
        Animation anim = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, -1.0f);
        anim.setDuration(ImmersiveHintConfig.Params.animDuration);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                onHidden();
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        mView.startAnimation(anim);
    }

    private void onShown() {
        ImmersiveHintManager.$().onShown(mOperate);
    }

    private void onHidden() {
        ImmersiveHintManager.$().onDismissed(mOperate);
        final ViewParent parent = mView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(mView);
        }
    }
}
