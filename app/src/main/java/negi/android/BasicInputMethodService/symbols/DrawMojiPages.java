package negi.android.BasicInputMethodService.symbols;

import negi.android.BaseInputMethodService.DrawBase;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;

public class DrawMojiPages extends DrawBase {
    
    private static final int SIZEOFDATA = 5;

    private final TextPaint mPaint;
    private final RectF mTargetRectF;
    private Drawable mCurrentIconDrawable;
    private int mPage = 0;

    private int mMojiSize = 96;

    private class PAGE {
        private int mTotalHeight = 0;
        private int mCurrentOffsetY = 0;
        private int mTargetOffsetY  = 0;
    };

    final PAGE mPAGE[] = new PAGE[SIZEOFDATA];
       
    public final Drawable GetCurrentIconDrawable()
    {
        return mCurrentIconDrawable;
    }

    public final int GetCurrentPageIndex()
    {
        return mPage;
    }    

    public DrawMojiPages(Context context) {
        super(context);
        mPaint = new TextPaint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setTextAlign(Align.LEFT);
        mTargetRectF = new RectF();
        for (int n = 0; n < SIZEOFDATA; n++) {
            mPAGE[n] = new PAGE();
        }
        mCurrentIconDrawable = null;
        mbDownTouch = false;
    }
    
    public void Draw(final Canvas canvas, final ManageEmojiPages mojipages, float tx, float ty) {
        final Paint paint = mPaint;
        canvas.clipRect(mClipRectF);
        mCurrentIconDrawable = null;
        for (int n = 0; n < mojipages.mPAGE.length; n++) {
            final Emojicon[] m = mojipages.DATA[n];
            final int nCurrentY = mPAGE[n].mCurrentOffsetY;
            final int count = m.length;
            final int nAlpha = 255;
            
            int nCurrentX = mCurrentOffsetX + (n * mWidth) + mWidth;

            nCurrentX -= mWidth;
            nCurrentX %= (TOTALPAGESIZE * mWidth);
            nCurrentX += mWidth;
            
            for (int ii = 0; ii < count; ii++) {
                final int _x = mojipages.mPAGE[n].mCellX[ii] + nCurrentX;
                final int _w = mojipages.mPAGE[n].mCellWidth[ii];
                final int _y = mojipages.mPAGE[n].mCellY[ii] + nCurrentY;
                final int _h = mojipages.mPAGE[n].mCellHeight[ii];
                mRectF.set(_x, _y, _x + _w, _y + _h);
                if (mTargetRectF.contains(mRectF)) {
                    final String emoji  = m[ii].getEmoji();
                    final int codePoint = m[ii].getCodePoint();
                    final int icon = StaticEmojiconHandler.getTWEmojiResource(codePoint);                    
                    //final int icon = StaticEmojiconHandler.getEmojiResource(codePoint);
                    if (icon > 0) {
                        final Drawable drawable = mContext.getResources().getDrawable(icon);
                        if (mRectF.contains(tx, ty)) {
                            paint.setColor(Color.DKGRAY);
                            paint.setStyle(Style.FILL_AND_STROKE);
                            canvas.drawRoundRect(mRectF, 8, 8, paint);
                            mCurrentIconDrawable = drawable;
                        }
                        if (drawable != null) {
                            canvas.save();
                            canvas.translate(_x, _y);
                            drawable.setAlpha(nAlpha);
                            drawable.setBounds(0, 0, mMojiSize, mMojiSize);
                            drawable.draw(canvas);
                            canvas.restore();
                        }
                    }
                    else {
                        if (mRectF.contains(tx, ty)) {
                            paint.setColor(Color.DKGRAY);
                            paint.setStyle(Style.FILL_AND_STROKE);
                            canvas.drawRoundRect(mRectF, 8, 8, paint);
                        }
                        paint.setStyle(Style.FILL);
                        paint.setAntiAlias(true);
                        paint.setColor(Color.WHITE);
                        paint.setTextAlign(Align.CENTER);
                        paint.setAlpha(255);
                        paint.setTextSize(mMojiSize-21);
                        canvas.drawColor(Color.TRANSPARENT);
                        canvas.save();
                        canvas.translate(_x, _y);
//                        canvas.drawText(emoji, mRectF.left + ((mRectF.right - mRectF.left)/2),  mRectF.bottom - 24, paint);
                        canvas.drawText(emoji, 0, 0, paint);
                        canvas.restore();
                    }
                }
            }
            float fHeight = mHeight;
            float fHeightRatio = fHeight / (float)mPAGE[n].mTotalHeight;
            float posy = -(mPAGE[n].mCurrentOffsetY * fHeightRatio) ;
            fHeight = fHeight * fHeightRatio;
            if (fHeight > mHeight) {
                fHeight = mHeight;
                posy = mPosY;
            }
            if (posy + fHeight > mHeight) {
                posy = mHeight - fHeight;
            }
            else if (posy < 0) {
                posy = 0;            
            }            
            paint.setAlpha(235);
            paint.setColor(Color.GRAY);
            paint.setStrokeWidth(12);
            canvas.drawLine(mPosX + mWidth - 6 + nCurrentX, posy, mPosX + mWidth - 6 + nCurrentX, posy + fHeight, paint);
        }        
        paint.setAlpha(155);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(1);
        canvas.drawLine(mPosX, mPosY,           mPosX + mWidth, mPosY, paint);
        canvas.drawLine(mPosX, mPosY + mHeight, mPosX + mWidth, mPosY + mHeight, paint);
    }
    
    private final void PreCalculate(final ManageEmojiPages mojipages, final int nPage) {
        final Emojicon[] m = mojipages.DATA[nPage];
        final int count    = m.length;
        int y = 0;
        mPAGE[nPage].mTotalHeight = 0;
        for (int i = 0; i < count; i++) {
            y = mojipages.mPAGE[nPage].mCellY[i];
            if (mPAGE[nPage].mTotalHeight < y) {
                mPAGE[nPage].mTotalHeight = y;
            }
        }
        mPAGE[nPage].mTotalHeight += mMojiSize;
    }

    public void SetPosAndSize(final int _x, final int _y, final int _w, final int _h, final ManageEmojiPages mojipages) {
        mPosX = _x; mPosY = _y; mWidth = _w; mHeight = _h;
        mClipRectF.set(mPosX, mPosY, mPosX + mWidth, mPosY + mHeight);
        mTargetRectF.set(mPosX - mMojiSize, mPosY - mMojiSize, mPosX + mMojiSize + mWidth, mPosY + mMojiSize + mHeight);
        for (int n = 0; n < mojipages.mPAGE.length; n++) {
            PreCalculate(mojipages, n);
        }
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
        if (0 <= mPage && mPage < mPAGE.length) {
            mPAGE[mPage].mTargetOffsetY = mPAGE[mPage].mTargetOffsetY + _nPosY;
        }
        mbDownTouch = bDown;
        DoRepeatMessage();
    }
    @Override
    public void resetTargetOffsetPos() {
        if (0 <= mPage && mPage < mPAGE.length) {
            mPAGE[mPage].mTargetOffsetY = 0;
        }
        super.resetTargetOffsetPos();
    }
    
    @Override
    protected void DoRepeatProcess(int what) {

        boolean bCallAgain = false;
        
        int nCurX = mCurrentOffsetX;
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

        if (0 <= mPage && mPage < mPAGE.length) {
            int nCurY = mPAGE[mPage].mCurrentOffsetY;
            if (nCurY != mPAGE[mPage].mTargetOffsetY) {
                int n = mScrollVal;
                nCurY += (mPAGE[mPage].mTargetOffsetY - nCurY) / n;
                while (nCurY == mPAGE[mPage].mCurrentOffsetY && n > 0) {
                    nCurY += (mPAGE[mPage].mTargetOffsetY - nCurY) / n;
                    n--;
                }
            }
            if (nCurY == mPAGE[mPage].mCurrentOffsetY) {
                mPAGE[mPage].mCurrentOffsetY = mPAGE[mPage].mTargetOffsetY;
            }
            else {
                mPAGE[mPage].mCurrentOffsetY = nCurY;
            }
            if (mPAGE[mPage].mTargetOffsetY != mPAGE[mPage].mCurrentOffsetY) {
                bCallAgain = true;
            }
        }
        
        if (bCallAgain) {
            DoRepeatMessage();
        }
        else
        if (mbDownTouch == false) {
            if (0 <= mPage && mPage < mPAGE.length) {
                if (mPAGE[mPage].mCurrentOffsetY > 0) {
                    mPAGE[mPage].mTargetOffsetY = 0;
                }
                else
                if (mPAGE[mPage].mCurrentOffsetY < -(mPAGE[mPage].mTotalHeight - mHeight)) {
                    if (mPAGE[mPage].mTotalHeight > mHeight) {
                        mPAGE[mPage].mTargetOffsetY = -mPAGE[mPage].mTotalHeight + mHeight;
                    }
                    else {
                        mPAGE[mPage].mTargetOffsetY = 0;
                    }
                }
                if (mPAGE[mPage].mTargetOffsetY != mPAGE[mPage].mCurrentOffsetY) {
                    bCallAgain = true;
                }
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
        if (bCallAgain == false && mbDownTouch == false) {
            int nPage = (-mCurrentOffsetX - mWidth) / mWidth;
            mPage = nPage;
        }
    }
};
