package negi.android.BaseInputMethodService;

import android.content.Context;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;

public class DrawBase {
    
    public static final int TOTALPAGESIZE = 6;
    public static final int OUT_OF_BOUNDS = -1;

    protected final RectF mRectF;
    protected final RectF mClipRectF;

    protected final Context mContext;
            
    protected int mPosX   = 0;
    protected int mPosY   = 0;
    protected int mWidth  = 1;
    protected int mHeight = 1;

    protected int mCurrentOffsetX = 0;
    protected int mCurrentOffsetY = 0;
    protected int mTargetOffsetX  = 0;
    protected int mTargetOffsetY  = 0;
    protected int mBeginOffsetX   = 0;
    protected int mBeginOffsetY   = 0;
    protected int mTotalHeight    = 0;
    
    protected int mScrollVal      = 8;

    protected boolean mbDownTouch = false;
    
    private final Handler theRepeatHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DoRepeatProcess(msg.what);
        }
    };

    public final int GetCurrentOffsetX() { return mCurrentOffsetX; };
    public final int GetCurrentOffsetY() { return mCurrentOffsetY; };

    public DrawBase(Context context) {
        mContext   = context;
        mRectF     = new RectF();
        mClipRectF = new RectF();
    }
    
    public void SetPosAndSize(final int x, final int y, final int w, final int h) {
        mPosX = x; mPosY = y; mWidth = w; mHeight = h;
        mClipRectF.set(mPosX, mPosY, mPosX + mWidth, mPosY + mHeight);
    }

    public void setTargetOffsetPos(final int nPosX, final int nPosY, final boolean bDown) {
        int nAbsX = nPosX < 0 ? - nPosX : nPosX;
        int nAbsY = nPosY < 0 ? - nPosY : nPosY;
        int _nPosX = nPosX;
        int _nPosY = nPosY;
        if (nAbsX < nAbsY) _nPosX = 0;
        if (nAbsX > nAbsY) _nPosY = 0;
        if (mbDownTouch == false) {
            if (bDown == true) {
                mBeginOffsetX = mCurrentOffsetX;                              
                mBeginOffsetY = mCurrentOffsetY;
            }            
        }
        mTargetOffsetX = mTargetOffsetX + _nPosX;
        mTargetOffsetY = mTargetOffsetY + _nPosY;
        mbDownTouch = bDown;
        DoRepeatMessage();
    }

    public void resetTargetOffsetPos() {
        mTargetOffsetX = 0;
        mTargetOffsetY = 0;
        DoRepeatMessage();
    }

    protected void DoRepeatMessage() {
        SendRepeatMessage(0,5);
    }

    private void SendRepeatMessage(final int nCode, int milsec) {
        Message newMessage = theRepeatHandler.obtainMessage(nCode, 0, 0, null);
        theRepeatHandler.sendMessageDelayed(newMessage, milsec);
    }

    protected void DoRepeatProcess(int what) {
        boolean bCallAgain = false;
        int nCurX = mCurrentOffsetX;
        int nCurY = mCurrentOffsetY;
        if (nCurX != mTargetOffsetX) {
            int n = mScrollVal;
            nCurX += (mTargetOffsetX - nCurX) / n;
            while (nCurX == mCurrentOffsetX && n > 0) {
                nCurX += (mTargetOffsetX - nCurX) / n;
                n--;            
            }
        }
        
        if (nCurX == mCurrentOffsetX) {
            mCurrentOffsetX = mTargetOffsetX;
        }
        else {
            mCurrentOffsetX = nCurX;
        }
        if (mTargetOffsetX != mCurrentOffsetX) {
            bCallAgain = true;
        }

        if (nCurY != mTargetOffsetY) {
            int n = mScrollVal;
            nCurY += (mTargetOffsetY - nCurY) / n;
            while (nCurY == mCurrentOffsetY && n > 0) {
                nCurY += (mTargetOffsetY - nCurY) / n;
                n--;
            }
        }

        if (nCurY == mCurrentOffsetY) {
            mCurrentOffsetY = mTargetOffsetY;
        }
        else {
            mCurrentOffsetY = nCurY;
        }
        if (mTargetOffsetY != mCurrentOffsetY) {
            bCallAgain = true;
        }

        if (bCallAgain) {
            DoRepeatMessage();
        }
        else
        if (mbDownTouch == false) {            
            if (mCurrentOffsetY > 0) {
                mTargetOffsetY = 0;
            }
            else
            if (mCurrentOffsetY < -(mTotalHeight - mHeight)) {
                if (mTotalHeight > mHeight) {
                    mTargetOffsetY = -mTotalHeight + mHeight;
                }
                else {
                    mTargetOffsetY = 0;
                }
            }
            if (mTargetOffsetY != mCurrentOffsetY) {
                bCallAgain = true;
            }
            if (mTargetOffsetX > 0) {
                mTargetOffsetX = 0;
            }
            if (mTargetOffsetX == mCurrentOffsetX) {
                int nPage = (-mBeginOffsetX) / mWidth;
                if (mCurrentOffsetX < 0) {
                    int nAbsX = (mCurrentOffsetX -  mBeginOffsetX) * (mCurrentOffsetX -  mBeginOffsetX);
                    if (nAbsX > (mWidth/4)*(mWidth/4)) {
                        int nTargetXL = -(nPage-1) * mWidth;
                        int nTargetXR = -(nPage) * mWidth - mWidth;
                        if (mBeginOffsetX > mCurrentOffsetX) {  
                            mTargetOffsetX = nTargetXR;
                            mBeginOffsetX  = nTargetXR;
                        }
                        else
                        if (mBeginOffsetX < mCurrentOffsetX) {  
                            mTargetOffsetX = nTargetXL;                
                            mBeginOffsetX  = nTargetXL;
                        }
                    }
                    else {
                        
                        mTargetOffsetX = mBeginOffsetX;
                    }
                }
            }    
            if (mTargetOffsetX != mCurrentOffsetX) {
                bCallAgain = true;
            }
            if (bCallAgain) {
                DoRepeatMessage();
            }
            if (bCallAgain == false) {
                mCurrentOffsetX %= (TOTALPAGESIZE * mWidth);
                mTargetOffsetX = mCurrentOffsetX;
            }
        }        
    }
};
