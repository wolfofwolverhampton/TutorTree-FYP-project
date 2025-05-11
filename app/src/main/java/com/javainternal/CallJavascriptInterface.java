package com.javainternal;

import android.webkit.JavascriptInterface;

public class CallJavascriptInterface {
    private final CallActivity activity;

    public CallJavascriptInterface(CallActivity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public void onPeerConnected() {
        activity.onPeerConnected();
    }

    @JavascriptInterface
    public void onPeerError(String error) {
        activity.onPeerError(error);
    }

    @JavascriptInterface
    public void onMediaError(String error) {
        activity.onMediaError(error);
    }
}