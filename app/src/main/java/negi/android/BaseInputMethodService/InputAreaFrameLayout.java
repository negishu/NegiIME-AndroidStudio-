package negi.android.BaseInputMethodService;

import negi.android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class InputAreaFrameLayout extends FrameLayout {
	
	public InputAreaFrameLayout(Context context) {
		super(context);
	}

	public InputAreaFrameLayout(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.InputAreaFrameLayoutStyle);
	}

	public InputAreaFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void draw(final Canvas canvas) {
	}
}
