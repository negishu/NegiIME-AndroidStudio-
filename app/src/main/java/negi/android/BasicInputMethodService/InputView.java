package negi.android.BasicInputMethodService;

import java.util.ArrayList;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.graphics.Canvas;

interface InputListener {
    public void OnInputTouch(final int action, final int offsetX, final int offsetY);
    public void OnInputSwipeLeft();
    public void OnInputSwipeRight();
    public void OnInputSwipeUp();
    public void OnInputSwipeDown();
    public void OnInputChar(final String string);
    public void OnInputKeyDown(final int nKey);
    public void OnInputKeyUp(final int nKey);
    public void OnInputKeyRepeat(final int nKey);
};

public class InputView extends negi.android.BaseInputMethodService.InputView implements KeyboardListener {
    private InputListener theInputListener = null;
    private InputManager mInputManager = null;
    @Override
    public void SetSuggestions(final ArrayList<String> aSuggestions, boolean completions, boolean typedWordValid) {
        super.SetSuggestions(aSuggestions, completions, typedWordValid);
        if (mInputManager != null) {
            mInputManager.SetSuggestions(mSuggestions, completions, typedWordValid);
        }
    }
    public InputView(Context context, int nParentSize, int nViewSize) {
        super(context, nParentSize, nViewSize);
        mInputManager = new InputManager(this, context);
    }
    public void SetInputListner(InputListener aInputListener) {
        theInputListener = aInputListener;
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);        
        mInputManager.SetPosAndSize(0, 0, mViewWidth, mViewHeight);
    }
    @Override
    public void draw(final Canvas canvas) {
    }
    @Override
    public boolean onTouchEvent(MotionEvent me) {
        super.onTouchEvent(me);
        mInputManager.onTouchEvent(this, me);
        return true;
    }
    @Override
    protected void DoDraw(final Canvas canvas) {
        super.DoDraw(canvas);
        mInputManager.DoDraw(canvas);
    }    
    @Override
    public void swipeLeft(float deltaX) {
        if (theInputListener != null) {
            theInputListener.OnInputSwipeLeft();
        }
    }
    @Override
    public void swipeRight(float deltaX) {
        if (theInputListener != null) {
            theInputListener.OnInputSwipeRight();
        }
    }
    @Override
    public void swipeUp(float deltaY) {
        if (theInputListener != null) {
            theInputListener.OnInputSwipeUp();
        }
    }
    @Override
    public void swipeDown(float deltaY) {
        if (theInputListener != null) {
            theInputListener.OnInputSwipeDown();
        }
    }
    @Override
    public void OnKeyDown(final Key aKey) {
        if (theInputListener != null) {
            theInputListener.OnInputKeyDown(aKey.GetVal());
        }
    }
    @Override
    public void OnKeyUp(final Key aKey) {
        if (theInputListener != null) {
            theInputListener.OnInputKeyUp(aKey.GetVal());
            if (aKey.GetVal() != Key.BACK_KEY) {
                theInputListener.OnInputChar(String.valueOf(aKey.GetVal()));
            }
        }
    }
    @Override
    public void OnKeyRepeat(final Key aKey) {
        if (theInputListener != null) {
            theInputListener.OnInputKeyRepeat(aKey.GetVal());
        }
    }
    @Override
    public void OnKeyCancel() {
    }
}
