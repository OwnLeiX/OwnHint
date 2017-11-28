package lx.own.hint.immersive;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * <b> </b><br/>
 *
 * @author LeiXun
 *         Created on 2017/10/11.
 */

final public class ImmersiveLayout extends LinearLayout {

    private OnLayoutChangedListener mLayoutChangedListener;
    private OnDetachedListener mDetachedListener;
    private OnUpglideListener mOnUpglideListener;
    ImageView mIconView;
    TextView mMessageView, mActionView;
    private float downX;
    private float downY;

    private boolean isTouching;

    public ImmersiveLayout(Context context) {
        this(context, null);
    }

    public ImmersiveLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ImmersiveLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && mLayoutChangedListener != null)
            mLayoutChangedListener.onLayoutChanged(this, l, t, r, b);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDetachedListener != null)
            mDetachedListener.onDetachedFromWindow(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            isTouching = true;
            return true;
        } else if (action == MotionEvent.ACTION_MOVE && isTouching) {
            float currX = event.getX();
            float currY = event.getY();
            if (Math.abs(downX - currX) > 20) {
                isTouching = false;
            } else if (downY - currY > 30) {
                OnUpglideListener upglideListener = this.mOnUpglideListener;
                if (upglideListener != null)
                    upglideListener.onUpglide();
                isTouching = false;
            } else {
                return true;
            }
        } else {
            isTouching = false;
        }
        return super.onTouchEvent(event);
    }

    void adaptContent(@NonNull ImmersiveConfig.Type type, @Nullable String message, @Nullable String actionText, @Nullable OnClickListener actionListener) {
        final Context context = getContext();
        final HintTypeConfig config = type.config;
        final int paddingEndsHorizontal = config.paddingEndsHorizontal;
        final int paddingEndsVertical = config.paddingEndsVertical;
        final boolean transmissionTouchEvent = config.transmissionTouchEvent;
        final boolean hasAction = !TextUtils.isEmpty(actionText);
        setPadding(paddingEndsHorizontal, paddingEndsVertical, paddingEndsHorizontal, paddingEndsVertical);
        setClickable(!transmissionTouchEvent);
        mIconView = buildIconView(context, config);
        mMessageView = buildMessageView(context, config);
        mActionView = buildActionView(context, config);
        removeAllViews();
        addView(mIconView);
        addView(mMessageView);
        addView(mActionView);
        ViewCompat.setAccessibilityLiveRegion(this,
                ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
        ViewCompat.setImportantForAccessibility(this,
                ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
        setBackgroundColor(config.backgroundColor);
        mMessageView.setText(message);
        mActionView.setVisibility(hasAction ? View.VISIBLE : View.GONE);
        if (hasAction) {
            mActionView.setText(actionText);
            mActionView.setOnClickListener(actionListener);
        }
    }

    void setOnLayoutChangedListener(OnLayoutChangedListener listener) {
        this.mLayoutChangedListener = listener;
    }

    void setDetachedListener(OnDetachedListener listener) {
        this.mDetachedListener = listener;
    }

    public void setOnUpglideListener(OnUpglideListener mOnUpglideListener) {
        this.mOnUpglideListener = mOnUpglideListener;
    }

    private ImageView buildIconView(Context context, HintTypeConfig config) {
        ImageView iconView = new ImageView(context);
        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        final int iconSize = config.iconSize;
        final int drawableId = config.iconResId;
        final int iconRightMargin = config.iconRightMargin;
        final boolean showIcon = config.showIcon;
        if (drawableId != -1)
            iconView.setImageResource(drawableId);
        iconView.setVisibility(showIcon ? VISIBLE : GONE);
        LayoutParams params = new LayoutParams(iconSize, iconSize);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.rightMargin = iconRightMargin;
        iconView.setLayoutParams(params);
        return iconView;
    }

    private TextView buildMessageView(Context context, HintTypeConfig config) {
        TextView textView = new TextView(context);
        final int messageTextColor = config.messageTextColor;
        final int messageTextSize = config.messageTextSizeSp;
        LayoutParams params = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.weight = 1;
        textView.setLayoutParams(params);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, messageTextSize);
        textView.setTextColor(messageTextColor);
        textView.setGravity(config.messageGravity);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        return textView;
    }

    private TextView buildActionView(Context context, HintTypeConfig config) {
        TextView textView = new TextView(context);
        final int actionTextSize = config.actionTextSizeSp;
        final int actionTextColor = config.actionTextColor;
        final int actionTextPaddingEndsHorizontal = config.actionPaddingEndsHorizontal;
        final int actionTextPaddingEndsVertical = config.actionPaddingEndsVertical;
        final int actionTextBackgroundResId = config.actionBackgroundResId;
        final int actionLeftMargin = config.actionLeftMargin;
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.leftMargin = actionLeftMargin;
        textView.setLayoutParams(params);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, actionTextSize);
        textView.setTextColor(actionTextColor);
        textView.setGravity(Gravity.CENTER);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setPadding(actionTextPaddingEndsHorizontal, actionTextPaddingEndsVertical, actionTextPaddingEndsHorizontal, actionTextPaddingEndsVertical);
        if (actionTextBackgroundResId != -1)
            textView.setBackgroundResource(actionTextBackgroundResId);
        return textView;
    }

    interface OnLayoutChangedListener {
        void onLayoutChanged(View view, int l, int t, int r, int b);
    }

    interface OnDetachedListener {
        void onDetachedFromWindow(View view);
    }

    interface OnUpglideListener {
        void onUpglide();
    }
}
