package negi.android.BasicInputMethodService;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class DrawTrace {

    private final Paint mPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path mPath = new Path();

    public DrawTrace(Context context) {
        
    }

    private final Path bezierTracePath(final int[]x, final int[]y, final long[]time, final int cnt) {

        float cx = x[cnt-1];
        float cy = y[cnt-1];

        mPath.reset();
        mPath.moveTo(cx, cy);
        
        for (int n = 1, i = cnt - 2; i >= 0 && n < cnt; i--, n++) {
            float nextX = x[i];
            float nextY = y[i];
            float midX = (cx + nextX) / 2;
            float midY = (cy + nextY) / 2;
            if (n == 1) {
                mPath.lineTo(midX, midY);
            } else {
                mPath.quadTo(cx, cy, midX, midY);
            }
            cx = nextX;
            cy = nextY;
        }
        mPath.lineTo(cx, cy);
        
        return mPath;
    }
    
    private void DoDrawTarce(final Canvas canvas, final int[]x, final int[]y, final long[]time, final float alpha, final int nCnt) {

        final Paint paint = mPaint;
        
        final int iTraceColor = Color.BLUE;
        final int iFadeScale = (int)alpha;
        final float widthScaled = 12;
        
        if (widthScaled > 0) {
            if (nCnt >= 2) { 
                final Path newpath = bezierTracePath(x, y, time, nCnt);
                paint.setColor(iTraceColor);
                paint.setAlpha(iFadeScale);
                paint.setStrokeWidth(widthScaled);
                paint.setStyle( Paint.Style.STROKE );
                canvas.drawPath(newpath, paint);
            }
        }        
    }

    public void Draw(final Canvas canvas, final ManageTrace aTrace) {
        for (int n = 0; n < ManageTrace.MAXSTROKE; n++) {
            DoDrawTarce(canvas, aTrace._ptX[n], aTrace._ptY[n], aTrace._time[n], aTrace._strokealpha[n], aTrace._cnt[n]);
        }
    }
};
