package negi.android.BasicInputMethodService;

import java.util.List;

import negi.android.R;
import negi.android.BaseInputMethodService.DrawBase;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

public class DrawList extends DrawBase {
    
    private static final int X_GAP  = 15;
    private static final int MAX_SUGGESTIONS = 256;

    private final Paint mPaint;
    private final int mColorNormal;
    private final RectF mCandRect;
   
    private int mIndex = OUT_OF_BOUNDS;

    public final int[] mCellWidth  = new int[MAX_SUGGESTIONS];
    public final int[] mCellHeight = new int[MAX_SUGGESTIONS];
    public final int[] mCellX      = new int[MAX_SUGGESTIONS];
    public final int[] mCellY      = new int[MAX_SUGGESTIONS];
    public final int[] mTextWidth  = new int[MAX_SUGGESTIONS];
    
    public DrawList(Context context) {
        super(context);
        final Resources resources = context.getResources();
        mColorNormal = resources.getColor(R.color.candidate_normal);
        mPaint = new Paint();
        mPaint.setTypeface(Typeface.DEFAULT);
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(255);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mCandRect = new RectF();
    }
    
    public final RectF Draw(final Canvas canvas, final List<String> aSuggestions, final float tx, final float ty) {

        final Paint paint = mPaint;

        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(36);
        
        canvas.save();
        canvas.clipRect(mClipRectF);

        int nCurrentX = mCurrentOffsetX;
        nCurrentX -= mWidth;
        nCurrentX %= (TOTALPAGESIZE * mWidth);
        nCurrentX += mWidth;

        if (aSuggestions != null) {
            String suggestion = null;
            final int count = aSuggestions.size();
            for (int ii = 0; ii < count; ii++) {
                suggestion = aSuggestions.get(ii);
                int x = (mCellX[ii] + (mCellWidth[ii] / 2)) - (mTextWidth[ii] / 2) + nCurrentX;
                int y = mCellY[ii] + mCurrentOffsetY;
                int w = mTextWidth[ii];
                int h = mCellHeight[ii];
                if (mIndex == ii) {
                    paint.setColor(Color.DKGRAY);
                    paint.setStyle(Style.FILL_AND_STROKE);
                    canvas.drawRoundRect(mRectF, 8, 8, paint);
                }
                paint.setColor(mColorNormal);
                paint.setStyle(Style.FILL_AND_STROKE);
                paint.setAlpha(255);
                paint.setStrokeWidth(1);
                canvas.drawText(suggestion, x+(w/2), y + h - 8, paint);
            }
            
            if (count > 0) {
                float fHeight = mHeight;
                float fHeightRatio = fHeight / (float)mTotalHeight;
                float posy = -(mCurrentOffsetY * fHeightRatio) ;
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
                canvas.drawLine(mPosX + nCurrentX + mWidth - 6, posy + 2, mPosX + nCurrentX + mWidth - 6, posy + fHeight - 3, paint);
            }
        }
        
        canvas.restore();

        paint.setAlpha(35);
        paint.setColor(Color.GRAY);
        paint.setStrokeWidth(1);
        canvas.drawLine(mPosX + nCurrentX, mPosY ,              mPosX + nCurrentX + mWidth, mPosY,               paint);
        canvas.drawLine(mPosX + nCurrentX, mPosY + mHeight - 1, mPosX + nCurrentX + mWidth, mPosY + mHeight - 1, paint);

        return mCandRect;
    }
    
    public final RectF PreDraw(final List<String> aSuggestions, final float tx, final float ty) {
        
        mTotalHeight = 0;
        
        if (null == aSuggestions) {
            return null;
        }
        
        final int count   = aSuggestions.size();
        final Paint paint = mPaint;

        final int leftEnd  = X_GAP;
        final int rightEnd = mWidth - (X_GAP*2);

        int x = leftEnd;
        int y = 0;

        final int nCellHieght = (int)(paint.getTextSize() + paint.descent() - paint.ascent() - 16);

        String suggestion = null;
        
        float nCellWidth = 0.0f;
        float textWidth  = 0.0f;
        
        for (int ii = 0, i = 0; i < count; i++) {
            suggestion = aSuggestions.get(i);
            textWidth = paint.measureText(suggestion);
            mTextWidth [i] = (int)textWidth;
            mCellHeight[i] = nCellHieght;
            nCellWidth = (textWidth + X_GAP);
            if (nCellWidth < X_GAP) nCellWidth = X_GAP;
            if ((x + nCellWidth) > rightEnd) {                
                int diff = rightEnd - x;
                while (diff > 0) {
                    for (int n = ii; n < i && diff > 0; n++) {
                        mCellWidth[n]++;
                        diff--;
                    }
                }
                if (diff > 0 && i > 0) {
                    mCellWidth[i-1]+=diff;
                }
                for (int n = ii+1; n < i; n++) {
                    mCellX[n] = mCellX[n-1] + mCellWidth[n-1];
                }

                ii = i;
                x  = 6;
                y += nCellHieght;
            }

            mCellX[i]     = x;
            mCellY[i]     = y;
            mCellWidth[i] = (int)nCellWidth;

            if (mTotalHeight < y) {
                mTotalHeight = y;
            }
            x += nCellWidth;
        }
        
        mTotalHeight += nCellHieght;
        
        mIndex =-1;
        
        for (int ii = 0; ii < count; ii++) {
            int _x = (mCellX[ii] + (mCellWidth[ii] / 2)) - (mTextWidth[ii] / 2);
            int _w = mTextWidth[ii];
            int _y = mCellY[ii];
            int _h = mCellHeight[ii];
            mRectF.set(_x-2 + mCurrentOffsetX, _y + mCurrentOffsetY, _x + mCurrentOffsetX + _w + 4, _y + mCurrentOffsetY + _h + 4);
            if (mRectF.contains(tx, ty)) {
                mCandRect.set(mRectF.left, mRectF.top, mRectF.right, mRectF.bottom);
                mIndex = ii;
                Log.v("HERE", "mIndex = " + mIndex);
                break;
            }
        }
        Log.v("THERE", "mIndex = " + mIndex);
        return mCandRect;
    }

    public final int GetCurIndex() {
        return mIndex;
    }
};
