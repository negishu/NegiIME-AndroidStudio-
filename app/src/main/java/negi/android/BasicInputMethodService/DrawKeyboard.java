package negi.android.BasicInputMethodService;

import negi.android.BaseInputMethodService.DrawBase;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;

public class DrawKeyboard extends DrawBase {
    private final Paint mPaint;
    public DrawKeyboard(Context context) {
        super(context);
        mPaint = new Paint();
        mPaint.setTypeface(Typeface.DEFAULT);
        mPaint.setAntiAlias(true);
    }
    public void Draw(final Canvas canvas, final ManageKeyboard keyboard, float mx, float my) {

        final Paint paint = mPaint;

        int nCurrentX = mCurrentOffsetX;
        nCurrentX -= mWidth;
        nCurrentX %= (TOTALPAGESIZE * mWidth);
        nCurrentX += mWidth;

        for (int n = 0; n < keyboard.GetKeyCount(); n++) {
            float x = keyboard.GetPhysicKey(n).GetX() + nCurrentX;
            float y = keyboard.GetPhysicKey(n).GetY();
            float w = keyboard.GetPhysicKey(n).GetW();
            float h = keyboard.GetPhysicKey(n).GetH();
            mRectF.set(x, y, x + w, y + h);
            paint.setStrokeWidth(2.0f);
            paint.setAlpha(135);
            if (mRectF.contains(mx, my)) {
                paint.setColor(Color.DKGRAY);
                paint.setStyle(Style.FILL_AND_STROKE);
                canvas.drawRoundRect(mRectF, 12, 12, paint);
            }
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Align.CENTER);
            paint.setStyle(Style.FILL_AND_STROKE);
            paint.setTextSize(keyboard.GetPhysicKey(n).GetLabelSize());
            paint.setAlpha(215);
            canvas.drawText(keyboard.GetPhysicKey(n).GetLabel(), 0, keyboard.GetPhysicKey(n).GetLabel().length(), x + (w/2), y + (h) - (h/4), paint);
        }
    }
};
