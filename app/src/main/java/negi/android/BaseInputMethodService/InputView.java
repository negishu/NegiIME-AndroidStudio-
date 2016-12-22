package negi.android.BaseInputMethodService;

import java.util.ArrayList;
import java.util.Random;

import negi.android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class InputView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static final float MESH_SIZE = 32.0f;
    
    private Paint mMeshPaint;
    private Drawable mViewBackground;

    final private SwipeTracker mSwipeTracker = new SwipeTracker();
    private int mSwipeThreshold = 5;

    protected InputMethodService mService    = null;
    protected ArrayList<String> mSuggestions = null;

    protected SurfaceHolder mHolder = null;
    protected Thread mThread = null;

    protected Context mContext;
    protected int mViewWidth  = 0;
    protected int mViewHeight = 0;
    protected int mParentSize = 0;
    
    private Stars mStars;
    private final Random rand = new Random();
    private static final ArrayList<String> EMPTY_LIST = new ArrayList<String>();

    public void setService(InputMethodService service) {
        mService = service;
    }
    public void SetSuggestions(final ArrayList<String> aSuggestions, boolean completions, boolean typedWordValid) {
        clear();
        if (null != aSuggestions) {
            mSuggestions = new ArrayList<String>(aSuggestions);
        }
    }
    private final GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return onGestureDown(e);
        }
        @Override
        public void onShowPress(MotionEvent e) {
            onGestureShowPress(e);
        }
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return onGestureSingleTapUp(e);
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY) {
            return onGestureScroll(e1,e2,distanceX,distanceY);
        }
        @Override
        public void onLongPress(MotionEvent e) {
            onGestureLongPress(e);          
        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
            return onGestureFling(e1,e2,velocityX,velocityY);
        }
    });
    public boolean onGestureDown(MotionEvent e) {
        return false;
    }
    public void onGestureShowPress(MotionEvent e) {
    }
    public boolean onGestureSingleTapUp(MotionEvent e) {
        return false;
    }
    public boolean onGestureScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY) {
        return false;
    }
    public void onGestureLongPress(MotionEvent e) {
    }
    public boolean onGestureFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
        mSwipeTracker.computeCurrentVelocity(1000);
        final float absX = Math.abs(velocityX);
        final float absY = Math.abs(velocityY);
        float deltaX = e2.getX() - e1.getX();
        float deltaY = e2.getY() - e1.getY();
        int travelX = getWidth()  / 8;
        int travelY = getHeight() / 8;
        if (velocityX > mSwipeThreshold && absY < absX && deltaX > travelX) {
            swipeRight(deltaX);
        }
        else if (velocityX < -mSwipeThreshold && absY < absX && deltaX < -travelX) {
            swipeLeft(deltaX);
        }
        else if (velocityY < -mSwipeThreshold && absX < absY && deltaY < -travelY) {
            swipeUp(deltaY);
        }
        else if (velocityY > mSwipeThreshold && absX < absY && deltaY > travelY) {
            swipeDown(deltaY);
        }
        return false;
    }
    public void swipeLeft(float deltaX) {
    }
    public void swipeRight(float deltaX) {
    }
    public void swipeUp(float deltaY) {
    }
    public void swipeDown(float deltaY) {
    }
    public InputView(Context context, int nParentSize, int nViewSize) {
    	super(context);
        mContext    = context;
        mParentSize = nParentSize;
        mViewHeight = nViewSize;
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSwipeThreshold = (int) (500 * getResources().getDisplayMetrics().density);
        setWillNotDraw(true);
//        setZOrderOnTop(true);
        mViewBackground = context.getResources().getDrawable(R.drawable.background);
        mMeshPaint = new Paint();
        mStars = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width  = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        mGestureDetector.onTouchEvent(me);
        mSwipeTracker.onTouchEvent(me);
        return true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        closing();
    }
    
    @Override
    public void draw(final Canvas canvas) {
    }
    
    private void closing() {
        
    }
    
    private void clear() {
        mSuggestions = EMPTY_LIST;
    }

    protected void DoDraw(final Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        final Paint paint = mMeshPaint;
        final Drawable viewBackground = mViewBackground;

        viewBackground.setAlpha(255);
        viewBackground.setBounds(0, 0, mViewWidth, mViewHeight);
        viewBackground.draw(canvas);

        if (false) {
            float x_center = mViewWidth  / 2;
            float y_center = mViewHeight / 2;
            for (float yy = 0; yy < y_center; yy += MESH_SIZE) {
                paint.setColor(Color.GRAY);
                paint.setAlpha(255);
                paint.setStrokeWidth(1);
                paint.setStyle(Style.STROKE);
                for (int x = 0; x < x_center; x += MESH_SIZE) {
                    canvas.drawLine(x_center - x, y_center - yy, x_center - x + 1, y_center - yy, paint);
                    canvas.drawLine(x_center + x, y_center - yy, x_center + x + 1, y_center - yy, paint);
                    canvas.drawLine(x_center - x, y_center + yy, x_center - x + 1, y_center + yy, paint);
                    canvas.drawLine(x_center + x, y_center + yy, x_center + x + 1, y_center + yy, paint);
                }
            }
            for (float xx = 0; xx < x_center; xx += MESH_SIZE) {
                mMeshPaint.setColor(Color.GRAY);
                for (int y = 0; y < y_center; y += MESH_SIZE) {
                    canvas.drawLine(x_center + xx, y_center - y, x_center + xx, y_center - y + 1, paint);
                    canvas.drawLine(x_center + xx, y_center + y, x_center + xx, y_center + y + 1, paint);
                    canvas.drawLine(x_center - xx, y_center - y, x_center - xx, y_center - y + 1, paint);
                    canvas.drawLine(x_center - xx, y_center + y, x_center - xx, y_center + y + 1, paint);
                }
            }
        }

        mStars.draw(canvas);
    }
    
    private void DoDraw() {
        Canvas canvas = mHolder.lockCanvas();
        DoDraw(canvas);
        mHolder.unlockCanvasAndPost(canvas);     
    }
        
    @Override
    public void run() {
        while (mThread != null) {
            try {
                DoDraw();
            }
            catch (Exception  e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mThread==null) {
            mViewWidth  = width;
            mViewHeight = height;
            mStars = new Stars(rand, width, height, 512);
            mThread = new Thread(this);
        }
        if (mThread!=null) {
            if (mThread.isAlive() == false) {
                mThread.start();
            }
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        mThread = null;
    }
}
