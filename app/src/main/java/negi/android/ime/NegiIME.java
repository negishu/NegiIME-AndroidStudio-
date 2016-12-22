package negi.android.ime;

import android.util.Log;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;

import com.google.protobuf.InvalidProtocolBufferException;

import negi.android.ime.InputMethodService;
import negi.android.NDK.*;
import negi.android.NDK.SET.Message.*;

public class NegiIME implements negi.android.NDK.CallbackListener {
	
	private NDK impl;
	
	private ByteBuffer inbytes ;
	private ByteBuffer outbytes;

	private InputMethodService theInputMethodService;
	
	public NegiIME (InputMethodService aInputMethodService) {
		impl = new NDK(this);
		theInputMethodService = aInputMethodService;
		inbytes  = ByteBuffer.allocateDirect(4096).order(ByteOrder.LITTLE_ENDIAN);
		outbytes = ByteBuffer.allocateDirect(4096).order(ByteOrder.LITTLE_ENDIAN);
	}
	
	private void destruction() {
		inbytes  = null;
		outbytes = null;
	}
	
	@Override
    protected void finalize() throws Throwable {
        try {
          super.finalize();
        }
        finally {
          destruction();
        }
    }

	public final long communicate(final String inline) {
		
		CANDS.Builder cands_builder = CANDS.newBuilder();
		CANDS.CAND.Builder cand_builder = CANDS.CAND.newBuilder();
		
		if (inline.length() > 0) {
		    cand_builder.setContext(inline);
            cand_builder.setType(CANDS.TYPE.EXACT);
            cands_builder.addCandidate(cand_builder);
		}

		CANDS cands = cands_builder.build();
		Log.d("NEGI IME", cands.toString());
		
		inbytes.clear();
		outbytes.clear();
		
		byte[] src = cands.toByteArray();
		int src_size = src.length;

		Integer n = src_size;
		Log.d("NEGI IME IN", n.toString());
		
		inbytes.put(src, 0, src_size);
		inbytes.flip();

		Log.d("NEGI IME IN", inbytes.toString());

		return impl.sendrecieve(inbytes, src_size, outbytes, 4096);
	}
	
    @Override
	public int callback(int retSize) {
    	try {
			outbytes.position(retSize);
    		outbytes.flip();
    		byte[] b = new byte[(int)outbytes.remaining()];
    		outbytes.get(b);
			Log.d("NEGI IME OUT", b.toString());
			Log.d("NEGI IME OUT", outbytes.toString());
			CANDS out_cands = CANDS.parseFrom(b);
			return theInputMethodService.callback(out_cands);
		}
    	catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			Log.d("NEGI IME OUT error","error");
		}
    	return 0;
	}
}
