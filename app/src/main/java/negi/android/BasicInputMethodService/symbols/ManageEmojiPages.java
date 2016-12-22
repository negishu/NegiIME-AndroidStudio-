package negi.android.BasicInputMethodService.symbols;

import negi.android.BaseInputMethodService.PreviewView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;

public class ManageEmojiPages {

    private static final int SIZEOFDATA = 5;
    private static final int MAX_SUGGESTIONS = 1024;
    private static final int OUT_OF_BOUNDS = -1;

    public final Emojicon[] DATA[] = new Emojicon[SIZEOFDATA][];

    private int mPage   = 0;
    private int mPosX   = 0;
    private int mPosY   = 0;
    private int mWidth  = 1;
    private int mHeight = 1;
    
    private int mMojiSize = 96;

    private float mTouchX = OUT_OF_BOUNDS;
    private float mTouchY = OUT_OF_BOUNDS;
    private float mPrevTouchX = OUT_OF_BOUNDS;
    private float mPrevTouchY = OUT_OF_BOUNDS;
    private float mDownTouchX = OUT_OF_BOUNDS;
    private float mDownTouchY = OUT_OF_BOUNDS;

    private DrawMojiPages mDrawMojiPages = null;
    private final PreviewView mPreviewView;
    private final PopupWindow mPreviewPopup;

    private final RectF mClipRect;

    public class PAGE {
        public final int[] mCellWidth  = new int[MAX_SUGGESTIONS];
        public final int[] mCellHeight = new int[MAX_SUGGESTIONS];
        public final int[] mCellX      = new int[MAX_SUGGESTIONS];
        public final int[] mCellY      = new int[MAX_SUGGESTIONS];
    };

    public final PAGE mPAGE[] = new PAGE[ManageEmojiPages.SIZEOFDATA];

    public ManageEmojiPages(Context context) {
        DATA[0] = Nature.DATA;
        DATA[1] = Objects.DATA;
        DATA[2] = People.DATA;
        DATA[3] = Places.DATA;
        DATA[4] = Symbols.DATA;
        for (int n = 0; n < SIZEOFDATA; n++) {
            mPAGE[n] = new PAGE();
        }
        mClipRect = new RectF();
        mDrawMojiPages = new DrawMojiPages(context);
        mPreviewView  = new PreviewView(context);
        mPreviewPopup = new PopupWindow(context);
        
        mPreviewPopup.setContentView(mPreviewView);
        mPreviewPopup.setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        mPreviewPopup.setTouchable(false);        
    }
    public void onTouchEvent(View view, int nAction, float mx, float my) {
        switch (nAction)
        {
        case MotionEvent.ACTION_DOWN:
            mTouchX = mx;
            mTouchY = my;
            mPrevTouchX = mTouchX;
            mPrevTouchY = mTouchY;            
            mDownTouchX = mTouchX;
            mDownTouchY = mTouchY;
        break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            mPreviewPopup.dismiss();
            mTouchX = mx;
            mTouchY = my;
        break;
        case MotionEvent.ACTION_MOVE:
            mPrevTouchX = mTouchX;
            mPrevTouchY = mTouchY;            
            mTouchX = mx;
            mTouchY = my;
        break;
        }
        final int nPage = mDrawMojiPages.GetCurrentPageIndex();
        boolean bCellFound = false;
        int nX = OUT_OF_BOUNDS, nY = OUT_OF_BOUNDS, nW = 0, nH = 0;
        if (0 <= nPage && nPage < mPAGE.length) {
            for (int n = 0; n < mPAGE[nPage].mCellX.length; n++) {
                int x = (int)mPAGE[nPage].mCellX[n];
                int y = (int)mPAGE[nPage].mCellY[n];
                int w = (int)mPAGE[nPage].mCellWidth [n];
                int h = (int)mPAGE[nPage].mCellHeight[n];
                Rect rect = new Rect(x, y, x + w, y + h);
                if (rect.contains((int)mx, (int)my)) {
                    nX = x; nY = y;
                    nW = w; nH = h;
                    bCellFound = true;
                    break;
                }
            }
        }
        if (bCellFound) {
            switch (nAction)
            {
                case MotionEvent.ACTION_DOWN:
                {
                    mPreviewView.setAlpha(235);
                    final Drawable drawable = mDrawMojiPages.GetCurrentIconDrawable();
                    mPreviewView.setDrawable(drawable, 50, 100, 100);
                    mPreviewPopup.setWidth(120);
                    mPreviewPopup.setHeight(120);
                    mPreviewPopup.showAtLocation(view, Gravity.NO_GRAVITY, (int)(nX), (int)(nY - 256));
                }
                case MotionEvent.ACTION_MOVE:
                {
                    mPreviewView.setAlpha(235);
                    final Drawable drawable = mDrawMojiPages.GetCurrentIconDrawable();
                    mPreviewView.setDrawable(drawable, 50, nW + 12, nH + 12);
                    mPreviewPopup.update((int)(nX), (int)(nY - 256), -1, -1, true);
                }
            }
        }
    }
    public void Draw(final Canvas canvas) {
        mDrawMojiPages.Draw(canvas, this, mTouchX, mTouchY);
    }
    public void SetPosAndSize(final int _x, final int _y, final int _w, final int _h) {
        mPosX = _x; mPosY = _y; mWidth = _w; mHeight = _h;
        for (int n = 0; n < ManageEmojiPages.SIZEOFDATA; n++) {
            PreCalculate(n);
        }
        mClipRect.set(_x, _y, _w, _h);
        mDrawMojiPages.SetPosAndSize(_x, _y, _w, _h, this);
    }
    public void setTargetOffsetPos(final int nPosX, final int nPosY, final int nDX, final int nDY, final boolean bDown) {
        if (mClipRect.contains(nPosX, nPosY)) {
            mDrawMojiPages.setTargetOffsetPos(nDX, nDY, bDown);
        }
        else {
            mDrawMojiPages.setTargetOffsetPos(nDX, nDY, bDown);            
        }
    }
    public void resetTargetOffsetPos() {
        mDrawMojiPages.resetTargetOffsetPos();
    }
    private final void PreCalculate(final int nPage) {
        final Emojicon[] m = DATA[nPage];
        final int count    = m.length;
        final int leftEnd  = 6;
        final int rightEnd = mWidth - 12;
        int x = leftEnd;
        int y = 2;
        final int nCellWidth  = mMojiSize;
        final int nCellHieght = mMojiSize;
        for (int ii = 0, i = 0; i < count; i++) {
            mPAGE[nPage].mCellWidth [i] = nCellWidth;
            mPAGE[nPage].mCellHeight[i] = nCellHieght;
            if ((x + nCellWidth) > rightEnd) {
                int diff = rightEnd - x;
                while (diff > 0) {
                    for (int n = ii; n < i && diff > 0; n++) {
                        mPAGE[nPage].mCellWidth[n]++;
                        diff--;
                    }
                }
                if (diff > 0 && i > 0) {
                    mPAGE[nPage].mCellWidth[i-1]+=diff;
                }
                for (int n = ii+1; n < i; n++) {
                    mPAGE[nPage].mCellX[n] = mPAGE[nPage].mCellX[n-1] + mPAGE[nPage].mCellWidth[n-1];
                }
                ii = i;
                x  = leftEnd;
                y += nCellHieght;
            }
            mPAGE[nPage].mCellX[i]     = x;
            mPAGE[nPage].mCellY[i]     = y;
            mPAGE[nPage].mCellWidth[i] = nCellWidth;
            x += nCellWidth;
        }
    }
}
