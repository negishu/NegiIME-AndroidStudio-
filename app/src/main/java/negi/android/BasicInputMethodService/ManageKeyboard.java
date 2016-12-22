package negi.android.BasicInputMethodService;

import negi.android.BaseInputMethodService.PreviewView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

interface KeyboardListener {
    public void OnKeyDown   (final Key nKey);
    public void OnKeyUp     (final Key nKey);
    public void OnKeyRepeat (final Key nKey);
    public void OnKeyCancel ();
};

class Key {
    final public static int SYMB_KEY = 0x01;
    final public static int BACK_KEY = 0x08;
    private float X;
    private float Y;
    private int W;
    private int H;
    private char Val;
    private char C;
    private int nRepeatInterval;
    private int nRepeatStartInterval;
    private int nState;
    private int nStateInterval;
    private int nFontSize;
    public float GetX()          { return X;};
    public float GetY()          { return Y;};
    public int   GetW()          { return W;};
    public int   GetH()          { return H;};
    public char  GetC()          { return C;};
    public char  GetVal()        { return Val;};
    public int   GetRepeatInterval()      { return nRepeatInterval;};
    public int   GetRepeatStartInterval() { return nRepeatStartInterval;};
    public int   GetState()               { return nState;};
    public int   GetStateInterval()       { return nStateInterval;};
    private Key() {
    }
    public void Set(float x, float y, int w, int h, char c, char v)
    {
        X = x;
        Y = y;
        W = w;
        H = h;
        C = c;
        Val = v;
        nFontSize            =   54;
        nState               =    0;
        nStateInterval       =  800;
        nRepeatInterval      =   15;
        nRepeatStartInterval =  500;
    }
    public void SetState(final int nstate)
    {
        nState = nstate;
    }
    public final String GetLabel() 
    {
        if (C == Key.BACK_KEY) {
            return "Back";
        }        

        if (C == Key.SYMB_KEY) {          
            return "SYMB";
        }        

        if (GetState() == 0) {

            return String.valueOf(GetC());
        }
        
        return String.valueOf(GetC()).toUpperCase();
    }
    public final int GetLabelSize() 
    {
        if (C == Key.BACK_KEY) {
            return 32;
        }
        if (C == Key.SYMB_KEY) {
            return 28;
        }
        return nFontSize;
    }
    public static Key createKEY(float x, float y, int w, int h, char c, char v) {
        Key akey = new Key();
        akey.Set(x, y, w, h, c, v);
        return akey;
    }
};

public class ManageKeyboard {
    public static final int SIZEOFDATA = 5;
    public final Key[] mKeyDATA[] = new Key[SIZEOFDATA][];
    public final Key[] mCurKeyDATA;
    public static final int OUT_OF_BOUNDS = -1;

    static final int REPEAT_KEY = 1;
    static final int LONG_PRESS_KEY = 2;
  
    public final Key mNoKey = Key.createKEY(0, 0, 0, 0, '\0', '\0');

    private final static int   MAXCOUNT = 64;
    private final static float MAXALPHA = 1024;
        
    private int mPosX   = 0;
    private int mPosY   = 0;
    private int mWidth  = 1;
    private int mHeight = 1;
    
    private final Key mPhysicKey[] = new Key[MAXCOUNT];
    private final PreviewView mPreviewView    [] = new PreviewView[MAXCOUNT];
    private final PopupWindow mKeyPreviewPopup[] = new PopupWindow[MAXCOUNT];
      
    private int mShowKeyPosX []   = new int[MAXCOUNT];
    private int mShowKeyPosY []   = new int[MAXCOUNT];
    private int mShowKeyIndex[]   = new int[MAXCOUNT];
    private float mShowKeyAlpha[] = new float[MAXCOUNT];
    private boolean mShownKey[]   = new boolean[MAXCOUNT];
    
    private int mCurrentKeyIndex = -1;
    private int mPreviousValidKeyIndex = -1;
    private int mCurrentValidKeyIndex = -1;
    
    private float mTouchX = OUT_OF_BOUNDS;
    private float mTouchY = OUT_OF_BOUNDS;
    private long  mTouchTime = 0;

    private float mPrevTouchX = OUT_OF_BOUNDS;
    private float mPrevTouchY = OUT_OF_BOUNDS;
    private long  mPrevTouchTime = 0;
    
    private float mDownTouchX = OUT_OF_BOUNDS;
    private float mDownTouchY = OUT_OF_BOUNDS;
    private long  mDownTouchTime = 0;
    
    private boolean mIsMoved = false;

    private final RectF mRectF;
    private final RectF mClipRect;

    protected DrawKeyboard mDrawKeyboard = null;

    private final Handler theRepeatHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DoRepeatProcess(msg.what);
        }
    };
    
    private final Handler theKeyStateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DoKeyStateProcess(msg.what);
        }
    };
    private final Handler theProcessHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DoProcess();
        }
    };
       
    private KeyboardListener mKeyboardListener = null;

    public static final Key[] mStandardRomajiKeyboardDATA = new Key[]{
        Key.createKEY (  0,  2,  9, 30, 'Q', 'q'),
        Key.createKEY ( 10,  2,  9, 30, 'W', 'w'),
        Key.createKEY ( 20,  2,  9, 30, 'E', 'e'),
        Key.createKEY ( 30,  2,  9, 30, 'R', 'r'),
        Key.createKEY ( 40,  2,  9, 30, 'T', 't'),
        Key.createKEY ( 50,  2,  9, 30, 'Y', 'y'),
        Key.createKEY ( 60,  2,  9, 30, 'U', 'u'),
        Key.createKEY ( 70,  2,  9, 30, 'I', 'i'),
        Key.createKEY ( 80,  2,  9, 30, 'O', 'o'),
        Key.createKEY ( 90,  2,  9, 30, 'P', 'p'),
        Key.createKEY (  0, 35,  9, 30, 'A', 'a'),
        Key.createKEY ( 10, 35,  9, 30, 'S', 's'),
        Key.createKEY ( 20, 35,  9, 30, 'D', 'd'),
        Key.createKEY ( 30, 35,  9, 30, 'F', 'f'),
        Key.createKEY ( 40, 35,  9, 30, 'G', 'g'),
        Key.createKEY ( 50, 35,  9, 30, 'H', 'h'),
        Key.createKEY ( 60, 35,  9, 30, 'J', 'j'),
        Key.createKEY ( 70, 35,  9, 30, 'K', 'k'),
        Key.createKEY ( 80, 35,  9, 30, 'L', 'l'),
        Key.createKEY ( 90, 35,  9, 30, '-', '-'),
        Key.createKEY (  0, 68, 14, 30, (char)Key.SYMB_KEY, (char)Key.SYMB_KEY),
        Key.createKEY ( 15, 68,  9, 30, 'Z', 'z'),
        Key.createKEY ( 25, 68,  9, 30, 'X', 'x'),
        Key.createKEY ( 35, 68,  9, 30, 'C', 'c'),
        Key.createKEY ( 45, 68,  9, 30, 'V', 'v'),
        Key.createKEY ( 55, 68,  9, 30, 'B', 'b'),
        Key.createKEY ( 65, 68,  9, 30, 'N', 'n'),
        Key.createKEY ( 75, 68,  9, 30, 'M', 'm'),
        Key.createKEY ( 85, 68, 14, 30, (char)Key.BACK_KEY, (char)Key.BACK_KEY),
    };
    
    public ManageKeyboard(Context context) {
        for (int n = 0; n < MAXCOUNT; n++) {
            mPreviewView[n]     = new PreviewView(context);
            mKeyPreviewPopup[n] = new PopupWindow(context);
            mKeyPreviewPopup[n].setContentView(mPreviewView[n]);
            mKeyPreviewPopup[n].setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
            mKeyPreviewPopup[n].setTouchable(false);
        }
        mRectF = new RectF();
        mClipRect = new RectF();
        mKeyDATA[0] = mStandardRomajiKeyboardDATA;
        mCurKeyDATA = mKeyDATA[0];
        mDrawKeyboard = new DrawKeyboard (context);
    }
    
    final public int GetKeyCount() { return mCurKeyDATA.length;};
    
    final public Key GetKey      (final int nIndex) { if (0 <= nIndex && nIndex < mCurKeyDATA.length) return mCurKeyDATA[nIndex]; else return mNoKey; };
    final public Key GetLogicKey (final int nIndex) { if (0 <= nIndex && nIndex < mCurKeyDATA.length) return mCurKeyDATA[nIndex]; else return mNoKey; };
    final public Key GetPhysicKey(final int nIndex) { if (0 <= nIndex && nIndex < mPhysicKey.length)  return mPhysicKey [nIndex]; else return mNoKey; };
    
    final public int GetXPos()   { return mPosX;};
    final public int GetYPos()   { return mPosY;};
    final public int GetWidth () { return mWidth;};
    final public int GetHeight() { return mHeight;};
 
    final public boolean IsMoved() { return mIsMoved; };
    
    public void SetPosAndSize(final int _x, final int _y, final int _w, final int _h) {
        mPosX = _x; mPosY = _y; mWidth = _w; mHeight = _h;
        for (int n = 0; n < mCurKeyDATA.length; n++) {
            int x = (int)(mWidth  * (mKeyDATA[0][n].GetX() / 100.0f)) + mPosX;
            int y = (int)(mHeight * (mKeyDATA[0][n].GetY() / 100.0f)) + mPosY;
            int w = (int)(mWidth  * (mKeyDATA[0][n].GetW() / 100.0f));
            int h = (int)(mHeight * (mKeyDATA[0][n].GetH() / 100.0f));
            mPhysicKey[n] = Key.createKEY((float)x, (float)y, w, h, mKeyDATA[0][n].GetC(), mKeyDATA[0][n].GetVal());
        }
        mDrawKeyboard.SetPosAndSize(mPosX, mPosY, mWidth, mHeight);
        mClipRect.set(_x, _y, _w, _h);
    }
    
    public void onTouchEvent(View view, int nAction, float mx, float my, long downTime) {
        switch (nAction)
        {
            case MotionEvent.ACTION_DOWN:
                mTouchX = mx;
                mTouchY = my;
                mTouchTime = downTime;
                mPrevTouchX = mTouchX;
                mPrevTouchY = mTouchY;            
                mPrevTouchTime = downTime;
                mDownTouchX = mTouchX;
                mDownTouchY = mTouchY;            
                mDownTouchTime = downTime;
                mIsMoved = false;
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mCurrentValidKeyIndex >= 0) {
                    if (nAction == MotionEvent.ACTION_UP) {
                        HandleKeyUp(mCurrentValidKeyIndex);
                        theRepeatHandler.removeMessages(mCurrentValidKeyIndex);
                        theKeyStateHandler.removeMessages(mCurrentValidKeyIndex);
                    }
                    else
                    if (nAction == MotionEvent.ACTION_CANCEL) {
                        HandleKeyCancel();
                        theRepeatHandler.removeMessages(mCurrentValidKeyIndex);
                        theKeyStateHandler.removeMessages(mCurrentValidKeyIndex);
                    }
                }
                mTouchX = -1;
                mTouchY = -1;
                mCurrentValidKeyIndex  = -1;
                mPreviousValidKeyIndex = -1;
                mIsMoved = false;
            break;
            case MotionEvent.ACTION_MOVE:
                mPrevTouchX = mTouchX;
                mPrevTouchY = mTouchY;            
                mPrevTouchTime = mTouchTime;
                mTouchX = mx;
                mTouchY = my;
                mTouchTime = downTime;
                
                mIsMoved = mIsMoved ? mIsMoved : ( mPreviousValidKeyIndex == -1 ? false : (mCurrentValidKeyIndex != mPreviousValidKeyIndex));
            break;
        }
        
        boolean bKeyFound = false;
        
        final int nOffsetX = mDrawKeyboard.GetCurrentOffsetX();
        final int nOffsetY = mDrawKeyboard.GetCurrentOffsetY();
        
        mPreviousValidKeyIndex = mCurrentKeyIndex;
        
        for (int n = 0; n < mCurKeyDATA.length; n++) {
            float x = GetPhysicKey(n).GetX() + nOffsetX;
            float y = GetPhysicKey(n).GetY();
            float w = GetPhysicKey(n).GetW();
            float h = GetPhysicKey(n).GetH();
            mRectF.set(x, y, x + w, y + h);
            if (mRectF.contains((int)mx, (int)my)) {
                mShowKeyPosX [n] = (int) x;
                mShowKeyPosY [n] = (int) y;
                mShowKeyIndex[n] = n;
                mCurrentKeyIndex = n;
                mCurrentValidKeyIndex = n;
                bKeyFound = true;
                break;
            }
            else {
                mPhysicKey[n].SetState(0);
                theKeyStateHandler.removeMessages(n);
            }
        }
        
        
        if (!bKeyFound) {            
            mCurrentKeyIndex = -1;
        }
        
        if (mCurrentKeyIndex >= 0) {
            if (nAction == MotionEvent.ACTION_DOWN) {
                HandleKeyDown(mCurrentValidKeyIndex);
                theRepeatHandler.removeMessages(mCurrentValidKeyIndex);
                theKeyStateHandler.removeMessages(mCurrentValidKeyIndex);
                SendRepeatMessage(mCurrentKeyIndex, mPhysicKey[mCurrentValidKeyIndex].GetRepeatStartInterval());
                SendKeyStateMessage(mCurrentKeyIndex, mPhysicKey[mCurrentValidKeyIndex].GetStateInterval());
                mIsMoved = IsMoved();
            }
                   
            if (mShownKey[mCurrentKeyIndex] == true) {
                if (!IsMoved()) {
                    mShowKeyAlpha[mCurrentKeyIndex] = MAXALPHA;
                    mPreviewView[mShowKeyIndex[mCurrentKeyIndex]].setAlpha(mShowKeyAlpha[mCurrentKeyIndex]/MAXALPHA);
                    mPreviewView[mShowKeyIndex[mCurrentKeyIndex]].setInfo(GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetLabel(),
                                                                          GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetLabelSize(),
                                                                          GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetW(),
                                                                          GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetH());
                    mKeyPreviewPopup[mShowKeyIndex[mCurrentKeyIndex]].update((int)(mShowKeyPosX[mCurrentKeyIndex] - GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetW()/2), (int)(mShowKeyPosY[mCurrentKeyIndex] - 256), -1, -1, true);
                }
            }
            else {

                if (!IsMoved()) {
                    mShownKey[mCurrentKeyIndex] = true;               
                    mShowKeyAlpha[mCurrentKeyIndex] = MAXALPHA;
                    mPreviewView[mShowKeyIndex[mCurrentKeyIndex]].setAlpha(mShowKeyAlpha[mCurrentKeyIndex]/MAXALPHA);
                    mPreviewView[mShowKeyIndex[mCurrentKeyIndex]].setInfo(GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetLabel(),
                                                                          GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetLabelSize(),
                                                                          GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetW(),
                                                                          GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetH());
                    mKeyPreviewPopup[mShowKeyIndex[mCurrentKeyIndex]].setWidth(GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetW());
                    mKeyPreviewPopup[mShowKeyIndex[mCurrentKeyIndex]].setHeight(GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetH());
                    mKeyPreviewPopup[mShowKeyIndex[mCurrentKeyIndex]].showAtLocation(view, Gravity.NO_GRAVITY, (int)(mShowKeyPosX[mCurrentKeyIndex] - GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetW()/2), (int)(mShowKeyPosY[mCurrentKeyIndex] - 256));
                }
            }
        }
        DoProcess();
    }
    public void Draw(final Canvas canvas) {
        mDrawKeyboard.Draw(canvas, this, mTouchX, mTouchY);
    }

    private void SendRepeatMessage(final int keyCode, int milsec) {
        Message newMessage = theRepeatHandler.obtainMessage(keyCode, 0, 0, null);
        theRepeatHandler.sendMessageDelayed(newMessage, milsec);
    }

    private void SendKeyStateMessage(final int keyCode, int milsec) {
        Message newMessage = theKeyStateHandler.obtainMessage(keyCode, 0, 0, null);
        theKeyStateHandler.sendMessageDelayed(newMessage, milsec);
    }
    
    private void DoProcess() {
        boolean bAgain = false;
        for (int n = 0; n < mCurKeyDATA.length; n++) {
            if (n == mCurrentKeyIndex) {
                continue;
            }            
            if (mShowKeyAlpha[n] > 12.0) {
                mPreviewView[n].setAlpha(mShowKeyAlpha[n]/MAXALPHA);                
                mShowKeyAlpha[n]*=0.95;
                bAgain = true;
            }
            else {
                mShowKeyAlpha[n] = 0;
                mKeyPreviewPopup[n].dismiss();
                mShownKey[n] = false;
            }
        }
        if (bAgain) {
            theProcessHandler.sendEmptyMessageDelayed(0, 25);
        }
    }

    public void DoRepeatProcess(final int msg) {
        if (mCurrentValidKeyIndex == msg) {
            HandleKeyRepeat(mCurrentValidKeyIndex);
            SendRepeatMessage(mCurrentKeyIndex, mPhysicKey[mCurrentValidKeyIndex].GetRepeatInterval());
        }
    }
    
    public void DoKeyStateProcess(final int msg) {
        if (mCurrentValidKeyIndex == msg) {
            mPhysicKey[mCurrentValidKeyIndex].SetState(1);
            mPreviewView[mShowKeyIndex[mCurrentKeyIndex]].setInfo(GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetLabel(),
                                                                  GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetLabelSize(),
                                                                  GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetW(),
                                                                  GetPhysicKey(mShowKeyIndex[mCurrentKeyIndex]).GetH());
            mPreviewView[mShowKeyIndex[mCurrentKeyIndex]].invalidate();
        }
    }

    public void setKeyboardListener(final KeyboardListener aKeyboardListener) {
        mKeyboardListener = aKeyboardListener;
    }
    
    public void setTargetOffsetPos(final int nPosX, final int nPosY, final int nDX, final int nDY, final boolean bDown) {
        if (mClipRect.contains(nPosX, nPosY)) {
            mDrawKeyboard.setTargetOffsetPos(nDX, nDY, bDown);
        }
        else {
            mDrawKeyboard.setTargetOffsetPos(nDX, nDY, bDown);            
        }
    }

    public void resetTargetOffsetPos() {
        mDrawKeyboard.resetTargetOffsetPos();
    }

    private void HandleKeyDown(final int nKey) {
        final Key aKey = GetKey (nKey);        
        if (aKey != mNoKey) {
            mKeyboardListener.OnKeyDown(aKey);        
        }
    }

    private void HandleKeyUp(final int nKey) {
        final Key aKey = GetKey (nKey);        
        if (aKey != mNoKey) {
            mKeyboardListener.OnKeyUp(aKey);
        }
    }

    private void HandleKeyRepeat(final int nKey) {
        final Key aKey = GetKey (nKey);        
        if (aKey != mNoKey) {
            mKeyboardListener.OnKeyRepeat(aKey);        
        }
    }

    private void HandleKeyCancel() {
        mKeyboardListener.OnKeyCancel();        
    }

    public int GetCurrentOffsetX() {
        return mDrawKeyboard.GetCurrentOffsetX();
    }

    public int GetCurrentOffsetY() {
        return mDrawKeyboard.GetCurrentOffsetY();
    }
};
