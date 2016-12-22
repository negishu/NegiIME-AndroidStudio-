package negi.android.ime;

import negi.android.NDK.SET.Message.CANDS;

public interface CallbackListener {
    public int callback(CANDS cands);
};
