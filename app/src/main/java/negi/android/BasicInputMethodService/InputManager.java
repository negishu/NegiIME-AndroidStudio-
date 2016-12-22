package negi.android.BasicInputMethodService;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import negi.android.BasicInputMethodService.symbols.ManageEmojiPages;

public class InputManager implements KeyboardListener {

    private static final String TAG = "InputManager";
    private static final int OUT_OF_BOUNDS = -1;
    
    private float mTouchX = OUT_OF_BOUNDS;
    private float mTouchY = OUT_OF_BOUNDS;
    private long  mTouchTime = 0;
    private float mPrevTouchX = OUT_OF_BOUNDS;
    private float mPrevTouchY = OUT_OF_BOUNDS;
    private long  mPrevTouchTime = 0;
    private float mDownTouchX = OUT_OF_BOUNDS;
    private float mDownTouchY = OUT_OF_BOUNDS;
    private long  mDownTouchTime = 0;
    
    private int mViewWidth  = 0;
    private int mViewHeight = 0;
    
    protected ManageKeyboard   mManageKeyboard   = null;
    protected ManageTrace      mManageTrace      = null;
    protected ManageList       mManageList       = null;
    protected ManageEmojiPages mManageEmojiPages = null;
    protected InputView        mParentInputView  = null;

    public InputManager(final InputView aInputView, Context context) {
        mManageKeyboard   = new ManageKeyboard  (context);
        mManageTrace      = new ManageTrace     (context);
        mManageList       = new ManageList      (context);
        mManageEmojiPages = new ManageEmojiPages(context);
        mParentInputView = aInputView;
        mManageKeyboard.setKeyboardListener(this);
    }
    public void SetSuggestions(final ArrayList<String> aSuggestions, boolean completions, boolean typedWordValid) {
        if (mManageList != null) {
            mManageList.SetSuggestions(aSuggestions, completions, typedWordValid);
        }
    }
    public void SetPosAndSize(final int _x, final int _y, final int _w, final int _h) {
        mViewWidth  = _w;
        mViewHeight = _h;
        mManageList.SetPosAndSize      (0,   0, mViewWidth, 180);
        mManageKeyboard.SetPosAndSize  (0, 180, mViewWidth, mViewHeight-180);
        mManageEmojiPages.SetPosAndSize(0,   0, mViewWidth, mViewHeight);
    }
    public boolean onTouchEvent(final View aView, MotionEvent me) {
        
        final int ptrIndex = me.findPointerIndex(0);
        
        if (ptrIndex >= 0) {

            final int nOffsetX = mManageKeyboard.GetCurrentOffsetX();
            final int nOffsetY = mManageKeyboard.GetCurrentOffsetY();

            final float nTouchX    = me.getX(ptrIndex);
            final float nTouchY    = me.getY(ptrIndex);
            final long  nTouchTime = me.getEventTime();
            
            final int   nAction  = me.getActionMasked();
            final float pressure = me.getPressure();
            
            Log.v(TAG, "pressure = " + pressure);
            
            switch (nAction)
            {
                case MotionEvent.ACTION_DOWN:
                    mPrevTouchX    = nTouchX;
                    mPrevTouchY    = nTouchY;            
                    mPrevTouchTime = nTouchTime;
                    mDownTouchX    = nTouchX;
                    mDownTouchY    = nTouchY;            
                    mDownTouchTime = nTouchTime;
                    mTouchX    = nTouchX;
                    mTouchY    = nTouchY;            
                    mTouchTime = nTouchTime;
                break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    setTargetOffsetPos((int)(0), (int)(0), (int)(0), (int)(0), false);
                    mTouchX = OUT_OF_BOUNDS;
                    mTouchY = OUT_OF_BOUNDS;
                break;
                case MotionEvent.ACTION_MOVE:
                    mPrevTouchX = mTouchX;
                    mPrevTouchY = mTouchY;            
                    mPrevTouchTime = mTouchTime;
                    mTouchX = nTouchX;
                    mTouchY = nTouchY;
                    mTouchTime = nTouchTime;

                    int nAbsX = (int) ((mTouchX-mDownTouchX) < 0 ? - (mTouchX-mDownTouchX) : (mTouchX-mDownTouchX));
                    int nAbsY = (int) ((mTouchY-mDownTouchY) < 0 ? - (mTouchY-mDownTouchY) : (mTouchY-mDownTouchY));
                                       
                    if (nOffsetX != 0) {

                        setTargetOffsetPos((int)mTouchX, (int)mTouchY, (int)(mTouchX-mPrevTouchX), (int)(mTouchY-mPrevTouchY), true);
                    }
                    else {

                        setTargetOffsetPos((int)mTouchX, (int)mTouchY, (int)(0), (int)(mTouchY-mPrevTouchY), true);
                    }
                break;
            }
    
            mManageKeyboard.onTouchEvent(aView, nAction, mTouchX, mTouchY, mTouchTime);
            mManageTrace.onTouchEvent(aView, nAction, mTouchX, mTouchY, mTouchTime);
            mManageList.onTouchEvent(aView, nAction, mTouchX, mTouchY, mTouchTime);
            mManageEmojiPages.onTouchEvent(aView, nAction, mTouchX, mTouchY);
        }
        return true;
    }

    private void setTargetOffsetPos(final int nPosX, final int nPosY, final int nDX, final int nDY, final boolean bDown) {
        mManageKeyboard.setTargetOffsetPos  (nPosX, nPosY, nDX, nDY, bDown);
        mManageList.setTargetOffsetPos      (nPosX, nPosY, nDX, nDY, bDown);
        mManageEmojiPages.setTargetOffsetPos(nPosX, nPosY, nDX, nDY, bDown);
    }

    public void DoDraw(final Canvas canvas) {
        mManageKeyboard.Draw(canvas);
        mManageList.Draw(canvas);
        if (mManageKeyboard.IsMoved())
        {
            mManageTrace.Draw(canvas);
        }
        mManageEmojiPages.Draw(canvas);
    }
    @Override
    public void OnKeyDown(Key aKey) {
        if (mParentInputView!=null) {
            mParentInputView.OnKeyDown(aKey);
        }
    }
    @Override
    public void OnKeyUp(Key aKey) {
        if (mParentInputView!=null) {
            if (!(mManageKeyboard.IsMoved())) {
                if (aKey.GetVal() == Key.SYMB_KEY) {
                    setTargetOffsetPos((int)mTouchX, (int)mTouchY, 0, 0, true);
                    setTargetOffsetPos((int)mTouchX, (int)mTouchY, -mViewWidth, 0, false);
                }
                else {
                    mParentInputView.OnKeyUp(aKey);
                }
            }
        }
    }
    @Override
    public void OnKeyRepeat(Key aKey) {
        if (mParentInputView!=null) {
            mParentInputView.OnKeyRepeat(aKey);
        }
    }
    @Override
    public void OnKeyCancel() {
        if (mParentInputView!=null) {
            mParentInputView.OnKeyCancel();
        }
    }
}
