package negi.android.BasicInputMethodService;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

public class ManageTrace {

    protected static final String TAG = "Trace";

    public static final int MAXSTROKE = 32;
    public static final int MAXPOINTS = 256;
    public static final int OUT_OF_BOUNDS = -1;

    private float mTouchX = OUT_OF_BOUNDS;
    private float mTouchY = OUT_OF_BOUNDS;

    protected DrawTrace mDrawTrace = null;

    public boolean mbTraceOn = false;
    
    public int mCurID = 0;
    public int _ptX[][]   = new int  [MAXSTROKE][MAXPOINTS];
    public int _ptY[][]   = new int  [MAXSTROKE][MAXPOINTS];
    public int _cnt[]     = new int  [MAXSTROKE];
    public long _time[][] = new long [MAXSTROKE][MAXPOINTS];
    public float _strokealpha[] = new float[MAXSTROKE];

    private final Handler theProcessHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DoProcess();
        }
    };
        
    public ManageTrace(Context context) {
        mDrawTrace = new DrawTrace(context);               
    }

    public void onTouchEvent(View view, int nAction, float mx, float my, long downTime) {
        
        mTouchX = mx;
        mTouchY = my;

        switch (nAction)
        {
        case MotionEvent.ACTION_DOWN:
            _cnt[mCurID] = 0;
            
            if (mTouchY < 160) break;
                
            mbTraceOn = true;
            
            _ptX[mCurID][_cnt[mCurID]]  = (int)mTouchX;
            _ptY[mCurID][_cnt[mCurID]]  = (int)mTouchY;
            _time[mCurID][_cnt[mCurID]] = downTime;
            _strokealpha[mCurID]        = 0.0f;
        break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            mbTraceOn = false;
            if (mCurID >= 0) {
                _ptX[mCurID][_cnt[mCurID]]   = (int)mTouchX;
                _ptY[mCurID][_cnt[mCurID]]   = (int)mTouchY;
                _time[mCurID][_cnt[mCurID]]  = downTime;
            }
            mCurID++;
            mCurID %= MAXSTROKE;
            _cnt[mCurID] = OUT_OF_BOUNDS;
            mTouchX = OUT_OF_BOUNDS;
            mTouchY = OUT_OF_BOUNDS;
            DoProcess();
        break;
        case MotionEvent.ACTION_MOVE:
            if (mbTraceOn) {
                if (mCurID >= 0) {
                    _ptX[mCurID][_cnt[mCurID]]   = (int)mTouchX;
                    _ptY[mCurID][_cnt[mCurID]]   = (int)mTouchY;
                    _time[mCurID][_cnt[mCurID]]  = downTime;
                    _strokealpha[mCurID] = 255.0f;
                    _cnt[mCurID]++;
                    _cnt[mCurID] %= MAXPOINTS;
                }
            }
        break;
        }
    }

    public void Draw(final Canvas canvas) {
        mDrawTrace.Draw(canvas, this);
    }

    private void DoProcess() {
        boolean bAgain = false;
        for (int n = 0; n < MAXSTROKE; n++) {
            if (_strokealpha[n] > 12.0) {
                _strokealpha[n] *= 0.95;
                bAgain = true;
            }
            else {
                _strokealpha[n] = 0.0f;
            }
        }
        if (bAgain) {
            theProcessHandler.sendEmptyMessageDelayed(0, 25);
        }
    }
};
