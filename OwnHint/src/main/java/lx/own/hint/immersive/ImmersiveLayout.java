package lx.own.hint.immersive;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
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

final public class ImmersiveLayout extends LinearLayout implements View.OnClickListener {

    private OnLayoutChangedListener mLayoutChangedListener;
    private OnDetachedListener mDetachedListener;
    private HintAction mAction;
    private ImageView mIconView;
    private TextView mMessageView, mActionView;

    public void setOnLayoutChangedListener(OnLayoutChangedListener listener) {
        this.mLayoutChangedListener = listener;
    }

    public void setDetachedListener(OnDetachedListener listener) {
        this.mDetachedListener = listener;
    }

    public ImmersiveLayout(Context context) {
        this(context, null);
    }

    public ImmersiveLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ImmersiveLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        buildContent(context, attrs, defStyleAttr);
    }

    private void buildContent(Context context, AttributeSet attrs, int defStyleAttr) {
        final int paddingEndsHorizontal = ImmersiveHintConfig.Params.paddingEndsHorizontal;
        final int paddingEndsVertical = ImmersiveHintConfig.Params.paddingEndsVertical;
        setPadding(paddingEndsHorizontal, paddingEndsVertical, paddingEndsHorizontal, paddingEndsVertical);
        mIconView = buildIconView(context, attrs, defStyleAttr);
        mMessageView = buildMessageView(context, attrs, defStyleAttr);
        mActionView = buildActionView(context, attrs, defStyleAttr);
        removeAllViews();
        addView(mIconView);
        addView(mMessageView);
        addView(mActionView);
        ViewCompat.setAccessibilityLiveRegion(this,
                ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
        ViewCompat.setImportantForAccessibility(this,
                ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    private ImageView buildIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        ImageView iconView = new ImageView(context);
        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        final int iconSize = ImmersiveHintConfig.Params.iconSize;
        final int drawableId = ImmersiveHintConfig.Params.iconResId;
        if (drawableId != -1) {
            iconView.setImageResource(drawableId);
        } else {
            iconView.setVisibility(GONE);
        }
        LayoutParams messageParams = new LayoutParams(iconSize, iconSize);
        iconView.setLayoutParams(messageParams);
        return iconView;
    }

    private TextView buildMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        TextView textView = new TextView(context);
        final int messageTextColor = ImmersiveHintConfig.Params.messageTextColor;
        final int messageTextSize = ImmersiveHintConfig.Params.messageTextSize;
        LayoutParams messageParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        messageParams.weight = 1;
        textView.setLayoutParams(messageParams);

        textView.setTextSize(messageTextSize);
        textView.setTextColor(messageTextColor);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        return textView;
    }

    private TextView buildActionView(Context context, AttributeSet attrs, int defStyleAttr) {
        TextView textView = new TextView(context);
        final int actionTextSize = ImmersiveHintConfig.Params.actionTextSize;
        final int actionTextColor = ImmersiveHintConfig.Params.actionTextColor;
        final int actionTextPaddingEnds = ImmersiveHintConfig.Params.actionPaddingEndsHorizontal;
        final int actionTextBackgroundResId = ImmersiveHintConfig.Params.actionBackgroundResId;
        LayoutParams messageParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        textView.setLayoutParams(messageParams);

        textView.setTextSize(actionTextSize);
        textView.setTextColor(actionTextColor);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setPadding(actionTextPaddingEnds, 0, actionTextPaddingEnds, 0);
        if (actionTextBackgroundResId != -1)
            textView.setBackgroundResource(actionTextBackgroundResId);
        return textView;
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

    void adaptContent(@NonNull ImmersiveHintConfig.Type type, @Nullable String message, @Nullable String actionText, @Nullable HintAction action) {
        int backgroundColor = 0;
        switch (type) {
            case Hint:
                backgroundColor = ImmersiveHintConfig.Params.hintBackgroundColor;
                break;
            case Warning:
                backgroundColor = ImmersiveHintConfig.Params.warningBackgroundColor;
                break;
            default:
                backgroundColor = ImmersiveHintConfig.Params.hintBackgroundColor;
                break;
        }
        setBackgroundColor(backgroundColor);
        mMessageView.setText(message);
        mAction = action;
        if (TextUtils.isEmpty(actionText)) {
            mActionView.setVisibility(View.GONE);
            mActionView.setOnClickListener(null);
        } else {
            mActionView.setVisibility(View.VISIBLE);
            mActionView.setText(actionText);
            mActionView.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mActionView && mAction != null)
            mAction.onAction();
    }

    interface OnLayoutChangedListener {
        void onLayoutChanged(View view, int l, int t, int r, int b);
    }

    interface OnDetachedListener {
        void onDetachedFromWindow(View view);
    }
}
