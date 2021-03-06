package thack.ac.wordnews;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class NewsItemActivity extends BaseActivity {
    String currentUrl = null;
    String source     = null;
    WebView     myWebView;
    ProgressBar progressBar;
    int currentProgress = 0;
    public final String TAG = ((Object) this).getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_item);

        currentUrl = getIntent().getExtras().getString("url");
        source = getIntent().getExtras().getString("source");

        View toolbarInclude = findViewById(R.id.my_toolbar);
        Toolbar myToolbar = (Toolbar) toolbarInclude.findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String title = String.format("%1$s: %2$s", getString(R.string.app_name), source == null ? "": source);
            actionBar.setTitle(title);
        }

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        final int screenWidth = metrics.widthPixels;
        final int screenHeight = metrics.heightPixels;
        Log.d(TAG, "width: " + screenWidth);
        Log.d(TAG, "height: " + screenHeight);

        progressBar = (ProgressBar) findViewById(R.id.progress);
        myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        WebViewClient myWebClient = new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

//                injectScriptFile(view, "js/old_app.js");
//                injectScriptFile(view, "js/jquery-2.1.1.min.js");
//
//                injectCSS(view, "gt_popup_css_compiled.css");
//                injectCSS(view, "gt_bubble_gss.css");
//                injectCSS(view, "slider/css/slider.css");

                injectScriptFile(view, "js/eventLogger.js");
                injectScriptFile(view, "js/common.js");
                injectScriptFile(view, "js/content-share.js");
                injectScriptFile(view, "js/learn.js");
                injectScriptFile(view, "js/annotate.js");
                injectScriptFile(view, "js/content-services.js");

                injectCSS( view, "css/gt_popup_css_compiled.css");
                injectCSS( view, "css/gt_bubble_gss.css");
                //injectCSSFile( view, "bootstrap/css/bootstrap.min.css");
                //injectCSSFile( view, "bootstrap/css/bootstrap-formhelpers.min.css");
                injectCSS( view, "css/content-share.css");

                Log.d(TAG, "Android ID: " + android_id);
                Log.d(TAG, "Display width in px is " + screenWidth);
                String jsScript = "javascript:setTimeout(initFromAndroid(), 0)";
                // init JavaScript
                view.loadUrl(jsScript);
            }
        };

        myWebView.setWebViewClient(myWebClient);
        myWebView.addJavascriptInterface(new NewsItemActivity.WebAppInterface(this), "Android");
        if(currentUrl != null) {
            Log.d(TAG, "URL: " + currentUrl);
            myWebView.loadUrl(currentUrl);
        } else {
            Toast.makeText(this, "No URL specified.", Toast.LENGTH_SHORT).show();
        }
    }

    // Inject CSS method: read style.css from assets folder
    // Append stylesheet to document head
    private void injectCSS(WebView view, String scriptFile) {
        try {
            InputStream inputStream = getAssets().open(scriptFile);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var style = document.createElement('style');" +
                    "style.type = 'text/css';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "style.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(style)" +
                    "})()");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void injectScriptFile(WebView view, String scriptFile) {
        InputStream input;
        try {
            input = getAssets().open(scriptFile);
            byte[] buffer = new byte[input.available()];
            input.read(buffer);
            input.close();

            // String-ify the script byte-array using BASE64 encoding !!!
            String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
            view.loadUrl("javascript:(function() {" +
                    "var parent = document.getElementsByTagName('head').item(0);" +
                    "var script = document.createElement('script');" +
                    "script.type = 'text/javascript';" +
                    // Tell the browser to BASE64-decode the string into your script !!!
                    "script.innerHTML = window.atob('" + encoded + "');" +
                    "parent.appendChild(script)" +
                    "})()");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void setProgress(int progress) {
            Log.e(TAG, "Progress: " + progress);
            if(currentProgress != 100 && progress == 100) {
                currentProgress = progress;
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "Remove loading bar");
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                }, 3000);

            }
        }

        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }

        @JavascriptInterface
        public void showDialog(String text) {
            new AlertDialog.Builder(mContext)
                    .setTitle("From Web page")
                    .setMessage(text)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
            myWebView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }
}
