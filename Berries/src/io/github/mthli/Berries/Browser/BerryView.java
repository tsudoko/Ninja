package io.github.mthli.Berries.Browser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.MailTo;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import io.github.mthli.Berries.R;
import io.github.mthli.Berries.Unit.BrowserUnit;
import io.github.mthli.Berries.Unit.IntentUnit;

import java.net.URISyntaxException;

public class BerryView extends WebView {
    private Context context;

    private boolean foreground;
    public boolean isForeground() {
        return foreground;
    }
    public void setForeground(boolean foreground) {
        this.foreground = foreground;
    }

    private boolean incognito;
    public boolean isIncognito() {
        return incognito;
    }
    public void setIncognito(boolean incognito) {
        this.incognito = incognito;
    }

    private Tab tab;
    public Tab getTab() {
        return tab;
    }

    private BerryWebViewClient webViewClient;
    private BerryWebChromeClient webChromeClient;
    private BerryClickHandler clickHandler;
    private GestureDetector gestureDetector;

    private BrowserController controller;
    public BrowserController getController() {
        return controller;
    }
    public void setController(BrowserController controller) {
        this.controller = controller;
    }

    public BerryView(Context context) {
        super(new BerryContextWrapper(context));

        this.context = new BerryContextWrapper(context);
        this.foreground = false;
        this.incognito = false;

        this.tab = new Tab(this);
        this.webViewClient = new BerryWebViewClient(this);
        this.webChromeClient = new BerryWebChromeClient(this);
        this.clickHandler = new BerryClickHandler(this);
        this.gestureDetector = new GestureDetector(context, new BerryGestureListener(this));

        this.initWebView();
        this.initWebSettings();
        this.initPreferences();
    }

    public BerryView(Context context, AttributeSet attrs) {
        super(new BerryContextWrapper(context), attrs);

        this.context = new BerryContextWrapper(context);
        this.foreground = false;
        this.incognito = false;

        this.tab = new Tab(this);
        this.webViewClient = new BerryWebViewClient(this);
        this.webChromeClient = new BerryWebChromeClient(this);
        this.clickHandler = new BerryClickHandler(this);
        this.gestureDetector = new GestureDetector(context, new BerryGestureListener(this));

        this.initWebView();
        this.initWebSettings();
        this.initPreferences();
    }

    public BerryView(Context context, AttributeSet attrs, int defStyle) {
        super(new BerryContextWrapper(context), attrs, defStyle);

        this.context = new BerryContextWrapper(context);
        this.foreground = false;
        this.incognito = false;

        this.tab = new Tab(this);
        this.webViewClient = new BerryWebViewClient(this);
        this.webChromeClient = new BerryWebChromeClient(this);
        this.clickHandler = new BerryClickHandler(this);
        this.gestureDetector = new GestureDetector(context, new BerryGestureListener(this));

        this.initWebView();
        this.initWebSettings();
        this.initPreferences();
    }

    public BerryView(Context context, boolean incognito) {
        super(new BerryContextWrapper(context));

        this.context = new BerryContextWrapper(context);
        this.foreground = false;
        this.incognito = incognito;

        this.tab = new Tab(this);
        this.webViewClient = new BerryWebViewClient(this);
        this.webChromeClient = new BerryWebChromeClient(this);
        this.clickHandler = new BerryClickHandler(this);
        this.gestureDetector = new GestureDetector(context, new BerryGestureListener(this));

        this.initWebView();
        this.initWebSettings();
        this.initPreferences();
    }

    private synchronized void initWebView() {
        setAlwaysDrawnWithCacheEnabled(true);
        setAnimationCacheEnabled(true);

        setBackground(null);
        getRootView().setBackground(null);
        setBackgroundColor(context.getResources().getColor(R.color.white));

        setDrawingCacheBackgroundColor(0x00000000);
        setDrawingCacheEnabled(true);

        setFocusable(true);
        setFocusableInTouchMode(true);

        setSaveEnabled(true);
        setScrollbarFadingEnabled(true);

        setWillNotCacheDrawing(false);

        // TODO
        setWebViewClient(webViewClient);
        setWebChromeClient(webChromeClient);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return false;
            }
        });
    }

    private synchronized void initWebSettings() {
        WebSettings webSettings = getSettings();

        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        webSettings.setAppCacheEnabled(true);
        webSettings.setAppCachePath(context.getCacheDir().toString());
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webSettings.setDefaultTextEncodingName(BrowserUnit.URL_ENCODING);

        webSettings.setGeolocationDatabasePath(context.getFilesDir().toString());

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
    }

    private synchronized void initPreferences() {
        SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.sp_name), Context.MODE_PRIVATE);
        WebSettings webSettings = getSettings();

        webSettings.setBlockNetworkImage(sp.getBoolean(context.getString(R.string.sp_images), false));
        webSettings.setJavaScriptEnabled(sp.getBoolean(context.getString(R.string.sp_javascript), true));
        webSettings.setGeolocationEnabled(sp.getBoolean(context.getString(R.string.sp_location), true));
        webSettings.setSaveFormData(sp.getBoolean(context.getString(R.string.sp_passwords), true));
        webSettings.setSupportMultipleWindows(sp.getBoolean(context.getString(R.string.sp_multiple_window), true));

        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setTextZoom(100);
        webSettings.setUseWideViewPort(true);
    }

    @Override
    public synchronized void loadUrl(String url) {
        if (url == null || url.isEmpty()) {
            return;
        }

        if (url.startsWith(BrowserUnit.URL_SCHEME_MAIL_TO)) {
            Intent intent = IntentUnit.getEmailIntent(MailTo.parse(url));
            context.startActivity(intent);
            reload();
            return;
        } else if (url.startsWith(BrowserUnit.URL_SCHEME_INTENT)) {
            Intent intent;
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                context.startActivity(intent);
            } catch (URISyntaxException u) {}
            return;
        }

        super.loadUrl(url);
        if (controller != null && foreground) {
            controller.updateBookmarkButton();
        }
    }

    public synchronized void activate() {
        setVisibility(View.VISIBLE);
        requestFocus();
        foreground = true;
        tab.activate();
    }

    public synchronized void deactivate() {
        clearFocus();
        setVisibility(View.INVISIBLE);
        foreground = false;
        tab.deactivate();
    }

    public synchronized void update(String title, String url) {
        tab.setTitle(title);
        if (foreground) {
            controller.updateBookmarkButton();
            controller.updateInputBox(url);
        }
    }

    public synchronized void update(int progress) {
        if (foreground) {
            controller.updateProgress(progress);
        }
    }

    public synchronized void pause() {
        onPause();
        pauseTimers();
    }

    public synchronized void resume() {
        onResume();
        resumeTimers();
    }

    public synchronized void destroy() {
        stopLoading();
        onPause();
        clearHistory();
        setVisibility(View.GONE);
        removeAllViews();
        destroyDrawingCache();
    }

    public boolean isLoadFinish() {
        return getProgress() >= BrowserUnit.PROGRESS_MAX;
    }

    public void onLongPress() {
        Message click = clickHandler.obtainMessage();
        if (click != null) {
            click.setTarget(clickHandler);
        }
        requestFocusNodeHref(click);
    }
}