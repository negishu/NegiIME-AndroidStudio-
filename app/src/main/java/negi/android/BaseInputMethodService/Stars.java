package negi.android.BaseInputMethodService;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;

public class Stars {
    private final Handler theHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DoMessageProcess();
        }
    };
    protected void DoMessageProcess() {
        update();
        theHandler.sendEmptyMessageDelayed(1000, 100);
    }
	private class Star {
		public Star(int x, int y, int dx, int dy, int r, int a){
			this.x = x; this.y = y; this.dx = dx; this.dy = dy; this.t = 0;
		}
		public void update(final int w, final int h) {
			x+=dx;
			y+=dy;
			t++;
			//a = a + (1 - rand.nextInt(3));
            if ((y/5) > height) {
                y  = 0;
                x  = rand.nextInt(width*5);
                dx = (rand.nextInt(5) - 2);
                dy = (rand.nextInt(5) + 1);
                t  = 0;
            }
            
            if (t > 50) {              
                dx = (rand.nextInt(5) - 2);
                t = 0;
            }
		}
		public int x,y,dx,dy,t;
	}
	private Star[] mStars = null;
	private int width, height;
	private final Random rand;
    private final Paint mPaint;

	public Stars(final Random rand, final int width, final int height, int n) {
		mStars = new Star[n];
		for (int i = 0; i < n; i++){
			int x = (rand.nextInt(width*5));
			int y = (rand.nextInt(height*5));
			int dx = (rand.nextInt(7)  -   3);
			int dy = (rand.nextInt(5)  +   1);
			int r  = (rand.nextInt(1)  +   1);
			int a  = (rand.nextInt(64) + 192);
			mStars[i] = new Star(x,y,dx,dy,r,a);
		}
		this.width = width;
		this.height = height;
		this.rand  = rand;

		mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setAlpha(255);

        DoMessageProcess();
	}
	
	protected void update() {
		final int size = mStars.length;
		for (int i = 0; i < size; i++){
			mStars[i].update(width, height);
		}
	}
	
	public void draw(final Canvas canvas) {
		final int size = mStars.length;
		final Paint paint = mPaint;
        paint.setColor(Color.WHITE);
        paint.setAlpha(255);
		for (int i = 0; i < size; i++) {
            canvas.drawPoint((mStars[i].x/5), (mStars[i].y/5), paint);
		}
	}
}
