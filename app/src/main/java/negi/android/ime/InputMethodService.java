package negi.android.ime;

import java.util.ArrayList;

import negi.android.NDK.SET.Message.CANDS;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;

public class InputMethodService extends negi.android.BasicInputMethodService.InputMethodService implements CallbackListener {

	NegiIME mNegiIME;
    
	private String            mInlineString = "";
	private ArrayList<String> mSuggestions = new ArrayList<String>();

	private final Handler theConvHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DoConvProcess(msg.what);
        }
    };
    private void SendConvMessage(final int nCode, final int milsec) {
        Message newMessage = theConvHandler.obtainMessage(nCode, 0, 0, null);
        theConvHandler.sendMessageDelayed(newMessage, milsec);
    }

    public InputMethodService() {
    }
    public InputMethodService(Context context) {
        attachBaseContext(context);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mNegiIME = new NegiIME(this);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
    }
    @Override
    public void onFinishInput() {
        super.onFinishInput();
    }
    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
    }
    @Override
    public void onFinishInputView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
    }
    @Override
    public void onStartCandidatesView(EditorInfo info, boolean restarting) {
        super.onStartCandidatesView(info,restarting);
    }
    @Override
    public void onFinishCandidatesView(boolean finishingInput) {
        super.onFinishInputView(finishingInput);
    }
    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);

        Log.d("NEGI IME", "oldSelStart = " + oldSelStart);
        Log.d("NEGI IME", "oldSelEnd = " + oldSelEnd);
        Log.d("NEGI IME", "newSelStart = " + newSelStart);
        Log.d("NEGI IME", "newSelEnd = " + newSelEnd);
        Log.d("NEGI IME", "candidatesStart = " + candidatesStart);
        Log.d("NEGI IME", "candidatesEnd = " + candidatesEnd);
  
    }
    @Override
    public void onDisplayCompletions(CompletionInfo[] completions) {
        super.onDisplayCompletions(completions);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    @Override
    public void onWindowShown() {
        super.onWindowShown();
    }
    @Override
    public void OnInputSwipeLeft() {
        DoBackDelete();
    }
    @Override
    public void OnInputSwipeRight() {
        DoAddSpace();
    }
    @Override
    public void OnInputSwipeUp() {
    }
    @Override
    public void OnInputSwipeDown() {
    }
    @Override
    public void OnInputChar(final String string) {
        mInlineString += string;
        DoPrivateUpdateInline(mInlineString);
    }
    @Override
    public void OnInputKeyDown(final int nKey) {
        if (nKey == 0x08) {
            DoPrivateBackDelete();
        }
    }
    @Override
    public void OnInputKeyUp(final int nKey) {
    }
    @Override
    public void OnInputKeyRepeat(final int nKey) {
        if (nKey == 0x08) {
            DoPrivateBackDelete();
        }
    }    
	@Override
	public int callback(CANDS cands) {
    	mSuggestions.clear();
		for (int i = 0; i < cands.getCandidateCount(); i++) {
			String s = cands.getCandidate(i).getContext();
	    	mSuggestions.add(s);
		}
        SetSuggestions(mSuggestions, true, true);
		return 1;
	}
    private boolean DoPrivateBackDelete() {
        int nLen = mInlineString.length();
        if (nLen > 0){
            mInlineString = mInlineString.substring(0, nLen-1);
            DoPrivateUpdateInline(mInlineString);
            return false;
        }
        DoBackDelete();
        return false;
    }
    private boolean DoPrivateUpdateInline(final String string) {
        UpdateInline(string);
        theConvHandler.removeMessages(0);
        SendConvMessage(0,500);
        return false;
    }
    private void DoPrivateCommunicate(final String string) {
        mNegiIME.communicate(string);
    }
    
    private void DoConvProcess(final int nWhat) {
        DoPrivateCommunicate(mInlineString);
    }
}
