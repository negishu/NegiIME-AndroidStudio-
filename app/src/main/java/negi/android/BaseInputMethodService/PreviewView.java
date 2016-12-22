package negi.android.BaseInputMethodService;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class PreviewView extends FrameLayout {

    private class BaseView extends View {
        private Paint mPaint;
        private CharSequence mText = null;
        private Drawable mDrawable = null;
        private final RectF mRectF;
        
        public BaseView(Context context) {
            super(context);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setTextSize(18);
            mPaint.setTextAlign(Align.CENTER);
            mPaint.setAlpha(255);
            setWillNotDraw(false);
            setFocusable(false);
            mRectF = new RectF();
        }
                
        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            setMeasuredDimension(width, height);
        }

        @Override
        public void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        public boolean onTouchEvent(MotionEvent me) {
            return false; 
        }
        @Override
        public void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            closing();
        }
        private void closing() {
        
        }

        public void setInfo(final CharSequence text, final int nFontSize, final int nW, final int nH) {
            mText = text;
            mDrawable = null;
            mRectF.set(0, 0, nW, nH);
            mPaint.setTextSize(nFontSize);
            setMeasuredDimension(nW, nH);
            invalidate();
        }
        public void setDrawable(final Drawable drawable, final int nSize, final int nW, final int nH) {
            mDrawable = drawable;
            mText = null;
            mRectF.set(0, 0, nW, nH);
            invalidate();
        }
        @Override
        public void onDraw(Canvas canvas) {
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Style.FILL_AND_STROKE);
            mPaint.setTextAlign(Align.CENTER);
            mPaint.setAlpha(235);
            if (mDrawable != null) {
                canvas.save();
                canvas.translate(0, 0);
                mDrawable.setBounds(0, 0, (int)mRectF.right, (int)mRectF.bottom);
                mDrawable.draw(canvas);
                canvas.restore();
            }
            if (mText != null) {
                canvas.drawRoundRect(mRectF, 12, 12, mPaint);
                mPaint.setAlpha(255);
                mPaint.setColor(Color.BLACK);
                canvas.drawText(mText, 0, mText.length(), (mRectF.right- mRectF.left) / 2, (mRectF.bottom - mRectF.top) * 0.80f, mPaint);
            }
        }
    }

    private BaseView theBaseView;
    
    public PreviewView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    public PreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public PreviewView(Context context) {
        super(context);
        init(context);
    }
    public void setInfo(final CharSequence text, final int nFontSize, final int nW, final int nH) {
        theBaseView.setInfo(text, nFontSize, nW, nH);
        theBaseView.setVisibility(VISIBLE);
        theBaseView.invalidate();
    }
    public void setDrawable(final Drawable drawable, final int nSize, final int nW, final int nH) {
        theBaseView.setDrawable(drawable, nSize, nW, nH);
        theBaseView.setVisibility(VISIBLE);
        theBaseView.invalidate();
    }
    public void setAlpha(float alpha) {
        theBaseView.setAlpha(alpha);
    }
    private void init(Context context) {
        theBaseView = new BaseView(context);
        this.addView(theBaseView);
        setFocusable(false);
    }
}
