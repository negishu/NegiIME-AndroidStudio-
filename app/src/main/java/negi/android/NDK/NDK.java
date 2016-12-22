package negi.android.NDK;

import java.nio.ByteBuffer;

public class NDK {
	static private CallbackListener theCallbackListener = null;
	static private long nativeObjPointer = 0;
	static {
		nativeObjPointer = 0;
		System.loadLibrary("native-lib");
	}
	public NDK(CallbackListener aCallbackListener) {
		theCallbackListener = aCallbackListener;
		nativeObjPointer = initialize(this);
	}
	public long sendrecieve(ByteBuffer in_byteBuffer, int inSize, ByteBuffer out_byteBuffer, int outSize){
		if (nativeObjPointer != 0) {
			return communicate(this, nativeObjPointer, in_byteBuffer, inSize, out_byteBuffer, outSize);
		}
 		return -1;
	}
	public int callback(int retSize){ 
         return theCallbackListener.callback(retSize); 
	}
	public static native long initialize(NDK target);
	public static native long communicate(NDK target, long lObjPointer, ByteBuffer in_byteBuffer, long inSize, ByteBuffer out_byteBuffer, long outSize);
}
