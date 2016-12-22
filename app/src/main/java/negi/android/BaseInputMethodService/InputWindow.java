package negi.android.BaseInputMethodService;

import android.app.Dialog;
import android.content.Context;
import android.os.IBinder;
import android.view.Gravity;
import android.view.WindowManager;

public class InputWindow extends Dialog {
	public InputWindow(Context context, int theme) {
		super(context, theme);
		initDockWindow();
	}
	public void setToken(IBinder token) {
		final WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.token  = token;
		getWindow().setAttributes(lp);
	}
	public int getSize() {
		final WindowManager.LayoutParams lp = getWindow().getAttributes();
		if (lp.gravity == Gravity.TOP || lp.gravity == Gravity.BOTTOM) {
			return lp.height;
		} else {
			return lp.width;
		}
	}
	public void setSize(int size) {
		final WindowManager.LayoutParams lp = getWindow().getAttributes();
		if (lp.gravity == Gravity.TOP || lp.gravity == Gravity.BOTTOM) {
			lp.width = -1;
			lp.height = size;
		} else {
			lp.width = size;
			lp.height = -1;
		}
		getWindow().setAttributes(lp);
	}
	public void setGravity(int gravity) {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		final boolean oldIsVertical = (lp.gravity == Gravity.TOP || lp.gravity == Gravity.BOTTOM);
		final boolean newIsVertical;
		lp.gravity = gravity;
		newIsVertical = (lp.gravity == Gravity.TOP || lp.gravity == Gravity.BOTTOM);
		if (oldIsVertical != newIsVertical) {
			final int tmp = lp.width;
			lp.width = lp.height;
			lp.height = tmp;
	        getWindow().setAttributes(lp);
		}
	}

	private void initDockWindow() {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.type = WindowManager.LayoutParams.TYPE_INPUT_METHOD;
		lp.setTitle("InputMethod");
		lp.gravity = Gravity.BOTTOM;
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE ,
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(lp);
	}
}
