package negi.android.BasicInputMethodService;

import java.util.ArrayList;

import negi.android.BaseInputMethodService.PreviewView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;

public class ManageList {    
    protected static final String TAG = "ManageList";
    public static final int   OUT_OF_INDEX  = -1;
    public static final float OUT_OF_BOUNDS = -1;
    private float mTouchX = OUT_OF_BOUNDS;
    private float mTouchY = OUT_OF_BOUNDS;
    private float mPrevTouchX = OUT_OF_BOUNDS;
    private float mPrevTouchY = OUT_OF_BOUNDS;
    private float mDownTouchX = OUT_OF_BOUNDS;
    private float mDownTouchY = OUT_OF_BOUNDS;

    private int mIndex = OUT_OF_INDEX;
    private RectF mCandRect = null;

    private final PreviewView mPreviewView;
    private final PopupWindow mKeyPreviewPopup;
    private final DrawList    mDrawList;
    private final RectF       mClipRect;

    private ArrayList<String> mSuggestions = null;

    public ManageList(Context context) {
        
        mDrawList        = new DrawList(context);
        mPreviewView     = new PreviewView(context);
        mKeyPreviewPopup = new PopupWindow(context);
        mKeyPreviewPopup.setContentView(mPreviewView);
        mKeyPreviewPopup.setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        mKeyPreviewPopup.setTouchable(false);
        mClipRect = new RectF();
    }
    public void SetSuggestions(final ArrayList<String> aSuggestions, boolean completions, boolean typedWordValid) {

        mSuggestions = aSuggestions;
    }

    public void Draw(final Canvas canvas) {
        mCandRect = mDrawList.PreDraw(mSuggestions, mTouchX, mTouchY);            
        mDrawList.Draw(canvas, mSuggestions, mTouchX, mTouchY);
        mIndex = mDrawList.GetCurIndex();
        Log.v("WHAT IS ", "mIndex = " + mIndex);
    }
    
    public void onTouchEvent(View view, int nAction, float mx, float my, long downTime) {
        
        switch (nAction)
        {
        case MotionEvent.ACTION_DOWN:
            mTouchX = mx;
            mTouchY = my;
            mPrevTouchX = mTouchX;
            mPrevTouchY = mTouchY;            
            mDownTouchX = mTouchX;
            mDownTouchY = mTouchY;            
            if (mClipRect.contains(mTouchX, mTouchY)) {
                mCandRect = mDrawList.PreDraw(mSuggestions, mTouchX, mTouchY);            
                mIndex = mDrawList.GetCurIndex();
                Log.v("WHAT IS ", "mIndex = " + mIndex);
                if (mIndex != OUT_OF_INDEX) {
                    String suggestion = mSuggestions.get(mIndex);
                    mPreviewView.setInfo(suggestion, 36, (int)(mCandRect.right - mCandRect.left) + 16, (int)(mCandRect.bottom - mCandRect.top) + 16);
                    mKeyPreviewPopup.setWidth((int)(mCandRect.right - mCandRect.left) + 16);
                    mKeyPreviewPopup.setHeight((int)(mCandRect.bottom - mCandRect.top) + 16);
                    mKeyPreviewPopup.showAtLocation(view, Gravity.NO_GRAVITY, (int)(mTouchX - (mCandRect.right - mCandRect.left)/2), (int)(mTouchY - 256));
                }
            }
        break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            mPrevTouchX = mTouchX;
            mPrevTouchY = mTouchY;            
            mTouchX = OUT_OF_BOUNDS;
            mTouchY = OUT_OF_BOUNDS;
            mKeyPreviewPopup.dismiss();
        break;
        case MotionEvent.ACTION_MOVE:
            mPrevTouchX = mTouchX;
            mPrevTouchY = mTouchY;            
            mTouchX = mx;
            mTouchY = my;
            if (mClipRect.contains(mTouchX, mTouchY)) {
                if (mIndex != OUT_OF_INDEX) {
                    String suggestion = mSuggestions.get(mIndex);
                    if (!mKeyPreviewPopup.isShowing()) {
                        mKeyPreviewPopup.setWidth((int)(mCandRect.right - mCandRect.left) + 16);
                        mKeyPreviewPopup.setHeight((int)(mCandRect.bottom - mCandRect.top) + 16);
                        mKeyPreviewPopup.showAtLocation(view, Gravity.NO_GRAVITY, (int)(mTouchX - (mCandRect.right - mCandRect.left)/2), (int)(mTouchY - 256));                    
                    }
                    mPreviewView.setInfo(suggestion, 36, (int)(mCandRect.right - mCandRect.left) + 16, (int)(mCandRect.bottom - mCandRect.top) + 16);
                    mKeyPreviewPopup.update((int)(mTouchX - (mCandRect.right - mCandRect.left)/2), (int)(mTouchY - 256), (int)(mCandRect.right - mCandRect.left) + 16, (int)(mCandRect.bottom - mCandRect.top) + 16, true);
                }
            }
        break;
        }
    }
    
    public void SetPosAndSize(final int _x, final int _y, final int _w, final int _h) {
        mDrawList.SetPosAndSize(_x, _y, _w, _h);
        mClipRect.set(_x, _y, _w, _h);
    }
    
    public void setTargetOffsetPos(final int nPosX, final int nPosY, final int nDX, final int nDY, final boolean bDown) {
        if (mClipRect.contains(nPosX, nPosY)) {
            mDrawList.setTargetOffsetPos(nDX, nDY, bDown);
        }
        else {
            mDrawList.setTargetOffsetPos(nDX, 0, bDown);            
        }
    }

    public void resetTargetOffsetPos() {
        mDrawList.resetTargetOffsetPos();
    }
};
