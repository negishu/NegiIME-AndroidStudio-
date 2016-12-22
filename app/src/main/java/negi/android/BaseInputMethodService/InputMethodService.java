package negi.android.BaseInputMethodService;

import negi.android.R;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.inputmethodservice.AbstractInputMethodService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ResultReceiver;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class InputMethodService extends AbstractInputMethodService {
    
    protected final StringBuilder mComposing = new StringBuilder(0);

    private InputMethodManager mImm;

    static final String TAG = "InputMethodService";
    int mTheme = android.R.style.Theme_InputMethod;

    protected int mLastDisplayWidth;
    LayoutInflater mInflater;
    TypedArray mThemeAttrs;
    protected View mRootView;
    InputWindow mWindow;
    boolean mInitialized;
    boolean mWindowVisible;
    boolean mWindowWasVisible;
    boolean mInShowWindow;

    FrameLayout mExtractFrame;
    FrameLayout mCandidatesFrame;
    InputAreaFrameLayout mInputFrame;

    IBinder mToken;
    InputBinding mInputBinding;
    InputConnection mInputConnection;
    
    boolean mInputStarted;
    boolean mInputViewStarted;
    boolean mCandidatesViewStarted;
    InputConnection mStartedInputConnection;
    EditorInfo mInputEditorInfo;

    int mShowInputFlags;
    boolean mShowInputRequested;
    
    CompletionInfo[] mCurCompletions;

    protected negi.android.BaseInputMethodService.InputView mInputView;
    protected negi.android.BaseInputMethodService.InputView mCandidateView;
    
    boolean mIsInputViewShown;

    int mStatusIcon;

    final int[] mTmpLocation = new int[2];

    public class InputMethodImpl extends AbstractInputMethodImpl {
        @Override public void attachToken(IBinder token) {
            if (null == mToken) {
                mToken = token;
                mWindow.setToken(token);
            }
        }
        @Override public void bindInput(InputBinding binding) {
            final InputConnection ic;
            mInputBinding = binding;
            mInputConnection = binding.getConnection();
            ic = getCurrentInputConnection();
            if (null != ic) {
                ic.reportFullscreenMode(false);
            }
            initialize();
            onBindInput();
        }
        @Override public void unbindInput() {
            onUnbindInput();
            mInputStarted = false;
            mInputBinding = null;
            mInputConnection = null;
        }
        @Override public void startInput(InputConnection ic, EditorInfo attribute) {
            doStartInput(ic, attribute, false);
        }
        @Override public void restartInput(InputConnection ic, EditorInfo attribute) {
            doStartInput(ic, attribute, true);
        }
        @Override public void hideSoftInput(int flags, ResultReceiver resultReceiver) {
            final boolean wasVis = isInputViewShown();
            mShowInputFlags = 0;
            mShowInputRequested = false;
            hideWindow();
            if (null != resultReceiver) {
                resultReceiver
                        .send((wasVis != isInputViewShown()) ? InputMethodManager.RESULT_HIDDEN
                                : (wasVis ? InputMethodManager.RESULT_UNCHANGED_SHOWN : InputMethodManager.RESULT_UNCHANGED_HIDDEN),
                                null);
            }
        }
        @Override public void showSoftInput(int flags, ResultReceiver resultReceiver) {
            final boolean wasVis = isInputViewShown();
            mShowInputFlags = 0;
            if (onShowInputRequested(flags, false)) {
                showWindow(true);
            }
            if (null != resultReceiver) {
                resultReceiver
                        .send((wasVis != isInputViewShown()) ? InputMethodManager.RESULT_SHOWN
                                : (wasVis ? InputMethodManager.RESULT_UNCHANGED_SHOWN : InputMethodManager.RESULT_UNCHANGED_HIDDEN),
                                null);
            }
        }
        @SuppressLint("NewApi")
        @TargetApi(11)
        @Override public void changeInputMethodSubtype(InputMethodSubtype subtype) {
        }
    }
    
    @Override
    public AbstractInputMethodImpl onCreateInputMethodInterface() {
        return new InputMethodImpl();
    }

    public class InputMethodSessionImpl extends AbstractInputMethodSessionImpl {
        @Override public void finishInput() {
            if (!isEnabled()) {
                return;
            }
            InputMethodService.this.doFinishInput();
        }
        @Override public void displayCompletions(CompletionInfo[] completions) {
            if (!isEnabled()) {
                return;
            }
            mCurCompletions = completions;
            InputMethodService.this.onDisplayCompletions(completions);
        }
        @Override public void updateExtractedText(int token, ExtractedText text) {
            if (!isEnabled()) {
                return;
            }
            InputMethodService.this.onUpdateExtractedText(token, text);
        }
        @Override public void updateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {
            if (!isEnabled()) {
                return;
            }
            InputMethodService.this.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
        }
        @Override public void updateCursor(Rect newCursor) {
            if (!isEnabled()) {
                return;
            }
            InputMethodService.this.onUpdateCursor(newCursor);
        }
        @Override public void appPrivateCommand(String action, Bundle data) {
            if (!isEnabled()) {
                return;
            }
            InputMethodService.this.onAppPrivateCommand(action, data);
        }
        @Override public void toggleSoftInput(int showFlags, int hideFlags) {
            if (!isEnabled()) {
                return;
            }
            InputMethodService.this.onToggleSoftInput(showFlags, hideFlags);
        }
        @Override public void viewClicked(boolean focusChanged) {
            if (!isEnabled()) {
                return;
            }
            InputMethodService.this.onViewClicked(focusChanged);
        }
		@Override
		public void updateCursorAnchorInfo(CursorAnchorInfo arg0) {
		}
    }
    
    @Override
    public AbstractInputMethodSessionImpl onCreateInputMethodSessionInterface() {
        return new InputMethodSessionImpl();
    }

    @Override
    public void onCreate() {
        super.setTheme(mTheme);
        super.onCreate();
        mImm      = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mInflater = (LayoutInflater)     getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mWindow   = new InputWindow(this, mTheme);
        initViews();
    }
    
    @Override
    public boolean onKeyLongPress(int arg0, KeyEvent arg1) {
        return false;
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {
        return false;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mInputView     = null;
        mCandidateView = null;
        if (mWindow!=null) {
            mWindow.dismiss();
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        final boolean visible;
        final int showFlags;
        final boolean showingInput;
        final CompletionInfo[] completions;

        super.onConfigurationChanged(newConfig);
        visible = mWindowVisible;
        showFlags = mShowInputFlags;
        showingInput = mShowInputRequested;
        completions = mCurCompletions;
        initViews();
        mInputViewStarted = false;
        mCandidatesViewStarted = false;
        
        if (mInputStarted) {
            doStartInput(getCurrentInputConnection(), getCurrentInputEditorInfo(), true);
        }
        
        if (visible) {
            if (showingInput) {
                if (onShowInputRequested(showFlags, true)) {
                    showWindow(true);
                    if (null != completions) {
                        mCurCompletions = completions;
                        onDisplayCompletions(completions);
                    }
                } else {
                    hideWindow();
                }
            } else {
                hideWindow();
            }
        }
    }
        
    @SuppressWarnings("deprecation")
    public int getMaxWidth() {
        final WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }
    public InputBinding getCurrentInputBinding() {
        return mInputBinding;
    }
    public InputConnection getCurrentInputConnection() {
        final InputConnection ic = mStartedInputConnection;
        if (null != ic) {
            return ic;
        }
        return mInputConnection;
    }
    public boolean getCurrentInputStarted() {
        return mInputStarted;
    }
    public EditorInfo getCurrentInputEditorInfo() {
        return mInputEditorInfo;
    }
    public boolean isShowInputRequested() {
        return mShowInputRequested;
    }
    public boolean isInputViewShown() {
        return mIsInputViewShown && mWindowVisible;
    }
    public boolean onEvaluateInputViewShown() {
        final Configuration config = getResources().getConfiguration();
        return config.keyboard == Configuration.KEYBOARD_NOKEYS || config.hardKeyboardHidden == Configuration.KEYBOARDHIDDEN_YES;
    }
    public void showStatusIcon(int iconResId) {
        mStatusIcon = iconResId;
        mImm.showStatusIcon(mToken, getPackageName(), iconResId);
    }
    public void hideStatusIcon() {
        mStatusIcon = 0;
        mImm.hideStatusIcon(mToken);
    }
    public void switchInputMethod(String id) {
        mImm.setInputMethod(mToken, id);
    }
    public void onInitializeInterface() {
        final int displayWidth = getMaxWidth();
        if (displayWidth == mLastDisplayWidth) {
            return;
        }
        mLastDisplayWidth = displayWidth;
    }
    public InputView onCreateInputView(int nParentSize, int nViewSize) {
        return null;
    }
    public InputView onCreateCandidatesView(int nParentSize, int nViewSize) {
        return null;
    }
    public void onStartInputView(EditorInfo info, boolean restarting) {
    }
    public void onFinishInputView(boolean finishingInput) {
    }
    public void onStartCandidatesView(EditorInfo info, boolean restarting) {
    }
    public void onFinishCandidatesView(boolean finishingInput) {
    }
    public boolean onShowInputRequested(int flags, boolean configChange) {
        final Configuration config;
        if (!onEvaluateInputViewShown()) {
            return false;
        }
        if (0 == (flags & InputMethod.SHOW_EXPLICIT)) {
            if (!configChange) {
                return false;
            }
            config = getResources().getConfiguration();
            if (config.keyboard != Configuration.KEYBOARD_NOKEYS) {
                return false;
            }
        }
        return true;
    }
    public void setTheme(int theme) {
        if (null != mWindow) {
            throw new IllegalStateException("Must be called before onCreate()");
        }
        mTheme = theme;
    }
    private void initialize() {
        if (!mInitialized) {
            mInitialized = true;
            onInitializeInterface();
        }
    }
    @SuppressLint("InflateParams")
	private void initViews() {
        mInitialized        = false;
        mShowInputRequested = false;
        mIsInputViewShown   = false;
        mThemeAttrs = obtainStyledAttributes(R.styleable.InputMethodService);
        mRootView = mInflater.inflate(R.layout.rootview, null);
        mRootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mWindow.setContentView(mRootView);
        mInputFrame      = (InputAreaFrameLayout) mRootView.findViewById(android.R.id.inputArea);
        mInputView       = null;
        mCandidateView   = null;
        mInputFrame.setVisibility(View.VISIBLE);
    }
    private void updateInputViewShown(int nParentSize) {
        if (null == mInputView) {
            final InputView v = onCreateInputView(nParentSize,nParentSize);
            if (null != v) {
                setInputView(v,nParentSize);
            }
        }
    }
    private void setInputView(InputView view, int nHeight) {
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, nHeight);
        layoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        layoutParams.topMargin    = 0;
        layoutParams.bottomMargin = 0;        
        mInputFrame.addView(view, 0, layoutParams);
        mInputView = view;
    }
    private void showWindow(boolean showInput) {
        if (mInShowWindow) {
            return;
        }
        try {
            mWindowWasVisible = mWindowVisible;
            mInShowWindow = true;
            showWindowInner(showInput);
        } finally {
            mWindowWasVisible = true;
            mInShowWindow = false;
        }
    }
    private void showWindowInner(boolean showInput) {
        boolean wasVisible = mWindowVisible;
        mWindowVisible = true;
        if (!mShowInputRequested) {
            if (mInputStarted) {
                if (showInput) {
                    mShowInputRequested = true;
                }
            }
        }
        initialize();
        
        int nMySize = 600;
        
        if (mShowInputRequested) {
            mWindow.setSize(nMySize);
            nMySize =  mWindow.getSize();
            final WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                Point real = new Point(0, 0);
                display.getRealSize(real);
                Point p = new Point();
                display.getSize(p);
                final int nNavigationBarHeight = real.y - p.y;
                final boolean isShown = mShowInputRequested && onEvaluateInputViewShown();
                if (mIsInputViewShown != isShown && mWindowVisible) {
                    mIsInputViewShown = isShown;
                    mInputFrame.setVisibility(isShown ? View.VISIBLE : View.GONE);
                    updateInputViewShown(nMySize - nNavigationBarHeight);
                }
            }
            else {
            
                final boolean isShown = mShowInputRequested && onEvaluateInputViewShown();
                if (mIsInputViewShown != isShown && mWindowVisible) {
                    mIsInputViewShown = isShown;
                    mInputFrame.setVisibility(isShown ? View.VISIBLE : View.GONE);
    	            updateInputViewShown(nMySize);
                }
            }
            
            if (!mInputViewStarted) {
                mInputViewStarted = true;
                onStartInputView(mInputEditorInfo, false);
            }
            if (!mCandidatesViewStarted) {
                mCandidatesViewStarted = true;
                onStartCandidatesView(mInputEditorInfo, false);
            }
        }
        if (!wasVisible) {
            onWindowShown();
            mWindow.show();
        }
    }
    private void hideWindow() {
        if (mInputViewStarted) {
            onFinishInputView(false);
        }
        if (mCandidatesViewStarted) {
            onFinishCandidatesView(false);
        }
        mInputViewStarted = false;
        mCandidatesViewStarted = false;
        if (mWindowVisible) {
            mWindow.hide();
            mWindowVisible = false;
            onWindowHidden();
            mWindowWasVisible = false;
        }
    }
    private void doStartInput(InputConnection ic, EditorInfo attribute, boolean restarting) {
        if (!restarting) {
            doFinishInput();
        }
        mInputStarted = true;
        mStartedInputConnection = ic;
        mInputEditorInfo = attribute;
        initialize();
        onStartInput(attribute, restarting);
        if (mWindowVisible) {
            if (mShowInputRequested) {
                mInputViewStarted = true;
                onStartInputView(mInputEditorInfo, restarting);
            }
        }
    }
    private void doFinishInput() {
        if (mInputViewStarted) {
            onFinishInputView(true);
        } 
        if (mCandidatesViewStarted) {
            onFinishCandidatesView(true);
        }
        mInputViewStarted      = false;
        mCandidatesViewStarted = false;
        if (mInputStarted) {
            onFinishInput();
        }
        mInputStarted           = false;
        mStartedInputConnection = null;
        mCurCompletions         = null;
    }
    private void onToggleSoftInput(int showFlags, int hideFlags) {
        if (isInputViewShown()) {
            requestHideSelf(hideFlags);
        } else {
            requestShowSelf(showFlags);
        }
    }

    public void onStartInput(EditorInfo attribute, boolean restarting) {
        mComposing.setLength(0);
    }
    public void onFinishInput() {
        mComposing.setLength(0);
    }
    public void onBindInput() {

    }
    public void onUnbindInput() {

    }
    public void onWindowShown() {

    }
    public void onWindowHidden() {

    }
    public void onDisplayCompletions(CompletionInfo[] completions) {

    }
    public void onUpdateExtractedText(int token, ExtractedText text) {

    }
    public void onViewClicked(boolean focusChanged) {

    }
    public void onUpdateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd, int candidatesStart, int candidatesEnd) {

    }
    public void onUpdateCursor(Rect newCursor) {

    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return false;
    }
    public boolean onTrackballEvent(MotionEvent event) {
        return false;
    }
    public void onAppPrivateCommand(String action, Bundle data) {
    }
    public void requestShowSelf(int flags) {
        mImm.showSoftInputFromInputMethod(mToken, flags);
    }
    public void requestHideSelf(int flags) {
        mImm.hideSoftInputFromInputMethod(mToken, flags);
    }
    
	private static final int MSG_UPDATEINLINE                   = 1000;
    private static final int DELAY_MSG_UPDATEINLINE             = 5;
	private static final int MSG_CONFIRMINLINE                  = 1001;
    private static final int DELAY_MSG_CONFIRMINLINE            = 5;
	private static final int MSG_CONFIRMINLINE_WITH_SPACE       = 1002;
    private static final int DELAY_MSG_CONFIRMINLINE_WITH_SPACE = 5;
	private static final int MSG_ADDSTRING                      = 1003;
    private static final int DELAY_MSG_ADDSTRING                = 5;
	private static final int MSG_BACKDELETE                     = 1004;
    private static final int DELAY_MSG_BACKDELETE               = 5;
	private static final int MSG_ADDSPACE                       = 1005;
    private static final int DELAY_MSG_ADDSPACE                 = 5;

    protected void _DoUpdateInline(final String string) {		
        ICUpdateComposingText(string);
	}

    protected void _DoSubmitConfirm(final String string) {
    	ICSubmitConfirmText(string);
	}

    protected void _DoSubmitConfirmWithSpace(final String string) {
    	ICSubmitConfirmText(" " + string);
	}

    protected void _DoBackDelete() {
    	ICSendKeyDown(KeyEvent.KEYCODE_DEL);
	}

    protected void _DoAddSpace() {
    	ICSubmitConfirmText(" ");
	}

	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATEINLINE:
        	_DoUpdateInline(msg.obj.toString());
            break;
            case MSG_CONFIRMINLINE:
        	_DoSubmitConfirm(msg.obj.toString());
            break;
            case MSG_CONFIRMINLINE_WITH_SPACE:
            _DoSubmitConfirmWithSpace(msg.obj.toString());
            break;
            case MSG_ADDSTRING:
            _DoSubmitConfirm(msg.obj.toString());
            break;
            case MSG_BACKDELETE:
        	_DoBackDelete();
            break;
            case MSG_ADDSPACE:
            _DoAddSpace();
            break;
            }
        }
    };

    public void UpdateInline(final String string) {
   		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATEINLINE,0,0,string), DELAY_MSG_UPDATEINLINE);
    }
    public void DoSubmitConfirm(final String string) {
   		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_CONFIRMINLINE,0,0,string), DELAY_MSG_CONFIRMINLINE);
    }
    public void DoSubmitConfirmWithSpace(final String string) {
   		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_CONFIRMINLINE_WITH_SPACE,0,0,string), DELAY_MSG_CONFIRMINLINE_WITH_SPACE);
    }
    public void DoAddString(final String string) {
   		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_ADDSTRING,0,0,string), DELAY_MSG_ADDSTRING);
    }
    public void DoBackDelete() {
   		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_BACKDELETE,0,0), DELAY_MSG_BACKDELETE);
    }
    public void DoAddSpace() {
   		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_ADDSPACE,0,0), DELAY_MSG_ADDSPACE);
    }
    public void ICUpdateComposingText(String composing) {
    }
    public void ICSubmitConfirmText(String confirming) {
    }
    public void ICSendKeyDown(int keyEventCode) {
    }
    public void ICSendKeyUp(int keyEventCode) {
    }
    public void ICSendKeyDownUp(int keyEventCode) {
    }
}
