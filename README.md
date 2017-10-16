# OwnHint
各种样式的提示

### ImmersiveHint 沉浸式提示(4.4以下非沉浸式)

* 样式配置
	
	1. 构建自己的HintTypeConfig

	2. 通过<code>ImmersiveHintManager.configure(ImmersiveConfig.Type,HintTypeConfig)</code>
配置

	3. HintTypeConfig参数说明：
	
    <code>iconResId = -1;//图标drawable资源id</code>

    <code>int iconSize = 20;//图标大小</code>

    <code>int iconRightMargin = 10;//图标区域与message区域间隔</code>

    <code>boolean showIcon = false;//默认是否显示icon</code>

    <code>int messageTextColor = 0xFFFFFFFF;//message区域文本颜色</code>

    <code>int messageTextSizeSp = 20;//message区域文本字体大小 sp</code>

    <code>int messageGravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;//message区域文本对齐方式</code>

    <code>int actionTextColor = 0xFFFFFFFF;//Action区域文本颜色</code>

    <code>int actionBackgroundResId = -1;//action区域背景drawable资源id</code>

    <code>int actionTextSizeSp = 20;//action文本字体大小 sp</code>

    <code>int actionPaddingEndsHorizontal = 10;//Action区域左右两端padding</code>

    <code>int actionPaddingEndsVertical = 10;//action区域上下两端padding</code>

    <code>int actionLeftMargin = 100;//action与message的间距</code>

    <code>boolean actionDismiss = false;//点击Action是否消失</code>

    <code>int backgroundColor = 0xFF00FF00;//背景色</code>

    <code>int paddingEndsHorizontal = 10;//左右两端padding</code>

    <code>int paddingEndsVertical = 10;//上下两端padding</code>

    <code>boolean transmissionTouchEvent = false;//是否向下传递点击事件</code>

    <code>long showDuration = 2000L;//停留时间</code>

    <code>long animDuration = 500L;//进入 | 退出 动画时间</code>

    <code>OverallModelSupporter overallModelSupporter;//跨屏模式支持者</code>

	4.当然你也可以在<code>show()</code>之前使用<code>redefineXXX()</code>系列方法临时更改单个提示的配置
 
* 示例
		
		//构建跨屏支持者
		OverallModelSupporter supporter = new HintTypeConfig.OverallModelSupporter() {
			@Override
			public Activity provideTopActivity() {
				return mCurr;
			}
		};

		HintTypeConfig customConfig = new HintTypeConfig().overallModel(supporter);
		ImmersiveHintManager.$()
							.configure(ImmersiveConfig.Type.Hint, customConfig)
							.configure(ImmersiveConfig.Type.Warning, customConfig);


		ImmersiveHint.make(ImmersiveConfig.Type.Hint, this, "this is Low", "action", null)
                        .priority(ImmersiveConfig.Priority.EASY)
                        .withIcon(true)
                        .redefineIconSize(100)
                        .redefineIconDrawable(R.mipmap.ic_launcher_round)
                        .show();
