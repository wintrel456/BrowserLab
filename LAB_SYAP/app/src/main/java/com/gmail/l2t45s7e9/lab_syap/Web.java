package com.gmail.l2t45s7e9.lab_syap;

import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

public class Web extends WebViewClient {
    private EditText urlRequest;

    public Web(EditText urlRequest) {
        this.urlRequest = urlRequest;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        view.loadUrl(request.getUrl().toString());
        urlRequest.setText(request.getUrl().toString());
        return false;
    }
}
