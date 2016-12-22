package negi.android.BasicInputMethodService;

import java.util.ArrayList;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

public class InputMethodService extends negi.android.BaseInputMethodService.InputMethodService implements InputListener {
    @Override
    public negi.android.BaseInputMethodService.InputView onCreateInputView(int nParentSize, int nViewSize) {
        mInputView = new InputView(this, nParentSize, nViewSize);
        mInputView.setService(this);
        return mInputView;
    }
    @Override
    public negi.android.BaseInputMethodService.InputView onCreateCandidatesView(int nParentSize, int nViewSize) {
        return null;
    }
    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        InputView view = (InputView)mInputView;
        if (view != null) {
            view.SetInputListner(this);
        }
    }
    @Override
    public void onStartCandidatesView(EditorInfo info, boolean restarting) {
    }
    public void SetSuggestions(final ArrayList<String> aSuggestions, boolean completions, boolean typedWordValid) {
        if (null != mInputView) {
            mInputView.SetSuggestions(aSuggestions, completions, typedWordValid);
        }
    }
    @Override
    public synchronized void ICUpdateComposingText(String composing) {
        mComposing.setLength(0);
        mComposing.insert(0, composing);
        if (mComposing.length() > 0) {
            final InputConnection ic = getCurrentInputConnection();
            ic.beginBatchEdit();
            ic.setComposingText(mComposing, 1);
            ic.endBatchEdit();
        }
        else {
            final InputConnection ic = getCurrentInputConnection();
            ic.beginBatchEdit();
            ic.setComposingText("", 1);
            ic.endBatchEdit();
        }
    }
    @Override
    public synchronized void ICSubmitConfirmText(String confirming) {
        mComposing.setLength(0);
        mComposing.insert(0, confirming);
        if (mComposing.length() > 0) {
            final InputConnection ic = getCurrentInputConnection();
            ic.beginBatchEdit();
        	ic.commitText(mComposing, 1);
            ic.setComposingText("", 1);
            ic.endBatchEdit();
            mComposing.setLength(0);
        }
    }
    @Override
    public synchronized void ICSendKeyDown(int keyEventCode) {
        final InputConnection ic = getCurrentInputConnection();
        ic.beginBatchEdit();
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        ic.endBatchEdit();
    }
    @Override
    public synchronized void ICSendKeyUp(int keyEventCode) {
        final InputConnection ic = getCurrentInputConnection();
        ic.beginBatchEdit();
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
        ic.endBatchEdit();
    }
    @Override
    public synchronized void ICSendKeyDownUp(int keyEventCode) {
        final InputConnection ic = getCurrentInputConnection();
        ic.beginBatchEdit();
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
        ic.endBatchEdit();
    }
    @Override
    public void OnInputTouch(final int action, final int offsetX, final int offsetY) {
    }
    @Override
    public void OnInputSwipeLeft() {
    }
    @Override
    public void OnInputSwipeRight() {
    }
    @Override
    public void OnInputSwipeUp() {
    }
    @Override
    public void OnInputSwipeDown() {
    }
    @Override
    public void OnInputChar(final String string) {
    }
    @Override
    public void OnInputKeyUp(final int nKey) {
    }
    @Override
    public void OnInputKeyDown(final int nKey) {
    }
    @Override
    public void OnInputKeyRepeat(final int nKey) {
    }
}
